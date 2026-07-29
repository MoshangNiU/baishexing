package com.yunlan.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunlan.config.WechatPayClient;
import com.yunlan.config.WechatPayProperties;
import com.yunlan.dto.CancelOrderDTO;
import com.yunlan.dto.OrderDetailVO;
import com.yunlan.dto.OrderVO;
import com.yunlan.dto.PayOrderDTO;
import com.yunlan.dto.PlaceOrderDTO;
import com.yunlan.entity.*;
import com.yunlan.enums.OrderStatusEnum;
import com.yunlan.mapper.OrdersMapper;
import com.yunlan.mapper.CouponMapper;
import com.yunlan.mapper.CouponActivityMapper;
import com.yunlan.service.*;
import com.yunlan.utils.UserHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    private static final Logger log = LoggerFactory.getLogger(OrdersServiceImpl.class);

    @Resource
    private ServeItemService serveItemService;

    @Resource
    private ServeCategoryService serveCategoryService;

    @Resource
    private AddressBookService addressBookService;

    @Resource
    private NotificationService notificationService;

    @Resource
    private DistributionService distributionService;

    @Resource
    private CouponMapper couponMapper;

    @Resource
    private CouponActivityMapper couponActivityMapper;

    @Resource
    private UserService userService;

    @Resource
    private TradingService tradingService;

    @Resource
    private WechatPayClient wechatPayClient;

    @Resource
    private WechatPayProperties wechatPayProperties;

    private PrivateKey privateKey;

    private PrivateKey getPrivateKey() {
        if (privateKey != null) return privateKey;
        try {
            String keyContent = wechatPayProperties.getPrivateKeyContent();
            if (keyContent == null || keyContent.isEmpty()) {
                String path = wechatPayProperties.getPrivateKeyPath();
                if (path != null && !path.isEmpty()) {
                    java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(path);
                    if (is == null) is = getClass().getResourceAsStream(path);
                    if (is != null) {
                        keyContent = new java.io.BufferedReader(new java.io.InputStreamReader(is))
                                .lines().collect(java.util.stream.Collectors.joining("\n"));
                    }
                }
            }
            String pem = keyContent
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] encoded = Base64.getDecoder().decode(pem);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            privateKey = kf.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load WeChat Pay private key", e);
        }
        return privateKey;
    }

    private String buildPaySign(String appId, long timeStamp, String nonceStr, String packageStr) {
        String message = appId + "\n" + timeStamp + "\n" + nonceStr + "\n" + packageStr + "\n";
        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(getPrivateKey());
            sign.update(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(sign.sign());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate paySign", e);
        }
    }

    @Override
    @Transactional
    public Orders placeOrder(PlaceOrderDTO dto) {
        Long userId = UserHolder.get();
        Orders order = new Orders();
        order.setUserId(userId);
        order.setServeItemId(dto.getServeItemId());
        order.setServeCategoryId(dto.getServeCategoryId());
        order.setAddressId(dto.getAddressId());
        order.setCouponId(dto.getCouponId());
        order.setRemarks(dto.getRemarks());
        order.setPurNum(dto.getPurNum() != null ? dto.getPurNum() : 1);
        order.setStatus(OrderStatusEnum.PENDING_PAYMENT.getCode());
        order.setPaymentStatus(0);

        BigDecimal totalAmount = BigDecimal.ZERO;
        ServeItem item = serveItemService.getById(dto.getServeItemId());
        if (item != null && item.getPrice() != null) {
            totalAmount = item.getPrice().multiply(BigDecimal.valueOf(order.getPurNum()));
        }
        order.setTotalAmount(totalAmount);

        BigDecimal actualAmount = totalAmount;
        if (dto.getCouponId() != null) {
            try {
                Coupon coupon = couponMapper.selectById(dto.getCouponId());
                if (coupon != null && coupon.getStatus() == 1) {
                    CouponActivity act = couponActivityMapper.selectById(coupon.getActivityId());
                    if (act != null && act.getDiscountAmount() != null) {
                        actualAmount = totalAmount.subtract(act.getDiscountAmount());
                        if (actualAmount.compareTo(BigDecimal.ZERO) < 0) actualAmount = BigDecimal.ZERO;
                    }
                }
            } catch (Exception ignored) {}
        }
        order.setActualAmount(actualAmount);

        if (dto.getAddressId() != null) {
            AddressBook addr = addressBookService.getById(dto.getAddressId());
            if (addr != null) {
                order.setServeAddress(addr.getProvince() + addr.getCity() + addr.getDistrict() + addr.getDetailAddress());
                order.setContactsName(addr.getName());
                order.setContactsPhone(addr.getPhone());
            }
        } else {
            order.setContactsName(dto.getContactsName());
            order.setContactsPhone(dto.getContactsPhone());
        }
        if (dto.getServeStartTime() != null) {
            order.setServeStartTime(dto.getServeStartTime());
        }

        this.save(order);

        try {
            notificationService.createNotification(userId, "订单已创建",
                    "您的订单已创建，请尽快完成支付", "ORDER", order.getId());
        } catch (Exception ignored) {}

        return order;
    }

    @Override
    public List<OrderVO> getOrderPage(int page, int pageSize, Integer ordersStatus) {
        Long userId = UserHolder.get();
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId)
                .orderByDesc(Orders::getCreateTime)
                .last("LIMIT " + (page - 1) * pageSize + "," + pageSize);
        if (ordersStatus != null) {
            wrapper.eq(Orders::getStatus, mapFrontendStatusToBackend(ordersStatus));
        }
        return this.list(wrapper).stream().map(this::convertToOrderVO).collect(Collectors.toList());
    }

    @Override
    public OrderDetailVO getOrderDetail(Long id) {
        Long userId = UserHolder.get();
        Orders order = this.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在");
        }
        return convertToOrderDetailVO(order);
    }

    @Override
    public void cancelOrder(CancelOrderDTO dto) {
        Long userId = UserHolder.get();
        Orders order = this.getById(dto.getId());
        if (order == null || !order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在");
        }
        order.setStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setCancelReason(dto.getCancelReason());
        order.setCancelTime(LocalDateTime.now());
        this.updateById(order);

        try {
            notificationService.createNotification(userId, "订单已取消",
                    "您的订单 #" + order.getId() + " 已成功取消", "ORDER", order.getId());
        } catch (Exception ignored) {}
    }

    @Override
    public void hideOrder(Long id) {
        Long userId = UserHolder.get();
        Orders order = this.getById(id);
        if (order != null && order.getUserId().equals(userId)) {
            this.removeById(id);
        }
    }

    @Override
    @Transactional
    public Map<String, Object> payOrder(Long id, PayOrderDTO dto) {
        Long userId = UserHolder.get();
        Orders order = this.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在");
        }

        Map<String, Object> result = new HashMap<>();
        if (order.getPaymentStatus() == 1) {
            result.put("payStatus", 1);
            return result;
        }

        // 获取 openId：请求参数优先，否则从用户表读取
        String openId = dto.getOpenId();
        if (openId == null || openId.isEmpty()) {
            User user = userService.getById(userId);
            if (user != null) openId = user.getOpenid();
        }
        if (openId == null || openId.isEmpty()) {
            throw new IllegalArgumentException("缺少微信openId，请在小程序环境登录");
        }

        // 计算实付金额（含优惠券抵扣逻辑）
        BigDecimal actualAmount = computeActualAmount(order, dto.getCouponId());

        // 调用微信支付 JSAPI 下单
        Long tradingOrderNo = IdUtil.getSnowflakeNextId();
        int totalFen = actualAmount.multiply(BigDecimal.valueOf(100)).intValue();

        JSONObject wxReq = JSONUtil.createObj()
                .set("mchid", wechatPayProperties.getMchId())
                .set("appid", wechatPayProperties.getAppId())
                .set("description", "服务订单-" + order.getId())
                .set("notify_url", wechatPayProperties.getNotifyUrl())
                .set("out_trade_no", String.valueOf(tradingOrderNo))
                .set("amount", JSONUtil.createObj().set("total", Math.max(totalFen, 1)).set("currency", "CNY"))
                .set("payer", JSONUtil.createObj().set("openid", openId));

        JSONObject wxResp = wechatPayClient.doPost("/v3/pay/transactions/jsapi", wxReq);
        int httpCode = wxResp.getInt("_status_code");
        if (httpCode < 200 || httpCode >= 300) {
            log.error("微信JSAPI下单失败, httpCode={}, resp={}", httpCode, wxResp);
            throw new RuntimeException("微信支付下单失败: " + wxResp.getStr("message", "未知错误"));
        }

        String prepayId = wxResp.getStr("prepay_id");
        String packageStr = "prepay_id=" + prepayId;
        String nonceStr = IdUtil.fastSimpleUUID();
        long timeStamp = System.currentTimeMillis() / 1000;
        String paySign = buildPaySign(wechatPayProperties.getAppId(), timeStamp, nonceStr, packageStr);

        JSONObject placeOrderJson = JSONUtil.createObj()
                .set("timeStamp", String.valueOf(timeStamp))
                .set("nonceStr", nonceStr)
                .set("package", packageStr)
                .set("signType", "RSA")
                .set("paySign", paySign);

        // 保存交易记录（含 prepayId / tradeType 便于后续查询）
        Trading trading = new Trading();
        trading.setOrderId(order.getId());
        trading.setUserId(userId);
        trading.setTotalAmount(actualAmount);
        trading.setStatus(0);
        trading.setPayChannel(1);
        trading.setTradingOrderNo(tradingOrderNo);
        trading.setPrepayId(prepayId);
        trading.setTradeType("JSAPI");
        trading.setPlaceOrderJson(placeOrderJson.toStringPretty());
        tradingService.save(trading);

        result.put("placeOrderJson", placeOrderJson.toStringPretty());
        result.put("tradingOrderNo", String.valueOf(tradingOrderNo));
        return result;
    }

    @Override
    @Transactional
    public String h5Pay(Long id, String clientIp) {
        Long userId = UserHolder.get();
        Orders order = this.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (order.getPaymentStatus() == 1) return null;

        if (clientIp == null || clientIp.isEmpty()) clientIp = "127.0.0.1";

        // 与 payOrder 保持一致的实付金额计算逻辑
        BigDecimal actualAmount = computeActualAmount(order, null);

        Long tradingOrderNo = IdUtil.getSnowflakeNextId();
        int totalFen = actualAmount.multiply(BigDecimal.valueOf(100)).intValue();

        JSONObject wxReq = JSONUtil.createObj()
                .set("mchid", wechatPayProperties.getMchId())
                .set("appid", wechatPayProperties.getAppId())
                .set("description", "服务订单-" + order.getId())
                .set("notify_url", wechatPayProperties.getNotifyUrl())
                .set("out_trade_no", String.valueOf(tradingOrderNo))
                .set("amount", JSONUtil.createObj().set("total", Math.max(totalFen, 1)).set("currency", "CNY"))
                .set("scene_info", JSONUtil.createObj()
                        .set("payer_client_ip", clientIp)
                        .set("h5_info", JSONUtil.createObj().set("type", "Wap")));

        JSONObject wxResp = wechatPayClient.doPost("/v3/pay/transactions/h5", wxReq);
        int httpCode = wxResp.getInt("_status_code");
        if (httpCode < 200 || httpCode >= 300) {
            log.error("微信H5支付下单失败, httpCode={}, resp={}", httpCode, wxResp);
            throw new RuntimeException("微信H5支付下单失败: " + wxResp.getStr("message", "未知错误"));
        }

        String h5Url = wxResp.getStr("h5_url");

        Trading trading = new Trading();
        trading.setOrderId(order.getId());
        trading.setUserId(userId);
        trading.setTotalAmount(actualAmount);
        trading.setStatus(0);
        trading.setPayChannel(1);
        trading.setTradingOrderNo(tradingOrderNo);
        trading.setClientIp(clientIp);
        trading.setTradeType("MWEB");
        trading.setPlaceOrderMsg(h5Url);
        tradingService.save(trading);

        return h5Url;
    }

    /**
     * 微信支付异步通知处理
     *
     * @param outTradeNo    业务交易单号 = trading.trading_order_no
     * @param transactionId 微信支付订单号，写入交易单用于后续对账
     */
    @Override
    @Transactional
    public void handlePayNotify(String outTradeNo, String transactionId) {
        Trading trading = tradingService.getOne(
                new LambdaQueryWrapper<Trading>().eq(Trading::getTradingOrderNo, outTradeNo), false);
        if (trading == null) {
            log.warn("PayNotify: trading not found for outTradeNo={}", outTradeNo);
            return;
        }

        Orders order = this.getById(trading.getOrderId());
        if (order == null) {
            log.warn("PayNotify: order not found for trading.orderId={}", trading.getOrderId());
            return;
        }
        if (order.getPaymentStatus() == 1) {
            // 已处理过的回调（微信可能重复通知），直接返回 SUCCESS 避免重试
            return;
        }

        Long userId = order.getUserId();

        // 1. 更新订单：支付状态 → 已支付，订单状态 → 待服务
        order.setPaymentStatus(1);
        order.setStatus(OrderStatusEnum.PENDING_SERVICE.getCode());
        this.updateById(order);

        // 2. 标记优惠券已使用
        if (order.getCouponId() != null) {
            try {
                Coupon coupon = couponMapper.selectById(order.getCouponId());
                if (coupon != null && coupon.getStatus() == 1) {
                    coupon.setStatus(2);
                    couponMapper.updateById(coupon);
                }
            } catch (Exception ignored) {}
        }

        // 3. 更新交易单：状态、微信支付单号、支付完成时间
        trading.setStatus(1);
        trading.setTransactionId(transactionId);
        trading.setPayTime(LocalDateTime.now());
        tradingService.updateById(trading);

        // 4. 发送站内通知
        try {
            notificationService.createNotification(userId, "支付成功",
                    "您的订单 #" + order.getId() + " 已支付成功，等待安排服务", "ORDER", order.getId());
        } catch (Exception ignored) {}

        // 5. 分销返利（邀请人返利 5%）
        try {
            User currentUser = userService.getById(userId);
            if (currentUser != null && currentUser.getInviterId() != null) {
                BigDecimal rebateAmount = order.getActualAmount()
                        .multiply(new BigDecimal("0.05"))
                        .setScale(2, BigDecimal.ROUND_HALF_UP);
                if (rebateAmount.compareTo(BigDecimal.ZERO) > 0) {
                    distributionService.createRebateForOrder(
                            order.getId(), userId, currentUser.getInviterId(), rebateAmount);
                }
            }
        } catch (Exception ignored) {}
    }

    private BigDecimal computeActualAmount(Orders order, Long couponId) {
        Long finalCouponId = order.getCouponId();
        if (couponId != null) finalCouponId = couponId;

        BigDecimal actualAmount = order.getTotalAmount();
        if (finalCouponId != null) {
            try {
                Coupon coupon = couponMapper.selectById(finalCouponId);
                if (coupon != null && coupon.getStatus() == 1) {
                    CouponActivity act = couponActivityMapper.selectById(coupon.getActivityId());
                    if (act != null && act.getDiscountAmount() != null) {
                        actualAmount = order.getTotalAmount().subtract(act.getDiscountAmount());
                        if (actualAmount.compareTo(BigDecimal.ZERO) < 0) actualAmount = BigDecimal.ZERO;
                    }
                }
            } catch (Exception ignored) {}
        }
        return actualAmount;
    }

    @Override
    public Map<String, Object> getPayResult(Long id) {
        Long userId = UserHolder.get();
        Orders order = this.getById(id);
        Map<String, Object> result = new HashMap<>();
        if (order != null && order.getUserId().equals(userId)) {
            result.put("status", order.getPaymentStatus());
            result.put("payStatus", order.getPaymentStatus() == 1 ? 1 : 0);
        }
        return result;
    }

    @Override
    public List<OrderVO> consumerQueryList(Long lastId, Integer pageSize, Integer ordersStatus) {
        Long userId = UserHolder.get();
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId)
                .orderByDesc(Orders::getId);

        if (lastId != null && lastId > 0) wrapper.lt(Orders::getId, lastId);
        if (ordersStatus != null) wrapper.eq(Orders::getStatus, mapFrontendStatusToBackend(ordersStatus));
        wrapper.last("LIMIT " + (pageSize != null ? pageSize : 10));
        return this.list(wrapper).stream().map(this::convertToOrderVO).collect(Collectors.toList());
    }

    @Override
    public OrderVO convertToOrderVO(Orders order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setServeItemId(order.getServeItemId());
        vo.setOrdersStatus(mapBackendStatusToFrontend(order));
        vo.setPayStatus(order.getPaymentStatus());
        vo.setPurNum(order.getPurNum());
        vo.setServeAddress(order.getServeAddress());
        vo.setContactsName(order.getContactsName());
        vo.setContactsPhone(order.getContactsPhone());
        vo.setServerName(order.getServerName());
        vo.setServeStartTime(order.getServeStartTime());
        vo.setServeActualEndTime(order.getServeActualEndTime());
        vo.setCancelTime(order.getCancelTime());
        vo.setCancelReason(order.getCancelReason());
        vo.setPrice(order.getTotalAmount());
        vo.setRealPayAmount(order.getActualAmount());
        vo.setCreateTime(order.getCreateTime() != null
                ? order.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);

        if (order.getServeItemId() != null) {
            ServeItem item = serveItemService.getById(order.getServeItemId());
            if (item != null) {
                vo.setServeItemImg(item.getImage());
                vo.setServeItemName(item.getName());
                vo.setUnit(item.getUnit());
            }
        }
        if (order.getServeCategoryId() != null) {
            ServeCategory cat = serveCategoryService.getById(order.getServeCategoryId());
            if (cat != null) vo.setServeTypeName(cat.getName());
        }
        return vo;
    }

    private OrderDetailVO convertToOrderDetailVO(Orders order) {
        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(order.getId());
        vo.setServeItemId(order.getServeItemId());
        vo.setOrdersStatus(mapBackendStatusToFrontend(order));
        vo.setPayStatus(order.getPaymentStatus());
        vo.setPurNum(order.getPurNum());
        vo.setServeAddress(order.getServeAddress());
        vo.setContactsName(order.getContactsName());
        vo.setContactsPhone(order.getContactsPhone());
        vo.setServerName(order.getServerName());
        vo.setServeStartTime(order.getServeStartTime());
        vo.setServeActualEndTime(order.getServeActualEndTime());
        vo.setCancelReason(order.getCancelReason());
        vo.setRemarks(order.getRemarks());
        vo.setCancelTime(order.getCancelTime() != null
                ? order.getCancelTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        vo.setPrice(order.getTotalAmount());
        vo.setRealPayAmount(order.getActualAmount());
        vo.setCreateTime(order.getCreateTime());

        if (order.getServeItemId() != null) {
            ServeItem item = serveItemService.getById(order.getServeItemId());
            if (item != null) {
                vo.setServeItemImg(item.getImage());
                vo.setServeItemName(item.getName());
                vo.setUnit(item.getUnit());
            }
        }
        if (order.getServeCategoryId() != null) {
            ServeCategory cat = serveCategoryService.getById(order.getServeCategoryId());
            if (cat != null) vo.setServeTypeName(cat.getName());
        }
        return vo;
    }

    private Integer mapBackendStatusToFrontend(Orders order) {
        Integer status = order.getStatus();
        Integer payStatus = order.getPaymentStatus();
        if (status == 0) return 0;
        if (status == 1 && payStatus == 1) return 200;
        if (status == 2) return 300;
        if (status == 3) return 400;
        if (status == 4) return 600;
        if (status == 1 && payStatus == 0) return 100;
        return 0;
    }

    private Integer mapFrontendStatusToBackend(Integer frontendStatus) {
        if (frontendStatus == null) return null;
        if (frontendStatus == 0) return 0;
        if (frontendStatus == 100 || frontendStatus == 200) return 1;
        if (frontendStatus == 300) return 2;
        if (frontendStatus == 400 || frontendStatus == 500) return 3;
        if (frontendStatus == 600 || frontendStatus == 700) return 4;
        return null;
    }
}

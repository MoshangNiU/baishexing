package com.yunlan.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunlan.dto.LoginDTO;
import com.yunlan.dto.RealNameVerifyDTO;
import com.yunlan.dto.UserDTO;
import com.yunlan.dto.UserProfileVO;
import com.yunlan.entity.Evaluation;
import com.yunlan.entity.User;
import com.yunlan.entity.Orders;
import com.yunlan.mapper.EvaluationMapper;
import com.yunlan.mapper.CouponMapper;
import com.yunlan.mapper.OrdersMapper;
import com.yunlan.mapper.UserMapper;
import com.yunlan.service.UserService;
import com.yunlan.utils.JwtUtils;
import com.yunlan.utils.UserHolder;
import com.yunlan.service.SmsService;
import com.yunlan.service.WechatService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private EvaluationMapper evaluationMapper;

    @Resource
    private CouponMapper couponMapper;

    @Resource
    private OrdersMapper ordersMapper;

    @Resource
    private SmsService smsService;

    @Resource
    private WechatService wechatService;

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        if (StrUtil.isBlank(dto.getPhone()) && StrUtil.isNotBlank(dto.getCode())) {
            // 微信登录无手机号时生成临时用户
            dto.setPhone("tmp_" + System.currentTimeMillis());
        }
        if (StrUtil.isBlank(dto.getPhone())) {
            dto.setPhone("tmp_" + System.currentTimeMillis());
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        User user = this.getOne(wrapper);

        if (user == null) {
            user = new User();
            user.setNickname(dto.getNickname() != null ? dto.getNickname() : "用户" + RandomUtil.randomNumbers(6));
            user.setAvatar(dto.getAvatar());
            user.setPhone(dto.getPhone());
            user.setStatus(1);
            this.save(user);
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new IllegalStateException("该手机号已被冻结");
        }

        String token = JwtUtils.generateToken(user.getId());
        user.setToken(token);
        this.updateById(user);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("nickName", user.getNickname());
        return result;
    }

    @Override
    public Map<String, Object> register(LoginDTO dto) {
        if (StrUtil.isBlank(dto.getPhone()) || StrUtil.isBlank(dto.getPhoneCode())) {
            throw new IllegalArgumentException("手机号和验证码不能为空");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        User existing = this.getOne(wrapper);
        if (existing != null) {
            throw new IllegalArgumentException("该手机号已注册");
        }

        User user = new User();
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : "用户" + dto.getPhone().substring(Math.max(0, dto.getPhone().length() - 4)));
        user.setAvatar(dto.getAvatar());
        user.setPhone(dto.getPhone());
        user.setPassword(dto.getPassword());
        user.setStatus(1);
        this.save(user);

        String token = JwtUtils.generateToken(user.getId());
        user.setToken(token);
        this.updateById(user);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("nickName", user.getNickname());
        result.put("id", user.getId());
        return result;
    }

    @Override
    public Map<String, Object> passwordLogin(LoginDTO dto) {
        if (StrUtil.isBlank(dto.getPhone()) || StrUtil.isBlank(dto.getPassword())) {
            throw new IllegalArgumentException("手机号和密码不能为空");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        User user = this.getOne(wrapper);

        if (user == null) {
            throw new IllegalArgumentException("手机号未注册");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new IllegalStateException("该账号已被冻结");
        }
        if (!dto.getPassword().equals(user.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }

        String token = JwtUtils.generateToken(user.getId());
        user.setToken(token);
        this.updateById(user);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("nickName", user.getNickname());
        return result;
    }

    @Override
    public Map<String, Object> smsLogin(LoginDTO dto) {
        if (dto.getPhone() == null || dto.getPhoneCode() == null) {
            throw new IllegalArgumentException("手机号和验证码不能为空");
        }

        // Verify SMS code
        smsService.verifyCode(dto.getPhone(), dto.getPhoneCode());

        // Find or create user
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        User user = this.getOne(wrapper);

        if (user == null) {
            // Auto-register
            user = new User();
            String nickname = dto.getNickname() != null ? dto.getNickname() :
                    "用户" + dto.getPhone().substring(Math.max(0, dto.getPhone().length() - 4));
            user.setNickname(nickname);
            // Default avatar using DiceBear (initials-based, consistent per phone)
            user.setAvatar("https://api.dicebear.com/7.x/initials/svg?seed=" + nickname + "&backgroundColor=ff9900");
            user.setPhone(dto.getPhone());
            user.setStatus(1);
            this.save(user);
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new IllegalStateException("该手机号已被冻结");
        }

        String token = JwtUtils.generateToken(user.getId());
        user.setToken(token);
        this.updateById(user);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("nickName", user.getNickname());
        result.put("avatar", user.getAvatar());
        result.put("id", user.getId());
        return result;
    }

    @Override
    public Map<String, Object> wechatLogin(LoginDTO dto) {
        if (dto.getCode() == null) {
            throw new IllegalArgumentException("微信临时code不能为空");
        }

        // Exchange code for openid via WeChat API
        String openid = wechatService.getOpenid(dto.getCode());

        // Find or create user by openid
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = this.getOne(wrapper);

        if (user == null) {
            // Auto-register with WeChat openid
            user = new User();
            String nickname = dto.getNickname() != null ? dto.getNickname() :
                    "用户" + openid.substring(Math.max(0, openid.length() - 6));
            user.setNickname(nickname);
            user.setAvatar(dto.getAvatar() != null ? dto.getAvatar() :
                    "https://api.dicebear.com/7.x/initials/svg?seed=" + nickname + "&backgroundColor=ff9900");
            user.setOpenid(openid);
            user.setStatus(1);
            this.save(user);
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new IllegalStateException("该账号已被冻结");
        }

        String token = JwtUtils.generateToken(user.getId());
        user.setToken(token);
        this.updateById(user);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("nickName", user.getNickname());
        result.put("avatar", user.getAvatar());
        result.put("id", user.getId());
        result.put("openid", openid);
        return result;
    }

    @Override
    public void getPhone(String phoneCode) {
        // 模拟获取手机号，实际对接微信/阿里云等
        // phoneCode 为临时code, 解密得到手机号后更新
    }

    @Override
    public User getCurrentUser() {
        Long userId = UserHolder.get();
        if (userId == null) return null;
        return this.getById(userId);
    }

    @Override
    public UserProfileVO getCurrentUserProfile() {
        Long userId = UserHolder.get();
        if (userId == null) return null;
        User user = this.getById(userId);
        if (user == null) return null;

        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());

        // Count evaluations
        Long evalCount = evaluationMapper.selectCount(
                new LambdaQueryWrapper<Evaluation>()
                        .eq(Evaluation::getUserId, userId)
                        .eq(Evaluation::getStatus, 1)
        );
        vo.setEvaluationCount(evalCount.intValue());
        Long couponCount = couponMapper.selectCount(
                new LambdaQueryWrapper<com.yunlan.entity.Coupon>()
                        .eq(com.yunlan.entity.Coupon::getUserId, userId)
                        .eq(com.yunlan.entity.Coupon::getStatus, 1)
        );
        vo.setCouponCount(couponCount != null ? couponCount.intValue() : 0);

        // Count orders
        Long orderCount = ordersMapper.selectCount(
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getUserId, userId)
        );
        vo.setOrderCount(orderCount != null ? orderCount.intValue() : 0);

        // Distribution info
        vo.setInviteCode(user.getInviteCode());
        vo.setBalance(user.getBalance());
        vo.setTotalRebate(user.getTotalRebate());

        return vo;
    }

    @Override
    public void updateUserInfo(UserDTO dto) {
        Long userId = UserHolder.get();
        User user = this.getById(userId);
        if (user == null) throw new IllegalArgumentException("用户不存在");
        if (StrUtil.isNotBlank(dto.getNickname())) user.setNickname(dto.getNickname());
        if (StrUtil.isNotBlank(dto.getAvatar())) user.setAvatar(dto.getAvatar());
        if (StrUtil.isNotBlank(dto.getPhone())) user.setPhone(dto.getPhone());
        this.updateById(user);
    }

    @Override
    public void verifyRealName(RealNameVerifyDTO dto) {
        Long userId = UserHolder.get();
        // 模拟实名认证，对接第三方认证服务
        User user = this.getById(userId);
        if (user == null) throw new IllegalArgumentException("用户不存在");
        // 保存实名信息（字段可扩展）
    }
}

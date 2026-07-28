-- trading表增加微信支付字段
ALTER TABLE `trading`
    ADD COLUMN IF NOT EXISTS `client_ip` VARCHAR(64) DEFAULT NULL COMMENT '客户端IP（H5支付）' AFTER `trading_order_no`,
    ADD COLUMN IF NOT EXISTS `place_order_json` TEXT DEFAULT NULL COMMENT 'JSAPI调起支付参数JSON' AFTER `client_ip`,
    ADD COLUMN IF NOT EXISTS `place_order_msg` VARCHAR(500) DEFAULT NULL COMMENT 'H5支付跳转URL' AFTER `place_order_json`;

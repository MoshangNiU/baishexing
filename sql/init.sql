-- yunlan.address_book definition

CREATE TABLE `address_book` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `user_id` bigint NOT NULL COMMENT '用户ID',
                                `name` varchar(50) DEFAULT NULL COMMENT '联系人',
                                `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
                                `province` varchar(50) DEFAULT NULL COMMENT '省份',
                                `city` varchar(50) DEFAULT NULL COMMENT '城市',
                                `district` varchar(50) DEFAULT NULL COMMENT '区县',
                                `detail_address` varchar(500) DEFAULT NULL COMMENT '详细地址',
                                `lat` varchar(50) DEFAULT NULL COMMENT '纬度',
                                `lng` varchar(50) DEFAULT NULL COMMENT '经度',
                                `is_default` tinyint DEFAULT '0' COMMENT '是否默认 1是 0否',
                                `label` varchar(50) DEFAULT NULL COMMENT '标签(家/公司)',
                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                `deleted` tinyint DEFAULT '0',
                                PRIMARY KEY (`id`),
                                KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='地址簿表';


-- yunlan.area definition

CREATE TABLE `area` (
                        `id` bigint NOT NULL AUTO_INCREMENT,
                        `parent_id` bigint DEFAULT '0' COMMENT '父级ID',
                        `name` varchar(50) DEFAULT NULL COMMENT '名称',
                        `level` tinyint DEFAULT NULL COMMENT '层级 1省 2市 3区/县',
                        PRIMARY KEY (`id`),
                        KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='省市区表';


-- yunlan.banner definition

CREATE TABLE `banner` (
                          `id` bigint NOT NULL AUTO_INCREMENT,
                          `title` varchar(100) DEFAULT '' COMMENT '标题',
                          `image` varchar(500) DEFAULT '' COMMENT '图片URL',
                          `color` varchar(20) DEFAULT '#ff9900' COMMENT '背景色',
                          `text` varchar(200) DEFAULT '' COMMENT '展示文字',
                          `link` varchar(500) DEFAULT '' COMMENT '跳转链接',
                          `sort` int DEFAULT '0' COMMENT '排序',
                          `status` tinyint DEFAULT '1' COMMENT '状态 1=启用 0=禁用',
                          `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                          `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='首页轮播图';


-- yunlan.coupon definition

CREATE TABLE `coupon` (
                          `id` bigint NOT NULL AUTO_INCREMENT,
                          `user_id` bigint NOT NULL COMMENT '用户ID',
                          `activity_id` bigint NOT NULL COMMENT '活动ID',
                          `status` tinyint DEFAULT '1' COMMENT '状态 1未使用 2已使用 3已过期',
                          `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                          `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户优惠券表';


-- yunlan.coupon_activity definition

CREATE TABLE `coupon_activity` (
                                   `id` bigint NOT NULL AUTO_INCREMENT,
                                   `name` varchar(100) DEFAULT NULL COMMENT '活动名称',
                                   `description` varchar(500) DEFAULT NULL COMMENT '活动描述',
                                   `discount_amount` decimal(10,2) DEFAULT '0.00' COMMENT '优惠金额',
                                   `condition_amount` decimal(10,2) DEFAULT '0.00' COMMENT '使用条件金额',
                                   `total_count` int DEFAULT '0' COMMENT '总数量',
                                   `remain_count` int DEFAULT '0' COMMENT '剩余数量',
                                   `start_time` datetime DEFAULT NULL COMMENT '开始时间',
                                   `end_time` datetime DEFAULT NULL COMMENT '结束时间',
                                   `status` tinyint DEFAULT '1' COMMENT '状态 1启用 0禁用',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='优惠券活动表';


-- yunlan.evaluation definition

CREATE TABLE `evaluation` (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `user_id` bigint NOT NULL COMMENT '用户ID',
                              `order_id` bigint DEFAULT NULL COMMENT '订单ID',
                              `serve_item_id` bigint DEFAULT NULL COMMENT '服务项目ID',
                              `content` text COMMENT '评价内容',
                              `pics` text COMMENT '图片,逗号分隔',
                              `star` tinyint DEFAULT '5' COMMENT '星级 1-5',
                              `like_count` int DEFAULT '0' COMMENT '点赞数',
                              `status` tinyint DEFAULT '1' COMMENT '状态 1显示 0隐藏',
                              `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                              `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              `deleted` tinyint DEFAULT '0',
                              PRIMARY KEY (`id`),
                              KEY `idx_serve_item` (`serve_item_id`),
                              KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评价表';


-- yunlan.notification definition

CREATE TABLE `notification` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `user_id` bigint NOT NULL COMMENT '用户ID',
                                `title` varchar(200) NOT NULL COMMENT '通知标题',
                                `content` text COMMENT '通知内容',
                                `type` varchar(20) DEFAULT 'SYSTEM' COMMENT '类型 ORDER/SYSTEM/PROMOTION',
                                `related_order_id` bigint DEFAULT NULL COMMENT '关联订单ID',
                                `is_read` tinyint DEFAULT '0' COMMENT '是否已读 0未读 1已读',
                                `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`),
                                KEY `idx_user_read` (`user_id`,`is_read`),
                                KEY `idx_user_time` (`user_id`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知消息表';


-- yunlan.orders definition

CREATE TABLE `orders` (
                          `id` bigint NOT NULL AUTO_INCREMENT,
                          `user_id` bigint NOT NULL COMMENT '用户ID',
                          `serve_item_id` bigint DEFAULT NULL COMMENT '服务项目ID',
                          `serve_category_id` bigint DEFAULT NULL COMMENT '服务分类ID',
                          `address_id` bigint DEFAULT NULL COMMENT '地址ID',
                          `status` tinyint DEFAULT '0' COMMENT '0待支付 1待服务 2服务中 3已完成 4已取消',
                          `payment_status` tinyint DEFAULT '0' COMMENT '支付状态 0未支付 1已支付',
                          `total_amount` decimal(10,2) DEFAULT '0.00' COMMENT '总金额',
                          `actual_amount` decimal(10,2) DEFAULT '0.00' COMMENT '实付金额',
                          `service_time` datetime DEFAULT NULL COMMENT '预约服务时间',
                          `remarks` varchar(500) DEFAULT NULL COMMENT '备注',
                          `coupon_id` bigint DEFAULT NULL COMMENT '使用的优惠券ID',
                          `pur_num` int DEFAULT '1' COMMENT '购买数量',
                          `serve_address` varchar(500) DEFAULT NULL COMMENT '服务地址',
                          `contacts_name` varchar(50) DEFAULT NULL COMMENT '联系人',
                          `contacts_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
                          `cancel_reason` varchar(500) DEFAULT NULL COMMENT '取消原因',
                          `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
                          `server_name` varchar(50) DEFAULT NULL COMMENT '服务人员姓名',
                          `serve_actual_end_time` datetime DEFAULT NULL COMMENT '实际服务完成时间',
                          `serve_start_time` datetime DEFAULT NULL COMMENT '预约服务开始时间',
                          `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                          `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          `deleted` tinyint DEFAULT '0',
                          PRIMARY KEY (`id`),
                          KEY `idx_user_status` (`user_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';


-- yunlan.rebate_record definition

CREATE TABLE `rebate_record` (
                                 `id` bigint NOT NULL AUTO_INCREMENT,
                                 `order_id` bigint NOT NULL COMMENT '订单ID',
                                 `user_id` bigint NOT NULL COMMENT '被邀请用户ID',
                                 `inviter_id` bigint NOT NULL COMMENT '邀请人ID',
                                 `amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '返利金额',
                                 `status` tinyint DEFAULT '0' COMMENT '状态 0待结算 1已结算 2已失效',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 PRIMARY KEY (`id`),
                                 KEY `idx_inviter` (`inviter_id`),
                                 KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='返利记录表';


-- yunlan.region definition

CREATE TABLE `region` (
                          `id` bigint NOT NULL AUTO_INCREMENT,
                          `city_code` varchar(20) DEFAULT NULL COMMENT '城市编码',
                          `city_name` varchar(50) DEFAULT NULL COMMENT '城市名称',
                          `province` varchar(50) DEFAULT NULL COMMENT '省份',
                          `province_code` varchar(20) DEFAULT NULL COMMENT '省份编码',
                          `active_status` tinyint DEFAULT '0' COMMENT '开通状态 1已开通',
                          `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                          `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='区域/开通城市表';


-- yunlan.serve_category definition

CREATE TABLE `serve_category` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `name` varchar(50) NOT NULL COMMENT '分类名称',
                                  `icon` varchar(500) DEFAULT NULL COMMENT '图标',
                                  `sort` int DEFAULT '0' COMMENT '排序',
                                  `status` tinyint DEFAULT '1' COMMENT '状态 1启用 0禁用',
                                  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务分类表';


-- yunlan.serve_item definition

CREATE TABLE `serve_item` (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `category_id` bigint NOT NULL COMMENT '分类ID',
                              `name` varchar(100) NOT NULL COMMENT '服务名称',
                              `description` text COMMENT '服务描述',
                              `price` decimal(10,2) DEFAULT '0.00' COMMENT '价格',
                              `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价/划线价',
                              `unit` varchar(20) DEFAULT '次' COMMENT '单位',
                              `image` varchar(500) DEFAULT NULL COMMENT '图片',
                              `detail_img` varchar(500) DEFAULT NULL COMMENT '详情图片',
                              `hot_status` tinyint DEFAULT '0' COMMENT '热门 1热门 0普通',
                              `status` tinyint DEFAULT '1' COMMENT '状态 1上架 0下架',
                              `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                              `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`),
                              KEY `idx_category` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务项目表';


-- yunlan.trading definition

CREATE TABLE `trading` (
                           `id` bigint NOT NULL AUTO_INCREMENT,
                           `order_id` bigint DEFAULT NULL COMMENT '订单ID',
                           `user_id` bigint NOT NULL COMMENT '用户ID',
                           `total_amount` decimal(10,2) DEFAULT '0.00' COMMENT '交易金额',
                           `status` tinyint DEFAULT '0' COMMENT '交易状态 0待支付 1支付成功 2已关闭',
                           `pay_channel` tinyint DEFAULT NULL COMMENT '支付渠道 1微信 2支付宝',
                           `trading_order_no` bigint DEFAULT NULL COMMENT '交易单号',
                           `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                           `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='交易单表';


-- yunlan.`user` definition

CREATE TABLE `user` (
                        `id` bigint NOT NULL AUTO_INCREMENT,
                        `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
                        `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
                        `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
                        `openid` varchar(100) DEFAULT '' COMMENT '微信openid',
                        `token` varchar(500) DEFAULT NULL COMMENT '登录token',
                        `status` tinyint DEFAULT '1' COMMENT '状态 1正常 0冻结',
                        `inviter_id` bigint DEFAULT NULL COMMENT '邀请人ID',
                        `invite_code` varchar(20) DEFAULT NULL COMMENT '邀请码',
                        `total_rebate` decimal(10,2) DEFAULT '0.00' COMMENT '累计返利金额',
                        `balance` decimal(10,2) DEFAULT '0.00' COMMENT '可提现余额',
                        `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                        `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除 0未删除 1已删除',
                        PRIMARY KEY (`id`),
                        KEY `idx_phone` (`phone`),
                        KEY `idx_invite_code` (`invite_code`),
                        KEY `idx_inviter` (`inviter_id`),
                        KEY `idx_openid` (`openid`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';


-- yunlan.user_favorite definition

CREATE TABLE `user_favorite` (
                                 `id` bigint NOT NULL AUTO_INCREMENT,
                                 `user_id` bigint NOT NULL COMMENT '用户ID',
                                 `target_id` bigint NOT NULL COMMENT '目标ID',
                                 `target_type` varchar(20) NOT NULL COMMENT '目标类型: serve_item/service, worker_recommend/auntie',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_user_target` (`user_id`,`target_id`,`target_type`),
                                 KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收藏';


-- yunlan.withdrawal definition

CREATE TABLE `withdrawal` (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `user_id` bigint NOT NULL COMMENT '用户ID',
                              `amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '提现金额',
                              `status` tinyint DEFAULT '0' COMMENT '状态 0待审核 1已通过 2已拒绝',
                              `account_info` varchar(500) DEFAULT NULL COMMENT '提现账户信息',
                              `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核备注',
                              `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
                              `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                              `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`),
                              KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='提现记录表';


-- yunlan.worker_recommend definition

CREATE TABLE `worker_recommend` (
                                    `id` bigint NOT NULL AUTO_INCREMENT,
                                    `name` varchar(50) NOT NULL COMMENT '姓名',
                                    `avatar` varchar(500) DEFAULT NULL COMMENT '性别',
                                    `experience_years` int DEFAULT '0' COMMENT '经验年数',
                                    `skills` varchar(500) DEFAULT NULL COMMENT '技能',
                                    `description` varchar(1000) DEFAULT '' COMMENT '详细介绍',
                                    `rating` decimal(2,1) DEFAULT '5.0' COMMENT '评分',
                                    `price` decimal(10,2) DEFAULT '0.00' COMMENT '价格',
                                    `serve_count` int DEFAULT '0' COMMENT '服务单数',
                                    `status` tinyint DEFAULT '1' COMMENT '状态 1启用 0禁用',
                                    `region_id` bigint DEFAULT NULL COMMENT '地点',
                                    `sort` int DEFAULT '0' COMMENT '分类',
                                    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                                    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`id`),
                                    KEY `idx_region` (`region_id`),
                                    KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='阿姨推荐';


-- 云打印小程序数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `printer_cloud` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `printer_cloud`;

-- 用户表
CREATE TABLE `pc_user` (
  `id` bigint NOT NULL COMMENT '用户ID',
  `open_id` varchar(64) NOT NULL COMMENT '微信OpenID',
  `union_id` varchar(64) DEFAULT NULL COMMENT '微信UnionID',
  `nickname` varchar(50) DEFAULT NULL COMMENT '用户昵称',
  `avatar_url` varchar(255) DEFAULT NULL COMMENT '用户头像',
  `gender` tinyint DEFAULT '0' COMMENT '性别：0-未知，1-男，2-女',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `status` tinyint DEFAULT '0' COMMENT '用户状态：0-正常，1-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_open_id` (`open_id`),
  KEY `idx_union_id` (`union_id`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 打印文件表
CREATE TABLE `pc_print_file` (
  `id` bigint NOT NULL COMMENT '文件ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_name` varchar(255) NOT NULL COMMENT '存储文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_size` bigint NOT NULL COMMENT '文件大小（字节）',
  `file_type` varchar(50) NOT NULL COMMENT '文件类型',
  `file_md5` varchar(32) DEFAULT NULL COMMENT '文件MD5',
  `page_count` int DEFAULT NULL COMMENT '文件页数',
  `preview_path` varchar(500) DEFAULT NULL COMMENT '预览图片路径',
  `status` tinyint DEFAULT '0' COMMENT '文件状态：0-上传中，1-上传成功，2-解析中，3-解析成功，4-解析失败',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_file_md5` (`file_md5`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打印文件表';

-- 打印订单表
CREATE TABLE `pc_print_order` (
  `id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(32) NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `file_id` bigint NOT NULL COMMENT '文件ID',
  `copies` int NOT NULL DEFAULT '1' COMMENT '打印份数',
  `page_range` varchar(255) DEFAULT NULL COMMENT '打印页数范围',
  `actual_pages` int NOT NULL COMMENT '实际打印页数',
  `is_color` tinyint NOT NULL DEFAULT '0' COMMENT '是否彩色：0-黑白，1-彩色',
  `is_double_side` tinyint NOT NULL DEFAULT '0' COMMENT '是否双面：0-单面，1-双面',
  `paper_size` varchar(20) DEFAULT 'A4' COMMENT '纸张规格',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注信息',
  `amount` decimal(10,2) NOT NULL COMMENT '订单金额',
  `verify_code` varchar(10) NOT NULL COMMENT '验证码',
  `status` tinyint DEFAULT '0' COMMENT '订单状态：0-待支付，1-已支付，2-打印中，3-已完成，4-已取消，5-已退款',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `wx_order_no` varchar(64) DEFAULT NULL COMMENT '微信支付订单号',
  `wx_transaction_id` varchar(64) DEFAULT NULL COMMENT '微信支付交易号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  UNIQUE KEY `uk_verify_code` (`verify_code`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_file_id` (`file_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打印订单表';

-- 支付记录表
CREATE TABLE `pc_payment_record` (
  `id` bigint NOT NULL COMMENT '支付记录ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(32) NOT NULL COMMENT '订单号',
  `payment_type` varchar(20) NOT NULL COMMENT '支付类型：WECHAT',
  `amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `wx_prepay_id` varchar(64) DEFAULT NULL COMMENT '微信预支付ID',
  `wx_transaction_id` varchar(64) DEFAULT NULL COMMENT '微信交易号',
  `status` tinyint DEFAULT '0' COMMENT '支付状态：0-待支付，1-支付成功，2-支付失败，3-已退款',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_wx_transaction_id` (`wx_transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

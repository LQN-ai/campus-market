-- =============================================================
-- 校园二手交易市场 数据库建库脚本
-- 数据库名：2024011443
-- 字符集 utf8mb4，存储引擎 InnoDB
-- =============================================================

CREATE DATABASE IF NOT EXISTS `2024011443` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `2024011443`;

-- 1. 用户表（普通用户 / 商家 / 管理员 共用）
CREATE TABLE IF NOT EXISTS `users` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`      VARCHAR(50)  NOT NULL COMMENT '登录名',
    `password`      VARCHAR(100) NOT NULL COMMENT '密码(BCrypt)',
    `real_name`     VARCHAR(50)  DEFAULT NULL COMMENT '姓名',
    `phone`         VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `email`         VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `city`          VARCHAR(50)  DEFAULT NULL COMMENT '城市',
    `gender`        TINYINT      DEFAULT 0 COMMENT '性别 0女 1男',
    `bank_account`  VARCHAR(16)  DEFAULT NULL COMMENT '银行账号(16位)',
    `avatar`        VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role`          VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色 USER/MERCHANT/ADMIN',
    `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '状态 0待审核 1正常 2拒绝 3封禁',
    `rating`        DECIMAL(3,2) NOT NULL DEFAULT 5.00 COMMENT '买家好评率(商家评价转化)',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role_status` (`role`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 商家扩展表（仅商家角色有记录）
CREATE TABLE IF NOT EXISTS `merchant_profiles` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`          BIGINT        NOT NULL COMMENT '关联用户ID',
    `shop_name`        VARCHAR(100)  DEFAULT NULL COMMENT '店铺名',
    `business_license` VARCHAR(255)  DEFAULT NULL COMMENT '营业执照图片URL',
    `id_card`          VARCHAR(255)  DEFAULT NULL COMMENT '身份证图片URL',
    `level`            TINYINT       NOT NULL DEFAULT 1 COMMENT '商家等级 1-5',
    `shop_status`      TINYINT       NOT NULL DEFAULT 0 COMMENT '店铺状态 0正常 1下架 2封禁',
    `ban_until`        DATETIME      DEFAULT NULL COMMENT '封禁截止时间',
    `rating`           DECIMAL(3,2)  NOT NULL DEFAULT 5.00 COMMENT '好评率',
    `total_sales`      DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计交易额',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家扩展表';

-- 3. 商品表
CREATE TABLE IF NOT EXISTS `products` (
    `id`             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `merchant_id`    BIGINT         NOT NULL COMMENT '商家用户ID',
    `name`           VARCHAR(100)   NOT NULL COMMENT '商品名称',
    `category`       VARCHAR(50)    DEFAULT NULL COMMENT '类别',
    `original_price` DECIMAL(10,2)  NOT NULL DEFAULT 0.00 COMMENT '原价',
    `price`          DECIMAL(10,2)  NOT NULL DEFAULT 0.00 COMMENT '现价/折扣价',
    `size`           VARCHAR(50)    DEFAULT NULL COMMENT '尺寸大小',
    `description`    TEXT           COMMENT '使用说明',
    `negotiable`     TINYINT        NOT NULL DEFAULT 0 COMMENT '是否允许议价 0否 1是',
    `stock`          INT            NOT NULL DEFAULT 1 COMMENT '库存数量',
    `condition_level` VARCHAR(20)   DEFAULT NULL COMMENT '新旧程度 全新/九成新/八成新等',
    `status`         VARCHAR(20)    NOT NULL DEFAULT 'PENDING' COMMENT '状态 PENDING待审核/PUBLISHED在售/OFFLINE下架/LOCKED锁定/SOLD售出',
    `sales`          INT            NOT NULL DEFAULT 0 COMMENT '销量',
    `rating`         DECIMAL(3,2)   NOT NULL DEFAULT 5.00 COMMENT '好评率',
    `created_at`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_merchant` (`merchant_id`),
    KEY `idx_status` (`status`),
    KEY `idx_category` (`category`),
    FULLTEXT KEY `ft_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 4. 商品图片表
CREATE TABLE IF NOT EXISTS `product_images` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `product_id` BIGINT       NOT NULL COMMENT '商品ID',
    `url`        VARCHAR(255) NOT NULL COMMENT '图片URL',
    `sort_order` INT          NOT NULL DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (`id`),
    KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';

-- 5. 购物车表
CREATE TABLE IF NOT EXISTS `cart_items` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`    BIGINT   NOT NULL COMMENT '用户ID',
    `product_id` BIGINT   NOT NULL COMMENT '商品ID',
    `quantity`   INT      NOT NULL DEFAULT 1 COMMENT '数量',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 6. 订单表
CREATE TABLE IF NOT EXISTS `orders` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no`     VARCHAR(32)   NOT NULL COMMENT '订单号',
    `user_id`      BIGINT        NOT NULL COMMENT '买家用户ID',
    `merchant_id`  BIGINT        NOT NULL COMMENT '商家用户ID',
    `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实际支付金额',
    `status`       VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '状态 PENDING待收货/CONFIRMED已确认/AUTO_CONFIRMED自动确认/RETURN_REQUESTED退货申请/RETURNED已退货',
    `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `confirmed_at` DATETIME      DEFAULT NULL COMMENT '确认收货时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user` (`user_id`),
    KEY `idx_merchant` (`merchant_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 7. 订单明细表
CREATE TABLE IF NOT EXISTS `order_items` (
    `id`         BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_id`   BIGINT        NOT NULL COMMENT '订单ID',
    `product_id` BIGINT        NOT NULL COMMENT '商品ID',
    `quantity`   INT           NOT NULL DEFAULT 1 COMMENT '数量',
    `price`      DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '成交单价',
    PRIMARY KEY (`id`),
    KEY `idx_order` (`order_id`),
    KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 8. 钱包表（余额 + 积分）
CREATE TABLE IF NOT EXISTS `wallets` (
    `id`      BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT        NOT NULL COMMENT '用户ID',
    `balance` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '余额',
    `points`  INT           NOT NULL DEFAULT 0 COMMENT '积分',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包表';

-- 9. 钱包流水表
CREATE TABLE IF NOT EXISTS `wallet_transactions` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`     BIGINT        NOT NULL COMMENT '用户ID',
    `type`        VARCHAR(20)   NOT NULL COMMENT '类型 RECHARGE充值/PAY支付/REFUND退款/RECEIVE收款/FEE手续费',
    `amount`      DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '金额(正入负出)',
    `order_id`    BIGINT        DEFAULT NULL COMMENT '关联订单ID',
    `description` VARCHAR(255)  DEFAULT NULL COMMENT '描述',
    `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包流水表';

-- 10. 积分流水表
CREATE TABLE IF NOT EXISTS `point_transactions` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
    `type`        VARCHAR(20)  NOT NULL COMMENT '类型 EARN获取/REDEEM抵扣',
    `points`      INT          NOT NULL DEFAULT 0 COMMENT '积分(正入负出)',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水表';

-- 11. 商品评价表
CREATE TABLE IF NOT EXISTS `product_reviews` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_id`   BIGINT      NOT NULL COMMENT '订单ID',
    `product_id` BIGINT      NOT NULL COMMENT '商品ID',
    `user_id`    BIGINT      NOT NULL COMMENT '评价用户ID',
    `rating`     TINYINT     NOT NULL DEFAULT 5 COMMENT '评分 1-5星',
    `content`    TEXT        COMMENT '评价内容',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    PRIMARY KEY (`id`),
    KEY `idx_product` (`product_id`),
    KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';

-- 12. 商家评价表
CREATE TABLE IF NOT EXISTS `merchant_reviews` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`     BIGINT      NOT NULL COMMENT '评价用户ID',
    `merchant_id` BIGINT      NOT NULL COMMENT '商家用户ID',
    `order_id`    BIGINT      DEFAULT NULL COMMENT '关联订单ID',
    `rating`      TINYINT     NOT NULL DEFAULT 5 COMMENT '评分 1-5星',
    `content`     TEXT        COMMENT '评价内容',
    `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    PRIMARY KEY (`id`),
    KEY `idx_merchant` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家评价表';

-- 13. 拉黑表（可选功能）
CREATE TABLE IF NOT EXISTS `blacklist` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`     BIGINT      NOT NULL COMMENT '被拉黑的买家ID',
    `merchant_id` BIGINT      DEFAULT NULL COMMENT '拉黑方商家ID(NULL表示平台拉黑)',
    `reason`      VARCHAR(255) DEFAULT NULL COMMENT '原因',
    `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拉黑表';

-- 14. 买家评价表（商家评价买家）
CREATE TABLE IF NOT EXISTS `buyer_reviews` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `merchant_id` BIGINT      NOT NULL COMMENT '评价商家ID',
    `user_id`     BIGINT      NOT NULL COMMENT '被评价买家ID',
    `order_id`    BIGINT      DEFAULT NULL COMMENT '关联订单ID',
    `rating`      TINYINT     NOT NULL DEFAULT 5 COMMENT '评分 1-5星',
    `content`     TEXT        COMMENT '评价内容',
    `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_merchant` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='买家评价表';

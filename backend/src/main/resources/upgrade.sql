-- =============================================================
-- 数据库升级脚本（在已建好的数据库上执行一次即可）
-- 新增：买家好评率字段 + 买家评价表
-- 用法：mysql -u root -p 你的学号 < upgrade.sql
-- =============================================================

ALTER TABLE `users`
    ADD COLUMN `rating` DECIMAL(3,2) NOT NULL DEFAULT 5.00 COMMENT '买家好评率(商家评价转化)' AFTER `status`;

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

-- 订单表新增：线下交易时间、地点
ALTER TABLE `orders`
    ADD COLUMN `trade_time` VARCHAR(50) DEFAULT NULL COMMENT '线下交易时间(约定)' AFTER `status`,
    ADD COLUMN `trade_location` VARCHAR(100) DEFAULT NULL COMMENT '线下交易地点(约定)' AFTER `trade_time`;

-- 商品表新增：是否推荐到首页轮播
ALTER TABLE `products`
    ADD COLUMN `recommend` TINYINT NOT NULL DEFAULT 0 COMMENT '是否推荐到首页轮播 0否 1是' AFTER `status`;

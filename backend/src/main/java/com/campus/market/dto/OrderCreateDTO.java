package com.campus.market.dto;

import lombok.Data;

import java.util.List;

/**
 * 下单请求：直接购买单个商品，或从购物车批量下单
 */
@Data
public class OrderCreateDTO {
    /** 直接购买单个商品时传 */
    private Long productId;
    private Integer quantity;

    /** 从购物车下单时传多个购物车项ID */
    private List<Long> cartItemIds;
}

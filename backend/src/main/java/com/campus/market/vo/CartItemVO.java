package com.campus.market.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemVO {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private String image;
    private BigDecimal price;
    private Integer quantity;
    private Integer stock;
    private Long merchantId;
    private String shopName;
}

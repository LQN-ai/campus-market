package com.campus.market.vo;

import com.campus.market.entity.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderVO {
    private Order order;
    private String shopName;
    private String buyerName;
    private BigDecimal buyerRating;
    private List<Item> items;

    @Data
    public static class Item {
        private Long productId;
        private String productName;
        private String image;
        private Integer quantity;
        private BigDecimal price;
    }
}

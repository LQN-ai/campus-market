package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("products")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long merchantId;
    private String name;
    private String category;
    private BigDecimal originalPrice;
    private BigDecimal price;
    private String size;
    private String description;
    private Integer negotiable;
    private Integer stock;
    private String conditionLevel;
    private String status;
    private Integer recommend;
    private Integer sales;
    private BigDecimal rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

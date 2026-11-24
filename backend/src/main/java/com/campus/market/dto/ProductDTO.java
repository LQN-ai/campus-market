package com.campus.market.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 发布/修改商品
 */
@Data
public class ProductDTO {
    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String category;

    @NotNull(message = "原价不能为空")
    private BigDecimal originalPrice;

    @NotNull(message = "售价不能为空")
    private BigDecimal price;

    private String size;
    private String description;
    private Integer negotiable;

    @NotNull(message = "库存不能为空")
    private Integer stock;

    private String conditionLevel;

    /** 商品图片 URL 列表 */
    private List<String> images;
}

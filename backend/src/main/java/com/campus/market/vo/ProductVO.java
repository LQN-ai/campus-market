package com.campus.market.vo;

import com.campus.market.entity.Product;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品视图对象：附带店铺名、图片列表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductVO extends Product {

    /** 店铺名（关联查询得到） */
    private String shopName;

    /** 商家好评率（关联查询得到） */
    private BigDecimal merchantRating;

    /** 商品图片 URL 列表 */
    private List<String> images;
}

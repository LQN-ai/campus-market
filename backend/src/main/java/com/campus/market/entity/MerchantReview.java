package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("merchant_reviews")
public class MerchantReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long merchantId;
    private Long orderId;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
}

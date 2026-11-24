package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("buyer_reviews")
public class BuyerReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long merchantId;
    private Long userId;
    private Long orderId;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
}

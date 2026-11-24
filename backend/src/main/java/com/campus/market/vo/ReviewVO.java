package com.campus.market.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewVO {
    private Long id;
    private Long userId;
    private String userName;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
}

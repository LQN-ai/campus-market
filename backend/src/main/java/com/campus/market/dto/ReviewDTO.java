package com.campus.market.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评价请求
 */
@Data
public class ReviewDTO {
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分至少1星")
    @Max(value = 5, message = "评分最多5星")
    private Integer rating;

    private String content;
}

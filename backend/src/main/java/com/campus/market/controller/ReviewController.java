package com.campus.market.controller;

import com.campus.market.common.Result;
import com.campus.market.dto.ReviewDTO;
import com.campus.market.security.UserContext;
import com.campus.market.service.ReviewService;
import com.campus.market.vo.ReviewVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/products/{id}/reviews")
    public Result<Void> reviewProduct(@PathVariable Long id,
                                      @RequestParam Long orderId,
                                      @Valid @RequestBody ReviewDTO dto) {
        reviewService.reviewProduct(UserContext.getUserId(), id, orderId, dto);
        return Result.success();
    }

    @GetMapping("/products/{id}/reviews")
    public Result<List<ReviewVO>> productReviews(@PathVariable Long id) {
        return Result.success(reviewService.listProductReviews(id));
    }

    @PostMapping("/merchants/{id}/reviews")
    public Result<Void> reviewMerchant(@PathVariable Long id,
                                       @RequestParam Long orderId,
                                       @Valid @RequestBody ReviewDTO dto) {
        reviewService.reviewMerchant(UserContext.getUserId(), id, orderId, dto);
        return Result.success();
    }

    @GetMapping("/merchants/{id}/reviews")
    public Result<List<ReviewVO>> merchantReviews(@PathVariable Long id) {
        return Result.success(reviewService.listMerchantReviews(id));
    }

    /**
     * 商家评价买家（转化为买家好评率）
     */
    @PostMapping("/buyers/{id}/reviews")
    public Result<Void> reviewBuyer(@PathVariable Long id,
                                    @RequestParam Long orderId,
                                    @Valid @RequestBody ReviewDTO dto) {
        reviewService.reviewBuyer(UserContext.getUserId(), id, orderId, dto);
        return Result.success();
    }
}

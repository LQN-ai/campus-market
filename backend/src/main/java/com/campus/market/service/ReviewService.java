package com.campus.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.market.common.BusinessException;
import com.campus.market.dto.ReviewDTO;
import com.campus.market.entity.*;
import com.campus.market.mapper.*;
import com.campus.market.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 评价服务
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ProductReviewMapper productReviewMapper;
    private final MerchantReviewMapper merchantReviewMapper;
    private final BuyerReviewMapper buyerReviewMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final MerchantProfileMapper merchantProfileMapper;

    /**
     * 商品评价（购买并确认收货后才能评价）
     */
    public void reviewProduct(Long userId, Long productId, Long orderId, ReviewDTO dto) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (!"CONFIRMED".equals(order.getStatus()) && !"AUTO_CONFIRMED".equals(order.getStatus())) {
            throw new BusinessException("订单未完成，无法评价");
        }
        ProductReview review = new ProductReview();
        review.setOrderId(orderId);
        review.setProductId(productId);
        review.setUserId(userId);
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        productReviewMapper.insert(review);
        refreshProductRating(productId);
    }

    /**
     * 商家评价
     */
    public void reviewMerchant(Long userId, Long merchantId, Long orderId, ReviewDTO dto) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        MerchantReview review = new MerchantReview();
        review.setUserId(userId);
        review.setMerchantId(merchantId);
        review.setOrderId(orderId);
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        merchantReviewMapper.insert(review);
        refreshMerchantRating(merchantId);
    }

    /**
     * 商家评价买家（评价转化为买家好评率）
     */
    public void reviewBuyer(Long merchantId, Long buyerId, Long orderId, ReviewDTO dto) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(buyerId)) {
            throw new BusinessException("买家与订单不匹配");
        }
        if (!"CONFIRMED".equals(order.getStatus()) && !"AUTO_CONFIRMED".equals(order.getStatus())
                && !"RETURNED".equals(order.getStatus())) {
            throw new BusinessException("订单未完成，无法评价");
        }
        BuyerReview review = new BuyerReview();
        review.setMerchantId(merchantId);
        review.setUserId(buyerId);
        review.setOrderId(orderId);
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        buyerReviewMapper.insert(review);
        refreshBuyerRating(buyerId);
    }

    public List<ReviewVO> listProductReviews(Long productId) {
        List<ProductReview> reviews = productReviewMapper.selectList(
                new LambdaQueryWrapper<ProductReview>()
                        .eq(ProductReview::getProductId, productId)
                        .orderByDesc(ProductReview::getCreatedAt));
        List<ReviewVO> result = new ArrayList<>();
        for (ProductReview r : reviews) {
            ReviewVO vo = new ReviewVO();
            vo.setId(r.getId());
            vo.setUserId(r.getUserId());
            vo.setRating(r.getRating());
            vo.setContent(r.getContent());
            vo.setCreatedAt(r.getCreatedAt());
            vo.setUserName(userName(r.getUserId()));
            result.add(vo);
        }
        return result;
    }

    public List<ReviewVO> listMerchantReviews(Long merchantId) {
        List<MerchantReview> reviews = merchantReviewMapper.selectList(
                new LambdaQueryWrapper<MerchantReview>()
                        .eq(MerchantReview::getMerchantId, merchantId)
                        .orderByDesc(MerchantReview::getCreatedAt));
        List<ReviewVO> result = new ArrayList<>();
        for (MerchantReview r : reviews) {
            ReviewVO vo = new ReviewVO();
            vo.setId(r.getId());
            vo.setUserId(r.getUserId());
            vo.setRating(r.getRating());
            vo.setContent(r.getContent());
            vo.setCreatedAt(r.getCreatedAt());
            vo.setUserName(userName(r.getUserId()));
            result.add(vo);
        }
        return result;
    }

    private String userName(Long userId) {
        User u = userMapper.selectById(userId);
        return u == null ? null : u.getRealName();
    }

    /**
     * 重算商品平均评分
     */
    private void refreshProductRating(Long productId) {
        List<ProductReview> reviews = productReviewMapper.selectList(
                new LambdaQueryWrapper<ProductReview>().eq(ProductReview::getProductId, productId));
        double avg = reviews.stream().mapToInt(ProductReview::getRating).average().orElse(5.0);
        Product p = productMapper.selectById(productId);
        if (p != null) {
            p.setRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
            productMapper.updateById(p);
        }
    }

    /**
     * 重算商家平均评分
     */
    private void refreshMerchantRating(Long merchantId) {
        List<MerchantReview> reviews = merchantReviewMapper.selectList(
                new LambdaQueryWrapper<MerchantReview>().eq(MerchantReview::getMerchantId, merchantId));
        double avg = reviews.stream().mapToInt(MerchantReview::getRating).average().orElse(5.0);
        MerchantProfile profile = merchantProfileMapper.selectOne(
                new LambdaQueryWrapper<MerchantProfile>().eq(MerchantProfile::getUserId, merchantId));
        if (profile != null) {
            profile.setRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
            merchantProfileMapper.updateById(profile);
        }
    }

    /**
     * 重算买家平均评分（好评率）
     */
    private void refreshBuyerRating(Long buyerId) {
        List<BuyerReview> reviews = buyerReviewMapper.selectList(
                new LambdaQueryWrapper<BuyerReview>().eq(BuyerReview::getUserId, buyerId));
        double avg = reviews.stream().mapToInt(BuyerReview::getRating).average().orElse(5.0);
        User u = userMapper.selectById(buyerId);
        if (u != null) {
            u.setRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
            userMapper.updateById(u);
        }
    }
}

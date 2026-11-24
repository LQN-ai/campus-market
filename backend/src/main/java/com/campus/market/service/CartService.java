package com.campus.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.market.common.BusinessException;
import com.campus.market.entity.CartItem;
import com.campus.market.entity.MerchantProfile;
import com.campus.market.entity.Product;
import com.campus.market.entity.ProductImage;
import com.campus.market.mapper.CartItemMapper;
import com.campus.market.mapper.MerchantProfileMapper;
import com.campus.market.mapper.ProductImageMapper;
import com.campus.market.mapper.ProductMapper;
import com.campus.market.vo.CartItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final MerchantProfileMapper merchantProfileMapper;

    public List<CartItemVO> list(Long userId) {
        List<CartItem> items = cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId));
        List<CartItemVO> result = new ArrayList<>();
        for (CartItem c : items) {
            Product p = productMapper.selectById(c.getProductId());
            if (p == null) {
                continue;
            }
            CartItemVO vo = new CartItemVO();
            vo.setCartItemId(c.getId());
            vo.setProductId(p.getId());
            vo.setProductName(p.getName());
            vo.setPrice(p.getPrice());
            vo.setQuantity(c.getQuantity());
            vo.setStock(p.getStock());
            vo.setMerchantId(p.getMerchantId());

            ProductImage img = productImageMapper.selectOne(
                    new LambdaQueryWrapper<ProductImage>()
                            .eq(ProductImage::getProductId, p.getId())
                            .orderByAsc(ProductImage::getSortOrder)
                            .last("LIMIT 1"));
            vo.setImage(img == null ? null : img.getUrl());

            MerchantProfile profile = merchantProfileMapper.selectOne(
                    new LambdaQueryWrapper<MerchantProfile>().eq(MerchantProfile::getUserId, p.getMerchantId()));
            vo.setShopName(profile == null ? null : profile.getShopName());
            result.add(vo);
        }
        return result;
    }

    public void add(Long userId, Long productId, Integer quantity) {
        Product p = productMapper.selectById(productId);
        if (p == null || !"PUBLISHED".equals(p.getStatus())) {
            throw new BusinessException("商品不可购买");
        }
        if (p.getMerchantId().equals(userId)) {
            throw new BusinessException("不能购买自己发布的商品");
        }
        CartItem exist = cartItemMapper.selectOne(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .eq(CartItem::getProductId, productId));
        int qty = quantity == null ? 1 : quantity;
        if (exist != null) {
            exist.setQuantity(exist.getQuantity() + qty);
            cartItemMapper.updateById(exist);
        } else {
            CartItem c = new CartItem();
            c.setUserId(userId);
            c.setProductId(productId);
            c.setQuantity(qty);
            cartItemMapper.insert(c);
        }
    }

    public void update(Long userId, Long cartItemId, Integer quantity) {
        CartItem c = cartItemMapper.selectById(cartItemId);
        if (c == null || !c.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在");
        }
        if (quantity != null && quantity > 0) {
            c.setQuantity(quantity);
            cartItemMapper.updateById(c);
        }
    }

    public void delete(Long userId, Long cartItemId) {
        CartItem c = cartItemMapper.selectById(cartItemId);
        if (c == null || !c.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在");
        }
        cartItemMapper.deleteById(cartItemId);
    }
}

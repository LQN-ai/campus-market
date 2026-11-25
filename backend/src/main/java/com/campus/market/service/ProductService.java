package com.campus.market.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.common.BusinessException;
import com.campus.market.dto.ProductDTO;
import com.campus.market.entity.MerchantProfile;
import com.campus.market.entity.Product;
import com.campus.market.entity.ProductImage;
import com.campus.market.mapper.MerchantProfileMapper;
import com.campus.market.mapper.ProductImageMapper;
import com.campus.market.mapper.ProductMapper;
import com.campus.market.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品服务
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final MerchantProfileMapper merchantProfileMapper;

    /**
     * 条件搜索 + 排序（分页）
     */
    public IPage<ProductVO> search(String keyword, String category, String sortBy, int page, int size) {
        Page<ProductVO> p = new Page<>(page, size);
        IPage<ProductVO> result = productMapper.search(p, keyword, category, sortBy);
        fillImages(result.getRecords());
        return result;
    }

    /**
     * 商品详情
     */
    public ProductVO detail(Long id) {
        ProductVO vo = productMapper.selectDetailWithShop(id);
        if (vo == null) {
            throw new BusinessException("商品不存在");
        }
        vo.setImages(queryImages(id));
        return vo;
    }

    /**
     * 商家发布商品（待审核）
     */
    public void create(Long merchantId, ProductDTO dto) {
        MerchantProfile profile = merchantProfileMapper.selectOne(
                new LambdaQueryWrapper<MerchantProfile>().eq(MerchantProfile::getUserId, merchantId));
        if (profile != null && profile.getShopStatus() != 0) {
            throw new BusinessException("店铺已被封禁或下架，无法发布商品");
        }
        Product p = new Product();
        p.setMerchantId(merchantId);
        copyDto(dto, p);
        p.setStatus("PENDING");
        p.setSales(0);
        p.setRating(new BigDecimal("5.00"));
        productMapper.insert(p);
        saveImages(p.getId(), dto.getImages());
    }

    /**
     * 商家修改商品（修改后重新审核）
     */
    public void update(Long merchantId, Long productId, ProductDTO dto) {
        Product p = productMapper.selectById(productId);
        if (p == null || !p.getMerchantId().equals(merchantId)) {
            throw new BusinessException("无权操作该商品");
        }
        copyDto(dto, p);
        p.setStatus("PENDING");
        productMapper.updateById(p);
        productImageMapper.delete(new LambdaQueryWrapper<ProductImage>()
                .eq(ProductImage::getProductId, productId));
        saveImages(productId, dto.getImages());
    }

    /**
     * 商家下架商品
     */
    public void offline(Long merchantId, Long productId) {
        Product p = productMapper.selectById(productId);
        if (p == null || !p.getMerchantId().equals(merchantId)) {
            throw new BusinessException("无权操作该商品");
        }
        p.setStatus("OFFLINE");
        productMapper.updateById(p);
    }

    /**
     * 商家查看自己的商品
     */
    public List<ProductVO> listByMerchant(Long merchantId) {
        List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getMerchantId, merchantId)
                        .orderByDesc(Product::getCreatedAt));
        List<ProductVO> list = new ArrayList<>();
        for (Product p : products) {
            ProductVO vo = BeanUtil.copyProperties(p, ProductVO.class);
            vo.setImages(queryImages(p.getId()));
            list.add(vo);
        }
        return list;
    }

    /**
     * 管理端商品列表（含图片、店铺名）
     */
    public IPage<ProductVO> listAll(int page, int size, String status) {
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            qw.eq(Product::getStatus, status);
        }
        qw.orderByDesc(Product::getCreatedAt);
        IPage<Product> pp = productMapper.selectPage(new Page<>(page, size), qw);

        List<ProductVO> records = new ArrayList<>();
        for (Product p : pp.getRecords()) {
            ProductVO vo = BeanUtil.copyProperties(p, ProductVO.class);
            vo.setImages(queryImages(p.getId()));
            MerchantProfile mp = merchantProfileMapper.selectOne(
                    new LambdaQueryWrapper<MerchantProfile>().eq(MerchantProfile::getUserId, p.getMerchantId()));
            vo.setShopName(mp == null ? null : mp.getShopName());
            records.add(vo);
        }
        Page<ProductVO> result = new Page<>(page, size, pp.getTotal());
        result.setRecords(records);
        return result;
    }

    /**
     * 首页轮播图：推荐且在售的商品
     */
    public List<ProductVO> listRecommend() {
        List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getRecommend, 1)
                        .eq(Product::getStatus, "PUBLISHED")
                        .orderByDesc(Product::getCreatedAt));
        List<ProductVO> list = new ArrayList<>();
        for (Product p : products) {
            ProductVO vo = BeanUtil.copyProperties(p, ProductVO.class);
            vo.setImages(queryImages(p.getId()));
            MerchantProfile mp = merchantProfileMapper.selectOne(
                    new LambdaQueryWrapper<MerchantProfile>().eq(MerchantProfile::getUserId, p.getMerchantId()));
            vo.setShopName(mp == null ? null : mp.getShopName());
            list.add(vo);
        }
        return list;
    }

    private void copyDto(ProductDTO dto, Product p) {
        p.setName(dto.getName());
        p.setCategory(dto.getCategory());
        p.setOriginalPrice(dto.getOriginalPrice());
        p.setPrice(dto.getPrice());
        p.setSize(dto.getSize());
        p.setDescription(dto.getDescription());
        p.setNegotiable(dto.getNegotiable() == null ? 0 : dto.getNegotiable());
        p.setStock(dto.getStock());
        p.setConditionLevel(dto.getConditionLevel());
        p.setRecommend(dto.getRecommend() == null ? 0 : dto.getRecommend());
    }

    private void saveImages(Long productId, List<String> images) {
        if (images == null) {
            return;
        }
        int order = 0;
        for (String url : images) {
            ProductImage img = new ProductImage();
            img.setProductId(productId);
            img.setUrl(url);
            img.setSortOrder(order++);
            productImageMapper.insert(img);
        }
    }

    private List<String> queryImages(Long productId) {
        List<ProductImage> imgs = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, productId)
                        .orderByAsc(ProductImage::getSortOrder));
        return imgs.stream().map(ProductImage::getUrl).toList();
    }

    private void fillImages(List<ProductVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Long> ids = list.stream().map(Product::getId).toList();
        List<ProductImage> imgs = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .in(ProductImage::getProductId, ids)
                        .orderByAsc(ProductImage::getSortOrder));
        Map<Long, List<String>> map = new HashMap<>();
        for (ProductImage img : imgs) {
            map.computeIfAbsent(img.getProductId(), k -> new ArrayList<>()).add(img.getUrl());
        }
        for (ProductVO vo : list) {
            vo.setImages(map.getOrDefault(vo.getId(), new ArrayList<>()));
        }
    }
}

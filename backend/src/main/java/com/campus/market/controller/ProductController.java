package com.campus.market.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.market.common.Result;
import com.campus.market.dto.ProductDTO;
import com.campus.market.entity.Product;
import com.campus.market.security.RequireRole;
import com.campus.market.security.UserContext;
import com.campus.market.service.ProductService;
import com.campus.market.vo.ProductVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Result<IPage<ProductVO>> search(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String category,
                                           @RequestParam(required = false) String sortBy,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return Result.success(productService.search(keyword, category, sortBy, page, size));
    }

    @GetMapping("/recommend")
    public Result<List<ProductVO>> recommend() {
        return Result.success(productService.listRecommend());
    }

    @GetMapping("/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.success(productService.detail(id));
    }

    @PostMapping
    @RequireRole("MERCHANT")
    public Result<Void> create(@Valid @RequestBody ProductDTO dto) {
        productService.create(UserContext.getUserId(), dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequireRole("MERCHANT")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        productService.update(UserContext.getUserId(), id, dto);
        return Result.success();
    }

    @PostMapping("/{id}/offline")
    @RequireRole("MERCHANT")
    public Result<Void> offline(@PathVariable Long id) {
        productService.offline(UserContext.getUserId(), id);
        return Result.success();
    }

    @GetMapping("/mine")
    @RequireRole("MERCHANT")
    public Result<List<ProductVO>> mine() {
        return Result.success(productService.listByMerchant(UserContext.getUserId()));
    }
}

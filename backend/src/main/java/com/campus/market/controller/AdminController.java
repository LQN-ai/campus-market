package com.campus.market.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.market.common.Result;
import com.campus.market.entity.User;
import com.campus.market.security.RequireRole;
import com.campus.market.service.AdminService;
import com.campus.market.service.ProductService;
import com.campus.market.vo.MerchantVO;
import com.campus.market.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@RequireRole("ADMIN")
public class AdminController {

    private final AdminService adminService;
    private final ProductService productService;

    @GetMapping("/users")
    public Result<IPage<User>> users(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(required = false) String role,
                                     @RequestParam(required = false) Integer status) {
        return Result.success(adminService.listUsers(page, size, role, status));
    }

    @PostMapping("/users/{id}/audit")
    public Result<Void> auditUser(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        adminService.auditUser(id, Boolean.TRUE.equals(body.get("approve")));
        return Result.success();
    }

    @PutMapping("/users/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody User info) {
        adminService.updateUser(id, info);
        return Result.success();
    }

    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return Result.success();
    }

    @PostMapping("/users/{id}/recharge")
    public Result<Void> recharge(@PathVariable Long id, @RequestBody Map<String, BigDecimal> body) {
        adminService.recharge(id, body.get("amount"));
        return Result.success();
    }

    @GetMapping("/merchants")
    public Result<List<MerchantVO>> merchants() {
        return Result.success(adminService.listMerchants());
    }

    @PostMapping("/merchants/{id}/audit")
    public Result<Void> auditMerchant(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        adminService.auditMerchant(id, Boolean.TRUE.equals(body.get("approve")));
        return Result.success();
    }

    @PutMapping("/merchants/{id}/level")
    public Result<Void> setLevel(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.setLevel(id, body.get("level"));
        return Result.success();
    }

    @PostMapping("/merchants/{id}/ban")
    public Result<Void> ban(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.banMerchant(id, body.get("days"));
        return Result.success();
    }

    @PostMapping("/merchants/{id}/offline")
    public Result<Void> offline(@PathVariable Long id) {
        adminService.offlineMerchant(id);
        return Result.success();
    }

    @PostMapping("/merchants/{id}/unban")
    public Result<Void> unban(@PathVariable Long id) {
        adminService.unbanMerchant(id);
        return Result.success();
    }

    @GetMapping("/products")
    public Result<IPage<ProductVO>> products(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(required = false) String status) {
        return Result.success(productService.listAll(page, size, status));
    }

    @PostMapping("/products/{id}/audit")
    public Result<Void> auditProduct(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        adminService.auditProduct(id, Boolean.TRUE.equals(body.get("approve")));
        return Result.success();
    }
}

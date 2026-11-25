package com.campus.market.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.common.BusinessException;
import com.campus.market.entity.Blacklist;
import com.campus.market.entity.MerchantProfile;
import com.campus.market.entity.Product;
import com.campus.market.entity.User;
import com.campus.market.entity.Wallet;
import com.campus.market.mapper.BlacklistMapper;
import com.campus.market.mapper.MerchantProfileMapper;
import com.campus.market.mapper.ProductMapper;
import com.campus.market.mapper.UserMapper;
import com.campus.market.mapper.WalletMapper;
import com.campus.market.vo.MerchantVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理端服务：审核、等级、封禁、充值
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserMapper userMapper;
    private final MerchantProfileMapper merchantProfileMapper;
    private final ProductMapper productMapper;
    private final WalletMapper walletMapper;
    private final BlacklistMapper blacklistMapper;
    private final WalletService walletService;

    public IPage<User> listUsers(int page, int size, String role, Integer status) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        if (role != null && !role.isEmpty()) {
            qw.eq(User::getRole, role);
        }
        if (status != null) {
            qw.eq(User::getStatus, status);
        }
        qw.orderByDesc(User::getCreatedAt);
        IPage<User> result = userMapper.selectPage(new Page<>(page, size), qw);
        result.getRecords().forEach(u -> u.setPassword(null));
        return result;
    }

    public List<MerchantVO> listMerchants() {
        List<User> merchants = userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getRole, "MERCHANT"));
        List<MerchantVO> result = new ArrayList<>();
        for (User u : merchants) {
            MerchantVO vo = new MerchantVO();
            vo.setUserId(u.getId());
            vo.setUsername(u.getUsername());
            vo.setRealName(u.getRealName());
            vo.setPhone(u.getPhone());
            vo.setUserStatus(u.getStatus());
            MerchantProfile p = merchantProfileMapper.selectOne(
                    new LambdaQueryWrapper<MerchantProfile>().eq(MerchantProfile::getUserId, u.getId()));
            if (p != null) {
                BeanUtil.copyProperties(p, vo);
            }
            result.add(vo);
        }
        return result;
    }

    public void auditUser(Long userId, boolean approve) {
        User u = userMapper.selectById(userId);
        if (u == null) {
            throw new BusinessException("用户不存在");
        }
        u.setStatus(approve ? 1 : 2);
        userMapper.updateById(u);
    }

    /**
     * 管理员修改用户信息（姓名、手机号、邮箱、城市、性别）
     */
    public void updateUser(Long userId, User info) {
        User u = userMapper.selectById(userId);
        if (u == null) {
            throw new BusinessException("用户不存在");
        }
        if (info.getRealName() != null) {
            u.setRealName(info.getRealName());
        }
        if (info.getPhone() != null) {
            u.setPhone(info.getPhone());
        }
        if (info.getEmail() != null) {
            u.setEmail(info.getEmail());
        }
        if (info.getCity() != null) {
            u.setCity(info.getCity());
        }
        if (info.getGender() != null) {
            u.setGender(info.getGender());
        }
        userMapper.updateById(u);
    }

    /**
     * 管理员删除用户（及关联的商家资料、钱包）
     */
    public void deleteUser(Long userId) {
        User u = userMapper.selectById(userId);
        if (u == null) {
            throw new BusinessException("用户不存在");
        }
        merchantProfileMapper.delete(new LambdaQueryWrapper<MerchantProfile>()
                .eq(MerchantProfile::getUserId, userId));
        walletMapper.delete(new LambdaQueryWrapper<Wallet>()
                .eq(Wallet::getUserId, userId));
        userMapper.deleteById(userId);
    }

    public void auditMerchant(Long userId, boolean approve) {
        User u = userMapper.selectById(userId);
        if (u == null || !"MERCHANT".equals(u.getRole())) {
            throw new BusinessException("商家不存在");
        }
        u.setStatus(approve ? 1 : 2);
        userMapper.updateById(u);
        if (approve) {
            MerchantProfile p = merchantProfileMapper.selectOne(
                    new LambdaQueryWrapper<MerchantProfile>().eq(MerchantProfile::getUserId, userId));
            if (p != null) {
                p.setShopStatus(0);
                merchantProfileMapper.updateById(p);
            }
        }
    }

    public void auditProduct(Long productId, boolean approve) {
        Product p = productMapper.selectById(productId);
        if (p == null) {
            throw new BusinessException("商品不存在");
        }
        p.setStatus(approve ? "PUBLISHED" : "OFFLINE");
        productMapper.updateById(p);
    }

    public void setLevel(Long userId, Integer level) {
        if (level == null || level < 1 || level > 5) {
            throw new BusinessException("等级必须在1-5之间");
        }
        MerchantProfile p = getProfile(userId);
        p.setLevel(level);
        merchantProfileMapper.updateById(p);
    }

    public void banMerchant(Long userId, Integer days) {
        MerchantProfile p = getProfile(userId);
        p.setShopStatus(2);
        p.setBanUntil(LocalDateTime.now().plusDays(days == null ? 1 : days));
        merchantProfileMapper.updateById(p);
    }

    /**
     * 解除封禁 / 恢复店铺正常营业
     */
    public void unbanMerchant(Long userId) {
        MerchantProfile p = getProfile(userId);
        p.setShopStatus(0);
        p.setBanUntil(null);
        merchantProfileMapper.updateById(p);
    }

    public void offlineMerchant(Long userId) {
        MerchantProfile p = getProfile(userId);
        p.setShopStatus(1);
        merchantProfileMapper.updateById(p);
        productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getMerchantId, userId)
                .eq(Product::getStatus, "PUBLISHED")
                .set(Product::getStatus, "OFFLINE"));
    }

    /**
     * 解除下架（恢复店铺正常营业）
     */
    public void unofflineMerchant(Long userId) {
        MerchantProfile p = getProfile(userId);
        p.setShopStatus(0);
        merchantProfileMapper.updateById(p);
    }

    public void recharge(Long userId, BigDecimal amount) {
        walletService.recharge(userId, amount);
    }

    /**
     * 平台拉黑买家（merchant_id 为 NULL 表示全平台拉黑）
     */
    public void banUser(Long userId) {
        Long exist = blacklistMapper.selectCount(new LambdaQueryWrapper<Blacklist>()
                .eq(Blacklist::getUserId, userId)
                .isNull(Blacklist::getMerchantId));
        if (exist > 0) {
            return;
        }
        Blacklist b = new Blacklist();
        b.setUserId(userId);
        b.setMerchantId(null);
        blacklistMapper.insert(b);
    }

    /**
     * 解除平台拉黑
     */
    public void unbanUser(Long userId) {
        blacklistMapper.delete(new LambdaQueryWrapper<Blacklist>()
                .eq(Blacklist::getUserId, userId)
                .isNull(Blacklist::getMerchantId));
    }

    private MerchantProfile getProfile(Long userId) {
        MerchantProfile p = merchantProfileMapper.selectOne(
                new LambdaQueryWrapper<MerchantProfile>().eq(MerchantProfile::getUserId, userId));
        if (p == null) {
            throw new BusinessException("商家不存在");
        }
        return p;
    }
}

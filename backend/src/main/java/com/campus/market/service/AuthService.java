package com.campus.market.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.market.common.BusinessException;
import com.campus.market.dto.LoginDTO;
import com.campus.market.dto.MerchantRegisterDTO;
import com.campus.market.dto.RegisterDTO;
import com.campus.market.entity.MerchantProfile;
import com.campus.market.entity.User;
import com.campus.market.entity.Wallet;
import com.campus.market.mapper.MerchantProfileMapper;
import com.campus.market.mapper.UserMapper;
import com.campus.market.mapper.WalletMapper;
import com.campus.market.security.JwtUtil;
import com.campus.market.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证服务：验证码、注册、登录
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final MerchantProfileMapper merchantProfileMapper;
    private final WalletMapper walletMapper;
    private final JwtUtil jwtUtil;

    /** 内存验证码存储 */
    private final Map<String, CaptchaEntry> captchaStore = new ConcurrentHashMap<>();

    /**
     * 生成图片验证码，返回 captchaId + base64 图片
     */
    public Map<String, String> getCaptcha() {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 30);
        String id = UUID.randomUUID().toString().replace("-", "");
        captchaStore.put(id, new CaptchaEntry(captcha.getCode(), System.currentTimeMillis() + 5 * 60 * 1000));
        captchaStore.entrySet().removeIf(e -> e.getValue().expireAt < System.currentTimeMillis());
        Map<String, String> result = new HashMap<>();
        result.put("captchaId", id);
        result.put("image", "data:image/png;base64," + captcha.getImageBase64());
        return result;
    }

    private void verifyCaptcha(String id, String code) {
        CaptchaEntry entry = captchaStore.remove(id);
        if (entry == null || entry.expireAt < System.currentTimeMillis()) {
            throw new BusinessException("验证码已过期，请刷新");
        }
        if (!entry.code.equalsIgnoreCase(code)) {
            throw new BusinessException("验证码错误");
        }
    }

    /**
     * 普通用户注册（注册后待管理员审核）
     */
    public void register(RegisterDTO dto) {
        verifyCaptcha(dto.getCaptchaId(), dto.getCaptchaCode());
        checkUsername(dto.getUsername());

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setCity(dto.getCity());
        user.setGender(dto.getGender());
        user.setBankAccount(dto.getBankAccount());
        user.setRole("USER");
        user.setStatus(0);
        userMapper.insert(user);
        createWallet(user.getId());
    }

    /**
     * 商家注册（注册后待管理员审核）
     */
    public void merchantRegister(MerchantRegisterDTO dto) {
        verifyCaptcha(dto.getCaptchaId(), dto.getCaptchaCode());
        checkUsername(dto.getUsername());

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setGender(dto.getGender());
        user.setBankAccount(dto.getBankAccount());
        user.setRole("MERCHANT");
        user.setStatus(0);
        userMapper.insert(user);

        MerchantProfile profile = new MerchantProfile();
        profile.setUserId(user.getId());
        profile.setShopName(dto.getShopName());
        profile.setBusinessLicense(dto.getBusinessLicense());
        profile.setIdCard(dto.getIdCard());
        profile.setLevel(1);
        profile.setShopStatus(0);
        profile.setRating(new BigDecimal("5.00"));
        profile.setTotalSales(BigDecimal.ZERO);
        merchantProfileMapper.insert(profile);

        createWallet(user.getId());
    }

    /**
     * 登录：校验密码与状态，签发 JWT
     */
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null || !BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("账号未通过审核或已被封禁，请联系管理员");
        }
        String token = jwtUtil.createToken(user.getId(), user.getRole());
        user.setPassword(null);
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUser(user);
        return vo;
    }

    public User currentUser(Long userId) {
        User u = userMapper.selectById(userId);
        if (u != null) {
            u.setPassword(null);
        }
        return u;
    }

    private void checkUsername(String username) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
    }

    private void createWallet(Long userId) {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setPoints(0);
        walletMapper.insert(wallet);
    }

    static class CaptchaEntry {
        String code;
        long expireAt;

        CaptchaEntry(String code, long expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }
    }
}

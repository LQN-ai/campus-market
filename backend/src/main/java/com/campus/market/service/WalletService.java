package com.campus.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.market.common.BusinessException;
import com.campus.market.entity.PointTransaction;
import com.campus.market.entity.Wallet;
import com.campus.market.entity.WalletTransaction;
import com.campus.market.mapper.PointTransactionMapper;
import com.campus.market.mapper.WalletMapper;
import com.campus.market.mapper.WalletTransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 钱包服务：余额、积分、流水
 */
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletMapper walletMapper;
    private final WalletTransactionMapper walletTransactionMapper;
    private final PointTransactionMapper pointTransactionMapper;

    public Wallet getOrCreate(Long userId) {
        Wallet w = walletMapper.selectOne(
                new LambdaQueryWrapper<Wallet>().eq(Wallet::getUserId, userId));
        if (w == null) {
            w = new Wallet();
            w.setUserId(userId);
            w.setBalance(BigDecimal.ZERO);
            w.setPoints(0);
            walletMapper.insert(w);
        }
        return w;
    }

    /**
     * 余额变动（amount 可为正负），并记录流水
     */
    public void addBalance(Long userId, BigDecimal amount, String type, Long orderId, String description) {
        Wallet w = getOrCreate(userId);
        w.setBalance(w.getBalance().add(amount));
        walletMapper.updateById(w);

        WalletTransaction t = new WalletTransaction();
        t.setUserId(userId);
        t.setType(type);
        t.setAmount(amount);
        t.setOrderId(orderId);
        t.setDescription(description);
        walletTransactionMapper.insert(t);
    }

    /**
     * 积分变动，并记录流水
     */
    public void addPoints(Long userId, int points, String type, String description) {
        Wallet w = getOrCreate(userId);
        w.setPoints(w.getPoints() + points);
        walletMapper.updateById(w);

        PointTransaction t = new PointTransaction();
        t.setUserId(userId);
        t.setType(type);
        t.setPoints(points);
        t.setDescription(description);
        pointTransactionMapper.insert(t);
    }

    /**
     * 管理员充值
     */
    public void recharge(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("充值金额必须大于0");
        }
        addBalance(userId, amount, "RECHARGE", null, "管理员充值");
    }

    /**
     * 积分抵扣：每100积分抵扣1元
     */
    public void redeem(Long userId, int points) {
        if (points < 100) {
            throw new BusinessException("至少抵扣100积分");
        }
        if (points % 100 != 0) {
            throw new BusinessException("积分必须为100的整数倍");
        }
        Wallet w = getOrCreate(userId);
        if (w.getPoints() < points) {
            throw new BusinessException("积分不足");
        }
        BigDecimal cash = BigDecimal.valueOf(points / 100L);
        w.setPoints(w.getPoints() - points);
        w.setBalance(w.getBalance().add(cash));
        walletMapper.updateById(w);

        PointTransaction pt = new PointTransaction();
        pt.setUserId(userId);
        pt.setType("REDEEM");
        pt.setPoints(-points);
        pt.setDescription("积分抵扣" + cash + "元");
        pointTransactionMapper.insert(pt);

        WalletTransaction wt = new WalletTransaction();
        wt.setUserId(userId);
        wt.setType("RECHARGE");
        wt.setAmount(cash);
        wt.setDescription("积分抵扣转入");
        walletTransactionMapper.insert(wt);
    }

    public List<WalletTransaction> transactions(Long userId) {
        return walletTransactionMapper.selectList(
                new LambdaQueryWrapper<WalletTransaction>()
                        .eq(WalletTransaction::getUserId, userId)
                        .orderByDesc(WalletTransaction::getCreatedAt));
    }
}

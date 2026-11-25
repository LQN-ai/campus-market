package com.campus.market.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.market.common.BusinessException;
import com.campus.market.dto.OrderCreateDTO;
import com.campus.market.entity.*;
import com.campus.market.mapper.*;
import com.campus.market.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单服务：下单、资金托管、结算、退货
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final CartItemMapper cartItemMapper;
    private final MerchantProfileMapper merchantProfileMapper;
    private final BlacklistMapper blacklistMapper;
    private final UserMapper userMapper;
    private final WalletService walletService;

    /**
     * 商家等级对应费率
     */
    public static BigDecimal feeRate(int level) {
        return switch (level) {
            case 1 -> new BigDecimal("0.001");
            case 2 -> new BigDecimal("0.002");
            case 3 -> new BigDecimal("0.005");
            case 4 -> new BigDecimal("0.0075");
            case 5 -> new BigDecimal("0.01");
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * 下单：直接购买单个商品，或从购物车一键下单（按商家拆单）
     */
    @Transactional
    public List<Order> create(Long userId, OrderCreateDTO dto) {
        List<OrderItemInput> inputs = new ArrayList<>();
        if (dto.getProductId() != null) {
            int qty = dto.getQuantity() == null ? 1 : dto.getQuantity();
            inputs.add(new OrderItemInput(dto.getProductId(), qty));
        } else if (dto.getCartItemIds() != null && !dto.getCartItemIds().isEmpty()) {
            List<CartItem> cartItems = cartItemMapper.selectBatchIds(dto.getCartItemIds());
            for (CartItem c : cartItems) {
                if (!c.getUserId().equals(userId)) {
                    throw new BusinessException("购物车数据异常");
                }
                inputs.add(new OrderItemInput(c.getProductId(), c.getQuantity()));
            }
        } else {
            throw new BusinessException("下单参数错误");
        }

        // 按商家分组
        Map<Long, List<OrderItemInput>> grouped = new LinkedHashMap<>();
        for (OrderItemInput in : inputs) {
            Product p = productMapper.selectById(in.productId);
            if (p == null) {
                throw new BusinessException("商品不存在");
            }
            grouped.computeIfAbsent(p.getMerchantId(), k -> new ArrayList<>()).add(in);
        }

        List<Order> created = new ArrayList<>();
        for (Map.Entry<Long, List<OrderItemInput>> entry : grouped.entrySet()) {
            created.add(createSingleOrder(userId, entry.getKey(), entry.getValue(), dto.getTradeTime(), dto.getTradeLocation()));
        }
        // 购物车下单成功后移除对应项
        if (dto.getCartItemIds() != null && !dto.getCartItemIds().isEmpty()) {
            cartItemMapper.deleteBatchIds(dto.getCartItemIds());
        }
        return created;
    }

    private Order createSingleOrder(Long userId, Long merchantId, List<OrderItemInput> inputs, String tradeTime, String tradeLocation) {
        // 拉黑校验（可选功能）
        Long black = blacklistMapper.selectCount(new LambdaQueryWrapper<Blacklist>()
                .eq(Blacklist::getUserId, userId)
                .and(w -> w.eq(Blacklist::getMerchantId, merchantId).or().isNull(Blacklist::getMerchantId)));
        if (black > 0) {
            throw new BusinessException("您已被限制购买该商家的商品");
        }

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemInput in : inputs) {
            Product p = productMapper.selectById(in.productId);
            if (!"PUBLISHED".equals(p.getStatus())) {
                throw new BusinessException("商品「" + p.getName() + "」已下架或不可购买");
            }
            if (p.getStock() < in.quantity) {
                throw new BusinessException("商品「" + p.getName() + "」库存不足");
            }
            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(in.quantity)));

            OrderItem oi = new OrderItem();
            oi.setProductId(p.getId());
            oi.setQuantity(in.quantity);
            oi.setPrice(p.getPrice());
            items.add(oi);
        }

        // 校验余额
        BigDecimal balance = walletService.getOrCreate(userId).getBalance();
        if (balance.compareTo(total) < 0) {
            throw new BusinessException("钱包余额不足，请先充值");
        }

        // 扣库存、加销量
        for (OrderItemInput in : inputs) {
            Product p = productMapper.selectById(in.productId);
            p.setStock(p.getStock() - in.quantity);
            p.setSales((p.getSales() == null ? 0 : p.getSales()) + in.quantity);
            if (p.getStock() <= 0) {
                p.setStatus("LOCKED");
            }
            productMapper.updateById(p);
        }

        // 创建订单
        Order order = new Order();
        order.setOrderNo(DateUtil.format(new Date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(6));
        order.setUserId(userId);
        order.setMerchantId(merchantId);
        order.setTotalAmount(total);
        order.setStatus("PENDING");
        order.setTradeTime(tradeTime);
        order.setTradeLocation(tradeLocation);
        orderMapper.insert(order);

        for (OrderItem oi : items) {
            oi.setOrderId(order.getId());
            orderItemMapper.insert(oi);
        }

        // 买家扣款（资金进入托管）
        walletService.addBalance(userId, total.negate(), "PAY", order.getId(), "购买商品，资金托管");
        // 消费赠送积分（1元=1积分）
        int points = total.setScale(0, RoundingMode.DOWN).intValue();
        if (points > 0) {
            walletService.addPoints(userId, points, "EARN", "消费赠送积分");
        }
        return order;
    }

    /**
     * 买家确认收货 -> 结算货款给商家（扣除手续费）
     */
    @Transactional
    public void confirm(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许确认收货");
        }
        order.setStatus("CONFIRMED");
        order.setConfirmedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        settle(order);
    }

    /**
     * 申请退货：确认收货后24小时内
     */
    @Transactional
    public void requestReturn(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (!"CONFIRMED".equals(order.getStatus()) && !"AUTO_CONFIRMED".equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许退货");
        }
        if (order.getConfirmedAt() == null
                || order.getConfirmedAt().plusHours(24).isBefore(LocalDateTime.now())) {
            throw new BusinessException("超过收货24小时，无法退货");
        }
        order.setStatus("RETURN_REQUESTED");
        orderMapper.updateById(order);
    }

    /**
     * 商家审核退货
     */
    @Transactional
    public void auditReturn(Long merchantId, Long orderId, boolean approve) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new BusinessException("订单不存在");
        }
        if (!"RETURN_REQUESTED".equals(order.getStatus())) {
            throw new BusinessException("订单不在退货申请状态");
        }
        if (approve) {
            order.setStatus("RETURNED");
            orderMapper.updateById(order);
            refund(order);
            // 恢复库存、扣减销量
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
            for (OrderItem oi : items) {
                Product p = productMapper.selectById(oi.getProductId());
                if (p != null) {
                    p.setStock(p.getStock() + oi.getQuantity());
                    int newSales = Math.max(0, (p.getSales() == null ? 0 : p.getSales()) - oi.getQuantity());
                    p.setSales(newSales);
                    if ("LOCKED".equals(p.getStatus())) {
                        p.setStatus("PUBLISHED");
                    }
                    productMapper.updateById(p);
                }
            }
        } else {
            order.setStatus("CONFIRMED");
            orderMapper.updateById(order);
        }
    }

    /**
     * 7天未确认自动确认收货
     */
    @Scheduled(fixedDelay = 3600000L)
    @Transactional
    public void autoConfirm() {
        List<Order> pending = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, "PENDING"));
        LocalDateTime deadline = LocalDateTime.now().minusDays(7);
        for (Order order : pending) {
            if (order.getCreatedAt() != null && order.getCreatedAt().isBefore(deadline)) {
                order.setStatus("AUTO_CONFIRMED");
                order.setConfirmedAt(LocalDateTime.now());
                orderMapper.updateById(order);
                settle(order);
            }
        }
    }

    private void settle(Order order) {
        MerchantProfile profile = merchantProfileMapper.selectOne(
                new LambdaQueryWrapper<MerchantProfile>().eq(MerchantProfile::getUserId, order.getMerchantId()));
        BigDecimal rate = feeRate(profile == null ? 1 : profile.getLevel());
        BigDecimal fee = order.getTotalAmount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal net = order.getTotalAmount().subtract(fee);
        walletService.addBalance(order.getMerchantId(), net, "RECEIVE", order.getId(),
                "货款结算（等级" + (profile == null ? 1 : profile.getLevel()) + "，手续费" + fee + "元）");
        if (profile != null) {
            profile.setTotalSales(profile.getTotalSales().add(order.getTotalAmount()));
            merchantProfileMapper.updateById(profile);
        }
    }

    private void refund(Order order) {
        MerchantProfile profile = merchantProfileMapper.selectOne(
                new LambdaQueryWrapper<MerchantProfile>().eq(MerchantProfile::getUserId, order.getMerchantId()));
        BigDecimal rate = feeRate(profile == null ? 1 : profile.getLevel());
        BigDecimal fee = order.getTotalAmount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal net = order.getTotalAmount().subtract(fee);
        walletService.addBalance(order.getUserId(), order.getTotalAmount(), "REFUND", order.getId(), "退货退款");
        walletService.addBalance(order.getMerchantId(), net.negate(), "REFUND", order.getId(), "退货扣除货款");
    }

    public OrderVO detail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return toVO(List.of(order)).get(0);
    }

    /**
     * 买家订单列表
     */
    public List<OrderVO> listByUser(Long userId) {
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId).orderByDesc(Order::getCreatedAt));
        return toVO(orders);
    }

    /**
     * 商家订单列表
     */
    public List<OrderVO> listByMerchant(Long merchantId) {
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().eq(Order::getMerchantId, merchantId).orderByDesc(Order::getCreatedAt));
        return toVO(orders);
    }

    private List<OrderVO> toVO(List<Order> orders) {
        List<OrderVO> result = new ArrayList<>();
        for (Order order : orders) {
            OrderVO vo = new OrderVO();
            vo.setOrder(order);
            MerchantProfile profile = merchantProfileMapper.selectOne(
                    new LambdaQueryWrapper<MerchantProfile>().eq(MerchantProfile::getUserId, order.getMerchantId()));
            vo.setShopName(profile == null ? null : profile.getShopName());
            User buyer = userMapper.selectById(order.getUserId());
            vo.setBuyerName(buyer == null ? null : buyer.getRealName());
            vo.setBuyerRating(buyer == null ? null : buyer.getRating());
            Long black = blacklistMapper.selectCount(new LambdaQueryWrapper<Blacklist>()
                    .eq(Blacklist::getUserId, order.getUserId())
                    .eq(Blacklist::getMerchantId, order.getMerchantId()));
            vo.setBuyerBlacklisted(black > 0);

            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
            List<OrderVO.Item> voItems = new ArrayList<>();
            for (OrderItem oi : items) {
                OrderVO.Item it = new OrderVO.Item();
                it.setProductId(oi.getProductId());
                it.setQuantity(oi.getQuantity());
                it.setPrice(oi.getPrice());
                Product p = productMapper.selectById(oi.getProductId());
                if (p != null) {
                    it.setProductName(p.getName());
                    ProductImage img = productImageMapper.selectOne(
                            new LambdaQueryWrapper<ProductImage>()
                                    .eq(ProductImage::getProductId, p.getId())
                                    .orderByAsc(ProductImage::getSortOrder)
                                    .last("LIMIT 1"));
                    it.setImage(img == null ? null : img.getUrl());
                }
                voItems.add(it);
            }
            vo.setItems(voItems);
            result.add(vo);
        }
        return result;
    }

    static class OrderItemInput {
        Long productId;
        int quantity;

        OrderItemInput(Long productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
    }
}

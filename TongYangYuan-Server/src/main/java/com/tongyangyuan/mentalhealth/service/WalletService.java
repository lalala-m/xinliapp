package com.tongyangyuan.mentalhealth.service;

import com.tongyangyuan.mentalhealth.entity.*;
import com.tongyangyuan.mentalhealth.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    @Autowired
    private UserWalletRepository userWalletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private RechargePackageRepository rechargePackageRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    /**
     * 获取或创建用户钱包
     */
    @Transactional
    public UserWallet getOrCreateWallet(Long userId) {
        Optional<UserWallet> walletOpt = userWalletRepository.findByUserId(userId);
        if (walletOpt.isPresent()) {
            return walletOpt.get();
        }
        UserWallet wallet = new UserWallet();
        wallet.setUserId(userId);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setTotalRecharged(BigDecimal.ZERO);
        wallet.setTotalConsumed(BigDecimal.ZERO);
        userWalletRepository.save(wallet);
        logger.info("创建用户钱包: userId={}", userId);
        return wallet;
    }

    /**
     * 获取钱包余额
     */
    public BigDecimal getBalance(Long userId) {
        Optional<UserWallet> walletOpt = userWalletRepository.findByUserId(userId);
        return walletOpt.map(UserWallet::getBalance).orElse(BigDecimal.ZERO);
    }

    /**
     * 检查余额是否充足
     */
    public boolean hasEnoughBalance(Long userId, BigDecimal amount) {
        return getBalance(userId).compareTo(amount) >= 0;
    }

    /**
     * 充值（支付成功后调用）
     */
    @Transactional
    public boolean recharge(Long userId, BigDecimal amount, String orderNo, String description) {
        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                logger.error("充值金额无效: amount={}", amount);
                return false;
            }

            UserWallet wallet = getOrCreateWallet(userId);
            BigDecimal balanceBefore = wallet.getBalance();

            // 更新钱包余额
            wallet.addBalance(amount);
            userWalletRepository.save(wallet);

            // 记录交易
            WalletTransaction transaction = new WalletTransaction();
            transaction.setUserId(userId);
            transaction.setTransactionNo(generateTransactionNo());
            transaction.setType(WalletTransaction.TYPE_RECHARGE);
            transaction.setAmount(amount);
            transaction.setBalanceBefore(balanceBefore);
            transaction.setBalanceAfter(wallet.getBalance());
            transaction.setRelatedOrderNo(orderNo);
            transaction.setDescription(description != null ? description : "钱包充值");
            walletTransactionRepository.save(transaction);

            logger.info("钱包充值成功: userId={}, amount={}, balance={}", userId, amount, wallet.getBalance());
            return true;

        } catch (Exception e) {
            logger.error("钱包充值失败", e);
            return false;
        }
    }

    /**
     * 消费（支付预约费用）
     */
    @Transactional
    public boolean consume(Long userId, BigDecimal amount, Long appointmentId, String description) {
        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                logger.error("消费金额无效: amount={}", amount);
                return false;
            }

            UserWallet wallet = getOrCreateWallet(userId);
            if (!wallet.hasEnoughBalance(amount)) {
                logger.warn("余额不足: userId={}, balance={}, required={}", userId, wallet.getBalance(), amount);
                return false;
            }

            BigDecimal balanceBefore = wallet.getBalance();

            // 扣减余额
            wallet.subtractBalance(amount);
            userWalletRepository.save(wallet);

            // 记录交易
            WalletTransaction transaction = new WalletTransaction();
            transaction.setUserId(userId);
            transaction.setTransactionNo(generateTransactionNo());
            transaction.setType(WalletTransaction.TYPE_CONSUME);
            transaction.setAmount(amount);
            transaction.setBalanceBefore(balanceBefore);
            transaction.setBalanceAfter(wallet.getBalance());
            transaction.setRelatedAppointmentId(appointmentId);
            transaction.setDescription(description != null ? description : "预约支付");
            walletTransactionRepository.save(transaction);

            // 更新预约支付状态
            if (appointmentId != null) {
                Optional<Appointment> aptOpt = appointmentRepository.findById(appointmentId);
                if (aptOpt.isPresent()) {
                    Appointment appointment = aptOpt.get();
                    appointment.setPaymentStatus("PAID");
                    appointment.setPaidByWallet(true);
                    appointmentRepository.save(appointment);
                }
            }

            logger.info("钱包消费成功: userId={}, amount={}, balance={}", userId, amount, wallet.getBalance());
            return true;

        } catch (Exception e) {
            logger.error("钱包消费失败", e);
            return false;
        }
    }

    /**
     * 退款
     */
    @Transactional
    public boolean refund(Long userId, BigDecimal amount, Long appointmentId, String description) {
        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                logger.error("退款金额无效: amount={}", amount);
                return false;
            }

            UserWallet wallet = getOrCreateWallet(userId);
            BigDecimal balanceBefore = wallet.getBalance();

            // 增加余额
            wallet.setBalance(wallet.getBalance().add(amount));
            userWalletRepository.save(wallet);

            // 记录交易
            WalletTransaction transaction = new WalletTransaction();
            transaction.setUserId(userId);
            transaction.setTransactionNo(generateTransactionNo());
            transaction.setType(WalletTransaction.TYPE_REFUND);
            transaction.setAmount(amount);
            transaction.setBalanceBefore(balanceBefore);
            transaction.setBalanceAfter(wallet.getBalance());
            transaction.setRelatedAppointmentId(appointmentId);
            transaction.setDescription(description != null ? description : "预约退款");
            walletTransactionRepository.save(transaction);

            // 更新预约支付状态
            if (appointmentId != null) {
                Optional<Appointment> aptOpt = appointmentRepository.findById(appointmentId);
                if (aptOpt.isPresent()) {
                    Appointment appointment = aptOpt.get();
                    appointment.setPaymentStatus("REFUNDED");
                    appointmentRepository.save(appointment);
                }
            }

            logger.info("钱包退款成功: userId={}, amount={}, balance={}", userId, amount, wallet.getBalance());
            return true;

        } catch (Exception e) {
            logger.error("钱包退款失败", e);
            return false;
        }
    }

    /**
     * 获取交易记录
     */
    public List<WalletTransaction> getTransactions(Long userId, int limit) {
        return walletTransactionRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(0, limit));
    }

    public List<WalletTransaction> getAllTransactions(Long userId) {
        return walletTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 获取充值套餐列表
     */
    public List<RechargePackage> getRechargePackages() {
        return rechargePackageRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }

    /**
     * 获取充值套餐详情
     */
    public Optional<RechargePackage> getRechargePackageByCode(String code) {
        // code格式: wallet_50, wallet_100等
        try {
            String amountStr = code.replace("wallet_", "");
            BigDecimal amount = new BigDecimal(amountStr);
            List<RechargePackage> packages = rechargePackageRepository.findByIsActiveTrueOrderBySortOrderAsc();
            return packages.stream()
                    .filter(p -> p.getAmount().compareTo(amount) == 0)
                    .findFirst();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 判断是否为钱包充值套餐代码
     */
    public boolean isWalletRechargePackage(String packageCode) {
        return packageCode != null && packageCode.startsWith("wallet_");
    }

    /**
     * 获取充值金额（含赠送）
     */
    public BigDecimal getRechargeTotalAmount(String packageCode) {
        Optional<RechargePackage> pkgOpt = getRechargePackageByCode(packageCode);
        return pkgOpt.map(RechargePackage::getTotalAmount).orElse(BigDecimal.ZERO);
    }

    // ========== 私有方法 ==========

    private String generateTransactionNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "WTX" + timestamp + uuid;
    }
}

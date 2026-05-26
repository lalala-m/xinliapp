package com.tongyangyuan.mentalhealth.repository;

import com.tongyangyuan.mentalhealth.entity.WalletTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    List<WalletTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<WalletTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<WalletTransaction> findByTransactionNo(String transactionNo);

    boolean existsByTransactionNo(String transactionNo);
}

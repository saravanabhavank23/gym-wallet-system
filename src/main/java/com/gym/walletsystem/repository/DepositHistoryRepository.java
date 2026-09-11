package com.gym.walletsystem.repository;

import com.gym.walletsystem.entity.DepositHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepositHistoryRepository extends JpaRepository<DepositHistory, Long> {
    List<DepositHistory> findByWalletId(Long walletId);
}
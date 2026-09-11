package com.gym.walletsystem.repository;

import com.gym.walletsystem.entity.PurchaseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseHistoryRepository extends JpaRepository<PurchaseHistory, Long> {
    List<PurchaseHistory> findByCustomerId(Long customerId);
}
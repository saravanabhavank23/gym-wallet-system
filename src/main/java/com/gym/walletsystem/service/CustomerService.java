package com.gym.walletsystem.service;

import com.gym.walletsystem.dto.PurchaseHistoryResponse;
import com.gym.walletsystem.dto.WalletResponse;
import com.gym.walletsystem.entity.*;
import com.gym.walletsystem.exception.InsufficientBalanceException;
import com.gym.walletsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.walletsystem.exception.InsufficientBalanceException;
import com.gym.walletsystem.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final ProductRepository productRepository;
    private final PurchaseHistoryRepository purchaseHistoryRepository;

    public WalletResponse getMyWallet(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        return new WalletResponse(wallet.getBalance());
    }

    public List<Product> browseProducts() {
        return productRepository.findAll();
    }

    @Transactional
    public void purchaseProduct(String email, Long productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (wallet.getBalance().compareTo(product.getPrice()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(product.getPrice()));
        walletRepository.save(wallet);

        PurchaseHistory purchase = new PurchaseHistory();
        purchase.setCustomer(user);
        purchase.setProductName(product.getName());
        purchase.setPriceAtPurchase(product.getPrice());
        purchaseHistoryRepository.save(purchase);
    }

    public List<PurchaseHistoryResponse> getMyPurchases(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return purchaseHistoryRepository.findByCustomerId(user.getId()).stream()
                .map(p -> new PurchaseHistoryResponse(p.getProductName(), p.getPriceAtPurchase(), p.getPurchasedAt()))
                .toList();
    }
}
package com.gym.walletsystem.service;

import com.gym.walletsystem.dto.*;
import com.gym.walletsystem.entity.*;
import com.gym.walletsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gym.walletsystem.exception.DuplicateEmailException;
import com.gym.walletsystem.exception.ResourceNotFoundException;
import com.gym.walletsystem.dto.UserResponse;
import com.gym.walletsystem.exception.InsufficientBalanceException;
import com.gym.walletsystem.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final DepositHistoryRepository depositHistoryRepository;
    private final PurchaseHistoryRepository purchaseHistoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createCustomer(CreateCustomerRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already in use");
        }

        User customer = new User();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setRole(Role.CUSTOMER);
        userRepository.save(customer);

        Wallet wallet = new Wallet();
        wallet.setUser(customer);
        walletRepository.save(wallet);

        return new UserResponse(customer.getId(), customer.getName(), customer.getEmail(), customer.getRole());
    }

    @Transactional
    public void depositMoney(Long customerId, DepositRequest request) {
        Wallet wallet = walletRepository.findByUserId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for this customer"));

        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        walletRepository.save(wallet);

        DepositHistory deposit = new DepositHistory();
        deposit.setWallet(wallet);
        deposit.setAmount(request.getAmount());
        depositHistoryRepository.save(deposit);
    }

    public List<UserResponse> getAllCustomers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.CUSTOMER)
                .map(u -> new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole()))
                .toList();
    }

    public WalletResponse getCustomerWallet(Long customerId) {
        Wallet wallet = walletRepository.findByUserId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        return new WalletResponse(wallet.getBalance());
    }

    public List<DepositHistoryResponse> getCustomerDeposits(Long customerId) {
        Wallet wallet = walletRepository.findByUserId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        return depositHistoryRepository.findByWalletId(wallet.getId()).stream()
                .map(d -> new DepositHistoryResponse(d.getAmount(), d.getDepositedAt()))
                .toList();
    }

    public List<PurchaseHistoryResponse> getCustomerPurchases(Long customerId) {
        return purchaseHistoryRepository.findByCustomerId(customerId).stream()
                .map(p -> new PurchaseHistoryResponse(p.getProductName(), p.getPriceAtPurchase(), p.getPurchasedAt()))
                .toList();
    }

    public Product createProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        return productRepository.save(product);
    }

    public Product updateProduct(Long productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        return productRepository.save(product);
    }

    @Transactional
    public void purchaseForCustomer(Long customerId, Long productId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Wallet wallet = walletRepository.findByUserId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (wallet.getBalance().compareTo(product.getPrice()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(product.getPrice()));
        walletRepository.save(wallet);

        PurchaseHistory purchase = new PurchaseHistory();
        purchase.setCustomer(customer);
        purchase.setProductName(product.getName());
        purchase.setPriceAtPurchase(product.getPrice());
        purchaseHistoryRepository.save(purchase);
    }

    public void deleteProduct(Long productId) {
        productRepository.deleteById(productId);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}
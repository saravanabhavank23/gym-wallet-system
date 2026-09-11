package com.gym.walletsystem.controller;

import com.gym.walletsystem.dto.*;
import com.gym.walletsystem.entity.Product;
import com.gym.walletsystem.entity.User;
import com.gym.walletsystem.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.gym.walletsystem.dto.UserResponse;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/customers")
    public UserResponse createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        return adminService.createCustomer(request);
    }

    @PostMapping("/customers/{id}/deposit")
    public String deposit(@PathVariable Long id, @Valid @RequestBody DepositRequest request) {
        adminService.depositMoney(id, request);
        return "Deposit successful";
    }

    @GetMapping("/customers")
    public List<UserResponse> getAllCustomers() {
        return adminService.getAllCustomers();
    }

    @GetMapping("/customers/{id}/wallet")
    public WalletResponse getCustomerWallet(@PathVariable Long id) {
        return adminService.getCustomerWallet(id);
    }

    @GetMapping("/customers/{id}/deposits")
    public List<DepositHistoryResponse> getCustomerDeposits(@PathVariable Long id) {
        return adminService.getCustomerDeposits(id);
    }

    @GetMapping("/customers/{id}/purchases")
    public List<PurchaseHistoryResponse> getCustomerPurchases(@PathVariable Long id) {
        return adminService.getCustomerPurchases(id);
    }

    @PostMapping("/products")
    public Product createProduct(@Valid @RequestBody ProductRequest request) {
        return adminService.createProduct(request);
    }

    @PutMapping("/products/{id}")
    public Product updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return adminService.updateProduct(id, request);
    }

    @DeleteMapping("/products/{id}")
    public String deleteProduct(@PathVariable Long id) {
        adminService.deleteProduct(id);
        return "Product deleted";
    }

    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return adminService.getAllProducts();
    }
}
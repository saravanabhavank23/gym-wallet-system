package com.gym.walletsystem.controller;

import com.gym.walletsystem.dto.PurchaseHistoryResponse;
import com.gym.walletsystem.dto.WalletResponse;
import com.gym.walletsystem.entity.Product;
import com.gym.walletsystem.security.CustomUserDetails;
import com.gym.walletsystem.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/wallet")
    public WalletResponse getMyWallet(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return customerService.getMyWallet(userDetails.getUsername());
    }

    @GetMapping("/products")
    public List<Product> browseProducts() {
        return customerService.browseProducts();
    }

    @PostMapping("/purchase/{productId}")
    public String purchase(@AuthenticationPrincipal CustomUserDetails userDetails,
                           @PathVariable Long productId) {
        customerService.purchaseProduct(userDetails.getUsername(), productId);
        return "Purchase successful";
    }

    @GetMapping("/purchases")
    public List<PurchaseHistoryResponse> getMyPurchases(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return customerService.getMyPurchases(userDetails.getUsername());
    }
}
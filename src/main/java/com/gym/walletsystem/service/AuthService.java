package com.gym.walletsystem.service;

import com.gym.walletsystem.dto.LoginRequest;
import com.gym.walletsystem.dto.LoginResponse;
import com.gym.walletsystem.entity.User;
import com.gym.walletsystem.repository.UserRepository;
import com.gym.walletsystem.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import com.gym.walletsystem.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return new LoginResponse(token, user.getRole().name(), user.getName());
    }
}
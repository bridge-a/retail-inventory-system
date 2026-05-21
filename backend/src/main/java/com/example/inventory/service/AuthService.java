package com.example.inventory.service;

import com.example.inventory.dto.LoginRequest;
import com.example.inventory.entity.User;

public interface AuthService {
    User login(LoginRequest request);

    User getUserDetail(Long id);

    Object listUsers();

    void ensureAdmin(Long userId);
}

package com.example.inventory.controller;

import com.example.inventory.dto.LoginRequest;
import com.example.inventory.entity.User;
import com.example.inventory.service.AuthService;

public class UserController {
    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    public User login(LoginRequest request) {
        return authService.login(request);
    }

    public User getUserDetail(Long id) {
        return authService.getUserDetail(id);
    }

    public Object listUsers() {
        return authService.listUsers();
    }
}

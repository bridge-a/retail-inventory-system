package com.example.inventory.service.impl;

import com.example.inventory.dto.LoginRequest;
import com.example.inventory.entity.User;
import com.example.inventory.exception.BusinessException;
import com.example.inventory.mapper.UserMapper;
import com.example.inventory.service.AuthService;

public class AuthServiceImpl implements AuthService {
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_EMPLOYEE = "EMPLOYEE";

    private static final int STATUS_ENABLED = 1;

    private final UserMapper userMapper;

    public AuthServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User login(LoginRequest request) {
        validateLoginRequest(request);

        User user = userMapper.findByUsername(request.getUsername().trim());
        if (user == null || !request.getPassword().equals(user.getPassword())) {
            throw new BusinessException("Invalid username or password.");
        }
        ensureEnabled(user);
        return user;
    }

    @Override
    public User getUserDetail(Long id) {
        return getExistingUser(id);
    }

    @Override
    public Object listUsers() {
        return userMapper.findAll();
    }

    @Override
    public void ensureAdmin(Long userId) {
        User user = getExistingUser(userId);
        ensureEnabled(user);
        if (!ROLE_ADMIN.equals(user.getRole())) {
            throw new BusinessException("Only admin users can approve purchase orders.");
        }
    }

    private void validateLoginRequest(LoginRequest request) {
        if (request == null) {
            throw new BusinessException("Login request is required.");
        }
        if (isBlank(request.getUsername())) {
            throw new BusinessException("Username is required.");
        }
        if (isBlank(request.getPassword())) {
            throw new BusinessException("Password is required.");
        }
    }

    private User getExistingUser(Long id) {
        if (id == null) {
            throw new BusinessException("User id is required.");
        }

        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException("User does not exist.");
        }
        return user;
    }

    private void ensureEnabled(User user) {
        if (user.getStatus() == null || user.getStatus() != STATUS_ENABLED) {
            throw new BusinessException("User is disabled.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

package com.example.inventory.controller;

import com.example.inventory.dto.LoginRequest;
import com.example.inventory.entity.User;
import com.example.inventory.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public User login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/{id}")
    public User getUserDetail(@PathVariable Long id) {
        return authService.getUserDetail(id);
    }

    @GetMapping
    public Object listUsers() {
        return authService.listUsers();
    }
}

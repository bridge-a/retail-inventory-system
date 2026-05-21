package com.example.inventory.mapper;

import com.example.inventory.entity.User;
import java.util.List;

public interface UserMapper {
    User findById(Long id);

    User findByUsername(String username);

    void insert(User user);

    List<User> findAll();
}

package com.example.inventory.mapper.jdbc;

import com.example.inventory.entity.User;
import com.example.inventory.mapper.UserMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class JdbcUserMapper implements UserMapper {
    private final JdbcTemplate jdbcTemplate;

    public JdbcUserMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User findById(Long id) {
        List<User> users = jdbcTemplate.query("SELECT * FROM user_account WHERE id = ?", userRowMapper(), id);
        return users.isEmpty() ? null : users.get(0);
    }

    @Override
    public User findByUsername(String username) {
        List<User> users = jdbcTemplate.query("SELECT * FROM user_account WHERE username = ?", userRowMapper(), username);
        return users.isEmpty() ? null : users.get(0);
    }

    @Override
    public void insert(User user) {
        String sql = """
                INSERT INTO user_account (username, password, real_name, role, status)
                VALUES (?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getRealName());
            statement.setString(4, user.getRole());
            statement.setInt(5, user.getStatus());
            return statement;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            user.setId(keyHolder.getKey().longValue());
        }
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM user_account ORDER BY id", userRowMapper());
    }

    private RowMapper<User> userRowMapper() {
        return (resultSet, rowNumber) -> {
            User user = new User();
            user.setId(resultSet.getLong("id"));
            user.setUsername(resultSet.getString("username"));
            user.setPassword(resultSet.getString("password"));
            user.setRealName(resultSet.getString("real_name"));
            user.setRole(resultSet.getString("role"));
            user.setStatus(resultSet.getInt("status"));
            user.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
            user.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
            return user;
        };
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}

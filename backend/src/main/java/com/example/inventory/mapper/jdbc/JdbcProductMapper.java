package com.example.inventory.mapper.jdbc;

import com.example.inventory.entity.Product;
import com.example.inventory.mapper.ProductMapper;
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
public class JdbcProductMapper implements ProductMapper {
    private final JdbcTemplate jdbcTemplate;

    public JdbcProductMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Product findById(Long id) {
        List<Product> products = jdbcTemplate.query("SELECT * FROM product WHERE id = ?", productRowMapper(), id);
        return products.isEmpty() ? null : products.get(0);
    }

    @Override
    public Product findByCode(String code) {
        List<Product> products = jdbcTemplate.query("SELECT * FROM product WHERE code = ?", productRowMapper(), code);
        return products.isEmpty() ? null : products.get(0);
    }

    @Override
    public void insert(Product product) {
        String sql = """
                INSERT INTO product (category_id, name, code, unit, current_stock, safe_stock, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, product.getCategoryId());
            statement.setString(2, product.getName());
            statement.setString(3, product.getCode());
            statement.setString(4, product.getUnit());
            statement.setInt(5, product.getCurrentStock());
            statement.setInt(6, product.getSafeStock());
            statement.setInt(7, product.getStatus());
            return statement;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            product.setId(keyHolder.getKey().longValue());
        }
    }

    @Override
    public void update(Product product) {
        jdbcTemplate.update("""
                        UPDATE product
                        SET category_id = ?, name = ?, code = ?, unit = ?, current_stock = ?,
                            safe_stock = ?, status = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                product.getCategoryId(),
                product.getName(),
                product.getCode(),
                product.getUnit(),
                product.getCurrentStock(),
                product.getSafeStock(),
                product.getStatus(),
                product.getId()
        );
    }

    @Override
    public List<Product> findAll() {
        return jdbcTemplate.query("SELECT * FROM product ORDER BY id", productRowMapper());
    }

    private RowMapper<Product> productRowMapper() {
        return (resultSet, rowNumber) -> {
            Product product = new Product();
            product.setId(resultSet.getLong("id"));
            product.setCategoryId(resultSet.getLong("category_id"));
            product.setName(resultSet.getString("name"));
            product.setCode(resultSet.getString("code"));
            product.setUnit(resultSet.getString("unit"));
            product.setCurrentStock(resultSet.getInt("current_stock"));
            product.setSafeStock(resultSet.getInt("safe_stock"));
            product.setStatus(resultSet.getInt("status"));
            product.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
            product.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
            return product;
        };
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}

package com.example.inventory.mapper.jdbc;

import com.example.inventory.entity.StockWarning;
import com.example.inventory.mapper.StockWarningMapper;
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
public class JdbcStockWarningMapper implements StockWarningMapper {
    private final JdbcTemplate jdbcTemplate;

    public JdbcStockWarningMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public StockWarning findByProductId(Long productId) {
        List<StockWarning> warnings = jdbcTemplate.query(
                "SELECT * FROM stock_warning WHERE product_id = ? ORDER BY id DESC LIMIT 1",
                stockWarningRowMapper(),
                productId
        );
        return warnings.isEmpty() ? null : warnings.get(0);
    }

    @Override
    public void insert(StockWarning warning) {
        String sql = """
                INSERT INTO stock_warning (product_id, warning_stock, safe_stock, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, warning.getProductId());
            statement.setInt(2, warning.getWarningStock());
            statement.setInt(3, warning.getSafeStock());
            statement.setString(4, warning.getStatus());
            statement.setTimestamp(5, Timestamp.valueOf(warning.getCreatedAt()));
            statement.setTimestamp(6, Timestamp.valueOf(warning.getUpdatedAt()));
            return statement;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            warning.setId(keyHolder.getKey().longValue());
        }
    }

    @Override
    public void update(StockWarning warning) {
        jdbcTemplate.update("""
                        UPDATE stock_warning
                        SET warning_stock = ?, safe_stock = ?, status = ?, updated_at = ?
                        WHERE id = ?
                        """,
                warning.getWarningStock(),
                warning.getSafeStock(),
                warning.getStatus(),
                Timestamp.valueOf(warning.getUpdatedAt()),
                warning.getId()
        );
    }

    @Override
    public List<StockWarning> findActiveWarnings() {
        return jdbcTemplate.query(
                "SELECT * FROM stock_warning WHERE status = 'ACTIVE' ORDER BY id DESC",
                stockWarningRowMapper()
        );
    }

    @Override
    public List<StockWarning> findAll() {
        return jdbcTemplate.query("SELECT * FROM stock_warning ORDER BY id DESC", stockWarningRowMapper());
    }

    private RowMapper<StockWarning> stockWarningRowMapper() {
        return (resultSet, rowNumber) -> {
            StockWarning warning = new StockWarning();
            warning.setId(resultSet.getLong("id"));
            warning.setProductId(resultSet.getLong("product_id"));
            warning.setWarningStock(resultSet.getInt("warning_stock"));
            warning.setSafeStock(resultSet.getInt("safe_stock"));
            warning.setStatus(resultSet.getString("status"));
            warning.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
            warning.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
            return warning;
        };
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}

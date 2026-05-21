package com.example.inventory.mapper.jdbc;

import com.example.inventory.entity.PurchaseOrderItem;
import com.example.inventory.mapper.PurchaseOrderItemMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class JdbcPurchaseOrderItemMapper implements PurchaseOrderItemMapper {
    private final JdbcTemplate jdbcTemplate;

    public JdbcPurchaseOrderItemMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(PurchaseOrderItem item) {
        String sql = """
                INSERT INTO purchase_order_item (order_id, product_id, quantity, remark)
                VALUES (?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, item.getOrderId());
            statement.setLong(2, item.getProductId());
            statement.setInt(3, item.getQuantity());
            statement.setString(4, item.getRemark());
            return statement;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            item.setId(keyHolder.getKey().longValue());
        }
    }

    @Override
    public List<PurchaseOrderItem> findByOrderId(Long orderId) {
        return jdbcTemplate.query(
                "SELECT * FROM purchase_order_item WHERE order_id = ? ORDER BY id",
                purchaseOrderItemRowMapper(),
                orderId
        );
    }

    private RowMapper<PurchaseOrderItem> purchaseOrderItemRowMapper() {
        return (resultSet, rowNumber) -> {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setId(resultSet.getLong("id"));
            item.setOrderId(resultSet.getLong("order_id"));
            item.setProductId(resultSet.getLong("product_id"));
            item.setQuantity(resultSet.getInt("quantity"));
            item.setRemark(resultSet.getString("remark"));
            return item;
        };
    }
}

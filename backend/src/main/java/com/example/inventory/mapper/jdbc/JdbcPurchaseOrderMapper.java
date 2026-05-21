package com.example.inventory.mapper.jdbc;

import com.example.inventory.entity.PurchaseOrder;
import com.example.inventory.mapper.PurchaseOrderMapper;
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
public class JdbcPurchaseOrderMapper implements PurchaseOrderMapper {
    private final JdbcTemplate jdbcTemplate;

    public JdbcPurchaseOrderMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(PurchaseOrder order) {
        String sql = """
                INSERT INTO purchase_order
                    (applicant_id, status, reason, approver_id, approval_remark, approved_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, order.getApplicantId());
            statement.setString(2, order.getStatus());
            statement.setString(3, order.getReason());
            statement.setObject(4, order.getApproverId());
            statement.setString(5, order.getApprovalRemark());
            statement.setTimestamp(6, toTimestamp(order.getApprovedAt()));
            return statement;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            order.setId(keyHolder.getKey().longValue());
        }
    }

    @Override
    public PurchaseOrder findById(Long id) {
        List<PurchaseOrder> orders = jdbcTemplate.query(
                "SELECT * FROM purchase_order WHERE id = ?",
                purchaseOrderRowMapper(),
                id
        );
        return orders.isEmpty() ? null : orders.get(0);
    }

    @Override
    public void update(PurchaseOrder order) {
        jdbcTemplate.update("""
                        UPDATE purchase_order
                        SET applicant_id = ?, status = ?, reason = ?, approver_id = ?,
                            approval_remark = ?, approved_at = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                order.getApplicantId(),
                order.getStatus(),
                order.getReason(),
                order.getApproverId(),
                order.getApprovalRemark(),
                toTimestamp(order.getApprovedAt()),
                order.getId()
        );
    }

    @Override
    public List<PurchaseOrder> findAll() {
        return jdbcTemplate.query("SELECT * FROM purchase_order ORDER BY id DESC", purchaseOrderRowMapper());
    }

    private RowMapper<PurchaseOrder> purchaseOrderRowMapper() {
        return (resultSet, rowNumber) -> {
            PurchaseOrder order = new PurchaseOrder();
            order.setId(resultSet.getLong("id"));
            order.setApplicantId(resultSet.getLong("applicant_id"));
            order.setStatus(resultSet.getString("status"));
            order.setReason(resultSet.getString("reason"));
            order.setApproverId(getNullableLong(resultSet, "approver_id"));
            order.setApprovalRemark(resultSet.getString("approval_remark"));
            order.setApprovedAt(toLocalDateTime(resultSet.getTimestamp("approved_at")));
            order.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
            order.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
            return order;
        };
    }

    private Long getNullableLong(java.sql.ResultSet resultSet, String column) throws java.sql.SQLException {
        long value = resultSet.getLong(column);
        return resultSet.wasNull() ? null : value;
    }

    private Timestamp toTimestamp(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}

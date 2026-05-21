package com.example.inventory.mapper.jdbc;

import com.example.inventory.entity.StockRecord;
import com.example.inventory.mapper.StockRecordMapper;
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
public class JdbcStockRecordMapper implements StockRecordMapper {
    private final JdbcTemplate jdbcTemplate;

    public JdbcStockRecordMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(StockRecord stockRecord) {
        String sql = """
                INSERT INTO stock_record
                    (product_id, change_type, quantity, before_stock, after_stock, source_type, source_id, operator_id, remark, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, stockRecord.getProductId());
            statement.setString(2, stockRecord.getChangeType());
            statement.setInt(3, stockRecord.getQuantity());
            statement.setInt(4, stockRecord.getBeforeStock());
            statement.setInt(5, stockRecord.getAfterStock());
            statement.setString(6, stockRecord.getSourceType());
            statement.setObject(7, stockRecord.getSourceId());
            statement.setObject(8, stockRecord.getOperatorId());
            statement.setString(9, stockRecord.getRemark());
            statement.setTimestamp(10, Timestamp.valueOf(stockRecord.getCreatedAt()));
            return statement;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            stockRecord.setId(keyHolder.getKey().longValue());
        }
    }

    @Override
    public List<StockRecord> findByProductId(Long productId) {
        return jdbcTemplate.query(
                "SELECT * FROM stock_record WHERE product_id = ? ORDER BY id DESC",
                stockRecordRowMapper(),
                productId
        );
    }

    @Override
    public List<StockRecord> findAll() {
        return jdbcTemplate.query("SELECT * FROM stock_record ORDER BY id DESC", stockRecordRowMapper());
    }

    private RowMapper<StockRecord> stockRecordRowMapper() {
        return (resultSet, rowNumber) -> {
            StockRecord record = new StockRecord();
            record.setId(resultSet.getLong("id"));
            record.setProductId(resultSet.getLong("product_id"));
            record.setChangeType(resultSet.getString("change_type"));
            record.setQuantity(resultSet.getInt("quantity"));
            record.setBeforeStock(resultSet.getInt("before_stock"));
            record.setAfterStock(resultSet.getInt("after_stock"));
            record.setSourceType(resultSet.getString("source_type"));
            record.setSourceId(getNullableLong(resultSet, "source_id"));
            record.setOperatorId(getNullableLong(resultSet, "operator_id"));
            record.setRemark(resultSet.getString("remark"));
            record.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
            return record;
        };
    }

    private Long getNullableLong(java.sql.ResultSet resultSet, String column) throws java.sql.SQLException {
        long value = resultSet.getLong(column);
        return resultSet.wasNull() ? null : value;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}

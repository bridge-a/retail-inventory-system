package com.example.inventory;

import com.example.inventory.dto.StockInRequest;
import com.example.inventory.dto.StockOutRequest;
import com.example.inventory.entity.Product;
import com.example.inventory.entity.StockRecord;
import com.example.inventory.exception.BusinessException;
import com.example.inventory.mapper.ProductMapper;
import com.example.inventory.mapper.StockRecordMapper;
import com.example.inventory.service.StockWarningService;
import com.example.inventory.service.impl.StockServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockFlowIntegrationTest {
    public static void main(String[] args) {
        shouldCreateStockRecordWhenStockIn();
        shouldCreateStockRecordWhenStockOut();
        shouldRejectStockOutWhenQuantityExceedsCurrentStock();
        shouldRejectInvalidStockOutQuantity();

        System.out.println("StockFlowIntegrationTest passed.");
    }

    private static void shouldCreateStockRecordWhenStockIn() {
        FakeProductMapper productMapper = new FakeProductMapper();
        FakeStockRecordMapper stockRecordMapper = new FakeStockRecordMapper();
        FakeStockWarningService stockWarningService = new FakeStockWarningService();

        Product product = createProduct(1L, 5, 10);
        productMapper.save(product);

        StockServiceImpl stockService = new StockServiceImpl(productMapper, stockRecordMapper, stockWarningService);

        StockInRequest request = new StockInRequest();
        request.setProductId(1L);
        request.setQuantity(3);
        request.setOperatorId(100L);
        request.setRemark("manual stock in");

        stockService.stockIn(request);

        assertEquals(8, productMapper.findById(1L).getCurrentStock(), "stock-in should increase currentStock");
        assertEquals(1, stockRecordMapper.records.size(), "stock-in should create one stock record");

        StockRecord record = stockRecordMapper.records.get(0);
        assertEquals("IN", record.getChangeType(), "stock-in record type should be IN");
        assertEquals(5, record.getBeforeStock(), "stock-in beforeStock should be recorded");
        assertEquals(8, record.getAfterStock(), "stock-in afterStock should be recorded");
        assertEquals(1L, stockWarningService.lastRefreshedProductId, "stock-in should refresh warning");
    }

    private static void shouldCreateStockRecordWhenStockOut() {
        FakeProductMapper productMapper = new FakeProductMapper();
        FakeStockRecordMapper stockRecordMapper = new FakeStockRecordMapper();
        FakeStockWarningService stockWarningService = new FakeStockWarningService();

        Product product = createProduct(2L, 8, 10);
        productMapper.save(product);

        StockServiceImpl stockService = new StockServiceImpl(productMapper, stockRecordMapper, stockWarningService);

        StockOutRequest request = new StockOutRequest();
        request.setProductId(2L);
        request.setQuantity(3);
        request.setOperatorId(100L);
        request.setRemark("manual stock out");

        stockService.stockOut(request);

        assertEquals(5, productMapper.findById(2L).getCurrentStock(), "stock-out should decrease currentStock");
        assertEquals(1, stockRecordMapper.records.size(), "stock-out should create one stock record");

        StockRecord record = stockRecordMapper.records.get(0);
        assertEquals("OUT", record.getChangeType(), "stock-out record type should be OUT");
        assertEquals(8, record.getBeforeStock(), "stock-out beforeStock should be recorded");
        assertEquals(5, record.getAfterStock(), "stock-out afterStock should be recorded");
        assertEquals(2L, stockWarningService.lastRefreshedProductId, "stock-out should refresh warning");
    }

    private static void shouldRejectStockOutWhenQuantityExceedsCurrentStock() {
        FakeProductMapper productMapper = new FakeProductMapper();
        FakeStockRecordMapper stockRecordMapper = new FakeStockRecordMapper();
        FakeStockWarningService stockWarningService = new FakeStockWarningService();

        Product product = createProduct(3L, 5, 10);
        productMapper.save(product);

        StockServiceImpl stockService = new StockServiceImpl(productMapper, stockRecordMapper, stockWarningService);

        StockOutRequest request = new StockOutRequest();
        request.setProductId(3L);
        request.setQuantity(10);
        request.setOperatorId(100L);

        assertThrows(
                BusinessException.class,
                () -> stockService.stockOut(request),
                "stock-out should fail when quantity exceeds currentStock"
        );

        assertEquals(5, productMapper.findById(3L).getCurrentStock(), "stock should remain unchanged");
        assertEquals(0, stockRecordMapper.records.size(), "failed stock-out should not create stock record");
    }

    private static void shouldRejectInvalidStockOutQuantity() {
        FakeProductMapper productMapper = new FakeProductMapper();
        FakeStockRecordMapper stockRecordMapper = new FakeStockRecordMapper();
        FakeStockWarningService stockWarningService = new FakeStockWarningService();

        Product product = createProduct(4L, 5, 10);
        productMapper.save(product);

        StockServiceImpl stockService = new StockServiceImpl(productMapper, stockRecordMapper, stockWarningService);

        StockOutRequest request = new StockOutRequest();
        request.setProductId(4L);
        request.setQuantity(0);
        request.setOperatorId(100L);

        assertThrows(
                BusinessException.class,
                () -> stockService.stockOut(request),
                "stock-out should reject zero quantity"
        );
    }

    private static Product createProduct(Long id, Integer currentStock, Integer safeStock) {
        Product product = new Product();
        product.setId(id);
        product.setCategoryId(1L);
        product.setName("Test Product");
        product.setCode("P-" + id);
        product.setUnit("pcs");
        product.setCurrentStock(currentStock);
        product.setSafeStock(safeStock);
        product.setStatus(1);
        return product;
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && expected.equals(actual)) {
            return;
        }
        throw new AssertionError(message + ", expected=" + expected + ", actual=" + actual);
    }

    private static void assertThrows(Class<? extends Throwable> expectedType, Runnable action, String message) {
        try {
            action.run();
        } catch (Throwable actual) {
            if (expectedType.isInstance(actual)) {
                return;
            }
            throw new AssertionError(message + ", unexpected exception=" + actual.getClass().getName());
        }
        throw new AssertionError(message + ", expected exception=" + expectedType.getName());
    }

    private static class FakeProductMapper implements ProductMapper {
        private final Map<Long, Product> products = new HashMap<>();

        void save(Product product) {
            products.put(product.getId(), product);
        }

        @Override
        public Product findById(Long id) {
            return products.get(id);
        }

        @Override
        public Product findByCode(String code) {
            for (Product product : products.values()) {
                if (code.equals(product.getCode())) {
                    return product;
                }
            }
            return null;
        }

        @Override
        public void insert(Product product) {
            products.put(product.getId(), product);
        }

        @Override
        public void update(Product product) {
            products.put(product.getId(), product);
        }

        @Override
        public List<Product> findAll() {
            return new ArrayList<>(products.values());
        }
    }

    private static class FakeStockRecordMapper implements StockRecordMapper {
        private final List<StockRecord> records = new ArrayList<>();

        @Override
        public void insert(StockRecord stockRecord) {
            records.add(stockRecord);
        }

        @Override
        public List<StockRecord> findByProductId(Long productId) {
            List<StockRecord> result = new ArrayList<>();
            for (StockRecord record : records) {
                if (productId.equals(record.getProductId())) {
                    result.add(record);
                }
            }
            return result;
        }

        @Override
        public List<StockRecord> findAll() {
            return records;
        }
    }

    private static class FakeStockWarningService implements StockWarningService {
        private Long lastRefreshedProductId;

        @Override
        public void refreshWarning(Long productId) {
            lastRefreshedProductId = productId;
        }

        @Override
        public Object listActiveWarnings() {
            return new ArrayList<>();
        }

        @Override
        public Object listAllWarnings() {
            return new ArrayList<>();
        }
    }
}

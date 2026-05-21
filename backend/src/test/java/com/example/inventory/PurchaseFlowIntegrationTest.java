package com.example.inventory;

import com.example.inventory.dto.PurchaseApproveRequest;
import com.example.inventory.dto.PurchaseCompleteRequest;
import com.example.inventory.dto.PurchaseCreateRequest;
import com.example.inventory.dto.PurchaseItemRequest;
import com.example.inventory.dto.StockInRequest;
import com.example.inventory.dto.StockOutRequest;
import com.example.inventory.entity.Product;
import com.example.inventory.entity.PurchaseOrder;
import com.example.inventory.entity.PurchaseOrderItem;
import com.example.inventory.exception.BusinessException;
import com.example.inventory.mapper.ProductMapper;
import com.example.inventory.mapper.PurchaseOrderItemMapper;
import com.example.inventory.mapper.PurchaseOrderMapper;
import com.example.inventory.service.StockService;
import com.example.inventory.service.impl.PurchaseOrderServiceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchaseFlowIntegrationTest {
    public static void main(String[] args) {
        shouldCreatePurchaseOrderWithPendingStatus();
        shouldApprovePendingPurchaseOrder();
        shouldRejectPendingPurchaseOrder();
        shouldRejectApproveWhenOrderIsNotPending();
        shouldRejectReviewWhenOrderIsCompleted();
        shouldRejectCompleteWhenOrderIsNotApproved();
        shouldCompleteApprovedOrderAndCallStockIn();

        System.out.println("PurchaseFlowIntegrationTest passed.");
    }

    private static void shouldCreatePurchaseOrderWithPendingStatus() {
        TestContext context = createContext();
        context.productMapper.save(createProduct(1L));

        PurchaseCreateRequest request = createPurchaseRequest(1L, 5);

        Long orderId = context.service.createPurchaseOrder(request);

        PurchaseOrder order = context.purchaseOrderMapper.findById(orderId);
        assertNotNull(order, "purchase order should be saved");
        assertEquals("PENDING", order.getStatus(), "new purchase order status should be PENDING");
        assertEquals(200L, order.getApplicantId(), "applicant id should be saved");
        assertEquals(1, context.purchaseOrderItemMapper.findByOrderId(orderId).size(), "purchase item should be saved");
    }

    private static void shouldApprovePendingPurchaseOrder() {
        TestContext context = createContext();
        PurchaseOrder order = createOrder(10L, "PENDING");
        context.purchaseOrderMapper.save(order);

        PurchaseApproveRequest request = new PurchaseApproveRequest();
        request.setApproverId(300L);
        request.setApprovalRemark("approved for low stock");

        context.service.approvePurchaseOrder(10L, request);

        PurchaseOrder updated = context.purchaseOrderMapper.findById(10L);
        assertEquals("APPROVED", updated.getStatus(), "pending order should become APPROVED");
        assertEquals(300L, updated.getApproverId(), "approver id should be saved");
        assertEquals("approved for low stock", updated.getApprovalRemark(), "approval remark should be saved");
        assertNotNull(updated.getApprovedAt(), "approvedAt should be filled");
    }

    private static void shouldRejectPendingPurchaseOrder() {
        TestContext context = createContext();
        PurchaseOrder order = createOrder(11L, "PENDING");
        context.purchaseOrderMapper.save(order);

        PurchaseApproveRequest request = new PurchaseApproveRequest();
        request.setApproverId(301L);
        request.setApprovalRemark("quantity is too high");

        context.service.rejectPurchaseOrder(11L, request);

        PurchaseOrder updated = context.purchaseOrderMapper.findById(11L);
        assertEquals("REJECTED", updated.getStatus(), "pending order should become REJECTED");
        assertEquals(301L, updated.getApproverId(), "approver id should be saved");
        assertEquals("quantity is too high", updated.getApprovalRemark(), "reject remark should be saved");
    }

    private static void shouldRejectApproveWhenOrderIsNotPending() {
        TestContext context = createContext();
        PurchaseOrder order = createOrder(12L, "APPROVED");
        context.purchaseOrderMapper.save(order);

        PurchaseApproveRequest request = new PurchaseApproveRequest();
        request.setApproverId(302L);
        request.setApprovalRemark("try approve again");

        assertThrows(
                BusinessException.class,
                () -> context.service.approvePurchaseOrder(12L, request),
                "approved order should not be approved again"
        );

        assertEquals("APPROVED", context.purchaseOrderMapper.findById(12L).getStatus(), "order status should remain unchanged");
    }

    private static void shouldRejectReviewWhenOrderIsCompleted() {
        TestContext context = createContext();
        PurchaseOrder order = createOrder(15L, "COMPLETED");
        context.purchaseOrderMapper.save(order);

        PurchaseApproveRequest request = new PurchaseApproveRequest();
        request.setApproverId(303L);
        request.setApprovalRemark("try review completed order");

        assertThrows(
                BusinessException.class,
                () -> context.service.approvePurchaseOrder(15L, request),
                "completed order should not be approved again"
        );

        assertThrows(
                BusinessException.class,
                () -> context.service.rejectPurchaseOrder(15L, request),
                "completed order should not be rejected again"
        );

        assertEquals("COMPLETED", context.purchaseOrderMapper.findById(15L).getStatus(), "completed order status should remain unchanged");
    }

    private static void shouldRejectCompleteWhenOrderIsNotApproved() {
        TestContext context = createContext();
        PurchaseOrder order = createOrder(13L, "PENDING");
        context.purchaseOrderMapper.save(order);
        context.purchaseOrderItemMapper.save(createOrderItem(13L, 1L, 4));

        PurchaseCompleteRequest request = new PurchaseCompleteRequest();
        request.setOperatorId(400L);
        request.setRemark("goods arrived");

        assertThrows(
                BusinessException.class,
                () -> context.service.completePurchaseOrder(13L, request),
                "only APPROVED order can be completed"
        );

        assertEquals("PENDING", context.purchaseOrderMapper.findById(13L).getStatus(), "order status should remain unchanged");
        assertEquals(0, context.stockService.stockInCallCount, "stockIn should not be called");
    }

    private static void shouldCompleteApprovedOrderAndCallStockIn() {
        TestContext context = createContext();
        PurchaseOrder order = createOrder(14L, "APPROVED");
        context.purchaseOrderMapper.save(order);
        context.purchaseOrderItemMapper.save(createOrderItem(14L, 2L, 6));

        PurchaseCompleteRequest request = new PurchaseCompleteRequest();
        request.setOperatorId(401L);
        request.setRemark("goods arrived");

        context.service.completePurchaseOrder(14L, request);

        assertEquals("COMPLETED", context.purchaseOrderMapper.findById(14L).getStatus(), "approved order should become COMPLETED");
        assertEquals(1, context.stockService.stockInCallCount, "stockIn should be called once");
        assertEquals(2L, context.stockService.lastStockInRequest.getProductId(), "stockIn product id should come from purchase item");
        assertEquals(6, context.stockService.lastStockInRequest.getQuantity(), "stockIn quantity should come from purchase item");
        assertEquals(401L, context.stockService.lastStockInRequest.getOperatorId(), "stockIn operator id should come from complete request");
        assertContains(context.stockService.lastStockInRequest.getRemark(), "orderId=14", "stockIn remark should keep order id");
        assertContains(context.stockService.lastStockInRequest.getRemark(), "goods arrived", "stockIn remark should keep complete remark");
    }

    private static TestContext createContext() {
        FakePurchaseOrderMapper purchaseOrderMapper = new FakePurchaseOrderMapper();
        FakePurchaseOrderItemMapper purchaseOrderItemMapper = new FakePurchaseOrderItemMapper();
        FakeProductMapper productMapper = new FakeProductMapper();
        FakeStockService stockService = new FakeStockService();

        PurchaseOrderServiceImpl service = new PurchaseOrderServiceImpl(
                purchaseOrderMapper,
                purchaseOrderItemMapper,
                productMapper,
                stockService
        );

        return new TestContext(
                service,
                purchaseOrderMapper,
                purchaseOrderItemMapper,
                productMapper,
                stockService
        );
    }

    private static PurchaseCreateRequest createPurchaseRequest(Long productId, Integer quantity) {
        PurchaseItemRequest item = new PurchaseItemRequest();
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setRemark("low stock replenishment");

        PurchaseCreateRequest request = new PurchaseCreateRequest();
        request.setApplicantId(200L);
        request.setReason("stock is lower than safe stock");
        request.setItems(Collections.singletonList(item));
        return request;
    }

    private static Product createProduct(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setCategoryId(1L);
        product.setName("Test Product");
        product.setCode("P-" + id);
        product.setUnit("pcs");
        product.setCurrentStock(3);
        product.setSafeStock(10);
        product.setStatus(1);
        return product;
    }

    private static PurchaseOrder createOrder(Long id, String status) {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(id);
        order.setApplicantId(200L);
        order.setReason("stock replenishment");
        order.setStatus(status);
        return order;
    }

    private static PurchaseOrderItem createOrderItem(Long orderId, Long productId, Integer quantity) {
        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setOrderId(orderId);
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setRemark("purchase item");
        return item;
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

    private static void assertNotNull(Object value, String message) {
        if (value == null) {
            throw new AssertionError(message);
        }
    }

    private static void assertContains(String actual, String expectedPart, String message) {
        if (actual == null || !actual.contains(expectedPart)) {
            throw new AssertionError(message + ", expected to contain=" + expectedPart + ", actual=" + actual);
        }
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

    private static class TestContext {
        private final PurchaseOrderServiceImpl service;
        private final FakePurchaseOrderMapper purchaseOrderMapper;
        private final FakePurchaseOrderItemMapper purchaseOrderItemMapper;
        private final FakeProductMapper productMapper;
        private final FakeStockService stockService;

        private TestContext(
                PurchaseOrderServiceImpl service,
                FakePurchaseOrderMapper purchaseOrderMapper,
                FakePurchaseOrderItemMapper purchaseOrderItemMapper,
                FakeProductMapper productMapper,
                FakeStockService stockService
        ) {
            this.service = service;
            this.purchaseOrderMapper = purchaseOrderMapper;
            this.purchaseOrderItemMapper = purchaseOrderItemMapper;
            this.productMapper = productMapper;
            this.stockService = stockService;
        }
    }

    private static class FakePurchaseOrderMapper implements PurchaseOrderMapper {
        private final Map<Long, PurchaseOrder> orders = new HashMap<>();
        private long nextId = 1L;

        void save(PurchaseOrder order) {
            orders.put(order.getId(), order);
        }

        @Override
        public void insert(PurchaseOrder order) {
            if (order.getId() == null) {
                order.setId(nextId++);
            }
            orders.put(order.getId(), order);
        }

        @Override
        public PurchaseOrder findById(Long id) {
            return orders.get(id);
        }

        @Override
        public void update(PurchaseOrder order) {
            orders.put(order.getId(), order);
        }

        @Override
        public List<PurchaseOrder> findAll() {
            return new ArrayList<>(orders.values());
        }
    }

    private static class FakePurchaseOrderItemMapper implements PurchaseOrderItemMapper {
        private final List<PurchaseOrderItem> items = new ArrayList<>();
        private long nextId = 1L;

        void save(PurchaseOrderItem item) {
            if (item.getId() == null) {
                item.setId(nextId++);
            }
            items.add(item);
        }

        @Override
        public void insert(PurchaseOrderItem item) {
            save(item);
        }

        @Override
        public List<PurchaseOrderItem> findByOrderId(Long orderId) {
            List<PurchaseOrderItem> result = new ArrayList<>();
            for (PurchaseOrderItem item : items) {
                if (orderId.equals(item.getOrderId())) {
                    result.add(item);
                }
            }
            return result;
        }
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

    private static class FakeStockService implements StockService {
        private int stockInCallCount;
        private StockInRequest lastStockInRequest;

        @Override
        public void stockIn(StockInRequest request) {
            stockInCallCount++;
            lastStockInRequest = request;
        }

        @Override
        public void stockOut(StockOutRequest request) {
        }

        @Override
        public Object listStockRecords() {
            return new ArrayList<>();
        }

        @Override
        public Object listStockRecordsByProduct(Long productId) {
            return new ArrayList<>();
        }
    }
}

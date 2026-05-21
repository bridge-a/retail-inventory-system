package com.example.inventory;

import com.example.inventory.dto.LoginRequest;
import com.example.inventory.dto.PurchaseApproveRequest;
import com.example.inventory.dto.StockInRequest;
import com.example.inventory.dto.StockOutRequest;
import com.example.inventory.entity.Product;
import com.example.inventory.entity.PurchaseOrder;
import com.example.inventory.entity.PurchaseOrderItem;
import com.example.inventory.entity.User;
import com.example.inventory.exception.BusinessException;
import com.example.inventory.mapper.ProductMapper;
import com.example.inventory.mapper.PurchaseOrderItemMapper;
import com.example.inventory.mapper.PurchaseOrderMapper;
import com.example.inventory.mapper.UserMapper;
import com.example.inventory.service.StockService;
import com.example.inventory.service.impl.AuthServiceImpl;
import com.example.inventory.service.impl.PurchaseOrderServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthPermissionIntegrationTest {
    public static void main(String[] args) {
        shouldLoginActiveUser();
        shouldAllowAdminToApprovePurchaseOrder();
        shouldRejectEmployeeApprovingPurchaseOrder();
        shouldRejectEmployeeRejectingPurchaseOrder();
        shouldRejectDisabledAdminApprovingPurchaseOrder();

        System.out.println("AuthPermissionIntegrationTest passed.");
    }

    private static void shouldLoginActiveUser() {
        TestContext context = createContext();
        context.userMapper.save(createUser(1L, "admin", "123456", "ADMIN", 1));

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("123456");

        User loginUser = context.authService.login(request);

        assertEquals(1L, loginUser.getId(), "login should return matched user");
        assertEquals("ADMIN", loginUser.getRole(), "login should keep user role");
    }

    private static void shouldAllowAdminToApprovePurchaseOrder() {
        TestContext context = createContext();
        context.userMapper.save(createUser(2L, "manager", "123456", "ADMIN", 1));
        context.purchaseOrderMapper.save(createOrder(10L, "PENDING"));

        PurchaseApproveRequest request = new PurchaseApproveRequest();
        request.setApproverId(2L);
        request.setApprovalRemark("admin approved");

        context.purchaseOrderService.approvePurchaseOrder(10L, request);

        PurchaseOrder updated = context.purchaseOrderMapper.findById(10L);
        assertEquals("APPROVED", updated.getStatus(), "admin should approve pending purchase order");
        assertEquals(2L, updated.getApproverId(), "approver id should be saved");
    }

    private static void shouldRejectEmployeeApprovingPurchaseOrder() {
        TestContext context = createContext();
        context.userMapper.save(createUser(3L, "staff", "123456", "EMPLOYEE", 1));
        context.purchaseOrderMapper.save(createOrder(11L, "PENDING"));

        PurchaseApproveRequest request = new PurchaseApproveRequest();
        request.setApproverId(3L);
        request.setApprovalRemark("employee try approve");

        assertThrows(
                BusinessException.class,
                () -> context.purchaseOrderService.approvePurchaseOrder(11L, request),
                "employee should not approve purchase order"
        );

        PurchaseOrder unchanged = context.purchaseOrderMapper.findById(11L);
        assertEquals("PENDING", unchanged.getStatus(), "order status should remain PENDING");
        assertEquals(null, unchanged.getApproverId(), "employee approver should not be saved");
    }

    private static void shouldRejectEmployeeRejectingPurchaseOrder() {
        TestContext context = createContext();
        context.userMapper.save(createUser(5L, "staff2", "123456", "EMPLOYEE", 1));
        context.purchaseOrderMapper.save(createOrder(13L, "PENDING"));

        PurchaseApproveRequest request = new PurchaseApproveRequest();
        request.setApproverId(5L);
        request.setApprovalRemark("employee try reject");

        assertThrows(
                BusinessException.class,
                () -> context.purchaseOrderService.rejectPurchaseOrder(13L, request),
                "employee should not reject purchase order"
        );

        PurchaseOrder unchanged = context.purchaseOrderMapper.findById(13L);
        assertEquals("PENDING", unchanged.getStatus(), "order status should remain PENDING");
        assertEquals(null, unchanged.getApproverId(), "employee reject operator should not be saved");
        assertEquals(null, unchanged.getApprovalRemark(), "employee reject remark should not be saved");
    }

    private static void shouldRejectDisabledAdminApprovingPurchaseOrder() {
        TestContext context = createContext();
        context.userMapper.save(createUser(4L, "disabledAdmin", "123456", "ADMIN", 0));
        context.purchaseOrderMapper.save(createOrder(12L, "PENDING"));

        PurchaseApproveRequest request = new PurchaseApproveRequest();
        request.setApproverId(4L);
        request.setApprovalRemark("disabled admin try approve");

        assertThrows(
                BusinessException.class,
                () -> context.purchaseOrderService.approvePurchaseOrder(12L, request),
                "disabled admin should not approve purchase order"
        );

        assertEquals("PENDING", context.purchaseOrderMapper.findById(12L).getStatus(), "order status should remain PENDING");
    }

    private static TestContext createContext() {
        FakeUserMapper userMapper = new FakeUserMapper();
        FakePurchaseOrderMapper purchaseOrderMapper = new FakePurchaseOrderMapper();
        AuthServiceImpl authService = new AuthServiceImpl(userMapper);

        PurchaseOrderServiceImpl purchaseOrderService = new PurchaseOrderServiceImpl(
                purchaseOrderMapper,
                new FakePurchaseOrderItemMapper(),
                new FakeProductMapper(),
                new FakeStockService(),
                authService
        );

        return new TestContext(userMapper, purchaseOrderMapper, authService, purchaseOrderService);
    }

    private static User createUser(Long id, String username, String password, String role, Integer status) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setRealName(username);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }

    private static PurchaseOrder createOrder(Long id, String status) {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(id);
        order.setApplicantId(100L);
        order.setReason("stock replenishment");
        order.setStatus(status);
        return order;
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

    private static class TestContext {
        private final FakeUserMapper userMapper;
        private final FakePurchaseOrderMapper purchaseOrderMapper;
        private final AuthServiceImpl authService;
        private final PurchaseOrderServiceImpl purchaseOrderService;

        private TestContext(
                FakeUserMapper userMapper,
                FakePurchaseOrderMapper purchaseOrderMapper,
                AuthServiceImpl authService,
                PurchaseOrderServiceImpl purchaseOrderService
        ) {
            this.userMapper = userMapper;
            this.purchaseOrderMapper = purchaseOrderMapper;
            this.authService = authService;
            this.purchaseOrderService = purchaseOrderService;
        }
    }

    private static class FakeUserMapper implements UserMapper {
        private final Map<Long, User> users = new HashMap<>();

        void save(User user) {
            users.put(user.getId(), user);
        }

        @Override
        public User findById(Long id) {
            return users.get(id);
        }

        @Override
        public User findByUsername(String username) {
            for (User user : users.values()) {
                if (username.equals(user.getUsername())) {
                    return user;
                }
            }
            return null;
        }

        @Override
        public void insert(User user) {
            users.put(user.getId(), user);
        }

        @Override
        public List<User> findAll() {
            return new ArrayList<>(users.values());
        }
    }

    private static class FakePurchaseOrderMapper implements PurchaseOrderMapper {
        private final Map<Long, PurchaseOrder> orders = new HashMap<>();

        void save(PurchaseOrder order) {
            orders.put(order.getId(), order);
        }

        @Override
        public void insert(PurchaseOrder order) {
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
        @Override
        public void insert(PurchaseOrderItem item) {
        }

        @Override
        public List<PurchaseOrderItem> findByOrderId(Long orderId) {
            return new ArrayList<>();
        }
    }

    private static class FakeProductMapper implements ProductMapper {
        @Override
        public Product findById(Long id) {
            return null;
        }

        @Override
        public Product findByCode(String code) {
            return null;
        }

        @Override
        public void insert(Product product) {
        }

        @Override
        public void update(Product product) {
        }

        @Override
        public List<Product> findAll() {
            return new ArrayList<>();
        }
    }

    private static class FakeStockService implements StockService {
        @Override
        public void stockIn(StockInRequest request) {
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

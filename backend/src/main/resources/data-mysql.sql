INSERT INTO user_account (id, username, password, real_name, role, status)
VALUES
    (2, 'manager', '123456', '李店长', 'ADMIN', 1),
    (3, 'staff', '123456', '王店员', 'EMPLOYEE', 1),
    (4, 'disabled_admin', '123456', '停用管理员', 'ADMIN', 0);

INSERT INTO product_category (id, name, status)
VALUES
    (1, '饮品', 1),
    (2, '日用品', 1),
    (3, '出行', 1);

INSERT INTO product (id, category_id, name, code, unit, current_stock, safe_stock, status)
VALUES
    (1, 1, '精品咖啡豆', 'P-1001', '袋', 18, 12, 1),
    (2, 1, '燕麦奶', 'P-1002', '瓶', 6, 16, 1),
    (3, 2, '抽纸三连包', 'P-2001', '提', 10, 10, 1),
    (4, 1, '矿泉水', 'P-3001', '箱', 4, 20, 1),
    (5, 3, '便携雨伞', 'P-4001', '把', 25, 8, 1);

INSERT INTO stock_record (id, product_id, change_type, quantity, before_stock, after_stock, source_type, operator_id, remark, created_at)
VALUES
    (1, 2, 'OUT', 4, 10, 6, 'MANUAL', 3, '门店销售出库', CURRENT_TIMESTAMP),
    (2, 4, 'OUT', 8, 12, 4, 'MANUAL', 3, '门店销售出库', CURRENT_TIMESTAMP),
    (3, 1, 'IN', 6, 12, 18, 'PURCHASE', 2, '采购入库', CURRENT_TIMESTAMP),
    (4, 5, 'OUT', 2, 27, 25, 'MANUAL', 3, '门店销售出库', CURRENT_TIMESTAMP);

INSERT INTO purchase_order (id, applicant_id, status, reason, approver_id, approval_remark, approved_at)
VALUES
    (1, 3, 'PENDING', '燕麦奶低于安全库存', NULL, NULL, NULL),
    (2, 3, 'APPROVED', '矿泉水低库存补货', 2, '已通过', CURRENT_TIMESTAMP),
    (3, 3, 'COMPLETED', '周末备货', 2, '已入库', CURRENT_TIMESTAMP);

INSERT INTO purchase_order_item (id, order_id, product_id, quantity, remark)
VALUES
    (1, 1, 2, 24, '燕麦奶补货'),
    (2, 2, 4, 30, '矿泉水补货'),
    (3, 3, 1, 12, '咖啡豆补货');

INSERT INTO stock_warning (id, product_id, warning_stock, safe_stock, status)
VALUES
    (1, 2, 6, 16, 'ACTIVE'),
    (2, 4, 4, 20, 'ACTIVE');

ALTER TABLE user_account AUTO_INCREMENT = 10;
ALTER TABLE product_category AUTO_INCREMENT = 10;
ALTER TABLE product AUTO_INCREMENT = 10;
ALTER TABLE purchase_order AUTO_INCREMENT = 10;
ALTER TABLE purchase_order_item AUTO_INCREMENT = 10;
ALTER TABLE stock_record AUTO_INCREMENT = 10;
ALTER TABLE stock_warning AUTO_INCREMENT = 10;

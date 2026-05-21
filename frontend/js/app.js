const seedState = () => ({
    connected: false,
    currentRole: "ADMIN",
    stockType: "IN",
    nextRecordId: 5,
    nextOrderId: 4,
    products: [
        { id: 1, code: "P-1001", name: "精品咖啡豆", category: "饮品", unit: "袋", currentStock: 18, safeStock: 12, status: 1 },
        { id: 2, code: "P-1002", name: "燕麦奶", category: "饮品", unit: "瓶", currentStock: 6, safeStock: 16, status: 1 },
        { id: 3, code: "P-2001", name: "抽纸三连包", category: "日用品", unit: "提", currentStock: 10, safeStock: 10, status: 1 },
        { id: 4, code: "P-3001", name: "矿泉水", category: "饮品", unit: "箱", currentStock: 4, safeStock: 20, status: 1 },
        { id: 5, code: "P-4001", name: "便携雨伞", category: "出行", unit: "把", currentStock: 25, safeStock: 8, status: 1 }
    ],
    records: [
        { id: 1, time: "09:12", productId: 2, type: "OUT", quantity: 4, beforeStock: 10, afterStock: 6, source: "MANUAL", operator: "李店长" },
        { id: 2, time: "09:30", productId: 4, type: "OUT", quantity: 8, beforeStock: 12, afterStock: 4, source: "MANUAL", operator: "王店员" },
        { id: 3, time: "10:04", productId: 1, type: "IN", quantity: 6, beforeStock: 12, afterStock: 18, source: "PURCHASE", operator: "李店长" },
        { id: 4, time: "10:26", productId: 5, type: "OUT", quantity: 2, beforeStock: 27, afterStock: 25, source: "MANUAL", operator: "王店员" }
    ],
    purchaseOrders: [
        { id: 1, code: "PO-20260521-001", productId: 2, quantity: 24, applicant: "王店员", applicantRole: "EMPLOYEE", reason: "燕麦奶低于安全库存", status: "PENDING", approver: "", remark: "" },
        { id: 2, code: "PO-20260521-002", productId: 4, quantity: 30, applicant: "王店员", applicantRole: "EMPLOYEE", reason: "矿泉水低库存补货", status: "APPROVED", approver: "李店长", remark: "已通过" },
        { id: 3, code: "PO-20260520-006", productId: 1, quantity: 12, applicant: "王店员", applicantRole: "EMPLOYEE", reason: "周末备货", status: "COMPLETED", approver: "李店长", remark: "已入库" }
    ]
});

const users = {
    ADMIN: {
        id: 2,
        name: "李店长",
        role: "ADMIN",
        title: "管理员工作台",
        summary: "管理员视角：处理采购审批、采购入库和库存异常。",
        description: "可处理待审批采购单、执行采购入库，并查看全部库存流水和低库存预警。",
        focus: ["审批 PENDING 采购单", "执行采购入库", "查看全部预警"]
    },
    EMPLOYEE: {
        id: 3,
        name: "王店员",
        role: "EMPLOYEE",
        title: "普通员工工作台",
        summary: "普通员工视角：提交采购申请、查看申请状态和执行日常库存登记。",
        description: "不可审批采购单，只能提交申请并跟踪自己的采购单状态。",
        focus: ["提交采购申请", "查看我的申请", "不可审批采购单"]
    }
};

let state = seedState();
let toastTimer = null;
const API_BASE = "http://localhost:8080/api";

const $ = (selector) => document.querySelector(selector);
const $$ = (selector) => Array.from(document.querySelectorAll(selector));

const statusMap = {
    PENDING: { label: "待审批", className: "tag-blue" },
    APPROVED: { label: "已通过", className: "tag-green" },
    REJECTED: { label: "已驳回", className: "tag-red" },
    COMPLETED: { label: "已完成", className: "tag-purple" }
};

function getProduct(productId) {
    return state.products.find((product) => product.id === Number(productId));
}

function currentUser() {
    return users[state.currentRole];
}

async function apiFetch(path, options = {}) {
    const response = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        }
    });
    if (!response.ok) {
        let message = "后端接口请求失败";
        try {
            const errorBody = await response.json();
            message = errorBody.message || message;
        } catch (error) {
            message = response.statusText || message;
        }
        throw new Error(message);
    }
    if (response.status === 204) {
        return null;
    }
    const text = await response.text();
    return text ? JSON.parse(text) : null;
}

async function loadDashboard(options = {}) {
    try {
        await apiFetch("/health");
        const dashboard = await apiFetch("/dashboard");
        state.products = dashboard.products || state.products;
        state.records = dashboard.records || state.records;
        state.purchaseOrders = dashboard.purchaseOrders || state.purchaseOrders;
        state.connected = true;
        renderAll();
        if (!options.silent) {
            showToast("后端健康检查通过，已连接数据库", "success");
        }
    } catch (error) {
        state.connected = false;
        renderAll();
        if (!options.silent) {
            showToast("后端未启动，当前使用前端演示数据", "error");
        }
    }
}

function formatTime() {
    const now = new Date();
    return `${String(now.getHours()).padStart(2, "0")}:${String(now.getMinutes()).padStart(2, "0")}`;
}

function showToast(message, type = "success") {
    const toast = $("#toast");
    toast.textContent = message;
    toast.className = `toast is-visible is-${type}`;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => {
        toast.className = "toast";
        toast.textContent = "";
    }, 2800);
}

function lowStockProducts() {
    return state.products.filter((product) => product.status === 1 && product.currentStock < product.safeStock);
}

function renderMetrics() {
    $("#metricProducts").textContent = state.products.filter((product) => product.status === 1).length;
    $("#metricWarnings").textContent = lowStockProducts().length;
    $("#metricPending").textContent = state.purchaseOrders.filter((order) => order.status === "PENDING").length;
    $("#metricRecords").textContent = state.records.length;
}

function renderRoleContext() {
    const user = currentUser();
    document.body.dataset.role = user.role;
    const sourceText = state.connected ? "后端健康检查通过，已连接数据库" : "后端未启动，当前使用前端演示数据";
    $("#roleSummary").textContent = `${user.summary} · ${sourceText}`;
    $("#roleBadge").textContent = user.role;
    $("#roleBannerTitle").textContent = user.title;
    $("#roleBannerText").textContent = user.description;
    $("#roleFocusList").innerHTML = user.focus
        .map((item) => `<span class="role-focus">${item}</span>`)
        .join("");
}

function renderSelects() {
    const options = state.products
        .filter((product) => product.status === 1)
        .map((product) => `<option value="${product.id}">${product.code} ${product.name}</option>`)
        .join("");

    $("#stockProduct").innerHTML = options;
    $("#purchaseProduct").innerHTML = options;
}

function renderProducts() {
    const keyword = $("#productSearch").value.trim().toLowerCase();
    const rows = state.products
        .filter((product) => {
            const text = `${product.code} ${product.name} ${product.category}`.toLowerCase();
            return text.includes(keyword);
        })
        .map((product) => {
            const low = product.currentStock < product.safeStock;
            const width = Math.max(6, Math.min(100, Math.round((product.currentStock / Math.max(product.safeStock, 1)) * 100)));
            return `
                <tr>
                    <td>${product.code}</td>
                    <td>
                        <div class="product-name">
                            <strong>${product.name}</strong>
                            <span>${product.unit}</span>
                        </div>
                    </td>
                    <td>${product.category}</td>
                    <td class="stock-cell">
                        <strong>${product.currentStock}</strong>
                        <div class="stock-line ${low ? "is-low" : ""}"><span style="width:${width}%"></span></div>
                    </td>
                    <td>${product.safeStock}</td>
                    <td>${low ? `<span class="tag tag-amber">低库存</span>` : `<span class="tag tag-green">正常</span>`}</td>
                    <td>
                        <div class="row-actions">
                            <button class="small-button" type="button" data-quick-stock="IN" data-product-id="${product.id}">入库</button>
                            <button class="small-button" type="button" data-quick-stock="OUT" data-product-id="${product.id}">出库</button>
                        </div>
                    </td>
                </tr>
            `;
        })
        .join("");

    $("#productTable").innerHTML = rows || `<tr><td colspan="7">没有匹配的商品</td></tr>`;
}

function renderRecords() {
    const rows = [...state.records]
        .reverse()
        .slice(0, 10)
        .map((record) => {
            const product = getProduct(record.productId);
            const typeClass = record.type === "IN" ? "tag-green" : "tag-amber";
            const typeLabel = record.type === "IN" ? "入库" : "出库";
            return `
                <tr>
                    <td>${record.time}</td>
                    <td>${product ? product.name : "未知商品"}</td>
                    <td><span class="tag ${typeClass}">${typeLabel}</span></td>
                    <td>${record.quantity}</td>
                    <td>${record.beforeStock}</td>
                    <td>${record.afterStock}</td>
                    <td>${record.source}</td>
                    <td>${record.operator}</td>
                </tr>
            `;
        })
        .join("");

    $("#recordTable").innerHTML = rows;
}

function renderWarnings() {
    const warnings = lowStockProducts();
    if (warnings.length === 0) {
        $("#warningList").innerHTML = `
            <div class="warning-item is-clear">
                <div class="warning-title">
                    <span>暂无低库存商品</span>
                    <span class="tag tag-green">RESOLVED</span>
                </div>
                <div class="warning-meta">所有商品当前库存均不低于安全库存</div>
            </div>
        `;
        return;
    }

    $("#warningList").innerHTML = warnings
        .map((product) => `
            <div class="warning-item">
                <div class="warning-title">
                    <span>${product.name}</span>
                    <span class="tag tag-amber">ACTIVE</span>
                </div>
                <div class="warning-meta">当前库存 ${product.currentStock}，安全库存 ${product.safeStock}</div>
            </div>
        `)
        .join("");
}

function renderPurchases() {
    const user = currentUser();
    const isAdmin = user.role === "ADMIN";
    $("#purchasePanelTitle").textContent = isAdmin ? "采购审批处理" : "我的采购申请";
    $("#purchasePanelDesc").textContent = isAdmin
        ? "管理员处理 PENDING 采购单，并执行 APPROVED 采购入库"
        : "普通员工只查看自己提交的采购申请和审批进度";
    $("#roleHint").textContent = isAdmin ? "管理员可审批采购单" : "普通员工不可审批采购单";
    $("#roleHint").className = `role-hint ${isAdmin ? "" : "tag-red"}`;

    const visibleOrders = isAdmin
        ? state.purchaseOrders
        : state.purchaseOrders.filter((order) => order.applicant === user.name);

    const list = visibleOrders.map((order) => {
        const product = getProduct(order.productId);
        const status = statusMap[order.status];
        const canReview = isAdmin && order.status === "PENDING";
        const canComplete = isAdmin && order.status === "APPROVED";
        const actionArea = isAdmin
            ? `
                <button class="ghost-button" type="button" data-approve-id="${order.id}" ${canReview ? "" : "disabled"}>审批通过</button>
                <button class="danger-button" type="button" data-reject-id="${order.id}" ${canReview ? "" : "disabled"}>驳回</button>
                <button class="small-button" type="button" data-complete-id="${order.id}" ${canComplete ? "" : "disabled"}>采购入库</button>
            `
            : `<span class="employee-note">${employeeOrderNote(order.status)}</span>`;
        return `
            <article class="purchase-item">
                <div class="purchase-main">
                    <div class="purchase-title">
                        <strong>${order.code}</strong>
                        <span>${product ? product.name : "未知商品"} · ${order.quantity}${product ? product.unit : ""} · ${order.reason}</span>
                    </div>
                    <span class="tag ${status.className}">${status.label}</span>
                </div>
                <div class="purchase-main">
                    <div class="purchase-title">
                        <span>申请人：${order.applicant} · 审批人：${order.approver || "未审批"}</span>
                        <span>${order.remark || "等待处理"}</span>
                    </div>
                    <div class="purchase-actions">
                        ${actionArea}
                    </div>
                </div>
            </article>
        `;
    }).join("");

    $("#purchaseList").innerHTML = list || `<div class="empty-state">${isAdmin ? "暂无采购单" : "暂无我的采购申请"}</div>`;
}

function employeeOrderNote(status) {
    if (status === "PENDING") {
        return "等待管理员审批";
    }
    if (status === "APPROVED") {
        return "已通过，等待采购入库";
    }
    if (status === "REJECTED") {
        return "已驳回";
    }
    return "已完成入库";
}

function renderRoleButtons() {
    $$(".role-button").forEach((button) => {
        button.classList.toggle("is-active", button.dataset.role === state.currentRole);
    });
}

function renderStockSegments() {
    $$(".segment").forEach((button) => {
        button.classList.toggle("is-active", button.dataset.stockType === state.stockType);
    });
}

function renderAll() {
    renderRoleContext();
    renderMetrics();
    renderSelects();
    renderProducts();
    renderRecords();
    renderWarnings();
    renderPurchases();
    renderRoleButtons();
    renderStockSegments();
}

function applyStockChange(productId, type, quantity, remark, source = "MANUAL") {
    const product = getProduct(productId);
    if (!product) {
        showToast("商品不存在", "error");
        return false;
    }
    if (!Number.isInteger(quantity) || quantity <= 0) {
        showToast("数量必须大于 0", "error");
        return false;
    }
    if (type === "OUT" && quantity > product.currentStock) {
        showToast("出库失败：库存不能为负", "error");
        return false;
    }

    const beforeStock = product.currentStock;
    const afterStock = type === "IN" ? beforeStock + quantity : beforeStock - quantity;
    product.currentStock = afterStock;
    state.records.push({
        id: state.nextRecordId++,
        time: formatTime(),
        productId: product.id,
        type,
        quantity,
        beforeStock,
        afterStock,
        source,
        operator: currentUser().name,
        remark
    });
    return true;
}

async function submitStockForm(event) {
    event.preventDefault();
    const productId = Number($("#stockProduct").value);
    const quantity = Number($("#stockQuantity").value);
    const remark = $("#stockRemark").value.trim() || "库存调整";

    if (state.connected) {
        try {
            await apiFetch(`/stock/${state.stockType === "IN" ? "in" : "out"}`, {
                method: "POST",
                body: JSON.stringify({
                    productId,
                    quantity,
                    operatorId: currentUser().id,
                    remark
                })
            });
            await loadDashboard({ silent: true });
            showToast("库存变更已写入后端数据库", "success");
        } catch (error) {
            showToast(error.message, "error");
        }
        return;
    }

    const ok = applyStockChange(productId, state.stockType, quantity, remark);
    if (ok) {
        showToast("库存变更已记录", "success");
        renderAll();
    }
}

async function submitPurchaseForm(event) {
    event.preventDefault();
    const productId = Number($("#purchaseProduct").value);
    const quantity = Number($("#purchaseQuantity").value);
    const reason = $("#purchaseReason").value.trim() || "门店补货";
    if (!Number.isInteger(quantity) || quantity <= 0) {
        showToast("采购数量必须大于 0", "error");
        return;
    }

    if (state.connected) {
        try {
            await apiFetch("/purchase-orders", {
                method: "POST",
                body: JSON.stringify({
                    applicantId: currentUser().id,
                    reason,
                    items: [
                        {
                            productId,
                            quantity,
                            remark: reason
                        }
                    ]
                })
            });
            await loadDashboard({ silent: true });
            showToast("采购申请已写入后端数据库", "success");
        } catch (error) {
            showToast(error.message, "error");
        }
        return;
    }

    const orderNumber = String(state.nextOrderId).padStart(3, "0");
    state.purchaseOrders.unshift({
        id: state.nextOrderId,
        code: `PO-20260521-${orderNumber}`,
        productId,
        quantity,
        applicant: currentUser().name,
        applicantRole: currentUser().role,
        reason,
        status: "PENDING",
        approver: "",
        remark: ""
    });
    state.nextOrderId += 1;
    showToast("采购申请已提交", "success");
    renderAll();
}

async function approvePurchase(orderId) {
    const order = state.purchaseOrders.find((item) => item.id === Number(orderId));
    if (!order) {
        return;
    }
    if (currentUser().role !== "ADMIN") {
        showToast("普通员工不能审批采购单", "error");
        return;
    }
    if (order.status !== "PENDING") {
        showToast("只有 PENDING 采购单可以审批", "error");
        return;
    }
    if (state.connected) {
        try {
            await apiFetch(`/purchase-orders/${orderId}/approve`, {
                method: "POST",
                body: JSON.stringify({
                    approverId: currentUser().id,
                    approvalRemark: "审批通过，等待采购入库"
                })
            });
            await loadDashboard({ silent: true });
            showToast("采购单已在后端审批通过", "success");
        } catch (error) {
            showToast(error.message, "error");
        }
        return;
    }
    order.status = "APPROVED";
    order.approver = currentUser().name;
    order.remark = "审批通过，等待采购入库";
    showToast("采购单已审批通过", "success");
    renderAll();
}

async function rejectPurchase(orderId) {
    const order = state.purchaseOrders.find((item) => item.id === Number(orderId));
    if (!order) {
        return;
    }
    if (currentUser().role !== "ADMIN") {
        showToast("普通员工不能驳回采购单", "error");
        return;
    }
    if (order.status !== "PENDING") {
        showToast("只有 PENDING 采购单可以驳回", "error");
        return;
    }
    if (state.connected) {
        try {
            await apiFetch(`/purchase-orders/${orderId}/reject`, {
                method: "POST",
                body: JSON.stringify({
                    approverId: currentUser().id,
                    approvalRemark: "审批驳回"
                })
            });
            await loadDashboard({ silent: true });
            showToast("采购单已在后端驳回", "success");
        } catch (error) {
            showToast(error.message, "error");
        }
        return;
    }
    order.status = "REJECTED";
    order.approver = currentUser().name;
    order.remark = "审批驳回";
    showToast("采购单已驳回", "success");
    renderAll();
}

async function completePurchase(orderId) {
    const order = state.purchaseOrders.find((item) => item.id === Number(orderId));
    if (!order) {
        return;
    }
    if (order.status !== "APPROVED") {
        showToast("只有 APPROVED 采购单可以入库", "error");
        return;
    }
    if (state.connected) {
        try {
            await apiFetch(`/purchase-orders/${orderId}/complete`, {
                method: "POST",
                body: JSON.stringify({
                    operatorId: currentUser().id,
                    remark: `采购入库 ${order.code}`
                })
            });
            await loadDashboard({ silent: true });
            showToast("采购入库已写入后端数据库", "success");
        } catch (error) {
            showToast(error.message, "error");
        }
        return;
    }
    const ok = applyStockChange(order.productId, "IN", order.quantity, `采购入库 ${order.code}`, "PURCHASE");
    if (!ok) {
        return;
    }
    order.status = "COMPLETED";
    order.remark = "采购入库完成";
    showToast("采购入库完成，库存流水已生成", "success");
    renderAll();
}

function bindEvents() {
    $$(".role-button").forEach((button) => {
        button.addEventListener("click", () => {
            state.currentRole = button.dataset.role;
            showToast(`当前角色：${currentUser().name}`, "success");
            renderAll();
        });
    });

    $$(".segment").forEach((button) => {
        button.addEventListener("click", () => {
            state.stockType = button.dataset.stockType;
            renderStockSegments();
        });
    });

    $("#stockForm").addEventListener("submit", submitStockForm);
    $("#purchaseForm").addEventListener("submit", submitPurchaseForm);
    $("#productSearch").addEventListener("input", renderProducts);
    $("#resetData").addEventListener("click", () => {
        state = seedState();
        showToast("数据已重置", "success");
        renderAll();
    });

    document.addEventListener("click", (event) => {
        const quickStockButton = event.target.closest("[data-quick-stock]");
        if (quickStockButton) {
            state.stockType = quickStockButton.dataset.quickStock;
            $("#stockProduct").value = quickStockButton.dataset.productId;
            $("#stockQuantity").focus();
            renderStockSegments();
            return;
        }

        const approveButton = event.target.closest("[data-approve-id]");
        if (approveButton) {
            approvePurchase(approveButton.dataset.approveId);
            return;
        }

        const rejectButton = event.target.closest("[data-reject-id]");
        if (rejectButton) {
            rejectPurchase(rejectButton.dataset.rejectId);
            return;
        }

        const completeButton = event.target.closest("[data-complete-id]");
        if (completeButton) {
            completePurchase(completeButton.dataset.completeId);
        }
    });

    $$(".nav-item").forEach((item) => {
        item.addEventListener("click", () => {
            $$(".nav-item").forEach((nav) => nav.classList.remove("is-active"));
            item.classList.add("is-active");
        });
    });
}

bindEvents();
renderAll();
loadDashboard();

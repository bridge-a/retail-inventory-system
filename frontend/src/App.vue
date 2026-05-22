<template>
  <main v-if="!currentUser" class="login-page">
    <section class="login-panel">
      <div>
        <p class="eyebrow">Retail Inventory Console</p>
        <h1>小型零售门店库存采购系统</h1>
        <p class="muted">Vue + Vite 前端，连接 Spring Boot REST API。后端未启动时会使用演示数据。</p>
      </div>

      <div class="preset-grid">
        <button
          v-for="account in demoAccounts"
          :key="account.username"
          type="button"
          class="preset-card"
          :class="{ active: loginForm.username === account.username }"
          @click="selectAccount(account.username)"
        >
          <span>{{ account.role === 'ADMIN' ? '管理员' : '普通员工' }}</span>
          <strong>{{ account.username }}</strong>
          <small>{{ account.role === 'ADMIN' ? '可进入采购审批模块' : '不能进入采购审批模块' }}</small>
        </button>
      </div>

      <form class="login-form" @submit.prevent="login">
        <label>
          用户名
          <input v-model.trim="loginForm.username" autocomplete="username" placeholder="manager 或 staff" />
        </label>
        <label>
          密码
          <input v-model="loginForm.password" type="password" autocomplete="current-password" placeholder="123456" />
        </label>
        <button class="primary-button" type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
    </section>
  </main>

  <div v-else class="app-shell">
    <aside class="sidebar">
      <div class="brand-block">
        <div class="brand-mark">库</div>
        <div>
          <strong>库存采购系统</strong>
          <span>前后端分离演示</span>
        </div>
      </div>

      <nav class="nav-list" aria-label="模块导航">
        <button
          v-for="tab in visibleTabs"
          :key="tab.key"
          type="button"
          class="nav-item"
          :class="{ active: activeTab === tab.key }"
          @click="selectTab(tab.key)"
        >
          <span>{{ tab.icon }}</span>
          {{ tab.label }}
        </button>
      </nav>

      <div class="role-card">
        <span>当前角色</span>
        <strong>{{ roleLabel }}</strong>
        <small>{{ isManager ? '可审批采购申请' : '采购审批入口已隐藏' }}</small>
      </div>
    </aside>

    <section class="content">
      <header class="topbar">
        <div>
          <p class="eyebrow">http://localhost:5173</p>
          <h2>{{ activeTabTitle }}</h2>
          <p class="muted">
            {{ displayName }}，{{ roleLabel }}视角。
            {{ isManager ? '可查看并处理采购审批。' : '普通员工不能进入采购审批模块。' }}
          </p>
        </div>
        <div class="topbar-actions">
          <span class="connection-pill" :class="{ online: apiConnected }">
            {{ apiConnected ? '后端已连接' : '后端未连接' }}
          </span>
          <button class="secondary-button" type="button" @click="refreshDashboard">刷新数据</button>
          <button class="secondary-button" type="button" @click="logout">退出</button>
        </div>
      </header>

      <div v-if="!apiConnected" class="notice">
        {{ connectionMessage }} 当前页面保留演示数据，避免后端未启动时页面空白。
      </div>
      <div v-if="toast" class="toast" :class="toast.type">{{ toast.message }}</div>

      <section v-if="activeTab === 'overview'" class="view">
        <div class="metric-grid">
          <article class="metric-card">
            <span>商品库存</span>
            <strong>{{ products.length }}</strong>
            <small>商品档案与当前库存</small>
          </article>
          <article class="metric-card warning">
            <span>低库存预警</span>
            <strong>{{ activeWarnings.length }}</strong>
            <small>低于安全库存</small>
          </article>
          <article class="metric-card pending">
            <span>待审批采购</span>
            <strong>{{ pendingOrders.length }}</strong>
            <small>PENDING 状态</small>
          </article>
          <article class="metric-card flow">
            <span>库存流水</span>
            <strong>{{ records.length }}</strong>
            <small>出入库可追溯</small>
          </article>
        </div>

        <section class="panel">
          <h3>{{ roleLabel }}工作台</h3>
          <p>
            管理员可以处理采购审批和采购入库；普通员工可以查看库存、登记出入库、提交采购申请，但不会显示采购审批入口。
          </p>
        </section>
      </section>

      <section v-if="activeTab === 'products'" class="view">
        <section class="panel">
          <div class="panel-head">
            <div>
              <h3>商品库存</h3>
              <p>展示商品编码、分类、当前库存和安全库存。</p>
            </div>
            <input v-model.trim="productKeyword" class="search-input" placeholder="搜索商品名称或编码" />
          </div>
          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>编码</th>
                  <th>商品</th>
                  <th>分类</th>
                  <th>当前库存</th>
                  <th>安全库存</th>
                  <th>状态</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="product in filteredProducts" :key="product.id">
                  <td>{{ product.code }}</td>
                  <td>{{ product.name }}</td>
                  <td>{{ product.category || product.categoryId }}</td>
                  <td>{{ product.currentStock }} {{ product.unit }}</td>
                  <td>{{ product.safeStock }} {{ product.unit }}</td>
                  <td>
                    <span class="tag" :class="isLowStock(product) ? 'tag-warning' : 'tag-ok'">
                      {{ isLowStock(product) ? '低库存' : '正常' }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </section>

      <section v-if="activeTab === 'records'" class="view grid-view">
        <section class="panel">
          <h3>出入库登记</h3>
          <p>提交后会调用后端库存接口；演示模式下会更新本地数据。</p>
          <form class="stack-form" @submit.prevent="submitStockChange">
            <label>
              商品
              <select v-model.number="stockForm.productId">
                <option v-for="product in products" :key="product.id" :value="product.id">
                  {{ product.code }} {{ product.name }}
                </option>
              </select>
            </label>
            <div class="segmented">
              <button type="button" :class="{ active: stockForm.type === 'IN' }" @click="stockForm.type = 'IN'">入库</button>
              <button type="button" :class="{ active: stockForm.type === 'OUT' }" @click="stockForm.type = 'OUT'">出库</button>
            </div>
            <label>
              数量
              <input v-model.number="stockForm.quantity" min="1" type="number" />
            </label>
            <label>
              备注
              <input v-model.trim="stockForm.remark" />
            </label>
            <button class="primary-button" type="submit">提交库存变更</button>
          </form>
        </section>

        <section class="panel">
          <h3>库存流水</h3>
          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>时间</th>
                  <th>商品</th>
                  <th>类型</th>
                  <th>数量</th>
                  <th>变更前</th>
                  <th>变更后</th>
                  <th>来源</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="record in records" :key="record.id">
                  <td>{{ record.time || '-' }}</td>
                  <td>{{ productName(record.productId) }}</td>
                  <td>{{ record.type === 'IN' ? '入库' : '出库' }}</td>
                  <td>{{ record.quantity }}</td>
                  <td>{{ record.beforeStock }}</td>
                  <td>{{ record.afterStock }}</td>
                  <td>{{ record.source || 'MANUAL' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </section>

      <section v-if="activeTab === 'purchase'" class="view grid-view">
        <section class="panel">
          <h3>采购申请</h3>
          <p>新建采购单时状态由后端固定为 PENDING。</p>
          <form class="stack-form" @submit.prevent="submitPurchaseOrder">
            <label>
              商品
              <select v-model.number="purchaseForm.productId">
                <option v-for="product in products" :key="product.id" :value="product.id">
                  {{ product.code }} {{ product.name }}
                </option>
              </select>
            </label>
            <label>
              数量
              <input v-model.number="purchaseForm.quantity" min="1" type="number" />
            </label>
            <label>
              申请原因
              <input v-model.trim="purchaseForm.reason" />
            </label>
            <button class="primary-button" type="submit">提交采购申请</button>
          </form>
        </section>

        <section class="panel">
          <h3>{{ isManager ? '采购申请记录' : '我的采购申请' }}</h3>
          <div class="card-list">
            <article v-for="order in visiblePurchaseOrders" :key="order.id" class="order-card">
              <div>
                <strong>{{ order.code || `PO-${order.id}` }}</strong>
                <p>{{ productName(order.productId) }}，数量 {{ order.quantity }}，{{ order.reason }}</p>
              </div>
              <span class="tag" :class="statusClass(order.status)">{{ statusLabel(order.status) }}</span>
            </article>
          </div>
        </section>
      </section>

      <section v-if="activeTab === 'approval'" class="view">
        <section class="panel">
          <div class="panel-head">
            <div>
              <h3>采购审批</h3>
              <p>仅管理员可见。PENDING 可审批或驳回，APPROVED 可执行采购入库。</p>
            </div>
          </div>
          <div class="card-list">
            <article v-for="order in purchaseOrders" :key="order.id" class="order-card">
              <div>
                <strong>{{ order.code || `PO-${order.id}` }}</strong>
                <p>{{ productName(order.productId) }}，数量 {{ order.quantity }}，申请人：{{ order.applicant || '-' }}</p>
                <small>{{ order.reason }}</small>
              </div>
              <div class="row-actions">
                <span class="tag" :class="statusClass(order.status)">{{ statusLabel(order.status) }}</span>
                <button class="secondary-button" type="button" :disabled="order.status !== 'PENDING'" @click="approveOrder(order)">
                  通过
                </button>
                <button class="danger-button" type="button" :disabled="order.status !== 'PENDING'" @click="rejectOrder(order)">
                  驳回
                </button>
                <button class="primary-mini" type="button" :disabled="order.status !== 'APPROVED'" @click="completeOrder(order)">
                  入库
                </button>
              </div>
            </article>
          </div>
        </section>
      </section>

      <section v-if="activeTab === 'warnings'" class="view">
        <section class="panel">
          <h3>低库存预警</h3>
          <p>当前库存低于安全库存时显示 ACTIVE 预警。</p>
          <div class="card-list">
            <article v-for="warning in activeWarnings" :key="warning.productId" class="warning-card">
              <div>
                <strong>{{ productName(warning.productId) }}</strong>
                <p>当前库存 {{ warning.warningStock }}，安全库存 {{ warning.safeStock }}</p>
              </div>
              <span class="tag tag-warning">{{ warning.status || 'ACTIVE' }}</span>
            </article>
            <article v-if="activeWarnings.length === 0" class="empty-card">暂无低库存预警。</article>
          </div>
        </section>
      </section>
    </section>
  </div>
</template>

<script>
const API_BASE = "http://localhost:8080/api";

const DEMO_USERS = [
  { id: 2, username: "manager", password: "123456", realName: "门店管理员", role: "ADMIN" },
  { id: 3, username: "staff", password: "123456", realName: "普通员工", role: "EMPLOYEE" }
];

const demoData = () => ({
  products: [
    { id: 1, code: "P-1001", name: "精品咖啡豆", category: "饮品", unit: "袋", currentStock: 18, safeStock: 12, status: 1 },
    { id: 2, code: "P-1002", name: "燕麦牛奶", category: "饮品", unit: "瓶", currentStock: 6, safeStock: 16, status: 1 },
    { id: 3, code: "P-2001", name: "抽纸三连包", category: "日用品", unit: "提", currentStock: 10, safeStock: 10, status: 1 },
    { id: 4, code: "P-3001", name: "矿泉水", category: "饮品", unit: "箱", currentStock: 4, safeStock: 20, status: 1 }
  ],
  records: [
    { id: 1, time: "09:12", productId: 2, type: "OUT", quantity: 4, beforeStock: 10, afterStock: 6, source: "MANUAL" },
    { id: 2, time: "09:40", productId: 4, type: "OUT", quantity: 8, beforeStock: 12, afterStock: 4, source: "MANUAL" },
    { id: 3, time: "10:05", productId: 1, type: "IN", quantity: 6, beforeStock: 12, afterStock: 18, source: "PURCHASE" }
  ],
  purchaseOrders: [
    { id: 1, code: "PO-20260521-001", productId: 2, quantity: 24, applicant: "普通员工", reason: "低于安全库存", status: "PENDING", approver: "", remark: "" },
    { id: 2, code: "PO-20260521-002", productId: 4, quantity: 30, applicant: "普通员工", reason: "补充周末备货", status: "APPROVED", approver: "门店管理员", remark: "同意采购" },
    { id: 3, code: "PO-20260520-006", productId: 1, quantity: 12, applicant: "普通员工", reason: "促销备货", status: "COMPLETED", approver: "门店管理员", remark: "已入库" }
  ],
  warnings: []
});

export default {
  name: "App",
  data() {
    const seed = demoData();
    return {
      loading: false,
      apiConnected: false,
      connectionMessage: "后端未连接",
      currentUser: null,
      loginForm: {
        username: "manager",
        password: "123456"
      },
      activeTab: "overview",
      productKeyword: "",
      toast: null,
      products: seed.products,
      records: seed.records,
      purchaseOrders: seed.purchaseOrders,
      warnings: seed.warnings,
      nextRecordId: 4,
      nextOrderId: 4,
      stockForm: {
        productId: 1,
        type: "IN",
        quantity: 1,
        remark: "日常库存调整"
      },
      purchaseForm: {
        productId: 2,
        quantity: 10,
        reason: "库存低于安全库存"
      },
      tabs: [
        { key: "overview", label: "首页概览", icon: "总" },
        { key: "products", label: "商品库存", icon: "品" },
        { key: "records", label: "库存流水", icon: "流" },
        { key: "purchase", label: "采购申请", icon: "采" },
        { key: "approval", label: "采购审批", icon: "审", adminOnly: true },
        { key: "warnings", label: "低库存预警", icon: "警" }
      ]
    };
  },
  computed: {
    demoAccounts() {
      return DEMO_USERS;
    },
    isManager() {
      return this.currentUser?.role === "ADMIN";
    },
    roleLabel() {
      return this.isManager ? "管理员" : "普通员工";
    },
    displayName() {
      return this.currentUser?.realName || this.currentUser?.name || this.currentUser?.username || "";
    },
    visibleTabs() {
      return this.tabs.filter((tab) => !tab.adminOnly || this.isManager);
    },
    activeTabTitle() {
      return this.tabs.find((tab) => tab.key === this.activeTab)?.label || "首页概览";
    },
    filteredProducts() {
      const keyword = this.productKeyword.toLowerCase();
      if (!keyword) {
        return this.products;
      }
      return this.products.filter((product) => {
        return `${product.code} ${product.name} ${product.category}`.toLowerCase().includes(keyword);
      });
    },
    lowStockProducts() {
      return this.products.filter((product) => this.isLowStock(product));
    },
    activeWarnings() {
      const active = this.warnings.filter((warning) => warning.status === "ACTIVE");
      if (active.length > 0) {
        return active;
      }
      return this.lowStockProducts.map((product) => ({
        productId: product.id,
        warningStock: product.currentStock,
        safeStock: product.safeStock,
        status: "ACTIVE"
      }));
    },
    pendingOrders() {
      return this.purchaseOrders.filter((order) => order.status === "PENDING");
    },
    visiblePurchaseOrders() {
      if (this.isManager) {
        return this.purchaseOrders;
      }
      return this.purchaseOrders.filter((order) => order.applicant === this.displayName || order.applicantRole === "EMPLOYEE");
    }
  },
  methods: {
    selectAccount(username) {
      const account = DEMO_USERS.find((item) => item.username === username);
      if (account) {
        this.loginForm.username = account.username;
        this.loginForm.password = account.password;
      }
    },
    async login() {
      this.loading = true;
      try {
        const user = await this.apiFetch("/users/login", {
          method: "POST",
          body: JSON.stringify(this.loginForm)
        });
        this.currentUser = this.normalizeUser(user);
        this.apiConnected = true;
        this.connectionMessage = "后端已连接";
        await this.loadDashboard();
        this.showToast(`${this.displayName} 登录成功，已连接后端。`, "success");
      } catch (error) {
        const fallback = DEMO_USERS.find((user) => {
          return user.username === this.loginForm.username && user.password === this.loginForm.password;
        });
        if (!fallback) {
          this.showToast("登录失败，请使用 manager / 123456 或 staff / 123456。", "error");
          this.loading = false;
          return;
        }
        this.currentUser = this.normalizeUser(fallback);
        this.apiConnected = false;
        this.connectionMessage = "后端未连接，已进入前端演示模式。";
        this.showToast(`${this.displayName} 已进入演示模式。`, "warning");
      } finally {
        this.loading = false;
        if (!this.visibleTabs.some((tab) => tab.key === this.activeTab)) {
          this.activeTab = "overview";
        }
      }
    },
    logout() {
      this.currentUser = null;
      this.activeTab = "overview";
    },
    selectTab(tabKey) {
      const allowed = this.visibleTabs.some((tab) => tab.key === tabKey);
      this.activeTab = allowed ? tabKey : "overview";
    },
    normalizeUser(user) {
      return {
        id: user.id,
        username: user.username,
        realName: user.realName || user.name || user.username,
        role: user.role
      };
    },
    async apiFetch(path, options = {}) {
      const response = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers: {
          "Content-Type": "application/json",
          ...(options.headers || {})
        }
      });
      if (!response.ok) {
        let message = "接口请求失败";
        try {
          const body = await response.json();
          message = body.message || message;
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
    },
    async loadDashboard() {
      const dashboard = await this.apiFetch("/dashboard");
      this.products = Array.isArray(dashboard.products) ? dashboard.products : this.products;
      this.records = Array.isArray(dashboard.records) ? dashboard.records : this.records;
      this.purchaseOrders = Array.isArray(dashboard.purchaseOrders) ? dashboard.purchaseOrders : this.purchaseOrders;
      this.warnings = Array.isArray(dashboard.warnings) ? dashboard.warnings : this.warnings;
      this.ensureSelectedProducts();
    },
    async refreshDashboard() {
      try {
        await this.apiFetch("/health");
        await this.loadDashboard();
        this.apiConnected = true;
        this.connectionMessage = "后端已连接";
        this.showToast("已刷新后端数据。", "success");
      } catch (error) {
        this.apiConnected = false;
        this.connectionMessage = "后端未连接，当前使用前端演示数据。";
        this.showToast("后端未连接，继续显示演示数据。", "warning");
      }
    },
    ensureSelectedProducts() {
      if (this.products.length === 0) {
        return;
      }
      if (!this.products.some((product) => product.id === this.stockForm.productId)) {
        this.stockForm.productId = this.products[0].id;
      }
      if (!this.products.some((product) => product.id === this.purchaseForm.productId)) {
        this.purchaseForm.productId = this.products[0].id;
      }
    },
    async submitStockChange() {
      const path = this.stockForm.type === "IN" ? "/stock/in" : "/stock/out";
      const payload = {
        productId: this.stockForm.productId,
        quantity: Number(this.stockForm.quantity),
        operatorId: this.currentUser.id,
        remark: this.stockForm.remark
      };

      if (this.apiConnected) {
        try {
          await this.apiFetch(path, { method: "POST", body: JSON.stringify(payload) });
          await this.loadDashboard();
          this.showToast("库存变更已写入后端。", "success");
          return;
        } catch (error) {
          this.apiConnected = false;
          this.connectionMessage = "后端连接中断，当前切换为演示模式。";
          this.showToast(error.message, "error");
        }
      }

      this.applyLocalStockChange(payload.productId, this.stockForm.type, payload.quantity, payload.remark, "MANUAL");
    },
    applyLocalStockChange(productId, type, quantity, remark, source) {
      const product = this.products.find((item) => item.id === Number(productId));
      if (!product) {
        this.showToast("商品不存在。", "error");
        return;
      }
      if (!Number.isInteger(quantity) || quantity <= 0) {
        this.showToast("数量必须大于 0。", "error");
        return;
      }
      if (type === "OUT" && quantity > product.currentStock) {
        this.showToast("库存不足，不能出库。", "error");
        return;
      }

      const beforeStock = product.currentStock;
      const afterStock = type === "IN" ? beforeStock + quantity : beforeStock - quantity;
      product.currentStock = afterStock;
      this.records.unshift({
        id: this.nextRecordId++,
        time: new Date().toLocaleTimeString("zh-CN", { hour: "2-digit", minute: "2-digit" }),
        productId: product.id,
        type,
        quantity,
        beforeStock,
        afterStock,
        source,
        remark
      });
      this.showToast("演示模式下已记录库存变更。", "success");
    },
    async submitPurchaseOrder() {
      const quantity = Number(this.purchaseForm.quantity);
      if (!Number.isInteger(quantity) || quantity <= 0) {
        this.showToast("采购数量必须大于 0。", "error");
        return;
      }
      const payload = {
        applicantId: this.currentUser.id,
        reason: this.purchaseForm.reason,
        items: [
          {
            productId: this.purchaseForm.productId,
            quantity,
            remark: this.purchaseForm.reason
          }
        ]
      };

      if (this.apiConnected) {
        try {
          await this.apiFetch("/purchase-orders", { method: "POST", body: JSON.stringify(payload) });
          await this.loadDashboard();
          this.showToast("采购申请已提交到后端。", "success");
          return;
        } catch (error) {
          this.apiConnected = false;
          this.connectionMessage = "后端连接中断，当前切换为演示模式。";
          this.showToast(error.message, "error");
        }
      }

      this.purchaseOrders.unshift({
        id: this.nextOrderId,
        code: `PO-DEMO-${String(this.nextOrderId).padStart(3, "0")}`,
        productId: this.purchaseForm.productId,
        quantity,
        applicant: this.displayName,
        applicantRole: this.currentUser.role,
        reason: this.purchaseForm.reason,
        status: "PENDING",
        approver: "",
        remark: ""
      });
      this.nextOrderId += 1;
      this.showToast("演示模式下已提交采购申请。", "success");
    },
    async approveOrder(order) {
      if (!this.isManager) {
        this.showToast("普通员工不能审批采购申请。", "error");
        return;
      }
      if (this.apiConnected) {
        try {
          await this.apiFetch(`/purchase-orders/${order.id}/approve`, {
            method: "POST",
            body: JSON.stringify({ approverId: this.currentUser.id, approvalRemark: "审批通过" })
          });
          await this.loadDashboard();
          this.showToast("采购单已审批通过。", "success");
          return;
        } catch (error) {
          this.showToast(error.message, "error");
          return;
        }
      }
      order.status = "APPROVED";
      order.approver = this.displayName;
      order.remark = "审批通过";
      this.showToast("演示模式下采购单已通过。", "success");
    },
    async rejectOrder(order) {
      if (!this.isManager) {
        this.showToast("普通员工不能驳回采购申请。", "error");
        return;
      }
      if (this.apiConnected) {
        try {
          await this.apiFetch(`/purchase-orders/${order.id}/reject`, {
            method: "POST",
            body: JSON.stringify({ approverId: this.currentUser.id, approvalRemark: "审批驳回" })
          });
          await this.loadDashboard();
          this.showToast("采购单已驳回。", "success");
          return;
        } catch (error) {
          this.showToast(error.message, "error");
          return;
        }
      }
      order.status = "REJECTED";
      order.approver = this.displayName;
      order.remark = "审批驳回";
      this.showToast("演示模式下采购单已驳回。", "success");
    },
    async completeOrder(order) {
      if (!this.isManager) {
        this.showToast("普通员工不能执行采购入库。", "error");
        return;
      }
      if (this.apiConnected) {
        try {
          await this.apiFetch(`/purchase-orders/${order.id}/complete`, {
            method: "POST",
            body: JSON.stringify({ operatorId: this.currentUser.id, remark: `采购入库 orderId=${order.id}` })
          });
          await this.loadDashboard();
          this.showToast("采购入库已完成。", "success");
          return;
        } catch (error) {
          this.showToast(error.message, "error");
          return;
        }
      }
      this.applyLocalStockChange(order.productId, "IN", Number(order.quantity), `采购入库 orderId=${order.id}`, "PURCHASE");
      order.status = "COMPLETED";
      order.remark = "采购入库完成";
    },
    productName(productId) {
      return this.products.find((product) => product.id === Number(productId))?.name || "未知商品";
    },
    isLowStock(product) {
      return Number(product.status) === 1 && Number(product.currentStock || 0) < Number(product.safeStock || 0);
    },
    statusLabel(status) {
      return {
        PENDING: "待审批",
        APPROVED: "已通过",
        REJECTED: "已驳回",
        COMPLETED: "已完成"
      }[status] || status;
    },
    statusClass(status) {
      return {
        PENDING: "tag-blue",
        APPROVED: "tag-ok",
        REJECTED: "tag-danger",
        COMPLETED: "tag-purple"
      }[status] || "tag-blue";
    },
    showToast(message, type = "success") {
      this.toast = { message, type };
      window.clearTimeout(this.toastTimer);
      this.toastTimer = window.setTimeout(() => {
        this.toast = null;
      }, 3000);
    }
  }
};
</script>

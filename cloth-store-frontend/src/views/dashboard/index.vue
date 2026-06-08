<template>
  <div class="dashboard-page">
    <section class="workspace-head">
      <div>
        <p class="eyebrow">业务工作台</p>
        <h2>今天需要关注的门店事项</h2>
        <span>聚焦库存、收款和日常录入，首页只放可行动的信息。</span>
      </div>
      <button class="refresh-button" type="button" :disabled="loading" @click="loadSummary">
        <el-icon :class="{ spinning: loading }"><Refresh /></el-icon>
        <span>{{ loading ? '刷新中' : '刷新数据' }}</span>
      </button>
    </section>

    <section class="metric-grid">
      <button
        v-for="item in statCards"
        :key="item.key"
        class="metric-card"
        :class="item.theme"
        type="button"
        @click="goTo(item.path)"
      >
        <span class="metric-label">{{ item.label }}</span>
        <strong>{{ item.value }}<small>{{ item.unit }}</small></strong>
        <span class="metric-note">{{ item.note }}</span>
      </button>
    </section>

    <section class="content-grid">
      <div class="main-column">
        <section class="panel">
          <div class="panel-title-row">
            <div>
              <h3>待处理事项</h3>
              <p>按业务风险排序，处理后会从工作台降噪。</p>
            </div>
            <el-button text type="primary" @click="goTo('/sku')">查看库存</el-button>
          </div>

          <div class="todo-list">
            <button
              v-for="item in todoItems"
              :key="item.title"
              class="todo-row"
              :class="item.severity"
              type="button"
              @click="goTo(item.path)"
            >
              <span class="todo-count">{{ item.count }}</span>
              <span class="todo-copy">
                <strong>{{ item.title }}</strong>
                <small>{{ item.description }}</small>
              </span>
              <el-icon><ArrowRight /></el-icon>
            </button>
          </div>
        </section>

        <section class="panel">
          <div class="panel-title-row">
            <div>
              <h3>库存预警</h3>
              <p>展示库存不高于 2 件的规格，优先补货或核对盘点。</p>
            </div>
            <el-button class="action-link" type="primary" plain @click="goTo('/purchase-record')">新增进货</el-button>
          </div>

          <div v-if="stockWarnings.length === 0" class="stock-empty-inline">
            <el-icon><CircleCheckFilled /></el-icon>
            <span>暂无低库存商品，库存状态正常</span>
          </div>
          <div v-else class="table-like warning-table">
            <div class="table-head">
              <span>商品</span>
              <span>规格</span>
              <span>库存</span>
              <span>售价</span>
              <span>状态</span>
            </div>
            <button
              v-for="item in stockWarnings"
              :key="item.skuId"
              class="table-row warning-row"
              type="button"
              @click="goTo('/sku')"
            >
              <span class="primary-text">{{ item.productName || '-' }}</span>
              <span>{{ formatSku(item) }}</span>
              <span :class="['stock-value', item.warningLevel]">{{ item.stock ?? 0 }}</span>
              <span>{{ formatMoney(item.salePrice) }} 元</span>
              <el-tag :type="item.warningLevel === 'danger' ? 'danger' : 'warning'" effect="light">
                {{ item.warningLevel === 'danger' ? '零库存' : '低库存' }}
              </el-tag>
            </button>
          </div>
        </section>

        <section class="panel">
          <div class="panel-title-row">
            <div>
              <h3>最近库存流水</h3>
              <p>快速确认系统内最近的库存变化来源。</p>
            </div>
            <el-button class="action-link" type="primary" plain @click="goTo('/stock-record')">查看全部</el-button>
          </div>
          <div class="activity-list">
            <div v-if="summary.recentActivities.length === 0" class="stock-empty-inline">
              <el-icon><Document /></el-icon>
              <span>暂无操作记录，开始录入后会显示在这里</span>
            </div>
            <div v-for="item in summary.recentActivities" :key="item.time" class="activity-row">
              <span class="activity-type">{{ item.operator || '记录' }}</span>
              <span class="activity-content">{{ item.content || '-' }}</span>
              <span class="activity-time">{{ formatDateTime(item.time) }}</span>
            </div>
          </div>
        </section>
      </div>

      <aside class="side-column">
        <section class="panel quick-panel">
          <h3>常用操作</h3>
          <div class="quick-list">
            <button v-for="item in quickLinks" :key="item.path" type="button" @click="goTo(item.path)">
              <el-icon><component :is="item.icon" /></el-icon>
              <span>
                <strong>{{ item.label }}</strong>
                <small>{{ item.desc }}</small>
              </span>
            </button>
          </div>

          <div class="side-stock-card">
            <div class="side-card-head">
              <h4>库存状态</h4>
              <button class="mini-link" type="button" @click="goTo('/sku')">查看明细</button>
            </div>
            <div class="stock-health" :class="stockHealthLevel">
              <span class="health-dot"></span>
              <span>
                <strong>{{ stockHealthText }}</strong>
                <small>{{ stockHealthDesc }}</small>
              </span>
            </div>
            <div class="side-stat-grid">
              <button type="button" @click="goTo('/sku')">
                <strong>{{ summary.stockCount || 0 }}</strong>
                <span>库存件数</span>
              </button>
              <button type="button" @click="goTo('/sku')">
                <strong>{{ summary.lowStockCount || 0 }}</strong>
                <span>预警规格</span>
              </button>
              <button type="button" @click="goTo('/sku')">
                <strong>{{ summary.zeroStockCount || 0 }}</strong>
                <span>零库存</span>
              </button>
            </div>
          </div>
        </section>
      </aside>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getSummary } from '@/api/dashboard'

const router = useRouter()
const loading = ref(false)

const summary = reactive({
  todaySalesAmount: '0.00',
  yesterdaySalesAmount: '0.00',
  changePercent: null,
  productCount: 0,
  stockCount: 0,
  lowStockCount: 0,
  zeroStockCount: 0,
  unpaidOrderCount: 0,
  unpaidAmount: 0,
  todoItems: [],
  stockWarnings: [],
  recentActivities: []
})

const statCards = computed(() => [
  {
    key: 'sales',
    label: '今日销售额',
    value: formatMoney(summary.todaySalesAmount),
    unit: '元',
    note: formatChangeNote(summary.changePercent),
    theme: 'blue',
    path: '/sales-statistics'
  },
  {
    key: 'unpaid',
    label: '未收款金额',
    value: formatMoney(summary.unpaidAmount),
    unit: '元',
    note: `${summary.unpaidOrderCount || 0} 笔待核对`,
    theme: summary.unpaidOrderCount > 0 ? 'amber' : 'green',
    path: '/sale-record'
  },
  {
    key: 'stock',
    label: '库存总数',
    value: summary.stockCount || 0,
    unit: '件',
    note: `${summary.productCount || 0} 个商品`,
    theme: 'green',
    path: '/sku'
  },
  {
    key: 'low',
    label: '库存预警',
    value: summary.lowStockCount || 0,
    unit: '项',
    note: `${summary.zeroStockCount || 0} 项零库存`,
    theme: summary.lowStockCount > 0 ? 'red' : 'blue',
    path: '/sku'
  }
])

const quickLinks = [
  { path: '/sale-record', label: '新增销售记录', desc: '卖货后立即扣减库存', icon: 'Sell' },
  { path: '/purchase-record', label: '新增进货记录', desc: '补货后同步库存', icon: 'DocumentAdd' },
  { path: '/product', label: '维护商品信息', desc: '商品、货号、图片', icon: 'Goods' },
  { path: '/sku', label: '调整库存规格', desc: '颜色、尺码、价格', icon: 'Box' }
]

const todoItems = computed(() => summary.todoItems || [])
const stockWarnings = computed(() => summary.stockWarnings || [])
const stockHealthLevel = computed(() => {
  if ((summary.zeroStockCount || 0) > 0) return 'danger'
  if ((summary.lowStockCount || 0) > 0) return 'warning'
  return 'success'
})
const stockHealthText = computed(() => {
  if (stockHealthLevel.value === 'danger') return '存在零库存'
  if (stockHealthLevel.value === 'warning') return '有低库存待补'
  return '库存状态正常'
})
const stockHealthDesc = computed(() => {
  if (stockHealthLevel.value === 'danger') return '优先处理零库存规格，避免售卖缺货。'
  if (stockHealthLevel.value === 'warning') return '建议补货或核对近期销售变化。'
  return '暂无补货压力，可继续关注日常流水。'
})

function goTo(path) {
  if (path) router.push(path)
}

async function loadSummary() {
  loading.value = true
  try {
    const res = await getSummary()
    const data = res.data || {}
    Object.assign(summary, {
      todaySalesAmount: data.todaySalesAmount ?? '0.00',
      yesterdaySalesAmount: data.yesterdaySalesAmount ?? '0.00',
      changePercent: data.changePercent,
      productCount: data.productCount ?? 0,
      stockCount: data.stockCount ?? 0,
      lowStockCount: data.lowStockCount ?? 0,
      zeroStockCount: data.zeroStockCount ?? 0,
      unpaidOrderCount: data.unpaidOrderCount ?? 0,
      unpaidAmount: data.unpaidAmount ?? 0,
      todoItems: data.todoItems || [],
      stockWarnings: data.stockWarnings || [],
      recentActivities: data.recentActivities || []
    })
  } finally {
    loading.value = false
  }
}

function formatMoney(value) {
  const amount = Number(value || 0)
  if (!Number.isFinite(amount)) return '0'
  return amount % 1 === 0 ? amount.toFixed(0) : amount.toFixed(2)
}

function formatChangeNote(percent) {
  if (percent == null) return '昨日无销售'
  const value = Number(percent)
  if (!Number.isFinite(value) || value === 0) return '与昨日持平'
  return `比昨日${value > 0 ? '+' : ''}${value.toFixed(2)}%`
}

function formatSku(item) {
  return `${item.color || '无颜色'} / ${item.size || '无尺码'}`
}

function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

onMounted(() => {
  loadSummary()
})
</script>

<style scoped>
.dashboard-page {
  max-width: 1220px;
  margin: 0 auto;
}

.workspace-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 18px;
}

.eyebrow {
  margin: 0 0 8px;
  color: #2563eb;
  font-size: 13px;
  font-weight: 800;
}

.workspace-head h2 {
  margin: 0;
  color: #0f172a;
  font-size: 24px;
  font-weight: 850;
}

.workspace-head span,
.panel-title-row p,
.quick-list small,
.todo-copy small {
  color: #64748b;
}

.workspace-head span {
  display: block;
  margin-top: 8px;
  font-size: 14px;
}

.refresh-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 40px;
  padding: 0 14px;
  color: #1f2937;
  background: #ffffff;
  border: 1px solid #dbe3ee;
  border-radius: 6px;
  cursor: pointer;
}

.refresh-button:disabled {
  cursor: not-allowed;
  opacity: 0.72;
}

.spinning {
  animation: spin 1s linear infinite;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.metric-card {
  min-height: 118px;
  padding: 18px;
  text-align: left;
  background: #ffffff;
  border: 1px solid #e5ebf3;
  border-radius: 8px;
  cursor: pointer;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}

.metric-card:hover,
.todo-row:hover,
.quick-list button:hover,
.side-stat-grid button:hover,
.table-row:hover {
  border-color: #93c5fd;
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.07);
}

.metric-label {
  display: block;
  color: #475569;
  font-size: 14px;
  font-weight: 700;
}

.metric-card strong {
  display: block;
  margin-top: 14px;
  color: #0f172a;
  font-size: 30px;
  line-height: 1;
}

.metric-card small {
  margin-left: 5px;
  color: #64748b;
  font-size: 13px;
}

.metric-note {
  display: block;
  margin-top: 14px;
  color: #64748b;
  font-size: 13px;
}

.metric-card.blue {
  border-top: 3px solid #2563eb;
}

.metric-card.green {
  border-top: 3px solid #16a34a;
}

.metric-card.amber {
  border-top: 3px solid #f59e0b;
}

.metric-card.red {
  border-top: 3px solid #dc2626;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 12px;
  margin-bottom: 12px;
}

.main-column,
.side-column {
  display: grid;
  gap: 12px;
  align-content: start;
}

.panel {
  padding: 18px;
  background: #ffffff;
  border: 1px solid #e5ebf3;
  border-radius: 8px;
}

.panel-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.panel-title-row h3,
.quick-panel h3,
.side-card-head h4 {
  margin: 0;
  color: #0f172a;
  font-size: 17px;
  font-weight: 850;
}

.quick-panel {
  min-height: 100%;
}

.quick-panel h3 {
  margin-bottom: 14px;
}

.panel-title-row p {
  margin: 6px 0 0;
  font-size: 13px;
}

.action-link {
  color: #2563eb !important;
  background: transparent !important;
  border-color: transparent !important;
  box-shadow: none !important;
}

.action-link:hover,
.action-link:focus {
  color: #1d4ed8 !important;
  background: #eff6ff !important;
  border-color: #bfdbfe !important;
}

.todo-list,
.quick-list {
  display: grid;
  gap: 10px;
}

.todo-row,
.quick-list button,
.table-row {
  width: 100%;
  background: #ffffff;
  border: 1px solid #edf2f7;
  border-radius: 7px;
  font: inherit;
  cursor: pointer;
}

.todo-row {
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr) 24px;
  align-items: center;
  gap: 12px;
  padding: 12px;
  text-align: left;
}

.todo-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  color: #1d4ed8;
  background: #eff6ff;
  border-radius: 6px;
  font-size: 20px;
  font-weight: 850;
}

.todo-row.warning .todo-count {
  color: #b45309;
  background: #fffbeb;
}

.todo-row.danger .todo-count {
  color: #b91c1c;
  background: #fef2f2;
}

.todo-row.success .todo-count {
  color: #15803d;
  background: #f0fdf4;
}

.todo-copy {
  display: grid;
  gap: 5px;
  min-width: 0;
}

.todo-copy strong,
.quick-list strong {
  color: #0f172a;
  font-size: 14px;
}

.table-like {
  overflow: hidden;
  border: 1px solid #edf2f7;
  border-radius: 8px;
}

.table-head,
.table-row {
  display: grid;
  grid-template-columns: 1.4fr 1.1fr 72px 88px 84px;
  align-items: center;
  gap: 12px;
  padding: 0 14px;
}

.table-head {
  min-height: 42px;
  color: #475569;
  background: #f8fafc;
  font-size: 13px;
  font-weight: 800;
}

.table-row {
  min-height: 54px;
  color: #334155;
  text-align: left;
  border-width: 1px 0 0;
  border-radius: 0;
}

.primary-text {
  color: #0f172a;
  font-weight: 800;
}

.stock-value {
  font-weight: 850;
}

.stock-value.danger {
  color: #dc2626;
}

.stock-value.warning {
  color: #d97706;
}

.stock-empty-inline {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 54px;
  padding: 0 14px;
  color: #475569;
  background: #f8fafc;
  border: 1px solid #edf2f7;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 700;
}

.stock-empty-inline .el-icon {
  color: #16a34a;
  font-size: 20px;
}

.quick-list button {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 92px;
  padding: 16px 18px;
  text-align: left;
}

.quick-list .el-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  color: #2563eb;
  background: #eff6ff;
  border-radius: 6px;
  font-size: 22px;
  flex: 0 0 auto;
}

.quick-list span {
  display: grid;
  gap: 5px;
  min-width: 0;
}

.side-stock-card {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid #edf2f7;
}

.side-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.side-card-head h4 {
  font-size: 15px;
}

.mini-link {
  padding: 0;
  color: #2563eb;
  background: transparent;
  border: 0;
  font: inherit;
  font-size: 13px;
  font-weight: 750;
  cursor: pointer;
}

.mini-link:hover {
  color: #1d4ed8;
}

.stock-health {
  display: grid;
  grid-template-columns: 10px minmax(0, 1fr);
  gap: 10px;
  align-items: start;
  padding: 12px;
  background: #f8fafc;
  border: 1px solid #edf2f7;
  border-radius: 7px;
}

.health-dot {
  width: 10px;
  height: 10px;
  margin-top: 4px;
  border-radius: 50%;
}

.stock-health.success .health-dot {
  background: #16a34a;
  box-shadow: 0 0 0 4px rgba(22, 163, 74, 0.12);
}

.stock-health.warning .health-dot {
  background: #f59e0b;
  box-shadow: 0 0 0 4px rgba(245, 158, 11, 0.13);
}

.stock-health.danger .health-dot {
  background: #dc2626;
  box-shadow: 0 0 0 4px rgba(220, 38, 38, 0.12);
}

.stock-health span:last-child {
  display: grid;
  gap: 5px;
  min-width: 0;
}

.stock-health strong {
  color: #0f172a;
  font-size: 14px;
}

.stock-health small {
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

.side-stat-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin-top: 10px;
}

.side-stat-grid button {
  display: grid;
  gap: 6px;
  min-height: 68px;
  padding: 10px 6px;
  text-align: center;
  background: #ffffff;
  border: 1px solid #edf2f7;
  border-radius: 7px;
  font: inherit;
  cursor: pointer;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}

.side-stat-grid strong {
  color: #0f172a;
  font-size: 18px;
  line-height: 1;
}

.side-stat-grid span {
  color: #64748b;
  font-size: 12px;
}

.activity-list {
  border: 1px solid #edf2f7;
  border-radius: 8px;
  overflow: hidden;
}

.activity-row {
  display: grid;
  grid-template-columns: 86px minmax(0, 1fr) 150px;
  align-items: center;
  gap: 12px;
  min-height: 46px;
  padding: 0 14px;
  border-top: 1px solid #edf2f7;
  color: #334155;
  font-size: 14px;
}

.activity-row:first-child {
  border-top: 0;
}

.activity-type {
  color: #2563eb;
  font-weight: 800;
}

.activity-content {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-time {
  color: #64748b;
  text-align: right;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 138px;
  color: #64748b;
  text-align: center;
}

.empty-state.compact {
  min-height: 96px;
}

.empty-state .el-icon {
  margin-bottom: 10px;
  color: #94a3b8;
  font-size: 32px;
}

.empty-state strong {
  color: #334155;
  font-size: 14px;
}

.empty-state span {
  margin-top: 6px;
  font-size: 13px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media screen and (max-width: 1180px) {
  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .content-grid {
    grid-template-columns: 1fr;
  }

  .side-column {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media screen and (max-width: 768px) {
  .dashboard-page {
    max-width: none;
  }

  .workspace-head {
    display: block;
  }

  .refresh-button {
    width: 100%;
    justify-content: center;
    margin-top: 14px;
  }

  .metric-grid,
  .side-column {
    grid-template-columns: 1fr;
  }

  .panel {
    padding: 14px;
  }

  .panel-title-row {
    display: block;
  }

  .panel-title-row .el-button {
    width: 100%;
    margin-top: 12px;
  }

  .table-head {
    display: none;
  }

  .table-row {
    grid-template-columns: 1fr;
    gap: 7px;
    padding: 12px;
  }

  .activity-row {
    grid-template-columns: 1fr;
    gap: 5px;
    padding: 12px;
  }

  .activity-time {
    text-align: left;
  }
}
</style>

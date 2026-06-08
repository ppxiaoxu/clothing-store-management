<template>
  <div class="dashboard">
    <section class="panel overview-panel">
      <div class="panel-header">
        <h2>今日概况</h2>
        <button class="refresh-button" type="button" @click="loadSummary">
          <span>数据实时更新</span>
          <el-icon><Refresh /></el-icon>
        </button>
      </div>

      <div class="stats-grid">
        <button
          v-for="item in statCards"
          :key="item.key"
          class="stat-card"
          :class="item.theme"
          type="button"
          @click="goTo(item.path)"
        >
          <span class="stat-icon">
            <el-icon><component :is="item.icon" /></el-icon>
          </span>
          <span class="stat-content">
            <span class="stat-label">{{ item.label }}</span>
            <span class="stat-value">
              {{ item.value }}
              <span>{{ item.unit }}</span>
            </span>
            <span class="stat-note">{{ item.note }}</span>
          </span>
        </button>
      </div>
    </section>

    <section class="panel actions-panel">
      <div class="panel-header">
        <h2>常用操作</h2>
      </div>

      <div class="actions-grid">
        <button
          v-for="item in quickLinks"
          :key="item.path"
          class="action-card"
          :class="item.theme"
          type="button"
          @click="goTo(item.path)"
        >
          <span class="action-icon">
            <el-icon><component :is="item.icon" /></el-icon>
          </span>
          <span class="action-copy">
            <strong>{{ item.label }}</strong>
            <span>{{ item.desc }}</span>
          </span>
          <span class="action-arrow">
            <el-icon><ArrowRight /></el-icon>
          </span>
        </button>
      </div>
    </section>

    <section class="panel records-panel">
      <div class="panel-header">
        <h2>最近操作记录</h2>
      </div>

      <div class="records-table">
        <div class="table-head">
          <span>操作内容</span>
          <span>操作时间</span>
          <span>操作人员</span>
        </div>
        <div v-if="summary.recentActivities.length === 0" class="empty-state">
          <el-icon><Document /></el-icon>
          <strong>暂无操作记录</strong>
          <span>开始使用系统后，操作记录会显示在这里</span>
        </div>
        <div v-for="item in summary.recentActivities" :key="item.time" class="table-row">
          <span>{{ item.content || '-' }}</span>
          <span>{{ formatActivityTime(item.time) }}</span>
          <span><el-tag size="small" type="info">{{ item.operator }}</el-tag></span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getSummary } from '@/api/dashboard'

const router = useRouter()

const summary = reactive({
  todaySalesAmount: '0.00',
  yesterdaySalesAmount: '0.00',
  changePercent: null,
  productCount: 0,
  stockCount: 0,
  lowStockCount: 0,
  recentActivities: []
})

const statCards = computed(() => [
  {
    key: 'sales',
    label: '今日销售额',
    value: formatMoney(summary.todaySalesAmount),
    unit: '元',
    note: formatChangeNote(summary.changePercent),
    icon: 'Money',
    theme: 'green',
    path: '/sales-statistics'
  },
  {
    key: 'products',
    label: '商品总数',
    value: summary.productCount,
    unit: '种',
    note: '全部商品数量',
    icon: 'ShoppingBag',
    theme: 'blue',
    path: '/product'
  },
  {
    key: 'stocks',
    label: '库存总数',
    value: summary.stockCount,
    unit: '件',
    note: '所有商品库存',
    icon: 'Box',
    theme: 'orange',
    path: '/sku'
  },
  {
    key: 'lowStock',
    label: '低库存商品',
    value: summary.lowStockCount,
    unit: '种',
    note: '需要及时补货',
    icon: 'WarningFilled',
    theme: 'red',
    path: '/sku'
  }
])

const quickLinks = [
  { path: '/product', label: '新增商品', desc: '添加新商品', icon: 'Goods', theme: 'green' },
  { path: '/supplier', label: '管理进货商', desc: '查看进货商信息', icon: 'UserFilled', theme: 'blue' },
  { path: '/category', label: '整理分类', desc: '管理商品分类', icon: 'Grid', theme: 'orange' },
  { path: '/sku', label: '查看库存', desc: '查看库存情况', icon: 'Shop', theme: 'purple' }
]

function formatMoney(value) {
  const amount = Number(value)
  if (!Number.isFinite(amount)) return '0'
  return amount % 1 === 0 ? amount.toFixed(0) : amount.toFixed(2)
}

function goTo(path) {
  if (path) router.push(path)
}

async function loadSummary() {
  try {
    const res = await getSummary()
    const d = res.data || {}
    summary.todaySalesAmount = d.todaySalesAmount ?? '0.00'
    summary.yesterdaySalesAmount = d.yesterdaySalesAmount ?? '0.00'
    summary.changePercent = d.changePercent
    summary.productCount = d.productCount ?? 0
    summary.stockCount = d.stockCount ?? 0
    summary.lowStockCount = d.lowStockCount ?? 0
    summary.recentActivities = d.recentActivities || []
  } catch {
    summary.todaySalesAmount = summary.todaySalesAmount || '0.00'
  }
}

function formatChangeNote(percent) {
  if (percent == null) return '昨日无销售'
  const value = Number(percent)
  if (value === 0) return '与昨天持平'
  const sign = value > 0 ? '+' : ''
  return '比昨天 ' + sign + value.toFixed(2) + '%'
}

function formatActivityTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

onMounted(() => loadSummary())
</script>

<style scoped>
.dashboard {
  max-width: 1180px;
  margin: 0 auto;
}

.panel {
  padding: 20px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid #e8edf5;
  border-radius: 8px;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.06);
}

.panel + .panel {
  margin-top: 16px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 22px;
}

.panel-header h2 {
  margin: 0;
  color: #0f1d37;
  font-size: 22px;
  font-weight: 850;
}

.refresh-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0;
  color: #64748b;
  background: transparent;
  border: 0;
  font-size: 14px;
  cursor: pointer;
}

.refresh-button:hover {
  color: #2563eb;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.stat-card,
.action-card {
  border: 1px solid transparent;
  border-radius: 8px;
  font-family: inherit;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.stat-card {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  align-items: center;
  gap: 18px;
  min-height: 132px;
  padding: 24px 22px;
  text-align: left;
}

.stat-card:hover,
.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.09);
}

.stat-card.green,
.action-card.green {
  background: linear-gradient(135deg, #eefdf5 0%, #f8fffb 100%);
  border-color: #c4f0d8;
}

.stat-card.blue,
.action-card.blue {
  background: linear-gradient(135deg, #f0f7ff 0%, #fbfdff 100%);
  border-color: #cfe2ff;
}

.stat-card.orange,
.action-card.orange {
  background: linear-gradient(135deg, #fff7ed 0%, #fffdf9 100%);
  border-color: #fed7aa;
}

.stat-card.red {
  background: linear-gradient(135deg, #fff1f2 0%, #fffafa 100%);
  border-color: #fecdd3;
}

.action-card.purple {
  background: linear-gradient(135deg, #f5f0ff 0%, #fcfaff 100%);
  border-color: #ddd6fe;
}

.stat-icon,
.action-icon,
.action-arrow {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  border-radius: 50%;
  flex: 0 0 auto;
}

.stat-icon {
  width: 68px;
  height: 68px;
  font-size: 34px;
}

.green .stat-icon,
.green .action-icon,
.green .action-arrow {
  background: linear-gradient(135deg, #22c55e, #16a34a);
}

.blue .stat-icon,
.blue .action-icon,
.blue .action-arrow {
  background: linear-gradient(135deg, #60a5fa, #2563eb);
}

.orange .stat-icon,
.orange .action-icon,
.orange .action-arrow {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
}

.red .stat-icon {
  background: linear-gradient(135deg, #fb7185, #dc2626);
}

.purple .action-icon,
.purple .action-arrow {
  background: linear-gradient(135deg, #a78bfa, #7c3aed);
}

.stat-content {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.stat-label {
  color: #1f2937;
  font-size: 15px;
  font-weight: 700;
}

.stat-value {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin: 14px 0 16px;
  color: #0f172a;
  font-size: clamp(22px, 3vw, 36px);
  font-weight: 850;
  line-height: 1;
  min-width: 0;
  overflow: hidden;
  white-space: nowrap;
}

.stat-value span {
  font-size: clamp(12px, 1vw, 14px);
  font-weight: 700;
  flex-shrink: 0;
}

.stat-note {
  color: #475569;
  font-size: 14px;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.action-card {
  display: grid;
  grid-template-columns: 46px minmax(78px, 1fr) 32px;
  align-items: center;
  gap: 10px;
  min-height: 92px;
  padding: 16px;
  text-align: left;
}

.action-icon {
  width: 46px;
  height: 46px;
  border-radius: 8px;
  font-size: 25px;
}

.action-copy {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.action-copy strong {
  color: #0f1d37;
  font-size: 16px;
  font-weight: 850;
  white-space: nowrap;
}

.action-copy span {
  margin-top: 7px;
  color: #475569;
  font-size: 13px;
}

.action-arrow {
  width: 32px;
  height: 32px;
  font-size: 18px;
}

.records-table {
  overflow: hidden;
  border: 1px solid #e6ebf2;
  border-radius: 8px;
  background: #ffffff;
}

.table-head {
  display: grid;
  grid-template-columns: 1.4fr 1fr 1fr;
  min-height: 46px;
  align-items: center;
  padding: 0 16px;
  color: #374151;
  background: #f8fafc;
  border-bottom: 1px solid #e6ebf2;
  font-size: 14px;
  font-weight: 800;
}

.table-row {
  display: grid;
  grid-template-columns: 1.4fr 1fr 1fr;
  min-height: 46px;
  align-items: center;
  padding: 0 16px;
  color: #1f2937;
  border-bottom: 1px solid #f1f5f9;
  font-size: 14px;
}

.table-row:last-child {
  border-bottom: 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 150px;
  color: #7b8798;
  text-align: center;
}

.empty-state .el-icon {
  margin-bottom: 12px;
  color: #b7c0cc;
  font-size: 42px;
}

.empty-state strong {
  margin-bottom: 8px;
  color: #475569;
  font-size: 15px;
}

.empty-state span {
  font-size: 14px;
}

@media screen and (max-width: 1180px) {
  .stats-grid,
  .actions-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media screen and (max-width: 640px) {
  .dashboard {
    max-width: none;
    width: 100%;
  }

  .panel {
    padding: 14px;
  }

  .panel-header {
    align-items: flex-start;
    margin-bottom: 16px;
  }

  .panel-header h2 {
    font-size: 19px;
  }

  .refresh-button span {
    display: none;
  }

  .stats-grid,
  .actions-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
  }

  .stat-card {
    grid-template-columns: 1fr;
    gap: 10px;
    min-height: 148px;
    padding: 16px;
  }

  .stat-icon {
    width: 58px;
    height: 58px;
    font-size: 30px;
  }

  .stat-value {
    margin: 10px 0 12px;
    font-size: 28px;
  }

  .action-card {
    grid-template-columns: 44px minmax(0, 1fr);
    min-height: 96px;
    padding: 14px;
  }

  .action-arrow {
    display: none;
  }

  .action-copy strong {
    font-size: 16px;
  }

  .table-head {
    grid-template-columns: 1fr 0.9fr 0.8fr;
    padding: 0 12px;
    font-size: 12px;
  }
}

@media screen and (max-width: 420px) {
  .stats-grid,
  .actions-grid {
    grid-template-columns: 1fr;
  }

  .stat-card {
    grid-template-columns: auto minmax(0, 1fr);
    min-height: 112px;
  }
}
</style>

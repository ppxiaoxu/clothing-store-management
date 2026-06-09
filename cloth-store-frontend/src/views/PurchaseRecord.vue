<template>
  <div class="page">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="进货商">
          <el-input v-model="searchForm.supplierName" placeholder="请输入进货商名称" clearable />
        </el-form-item>
        <el-form-item label="进货日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            range-separator="至"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <div class="toolbar">
        <el-button type="primary" @click="openAdd">新增进货记录</el-button>
      </div>

      <div class="table-wrap desktop-table">
        <el-table :data="tableData" stripe border v-loading="loading">
          <el-table-column prop="purchaseTime" label="进货时间" min-width="170">
            <template #default="{ row }">{{ formatDateTime(row.purchaseTime) }}</template>
          </el-table-column>
          <el-table-column prop="supplierName" label="进货商名称" min-width="140" />
          <el-table-column prop="productName" label="商品名称" min-width="150" />
          <el-table-column prop="color" label="颜色" width="100" />
          <el-table-column prop="size" label="尺码" width="100" />
          <el-table-column prop="quantity" label="进货数量" width="110" />
          <el-table-column prop="purchasePrice" label="进货单价" width="120">
            <template #default="{ row }">{{ formatMoney(row.purchasePrice) }}</template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="进货总金额" width="130">
            <template #default="{ row }">{{ formatMoney(row.totalAmount) }}</template>
          </el-table-column>
          <el-table-column prop="creatorName" label="操作人" width="100">
            <template #default="{ row }">{{ row.creatorName || '-' }}</template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
          <el-table-column label="操作" width="190" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="openEdit(row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="mobile-list" v-loading="loading">
        <div v-for="row in tableData" :key="row.id" class="mobile-record-card">
          <h3 class="mobile-card-title">{{ row.productName || '未命名商品' }}</h3>
          <div class="mobile-card-row"><span>进货商</span><strong>{{ row.supplierName || '-' }}</strong></div>
          <div class="mobile-card-row"><span>规格</span><strong>{{ row.color || '无颜色' }} / {{ row.size || '无尺码' }}</strong></div>
          <div class="mobile-card-row"><span>数量</span><strong>{{ row.quantity }} 件</strong></div>
          <div class="mobile-card-row"><span>单价</span><strong>{{ formatMoney(row.purchasePrice) }} 元</strong></div>
          <div class="mobile-card-row"><span>总金额</span><strong>{{ formatMoney(row.totalAmount) }} 元</strong></div>
          <div class="mobile-card-row"><span>操作人</span><strong>{{ row.creatorName || '-' }}</strong></div>
          <div class="mobile-card-row"><span>备注</span><strong>{{ row.remark || '-' }}</strong></div>
          <div class="mobile-card-row"><span>时间</span><strong>{{ formatDateTime(row.purchaseTime) }}</strong></div>
          <div class="mobile-card-actions">
            <el-button type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" @click="handleDelete(row)">删除</el-button>
          </div>
        </div>
      </div>

      <el-pagination
        v-model:current-page="searchForm.pageNum"
        v-model:page-size="searchForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="620px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="record-form">
        <el-form-item label="进货商" prop="supplierId">
          <el-select v-model="form.supplierId" placeholder="请选择进货商" filterable @change="handleSupplierChange">
            <el-option v-for="item in supplierOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择商品" filterable :disabled="!form.supplierId" @change="handleProductChange">
            <el-option v-for="item in filteredProductOptions" :key="item.id" :label="item.name" :value="item.id" />
            <template #empty>
              {{ form.supplierId ? '该进货商暂无商品，请先添加商品信息' : '请先选择进货商' }}
            </template>
          </el-select>
        </el-form-item>
        <el-form-item label="商品规格" prop="skuId">
          <el-select v-model="form.skuId" placeholder="请选择商品规格" filterable :disabled="!form.productId">
            <el-option
              v-for="item in filteredSkuOptions"
              :key="item.id"
              :label="formatSkuLabel(item)"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="进货数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" :max="999999" :precision="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="进货单价" prop="purchasePrice">
          <el-input-number v-model="form.purchasePrice" :min="0" :max="999999" :precision="2" :step="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="进货总金额">
          <el-input :model-value="computedTotalAmount" disabled />
        </el-form-item>
        <el-form-item label="进货日期" prop="purchaseTime">
          <el-date-picker
            v-model="form.purchaseTime"
            type="date"
            placeholder="请选择进货日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="其他说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createPurchaseRecord,
  deletePurchaseRecord,
  getPurchaseRecordPage,
  updatePurchaseRecord
} from '@/api/purchaseRecord'
import { getSupplierList } from '@/api/supplier'
import { getProductList } from '@/api/product'
import { getSkuList } from '@/api/sku'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)
const dateRange = ref([])
const supplierOptions = ref([])
const productOptions = ref([])
const skuOptions = ref([])

const searchForm = reactive({
  pageNum: 1,
  pageSize: 10,
  productName: '',
  supplierName: ''
})

const form = reactive(getDefaultForm())

const rules = {
  supplierId: [{ required: true, message: '请选择进货商', trigger: 'change' }],
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  skuId: [{ required: true, message: '请选择商品规格', trigger: 'change' }],
  quantity: [
    { required: true, message: '请输入进货数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '进货数量必须大于0', trigger: 'blur' },
    { type: 'number', max: 999999, message: '进货数量不能超过999999', trigger: 'blur' }
  ],
  purchasePrice: [
    { required: true, message: '请输入进货单价', trigger: 'blur' },
    { type: 'number', min: 0, message: '进货单价不能小于0', trigger: 'blur' },
    { type: 'number', max: 999999, message: '进货单价不能超过999999', trigger: 'blur' }
  ],
  purchaseTime: [{ required: true, message: '请选择进货时间', trigger: 'change' }]
}

const filteredProductOptions = computed(() => {
  if (!form.supplierId) return []
  return productOptions.value.filter(item => item.supplierId === form.supplierId)
})

const filteredSkuOptions = computed(() => {
  if (!form.productId) return []
  return skuOptions.value.filter(item => item.productId === form.productId)
})

const computedTotalAmount = computed(() => {
  const quantity = Number(form.quantity || 0)
  const price = Number(form.purchasePrice || 0)
  return (quantity * price).toFixed(2)
})

function getDefaultForm() {
  return {
    id: undefined,
    supplierId: undefined,
    productId: undefined,
    skuId: undefined,
    quantity: 1,
    purchasePrice: 0,
    purchaseTime: getNowValue(),
    remark: ''
  }
}

function getNowValue() {
  const date = new Date()
  const pad = value => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      startTime: dateRange.value?.[0] ? dateRange.value[0] + 'T00:00:00' : undefined,
      endTime: dateRange.value?.[1] ? dateRange.value[1] + 'T23:59:59' : undefined
    }
    const res = await getPurchaseRecordPage(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  const [supplierRes, productRes, skuRes] = await Promise.all([
    getSupplierList({ pageNum: 1, pageSize: 99999 }),
    getProductList({ pageNum: 1, pageSize: 99999 }),
    getSkuList({ pageNum: 1, pageSize: 99999 })
  ])
  supplierOptions.value = supplierRes.data.records || []
  productOptions.value = productRes.data.records || []
  skuOptions.value = skuRes.data.records || []
}

function handleSearch() {
  searchForm.pageNum = 1
  fetchData()
}

function resetSearch() {
  searchForm.productName = ''
  searchForm.supplierName = ''
  dateRange.value = []
  handleSearch()
}

function openAdd() {
  isEdit.value = false
  dialogTitle.value = '新增进货记录'
  Object.assign(form, getDefaultForm())
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑进货记录'
  Object.assign(form, {
    id: row.id,
    supplierId: row.supplierId,
    productId: row.productId,
    skuId: row.skuId,
    quantity: row.quantity,
    purchasePrice: Number(row.purchasePrice || 0),
    purchaseTime: normalizeDateTime(row.purchaseTime),
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

function handleSupplierChange() {
  form.productId = undefined
  form.skuId = undefined
}

function handleProductChange() {
  form.skuId = undefined
}

function resetForm() {
  formRef.value?.clearValidate()
  Object.assign(form, getDefaultForm())
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const payload = {
      ...form,
      purchasePrice: Number(form.purchasePrice),
      quantity: Number(form.quantity),
      purchaseTime: form.purchaseTime ? form.purchaseTime + 'T00:00:00' : undefined
    }
    if (isEdit.value) {
      await updatePurchaseRecord(payload)
      ElMessage.success('修改成功')
    } else {
      await createPurchaseRecord(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadOptions()
    fetchData()
  } finally {
    saving.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除"${row.productName} ${row.color}/${row.size}"的进货记录吗？删除后会回滚对应库存。`, '提示', { type: 'warning' })
    .then(async () => {
      await deletePurchaseRecord(row.id)
      ElMessage.success('删除成功')
      await loadOptions()
      fetchData()
    })
    .catch(() => {})
}

function formatSkuLabel(item) {
  return `${item.color || '无颜色'} / ${item.size || '无尺码'}（库存 ${item.stock ?? 0}）`
}

function normalizeDateTime(value) {
  if (!value) return getNowValue()
  return String(value).replace(' ', 'T').slice(0, 10)
}

function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 10)
}

function formatMoney(value) {
  const amount = Number(value || 0)
  return amount % 1 === 0 ? amount.toFixed(0) : amount.toFixed(2)
}

onMounted(async () => {
  await loadOptions()
  fetchData()
})
</script>

<style scoped>
.page {
  max-width: 1180px;
  margin: 0 auto;
}

.search-card,
.table-card {
  margin-bottom: 12px;
}

.search-form :deep(.el-date-editor) {
  width: 360px;
}

.toolbar {
  margin-bottom: 12px;
}

.table-wrap {
  overflow-x: auto;
}

.record-form :deep(.el-select),
.record-form :deep(.el-date-editor),
.record-form :deep(.el-input-number) {
  width: 100%;
}

.record-form :deep(.el-form-item__label) {
  white-space: nowrap;
}

@media screen and (max-width: 768px) {
  .page {
    max-width: none;
    width: 100%;
  }

  .search-form :deep(.el-date-editor) {
    width: 100%;
  }

  .toolbar .el-button {
    width: 100%;
  }
}
</style>

<template>
  <div class="page">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="颜色">
          <el-input v-model="searchForm.color" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="尺码">
          <el-input v-model="searchForm.size" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card">
      <div class="toolbar">
        <el-button type="primary" @click="openAdd">新增规格</el-button>
      </div>
      <div class="table-wrap desktop-table">
        <el-table :data="tableData" stripe border v-loading="loading">
          <el-table-column prop="id" label="编号" width="80" />
          <el-table-column label="所属商品" min-width="150">
            <template #default="{ row }">{{ getProductName(row.productId) }}</template>
          </el-table-column>
          <el-table-column prop="color" label="颜色" width="110" />
          <el-table-column prop="size" label="尺码" width="90" />
          <el-table-column prop="stock" label="库存数量" width="110">
            <template #default="{ row }">
              <span :style="{ color: row.stock < 10 ? '#F56C6C' : '#303133', fontWeight: row.stock < 10 ? 700 : 400 }">
                {{ row.stock }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="costPrice" label="进价(元)" width="110" />
          <el-table-column prop="salePrice" label="售价(元)" width="110" />
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button type="success" size="small" @click="openAdjust(row)">调整库存</el-button>
              <el-button type="primary" size="small" @click="openEdit(row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="mobile-list" v-loading="loading">
        <div v-for="row in tableData" :key="row.id" class="mobile-record-card">
          <h3 class="mobile-card-title">{{ getProductName(row.productId) || '未命名商品' }}</h3>
          <div class="mobile-card-row"><span>颜色</span><strong>{{ row.color || '-' }}</strong></div>
          <div class="mobile-card-row"><span>尺码</span><strong>{{ row.size || '-' }}</strong></div>
          <div class="mobile-card-row"><span>库存</span><strong :class="{ low: row.stock < 10 }">{{ row.stock ?? 0 }}</strong></div>
          <div class="mobile-card-row"><span>进价</span><strong>{{ row.costPrice ?? 0 }} 元</strong></div>
          <div class="mobile-card-row"><span>售价</span><strong>{{ row.salePrice ?? 0 }} 元</strong></div>
          <div class="mobile-card-actions three-actions">
            <el-button type="success" @click="openAdjust(row)">调库存</el-button>
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

    <!-- 弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="520px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="所属商品" prop="productId">
          <el-select v-model="form.productId" style="width:100%" placeholder="请选择商品" filterable>
            <el-option
              v-for="p in productList"
              :key="p.id"
              :label="formatProductLabel(p)"
              :value="p.id"
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="颜色" prop="color">
              <el-input v-model="form.color" maxlength="20" show-word-limit placeholder="例如：红色、蓝色" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="尺码" prop="size">
              <el-input v-model="form.size" maxlength="20" show-word-limit placeholder="例如：110、120、XL、3-6岁" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="库存数量" prop="stock">
          <el-input-number v-model="form.stock" :min="0" :max="999999" style="width:100%" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="进价(元)" prop="costPrice">
              <el-input-number v-model="form.costPrice" :min="0" :max="999999" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="售价(元)" prop="salePrice">
              <el-input-number v-model="form.salePrice" :min="0" :max="999999" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="调整库存" v-model="adjustDialogVisible" width="480px" @close="resetAdjustForm">
      <el-form ref="adjustFormRef" :model="adjustForm" :rules="adjustRules" label-width="100px">
        <el-form-item label="当前库存">
          <el-input-number v-model="adjustForm.currentStock" :min="0" disabled style="width:100%" />
        </el-form-item>
        <el-form-item label="调整后库存" prop="newStock">
          <el-input-number v-model="adjustForm.newStock" :min="0" :max="999999" :step="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="调整原因" prop="reason">
          <el-input v-model="adjustForm.reason" type="textarea" :rows="3" maxlength="100" show-word-limit placeholder="请输入盘点调整原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="adjustDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="adjustSaving" @click="submitAdjust">提交</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSkuList, createSku, updateSku, deleteSku, adjustSkuStock } from '@/api/sku'
import { getProductList } from '@/api/product'

const loading = ref(false)
const saving = ref(false)
const adjustSaving = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)
const adjustDialogVisible = ref(false)
const adjustFormRef = ref(null)
const productList = ref([])

const searchForm = reactive({ pageNum: 1, pageSize: 10, color: '', size: '' })
const form = reactive({
  productId: null, color: '', size: '', stock: 0, costPrice: 0, salePrice: 0
})
const notPureNumberPattern = /^(?!\d+$).+$/
const sizePattern = /^[\u4e00-\u9fa5A-Za-z0-9-]+$/
const rules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  color: [
    { required: true, message: '请输入颜色', trigger: 'blur' },
    { min: 1, max: 20, message: '颜色长度必须为1~20个字符', trigger: 'blur' },
    { pattern: notPureNumberPattern, message: '颜色不能是纯数字', trigger: 'blur' }
  ],
  size: [
    { required: true, message: '请输入尺码', trigger: 'blur' },
    { min: 1, max: 20, message: '尺码长度必须为1~20个字符', trigger: 'blur' },
    { pattern: sizePattern, message: '尺码只能包含中文、英文、数字和横杠', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存数量', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存不能小于0', trigger: 'blur' },
    { type: 'number', max: 999999, message: '库存数量不能超过999999', trigger: 'blur' }
  ],
  costPrice: [
    { required: true, message: '请输入进价', trigger: 'blur' },
    { type: 'number', min: 0, message: '进价不能小于0', trigger: 'blur' },
    { type: 'number', max: 999999, message: '进价不能超过999999', trigger: 'blur' }
  ],
  salePrice: [
    { required: true, message: '请输入售价', trigger: 'blur' },
    { type: 'number', min: 0, message: '售价不能小于0', trigger: 'blur' },
    { type: 'number', max: 999999, message: '售价不能超过999999', trigger: 'blur' }
  ]
}
const adjustForm = reactive({
  skuId: null,
  currentStock: 0,
  newStock: 0,
  reason: ''
})
const adjustRules = {
  newStock: [
    { required: true, message: '请输入调整后库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '调整后库存不能小于0', trigger: 'blur' },
    { type: 'number', max: 999999, message: '库存数量不能超过999999', trigger: 'blur' }
  ],
  reason: [{ required: true, message: '请输入调整原因', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getSkuList(searchForm)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

async function loadProducts() {
  const res = await getProductList({ pageNum: 1, pageSize: 200 })
  productList.value = res.data.records
}

function normalizeText(value) {
  if (value === null || value === undefined) return ''
  const text = String(value).trim()
  if (!text || text.toLowerCase() === 'null' || text.toLowerCase() === 'undefined') return ''
  return text
}

function firstText(...values) {
  for (const value of values) {
    const text = normalizeText(value)
    if (text) return text
  }
  return ''
}

function formatProductLabel(item) {
  const name = normalizeText(item?.name) || '未命名商品'
  const supplierName = normalizeText(item?.supplierName) || '未设置'
  const productCode = firstText(item?.productCode, item?.goodsNo, item?.productNo, item?.code)
  const parts = [`供货商：${supplierName}`]

  if (productCode) {
    parts.push(`货号：${productCode}`)
  }

  return `${name}（${parts.join('，')}）`
}

function getProductName(id) {
  const product = productList.value.find(p => p.id === id)
  return product ? formatProductLabel(product) : ''
}

function handleSearch() { searchForm.pageNum = 1; fetchData() }
function resetSearch() { searchForm.color = ''; searchForm.size = ''; handleSearch() }

function openAdd() {
  isEdit.value = false; dialogTitle.value = '新增规格'
  dialogVisible.value = true
  loadProducts()
}

function openEdit(row) {
  isEdit.value = true; dialogTitle.value = '编辑规格'
  Object.assign(form, row)
  dialogVisible.value = true
  loadProducts()
}

function openAdjust(row) {
  Object.assign(adjustForm, {
    skuId: row.id,
    currentStock: Number(row.stock || 0),
    newStock: Number(row.stock || 0),
    reason: ''
  })
  adjustDialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    productId: null, color: '', size: '', stock: 0, costPrice: 0, salePrice: 0
  })
}

function resetAdjustForm() {
  adjustFormRef.value?.resetFields()
  Object.assign(adjustForm, {
    skuId: null,
    currentStock: 0,
    newStock: 0,
    reason: ''
  })
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value) { await updateSku(form.id, form); ElMessage.success('修改成功') }
    else { await createSku(form); ElMessage.success('新增成功') }
    dialogVisible.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function submitAdjust() {
  const valid = await adjustFormRef.value.validate().catch(() => false)
  if (!valid) return
  adjustSaving.value = true
  try {
    await adjustSkuStock({
      skuId: adjustForm.skuId,
      newStock: adjustForm.newStock,
      reason: adjustForm.reason
    })
    ElMessage.success('库存调整成功')
    adjustDialogVisible.value = false
    fetchData()
  } finally {
    adjustSaving.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定删除该规格吗？', '提示', { type: 'warning' })
    .then(async () => { await deleteSku(row.id); ElMessage.success('已删除'); fetchData() })
    .catch(() => {})
}

onMounted(() => { fetchData(); loadProducts() })
</script>

<style scoped>
.page { max-width: 1100px; margin: 0 auto; }
.search-card { margin-bottom: 12px; }
.table-card { margin-bottom: 12px; }
.toolbar { margin-bottom: 12px; }
.table-wrap { overflow-x: auto; }

.low {
  color: #f56c6c;
}

@media screen and (max-width: 768px) {
  .page {
    max-width: none;
    width: 100%;
  }

  .toolbar .el-button {
    width: 100%;
  }

  .three-actions {
    gap: 6px;
  }
}
</style>

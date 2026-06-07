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
      <div class="table-wrap">
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
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="openEdit(row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
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
          <el-select v-model="form.productId" style="width:100%" placeholder="请选择商品">
            <el-option
              v-for="p in productList"
              :key="p.id"
              :label="p.name + '（货号：' + p.productCode + '）'"
              :value="p.id"
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="颜色" prop="color">
              <el-input v-model="form.color" placeholder="例如：红色、蓝色" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="尺码" prop="size">
              <el-input v-model="form.size" placeholder="例如：110、120" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="库存数量" prop="stock">
          <el-input-number v-model="form.stock" :min="0" style="width:100%" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="进价(元)" prop="costPrice">
              <el-input-number v-model="form.costPrice" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="售价(元)" prop="salePrice">
              <el-input-number v-model="form.salePrice" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSkuList, createSku, updateSku, deleteSku } from '@/api/sku'
import { getProductList } from '@/api/product'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)
const productList = ref([])

const searchForm = reactive({ pageNum: 1, pageSize: 10, color: '', size: '' })
const form = reactive({
  productId: null, color: '', size: '', stock: 0, costPrice: 0, salePrice: 0
})
const rules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  color: [{ required: true, message: '请输入颜色', trigger: 'blur' }],
  size: [{ required: true, message: '请输入尺码', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存数量', trigger: 'blur' }],
  costPrice: [{ required: true, message: '请输入进价', trigger: 'blur' }],
  salePrice: [{ required: true, message: '请输入售价', trigger: 'blur' }]
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

function getProductName(id) { return productList.value.find(p => p.id === id)?.name || '' }

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

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    productId: null, color: '', size: '', stock: 0, costPrice: 0, salePrice: 0
  })
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (isEdit.value) { await updateSku(form.id, form); ElMessage.success('修改成功') }
  else { await createSku(form); ElMessage.success('新增成功') }
  dialogVisible.value = false
  fetchData()
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
</style>

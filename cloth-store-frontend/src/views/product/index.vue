<template>
  <div class="page">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.name" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="货号">
          <el-input v-model="searchForm.productCode" placeholder="请输入" clearable />
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
        <el-button type="primary" @click="openAdd">新增商品</el-button>
      </div>
      <div class="table-wrap">
        <el-table :data="tableData" stripe border v-loading="loading">
          <el-table-column prop="id" label="编号" width="80" />
          <el-table-column prop="name" label="商品名称" min-width="140" />
          <el-table-column prop="productCode" label="货号" width="130" />
          <el-table-column label="商品分类" width="110">
            <template #default="{ row }">{{ getCategoryName(row.categoryId) }}</template>
          </el-table-column>
          <el-table-column label="进货商" width="130">
            <template #default="{ row }">{{ getSupplierName(row.supplierId) }}</template>
          </el-table-column>
          <el-table-column label="性别" width="80">
            <template #default="{ row }">{{ genderMap[row.gender] }}</template>
          </el-table-column>
          <el-table-column prop="suitableAge" label="适合年龄" width="110" />
          <el-table-column prop="season" label="季节" width="80" />
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
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="货号" prop="productCode">
          <el-input v-model="form.productCode" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="商品分类" prop="categoryId">
              <el-select v-model="form.categoryId" style="width:100%">
                <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="进货商" prop="supplierId">
              <el-select v-model="form.supplierId" style="width:100%">
                <el-option v-for="s in supplierList" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="form.gender" style="width:100%">
                <el-option label="通用" :value="0" />
                <el-option label="男童" :value="1" />
                <el-option label="女童" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="季节">
              <el-select v-model="form.season" style="width:100%">
                <el-option label="春" value="春" />
                <el-option label="夏" value="夏" />
                <el-option label="秋" value="秋" />
                <el-option label="冬" value="冬" />
                <el-option label="四季通用" value="四季通用" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="适合年龄">
          <el-input v-model="form.suitableAge" placeholder="例如：3-5岁" />
        </el-form-item>
        <el-form-item label="图片地址">
          <el-input v-model="form.imageUrl" placeholder="可留空" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
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
import { getProductList, createProduct, updateProduct, deleteProduct } from '@/api/product'
import { getCategoryList } from '@/api/category'
import { getSupplierList } from '@/api/supplier'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)
const categoryList = ref([])
const supplierList = ref([])

const genderMap = { 0: '通用', 1: '男童', 2: '女童' }

const searchForm = reactive({ pageNum: 1, pageSize: 10, name: '', productCode: '' })
const form = reactive({
  name: '', productCode: '', categoryId: null, supplierId: null,
  gender: 0, suitableAge: '', season: '夏', imageUrl: '', remark: ''
})
const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  productCode: [{ required: true, message: '请输入货号', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  supplierId: [{ required: true, message: '请选择进货商', trigger: 'change' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getProductList(searchForm)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

async function loadOptions() {
  const [catRes, supRes] = await Promise.all([
    getCategoryList({ pageNum: 1, pageSize: 200 }),
    getSupplierList({ pageNum: 1, pageSize: 200 })
  ])
  categoryList.value = catRes.data.records
  supplierList.value = supRes.data.records
}

function getCategoryName(id) { return categoryList.value.find(c => c.id === id)?.name || '' }
function getSupplierName(id) { return supplierList.value.find(s => s.id === id)?.name || '' }

function handleSearch() { searchForm.pageNum = 1; fetchData() }
function resetSearch() { searchForm.name = ''; searchForm.productCode = ''; handleSearch() }

function openAdd() {
  isEdit.value = false; dialogTitle.value = '新增商品'
  dialogVisible.value = true
  loadOptions()
}

function openEdit(row) {
  isEdit.value = true; dialogTitle.value = '编辑商品'
  Object.assign(form, row)
  dialogVisible.value = true
  loadOptions()
}

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    name: '', productCode: '', categoryId: null, supplierId: null,
    gender: 0, suitableAge: '', season: '夏', imageUrl: '', remark: ''
  })
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (isEdit.value) { await updateProduct(form.id, form); ElMessage.success('修改成功') }
  else { await createProduct(form); ElMessage.success('新增成功') }
  dialogVisible.value = false
  fetchData()
}

function handleDelete(row) {
  ElMessageBox.confirm('确定删除该商品吗？', '提示', { type: 'warning' })
    .then(async () => { await deleteProduct(row.id); ElMessage.success('已删除'); fetchData() })
    .catch(() => {})
}

onMounted(() => { fetchData(); loadOptions() })
</script>

<style scoped>
.page { max-width: 1100px; margin: 0 auto; }
.search-card { margin-bottom: 12px; }
.table-card { margin-bottom: 12px; }
.toolbar { margin-bottom: 12px; }
.table-wrap { overflow-x: auto; }
</style>

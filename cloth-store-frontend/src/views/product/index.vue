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
      <div class="table-wrap desktop-table">
        <el-table :data="tableData" stripe border v-loading="loading">
          <el-table-column prop="id" label="编号" width="80" />
          <el-table-column label="图片" width="92">
            <template #default="{ row }">
              <el-image
                v-if="row.imageUrl"
                class="product-thumb"
                :src="row.imageUrl"
                :preview-src-list="[row.imageUrl]"
                preview-teleported
                fit="cover"
              />
              <span v-else class="empty-image">暂无</span>
            </template>
          </el-table-column>
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
      <div class="mobile-list" v-loading="loading">
        <div v-for="row in tableData" :key="row.id" class="mobile-record-card product-mobile-card">
          <div class="mobile-product-head">
            <el-image v-if="row.imageUrl" class="mobile-product-image" :src="row.imageUrl" fit="cover" />
            <div v-else class="mobile-product-image empty">暂无</div>
            <div class="mobile-product-main">
              <h3 class="mobile-card-title">{{ row.name }}</h3>
              <div class="mobile-card-row"><span>货号</span><strong>{{ row.productCode || '-' }}</strong></div>
            </div>
          </div>
          <div class="mobile-card-row"><span>分类</span><strong>{{ getCategoryName(row.categoryId) || '-' }}</strong></div>
          <div class="mobile-card-row"><span>进货商</span><strong>{{ getSupplierName(row.supplierId) || '-' }}</strong></div>
          <div class="mobile-card-row"><span>性别</span><strong>{{ genderMap[row.gender] || '-' }}</strong></div>
          <div class="mobile-card-row"><span>季节</span><strong>{{ row.season || '-' }}</strong></div>
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

    <!-- 弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="货号" prop="productCode">
          <el-input v-model="form.productCode" maxlength="50" show-word-limit />
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
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" style="width:100%">
                <el-option label="通用" :value="0" />
                <el-option label="男童" :value="1" />
                <el-option label="女童" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="季节" prop="season">
              <el-select v-model="form.season" style="width:100%">
                <el-option label="春季" value="春季" />
                <el-option label="夏季" value="夏季" />
                <el-option label="秋季" value="秋季" />
                <el-option label="冬季" value="冬季" />
                <el-option label="四季" value="四季" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="适合年龄" prop="suitableAge">
          <el-input v-model="form.suitableAge" maxlength="30" show-word-limit placeholder="例如：3-5岁" />
        </el-form-item>
        <el-form-item label="商品图片">
          <div class="upload-field">
            <el-upload
              class="product-uploader"
              action="/api/file/upload"
              name="file"
              :headers="uploadHeaders"
              :show-file-list="false"
              accept="image/*"
              :before-upload="beforeImageUpload"
              :on-success="handleImageSuccess"
              :on-error="handleImageError"
            >
              <img v-if="form.imageUrl" :src="form.imageUrl" class="upload-preview" alt="商品图片" />
              <div v-else class="upload-placeholder">
                <el-icon><Plus /></el-icon>
                <span>上传图片</span>
              </div>
            </el-upload>
            <el-input v-model="form.imageUrl" placeholder="上传后自动填入图片地址，也可手动填写" />
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductList, createProduct, updateProduct, deleteProduct } from '@/api/product'
import { getCategoryList } from '@/api/category'
import { getSupplierList } from '@/api/supplier'
import { getToken } from '@/utils/auth'

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
const uploadHeaders = computed(() => {
  const token = getToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const searchForm = reactive({ pageNum: 1, pageSize: 10, name: '', productCode: '' })
const form = reactive({
  name: '', productCode: '', categoryId: null, supplierId: null,
  gender: 0, suitableAge: '', season: '夏季', imageUrl: '', remark: ''
})
const seasonPattern = /^(春季|夏季|秋季|冬季|四季)$/
const rules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { max: 50, message: '商品名称长度必须为1~50个字符', trigger: 'blur' }
  ],
  productCode: [
    { max: 50, message: '货号长度不能超过50个字符', trigger: 'blur' }
  ],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  supplierId: [{ required: true, message: '请选择进货商', trigger: 'change' }],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' },
    { type: 'number', min: 0, max: 2, message: '性别只能是男童、女童、通用', trigger: 'change' }
  ],
  season: [
    { required: true, message: '请选择季节', trigger: 'change' },
    { pattern: seasonPattern, message: '季节只能是春季、夏季、秋季、冬季、四季', trigger: 'change' }
  ],
  suitableAge: [{ max: 30, message: '适合年龄长度不能超过30个字符', trigger: 'blur' }]
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
    gender: 0, suitableAge: '', season: '夏季', imageUrl: '', remark: ''
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

function beforeImageUpload(file) {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/webp']
  const allowedExtensions = ['jpg', 'jpeg', 'png', 'webp']
  const extension = file.name?.split('.').pop()?.toLowerCase()
  const validType = allowedTypes.includes(file.type) || allowedExtensions.includes(extension)
  if (!validType) {
    ElMessage.error('仅支持jpg、jpeg、png、webp格式图片')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  return true
}

function handleImageSuccess(response) {
  if (response?.code !== 200) {
    ElMessage.error(response?.message || '上传失败')
    return
  }
  form.imageUrl = response.data
  ElMessage.success('图片上传成功')
}

function handleImageError(error) {
  ElMessage.error(error?.message || '上传失败')
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
.product-thumb {
  width: 54px;
  height: 54px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}
.empty-image {
  color: #909399;
  font-size: 13px;
}
.upload-field {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}
.product-uploader {
  width: 130px;
}
.product-uploader :deep(.el-upload) {
  width: 130px;
  height: 130px;
  overflow: hidden;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  background: #f8fafc;
}
.product-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}
.upload-preview {
  display: block;
  width: 130px;
  height: 130px;
  object-fit: cover;
}
.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 130px;
  height: 130px;
  color: #64748b;
  font-size: 14px;
}
.upload-placeholder .el-icon {
  font-size: 28px;
}

@media screen and (max-width: 768px) {
  .page {
    max-width: none;
    width: 100%;
  }

  .search-card,
  .table-card {
    margin-bottom: 10px;
  }

  .toolbar .el-button {
    width: 100%;
  }

  .mobile-product-head {
    display: flex;
    gap: 12px;
    margin-bottom: 8px;
  }

  .mobile-product-image {
    flex: 0 0 auto;
    width: 72px;
    height: 72px;
    border-radius: 8px;
    border: 1px solid #e5e7eb;
  }

  .mobile-product-image.empty {
    display: flex;
    align-items: center;
    justify-content: center;
    color: #94a3b8;
    background: #f8fafc;
    font-size: 14px;
  }

  .mobile-product-main {
    min-width: 0;
    flex: 1;
  }

  .product-uploader,
  .product-uploader :deep(.el-upload),
  .upload-preview,
  .upload-placeholder {
    width: 116px;
    height: 116px;
  }
}
</style>

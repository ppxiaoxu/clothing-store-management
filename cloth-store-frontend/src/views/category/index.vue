<template>
  <div class="page">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="分类名称">
          <el-input v-model="searchForm.name" placeholder="请输入" clearable />
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
        <el-button type="primary" @click="openAdd">新增分类</el-button>
      </div>
      <div class="table-wrap desktop-table">
        <el-table :data="tableData" stripe border v-loading="loading">
          <el-table-column prop="id" label="编号" width="90" />
          <el-table-column prop="name" label="分类名称" min-width="160" />
          <el-table-column label="上级分类" width="140">
            <template #default="{ row }">{{ getParentName(row.parentId) }}</template>
          </el-table-column>
          <el-table-column prop="sortOrder" label="显示顺序" width="110" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="openEdit(row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="mobile-list" v-loading="loading">
        <div v-for="row in tableData" :key="row.id" class="mobile-record-card">
          <h3 class="mobile-card-title">{{ row.name }}</h3>
          <div class="mobile-card-row"><span>上级分类</span><strong>{{ getParentName(row.parentId) }}</strong></div>
          <div class="mobile-card-row"><span>显示顺序</span><strong>{{ row.sortOrder ?? 0 }}</strong></div>
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
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="520px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" maxlength="30" show-word-limit placeholder="例如：上衣、裤子" />
        </el-form-item>
        <el-form-item label="上级分类">
          <el-select v-model="form.parentId" style="width:100%">
            <el-option label="顶级分类" :value="0" />
            <el-option v-for="c in categoryOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="显示顺序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" :precision="0" style="width:100%" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategoryList, createCategory, updateCategory, deleteCategory } from '@/api/category'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)
const categoryOptions = ref([])

const searchForm = reactive({ pageNum: 1, pageSize: 10, name: '' })
const form = reactive({ name: '', parentId: 0, sortOrder: 0 })
const notPureNumberPattern = /^(?!\d+$).+$/
const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { max: 30, message: '分类名称长度必须为1~30个字符', trigger: 'blur' },
    { pattern: notPureNumberPattern, message: '分类名称不能是纯数字', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入显示顺序', trigger: 'blur' },
    { type: 'number', min: 0, max: 9999, message: '显示顺序不能超过9999', trigger: 'blur' }
  ]
}

function getParentName(pid) {
  if (pid === 0) return '顶级分类'
  return categoryOptions.value.find(c => c.id === pid)?.name || ''
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getCategoryList(searchForm)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

async function loadCategoryOptions() {
  const res = await getCategoryList({ pageNum: 1, pageSize: 200 })
  categoryOptions.value = res.data.records
}

function handleSearch() { searchForm.pageNum = 1; fetchData() }
function resetSearch() { searchForm.name = ''; handleSearch() }

function openAdd() {
  isEdit.value = false; dialogTitle.value = '新增分类'
  dialogVisible.value = true
  loadCategoryOptions()
}

function openEdit(row) {
  isEdit.value = true; dialogTitle.value = '编辑分类'
  Object.assign(form, row)
  dialogVisible.value = true
  loadCategoryOptions().then(() => {
    categoryOptions.value = categoryOptions.value.filter(c => c.id !== row.id)
  })
}

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, { name: '', parentId: 0, sortOrder: 0 })
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value) { await updateCategory(form.id, form); ElMessage.success('修改成功') }
    else { await createCategory(form); ElMessage.success('新增成功') }
    dialogVisible.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确定删除该分类吗？', '提示', { type: 'warning' })
    .then(async () => { await deleteCategory(row.id); ElMessage.success('已删除'); fetchData() })
    .catch(() => {})
}

onMounted(() => {
  fetchData()
  loadCategoryOptions()
})
</script>

<style scoped>
.page { max-width: 1100px; margin: 0 auto; }
.search-card { margin-bottom: 12px; }
.table-card { margin-bottom: 12px; }
.toolbar { margin-bottom: 12px; }
.table-wrap { overflow-x: auto; }

@media screen and (max-width: 768px) {
  .page {
    max-width: none;
    width: 100%;
  }

  .toolbar .el-button {
    width: 100%;
  }
}
</style>

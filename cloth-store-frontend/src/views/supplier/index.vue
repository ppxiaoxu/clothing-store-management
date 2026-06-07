<template>
  <div class="page">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="进货商名称">
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
        <el-button type="primary" @click="openAdd">新增进货商</el-button>
      </div>
      <div class="table-wrap">
        <el-table :data="tableData" stripe border v-loading="loading">
          <el-table-column prop="id" label="编号" width="90" />
          <el-table-column prop="name" label="进货商名称" min-width="160" />
          <el-table-column prop="contactPerson" label="联系人" width="120" />
          <el-table-column prop="phone" label="电话" width="150" />
          <el-table-column prop="address" label="地址" show-overflow-tooltip />
          <el-table-column prop="remark" label="备注" show-overflow-tooltip min-width="120" />
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
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="560px" @close="resetForm">
      <el-form ref="formRef" class="supplier-form" :model="form" :rules="rules" label-width="130px">
        <el-form-item label="进货商名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：杭州童装厂" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactPerson" placeholder="联系人姓名" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="手机或座机号" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="省、市、区、详细地址" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="其他说明" />
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
import { getSupplierList, createSupplier, updateSupplier, deleteSupplier } from '@/api/supplier'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref(null)

const searchForm = reactive({ pageNum: 1, pageSize: 10, name: '' })
const form = reactive({ name: '', contactPerson: '', phone: '', address: '', remark: '' })
const rules = { name: [{ required: true, message: '请输入进货商名称', trigger: 'blur' }] }

async function fetchData() {
  loading.value = true
  try {
    const res = await getSupplierList(searchForm)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

function handleSearch() { searchForm.pageNum = 1; fetchData() }
function resetSearch() { searchForm.name = ''; handleSearch() }

function openAdd() {
  isEdit.value = false; dialogTitle.value = '新增进货商'
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true; dialogTitle.value = '编辑进货商'
  Object.assign(form, row)
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, { name: '', contactPerson: '', phone: '', address: '', remark: '' })
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (isEdit.value) { await updateSupplier(form.id, form); ElMessage.success('修改成功') }
  else { await createSupplier(form); ElMessage.success('新增成功') }
  dialogVisible.value = false
  fetchData()
}

function handleDelete(row) {
  ElMessageBox.confirm('确定删除该进货商吗？', '提示', { type: 'warning' })
    .then(async () => { await deleteSupplier(row.id); ElMessage.success('已删除'); fetchData() })
    .catch(() => {})
}

onMounted(() => fetchData())
</script>

<style scoped>
.page { max-width: 1100px; margin: 0 auto; }
.search-card { margin-bottom: 12px; }
.table-card { margin-bottom: 12px; }
.toolbar { margin-bottom: 12px; }
.table-wrap { overflow-x: auto; }
.supplier-form :deep(.el-form-item__label) {
  white-space: nowrap;
}
</style>

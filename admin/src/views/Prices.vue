<template>
  <div class="prices-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>价格管理</span>
          <div>
            <el-button type="primary" @click="showCreateDialog">
              <el-icon><Plus /></el-icon>
              新增价格
            </el-button>
            <el-button type="primary" @click="refreshList">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索配置名称"
          style="width: 300px"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>

      <!-- 价格列表 -->
      <el-table
        v-loading="loading"
        :data="priceList"
        style="width: 100%"
      >
        <el-table-column prop="configName" label="配置名称" width="150" />
        
        <el-table-column prop="configKey" label="配置键" width="150" />
        
        <el-table-column prop="price" label="价格" width="120">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        
        <el-table-column prop="unit" label="单位" width="100" />
        
        <el-table-column prop="description" label="描述" min-width="200" />
        
        <el-table-column prop="isActive" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'danger'" size="small">
              {{ row.isActive ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="sortOrder" label="排序" width="80" />
        
        <el-table-column prop="updateTime" label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.updateTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="editPrice(row)"
            >
              编辑
            </el-button>
            <el-button
              :type="row.isActive ? 'warning' : 'success'"
              size="small"
              @click="togglePrice(row)"
            >
              {{ row.isActive ? '禁用' : '启用' }}
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="deletePrice(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑价格配置' : '新增价格配置'"
      width="600px"
    >
      <el-form
        ref="priceFormRef"
        :model="priceForm"
        :rules="priceRules"
        label-width="100px"
      >
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="priceForm.configName" placeholder="请输入配置名称" />
        </el-form-item>
        
        <el-form-item label="配置键" prop="configKey">
          <el-input 
            v-model="priceForm.configKey" 
            placeholder="请输入配置键（英文）"
            :disabled="isEdit"
          />
        </el-form-item>
        
        <el-form-item label="价格" prop="price">
          <el-input-number
            v-model="priceForm.price"
            :precision="2"
            :step="0.01"
            :min="0"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="单位" prop="unit">
          <el-input v-model="priceForm.unit" placeholder="如：元/页" />
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input
            v-model="priceForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述信息"
          />
        </el-form-item>
        
        <el-form-item label="排序">
          <el-input-number
            v-model="priceForm.sortOrder"
            :min="0"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-switch
            v-model="priceForm.isActive"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'

const loading = ref(false)
const searchQuery = ref('')
const priceList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const priceFormRef = ref()

const priceForm = reactive({
  id: null,
  configName: '',
  configKey: '',
  price: 0,
  unit: '',
  description: '',
  sortOrder: 0,
  isActive: true
})

const priceRules = {
  configName: [
    { required: true, message: '请输入配置名称', trigger: 'blur' }
  ],
  configKey: [
    { required: true, message: '请输入配置键', trigger: 'blur' },
    { pattern: /^[a-zA-Z_][a-zA-Z0-9_]*$/, message: '配置键只能包含字母、数字和下划线，且以字母或下划线开头', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' }
  ],
  unit: [
    { required: true, message: '请输入单位', trigger: 'blur' }
  ]
}

onMounted(() => {
  loadPriceList()
})

const loadPriceList = async () => {
  loading.value = true
  try {
    const response = await api.get('/price/admin/list')
    if (response.data.code === 200) {
      priceList.value = response.data.data || []
    } else {
      ElMessage.error(response.data.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error('加载价格列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  loading.value = true
  try {
    const response = await api.get('/price/admin/search', {
      params: { keyword: searchQuery.value }
    })
    if (response.data.code === 200) {
      priceList.value = response.data.data || []
    } else {
      ElMessage.error(response.data.message || '搜索失败')
    }
  } catch (error) {
    ElMessage.error('搜索失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const refreshList = () => {
  searchQuery.value = ''
  loadPriceList()
}

const showCreateDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const editPrice = (price) => {
  isEdit.value = true
  Object.assign(priceForm, price)
  dialogVisible.value = true
}

const resetForm = () => {
  Object.assign(priceForm, {
    id: null,
    configName: '',
    configKey: '',
    price: 0,
    unit: '',
    description: '',
    sortOrder: 0,
    isActive: true
  })
  if (priceFormRef.value) {
    priceFormRef.value.clearValidate()
  }
}

const submitForm = async () => {
  if (!priceFormRef.value) return
  
  await priceFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const url = isEdit.value ? '/price/admin/update' : '/price/admin/create'
        const method = isEdit.value ? 'put' : 'post'
        
        const response = await api[method](url, priceForm)
        if (response.data.code === 200) {
          ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
          dialogVisible.value = false
          loadPriceList()
        } else {
          ElMessage.error(response.data.message || '操作失败')
        }
      } catch (error) {
        ElMessage.error('操作失败')
        console.error(error)
      }
    }
  })
}

const togglePrice = async (price) => {
  try {
    const action = price.isActive ? '禁用' : '启用'
    await ElMessageBox.confirm(
      `确定要${action}价格配置 "${price.configName}" 吗？`,
      `确认${action}`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await api.put(`/price/admin/${price.id}/toggle`, {
      isActive: !price.isActive
    })
    
    if (response.data.code === 200) {
      ElMessage.success(response.data.message)
      loadPriceList()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const deletePrice = async (price) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除价格配置 "${price.configName}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await api.delete(`/price/admin/${price.id}`)
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      loadPriceList()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const formatTime = (time) => {
  return new Date(time).toLocaleString()
}
</script>

<style scoped>
.prices-page {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-bar {
  margin-bottom: 20px;
}

.dialog-footer {
  text-align: right;
}
</style>

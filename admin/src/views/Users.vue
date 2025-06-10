<template>
  <div class="users-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="refreshList">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchQuery"
              placeholder="搜索用户名或昵称"
              @keyup.enter="handleSearch"
            />
          </el-col>
          <el-col :span="4">
            <el-select v-model="roleFilter" placeholder="用户角色" clearable>
              <el-option label="普通用户" value="USER" />
              <el-option label="管理员" value="ADMIN" />
              <el-option label="超级管理员" value="SUPER_ADMIN" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select v-model="statusFilter" placeholder="用户状态" clearable>
              <el-option label="正常" :value="0" />
              <el-option label="禁用" :value="1" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 用户列表 -->
      <el-table
        v-loading="loading"
        :data="userList"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column prop="username" label="用户名" width="150" />
        
        <el-table-column prop="nickname" label="昵称" width="150" />
        
        <el-table-column prop="phone" label="手机号" width="130" />
        
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)" size="small">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
              {{ row.status === 0 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="warning"
              size="small"
              @click="disableUser(row)"
            >
              禁用
            </el-button>
            <el-button
              v-else
              type="success"
              size="small"
              @click="enableUser(row)"
            >
              启用
            </el-button>
            <el-button
              type="primary"
              size="small"
              @click="resetPassword(row)"
            >
              重置密码
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'

const loading = ref(false)
const searchQuery = ref('')
const roleFilter = ref('')
const statusFilter = ref('')
const userList = ref([])

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

onMounted(() => {
  loadUserList()
})

const loadUserList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }
    
    if (searchQuery.value) {
      params.search = searchQuery.value
    }
    
    if (roleFilter.value) {
      params.role = roleFilter.value
    }
    
    if (statusFilter.value !== '') {
      params.status = statusFilter.value
    }
    
    // 这里需要添加用户列表API
    // const response = await api.get('/user/list', { params })
    
    // 模拟数据
    const mockData = {
      code: 200,
      data: {
        content: [
          {
            id: 1,
            username: 'admin',
            nickname: '系统管理员',
            phone: '13800138000',
            role: 'ADMIN',
            status: 0,
            createTime: new Date().toISOString()
          },
          {
            id: 2,
            username: 'superadmin',
            nickname: '超级管理员',
            phone: '13800138001',
            role: 'SUPER_ADMIN',
            status: 0,
            createTime: new Date().toISOString()
          }
        ],
        totalElements: 2
      }
    }
    
    userList.value = mockData.data.content || []
    pagination.total = mockData.data.totalElements || 0
  } catch (error) {
    ElMessage.error('加载用户列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadUserList()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadUserList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadUserList()
}

const refreshList = () => {
  loadUserList()
}

const disableUser = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要禁用用户 "${user.username}" 吗？`,
      '确认禁用',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 这里需要添加禁用用户的API
    ElMessage.success('用户禁用成功')
    loadUserList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const enableUser = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要启用用户 "${user.username}" 吗？`,
      '确认启用',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 这里需要添加启用用户的API
    ElMessage.success('用户启用成功')
    loadUserList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const resetPassword = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户 "${user.username}" 的密码吗？重置后密码为：123456`,
      '确认重置密码',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 这里需要添加重置密码的API
    ElMessage.success('密码重置成功，新密码为：123456')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const formatTime = (time) => {
  return new Date(time).toLocaleString()
}

const getRoleType = (role) => {
  const types = {
    'USER': '',
    'ADMIN': 'warning',
    'SUPER_ADMIN': 'danger'
  }
  return types[role] || ''
}

const getRoleText = (role) => {
  const texts = {
    'USER': '普通用户',
    'ADMIN': '管理员',
    'SUPER_ADMIN': '超级管理员'
  }
  return texts[role] || '未知'
}
</script>

<style scoped>
.users-page {
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

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>

<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon files">
              <el-icon size="40"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalFiles }}</div>
              <div class="stat-label">总文件数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon orders">
              <el-icon size="40"><List /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalOrders }}</div>
              <div class="stat-label">总订单数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon users">
              <el-icon size="40"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalUsers }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon revenue">
              <el-icon size="40"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">¥{{ stats.totalRevenue }}</div>
              <div class="stat-label">总收入</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近文件 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近上传的文件</span>
              <el-button type="text" @click="$router.push('/files')">查看更多</el-button>
            </div>
          </template>
          <div v-loading="loading.files">
            <div v-if="recentFiles.length === 0" class="empty-data">
              暂无文件
            </div>
            <div v-else>
              <div
                v-for="file in recentFiles"
                :key="file.id"
                class="file-item"
              >
                <div class="file-info">
                  <el-icon><Document /></el-icon>
                  <div class="file-details">
                    <div class="file-name">{{ file.originalName }}</div>
                    <div class="file-meta">{{ formatFileSize(file.fileSize) }} • {{ formatTime(file.createTime) }}</div>
                  </div>
                </div>
                <el-button type="text" @click="downloadFile(file)">下载</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近订单</span>
              <el-button type="text" @click="$router.push('/orders')">查看更多</el-button>
            </div>
          </template>
          <div v-loading="loading.orders">
            <div v-if="recentOrders.length === 0" class="empty-data">
              暂无订单
            </div>
            <div v-else>
              <div
                v-for="order in recentOrders"
                :key="order.id"
                class="order-item"
              >
                <div class="order-info">
                  <div class="order-no">订单号: {{ order.orderNo }}</div>
                  <div class="order-meta">{{ order.fileName }} • ¥{{ order.amount }}</div>
                </div>
                <el-tag :type="getOrderStatusType(order.status)">
                  {{ getOrderStatusText(order.status) }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

const loading = reactive({
  files: false,
  orders: false
})

const stats = reactive({
  totalFiles: 0,
  totalOrders: 0,
  totalUsers: 0,
  totalRevenue: 0
})

const recentFiles = ref([])
const recentOrders = ref([])

onMounted(() => {
  loadDashboardData()
})

const loadDashboardData = async () => {
  await Promise.all([
    loadRecentFiles(),
    loadRecentOrders(),
    loadStats()
  ])
}

const loadRecentFiles = async () => {
  loading.files = true
  try {
    const response = await api.get('/file/list?page=1&size=5')
    if (response.data.code === 200) {
      recentFiles.value = response.data.data.content || []
      stats.totalFiles = response.data.data.totalElements || 0
    }
  } catch (error) {
    console.error('加载文件列表失败:', error)
  } finally {
    loading.files = false
  }
}

const loadRecentOrders = async () => {
  loading.orders = true
  try {
    const response = await api.get('/order/list?page=1&size=5')
    if (response.data.code === 200) {
      recentOrders.value = response.data.data.content || []
      stats.totalOrders = response.data.data.totalElements || 0
    }
  } catch (error) {
    console.error('加载订单列表失败:', error)
  } finally {
    loading.orders = false
  }
}

const loadStats = async () => {
  // 这里可以添加更多统计数据的API调用
  stats.totalUsers = 100 // 示例数据
  stats.totalRevenue = 5000 // 示例数据
}

const downloadFile = async (file) => {
  try {
    const response = await api.get(`/file/download/${file.id}`, {
      responseType: 'blob'
    })
    
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.download = file.originalName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatTime = (time) => {
  return new Date(time).toLocaleString()
}

const getOrderStatusType = (status) => {
  const types = {
    0: 'warning', // 待支付
    1: 'success', // 已支付
    2: 'primary', // 打印中
    3: 'success', // 已完成
    4: 'danger',  // 已取消
    5: 'info'     // 已退款
  }
  return types[status] || 'info'
}

const getOrderStatusText = (status) => {
  const texts = {
    0: '待支付',
    1: '已支付',
    2: '打印中',
    3: '已完成',
    4: '已取消',
    5: '已退款'
  }
  return texts[status] || '未知'
}
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-icon.files {
  background-color: #e3f2fd;
  color: #1976d2;
}

.stat-icon.orders {
  background-color: #f3e5f5;
  color: #7b1fa2;
}

.stat-icon.users {
  background-color: #e8f5e8;
  color: #388e3c;
}

.stat-icon.revenue {
  background-color: #fff3e0;
  color: #f57c00;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.file-item, .order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.file-item:last-child, .order-item:last-child {
  border-bottom: none;
}

.file-info, .order-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.file-details {
  margin-left: 10px;
}

.file-name, .order-no {
  font-weight: 500;
  color: #303133;
}

.file-meta, .order-meta {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.empty-data {
  text-align: center;
  color: #909399;
  padding: 20px 0;
}
</style>

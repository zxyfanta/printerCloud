<template>
  <div class="orders-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ pageTitle }}</span>
          <el-button type="primary" @click="refreshList">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <!-- 搜索栏和排序 -->
      <div class="search-bar">
        <el-row :gutter="20">
          <el-col :span="5">
            <el-input
              v-model="searchQuery"
              placeholder="搜索订单号或验证码"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="4" v-if="showStatusFilter">
            <el-select v-model="statusFilter" placeholder="订单状态" clearable @change="handleSearch">
              <el-option label="待支付" :value="0" />
              <el-option label="已支付" :value="1" />
              <el-option label="打印中" :value="2" />
              <el-option label="已完成" :value="3" />
              <el-option label="已取消" :value="4" />
              <el-option label="已退款" :value="5" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select v-model="sortBy" placeholder="排序方式" @change="handleSearch">
              <el-option label="创建时间(最新)" value="createTime_desc" />
              <el-option label="创建时间(最早)" value="createTime_asc" />
              <el-option label="金额(高到低)" value="amount_desc" />
              <el-option label="金额(低到高)" value="amount_asc" />
              <el-option label="状态" value="status_asc" />
            </el-select>
          </el-col>
          <el-col :span="3">
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </el-col>
          <el-col :span="4">
            <el-radio-group v-model="viewMode" @change="handleViewModeChange">
              <el-radio-button value="card">卡片</el-radio-button>
              <el-radio-button value="table">表格</el-radio-button>
            </el-radio-group>
          </el-col>
        </el-row>
      </div>

      <!-- 订单列表 - 卡片模式 -->
      <div v-if="viewMode === 'card'" v-loading="loading" class="order-cards">
        <div v-if="orderList.length === 0" class="empty-state">
          <el-empty description="暂无订单数据" />
        </div>
        <div v-else class="cards-container">
          <el-card
            v-for="order in orderList"
            :key="order.id"
            class="order-card"
            shadow="hover"
            @click="viewDetails(order)"
          >
            <div class="card-header">
              <div class="order-info">
                <h3 class="order-no">{{ order.orderNo }}</h3>
                <el-tag :type="getStatusType(order.status)" size="small">
                  {{ getStatusText(order.status) }}
                </el-tag>
              </div>
              <div class="order-actions">
                <el-dropdown @command="handleCommand" trigger="click">
                  <el-button type="text" size="small">
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item
                        v-if="order.status === 1 || order.status === 2"
                        :command="{action: 'download', order}"
                        icon="Download"
                      >
                        下载文件
                      </el-dropdown-item>
                      <el-dropdown-item
                        v-if="order.status === 1"
                        :command="{action: 'complete', order}"
                        icon="Check"
                      >
                        完成订单
                      </el-dropdown-item>
                      <el-dropdown-item
                        v-if="order.status === 0"
                        :command="{action: 'cancel', order}"
                        icon="Close"
                      >
                        取消订单
                      </el-dropdown-item>
                      <el-dropdown-item
                        :command="{action: 'detail', order}"
                        icon="View"
                      >
                        查看详情
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>

            <div class="card-content">
              <div class="file-info">
                <div class="file-icon">
                  <el-icon size="24" color="#409EFF">
                    <Document v-if="order.fileType === 'pdf'" />
                    <Picture v-else-if="order.fileType === 'image'" />
                    <Document v-else />
                  </el-icon>
                </div>
                <div class="file-details">
                  <p class="file-name">{{ order.fileName }}</p>
                  <p class="file-meta">{{ order.copies }}份 · {{ order.actualPages }}页 · {{ order.isColor ? '彩色' : '黑白' }} · {{ order.isDoubleSide ? '双面' : '单面' }}</p>
                </div>
              </div>

              <div class="order-meta">
                <div class="meta-item">
                  <span class="label">验证码:</span>
                  <el-tag type="info" size="small">{{ order.verifyCode }}</el-tag>
                </div>
                <div class="meta-item">
                  <span class="label">金额:</span>
                  <span class="amount">¥{{ order.amount }}</span>
                </div>
                <div class="meta-item">
                  <span class="label">创建时间:</span>
                  <span class="time">{{ formatTime(order.createTime) }}</span>
                </div>
                <div v-if="order.remark" class="meta-item remark">
                  <span class="label">备注:</span>
                  <span class="remark-text">{{ order.remark }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 订单列表 - 表格模式 -->
      <el-table
        v-else
        v-loading="loading"
        :data="orderList"
        style="width: 100%"
      >
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="fileName" label="文件名" min-width="200" />
        <el-table-column prop="copies" label="份数" width="80" />
        <el-table-column prop="actualPages" label="页数" width="80" />
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="verifyCode" label="验证码" width="120">
          <template #default="{ row }">
            <el-tag type="info">{{ row.verifyCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" v-if="showStatusColumn">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" type="primary" size="small" @click="completeOrder(row)">
              完成订单
            </el-button>
            <el-button v-if="row.status === 0" type="warning" size="small" @click="cancelOrder(row)">
              取消订单
            </el-button>
            <el-button type="info" size="small" @click="viewDetails(row)">详情</el-button>
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

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="selectedOrder" class="order-detail-content">
        <!-- 这里可以包含详细的订单信息，与原来的Orders.vue相同 -->
        <p>订单详情内容...</p>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'
import webSocketService from '@/utils/websocket'
import { generateMockOrders, getStatusText, getStatusType, shouldUseMockData, mockApiDelay } from '@/utils/mockData'

// Props
const props = defineProps({
  pageTitle: {
    type: String,
    default: '订单管理'
  },
  statusFilter: {
    type: [Number, String],
    default: ''
  },
  showStatusFilter: {
    type: Boolean,
    default: true
  },
  showStatusColumn: {
    type: Boolean,
    default: true
  }
})

const loading = ref(false)
const searchQuery = ref('')
const statusFilter = ref(props.statusFilter)
const sortBy = ref('createTime_desc')
const viewMode = ref('card')
const orderList = ref([])
const detailDialogVisible = ref(false)
const selectedOrder = ref(null)

// 轮询相关
const pollingInterval = ref(null)
const pollingEnabled = ref(true)
const pollingFrequency = ref(5000)
const updateMode = ref('websocket')

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 计算属性
const effectiveStatusFilter = computed(() => {
  return props.statusFilter !== '' ? props.statusFilter : statusFilter.value
})

onMounted(() => {
  // 从localStorage读取视图模式
  const savedViewMode = localStorage.getItem('orderViewMode')
  if (savedViewMode) {
    viewMode.value = savedViewMode
  }
  
  // 从localStorage读取轮询设置
  const savedPollingFrequency = localStorage.getItem('pollingFrequency')
  if (savedPollingFrequency) {
    pollingFrequency.value = parseInt(savedPollingFrequency)
  }
  
  const savedPollingEnabled = localStorage.getItem('pollingEnabled')
  if (savedPollingEnabled !== null) {
    pollingEnabled.value = savedPollingEnabled === 'true'
  }
  
  loadOrderList()
  startPolling()
  
  // 监听WebSocket事件
  window.addEventListener('newOrder', handleNewOrderNotification)
  window.addEventListener('orderUpdate', handleOrderUpdateNotification)
  window.addEventListener('navigateToOrder', handleNavigateToOrder)
  
  // 监听设置更新事件
  window.addEventListener('settingsUpdated', handleSettingsUpdated)
})

onUnmounted(() => {
  stopPolling()
  window.removeEventListener('newOrder', handleNewOrderNotification)
  window.removeEventListener('orderUpdate', handleOrderUpdateNotification)
  window.removeEventListener('navigateToOrder', handleNavigateToOrder)
  window.removeEventListener('settingsUpdated', handleSettingsUpdated)
})

// 加载订单列表
const loadOrderList = async () => {
  try {
    loading.value = true

    // 检查是否使用模拟数据
    if (shouldUseMockData()) {
      console.log('使用模拟数据模式')
      await mockApiDelay(300) // 模拟网络延迟

      const mockResponse = generateMockOrders(
        pagination.page,
        pagination.size,
        effectiveStatusFilter.value,
        searchQuery.value
      )

      orderList.value = mockResponse.data.content
      pagination.total = mockResponse.data.totalElements

      ElMessage.success('已加载模拟数据（后端服务不可用）')
      return
    }

    // 尝试调用真实API
    const params = {
      page: pagination.page,
      size: pagination.size,
      search: searchQuery.value,
      status: effectiveStatusFilter.value,
      sortBy: sortBy.value
    }

    const response = await api.get('/orders', { params })
    if (response.data.code === 200) {
      orderList.value = response.data.data.content
      pagination.total = response.data.data.totalElements
    } else {
      ElMessage.error(response.data.message || '获取订单列表失败')
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)

    // API调用失败时，自动启用模拟数据
    console.log('API调用失败，自动启用模拟数据模式')
    localStorage.setItem('useMockData', 'true')

    await mockApiDelay(300)
    const mockResponse = generateMockOrders(
      pagination.page,
      pagination.size,
      effectiveStatusFilter.value,
      searchQuery.value
    )

    orderList.value = mockResponse.data.content
    pagination.total = mockResponse.data.totalElements

    ElMessage.warning('后端服务不可用，已切换到模拟数据模式')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  loadOrderList()
}

// 刷新列表
const refreshList = () => {
  loadOrderList()
}

// 视图模式切换
const handleViewModeChange = () => {
  localStorage.setItem('orderViewMode', viewMode.value)
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadOrderList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadOrderList()
}

// 状态相关方法已从mockData导入

// 时间格式化
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

// 查看详情
const viewDetails = (order) => {
  selectedOrder.value = order
  detailDialogVisible.value = true
}

// 完成订单
const completeOrder = async (order) => {
  try {
    await ElMessageBox.confirm('确认完成此订单？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await api.post(`/orders/${order.id}/complete`)
    if (response.data.code === 200) {
      ElMessage.success('订单已完成')
      loadOrderList()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完成订单失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 取消订单
const cancelOrder = async (order) => {
  try {
    await ElMessageBox.confirm('确认取消此订单？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await api.post(`/orders/${order.id}/cancel`)
    if (response.data.code === 200) {
      ElMessage.success('订单已取消')
      loadOrderList()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 下拉菜单命令处理
const handleCommand = (command) => {
  const { action, order } = command
  switch (action) {
    case 'detail':
      viewDetails(order)
      break
    case 'complete':
      completeOrder(order)
      break
    case 'cancel':
      cancelOrder(order)
      break
    case 'download':
      downloadFile(order)
      break
  }
}

// 下载文件
const downloadFile = async (order) => {
  try {
    const response = await api.get(`/files/${order.fileId}/download`, {
      responseType: 'blob'
    })

    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = order.fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('文件下载成功')
  } catch (error) {
    console.error('下载文件失败:', error)
    ElMessage.error('下载文件失败')
  }
}

// WebSocket事件处理
const handleNewOrderNotification = (event) => {
  const notification = event.detail
  console.log('收到新订单通知:', notification)
  loadOrderList()
}

const handleOrderUpdateNotification = (event) => {
  const notification = event.detail
  console.log('收到订单更新通知:', notification)
  loadOrderList()
}

const handleNavigateToOrder = (event) => {
  const { orderId } = event.detail
  const order = orderList.value.find(o => o.id === orderId)
  if (order) {
    viewDetails(order)
  }
}

// 轮询相关函数
const startPolling = () => {
  if (!pollingEnabled.value || updateMode.value === 'websocket') return

  stopPolling()

  pollingInterval.value = setInterval(() => {
    if (pollingEnabled.value && updateMode.value === 'polling') {
      loadOrderList()
    }
  }, pollingFrequency.value)
}

const stopPolling = () => {
  if (pollingInterval.value) {
    clearInterval(pollingInterval.value)
    pollingInterval.value = null
  }
}

const updatePollingSettings = (enabled, frequency) => {
  pollingEnabled.value = enabled
  pollingFrequency.value = frequency

  localStorage.setItem('pollingEnabled', enabled.toString())
  localStorage.setItem('pollingFrequency', frequency.toString())

  if (enabled) {
    startPolling()
  } else {
    stopPolling()
  }
}

// 处理设置更新事件
const handleSettingsUpdated = (event) => {
  const { polling, display } = event.detail

  if (polling) {
    updateMode.value = polling.updateMode || 'websocket'
    updatePollingSettings(polling.enabled, polling.interval)

    if (polling.updateMode === 'websocket') {
      webSocketService.setEnabled(polling.enabled)
      stopPolling()
    } else {
      webSocketService.setEnabled(false)
      if (polling.enabled) {
        startPolling()
      } else {
        stopPolling()
      }
    }
  }

  if (display) {
    if (display.defaultViewMode) {
      viewMode.value = display.defaultViewMode
    }
    if (display.pageSize) {
      pagination.size = display.pageSize
      pagination.page = 1
      loadOrderList()
    }
  }
}
</script>

<style scoped>
.orders-page {
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

.order-cards {
  min-height: 400px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

.cards-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 16px;
}

.order-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.order-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.order-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.order-info h3 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-details {
  flex: 1;
}

.file-name {
  margin: 0 0 4px 0;
  font-weight: 500;
  color: #303133;
}

.file-meta {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.order-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.meta-item .label {
  color: #909399;
  min-width: 60px;
}

.amount {
  font-weight: 600;
  color: #E6A23C;
}

.time {
  color: #606266;
}

.remark {
  flex-direction: column;
  align-items: flex-start;
}

.remark-text {
  background: #F5F7FA;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
  width: 100%;
  word-break: break-all;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.order-detail-content {
  padding: 20px 0;
}
</style>

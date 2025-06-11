<template>
  <div class="orders-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
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
          <el-col :span="4">
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
        <el-table-column prop="status" label="状态" width="100">
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
        <!-- 订单基本信息 -->
        <el-card class="detail-section" shadow="never">
          <template #header>
            <div class="section-header">
              <h3>基本信息</h3>
              <el-tag :type="getStatusType(selectedOrder.status)" size="large">
                {{ getStatusText(selectedOrder.status) }}
              </el-tag>
            </div>
          </template>

          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <label>订单号:</label>
                <span class="order-no">{{ selectedOrder.orderNo }}</span>
                <el-button type="text" size="small" @click="copyToClipboard(selectedOrder.orderNo)">
                  <el-icon><CopyDocument /></el-icon>
                </el-button>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label>验证码:</label>
                <el-tag type="info" size="large">{{ selectedOrder.verifyCode }}</el-tag>
                <el-button type="text" size="small" @click="copyToClipboard(selectedOrder.verifyCode)">
                  <el-icon><CopyDocument /></el-icon>
                </el-button>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label>用户名:</label>
                <span>{{ selectedOrder.userName || '未知用户' }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label>订单金额:</label>
                <span class="amount">¥{{ selectedOrder.amount }}</span>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 文件信息 -->
        <el-card class="detail-section" shadow="never">
          <template #header>
            <h3>文件信息</h3>
          </template>

          <el-row :gutter="20">
            <el-col :span="24">
              <div class="file-info-detail">
                <div class="file-icon-large">
                  <el-icon size="48" color="#409EFF">
                    <Document v-if="selectedOrder.fileType === 'pdf'" />
                    <Picture v-else-if="selectedOrder.fileType === 'image'" />
                    <Document v-else />
                  </el-icon>
                </div>
                <div class="file-details-large">
                  <h4>{{ selectedOrder.fileName }}</h4>
                  <p class="file-type">文件类型: {{ selectedOrder.fileType || '未知' }}</p>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 打印配置 -->
        <el-card class="detail-section" shadow="never">
          <template #header>
            <h3>打印配置</h3>
          </template>

          <el-row :gutter="20">
            <el-col :span="6">
              <div class="config-item">
                <label>打印份数:</label>
                <span class="config-value">{{ selectedOrder.copies }}份</span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="config-item">
                <label>实际页数:</label>
                <span class="config-value">{{ selectedOrder.actualPages }}页</span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="config-item">
                <label>颜色模式:</label>
                <el-tag :type="selectedOrder.isColor ? 'warning' : 'info'" size="small">
                  {{ selectedOrder.isColor ? '彩色' : '黑白' }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="config-item">
                <label>打印面数:</label>
                <el-tag :type="selectedOrder.isDoubleSide ? 'success' : 'info'" size="small">
                  {{ selectedOrder.isDoubleSide ? '双面' : '单面' }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="config-item">
                <label>纸张规格:</label>
                <span class="config-value">{{ selectedOrder.paperSize || 'A4' }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="config-item">
                <label>页数范围:</label>
                <span class="config-value">{{ selectedOrder.pageRange || '全部页面' }}</span>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 时间信息 -->
        <el-card class="detail-section" shadow="never">
          <template #header>
            <h3>时间信息</h3>
          </template>

          <el-timeline>
            <el-timeline-item timestamp="创建时间" :hollow="true">
              {{ formatTime(selectedOrder.createTime) }}
            </el-timeline-item>
            <el-timeline-item v-if="selectedOrder.payTime" timestamp="支付时间" type="success">
              {{ formatTime(selectedOrder.payTime) }}
            </el-timeline-item>
            <el-timeline-item v-if="selectedOrder.finishTime" timestamp="完成时间" type="success">
              {{ formatTime(selectedOrder.finishTime) }}
            </el-timeline-item>
            <el-timeline-item timestamp="最后更新" type="info">
              {{ formatTime(selectedOrder.updateTime) }}
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <!-- 备注信息 -->
        <el-card v-if="selectedOrder.remark" class="detail-section" shadow="never">
          <template #header>
            <h3>备注信息</h3>
          </template>
          <div class="remark-content">
            {{ selectedOrder.remark }}
          </div>
        </el-card>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button
            v-if="selectedOrder && (selectedOrder.status === 1 || selectedOrder.status === 2)"
            type="primary"
            @click="downloadFile(selectedOrder)"
          >
            <el-icon><Download /></el-icon>
            下载文件
          </el-button>
          <el-button
            v-if="selectedOrder && selectedOrder.status === 1"
            type="success"
            @click="completeOrder(selectedOrder)"
          >
            <el-icon><Check /></el-icon>
            完成订单
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'
import webSocketService from '@/utils/websocket'

const loading = ref(false)
const searchQuery = ref('')
const statusFilter = ref('')
const sortBy = ref('createTime_desc')
const viewMode = ref('card')
const orderList = ref([])
const detailDialogVisible = ref(false)
const selectedOrder = ref(null)

// 轮询相关
const pollingInterval = ref(null)
const pollingEnabled = ref(true)
const pollingFrequency = ref(5000) // 默认5秒
const updateMode = ref('websocket') // websocket | polling

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
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

const loadOrderList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }

    if (searchQuery.value) {
      params.search = searchQuery.value
    }

    if (statusFilter.value !== '') {
      params.status = statusFilter.value
    }

    if (sortBy.value) {
      const [field, direction] = sortBy.value.split('_')
      params.sortBy = field
      params.sortDirection = direction
    }

    const response = await api.get('/orders', { params })
    if (response.data.code === 200) {
      orderList.value = response.data.data.content || []
      pagination.total = response.data.data.totalElements || 0
    } else {
      ElMessage.error(response.data.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error('加载订单列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadOrderList()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadOrderList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadOrderList()
}

const refreshList = () => {
  loadOrderList()
}

const handleViewModeChange = () => {
  // 视图模式切换时可以保存到localStorage
  localStorage.setItem('orderViewMode', viewMode.value)
}

const handleCommand = (command) => {
  const { action, order } = command
  switch (action) {
    case 'download':
      downloadFile(order)
      break
    case 'complete':
      completeOrder(order)
      break
    case 'cancel':
      cancelOrder(order)
      break
    case 'detail':
      viewDetails(order)
      break
  }
}

const downloadFile = async (order) => {
  try {
    // 这里需要实现文件下载逻辑
    const response = await api.get(`/files/download/${order.id}`, {
      responseType: 'blob'
    })

    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.download = order.fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('文件下载成功')

    // 更新订单状态为已下载
    await updateOrderStatus(order.id, 'downloaded')
  } catch (error) {
    ElMessage.error('文件下载失败')
    console.error(error)
  }
}

const updateOrderStatus = async (orderId, status) => {
  try {
    await api.post(`/orders/${orderId}/status`, { status })
    loadOrderList() // 刷新列表
  } catch (error) {
    console.error('更新订单状态失败:', error)
  }
}

const completeOrder = async (order) => {
  try {
    await ElMessageBox.confirm(
      `确定要完成订单 "${order.orderNo}" 吗？`,
      '确认完成',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await api.post('/orders/complete', null, {
      params: { verifyCode: order.verifyCode }
    })
    
    if (response.data.success) {
      ElMessage.success('订单完成成功')
      loadOrderList()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const cancelOrder = async (order) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消订单 "${order.orderNo}" 吗？`,
      '确认取消',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 这里需要添加取消订单的API
    ElMessage.success('订单取消成功')
    loadOrderList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const viewDetails = (order) => {
  selectedOrder.value = order
  detailDialogVisible.value = true
}

const formatTime = (time) => {
  return new Date(time).toLocaleString()
}

const getStatusType = (status) => {
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

const getStatusText = (status) => {
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

// WebSocket事件处理
const handleNewOrderNotification = (event) => {
  const notification = event.detail
  console.log('收到新订单通知:', notification)
  // 刷新订单列表
  loadOrderList()
}

const handleOrderUpdateNotification = (event) => {
  const notification = event.detail
  console.log('收到订单更新通知:', notification)
  // 刷新订单列表
  loadOrderList()
}

const handleNavigateToOrder = (event) => {
  const { orderId } = event.detail
  // 查找对应的订单并显示详情
  const order = orderList.value.find(o => o.id === orderId)
  if (order) {
    viewDetails(order)
  }
}

// 轮询相关函数
const startPolling = () => {
  if (!pollingEnabled.value || updateMode.value === 'websocket') return

  stopPolling() // 确保没有重复的定时器

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

  // 保存到localStorage
  localStorage.setItem('pollingEnabled', enabled.toString())
  localStorage.setItem('pollingFrequency', frequency.toString())

  // 重启轮询
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

    // 根据更新模式切换WebSocket或轮询
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



// 复制到剪贴板
const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    // 降级方案 - 使用现代方法
    try {
      const textArea = document.createElement('textarea')
      textArea.value = text
      textArea.style.position = 'fixed'
      textArea.style.opacity = '0'
      document.body.appendChild(textArea)
      textArea.focus()
      textArea.select()
      const successful = document.execCommand('copy')
      document.body.removeChild(textArea)
      if (successful) {
        ElMessage.success('已复制到剪贴板')
      } else {
        ElMessage.error('复制失败，请手动复制')
      }
    } catch (fallbackError) {
      ElMessage.error('复制失败，请手动复制')
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

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.order-details {
  padding: 10px 0;
}

/* 卡片模式样式 */
.order-cards {
  min-height: 400px;
}

.cards-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.order-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e4e7ed;
}

.order-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.order-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.order-info h3.order-no {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.order-actions {
  flex-shrink: 0;
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.file-icon {
  flex-shrink: 0;
}

.file-details {
  flex: 1;
  min-width: 0;
}

.file-name {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-meta {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.order-meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.meta-item.remark {
  grid-column: 1 / -1;
  align-items: flex-start;
}

.meta-item .label {
  color: #909399;
  font-weight: 500;
  flex-shrink: 0;
}

.amount {
  color: #f56c6c;
  font-weight: 600;
}

.time {
  color: #606266;
  font-size: 12px;
}

.remark-text {
  color: #606266;
  line-height: 1.4;
  word-break: break-all;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

/* 订单详情弹窗样式 */
.order-detail-content {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h3 {
  margin: 0;
  color: #303133;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  gap: 8px;
}

.info-item label {
  font-weight: 500;
  color: #606266;
  min-width: 80px;
}

.order-no {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #409EFF;
}

.amount {
  font-size: 18px;
  font-weight: 600;
  color: #f56c6c;
}

.file-info-detail {
  display: flex;
  align-items: center;
  gap: 16px;
}

.file-details-large h4 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 16px;
}

.file-type {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.config-item {
  margin-bottom: 16px;
}

.config-item label {
  display: block;
  font-weight: 500;
  color: #606266;
  margin-bottom: 4px;
}

.config-value {
  font-weight: 500;
  color: #303133;
}

.remark-content {
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 4px;
  line-height: 1.6;
  color: #606266;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>

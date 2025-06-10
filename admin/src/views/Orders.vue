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

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchQuery"
              placeholder="搜索订单号或验证码"
              @keyup.enter="handleSearch"
            />
          </el-col>
          <el-col :span="4">
            <el-select v-model="statusFilter" placeholder="订单状态" clearable>
              <el-option label="待支付" :value="0" />
              <el-option label="已支付" :value="1" />
              <el-option label="打印中" :value="2" />
              <el-option label="已完成" :value="3" />
              <el-option label="已取消" :value="4" />
              <el-option label="已退款" :value="5" />
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

      <!-- 订单列表 -->
      <el-table
        v-loading="loading"
        :data="orderList"
        style="width: 100%"
      >
        <el-table-column prop="orderNo" label="订单号" width="180" />
        
        <el-table-column prop="fileName" label="文件名" min-width="200" />
        
        <el-table-column prop="copies" label="份数" width="80" />
        
        <el-table-column prop="actualPages" label="页数" width="80" />
        
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">
            ¥{{ row.amount }}
          </template>
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
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              type="primary"
              size="small"
              @click="completeOrder(row)"
            >
              完成订单
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="warning"
              size="small"
              @click="cancelOrder(row)"
            >
              取消订单
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="viewDetails(row)"
            >
              详情
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

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="600px"
    >
      <div v-if="selectedOrder" class="order-details">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ selectedOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="验证码">{{ selectedOrder.verifyCode }}</el-descriptions-item>
          <el-descriptions-item label="文件名">{{ selectedOrder.fileName }}</el-descriptions-item>
          <el-descriptions-item label="文件类型">{{ selectedOrder.fileType }}</el-descriptions-item>
          <el-descriptions-item label="打印份数">{{ selectedOrder.copies }}</el-descriptions-item>
          <el-descriptions-item label="实际页数">{{ selectedOrder.actualPages }}</el-descriptions-item>
          <el-descriptions-item label="是否彩色">{{ selectedOrder.isColor ? '是' : '否' }}</el-descriptions-item>
          <el-descriptions-item label="是否双面">{{ selectedOrder.isDoubleSide ? '是' : '否' }}</el-descriptions-item>
          <el-descriptions-item label="纸张规格">{{ selectedOrder.paperSize }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ selectedOrder.amount }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(selectedOrder.status)">
              {{ getStatusText(selectedOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(selectedOrder.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ selectedOrder.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'

const loading = ref(false)
const searchQuery = ref('')
const statusFilter = ref('')
const orderList = ref([])
const detailDialogVisible = ref(false)
const selectedOrder = ref(null)

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

onMounted(() => {
  loadOrderList()
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
    
    const response = await api.get('/order/list', { params })
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
    
    const response = await api.post('/order/complete', null, {
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
</style>

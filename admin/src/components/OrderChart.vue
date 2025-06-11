<template>
  <div class="order-chart">
    <el-card>
      <template #header>
        <div class="chart-header">
          <span>订单趋势图</span>
          <div class="chart-controls">
            <el-select v-model="chartType" @change="updateChart" style="width: 120px; margin-right: 10px;">
              <el-option label="总订单" value="total" />
              <el-option label="全部状态" value="all" />
              <el-option label="待支付" value="pending" />
              <el-option label="已支付" value="paid" />
              <el-option label="打印中" value="printing" />
              <el-option label="已完成" value="completed" />
              <el-option label="已取消" value="cancelled" />
              <el-option label="已退款" value="refunded" />
            </el-select>
            
            <el-select v-model="dateRange" @change="updateChart" style="width: 120px; margin-right: 10px;">
              <el-option label="最近7天" :value="7" />
              <el-option label="最近15天" :value="15" />
              <el-option label="最近30天" :value="30" />
              <el-option label="最近60天" :value="60" />
              <el-option label="最近90天" :value="90" />
            </el-select>
            
            <el-button @click="refreshChart" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <div v-loading="loading" class="chart-container">
        <v-chart
          ref="chartRef"
          :option="chartOption"
          :style="{ height: '400px', width: '100%' }"
          autoresize
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent
} from 'echarts/components'
import api from '@/utils/api'

// 注册ECharts组件
use([
  CanvasRenderer,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent
])

const loading = ref(false)
const chartRef = ref()
const chartType = ref('total')
const dateRange = ref(7)

const chartOption = reactive({
  title: {
    text: '订单趋势',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross'
    }
  },
  legend: {
    top: '30px',
    type: 'scroll'
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    top: '80px',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: []
  },
  yAxis: {
    type: 'value',
    minInterval: 1
  },
  series: [],
  dataZoom: [
    {
      type: 'inside',
      start: 0,
      end: 100
    },
    {
      start: 0,
      end: 100,
      height: 30,
      bottom: 10
    }
  ]
})

// 状态颜色配置
const statusColors = {
  total: '#409EFF',
  pending: '#E6A23C',
  paid: '#67C23A',
  printing: '#409EFF',
  completed: '#67C23A',
  cancelled: '#F56C6C',
  refunded: '#909399'
}

onMounted(() => {
  loadChartData()
})

const loadChartData = async () => {
  try {
    loading.value = true
    
    const response = await api.get(`/statistics/recent/${dateRange.value}/chart`, {
      params: { type: chartType.value }
    })
    
    if (response.data.code === 200) {
      const data = response.data.data
      updateChartOption(data)
    } else {
      ElMessage.error(response.data.message || '获取图表数据失败')
    }
  } catch (error) {
    console.error('获取图表数据失败:', error)
    ElMessage.error('获取图表数据失败')
  } finally {
    loading.value = false
  }
}

const updateChartOption = (data) => {
  chartOption.xAxis.data = data.dates
  
  const series = []
  
  if (data.series && data.seriesNames) {
    Object.keys(data.series).forEach(key => {
      const seriesData = data.series[key]
      const seriesName = data.seriesNames[key]
      
      series.push({
        name: seriesName,
        type: 'line',
        data: seriesData,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          width: 2,
          color: statusColors[key] || '#409EFF'
        },
        itemStyle: {
          color: statusColors[key] || '#409EFF'
        },
        areaStyle: chartType.value === 'total' ? {
          opacity: 0.1,
          color: statusColors[key] || '#409EFF'
        } : null
      })
    })
  }
  
  chartOption.series = series
  
  // 更新标题
  const typeNames = {
    total: '总订单趋势',
    all: '各状态订单趋势',
    pending: '待支付订单趋势',
    paid: '已支付订单趋势',
    printing: '打印中订单趋势',
    completed: '已完成订单趋势',
    cancelled: '已取消订单趋势',
    refunded: '已退款订单趋势'
  }
  
  chartOption.title.text = typeNames[chartType.value] || '订单趋势'
  
  // 如果是显示所有状态，调整图例位置
  if (chartType.value === 'all') {
    chartOption.grid.top = '100px'
  } else {
    chartOption.grid.top = '80px'
  }
}

const updateChart = () => {
  loadChartData()
}

const refreshChart = () => {
  loadChartData()
}

// 暴露方法给父组件
defineExpose({
  refreshChart,
  loadChartData
})
</script>

<style scoped>
.order-chart {
  margin-bottom: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-controls {
  display: flex;
  align-items: center;
}

.chart-container {
  min-height: 400px;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>

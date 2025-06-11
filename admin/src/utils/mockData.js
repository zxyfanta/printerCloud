// 模拟数据工具
// 用于在后端服务不可用时提供测试数据

/**
 * 生成模拟订单数据
 */
export function generateMockOrders(page = 1, size = 20, status = null, search = '') {
  const mockOrders = [
    {
      id: 1,
      orderNo: 'PC20241211120001',
      userId: 1,
      userName: '张三',
      fileName: '工作报告.pdf',
      fileType: 'PDF',
      copies: 1,
      pageRange: '1-5',
      actualPages: 5,
      isColor: false,
      isDoubleSide: false,
      paperSize: 'A4',
      remark: '请在下午3点前完成',
      amount: 0.50,
      verifyCode: '123456',
      status: 1, // 已支付
      payTime: '2024-12-11 12:30:00',
      finishTime: null,
      createTime: '2024-12-11 12:00:00',
      updateTime: '2024-12-11 12:30:00'
    },
    {
      id: 2,
      orderNo: 'PC20241211120002',
      userId: 2,
      userName: '李四',
      fileName: '彩色文档.pdf',
      fileType: 'PDF',
      copies: 2,
      pageRange: '1-3',
      actualPages: 3,
      isColor: true,
      isDoubleSide: true,
      paperSize: 'A4',
      remark: '彩色打印质量要好',
      amount: 2.40,
      verifyCode: '234567',
      status: 2, // 打印中
      payTime: '2024-12-11 11:30:00',
      finishTime: null,
      createTime: '2024-12-11 11:00:00',
      updateTime: '2024-12-11 11:30:00'
    },
    {
      id: 3,
      orderNo: 'PC20241211120003',
      userId: 3,
      userName: '王五',
      fileName: '管理文档.docx',
      fileType: 'DOCX',
      copies: 1,
      pageRange: '1-10',
      actualPages: 10,
      isColor: false,
      isDoubleSide: true,
      paperSize: 'A4',
      remark: '需要装订',
      amount: 0.80,
      verifyCode: '345678',
      status: 3, // 已完成
      payTime: '2024-12-11 10:30:00',
      finishTime: '2024-12-11 14:00:00',
      createTime: '2024-12-11 10:00:00',
      updateTime: '2024-12-11 14:00:00'
    },
    {
      id: 4,
      orderNo: 'PC20241211120004',
      userId: 4,
      userName: '赵六',
      fileName: '待支付文档.pdf',
      fileType: 'PDF',
      copies: 1,
      pageRange: '1-2',
      actualPages: 2,
      isColor: false,
      isDoubleSide: false,
      paperSize: 'A4',
      remark: '',
      amount: 0.20,
      verifyCode: '456789',
      status: 0, // 待支付
      payTime: null,
      finishTime: null,
      createTime: '2024-12-11 09:00:00',
      updateTime: '2024-12-11 09:00:00'
    },
    {
      id: 5,
      orderNo: 'PC20241211120005',
      userId: 5,
      userName: '钱七',
      fileName: '已取消文档.pdf',
      fileType: 'PDF',
      copies: 1,
      pageRange: '1-1',
      actualPages: 1,
      isColor: false,
      isDoubleSide: false,
      paperSize: 'A4',
      remark: '',
      amount: 0.10,
      verifyCode: '567890',
      status: 4, // 已取消
      payTime: null,
      finishTime: null,
      createTime: '2024-12-11 08:00:00',
      updateTime: '2024-12-11 08:30:00'
    }
  ]

  // 根据状态过滤
  let filteredOrders = mockOrders
  if (status !== null && status !== undefined && status !== '') {
    filteredOrders = mockOrders.filter(order => order.status === parseInt(status))
  }

  // 根据搜索条件过滤
  if (search && search.trim()) {
    const searchTerm = search.trim().toLowerCase()
    filteredOrders = filteredOrders.filter(order => 
      order.orderNo.toLowerCase().includes(searchTerm) ||
      order.fileName.toLowerCase().includes(searchTerm) ||
      order.userName.toLowerCase().includes(searchTerm) ||
      order.verifyCode.includes(searchTerm)
    )
  }

  // 分页处理
  const startIndex = (page - 1) * size
  const endIndex = startIndex + size
  const paginatedOrders = filteredOrders.slice(startIndex, endIndex)

  return {
    code: 200,
    success: true,
    message: '获取订单列表成功',
    data: {
      content: paginatedOrders,
      totalElements: filteredOrders.length,
      totalPages: Math.ceil(filteredOrders.length / size),
      page: page,
      pageSize: size
    }
  }
}

/**
 * 获取订单状态文本
 */
export function getStatusText(status) {
  const statusMap = {
    0: '待支付',
    1: '已支付',
    2: '打印中',
    3: '已完成',
    4: '已取消',
    5: '已退款'
  }
  return statusMap[status] || '未知状态'
}

/**
 * 获取订单状态标签类型
 */
export function getStatusType(status) {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'primary',
    3: 'success',
    4: 'danger',
    5: 'info'
  }
  return typeMap[status] || 'info'
}

/**
 * 模拟API延迟
 */
export function mockApiDelay(ms = 500) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * 检查是否使用模拟数据
 */
export function shouldUseMockData() {
  // 可以通过环境变量或localStorage控制
  return localStorage.getItem('useMockData') === 'true' || 
         import.meta.env.VITE_USE_MOCK_DATA === 'true'
}

/**
 * 启用模拟数据
 */
export function enableMockData() {
  localStorage.setItem('useMockData', 'true')
  console.log('已启用模拟数据模式')
}

/**
 * 禁用模拟数据
 */
export function disableMockData() {
  localStorage.setItem('useMockData', 'false')
  console.log('已禁用模拟数据模式')
}

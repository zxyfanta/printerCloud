// pages/order-detail/order-detail.js
const app = getApp();

Page({
  /**
   * 页面的初始数据
   */
  data: {
    orderId: null,
    orderInfo: null,
    loading: true,
    hasError: false,
    errorMessage: '',
    statusClass: '',
    statusText: '',
    statusIcon: '',
    statusDescription: '',
    fileIcon: '📄'
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('订单详情页面加载', options);
    
    if (options.id) {
      this.setData({ orderId: options.id });
      this.loadOrderDetail();
    } else {
      this.setData({
        loading: false,
        hasError: true,
        errorMessage: '缺少订单ID参数'
      });
    }
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {
    console.log('订单详情页面渲染完成');
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    // 如果订单ID存在，刷新订单信息
    if (this.data.orderId) {
      this.loadOrderDetail();
    }
  },

  /**
   * 加载订单详情
   */
  loadOrderDetail() {
    this.setData({ loading: true, hasError: false });
    
    app.request({
      url: `/orders/${this.data.orderId}`,
      method: 'GET'
    }).then(res => {
      if (res.code === 200) {
        // 格式化订单信息
        const orderInfo = this.formatOrderInfo(res.data);
        
        this.setData({
          orderInfo: orderInfo,
          loading: false,
          statusClass: this.getStatusClass(orderInfo.status),
          statusText: this.getStatusText(orderInfo.status),
          statusIcon: this.getStatusIcon(orderInfo.status),
          statusDescription: this.getStatusDescription(orderInfo.status),
          fileIcon: this.getFileIcon(orderInfo.fileType || orderInfo.fileName)
        });
      } else {
        this.setData({
          loading: false,
          hasError: true,
          errorMessage: res.message || '获取订单详情失败'
        });
      }
    }).catch(err => {
      console.error('加载订单详情失败：', err);
      this.setData({
        loading: false,
        hasError: true,
        errorMessage: '网络错误，请稍后重试'
      });
    });
  },

  /**
   * 格式化订单信息
   */
  formatOrderInfo(order) {
    // 格式化时间
    if (order.createTime) {
      order.createTime = this.formatTime(order.createTime);
    }
    if (order.payTime) {
      order.payTime = this.formatTime(order.payTime);
    }
    if (order.finishTime) {
      order.finishTime = this.formatTime(order.finishTime);
    }
    
    // 格式化金额
    if (order.printAmount) {
      order.printAmount = parseFloat(order.printAmount).toFixed(2);
    }
    if (order.discountAmount) {
      order.discountAmount = parseFloat(order.discountAmount).toFixed(2);
    }
    if (order.totalAmount) {
      order.totalAmount = parseFloat(order.totalAmount).toFixed(2);
    }
    
    return order;
  },

  /**
   * 格式化时间
   */
  formatTime(timeStr) {
    if (!timeStr) return '';
    
    try {
      const date = new Date(timeStr);
      return `${date.getFullYear()}-${this.padZero(date.getMonth() + 1)}-${this.padZero(date.getDate())} ${this.padZero(date.getHours())}:${this.padZero(date.getMinutes())}`;
    } catch (error) {
      console.error('时间格式化错误：', error);
      return timeStr;
    }
  },

  /**
   * 数字补零
   */
  padZero(num) {
    return num < 10 ? '0' + num : num;
  },

  /**
   * 获取状态样式类
   */
  getStatusClass(status) {
    const statusMap = {
      0: 'pending',   // 待支付
      1: 'paid',      // 已支付
      2: 'processing', // 打印中
      3: 'completed',  // 已完成
      4: 'cancelled'   // 已取消
    };
    return statusMap[status] || 'unknown';
  },

  /**
   * 获取状态文本
   */
  getStatusText(status) {
    const statusMap = {
      0: '待支付',
      1: '已支付',
      2: '打印中',
      3: '已完成',
      4: '已取消'
    };
    return statusMap[status] || '未知状态';
  },

  /**
   * 获取状态图标
   */
  getStatusIcon(status) {
    const iconMap = {
      0: '💰',  // 待支付
      1: '✅',  // 已支付
      2: '🖨️',  // 打印中
      3: '🎉',  // 已完成
      4: '❌'   // 已取消
    };
    return iconMap[status] || '❓';
  },

  /**
   * 获取状态描述
   */
  getStatusDescription(status) {
    const descMap = {
      0: '请尽快完成支付，订单将在30分钟后自动取消',
      1: '您的文件已支付成功，正在等待处理',
      2: '您的文件正在打印中，请耐心等待',
      3: '您的文件已打印完成，请凭验证码取件',
      4: '订单已取消'
    };
    return descMap[status] || '';
  },

  /**
   * 获取文件图标
   */
  getFileIcon(fileType) {
    if (!fileType) return '📄';
    
    const lowerType = typeof fileType === 'string' ? fileType.toLowerCase() : '';
    
    if (lowerType.includes('pdf')) {
      return '📕';
    } else if (lowerType.includes('doc') || lowerType.includes('word')) {
      return '📝';
    } else if (lowerType.includes('xls') || lowerType.includes('excel')) {
      return '📊';
    } else if (lowerType.includes('ppt') || lowerType.includes('powerpoint')) {
      return '📈';
    } else if (lowerType.includes('jpg') || lowerType.includes('jpeg') || lowerType.includes('png') || lowerType.includes('image')) {
      return '🖼️';
    } else {
      return '📄';
    }
  },

  /**
   * 取消订单
   */
  cancelOrder() {
    // 只有待支付状态可以取消
    if (this.data.orderInfo.status !== 0) {
      app.showError('当前订单状态不可取消');
      return;
    }
    
    wx.showModal({
      title: '确认取消',
      content: '确定要取消这个订单吗？',
      success: (res) => {
        if (res.confirm) {
          app.request({
            url: '/orders/cancel',
            method: 'POST',
            data: { orderId: this.data.orderId }
          }).then(result => {
            if (result.code === 200) {
              app.showSuccess('订单已取消');
              this.loadOrderDetail();
            } else {
              app.showError(result.message);
            }
          }).catch(err => {
            console.error('取消订单失败：', err);
            app.showError('取消订单失败');
          });
        }
      }
    });
  },

  /**
   * 支付订单
   */
  payOrder() {
    // 只有待支付状态可以支付
    if (this.data.orderInfo.status !== 0) {
      app.showError('当前订单状态不可支付');
      return;
    }
    
    wx.navigateTo({
      url: `/pages/payment/payment?orderId=${this.data.orderId}`
    });
  },

  /**
   * 再次打印
   */
  reorder() {
    wx.navigateTo({
      url: `/pages/config/config?reorderId=${this.data.orderId}`
    });
  },

  /**
   * 联系客服
   */
  contactService() {
    wx.showModal({
      title: '联系客服',
      content: '如有问题，请拨打客服电话：400-123-4567',
      showCancel: false
    });
  },

  /**
   * 返回上一页
   */
  goBack() {
    wx.navigateBack();
  }
});
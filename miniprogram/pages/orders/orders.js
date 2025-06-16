// orders.js
const app = getApp();

Page({
  data: {
    orderList: [],
    currentStatus: -1, // -1表示全部
    loading: false,
    page: 1,
    pageSize: 10,
    hasMore: true,
    stats: {
      totalOrders: 0,
      totalPages: 0,
      totalAmount: 0
    }
  },

  onLoad() {
    console.log('订单页面加载');
    this.checkLoginAndLoadOrders();
  },

  onShow() {
    console.log('订单页面显示');
    // 刷新订单列表
    this.checkLoginAndLoadOrders();
  },

  /**
   * 检查登录状态并加载订单
   */
  checkLoginAndLoadOrders() {
    if (!app.globalData.isLogin || !app.globalData.userInfo) {
      wx.navigateTo({
        url: '/pages/login/login'
      });
      return;
    }
    
    this.loadOrderList();
  },



  /**
   * 加载订单列表
   */
  loadOrderList(refresh = false) {
    if (this.data.loading) return;

    if (refresh) {
      this.setData({
        page: 1,
        hasMore: true,
        orderList: []
      });
    }

    this.setData({ loading: true });

    const params = {
      page: this.data.page,
      pageSize: this.data.pageSize
    };

    if (this.data.currentStatus !== -1) {
      params.status = this.data.currentStatus;
    }

    app.request({
      url: '/orders',
      method: 'GET',
      data: params
    }).then(res => {
      if (res.code === 200) {
        // 处理不同的数据结构
        let orderData = [];
        if (res.data && res.data.records) {
          orderData = res.data.records;
        } else if (res.data && Array.isArray(res.data)) {
          orderData = res.data;
        } else if (res.data) {
          orderData = [res.data];
        }

        const orders = orderData.map(order => ({
          ...order,
          statusClass: this.getStatusClass(order.status),
          statusText: this.getStatusText(order.status),
          fileIcon: this.getFileIcon(order.fileType),
          createTime: this.formatTime(order.createTime)
        }));

        if (refresh) {
          this.setData({
            orderList: orders
          });
        } else {
          this.setData({
            orderList: [...this.data.orderList, ...orders]
          });
        }

        this.setData({
          hasMore: orders.length === this.data.pageSize,
          page: this.data.page + 1
        });
      } else {
        app.showError(res.message || '加载订单失败');
      }
    }).catch(err => {
      console.error('加载订单列表失败：', err);
      app.showError('加载订单列表失败');
    }).finally(() => {
      this.setData({ loading: false });
    });
  },

  /**
   * 按状态筛选
   */
  filterByStatus(e) {
    const status = parseInt(e.currentTarget.dataset.status);
    this.setData({
      currentStatus: status
    });
    this.loadOrderList(true);
  },

  /**
   * 跳转到订单详情
   */
  goToOrderDetail(e) {
    const orderId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/order-detail/order-detail?id=${orderId}`
    });
  },

  /**
   * 取消订单
   */
  cancelOrder(e) {
    const orderId = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '确认取消',
      content: '确定要取消这个订单吗？',
      success: (res) => {
        if (res.confirm) {
          app.request({
            url: '/orders/cancel',
            method: 'POST',
            data: { orderId: orderId }
          }).then(result => {
            if (result.code === 200) {
              app.showSuccess('订单已取消');
              this.loadOrderList(true);
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
  payOrder(e) {
    const orderId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/payment/payment?orderId=${orderId}`
    });
  },

  /**
   * 再次打印
   */
  reorder(e) {
    const orderId = e.currentTarget.dataset.id;
    // 获取原订单信息，跳转到配置页面
    wx.navigateTo({
      url: `/pages/config/config?reorderId=${orderId}`
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
   * 加载用户统计数据
   */
  loadUserStats() {
    app.request({
      url: '/auth/user/stats',
      method: 'GET'
    }).then(res => {
      if (res.code === 200) {
        this.setData({
          stats: res.data
        });
      }
    }).catch(err => {
      console.error('加载统计数据失败：', err);
    });
  },

  /**
   * 跳转到上传页面
   */
  goToUpload() {
    wx.navigateTo({
      url: '/pages/upload/upload'
    });
  },

  /**
   * 获取订单状态样式类
   */
  getStatusClass(status) {
    const statusMap = {
      0: 'pending',
      1: 'paid',
      2: 'printing',
      3: 'completed',
      4: 'cancelled',
      5: 'refunded'
    };
    return statusMap[status] || 'pending';
  },

  /**
   * 获取订单状态文本
   */
  getStatusText(status) {
    const statusMap = {
      0: '待支付',
      1: '已支付',
      2: '打印中',
      3: '已完成',
      4: '已取消',
      5: '已退款'
    };
    return statusMap[status] || '未知';
  },

  /**
   * 获取文件图标
   */
  getFileIcon(fileType) {
    const iconMap = {
      'pdf': '📄',
      'document': '📝',
      'spreadsheet': '📊',
      'image': '🖼️'
    };
    return iconMap[fileType] || '📁';
  },

  /**
   * 格式化时间
   */
  formatTime(timeStr) {
    const date = new Date(timeStr);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString().slice(0, 5);
  },

  /**
   * 阻止事件冒泡
   */
  stopPropagation() {
    // 阻止事件冒泡
  },

  /**
   * 下拉刷新
   */
  onPullDownRefresh() {
    this.loadOrderList(true);
    wx.stopPullDownRefresh();
  },

  /**
   * 上拉加载更多
   */
  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadOrderList(false);
    }
  }
});

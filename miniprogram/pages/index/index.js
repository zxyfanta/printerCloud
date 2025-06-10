// index.js
const app = getApp();

Page({
  data: {
    recentOrders: [],
    showPriceModal: false,
    showHelpModal: false
  },

  onLoad() {
    console.log('首页加载');
    this.checkLogin();
  },

  onShow() {
    console.log('首页显示');
    if (app.globalData.isLogin) {
      this.loadRecentOrders();
    }
  },

  /**
   * 检查登录状态
   */
  checkLogin() {
    if (!app.globalData.isLogin) {
      // 自动登录
      app.wxLogin().then(() => {
        this.loadRecentOrders();
      }).catch(err => {
        console.error('自动登录失败：', err);
        // 登录失败时显示离线模式
        this.showOfflineMode();
      });
    } else {
      this.loadRecentOrders();
    }
  },

  /**
   * 加载最近订单
   */
  loadRecentOrders() {
    app.request({
      url: '/order/recent',
      method: 'GET'
    }).then(res => {
      if (res.code === 200) {
        const orders = res.data.map(order => ({
          ...order,
          statusClass: this.getStatusClass(order.status),
          statusText: this.getStatusText(order.status),
          createTime: this.formatTime(order.createTime)
        }));
        this.setData({
          recentOrders: orders
        });
      }
    }).catch(err => {
      console.error('加载最近订单失败：', err);
      // 网络错误时显示离线模式
      this.showOfflineMode();
    });
  },

  /**
   * 显示离线模式
   */
  showOfflineMode() {
    console.log('当前为离线模式，部分功能不可用');
    // 可以设置一些离线状态的数据
    this.setData({
      recentOrders: [],
      isOffline: true
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
   * 格式化时间
   */
  formatTime(timeStr) {
    const date = new Date(timeStr);
    const now = new Date();
    const diff = now - date;
    
    if (diff < 60000) { // 1分钟内
      return '刚刚';
    } else if (diff < 3600000) { // 1小时内
      return Math.floor(diff / 60000) + '分钟前';
    } else if (diff < 86400000) { // 1天内
      return Math.floor(diff / 3600000) + '小时前';
    } else {
      return date.toLocaleDateString();
    }
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
   * 跳转到订单页面
   */
  goToOrders() {
    wx.switchTab({
      url: '/pages/orders/orders'
    });
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
   * 显示价格说明
   */
  showPriceInfo() {
    this.setData({
      showPriceModal: true
    });
  },

  /**
   * 隐藏价格说明
   */
  hidePriceModal() {
    this.setData({
      showPriceModal: false
    });
  },

  /**
   * 显示帮助说明
   */
  showHelp() {
    this.setData({
      showHelpModal: true
    });
  },

  /**
   * 隐藏帮助说明
   */
  hideHelpModal() {
    this.setData({
      showHelpModal: false
    });
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
    this.loadRecentOrders();
    wx.stopPullDownRefresh();
  }
});

// index.js
const app = getApp();
const dateUtil = require('../../utils/dateUtil');

Page({
  data: {
    recentOrders: [],
    showPriceModal: false,
    showHelpModal: false,
    priceList: [
      { label: '黑白打印', value: '¥0.1/页', type: 'bw' },
      { label: '彩色打印', value: '¥0.5/页', type: 'color' },
      { label: '双面打印', value: '8折优惠', type: 'double' },
      { label: 'A3纸张', value: '¥0.2/页', type: 'a3' }
    ]
  },

  onLoad() {
    console.log('首页加载');
    this.loadPriceList();
    // 延迟执行，确保小程序完全初始化
    // setTimeout(() =>
    //   this.checkLogin();
    // }, 200);
  },

  onShow() {
    console.log('首页显示');
    // 延迟执行，避免过早调用API
    setTimeout(() => {
      if (app.globalData.isLogin) {
        this.loadRecentOrders();
      }
    }, 100);
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
   * 加载价格列表
   */
  loadPriceList() {
    app.request({
      url: '/price/public',
      method: 'GET'
    }).then(res => {
      if (res.code === 200) {
        const prices = res.data;
        const priceList = [
          { label: '黑白打印', value: '¥0.1/页', type: 'bw' },
          { label: '彩色打印', value: '¥0.5/页', type: 'color' },
          { label: '双面打印', value: '8折优惠', type: 'double' },
          { label: 'A3纸张', value: '¥0.2/页', type: 'a3' }
        ];

        // 更新价格显示
        prices.forEach(price => {
          switch(price.configKey) {
            case 'bw_print':
              priceList[0].value = `¥${price.price}/页`;
              break;
            case 'color_print':
              priceList[1].value = `¥${price.price}/页`;
              break;
            case 'double_side_discount':
              const discountPercent = Math.round(parseFloat(price.price) * 10);
              priceList[2].value = `${discountPercent}折优惠`;
              break;
            case 'a3_extra':
              priceList[3].value = `¥${price.price}/页`;
              break;
          }
        });

        this.setData({
          priceList: priceList
        });
      }
    }).catch(err => {
      console.error('加载价格列表失败：', err);
      // 使用默认价格列表
    });
  },

  /**
   * 加载最近订单
   */
  loadRecentOrders() {
    app.request({
      url: '/orders/recent',
      method: 'GET'
    }).then(res => {
      if (res.code === 200) {
        const orders = res.data.map(order => ({
          ...order,
          statusClass: this.getStatusClass(order.status),
          statusText: this.getStatusText(order.status),
          createTime: dateUtil.formatRelativeTime(order.createTime)
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
      isOffline: true,
      stats: {
        totalOrders: 0,
        totalPages: 0,
        totalAmount: '0.00'
      }
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
   * 跳转到上传页面
   */
  goToUpload() {
    wx.navigateTo({
      url: '/pages/upload/upload'
    });
  },

  /**
   * 开始打印 - 检查登录状态
   */
  startPrint() {
    // 检查登录状态
    if (!app.globalData.isLogin || !app.globalData.userInfo) {
      wx.navigateTo({
        url: '/pages/login/login'
      });
      return;
    }
    
    // 已登录，直接跳转到上传页面
    this.goToUpload();
  },

  /**
   * 显示登录提示弹窗
   */
  showLoginModal() {
    wx.navigateTo({
      url: '/pages/login/login'
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
   * 跳转到二手商品页面
   */
  goToSecondHand() {
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  },

  /**
   * 跳转到优惠购页面
   */
  goToDiscount() {
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  },

  /**
   * 跳转到电子产品页面
   */
  goToElectronics() {
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
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
  },

  /**
   * 分享功能
   */
  onShareAppMessage() {
    return {
      title: '云打印服务 - 随时随地，轻松打印',
      path: '/pages/index/index',
      imageUrl: '/images/share-image.png'
    };
  },

  /**
   * 分享到朋友圈
   */
  onShareTimeline() {
    return {
      title: '云打印服务 - 随时随地，轻松打印',
      imageUrl: '/images/share-image.png'
    };
  }
});

// profile.js
const app = getApp();

Page({
  data: {
    userInfo: null,
    orderStats: {},
    showPriceModal: false,
    showHelpModal: false,
    showAboutModal: false
  },

  onLoad() {
    console.log('个人中心页面加载');
    this.checkLoginAndLoadProfile();
  },

  onShow() {
    console.log('个人中心页面显示');
    this.checkLoginAndLoadProfile();
  },

  /**
   * 检查登录状态并加载个人信息
   */
  checkLoginAndLoadProfile() {
    if (!app.globalData.isLogin || !app.globalData.userInfo) {
      this.setData({
        userInfo: null
      });
      return;
    }
    
    this.loadUserInfo();
    this.loadOrderStats();
  },

  /**
   * 加载用户信息
   */
  loadUserInfo() {
    if (app.globalData.isLogin && app.globalData.userInfo) {
      this.setData({
        userInfo: app.globalData.userInfo
      });
    } else {
      this.setData({
        userInfo: null
      });
    }
  },

  /**
   * 加载订单统计
   */
  loadOrderStats() {
    app.request({
      url: '/order/stats',
      method: 'GET'
    }).then(res => {
      if (res.code === 200) {
        this.setData({
          orderStats: res.data
        });
      }
    }).catch(err => {
      console.error('加载订单统计失败：', err);
    });
  },

  /**
   * 跳转到登录页面
   */
  login() {
    wx.navigateTo({
      url: '/pages/login/login'
    });
  },

  /**
   * 编辑个人资料
   */
  editProfile() {
    wx.getUserProfile({
      desc: '用于完善用户资料',
      success: (res) => {
        console.log('获取用户信息成功', res);
        
        // 更新用户信息
        app.request({
          url: '/auth/update',
          method: 'POST',
          data: JSON.stringify(res.userInfo)
        }).then(result => {
          if (result.code === 200) {
            // 更新本地用户信息
            app.globalData.userInfo = {
              ...app.globalData.userInfo,
              ...res.userInfo
            };
            wx.setStorageSync('userInfo', app.globalData.userInfo);
            
            this.setData({
              userInfo: app.globalData.userInfo
            });
            
            app.showSuccess('更新成功');
          } else {
            app.showError('更新失败');
          }
        }).catch(err => {
          console.error('更新用户信息失败：', err);
          app.showError('更新失败');
        });
      },
      fail: (err) => {
        console.error('获取用户信息失败：', err);
        app.showError('获取用户信息失败');
      }
    });
  },

  /**
   * 跳转到订单页面
   */
  goToOrders(e) {
    const status = e.currentTarget.dataset.status;
    let url = '/pages/orders/orders';
    
    if (status !== undefined) {
      url += `?status=${status}`;
    }
    
    wx.switchTab({
      url: '/pages/orders/orders'
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
   * 显示关于我们
   */
  showAbout() {
    this.setData({
      showAboutModal: true
    });
  },

  /**
   * 隐藏关于我们
   */
  hideAboutModal() {
    this.setData({
      showAboutModal: false
    });
  },

  /**
   * 联系客服
   */
  contactService() {
    wx.showModal({
      title: '联系客服',
      content: '客服电话：400-123-4567\n工作时间：9:00-18:00',
      showCancel: true,
      cancelText: '取消',
      confirmText: '拨打电话',
      success: (res) => {
        if (res.confirm) {
          wx.makePhoneCall({
            phoneNumber: '4001234567',
            fail: (err) => {
              console.error('拨打电话失败：', err);
              app.showError('拨打电话失败');
            }
          });
        }
      }
    });
  },

  /**
   * 清除缓存
   */
  clearCache() {
    wx.showModal({
      title: '清除缓存',
      content: '确定要清除本地缓存吗？',
      success: (res) => {
        if (res.confirm) {
          try {
            wx.clearStorageSync();
            app.showSuccess('缓存清除成功');
          } catch (err) {
            console.error('清除缓存失败：', err);
            app.showError('清除缓存失败');
          }
        }
      }
    });
  },

  /**
   * 退出登录
   */
  logout() {
    wx.showModal({
      title: '退出登录',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          app.logout();
          this.setData({
            userInfo: null,
            orderStats: {}
          });
          app.showSuccess('已退出登录');
        }
      }
    });
  },

  /**
   * 阻止事件冒泡
   */
  stopPropagation() {
    // 阻止事件冒泡
  },

  /**
   * 页面分享
   */
  onShareAppMessage() {
    return {
      title: '云打印服务',
      path: '/pages/index/index'
    };
  }
});

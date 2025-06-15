const app = getApp();

Page({
  data: {
    isLoading: false
  },

  onLoad(options) {
    console.log('登录页面加载', options);
    // 检查是否已经登录
    if (app.globalData.isLogin && app.globalData.userInfo) {
      this.navigateBack();
    }
  },

  /**
   * 微信登录
   */
  wxLogin() {
    if (this.data.isLoading) {
      return;
    }

    this.setData({
      isLoading: true
    });

    wx.showLoading({
      title: '登录中...',
      mask: true
    });

    app.wxLogin().then((userData) => {
      wx.hideLoading();
      wx.showToast({
        title: '登录成功',
        icon: 'success',
        duration: 1500
      });
      
      // 延迟返回上一页
      setTimeout(() => {
        this.navigateBack();
      }, 1500);
    }).catch((error) => {
      wx.hideLoading();
      console.error('登录失败：', error);
      
      wx.showModal({
        title: '登录失败',
        content: '微信登录失败，请检查网络连接后重试。\n错误信息：' + (error.message || '未知错误'),
        showCancel: true,
        cancelText: '取消',
        confirmText: '重试',
        success: (res) => {
          if (res.confirm) {
            // 重试登录
            setTimeout(() => {
              this.wxLogin();
            }, 500);
          }
        }
      });
    }).finally(() => {
      this.setData({
        isLoading: false
      });
    });
  },

  /**
   * 返回上一页
   */
  navigateBack() {
    const pages = getCurrentPages();
    if (pages.length > 1) {
      wx.navigateBack();
    } else {
      wx.switchTab({
        url: '/pages/index/index'
      });
    }
  },

  /**
   * 跳过登录
   */
  skipLogin() {
    wx.showModal({
      title: '提示',
      content: '跳过登录将无法使用打印服务和查看订单，确定要跳过吗？',
      showCancel: true,
      cancelText: '取消',
      confirmText: '确定跳过',
      success: (res) => {
        if (res.confirm) {
          this.navigateBack();
        }
      }
    });
  }
});
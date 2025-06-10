// app.js
App({
  globalData: {
    userInfo: null,
    token: null,
    baseUrl: 'http://localhost:8080/api', // 后端API地址
    ossBaseUrl: '', // OSS文件访问地址
    isLogin: false
  },

  onLaunch() {
    console.log('云打印小程序启动');
    
    // 检查登录状态
    this.checkLoginStatus();
    
    // 获取系统信息
    this.getSystemInfo();
  },

  onShow() {
    console.log('云打印小程序显示');
  },

  onHide() {
    console.log('云打印小程序隐藏');
  },

  onError(msg) {
    console.error('小程序错误：', msg);
  },

  /**
   * 检查登录状态
   */
  checkLoginStatus() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    
    if (token && userInfo) {
      this.globalData.token = token;
      this.globalData.userInfo = userInfo;
      this.globalData.isLogin = true;
    }
  },

  /**
   * 获取系统信息
   */
  getSystemInfo() {
    wx.getSystemInfo({
      success: (res) => {
        this.globalData.systemInfo = res;
        console.log('系统信息：', res);
      }
    });
  },

  /**
   * 微信登录
   */
  wxLogin() {
    return new Promise((resolve, reject) => {
      wx.login({
        success: (res) => {
          if (res.code) {
            // 发送 res.code 到后台换取 openId, sessionKey, unionId
            this.request({
              url: '/auth/login',
              method: 'POST',
              data: {
                code: res.code
              }
            }).then(result => {
              if (result.code === 200) {
                this.globalData.token = result.data.token;
                this.globalData.userInfo = result.data.userInfo;
                this.globalData.isLogin = true;
                
                // 保存到本地存储
                wx.setStorageSync('token', result.data.token);
                wx.setStorageSync('userInfo', result.data.userInfo);
                
                resolve(result.data);
              } else {
                reject(new Error(result.message));
              }
            }).catch(reject);
          } else {
            reject(new Error('登录失败：' + res.errMsg));
          }
        },
        fail: reject
      });
    });
  },

  /**
   * 网络请求封装
   */
  request(options) {
    return new Promise((resolve, reject) => {
      wx.request({
        url: this.globalData.baseUrl + options.url,
        method: options.method || 'GET',
        data: options.data || {},
        header: {
          'Content-Type': 'application/json',
          'Authorization': this.globalData.token ? `Bearer ${this.globalData.token}` : '',
          ...options.header
        },
        success: (res) => {
          if (res.statusCode === 200) {
            resolve(res.data);
          } else if (res.statusCode === 401) {
            // 未授权，清除登录信息
            this.logout();
            wx.showToast({
              title: '请重新登录',
              icon: 'none'
            });
            reject(new Error('未授权'));
          } else {
            reject(new Error(`请求失败：${res.statusCode}`));
          }
        },
        fail: reject
      });
    });
  },

  /**
   * 文件上传
   */
  uploadFile(filePath, fileName) {
    return new Promise((resolve, reject) => {
      wx.uploadFile({
        url: this.globalData.baseUrl + '/file/upload',
        filePath: filePath,
        name: 'file',
        formData: {
          fileName: fileName
        },
        header: {
          'Authorization': this.globalData.token ? `Bearer ${this.globalData.token}` : ''
        },
        success: (res) => {
          if (res.statusCode === 200) {
            const data = JSON.parse(res.data);
            resolve(data);
          } else {
            reject(new Error(`上传失败：${res.statusCode}`));
          }
        },
        fail: reject
      });
    });
  },

  /**
   * 退出登录
   */
  logout() {
    this.globalData.token = null;
    this.globalData.userInfo = null;
    this.globalData.isLogin = false;
    
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
  },

  /**
   * 显示加载提示
   */
  showLoading(title = '加载中...') {
    wx.showLoading({
      title: title,
      mask: true
    });
  },

  /**
   * 隐藏加载提示
   */
  hideLoading() {
    wx.hideLoading();
  },

  /**
   * 显示成功提示
   */
  showSuccess(title) {
    wx.showToast({
      title: title,
      icon: 'success'
    });
  },

  /**
   * 显示错误提示
   */
  showError(title) {
    wx.showToast({
      title: title,
      icon: 'none'
    });
  }
});

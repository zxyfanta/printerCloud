// app.js
App({
  globalData: {
    userInfo: null,
    token: null,
    // 环境配置
    env: {
      dev: {
        baseUrl: 'http://localhost:8082/api',
        ossBaseUrl: ''
      },
      prod: {
        baseUrl: 'https://your-domain.com/api', // 生产环境需要替换为实际的HTTPS域名
        ossBaseUrl: 'https://your-domain.com/files' // 生产环境文件访问地址
      }
    },
    // 当前环境：dev-开发环境，prod-生产环境
    currentEnv: 'dev',
    // API基础地址
    get baseUrl() {
      return this.env[this.currentEnv].baseUrl;
    },
    // OSS文件访问地址
    get ossBaseUrl() {
      return this.env[this.currentEnv].ossBaseUrl;
    },
    isLogin: false
  },

  onLaunch() {
    console.log('云打印小程序启动');

    // 初始化环境配置
    this.initEnvConfig();

    // 延迟执行初始化操作，避免过早调用API
    setTimeout(() => {
      // 检查登录状态
      this.checkLoginStatus();

      // 获取系统信息
      this.getSystemInfo();
    }, 100);
  },

  onShow() {
    console.log('云打印小程序显示');
  },

  onHide() {
    console.log('云打印小程序隐藏');
  },

  onError(msg) {
    console.error('小程序错误：', msg);

    // 过滤掉jsbridge相关的错误，避免重复报告
    if (msg && msg.includes && msg.includes('jsbridge')) {
      console.log('jsbridge错误已忽略，这通常是开发工具的兼容性问题');
      return;
    }

    // 其他错误可以进行上报或处理
    if (msg && !msg.includes('reportKeyValue')) {
      // 可以在这里添加错误上报逻辑
      console.error('需要处理的错误：', msg);
    }
  },

  /**
   * 初始化环境配置
   */
  initEnvConfig() {
    try {
      // 尝试从本地存储获取环境配置
      const savedEnv = wx.getStorageSync('currentEnv');
      if (savedEnv && (savedEnv === 'dev' || savedEnv === 'prod')) {
        this.globalData.currentEnv = savedEnv;
      }

      // 根据小程序版本自动切换环境
      // 开发版、体验版使用开发环境，正式版使用生产环境
      const accountInfo = wx.getAccountInfoSync();
      if (accountInfo && accountInfo.miniProgram) {
        const envVersion = accountInfo.miniProgram.envVersion;
        
        if (envVersion === 'release') {
          // 正式版使用生产环境
          this.globalData.currentEnv = 'prod';
        } else {
          // 开发版、体验版使用开发环境
          this.globalData.currentEnv = 'dev';
        }

        // 保存当前环境到本地存储
        wx.setStorageSync('currentEnv', this.globalData.currentEnv);
      }

      console.log('当前环境：', this.globalData.currentEnv);
      console.log('API地址：', this.globalData.baseUrl);
    } catch (error) {
      console.error('初始化环境配置失败：', error);
    }
  },

  /**
   * 检查登录状态
   */
  checkLoginStatus() {
    try {
      const token = wx.getStorageSync('token');
      const userInfo = wx.getStorageSync('userInfo');

      if (token && userInfo) {
        this.globalData.token = token;
        this.globalData.userInfo = userInfo;
        this.globalData.isLogin = true;
      }
    } catch (error) {
      console.error('检查登录状态失败：', error);
    }
  },

  /**
   * 获取系统信息
   */
  getSystemInfo() {
    try {
      wx.getSystemInfo({
        success: (res) => {
          this.globalData.systemInfo = res;
          console.log('系统信息：', res);
        },
        fail: (error) => {
          console.error('获取系统信息失败：', error);
          // 重试机制
          setTimeout(() => {
            this.getSystemInfo();
          }, 1000);
        }
      });
    } catch (error) {
      console.error('获取系统信息异常：', error);
      // 重试机制
      setTimeout(() => {
        this.getSystemInfo();
      }, 1000);
    }
  },

  /**
   * 微信登录
   */
  wxLogin() {
    const self = this;
    return new Promise((resolve, reject) => {
      wx.login({
        success: (res) => {
          if (res.code) {
            // 发送 res.code 到后台换取 openId, sessionKey, unionId
            console.log('发送登录请求，参数：', {
              code: res.code,
              loginType: 'WECHAT'
            });
            self.request({
              url: '/auth/login',
              method: 'POST',
              data: {
                code: res.code,
                loginType: 'WECHAT'
              }
            }).then(result => {
              console.log('后端登录响应：', result);
              console.log('result.token:', result.token);
              console.log('result.userInfo:', result.userInfo);
              if (result.code === 200) {
                // 直接从result获取数据，不使用result.data
                const token = result.token;
                const userInfo = result.userInfo;
                
                if (token && userInfo) {
                  self.globalData.token = token;
                  self.globalData.userInfo = userInfo;
                  self.globalData.isLogin = true;
                  
                  // 保存到本地存储
                  wx.setStorageSync('token', token);
                  wx.setStorageSync('userInfo', userInfo);
                  
                  console.log('登录成功，用户信息：', userInfo);
                  resolve({token: token, userInfo: userInfo});
                } else {
                  reject(new Error('登录响应数据格式错误'));
                }
              } else {
                reject(new Error(result.message || '登录失败'));
              }
            }).catch(error => {
              console.error('登录请求失败：', error);
              reject(error);
            });
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
            // 未授权，清除登录信息并自动触发登录
            this.logout();
            // 保存当前页面路径
            const pages = getCurrentPages();
            const currentPage = pages[pages.length - 1];
            const url = '/' + currentPage.route;
            // 跳转到登录页
            wx.navigateTo({
              url: '/pages/login/login'
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
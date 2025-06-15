// upload-server.js
const app = getApp();

Page({
  data: {
    orderData: null,
    fileInfo: {},
    uploading: false,
    uploadProgress: 0,
    uploadSuccess: false,
    orderId: null,
    useSimulatedUpload: false, // 是否使用模拟上传
    uploadError: false,
    errorMessage: '',
    retryCount: 0,
    maxRetries: 3
  },

  onLoad(options) {
    console.log('文件上传页面加载');
  },

  onReady() {
    console.log('文件上传页面渲染完成');
    this.loadOrderData();
    // 检查环境配置
    this.checkEnvironment();
    // 延迟启动上传，确保页面完全渲染
    setTimeout(() => {
      this.startUpload();
    }, 500);
  },

  /**
   * 检查环境配置
   */
  checkEnvironment() {
    // 检查是否为开发环境
    const envConfig = app.globalData.envConfig || {};
    const isDev = envConfig.env === 'development';
    
    // 在开发环境下默认使用模拟上传
    this.setData({
      useSimulatedUpload: isDev
    });
    
    console.log(`当前环境: ${isDev ? '开发环境' : '生产环境'}, 使用${isDev ? '模拟' : '真实'}上传`);
  },

  /**
   * 加载订单数据
   */
  loadOrderData() {
    const tempOrderData = app.globalData.tempOrderData;
    const tempFileInfo = app.globalData.tempFileInfo;

    if (!tempOrderData || !tempFileInfo) {
      app.showError('订单信息丢失');
      wx.navigateBack();
      return;
    }

    // 格式化文件信息
    const formattedFileInfo = {
      ...tempFileInfo,
      sizeText: this.formatFileSize(tempFileInfo.size)
    };

    this.setData({
      orderData: tempOrderData,
      fileInfo: formattedFileInfo
    });
  },

  /**
   * 格式化文件大小
   */
  formatFileSize(size) {
    if (!size || size === 0) {
      return '未知大小';
    }

    if (size < 1024) {
      return size + 'B';
    } else if (size < 1024 * 1024) {
      return (size / 1024).toFixed(1) + 'KB';
    } else if (size < 1024 * 1024 * 1024) {
      return (size / 1024 / 1024).toFixed(2) + 'MB';
    } else {
      return (size / 1024 / 1024 / 1024).toFixed(2) + 'GB';
    }
  },

  /**
   * 开始上传文件
   */
  startUpload() {
    // 重置上传状态
    this.setData({
      uploading: true,
      uploadProgress: 0,
      uploadSuccess: false,
      uploadError: false,
      errorMessage: ''
    });

    // 创建订单
    this.createOrder().then(orderId => {
      this.setData({ orderId });
      
      // 根据环境选择上传方式
      if (this.data.useSimulatedUpload) {
        return this.simulateUpload();
      } else {
        return this.uploadFileToServer();
      }
    }).then(() => {
      this.setData({
        uploading: false,
        uploadSuccess: true,
        retryCount: 0 // 重置重试计数
      });
      
      app.showSuccess('文件上传成功，订单已提交');
      
      // 清理临时数据
      app.globalData.tempOrderData = null;
      app.globalData.tempFileInfo = null;
      
      // 延迟跳转到订单详情
      setTimeout(() => {
        wx.redirectTo({
          url: `/pages/order-detail/order-detail?id=${this.data.orderId}`
        });
      }, 2000);
      
    }).catch(err => {
      console.error('上传失败：', err);
      
      this.setData({
        uploading: false,
        uploadError: true,
        errorMessage: this.getErrorMessage(err)
      });
      
      // 显示错误提示
      app.showError('上传失败，请重试');
    });
  },

  /**
   * 获取友好的错误信息
   */
  getErrorMessage(err) {
    if (!err) return '未知错误';
    
    // 网络错误处理
    if (err.errMsg && err.errMsg.includes('request:fail')) {
      if (err.errMsg.includes('timeout')) {
        return '网络请求超时，请检查网络连接';
      }
      if (err.errMsg.includes('fail')) {
        return '网络连接失败，请检查网络设置';
      }
    }
    
    // HTTP状态码错误
    if (err.message && err.message.includes('HTTP')) {
      if (err.message.includes('401')) {
        return '登录状态已过期，请重新登录';
      }
      if (err.message.includes('413')) {
        return '文件太大，超出服务器接收限制';
      }
      if (err.message.includes('500')) {
        return '服务器内部错误，请稍后重试';
      }
    }
    
    // 文件错误
    if (err.message && err.message.includes('文件')) {
      return err.message;
    }
    
    // 默认错误信息
    return err.message || '上传过程中出现错误，请重试';
  },

  /**
   * 创建订单
   */
  createOrder() {
    // 检查登录状态
    if (!app.globalData.isLogin || !app.globalData.userInfo) {
      wx.navigateTo({
        url: '/pages/login/login'
      });
      return;
    }

    return new Promise((resolve, reject) => {
      const { orderData } = this.data;
      
      const orderRequest = {
        userId: app.globalData.userInfo.id,
        userName: app.globalData.userInfo?.nickname || app.globalData.userInfo?.openId || '微信用户',
        fileName: orderData.fileInfo.name || '未知文件',
        fileType: this.getFileTypeFromName(orderData.fileInfo.name) || 'pdf',
        copies: orderData.printConfig.copies || 1,
        pageRange: orderData.printConfig.pageRangeType === 'custom' ? orderData.printConfig.pageRange : null,
        actualPages: orderData.calculatedPages || 1,
        isColor: orderData.printConfig.isColor || false,
        isDoubleSide: orderData.printConfig.isDoubleSide || false,
        paperSize: orderData.printConfig.paperSize || 'A4',
        remark: orderData.printConfig.remark || '',
        amount: (orderData.totalAmount || '0').toString()
      };

      console.log('发送订单请求:', orderRequest);

      app.request({
        url: '/orders',
        method: 'POST',
        data: orderRequest
      }).then(res => {
        if (res.code === 200) {
          resolve(res.data.orderId || Date.now()); // 如果后端没返回ID，使用时间戳
        } else {
          reject(new Error(res.message || '创建订单失败'));
        }
      }).catch(reject);
    });
  },

  /**
   * 模拟上传文件（开发环境使用）
   */
  simulateUpload() {
    return new Promise((resolve, reject) => {
      console.log('使用模拟上传模式');
      
      // 模拟上传进度
      let progress = 0;
      const timer = setInterval(() => {
        progress += Math.random() * 20;
        if (progress >= 100) {
          progress = 100;
          clearInterval(timer);
          resolve();
        }
        
        this.setData({
          uploadProgress: Math.floor(progress)
        });
      }, 200);
    });
  },

  /**
   * 上传文件到服务器（生产环境使用）
   */
  uploadFileToServer() {
    return new Promise((resolve, reject) => {
      const { fileInfo } = this.data;
      
      if (!fileInfo || !fileInfo.path) {
        reject(new Error('文件信息不完整'));
        return;
      }
      
      console.log('开始真实文件上传');
      
      // 检查文件大小
      if (fileInfo.size > 50 * 1024 * 1024) { // 50MB
        reject(new Error('文件大小超过50MB限制'));
        return;
      }
      
      const uploadTask = wx.uploadFile({
        url: app.globalData.baseUrl + '/file/upload',
        filePath: fileInfo.path,
        name: 'file',
        formData: {
          fileName: fileInfo.name,
          orderId: this.data.orderId
        },
        header: {
          'Authorization': app.globalData.token ? `Bearer ${app.globalData.token}` : ''
        },
        success: (res) => {
          if (res.statusCode === 200) {
            try {
              const data = JSON.parse(res.data);
              if (data.code === 200) {
                resolve(data.data);
              } else {
                reject(new Error(data.message || '服务器处理文件失败'));
              }
            } catch (e) {
              reject(new Error('解析服务器响应失败'));
            }
          } else {
            reject(new Error(`上传失败：HTTP ${res.statusCode}`));
          }
        },
        fail: (err) => {
          console.error('文件上传失败：', err);
          reject(err);
        }
      });

      // 监听上传进度
      uploadTask.onProgressUpdate((res) => {
        this.setData({
          uploadProgress: res.progress
        });
      });
    });
  },

  /**
   * 从文件名获取文件类型
   */
  getFileTypeFromName(fileName) {
    const ext = fileName.split('.').pop().toLowerCase();
    const typeMap = {
      'pdf': 'pdf',
      'doc': 'document',
      'docx': 'document',
      'xls': 'spreadsheet',
      'xlsx': 'spreadsheet',
      'ppt': 'presentation',
      'pptx': 'presentation',
      'jpg': 'image',
      'jpeg': 'image',
      'png': 'image',
      'gif': 'image'
    };
    return typeMap[ext] || 'document';
  },

  /**
   * 查看订单详情
   */
  viewOrder() {
    if (this.data.orderId) {
      wx.redirectTo({
        url: `/pages/order-detail/order-detail?id=${this.data.orderId}`
      });
    }
  },

  /**
   * 返回首页
   */
  goHome() {
    wx.switchTab({
      url: '/pages/index/index'
    });
  },



  /**
   * 重试上传
   */
  retryUpload() {
    // 增加重试计数
    const retryCount = this.data.retryCount + 1;
    this.setData({ retryCount });
    
    // 检查是否超过最大重试次数
    if (retryCount > this.data.maxRetries) {
      app.showError('已达到最大重试次数，请稍后再试');
      return;
    }
    
    // 开始重试上传
    console.log(`开始第${retryCount}次重试上传`);
    this.startUpload();
  }
});

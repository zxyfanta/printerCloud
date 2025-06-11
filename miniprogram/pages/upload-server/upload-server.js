// upload-server.js
const app = getApp();

Page({
  data: {
    orderData: null,
    fileInfo: {},
    uploading: false,
    uploadProgress: 0,
    uploadSuccess: false,
    orderId: null
  },

  onLoad(options) {
    console.log('文件上传页面加载');
  },

  onReady() {
    console.log('文件上传页面渲染完成');
    this.loadOrderData();
    // 延迟启动上传，确保页面完全渲染
    setTimeout(() => {
      this.startUpload();
    }, 500);
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
    this.setData({
      uploading: true,
      uploadProgress: 0
    });

    // 模拟创建订单
    this.createOrder().then(orderId => {
      this.setData({ orderId });
      return this.uploadFile();
    }).then(() => {
      this.setData({
        uploading: false,
        uploadSuccess: true
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
      this.setData({
        uploading: false
      });
      console.error('上传失败：', err);
      app.showError('上传失败，请重试');
    });
  },

  /**
   * 创建订单
   */
  createOrder() {
    return new Promise((resolve, reject) => {
      const { orderData } = this.data;
      
      const orderRequest = {
        fileName: orderData.fileInfo.name,
        fileSize: orderData.fileInfo.size || 0,
        fileType: this.getFileTypeFromName(orderData.fileInfo.name),
        copies: orderData.printConfig.copies,
        pageRange: orderData.printConfig.pageRangeType === 'custom' ? orderData.printConfig.pageRange : null,
        actualPages: orderData.calculatedPages,
        isColor: orderData.printConfig.isColor ? 1 : 0,
        isDoubleSide: orderData.printConfig.isDoubleSide ? 1 : 0,
        paperSize: orderData.printConfig.paperSize,
        remark: orderData.printConfig.remark,
        amount: parseFloat(orderData.totalAmount),
        status: 1 // 已支付状态
      };

      app.request({
        url: '/orders',
        method: 'POST',
        data: orderRequest
      }).then(res => {
        if (res.code === 200) {
          resolve(res.data.orderId || Date.now()); // 模拟订单ID
        } else {
          reject(new Error(res.message || '创建订单失败'));
        }
      }).catch(reject);
    });
  },

  /**
   * 上传文件
   */
  uploadFile() {
    return new Promise((resolve, reject) => {
      const { fileInfo } = this.data;
      
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

      // 实际上传逻辑（当后端准备好时启用）
      /*
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
            const data = JSON.parse(res.data);
            if (data.code === 200) {
              resolve(data.data);
            } else {
              reject(new Error(data.message));
            }
          } else {
            reject(new Error(`上传失败：${res.statusCode}`));
          }
        },
        fail: reject
      });

      uploadTask.onProgressUpdate((res) => {
        this.setData({
          uploadProgress: res.progress
        });
      });
      */
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
  }
});

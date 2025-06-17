// upload.js
const app = getApp();

Page({
  data: {
    fileInfo: null,
    uploading: false,
    uploadProgress: 0,
    showPreview: false,
    previewPages: [],
    pollingFileInfo: false,
    pollingMessage: ''
  },

  onLoad() {
    console.log('文件上传页面加载');
  },

  onReady() {
    // 页面渲染完成后再执行相关操作
    console.log('文件上传页面渲染完成');
  },

  onUnload() {
    // 页面卸载时清理轮询定时器
    if (this.pollingTimer) {
      clearInterval(this.pollingTimer);
      this.pollingTimer = null;
      wx.hideLoading();
    }
  },

  /**
   * 选择文件
   */
  chooseFile() {
    const that = this;

    // 先验证token是否有效
    app.validateToken().then(isValid => {
      if (!isValid) {
        // token无效，跳转到登录页面
        wx.navigateTo({
          url: '/pages/login/login?redirect=upload'
        });
        return;
      }

      // token有效，显示文件选择菜单
      wx.showActionSheet({
        itemList: ['从相册选择', '拍照', '选择文件'],
        success(res) {
          if (res.tapIndex === 0) {
            // 从相册选择
            that.chooseImage('album');
          } else if (res.tapIndex === 1) {
            // 拍照
            that.chooseImage('camera');
          } else if (res.tapIndex === 2) {
            // 选择文件
            that.chooseDocument();
          }
        }
      });
    }).catch(err => {
      console.error('Token验证失败:', err);
      app.showError('登录验证失败，请重新登录');
      wx.navigateTo({
        url: '/pages/login/login?redirect=upload'
      });
    });
  },

  /**
   * 选择图片
   */
  chooseImage(sourceType) {
    const that = this;
    
    wx.chooseImage({
      count: 1,
      sizeType: ['original'],
      sourceType: [sourceType],
      success(res) {
        const tempFilePath = res.tempFilePaths[0];
        that.handleFileSelected(tempFilePath, 'image');
      },
      fail(err) {
        console.error('选择图片失败：', err);
        app.showError('选择图片失败');
      }
    });
  },

  /**
   * 选择文档
   */
  chooseDocument() {
    const that = this;
    
    wx.chooseMessageFile({
      count: 1,
      type: 'file',
      success(res) {
        const file = res.tempFiles[0];
        that.handleFileSelected(file.path, file.type, file.name, file.size);
      },
      fail(err) {
        console.error('选择文件失败：', err);
        app.showError('选择文件失败');
      }
    });
  },

  /**
   * 处理文件选择
   */
  handleFileSelected(filePath, fileType, fileName, fileSize) {
    // 获取文件信息
    wx.getFileInfo({
      filePath: filePath,
      success: (res) => {
        const actualFileType = fileType || this.getFileTypeFromPath(filePath);
        const fileInfo = {
          path: filePath,
          name: fileName || this.getFileNameFromPath(filePath),
          size: fileSize || res.size,
          type: actualFileType,
          uploaded: false,
          serverId: null,
          // 预处理显示数据
          icon: this.getFileIcon(actualFileType),
          sizeText: this.formatFileSize(fileSize || res.size),
          typeName: this.getFileTypeName(actualFileType),
          isImage: this.isImageFile(actualFileType)
        };

        this.setData({
          fileInfo: fileInfo,
          showPreview: true
        });

        // 生成本地预览
        this.generateLocalPreview(fileInfo);

        // 立即上传文件到服务器
        this.uploadFileToServer(fileInfo);
      },
      fail: (err) => {
        console.error('获取文件信息失败：', err);
        app.showError('获取文件信息失败');
      }
    });
  },

  /**
   * 生成本地预览
   */
  generateLocalPreview(fileInfo) {
    if (this.isImageFile(fileInfo.type)) {
      // 图片文件直接预览
      this.setData({
        previewPages: [{
          type: 'image',
          url: fileInfo.path,
          pageNumber: 1
        }]
      });
    } else {
      // 其他文件类型显示文件信息
      this.setData({
        previewPages: [{
          type: 'document',
          name: fileInfo.name,
          size: this.formatFileSize(fileInfo.size),
          icon: this.getFileIcon(fileInfo.type),
          pageNumber: 1
        }]
      });
    }
  },

  /**
   * 删除文件
   */
  removeFile() {
    this.setData({
      fileInfo: null,
      uploading: false,
      uploadProgress: 0
    });
  },

  /**
   * 上传文件到服务器
   */
  uploadFileToServer(fileInfo) {
    // 直接开始上传（token已在chooseFile中验证）
    this.performFileUpload(fileInfo);
  },

  /**
   * 执行文件上传
   */
  performFileUpload(fileInfo) {
    this.setData({
      uploading: true,
      uploadProgress: 0
    });

    const uploadTask = wx.uploadFile({
      url: app.globalData.baseUrl + '/file/upload',
      filePath: fileInfo.path,
      name: 'file',
      header: {
        'Authorization': app.globalData.token ? `Bearer ${app.globalData.token}` : ''
      },
      success: (res) => {
        if (res.statusCode === 200) {
          const data = JSON.parse(res.data);
          if (data.code === 200) {
            // 上传成功，更新文件信息
            const updatedFileInfo = {
              ...fileInfo,
              uploaded: true,
              serverId: data.data.id,
              serverData: data.data
            };

            this.setData({
              fileInfo: updatedFileInfo,
              uploading: false,
              uploadProgress: 100
            });

            app.showSuccess('文件上传成功');

            // 存储到全局数据
            app.globalData.tempFileInfo = updatedFileInfo;

            // 开始轮询获取文件信息
            this.startPollingFileInfo(data.data.id);

          } else {
            this.handleUploadError(data.message || '上传失败');
          }
        } else if (res.statusCode === 401) {
          // 未授权，token可能已过期
          app.logout();
          this.setData({
            uploading: false,
            uploadProgress: 0
          });
          wx.showModal({
            title: '登录已过期',
            content: '您的登录已过期，请重新登录后继续上传',
            showCancel: false,
            confirmText: '重新登录',
            success: () => {
              wx.navigateTo({
                url: '/pages/login/login?redirect=upload'
              });
            }
          });
        } else {
          this.handleUploadError(`上传失败：${res.statusCode}`);
        }
      },
      fail: (err) => {
        console.error('文件上传失败：', err);
        this.handleUploadError('上传失败：' + (err.errMsg || '网络错误'));
      }
    });

    // 监听上传进度
    uploadTask.onProgressUpdate((res) => {
      this.setData({
        uploadProgress: res.progress
      });
    });
  },

  /**
   * 开始轮询获取文件信息
   */
  startPollingFileInfo(fileId) {
    console.log('开始轮询获取文件信息', fileId);

    // 显示加载状态，禁用页面操作
    this.setData({
      pollingFileInfo: true,
      pollingMessage: '正在处理文件，请稍候...'
    });

    // 显示加载提示
    wx.showLoading({
      title: '处理文件中...',
      mask: true
    });

    // 设置轮询间隔（毫秒）
    const pollingInterval = 2000; // 2秒

    // 超时时间（毫秒）
    const timeoutDuration = 30000; // 30秒

    let pollingCount = 0;
    const maxPollingCount = Math.ceil(timeoutDuration / pollingInterval);

    // 创建轮询定时器
    const pollingTimer = setInterval(() => {
      pollingCount++;

      // 超过最大轮询次数，停止轮询并超时
      if (pollingCount > maxPollingCount) {
        this.stopPollingWithTimeout(pollingTimer);
        return;
      }

      // 发起请求获取文件信息
      this.getFileInfo(fileId).then(fileInfo => {
        // 更新文件信息
        const updatedFileInfo = {
          ...this.data.fileInfo,
          pageCount: fileInfo.pageCount,
          status: fileInfo.status,
          serverData: fileInfo
        };

        this.setData({
          fileInfo: updatedFileInfo
        });

        // 更新全局的临时文件信息
        app.globalData.tempFileInfo = updatedFileInfo;

        // 如果文件处理完成，停止轮询
        if (fileInfo.status === 4) { // 4-处理完成
          this.stopPollingWithSuccess(pollingTimer, updatedFileInfo);
        }

        // 如果文件处理失败，停止轮询
        if (fileInfo.status === 5) { // 5-处理失败
          this.stopPollingWithError(pollingTimer, fileInfo.parseError || '文件处理失败');
        }

      }).catch(err => {
        console.error('获取文件信息失败：', err);
        // 如果是401错误，说明token已过期
        if (err.message && err.message.includes('401')) {
          this.stopPollingWithAuthError(pollingTimer);
        }
        // 其他错误继续轮询，直到超时
      });
    }, pollingInterval);

    // 保存定时器引用，以便在页面卸载时清理
    this.pollingTimer = pollingTimer;
  },

  /**
   * 轮询成功停止
   */
  stopPollingWithSuccess(pollingTimer, fileInfo) {
    clearInterval(pollingTimer);
    this.pollingTimer = null;

    wx.hideLoading();

    this.setData({
      pollingFileInfo: false,
      pollingMessage: ''
    });

    console.log('文件处理完成，停止轮询');
    console.log('更新后的文件信息：', fileInfo);

    // 显示成功提示
    wx.showToast({
      title: '文件处理完成',
      icon: 'success',
      duration: 1500
    });
  },

  /**
   * 轮询错误停止
   */
  stopPollingWithError(pollingTimer, errorMessage) {
    clearInterval(pollingTimer);
    this.pollingTimer = null;

    wx.hideLoading();

    this.setData({
      pollingFileInfo: false,
      pollingMessage: ''
    });

    console.log('文件处理失败，停止轮询');
    app.showError('文件处理失败：' + errorMessage);
  },

  /**
   * 轮询超时停止
   */
  stopPollingWithTimeout(pollingTimer) {
    clearInterval(pollingTimer);
    this.pollingTimer = null;

    wx.hideLoading();

    this.setData({
      pollingFileInfo: false,
      pollingMessage: ''
    });

    console.log('轮询超时，停止轮询');

    wx.showModal({
      title: '处理超时',
      content: '文件处理时间较长，您可以稍后在订单中查看处理结果，或重新上传文件',
      confirmText: '继续配置',
      cancelText: '重新上传',
      success: (res) => {
        if (res.cancel) {
          // 重新上传
          this.setData({
            fileInfo: {},
            uploading: false,
            uploadProgress: 0
          });
        }
        // 如果选择继续配置，保持当前状态
      }
    });
  },

  /**
   * 轮询认证错误停止
   */
  stopPollingWithAuthError(pollingTimer) {
    clearInterval(pollingTimer);
    this.pollingTimer = null;

    wx.hideLoading();

    this.setData({
      pollingFileInfo: false,
      pollingMessage: ''
    });

    app.logout();
    wx.showModal({
      title: '登录已过期',
      content: '您的登录已过期，请重新登录',
      showCancel: false,
      confirmText: '重新登录',
      success: () => {
        wx.navigateTo({
          url: '/pages/login/login?redirect=upload'
        });
      }
    });
  },

  /**
   * 获取文件信息
   */
  getFileInfo(fileId) {
    return new Promise((resolve, reject) => {
      app.request({
        url: `/file/info/${fileId}`,
        method: 'GET'
      }).then(res => {
        if (res.code === 200) {
          resolve(res.data);
        } else {
          reject(new Error(res.message || '获取文件信息失败'));
        }
      }).catch(reject);
    });
  },

  /**
   * 处理上传错误
   */
  handleUploadError(message) {
    this.setData({
      uploading: false,
      uploadProgress: 0
    });
    app.showError(message);
  },

  /**
   * 跳转到预览页面
   */
  goToPreview() {
    if (!this.data.fileInfo) {
      app.showError('请先选择文件');
      return;
    }

    if (!this.data.fileInfo.uploaded) {
      app.showError('文件还在上传中，请稍候');
      return;
    }

    // 将文件信息存储到全局数据中
    app.globalData.tempFileInfo = this.data.fileInfo;

    wx.navigateTo({
      url: '/pages/preview/preview'
    });
  },

  /**
   * 跳转到配置页面
   */
  goToConfig() {
    if (!this.data.fileInfo) {
      app.showError('请先选择文件');
      return;
    }

    // 将文件信息存储到全局数据中
    app.globalData.tempFileInfo = this.data.fileInfo;

    wx.navigateTo({
      url: '/pages/config/config'
    });
  },

  /**
   * 返回上一页
   */
  goBack() {
    wx.navigateBack();
  },

  /**
   * 从路径获取文件名
   */
  getFileNameFromPath(filePath) {
    return filePath.split('/').pop();
  },

  /**
   * 从路径获取文件类型
   */
  getFileTypeFromPath(filePath) {
    const extension = filePath.split('.').pop().toLowerCase();
    const typeMap = {
      'pdf': 'pdf',
      'doc': 'document',
      'docx': 'document',
      'xls': 'spreadsheet',
      'xlsx': 'spreadsheet',
      'jpg': 'image',
      'jpeg': 'image',
      'png': 'image',
      'gif': 'image'
    };
    return typeMap[extension] || 'unknown';
  },

  /**
   * 获取文件图标
   */
  getFileIcon(fileType) {
    const iconMap = {
      'pdf': '📄',
      'document': '📝',
      'spreadsheet': '📊',
      'presentation': '📋',
      'image': '🖼️',
      'text': '📄',
      'unknown': '📁'
    };
    return iconMap[fileType] || '📁';
  },

  /**
   * 获取文件类型名称
   */
  getFileTypeName(fileType) {
    const nameMap = {
      'pdf': 'PDF文档',
      'document': 'Word文档',
      'spreadsheet': 'Excel表格',
      'presentation': 'PPT演示',
      'image': '图片文件',
      'text': '文本文件',
      'unknown': '未知类型'
    };
    return nameMap[fileType] || '未知类型';
  },

  /**
   * 格式化文件大小
   */
  formatFileSize(size) {
    if (size < 1024) {
      return size + ' B';
    } else if (size < 1024 * 1024) {
      return (size / 1024).toFixed(1) + ' KB';
    } else {
      return (size / (1024 * 1024)).toFixed(1) + ' MB';
    }
  },

  /**
   * 判断是否为图片文件
   */
  isImageFile(fileType) {
    return fileType === 'image';
  },

  /**
   * 判断是否为PDF文件
   */
  isPdfFile(fileType) {
    return fileType === 'pdf';
  }
});

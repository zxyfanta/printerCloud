// pages/preview/preview.js
const app = getApp();

Page({
  data: {
    fileInfo: {},
    isReady: false,
    isDownloading: false,
    downloadProgress: 0,
    hasError: false,
    errorMessage: '',
    localFilePath: '',
    fileUrl: ''
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log('文件预览页面加载', options);

    // 获取文件信息
    this.loadFileInfo(options);
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {
    console.log('文件预览页面渲染完成');
  },

  /**
   * 加载文件信息
   */
  loadFileInfo(options) {
    // 方式1: 从全局数据获取（本地文件）
    const tempFileInfo = app.globalData.tempFileInfo;
    if (tempFileInfo) {
      const formattedFileInfo = {
        name: tempFileInfo.name,
        sizeText: this.formatFileSize(tempFileInfo.size),
        typeText: this.getFileTypeText(tempFileInfo.name),
        icon: this.getFileIcon(tempFileInfo.name),
        localPath: tempFileInfo.path,
        isLocal: true
      };

      this.setData({
        fileInfo: formattedFileInfo,
        localFilePath: tempFileInfo.path,
        isReady: true
      });
      return;
    }

    // 方式2: 从参数获取（远程文件）
    if (options.fileUrl) {
      const fileName = this.getFileNameFromUrl(options.fileUrl);
      const formattedFileInfo = {
        name: fileName,
        sizeText: '远程文件',
        typeText: this.getFileTypeText(fileName),
        icon: this.getFileIcon(fileName),
        isLocal: false
      };

      this.setData({
        fileInfo: formattedFileInfo,
        fileUrl: decodeURIComponent(options.fileUrl)
      });
      return;
    }

    // 没有文件信息，显示错误
    this.setData({
      hasError: true,
      errorMessage: '文件信息丢失，请重新选择文件'
    });
  },

  /**
   * 开始预览
   */
  startPreview() {
    if (this.data.fileInfo.isLocal) {
      // 本地文件直接预览
      this.openDocument();
    } else {
      // 远程文件需要先下载
      this.downloadAndPreview();
    }
  },

  /**
   * 下载并预览远程文件
   */
  downloadAndPreview() {
    if (!this.data.fileUrl) {
      app.showError('文件地址无效');
      return;
    }

    this.setData({
      isDownloading: true,
      downloadProgress: 0,
      hasError: false
    });

    const downloadTask = wx.downloadFile({
      url: this.data.fileUrl,
      success: (res) => {
        if (res.statusCode === 200) {
          this.setData({
            isDownloading: false,
            isReady: true,
            localFilePath: res.tempFilePath
          });

          app.showSuccess('文件下载完成');
        } else {
          this.handleDownloadError('下载失败，请检查网络连接');
        }
      },
      fail: (err) => {
        console.error('文件下载失败：', err);
        this.handleDownloadError('下载失败：' + (err.errMsg || '未知错误'));
      }
    });

    // 监听下载进度
    downloadTask.onProgressUpdate((res) => {
      this.setData({
        downloadProgress: res.progress
      });
    });
  },

  /**
   * 处理下载错误
   */
  handleDownloadError(message) {
    this.setData({
      isDownloading: false,
      hasError: true,
      errorMessage: message
    });
  },

  /**
   * 打开文档预览
   */
  openDocument() {
    if (!this.data.localFilePath) {
      app.showError('文件路径无效');
      return;
    }

    wx.openDocument({
      filePath: this.data.localFilePath,
      fileType: this.getFileExtension(this.data.fileInfo.name),
      success: () => {
        console.log('文档打开成功');
      },
      fail: (err) => {
        console.error('文档打开失败：', err);

        // 根据错误类型给出不同提示
        let errorMsg = '文档打开失败';
        if (err.errMsg.includes('not support')) {
          errorMsg = '不支持该文件格式';
        } else if (err.errMsg.includes('file not exist')) {
          errorMsg = '文件不存在或已损坏';
        } else if (err.errMsg.includes('permission')) {
          errorMsg = '没有权限打开该文件';
        }

        app.showError(errorMsg);
      }
    });
  },

  /**
   * 跳转到配置页面
   */
  goToConfig() {
    if (!this.data.fileInfo.isLocal) {
      app.showError('远程文件暂不支持打印配置');
      return;
    }

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
   * 获取文件类型文本
   */
  getFileTypeText(fileName) {
    const ext = this.getFileExtension(fileName).toLowerCase();
    const typeMap = {
      'pdf': 'PDF文档',
      'doc': 'Word文档',
      'docx': 'Word文档',
      'xls': 'Excel表格',
      'xlsx': 'Excel表格',
      'ppt': 'PowerPoint演示',
      'pptx': 'PowerPoint演示',
      'txt': '文本文档',
      'jpg': '图片文件',
      'jpeg': '图片文件',
      'png': '图片文件',
      'gif': '图片文件'
    };
    return typeMap[ext] || '未知格式';
  },

  /**
   * 获取文件图标
   */
  getFileIcon(fileName) {
    const ext = this.getFileExtension(fileName).toLowerCase();
    const iconMap = {
      'pdf': '📄',
      'doc': '📝',
      'docx': '📝',
      'xls': '📊',
      'xlsx': '📊',
      'ppt': '📈',
      'pptx': '📈',
      'txt': '📃',
      'jpg': '🖼️',
      'jpeg': '🖼️',
      'png': '🖼️',
      'gif': '🖼️'
    };
    return iconMap[ext] || '📁';
  },

  /**
   * 获取文件扩展名
   */
  getFileExtension(fileName) {
    if (!fileName) return '';
    const parts = fileName.split('.');
    return parts.length > 1 ? parts.pop() : '';
  },

  /**
   * 从URL获取文件名
   */
  getFileNameFromUrl(url) {
    if (!url) return '未知文件';
    const parts = url.split('/');
    return parts[parts.length - 1] || '未知文件';
  }
});
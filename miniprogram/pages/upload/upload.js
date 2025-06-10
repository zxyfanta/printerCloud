// upload.js
const app = getApp();

Page({
  data: {
    fileInfo: null,
    uploading: false,
    uploadProgress: 0
  },

  onLoad() {
    console.log('文件上传页面加载');
  },

  /**
   * 选择文件
   */
  chooseFile() {
    const that = this;
    
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
        const fileInfo = {
          path: filePath,
          name: fileName || this.getFileNameFromPath(filePath),
          size: fileSize || res.size,
          type: fileType || this.getFileTypeFromPath(filePath),
          uploaded: false
        };

        this.setData({
          fileInfo: fileInfo
        });

        // 开始上传
        this.uploadFile(fileInfo);
      },
      fail: (err) => {
        console.error('获取文件信息失败：', err);
        app.showError('获取文件信息失败');
      }
    });
  },

  /**
   * 上传文件
   */
  uploadFile(fileInfo) {
    const that = this;
    
    this.setData({
      uploading: true,
      uploadProgress: 0
    });

    const uploadTask = wx.uploadFile({
      url: app.globalData.baseUrl + '/file/upload',
      filePath: fileInfo.path,
      name: 'file',
      formData: {
        fileName: fileInfo.name
      },
      header: {
        'Authorization': app.globalData.token ? `Bearer ${app.globalData.token}` : ''
      },
      success: (res) => {
        if (res.statusCode === 200) {
          const data = JSON.parse(res.data);
          if (data.code === 200) {
            // 上传成功
            const updatedFileInfo = {
              ...fileInfo,
              uploaded: true,
              fileId: data.data.fileId,
              url: data.data.url,
              pageCount: data.data.pageCount
            };
            
            that.setData({
              fileInfo: updatedFileInfo,
              uploading: false
            });
            
            app.showSuccess('文件上传成功');
          } else {
            throw new Error(data.message);
          }
        } else {
          throw new Error(`上传失败：${res.statusCode}`);
        }
      },
      fail: (err) => {
        console.error('文件上传失败：', err);
        that.setData({
          uploading: false
        });
        app.showError('文件上传失败');
      }
    });

    // 监听上传进度
    uploadTask.onProgressUpdate((res) => {
      that.setData({
        uploadProgress: res.progress
      });
    });
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
   * 跳转到配置页面
   */
  goToConfig() {
    if (!this.data.fileInfo || !this.data.fileInfo.uploaded) {
      app.showError('请先上传文件');
      return;
    }

    wx.navigateTo({
      url: `/pages/config/config?fileId=${this.data.fileInfo.fileId}`
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
      'image': '🖼️',
      'unknown': '📁'
    };
    return iconMap[fileType] || '📁';
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

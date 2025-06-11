// upload.js
const app = getApp();

Page({
  data: {
    fileInfo: null,
    uploading: false,
    uploadProgress: 0,
    showPreview: false,
    previewPages: []
  },

  onLoad() {
    console.log('文件上传页面加载');
  },

  onReady() {
    // 页面渲染完成后再执行相关操作
    console.log('文件上传页面渲染完成');
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
        const fileType = fileType || this.getFileTypeFromPath(filePath);
        const fileInfo = {
          path: filePath,
          name: fileName || this.getFileNameFromPath(filePath),
          size: fileSize || res.size,
          type: fileType,
          uploaded: false,
          // 预处理显示数据
          icon: this.getFileIcon(fileType),
          sizeText: this.formatFileSize(fileSize || res.size),
          typeName: this.getFileTypeName(fileType),
          isImage: this.isImageFile(fileType)
        };

        this.setData({
          fileInfo: fileInfo,
          showPreview: true
        });

        // 生成本地预览
        this.generateLocalPreview(fileInfo);
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
   * 跳转到预览页面
   */
  goToPreview() {
    if (!this.data.fileInfo) {
      app.showError('请先选择文件');
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

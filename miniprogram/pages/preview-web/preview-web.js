// pages/preview-web/preview-web.js
const app = getApp();

Page({
  data: {
    pdfUrl: '',
    webviewUrl: '',
    webviewReady: false,
    hasError: false,
    errorMessage: ''
  },

  onLoad(options) {
    console.log('Web预览页面加载', options);
    
    if (options.pdfUrl) {
      this.initWebPreview(decodeURIComponent(options.pdfUrl));
    } else {
      this.showError('PDF文件地址无效');
    }
  },

  /**
   * 初始化Web预览
   */
  initWebPreview(pdfUrl) {
    this.setData({
      pdfUrl: pdfUrl,
      // 使用PDF.js在线服务预览PDF
      // 注意：生产环境建议部署自己的PDF.js服务
      webviewUrl: `https://mozilla.github.io/pdf.js/web/viewer.html?file=${encodeURIComponent(pdfUrl)}`
    });
  },

  /**
   * Web-view加载完成
   */
  onWebviewLoad() {
    console.log('Web-view加载完成');
    this.setData({
      webviewReady: true,
      hasError: false
    });
  },

  /**
   * Web-view加载错误
   */
  onWebviewError(e) {
    console.error('Web-view加载错误：', e);
    this.showError('PDF预览加载失败，请检查网络连接');
  },

  /**
   * 显示错误
   */
  showError(message) {
    this.setData({
      hasError: true,
      errorMessage: message,
      webviewReady: false
    });
  },

  /**
   * 重试
   */
  retry() {
    this.setData({
      hasError: false,
      webviewReady: false
    });
    
    if (this.data.pdfUrl) {
      this.initWebPreview(this.data.pdfUrl);
    }
  },

  /**
   * 使用系统原生预览
   */
  useNativePreview() {
    wx.navigateBack();
    
    // 延迟跳转到原生预览页面
    setTimeout(() => {
      wx.navigateTo({
        url: `/pages/preview/preview?fileUrl=${encodeURIComponent(this.data.pdfUrl)}`
      });
    }, 500);
  }
});

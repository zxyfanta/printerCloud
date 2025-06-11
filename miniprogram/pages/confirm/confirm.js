// confirm.js
const app = getApp();

Page({
  data: {
    orderData: null,
    fileInfo: {},
    printConfig: {},
    calculatedPages: 0,
    unitPrice: 0,
    totalAmount: 0,
    loading: false
  },

  onLoad(options) {
    console.log('订单确认页面加载');
  },

  onReady() {
    console.log('订单确认页面渲染完成');
    this.loadOrderData();
  },

  /**
   * 加载订单数据
   */
  loadOrderData() {
    const tempOrderData = app.globalData.tempOrderData;
    if (!tempOrderData) {
      app.showError('订单信息丢失，请重新配置');
      wx.navigateBack();
      return;
    }

    // 预处理显示文本
    const printConfig = {
      ...tempOrderData.printConfig,
      colorText: this.getColorText(tempOrderData.printConfig.isColor),
      sideText: this.getSideText(tempOrderData.printConfig.isDoubleSide),
      pageRangeText: this.getPageRangeText(
        tempOrderData.printConfig.pageRangeType,
        tempOrderData.printConfig.pageRange,
        tempOrderData.fileInfo.pageCount
      )
    };

    this.setData({
      orderData: tempOrderData,
      fileInfo: tempOrderData.fileInfo,
      printConfig: printConfig,
      calculatedPages: tempOrderData.calculatedPages,
      unitPrice: tempOrderData.unitPrice,
      totalAmount: tempOrderData.totalAmount
    });
  },

  /**
   * 确认订单并支付
   */
  confirmAndPay() {
    this.setData({ loading: true });

    // 模拟支付过程
    wx.showModal({
      title: '确认支付',
      content: `确认支付 ¥${this.data.totalAmount} 吗？`,
      success: (res) => {
        if (res.confirm) {
          this.processPayment();
        } else {
          this.setData({ loading: false });
        }
      }
    });
  },

  /**
   * 处理支付
   */
  processPayment() {
    app.showLoading('支付处理中...');

    // 模拟支付API调用
    setTimeout(() => {
      app.hideLoading();
      
      // 支付成功，跳转到文件上传页面
      wx.redirectTo({
        url: '/pages/upload-server/upload-server'
      });
    }, 2000);
  },

  /**
   * 修改配置
   */
  editConfig() {
    wx.navigateBack();
  },

  /**
   * 获取颜色文本
   */
  getColorText(isColor) {
    return isColor ? '彩色' : '黑白';
  },

  /**
   * 获取单双面文本
   */
  getSideText(isDoubleSide) {
    return isDoubleSide ? '双面' : '单面';
  },

  /**
   * 获取页数范围文本
   */
  getPageRangeText(pageRangeType, pageRange, totalPages) {
    if (pageRangeType === 'all') {
      return `全部页面 (${totalPages}页)`;
    } else {
      return `自定义: ${pageRange}`;
    }
  }
});

// payment.js
const app = getApp();

Page({
  data: {
    orderId: null,
    orderInfo: {},
    paying: false,
    showPaymentResult: false,
    paymentSuccess: false,
    paymentErrorMsg: ''
  },

  onLoad(options) {
    console.log('支付页面加载', options);
    
    if (options.orderId) {
      this.setData({ orderId: options.orderId });
      this.loadOrderInfo();
    } else {
      app.showError('缺少订单信息');
      wx.navigateBack();
    }
  },

  /**
   * 加载订单信息
   */
  loadOrderInfo() {
    app.showLoading('加载订单信息...');
    
    app.request({
      url: `/orders/${this.data.orderId}`,
      method: 'GET'
    }).then(res => {
      app.hideLoading();
      
      if (res.code === 200) {
        this.setData({
          orderInfo: res.data
        });
        
        // 如果订单已支付，直接显示成功结果
        if (res.data.status >= 1) {
          this.setData({
            showPaymentResult: true,
            paymentSuccess: true
          });
        }
      } else {
        app.showError('获取订单信息失败');
        wx.navigateBack();
      }
    }).catch(err => {
      app.hideLoading();
      console.error('加载订单信息失败：', err);
      app.showError('加载订单信息失败');
      wx.navigateBack();
    });
  },

  /**
   * 开始支付
   */
  startPayment() {
    if (this.data.paying) return;
    
    // 检查订单状态
    if (this.data.orderInfo.status !== 0) {
      app.showError('订单状态异常，无法支付');
      return;
    }
    
    this.setData({ paying: true });
    
    // 创建支付订单
    app.request({
      url: '/pay/create',
      method: 'POST',
      data: {
        orderId: this.data.orderId
      }
    }).then(res => {
      if (res.code === 200) {
        const paymentData = res.data;
        this.requestPayment(paymentData);
      } else {
        this.setData({ paying: false });
        app.showError(res.message || '创建支付订单失败');
      }
    }).catch(err => {
      this.setData({ paying: false });
      console.error('创建支付订单失败：', err);
      app.showError('创建支付订单失败');
    });
  },

  /**
   * 调用微信支付
   */
  requestPayment(paymentData) {
    wx.requestPayment({
      timeStamp: paymentData.timeStamp,
      nonceStr: paymentData.nonceStr,
      package: paymentData.package,
      signType: paymentData.signType,
      paySign: paymentData.paySign,
      success: (res) => {
        console.log('支付成功', res);
        this.handlePaymentSuccess();
      },
      fail: (err) => {
        console.error('支付失败', err);
        this.handlePaymentFail(err.errMsg);
      },
      complete: () => {
        this.setData({ paying: false });
      }
    });
  },

  /**
   * 处理支付成功
   */
  handlePaymentSuccess() {
    // 查询支付结果
    this.queryPaymentResult();
  },

  /**
   * 处理支付失败
   */
  handlePaymentFail(errMsg) {
    let errorMessage = '支付失败';
    
    if (errMsg.includes('cancel')) {
      errorMessage = '支付已取消';
    } else if (errMsg.includes('fail')) {
      errorMessage = '支付失败，请重试';
    }
    
    this.setData({
      showPaymentResult: true,
      paymentSuccess: false,
      paymentErrorMsg: errorMessage
    });
  },

  /**
   * 查询支付结果
   */
  queryPaymentResult() {
    app.request({
      url: `/pay/query/${this.data.orderId}`,
      method: 'GET'
    }).then(res => {
      if (res.code === 200) {
        const paymentResult = res.data;
        
        if (paymentResult.status === 'SUCCESS') {
          // 支付成功，更新订单信息
          this.setData({
            'orderInfo.status': 1,
            'orderInfo.verifyCode': paymentResult.verifyCode,
            showPaymentResult: true,
            paymentSuccess: true
          });
        } else {
          this.handlePaymentFail('支付未完成');
        }
      } else {
        this.handlePaymentFail('查询支付结果失败');
      }
    }).catch(err => {
      console.error('查询支付结果失败：', err);
      this.handlePaymentFail('查询支付结果失败');
    });
  },

  /**
   * 隐藏支付结果弹窗
   */
  hidePaymentResult() {
    this.setData({
      showPaymentResult: false,
      paymentSuccess: false,
      paymentErrorMsg: ''
    });
  },

  /**
   * 跳转到订单详情
   */
  goToOrderDetail() {
    wx.redirectTo({
      url: `/pages/order-detail/order-detail?id=${this.data.orderId}`
    });
  },

  /**
   * 页面分享
   */
  onShareAppMessage() {
    return {
      title: '云打印服务',
      path: '/pages/index/index'
    };
  }
});

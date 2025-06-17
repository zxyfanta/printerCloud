// config.js
const app = getApp();

Page({
  data: {
    fileInfo: {},
    printConfig: {
      copies: 1,
      pageRangeType: 'all', // all | custom
      pageRange: '',
      isColor: false,
      isDoubleSide: false,
      paperSize: 'A4',
      remark: ''
    },
    priceConfig: {
      blackWhite: 0.1,
      color: 0.5,
      doubleSideDiscount: 0.8,
      doubleSideDiscountText: '8折',
      doubleSideDiscountPercentText: '-20%'
    },
    paperSizes: ['A4', 'A3', 'A5'],
    paperSizeIndex: 0,
    calculatedPages: 1,
    unitPrice: 0.1,
    totalAmount: 0.1,
    canCreateOrder: true
  },

  onLoad(options) {
    console.log('打印配置页面加载', options);
    this.loadLocalFileInfo();
    this.loadPriceConfig();
  },

  onReady() {
    console.log('打印配置页面渲染完成');
  },

  /**
   * 加载文件信息
   */
  loadLocalFileInfo() {
    const tempFileInfo = app.globalData.tempFileInfo;
    if (!tempFileInfo) {
      app.showError('文件信息丢失，请重新选择');
      wx.navigateBack();
      return;
    }

    if (tempFileInfo.uploaded && tempFileInfo.serverId) {
      // 从服务器获取文件信息
      this.loadServerFileInfo(tempFileInfo.serverId);
    } else {
      // 使用本地估算信息
      this.loadLocalEstimatedInfo(tempFileInfo);
    }
  },

  /**
   * 加载价格配置
   */
  loadPriceConfig() {
    app.request({
      url: '/price/public',
      method: 'GET'
    }).then(res => {
      if (res.code === 200) {
        const prices = res.data;
        const priceConfig = {
          blackWhite: 0.1,
          color: 0.5,
          doubleSideDiscount: 0.8,
          doubleSideDiscountText: '8折',
          doubleSideDiscountPercentText: '-20%'
        };

        // 解析价格配置
        prices.forEach(price => {
          switch(price.configKey) {
            case 'bw_print':
              priceConfig.blackWhite = parseFloat(price.price);
              break;
            case 'color_print':
              priceConfig.color = parseFloat(price.price);
              break;
            case 'double_side_discount':
              priceConfig.doubleSideDiscount = parseFloat(price.price);
              const discountPercent = Math.round((1 - priceConfig.doubleSideDiscount) * 100);
              priceConfig.doubleSideDiscountText = `${Math.round(priceConfig.doubleSideDiscount * 10)}折`;
              priceConfig.doubleSideDiscountPercentText = `-${discountPercent}%`;
              break;
          }
        });

        this.setData({
          priceConfig: priceConfig
        });

        // 重新计算价格
        this.calculatePrice();
      }
    }).catch(err => {
      console.error('加载价格配置失败：', err);
      // 使用默认价格配置
    });
  },

  /**
   * 从服务器加载文件信息
   */
  loadServerFileInfo(fileId) {
    app.showLoading('获取文件信息...');

    app.request({
      url: `/file/${fileId}`,
      method: 'GET'
    }).then(res => {
      app.hideLoading();

      if (res.code === 200) {
        const serverFileInfo = res.data;
        const fileInfo = {
          id: serverFileInfo.id,
          name: serverFileInfo.originalName,
          pageCount: serverFileInfo.pageCount || this.estimatePagesBySize(serverFileInfo.fileSize),
          icon: this.getFileIcon(serverFileInfo.originalName),
          size: this.formatFileSize(serverFileInfo.fileSize),
          status: serverFileInfo.status,
          parseError: serverFileInfo.parseError,
          isServerFile: true
        };

        this.setData({
          fileInfo: fileInfo,
          calculatedPages: fileInfo.pageCount
        });

        this.calculatePrice();
        this.checkCanCreateOrder();

        // 如果文件还在解析中，定时查询状态
        if (serverFileInfo.status === 2) {
          this.startPollingFileStatus(fileId);
        }

      } else {
        app.showError(res.message || '获取文件信息失败');
        this.loadLocalEstimatedInfo(app.globalData.tempFileInfo);
      }
    }).catch(err => {
      app.hideLoading();
      console.error('获取文件信息失败：', err);
      app.showError('获取文件信息失败');
      this.loadLocalEstimatedInfo(app.globalData.tempFileInfo);
    });
  },

  /**
   * 加载本地估算信息
   */
  loadLocalEstimatedInfo(tempFileInfo) {
    const estimatedPages = this.estimatePagesBySize(tempFileInfo.size);

    this.setData({
      fileInfo: {
        name: tempFileInfo.name,
        pageCount: estimatedPages,
        icon: this.getFileIcon(tempFileInfo.name),
        size: this.formatFileSize(tempFileInfo.size),
        localPath: tempFileInfo.path,
        isServerFile: false
      },
      calculatedPages: estimatedPages
    });

    this.calculatePrice();
    this.checkCanCreateOrder();
  },

  /**
   * 根据文件大小估算页数
   */
  estimatePagesBySize(fileSize) {
    if (!fileSize) return 1;

    // 根据文件大小估算页数（每页约50KB）
    return Math.max(1, Math.ceil(fileSize / (50 * 1024)));
  },

  /**
   * 定时查询文件解析状态
   */
  startPollingFileStatus(fileId) {
    const pollInterval = setInterval(() => {
      app.request({
        url: `/file/${fileId}`,
        method: 'GET'
      }).then(res => {
        if (res.code === 200) {
          const serverFileInfo = res.data;

          if (serverFileInfo.status === 3) {
            // 解析成功，更新页数
            clearInterval(pollInterval);

            this.setData({
              'fileInfo.pageCount': serverFileInfo.pageCount,
              'fileInfo.status': 3,
              calculatedPages: serverFileInfo.pageCount
            });

            this.calculatePrice();
            app.showSuccess('文件解析完成');

          } else if (serverFileInfo.status === 4) {
            // 解析失败
            clearInterval(pollInterval);

            this.setData({
              'fileInfo.status': 4,
              'fileInfo.parseError': serverFileInfo.parseError
            });

            app.showError('文件解析失败，使用估算页数');
          }
        }
      }).catch(err => {
        console.error('查询文件状态失败：', err);
      });
    }, 3000); // 每3秒查询一次

    // 30秒后停止查询
    setTimeout(() => {
      clearInterval(pollInterval);
    }, 30000);
  },





  /**
   * 修改打印份数
   */
  changeCopies(e) {
    const type = e.currentTarget.dataset.type;
    let copies = this.data.printConfig.copies;
    
    if (type === 'plus') {
      copies = Math.min(copies + 1, 999);
    } else if (type === 'minus') {
      copies = Math.max(copies - 1, 1);
    }
    
    this.setData({
      'printConfig.copies': copies
    });
    
    this.calculatePrice();
  },

  /**
   * 份数输入
   */
  onCopiesInput(e) {
    let copies = parseInt(e.detail.value) || 1;
    copies = Math.max(1, Math.min(copies, 999));
    
    this.setData({
      'printConfig.copies': copies
    });
    
    this.calculatePrice();
  },

  /**
   * 选择页数范围类型
   */
  selectPageRange(e) {
    const type = e.currentTarget.dataset.type;
    this.setData({
      'printConfig.pageRangeType': type
    });
    
    if (type === 'all') {
      this.setData({
        'printConfig.pageRange': '',
        calculatedPages: this.data.fileInfo.pageCount || 1
      });
    }
    
    this.calculatePrice();
    this.checkCanCreateOrder();
  },

  /**
   * 页数范围输入
   */
  onPageRangeInput(e) {
    const pageRange = e.detail.value;
    this.setData({
      'printConfig.pageRange': pageRange
    });
    
    // 计算实际页数
    const pages = this.calculatePagesFromRange(pageRange);
    this.setData({
      calculatedPages: pages
    });
    
    this.calculatePrice();
    this.checkCanCreateOrder();
  },

  /**
   * 选择颜色
   */
  selectColor(e) {
    const isColor = e.currentTarget.dataset.color === 'true';
    this.setData({
      'printConfig.isColor': isColor
    });
    
    this.calculatePrice();
  },

  /**
   * 选择单双面
   */
  selectSide(e) {
    const isDoubleSide = e.currentTarget.dataset.side === 'true';
    this.setData({
      'printConfig.isDoubleSide': isDoubleSide
    });
    
    this.calculatePrice();
  },

  /**
   * 纸张规格选择
   */
  onPaperSizeChange(e) {
    const index = e.detail.value;
    this.setData({
      paperSizeIndex: index,
      'printConfig.paperSize': this.data.paperSizes[index]
    });
  },

  /**
   * 备注输入
   */
  onRemarkInput(e) {
    this.setData({
      'printConfig.remark': e.detail.value
    });
  },

  /**
   * 计算页数范围
   */
  calculatePagesFromRange(rangeStr) {
    if (!rangeStr.trim()) {
      return this.data.fileInfo.pageCount || 1;
    }
    
    try {
      const ranges = rangeStr.split(',');
      let totalPages = 0;
      const maxPage = this.data.fileInfo.pageCount || 1;
      
      for (let range of ranges) {
        range = range.trim();
        if (range.includes('-')) {
          const [start, end] = range.split('-').map(n => parseInt(n.trim()));
          if (start && end && start <= end && start <= maxPage) {
            totalPages += Math.min(end, maxPage) - start + 1;
          }
        } else {
          const page = parseInt(range);
          if (page && page <= maxPage) {
            totalPages += 1;
          }
        }
      }
      
      return Math.max(totalPages, 1);
    } catch (err) {
      return 1;
    }
  },

  /**
   * 计算价格
   */
  calculatePrice() {
    const { printConfig, calculatedPages, priceConfig } = this.data;
    
    // 单价
    const unitPrice = printConfig.isColor ? priceConfig.color : priceConfig.blackWhite;
    
    // 总页数
    const totalPages = calculatedPages * printConfig.copies;
    
    // 基础金额
    let amount = totalPages * unitPrice;
    
    // 双面折扣
    if (printConfig.isDoubleSide) {
      amount *= priceConfig.doubleSideDiscount;
    }
    
    this.setData({
      unitPrice: unitPrice,
      totalAmount: amount.toFixed(2)
    });
  },

  /**
   * 检查是否可以创建订单
   */
  checkCanCreateOrder() {
    const { printConfig, calculatedPages } = this.data;
    
    let canCreate = true;
    
    // 检查自定义页数范围
    if (printConfig.pageRangeType === 'custom') {
      if (!printConfig.pageRange.trim() || calculatedPages <= 0) {
        canCreate = false;
      }
    }
    
    this.setData({
      canCreateOrder: canCreate
    });
  },

  /**
   * 创建订单
   */
  createOrder() {
    if (!this.data.canCreateOrder) {
      app.showError('请检查打印配置');
      return;
    }

    // 跳转到确认页面
    const orderData = {
      fileInfo: this.data.fileInfo,
      printConfig: this.data.printConfig,
      calculatedPages: this.data.calculatedPages,
      unitPrice: this.data.unitPrice,
      totalAmount: this.data.totalAmount
    };

    // 将订单数据存储到全局
    app.globalData.tempOrderData = orderData;

    wx.navigateTo({
      url: '/pages/confirm/confirm'
    });
  },

  /**
   * 返回上一页
   */
  goBack() {
    wx.navigateBack();
  },

  /**
   * 获取文件图标
   */
  getFileIcon(fileType) {
    const iconMap = {
      'pdf': '📄',
      'document': '📝',
      'spreadsheet': '📊',
      'image': '🖼️'
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
  }
});

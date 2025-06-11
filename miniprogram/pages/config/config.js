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
  },

  onReady() {
    console.log('打印配置页面渲染完成');
  },

  /**
   * 加载本地文件信息
   */
  loadLocalFileInfo() {
    const tempFileInfo = app.globalData.tempFileInfo;
    if (!tempFileInfo) {
      app.showError('文件信息丢失，请重新选择');
      wx.navigateBack();
      return;
    }

    // 估算页数（图片文件为1页，其他文件根据大小估算）
    let estimatedPages = 1;
    if (tempFileInfo.type === 'image') {
      estimatedPages = 1;
    } else {
      // 根据文件大小估算页数（每页约50KB）
      estimatedPages = Math.max(1, Math.ceil(tempFileInfo.size / (50 * 1024)));
    }

    this.setData({
      fileInfo: {
        name: tempFileInfo.name,
        pageCount: estimatedPages,
        icon: this.getFileIcon(tempFileInfo.type),
        size: this.formatFileSize(tempFileInfo.size),
        localPath: tempFileInfo.path
      },
      calculatedPages: estimatedPages
    });

    this.calculatePrice();
    this.checkCanCreateOrder();
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

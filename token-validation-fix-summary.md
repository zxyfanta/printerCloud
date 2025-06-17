# Token验证逻辑修复总结

## 问题分析

### 🔍 **发现的问题**

1. **文件上传前token验证不完整**：
   - `upload.js` 的 `uploadFileToServer` 方法只检查 `app.globalData.isLogin` 状态
   - 没有调用 `app.validateToken()` 验证token的实际有效性
   - 导致token过期但 `isLogin` 仍为true时，直接上传失败

2. **401错误处理不完善**：
   - `wx.uploadFile` 的错误处理中没有检查401状态码
   - 文件信息轮询时401错误没有触发重新登录
   - `app.uploadFile` 方法缺少401错误处理

3. **订单创建时的重复验证缺失**：
   - `upload-server.js` 的 `createOrder` 方法没有再次验证token
   - 从开始上传到创建订单可能有时间间隔，token可能在此期间过期

## 修复内容

### 1. 修复文件上传前的token验证

**文件**: `miniprogram/pages/upload/upload.js`

#### 修复前：
```javascript
uploadFileToServer(fileInfo) {
  // 检查登录状态
  if (!app.globalData.isLogin) {
    app.showError('请先登录');
    return;
  }
  // 直接开始上传...
}
```

#### 修复后：
```javascript
uploadFileToServer(fileInfo) {
  // 先验证token是否有效
  app.validateToken().then(isValid => {
    if (!isValid) {
      // token无效，跳转到登录页面
      wx.navigateTo({
        url: '/pages/login/login?redirect=upload'
      });
      return;
    }
    
    // token有效，开始上传
    this.performFileUpload(fileInfo);
  }).catch(err => {
    console.error('Token验证失败:', err);
    app.showError('登录验证失败，请重新登录');
    wx.navigateTo({
      url: '/pages/login/login?redirect=upload'
    });
  });
}
```

### 2. 增强文件上传的401错误处理

**文件**: `miniprogram/pages/upload/upload.js`

#### 新增401状态码处理：
```javascript
success: (res) => {
  if (res.statusCode === 200) {
    // 上传成功处理...
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
}
```

### 3. 修复文件信息轮询的401错误处理

**文件**: `miniprogram/pages/upload/upload.js`

#### 在轮询过程中处理401错误：
```javascript
}).catch(err => {
  console.error('获取文件信息失败：', err);
  // 如果是401错误，说明token已过期
  if (err.message && err.message.includes('401')) {
    clearInterval(pollingTimer);
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
  }
});
```

### 4. 修复订单创建时的token验证

**文件**: `miniprogram/pages/upload-server/upload-server.js`

#### 修复前：
```javascript
createOrder() {
  // 检查登录状态
  if (!app.globalData.isLogin || !app.globalData.userInfo) {
    wx.navigateTo({
      url: '/pages/login/login'
    });
    return;
  }
  // 直接创建订单...
}
```

#### 修复后：
```javascript
createOrder() {
  return new Promise((resolve, reject) => {
    // 再次验证token（因为从开始上传到创建订单可能有时间间隔）
    app.validateToken().then(isValid => {
      if (!isValid) {
        // token无效，跳转到登录页面
        wx.navigateTo({
          url: '/pages/login/login?redirect=upload-server'
        });
        reject(new Error('登录已过期，请重新登录'));
        return;
      }
      
      // token有效，继续创建订单
      this.performCreateOrder(resolve, reject);
    }).catch(err => {
      console.error('Token验证失败:', err);
      reject(new Error('登录验证失败，请重新登录'));
    });
  });
}
```

### 5. 增强app.js中uploadFile方法的401处理

**文件**: `miniprogram/app.js`

#### 新增401错误处理：
```javascript
success: (res) => {
  if (res.statusCode === 200) {
    const data = JSON.parse(res.data);
    resolve(data);
  } else if (res.statusCode === 401) {
    // 未授权，token可能已过期
    this.logout();
    reject(new Error('登录已过期，请重新登录'));
  } else {
    reject(new Error(`上传失败：${res.statusCode}`));
  }
}
```

## 修复后的完整流程

### 📋 **文件上传流程**

1. **用户选择文件** → `handleFileSelected` 验证token
2. **开始上传** → `uploadFileToServer` 再次验证token
3. **执行上传** → `performFileUpload` 处理401错误
4. **轮询文件信息** → `getFileInfo` 处理401错误

### 📋 **订单创建流程**

1. **开始上传** → `startUpload` 验证token
2. **创建订单** → `createOrder` 再次验证token
3. **执行创建** → `performCreateOrder` 处理API错误
4. **错误处理** → 识别401错误并触发重新登录

## 验证要点

### ✅ **现在的token验证机制**

1. **多层验证**：
   - 文件选择时验证
   - 文件上传前验证
   - 订单创建前验证

2. **完善的401处理**：
   - 文件上传401错误
   - API请求401错误
   - 轮询过程401错误

3. **自动登录跳转**：
   - 清除过期的登录状态
   - 跳转到登录页面
   - 保存重定向路径

4. **用户友好的错误提示**：
   - 明确的错误信息
   - 引导用户重新登录
   - 保持操作连续性

## 测试建议

### 🧪 **测试场景**

1. **正常流程测试**：
   - 登录后立即上传文件
   - 验证整个流程是否正常

2. **Token过期测试**：
   - 登录后等待token过期
   - 尝试上传文件，验证是否自动跳转登录

3. **网络异常测试**：
   - 模拟网络中断
   - 验证错误处理是否正确

4. **并发操作测试**：
   - 同时进行多个文件操作
   - 验证token验证的一致性

## 注意事项

1. **重定向参数**：所有登录跳转都包含了 `redirect` 参数，确保登录后能返回原页面
2. **状态清理**：401错误时会调用 `app.logout()` 清理本地状态
3. **用户体验**：使用模态框而不是toast提示重要的登录过期信息
4. **错误日志**：保留详细的错误日志便于调试

## 后续优化建议

1. **Token自动刷新**：考虑实现token自动刷新机制
2. **离线缓存**：实现文件上传的离线缓存和重试机制
3. **进度保存**：保存上传进度，登录后可以继续
4. **批量操作**：支持批量文件上传的token验证

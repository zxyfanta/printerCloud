# 云打印小程序环境配置说明

## 环境配置系统

云打印小程序实现了一个环境配置系统，可以根据不同的运行环境自动切换API地址等配置，解决开发环境和生产环境配置不同的问题。

### 配置说明

在 `app.js` 文件中，我们定义了环境配置系统：

```javascript
globalData: {
  // 环境配置
  env: {
    dev: {
      baseUrl: 'http://localhost:8082/api',
      ossBaseUrl: ''
    },
    prod: {
      baseUrl: 'https://your-domain.com/api', // 生产环境需要替换为实际的HTTPS域名
      ossBaseUrl: 'https://your-domain.com/files' // 生产环境文件访问地址
    }
  },
  // 当前环境：dev-开发环境，prod-生产环境
  currentEnv: 'dev',
  // API基础地址
  get baseUrl() {
    return this.env[this.currentEnv].baseUrl;
  },
  // OSS文件访问地址
  get ossBaseUrl() {
    return this.env[this.currentEnv].ossBaseUrl;
  }
}
```

### 环境自动切换

小程序会根据运行环境自动切换配置：

- 开发版、体验版：使用开发环境配置（dev）
- 正式版：使用生产环境配置（prod）

自动切换逻辑在 `initEnvConfig()` 方法中实现：

```javascript
initEnvConfig() {
  try {
    // 尝试从本地存储获取环境配置
    const savedEnv = wx.getStorageSync('currentEnv');
    if (savedEnv && (savedEnv === 'dev' || savedEnv === 'prod')) {
      this.globalData.currentEnv = savedEnv;
    }

    // 根据小程序版本自动切换环境
    const accountInfo = wx.getAccountInfoSync();
    if (accountInfo && accountInfo.miniProgram) {
      const envVersion = accountInfo.miniProgram.envVersion;
      
      if (envVersion === 'release') {
        // 正式版使用生产环境
        this.globalData.currentEnv = 'prod';
      } else {
        // 开发版、体验版使用开发环境
        this.globalData.currentEnv = 'dev';
      }

      // 保存当前环境到本地存储
      wx.setStorageSync('currentEnv', this.globalData.currentEnv);
    }

    console.log('当前环境：', this.globalData.currentEnv);
    console.log('API地址：', this.globalData.baseUrl);
  } catch (error) {
    console.error('初始化环境配置失败：', error);
  }
}
```

## 使用说明

### 开发环境

在开发环境中，API地址默认为 `http://localhost:8082/api`。开发者可以根据实际情况修改 `app.js` 中的配置。

### 生产环境

在发布小程序正式版前，需要修改 `app.js` 中的生产环境配置，将 `baseUrl` 和 `ossBaseUrl` 替换为实际的HTTPS域名：

```javascript
prod: {
  baseUrl: 'https://your-domain.com/api', // 替换为实际的HTTPS域名
  ossBaseUrl: 'https://your-domain.com/files' // 替换为实际的文件访问地址
}
```

### 手动切换环境

如果需要在开发过程中手动切换环境，可以在小程序启动前修改本地存储中的环境配置：

```javascript
wx.setStorageSync('currentEnv', 'prod'); // 切换到生产环境
```

或者直接修改 `app.js` 中的默认环境：

```javascript
currentEnv: 'prod', // 修改为 'prod' 使用生产环境
```

## 域名配置

### 开发环境

在微信开发者工具中，可以勾选"不校验合法域名"选项，避免开发过程中的域名限制。

### 生产环境

在发布小程序正式版前，需要在微信公众平台配置合法域名：

1. 登录微信公众平台 (https://mp.weixin.qq.com)
2. 进入"开发" -> "开发管理" -> "开发设置"
3. 在"服务器域名"中配置：
   - request合法域名
   - uploadFile合法域名
   - downloadFile合法域名

**注意事项：**
- 域名必须是HTTPS协议
- 域名需要备案
- 每月只能修改5次域名配置

## 常见问题

### 1. 域名校验失败

如果出现 `url not in domain list` 错误，可能是因为：
- 开发环境中未勾选"不校验合法域名"
- 生产环境中未配置合法域名
- API地址不是HTTPS协议

### 2. 环境配置不生效

如果环境配置不生效，可以尝试：
- 清除本地存储：`wx.clearStorageSync()`
- 重新编译小程序
- 检查 `initEnvConfig()` 方法是否正确调用

### 3. 如何添加新的环境配置

如果需要添加测试环境等其他环境，可以在 `env` 对象中添加新的配置：

```javascript
env: {
  dev: { ... },
  test: {
    baseUrl: 'https://test-api.your-domain.com',
    ossBaseUrl: 'https://test-files.your-domain.com'
  },
  prod: { ... }
}
```

然后修改 `initEnvConfig()` 方法中的环境切换逻辑。
# 微信登录实现指南

## 概述

本项目已实现微信小程序登录功能，用户在使用打印服务时会自动引导进行微信登录，确保订单与用户的openId绑定。

## 登录时机

### 1. 主动登录时机
- **首页点击"开始打印"**: 用户点击开始打印按钮时，如果未登录会弹出登录提示
- **订单提交时**: 在upload-server页面提交订单时，会检查登录状态
- **查看订单**: 访问orders页面时会检查登录状态
- **个人中心**: 访问profile页面时会检查登录状态

### 2. 自动登录时机
- **小程序启动**: app.js中会自动检查登录状态并尝试静默登录
- **首页显示**: 如果已登录会自动加载用户相关数据

## 技术实现

### 前端实现 (小程序)

#### 1. 全局登录管理 (app.js)
```javascript
// 微信登录方法
wxLogin() {
  return new Promise((resolve, reject) => {
    wx.login({
      success: (res) => {
        if (res.code) {
          // 发送code到后台换取用户信息
          this.request({
            url: '/auth/login',
            method: 'POST',
            data: {
              code: res.code,
              loginType: 'WECHAT'
            }
          }).then(result => {
            if (result.code === 200) {
              this.globalData.token = result.data.token;
              this.globalData.userInfo = result.data.user;
              this.globalData.isLogin = true;
              
              // 保存到本地存储
              wx.setStorageSync('token', result.data.token);
              wx.setStorageSync('userInfo', result.data.user);
              
              resolve(result.data);
            } else {
              reject(new Error(result.message));
            }
          }).catch(reject);
        }
      },
      fail: reject
    });
  });
}
```

#### 2. 页面级登录检查
各个页面都实现了登录状态检查：
- `index.js`: startPrint()方法
- `upload-server.js`: createOrder()方法
- `orders.js`: checkLoginAndLoadOrders()方法
- `profile.js`: checkLoginAndLoadProfile()方法

### 后端实现 (Spring Boot)

#### 1. 认证控制器 (AuthController.java)
```java
@PostMapping("/login")
public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
    if ("WECHAT".equals(request.getLoginType())) {
        User user = userService.wechatLogin(request.getCode());
        if (user != null) {
            String token = jwtUtil.generateToken(user.getId(), 
                user.getUsername() != null ? user.getUsername() : user.getOpenId(), 
                user.getRole());
            response.put("data", new LoginResponse(token, user));
            return ResponseEntity.ok(response);
        }
    }
}
```

#### 2. 用户服务 (UserService.java)
```java
public User wechatLogin(String code) {
    // 调用微信API获取用户信息
    WechatUserInfo wechatUserInfo = getWechatUserInfo(code);
    String openId = wechatUserInfo.getOpenId();
    
    // 查找或创建用户
    Optional<User> userOpt = userRepository.findByOpenId(openId);
    if (userOpt.isPresent()) {
        // 更新现有用户信息
        return updateExistingUser(userOpt.get(), wechatUserInfo);
    } else {
        // 创建新用户
        return createNewUser(wechatUserInfo);
    }
}
```

## 数据库设计

### User表字段
- `id`: 用户ID (主键)
- `open_id`: 微信openId (唯一索引)
- `union_id`: 微信unionId
- `nickname`: 用户昵称
- `avatar_url`: 头像URL
- `role`: 用户角色 (USER/ADMIN)
- `status`: 用户状态
- `create_time`: 创建时间
- `update_time`: 更新时间

### PrintOrder表关联
- `user_id`: 关联User表的id字段
- `user_name`: 冗余存储用户名称

## 配置说明

### 微信小程序配置

1. **开发环境**: 当前使用模拟登录，生成mock_openid
2. **生产环境**: 需要配置真实的微信API调用

### 后端配置

需要在`UserService.java`中配置微信小程序的appId和secret：

```java
private WechatUserInfo getWechatUserInfo(String code) {
    String appId = "your_wechat_appid";  // 替换为实际的appId
    String secret = "your_wechat_secret"; // 替换为实际的secret
    String url = String.format(
        "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
        appId, secret, code);
    
    // 发送HTTP请求获取openid和session_key
    // 解析响应并返回用户信息
}
```

## 用户体验优化

### 1. 渐进式登录
- 用户可以浏览首页而无需登录
- 只有在需要使用核心功能时才要求登录
- 登录提示友好，说明登录的必要性

### 2. 登录状态保持
- 使用JWT token保持登录状态
- 本地存储用户信息，避免重复登录
- 自动检查token有效性

### 3. 错误处理
- 网络错误时提供重试选项
- 登录失败时给出明确的错误提示
- 提供返回首页的选项

## 安全考虑

### 1. Token管理
- 使用JWT token进行身份验证
- Token包含用户ID、用户名和角色信息
- 后端验证token有效性

### 2. 数据验证
- 后端验证所有用户输入
- 订单创建时验证用户身份
- 防止用户访问他人的订单数据

### 3. 隐私保护
- 只存储必要的用户信息
- 敏感信息不在前端存储
- 遵循微信小程序隐私规范

## 测试说明

### 开发环境测试
1. 启动后端服务
2. 在小程序开发工具中运行前端
3. 点击"开始打印"测试登录流程
4. 检查控制台日志确认登录成功
5. 验证订单创建时用户信息正确绑定

### 生产环境部署
1. 配置真实的微信appId和secret
2. 实现真实的微信API调用
3. 配置HTTPS域名
4. 测试完整的登录和订单流程

## 后续优化建议

1. **实现真实微信API**: 替换模拟登录为真实的微信API调用
2. **添加用户授权**: 获取用户昵称和头像需要用户授权
3. **优化登录体验**: 添加登录动画和更好的UI反馈
4. **错误监控**: 添加登录失败的监控和统计
5. **多端同步**: 如果有其他端(如Web端)，考虑登录状态同步
# WebSocket连接问题修复总结

## 问题诊断

### 🔍 **原始错误**
```
Access to XMLHttpRequest at 'http://localhost:8082/api/ws/info?t=1749704519474' from origin 'http://localhost:3000' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.

GET http://localhost:8082/api/ws/info?t=1749704519474 net::ERR_FAILED 404 (Not Found)
```

### 🎯 **根本原因**
1. **WebSocket端点路径不匹配**：
   - 前端尝试连接：`http://localhost:8082/api/ws`
   - 后端配置的端点：`/ws`（没有`/api`前缀）
   - 由于移除了`context-path`，需要手动添加`/api`前缀

2. **CORS配置问题**：
   - WebSocket端点需要正确的跨域配置

## 修复方案

### 1. **更新WebSocket端点配置**

#### 修改WebSocketConfig.java
```java
@Override
public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
    // 修改前: registry.addEndpoint("/ws")
    // 修改后: registry.addEndpoint("/api/ws")
    registry.addEndpoint("/api/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS()
            .setHeartbeatTime(25000)
            .setDisconnectDelay(5000)
            .setStreamBytesLimit(128 * 1024)
            .setHttpMessageCacheSize(1000)
            .setSessionCookieNeeded(false);
}
```

### 2. **前端配置验证**

#### WebSocket服务配置正确
```javascript
getServerUrl() {
    // 开发环境
    if (import.meta.env.DEV) {
        return 'http://localhost:8082/api/ws'  // ✅ 正确
    }
    // 生产环境配置
    const protocol = window.location.protocol === 'https:' ? 'https:' : 'http:'
    const host = window.location.host
    return `${protocol}//${host}/api/ws`
}
```

### 3. **添加WebSocket测试功能**

#### 创建WebSocketTestController
- 提供测试端点：`/api/websocket/test-notification`
- 提供状态检查：`/api/websocket/status`
- 支持自定义消息发送

#### 创建测试页面
- `websocket-test.html` - 独立的WebSocket连接测试页面
- 支持连接测试、消息发送、日志查看

## 修复的文件列表

### ✅ **后端文件**
- `WebSocketConfig.java` - 更新端点路径为 `/api/ws`
- `WebSocketTestController.java` - 新增测试控制器

### ✅ **前端文件**
- `websocket.js` - 配置已正确，无需修改

### ✅ **测试文件**
- `websocket-test.html` - 新增独立测试页面

## WebSocket架构

### 📡 **端点和主题**
```
连接端点: /api/ws

消息主题:
├── /topic/newOrders      - 新订单通知
├── /topic/orderUpdates   - 订单状态更新
├── /topic/system         - 系统通知
└── /topic/test          - 测试消息

应用端点:
├── /app/test            - 测试消息处理
└── /app/*               - 其他应用消息
```

### 🔄 **消息流程**
1. **前端连接**：`SockJS` → `/api/ws`
2. **订阅主题**：监听各种通知
3. **接收消息**：实时显示通知
4. **发送消息**：通过STOMP协议

## 测试验证

### 🧪 **测试步骤**

#### 1. 启动后端服务
```bash
cd backend
./start-server.sh
```

#### 2. 验证WebSocket端点
```bash
curl http://localhost:8082/api/websocket/status
```

#### 3. 使用测试页面
```bash
# 在浏览器中打开
open websocket-test.html
```

#### 4. 测试连接流程
1. 点击"连接"按钮
2. 观察连接状态变化
3. 发送测试消息
4. 检查日志输出

#### 5. 启动前端验证
```bash
cd admin
pnpm dev
```

### 🎯 **预期结果**
- ✅ WebSocket连接成功建立
- ✅ 前端不再显示连接错误
- ✅ 实时通知功能正常工作
- ✅ 测试页面显示连接状态为"已连接"

## API端点总结

### 🌐 **WebSocket相关端点**
```
WebSocket连接:
└── /api/ws                           - WebSocket连接端点

REST API:
├── /api/websocket/status             - 获取WebSocket状态
├── /api/websocket/test-notification  - 发送测试通知
└── /api/websocket/send-message       - 发送自定义消息
```

### 📨 **消息类型**
```javascript
// 新订单通知
{
  "type": "NEW_ORDER",
  "title": "新订单通知",
  "message": "收到新订单：PC20241201...",
  "orderId": 123,
  "orderNo": "PC20241201...",
  "amount": 5.60,
  "fileName": "文档.pdf"
}

// 订单状态更新
{
  "type": "ORDER_STATUS_UPDATE", 
  "title": "订单状态更新",
  "message": "订单 PC20241201... 状态已更新为：已完成",
  "orderId": 123,
  "status": 3,
  "statusText": "已完成"
}

// 系统通知
{
  "type": "SYSTEM",
  "title": "系统通知",
  "message": "系统维护通知",
  "notificationType": "INFO"
}
```

## 故障排除

### 🔧 **常见问题**
1. **404错误**：检查端点路径是否为`/api/ws`
2. **CORS错误**：确认`setAllowedOriginPatterns("*")`配置
3. **连接超时**：检查后端服务是否正常运行
4. **消息接收失败**：验证主题订阅是否正确

### 📝 **调试技巧**
1. 使用`websocket-test.html`进行独立测试
2. 检查浏览器开发者工具的WebSocket连接
3. 查看后端日志中的WebSocket相关信息
4. 使用`/api/websocket/status`检查服务状态

## 下一步

1. ✅ 重启后端服务以应用WebSocket配置更改
2. ✅ 使用测试页面验证连接
3. ✅ 启动前端验证实时通知功能
4. ✅ 测试订单创建时的实时通知

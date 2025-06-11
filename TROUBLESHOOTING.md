# 故障排除指南

## 问题1：Element Plus Radio组件警告

### 问题描述
```
ElementPlusError: [el-radio] [API] label act as value is about to be deprecated in version 3.0.0, please use value instead.
```

### 解决方案
✅ **已修复** - 将所有`el-radio`和`el-radio-button`组件的`label`属性改为`value`属性。

修复的文件：
- `admin/src/views/Orders.vue`
- `admin/src/views/orders/OrdersBase.vue`
- `admin/src/views/Settings.vue`

### 修复前后对比
```vue
<!-- 修复前 -->
<el-radio label="card">卡片</el-radio>
<el-radio-button label="table">表格</el-radio-button>

<!-- 修复后 -->
<el-radio value="card">卡片</el-radio>
<el-radio-button value="table">表格</el-radio-button>
```

## 问题2：API请求500错误

### 问题描述
```
GET http://localhost:3000/api/orders?page=1&size=20&search=&status=1&sortBy=createTime_desc 500 (Internal Server Error)
```

### 原因分析
1. 后端服务未启动
2. Maven配置问题
3. 依赖缺失

### 解决方案

#### 方法1：使用简化启动脚本
```bash
./start-backend-simple.bat
```

#### 方法2：手动启动
1. **检查Java环境**
   ```bash
   java -version
   ```
   确保Java 8或更高版本已安装

2. **安装Maven**
   - 下载：https://maven.apache.org/download.cgi
   - 配置环境变量：`MAVEN_HOME` 和 `PATH`
   - 验证：`mvn -version`

3. **编译项目**
   ```bash
   cd backend
   mvn clean compile
   ```

4. **启动服务**
   ```bash
   mvn spring-boot:run
   ```

#### 方法3：使用IDE
1. **IntelliJ IDEA**
   - 导入backend项目
   - 等待Maven依赖下载完成
   - 运行`PrinterCloudApplication.java`

2. **Eclipse**
   - 导入Maven项目
   - 右键项目 → Run As → Java Application
   - 选择`PrinterCloudApplication`

#### 方法4：检查端口占用
```bash
# 检查8080端口是否被占用
netstat -an | findstr :8080

# 如果被占用，杀死进程或更改端口
```

### 验证服务启动
1. **健康检查**
   ```
   http://localhost:8080/api/health
   ```

2. **WebSocket测试**
   打开 `admin/websocket-test.html`

3. **API测试**
   ```
   http://localhost:8080/api/orders
   ```

## 问题3：Maven相关问题

### 问题：Maven命令不识别
**解决方案**：
1. 下载Maven：https://maven.apache.org/download.cgi
2. 解压到目录（如：`C:\apache-maven-3.9.5`）
3. 设置环境变量：
   - `MAVEN_HOME`: `C:\apache-maven-3.9.5`
   - `PATH`: 添加 `%MAVEN_HOME%\bin`
4. 重启命令行，验证：`mvn -version`

### 问题：Maven依赖下载失败
**解决方案**：
1. 配置阿里云镜像（已在项目中配置）
2. 清理本地仓库：`mvn clean`
3. 重新下载：`mvn dependency:resolve`

### 问题：Spring Boot插件找不到
**解决方案**：
✅ **已修复** - 更新了`pom.xml`中的Spring Boot Maven插件配置

## 问题4：WebSocket连接问题

### 问题描述
WebSocket连接失败，无法建立实时通信

### 解决方案
✅ **已修复** - 使用稳定的第三方库：
- 前端：`@stomp/stompjs` + `sockjs-client`
- 后端：Spring WebSocket + STOMP

详细信息请参考：`WEBSOCKET_FIX_README.md`

## 问题5：前端代理配置

### 问题描述
前端请求后端API时出现跨域或代理问题

### 解决方案
检查`admin/vite.config.js`中的代理配置：
```javascript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      secure: false
    }
  }
}
```

## 完整启动流程

### 1. 启动后端服务
```bash
# 方法1：使用脚本
./start-backend-simple.bat

# 方法2：手动启动
cd backend
mvn clean compile
mvn spring-boot:run
```

### 2. 验证后端服务
访问：http://localhost:8080/api/health

### 3. 启动前端服务
```bash
cd admin
npm install  # 首次运行
npm run dev
```

### 4. 访问管理后台
浏览器打开：http://localhost:3000

### 5. 测试WebSocket
打开：`admin/websocket-test.html`

## 常见错误代码

| 错误代码 | 描述 | 解决方案 |
|---------|------|----------|
| 500 | 服务器内部错误 | 检查后端服务是否启动 |
| 404 | 接口不存在 | 检查API路径和后端路由 |
| 403 | 权限不足 | 检查登录状态和权限 |
| ECONNREFUSED | 连接被拒绝 | 检查服务是否启动和端口 |

## 获取帮助

如果以上方法都无法解决问题，请：

1. 检查控制台错误信息
2. 查看后端日志
3. 确认环境配置
4. 重启服务和浏览器

## 开发环境要求

- **Java**: 8或更高版本
- **Maven**: 3.6或更高版本
- **Node.js**: 16或更高版本
- **浏览器**: Chrome/Firefox/Edge最新版本

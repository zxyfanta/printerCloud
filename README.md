# 云打印小程序系统

## 项目简介
基于微信小程序和Java Spring Boot的云打印业务系统，支持文件上传、在线预览、打印配置、微信支付等功能。

## 技术栈

### 前端（微信小程序）
- 原生微信小程序
- 微信支付API
- 文件上传和预览

### 后端（Java）
- Spring Boot 2.7+
- MyBatis Plus
- MySQL 8.0
- Redis
- 微信支付SDK
- 阿里云OSS/腾讯云COS

## 项目结构
```
printerCloud/
├── miniprogram/          # 微信小程序前端
├── backend/             # Java后端
├── docs/               # 项目文档
└── README.md
```

## 核心功能
1. 文件上传和预览（支持PDF、Word、Excel、图片等）
2. 打印参数配置（份数、页数、彩色/黑白、单双面等）
3. 微信支付集成
4. 订单管理和历史查询
5. 验证码系统
6. 退款功能
7. 用户管理

## 快速开始

### 环境要求
- Java 8+
- Maven 3.6+
- MySQL 8.0
- Redis
- 微信开发者工具

### 后端启动
1. **配置数据库**
   ```bash
   # 创建数据库
   mysql -u root -p < docs/database.sql
   ```

2. **修改配置文件**
   编辑 `backend/src/main/resources/application.yml`：
   - 数据库连接信息
   - Redis连接信息
   - 微信小程序配置
   - 阿里云OSS配置

3. **启动后端服务**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

### 小程序开发
1. 使用微信开发者工具打开 `miniprogram` 目录
2. 在 `app.js` 中配置后端API地址
3. 配置小程序AppID
4. 启动开发调试

### 一键启动（推荐）
```bash
chmod +x start.sh
./start.sh
```

## 开发进度
- [x] 项目初始化
- [x] 后端基础框架搭建
- [x] 数据库设计
- [x] 基础实体类和枚举
- [x] 小程序基础框架
- [x] 首页和上传页面
- [x] 订单列表页面
- [x] 打印配置页面
- [x] 支付页面
- [x] 个人中心页面
- [x] 用户管理模块框架
- [x] 文件管理模块框架
- [x] 订单管理模块框架
- [ ] 支付模块完整实现
- [ ] 文件解析和预览功能
- [ ] 微信支付集成
- [ ] 系统测试
- [ ] 部署上线

## 已完成功能

### 后端 (Java Spring Boot)
1. **项目基础架构**
   - Spring Boot 2.7.14 + MyBatis Plus + MySQL
   - 统一响应格式 (Result类)
   - 自动填充配置
   - 健康检查接口

2. **数据库设计**
   - 用户表 (pc_user)
   - 打印文件表 (pc_print_file)
   - 打印订单表 (pc_print_order)
   - 支付记录表 (pc_payment_record)

3. **核心实体类**
   - User (用户实体)
   - PrintFile (打印文件实体)
   - PrintOrder (打印订单实体)
   - 相关枚举类 (OrderStatusEnum, FileStatusEnum)

### 前端 (微信小程序)
1. **基础框架**
   - 原生微信小程序
   - 全局配置和样式
   - 网络请求封装
   - 微信登录集成

2. **已完成页面**
   - 首页 (index) - 功能入口和最近订单
   - 文件上传页 (upload) - 支持多种文件格式上传
   - 订单列表页 (orders) - 订单筛选和管理

3. **核心功能**
   - 文件选择和上传
   - 打印参数配置
   - 订单状态管理
   - 微信支付集成
   - 用户界面交互

## 配置说明

### 必需配置项

#### 1. 数据库配置
在 `backend/src/main/resources/application.yml` 中配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/printer_cloud
    username: your_username
    password: your_password
```

#### 2. 微信小程序配置
```yaml
printer-cloud:
  wechat:
    app-id: your-wechat-app-id
    app-secret: your-wechat-app-secret
```

#### 3. 微信支付配置
```yaml
printer-cloud:
  wechat-pay:
    mch-id: your-mch-id
    api-key: your-api-key
    cert-path: classpath:cert/apiclient_cert.p12
```

#### 4. 文件存储配置
```yaml
printer-cloud:
  oss:
    endpoint: https://oss-cn-hangzhou.aliyuncs.com
    access-key-id: your-access-key-id
    access-key-secret: your-access-key-secret
    bucket-name: printer-cloud-files
```

### 小程序配置
在 `miniprogram/app.js` 中配置：
```javascript
globalData: {
  baseUrl: 'http://localhost:8080/api', // 后端API地址
}
```

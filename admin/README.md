# 云打印管理后台

基于Vue3 + Element Plus的云打印小程序管理后台系统。

## 技术栈

- **前端框架**: Vue 3
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由管理**: Vue Router 4
- **HTTP客户端**: Axios
- **构建工具**: Vite
- **样式预处理**: Sass

## 功能特性

### 🔐 认证系统
- 管理员登录
- JWT Token认证
- 路由守卫

### 📊 仪表盘
- 数据统计概览
- 最近文件列表
- 最近订单列表

### 📁 文件管理
- 文件列表查看
- 文件搜索
- 文件下载
- 文件删除

### 📋 订单管理
- 订单列表查看
- 订单状态筛选
- 订单详情查看
- 订单操作（完成、取消）

### 👥 用户管理
- 用户列表查看
- 用户状态管理
- 密码重置

## 快速开始

### 环境要求
- Node.js 16+
- npm 或 yarn

### 安装依赖
```bash
npm install
# 或
yarn install
```

### 开发模式
```bash
npm run dev
# 或
yarn dev
```

访问 http://localhost:3000

### 构建生产版本
```bash
npm run build
# 或
yarn build
```

### 预览生产版本
```bash
npm run preview
# 或
yarn preview
```

## 项目结构

```
admin/
├── public/                 # 静态资源
├── src/
│   ├── components/         # 公共组件
│   ├── layout/            # 布局组件
│   ├── router/            # 路由配置
│   ├── stores/            # Pinia状态管理
│   ├── utils/             # 工具函数
│   ├── views/             # 页面组件
│   ├── App.vue            # 根组件
│   └── main.js            # 入口文件
├── index.html             # HTML模板
├── package.json           # 项目配置
├── vite.config.js         # Vite配置
└── README.md              # 项目说明
```

## 默认账户

- **管理员**: admin / admin123
- **超级管理员**: superadmin / super123

## API接口

管理后台通过代理访问后端API，所有请求都会转发到 `http://localhost:8080/api`

主要接口：
- `POST /auth/login` - 管理员登录
- `GET /auth/userinfo` - 获取用户信息
- `GET /file/list` - 获取文件列表
- `GET /file/download/{id}` - 下载文件
- `DELETE /file/{id}` - 删除文件
- `GET /order/list` - 获取订单列表
- `POST /order/complete` - 完成订单

## 开发说明

### 添加新页面
1. 在 `src/views/` 目录下创建新的Vue组件
2. 在 `src/router/index.js` 中添加路由配置
3. 在布局组件中添加菜单项

### 状态管理
使用Pinia进行状态管理，主要store：
- `auth.js` - 认证相关状态

### 样式规范
- 使用Element Plus的设计规范
- 响应式设计，支持不同屏幕尺寸
- 统一的颜色和间距

## 部署

### 开发环境
确保后端服务运行在 `http://localhost:8080`

### 生产环境
1. 修改 `vite.config.js` 中的代理配置
2. 构建项目：`npm run build`
3. 将 `dist` 目录部署到Web服务器

## 注意事项

1. 确保后端服务已启动并运行在正确端口
2. 管理后台需要管理员权限才能访问
3. 文件下载功能需要后端支持CORS
4. 生产环境请修改默认密码

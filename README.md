# PrinterCloud 云打印管理系统

## 项目结构

- `backend/` - 后端API服务 (Spring Boot)
- `java-admin/` - 管理员GUI应用 (Java Swing)
- `miniprogram/` - 微信小程序前端

## 运行说明

### 方式一：使用启动脚本（推荐）

1. 双击运行 `start-all.bat` 脚本
2. 脚本会自动启动后端API服务和管理员GUI应用
3. 等待服务启动完成

### 方式二：手动启动

#### 1. 启动后端API服务

```bash
cd backend
mvn spring-boot:run
```

后端服务将在 http://localhost:8082 启动

#### 2. 启动管理员GUI应用

```bash
cd java-admin
mvn exec:java -Dexec.mainClass="com.printercloud.admin.SwingAdminApp"
```

或者使用Spring Boot插件：

```bash
cd java-admin
mvn spring-boot:run
```

### 方式三：使用PowerShell脚本

```powershell
cd java-admin
.\run.ps1
```

## 默认登录信息

- 管理员账号：`admin` / `admin123`
- 超级管理员：`superadmin` / `super123`

## 常见问题解决

### 1. Maven命令超时

如果遇到Maven命令执行超时，可以尝试：

1. 使用IDE（如IntelliJ IDEA或Eclipse）直接运行主类
2. 先编译项目：`mvn clean compile`
3. 然后使用java命令运行（需要先下载依赖）

### 2. 后端API连接失败

确保后端服务已启动并在8082端口运行：
- 检查 http://localhost:8082/h2-console 是否可访问
- 查看后端控制台日志是否有错误

### 3. GUI应用无法启动

1. 确保Java 17已安装
2. 确保后端API服务已启动
3. 检查防火墙设置
4. 查看控制台错误日志

### 4. 依赖下载问题

如果Maven依赖下载缓慢或失败：

1. 配置国内Maven镜像源
2. 清理本地仓库：`mvn clean`
3. 重新下载依赖：`mvn dependency:resolve`

## 开发环境要求

- Java 17+
- Maven 3.6+
- 网络连接（用于下载依赖）

## 技术栈

### 后端
- Spring Boot 3.2.0
- Spring Data JPA
- H2 Database
- JWT认证

### 管理员GUI
- Java Swing
- FlatLaf UI主题
- MigLayout布局管理器
- JFreeChart图表组件
- Spring Boot集成

### 微信小程序
- 微信小程序原生开发
- WeUI组件库
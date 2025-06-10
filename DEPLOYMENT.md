# 云打印小程序系统部署指南

## 🏗️ 部署架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   小程序前端     │    │   管理后台       │    │   后端服务       │
│   (微信小程序)   │    │   (Vue3 + Vite) │    │ (Spring Boot)   │
│   Port: -       │    │   Port: 3000    │    │   Port: 8080    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │   数据库服务     │
                    │ (MySQL/H2)     │
                    │   Port: 3306   │
                    └─────────────────┘
```

## 🚀 开发环境部署

### 环境要求
- Java 8+
- Maven 3.6+
- Node.js 16+
- npm 或 yarn

### 快速启动
```bash
# 克隆项目
git clone <repository-url>
cd printerCloud

# 一键启动
chmod +x start-all.sh
./start-all.sh
```

### 分步启动

#### 1. 启动后端服务
```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

#### 2. 启动管理后台
```bash
cd admin
npm install
npm run dev
```

#### 3. 配置小程序
使用微信开发者工具打开 `miniprogram` 目录

## 🌐 生产环境部署

### 1. 后端服务部署

#### 使用Docker部署
```dockerfile
# Dockerfile
FROM openjdk:8-jre-alpine
COPY target/printer-cloud-backend.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
# 构建镜像
cd backend
mvn clean package
docker build -t printer-cloud-backend .

# 运行容器
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  --name printer-cloud-backend \
  printer-cloud-backend
```

#### 使用Systemd部署
```bash
# 打包应用
cd backend
mvn clean package

# 创建服务文件
sudo tee /etc/systemd/system/printer-cloud.service > /dev/null <<EOF
[Unit]
Description=Printer Cloud Backend Service
After=network.target

[Service]
Type=simple
User=app
WorkingDirectory=/opt/printer-cloud
ExecStart=/usr/bin/java -jar printer-cloud-backend.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# 启动服务
sudo systemctl enable printer-cloud
sudo systemctl start printer-cloud
```

### 2. 管理后台部署

#### 构建生产版本
```bash
cd admin
npm install
npm run build
```

#### 使用Nginx部署
```nginx
# /etc/nginx/sites-available/printer-cloud-admin
server {
    listen 80;
    server_name admin.printer-cloud.com;
    
    root /var/www/printer-cloud-admin;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
# 部署文件
sudo cp -r admin/dist/* /var/www/printer-cloud-admin/
sudo nginx -t
sudo systemctl reload nginx
```

### 3. 数据库配置

#### MySQL生产环境配置
```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/printer_cloud?useSSL=true&serverTimezone=UTC
    username: ${DB_USERNAME:printer_cloud}
    password: ${DB_PASSWORD:your_password}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
```

#### 数据库初始化
```sql
-- 创建数据库
CREATE DATABASE printer_cloud CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户
CREATE USER 'printer_cloud'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON printer_cloud.* TO 'printer_cloud'@'localhost';
FLUSH PRIVILEGES;
```

## 🔧 配置管理

### 环境变量配置
```bash
# 后端环境变量
export SPRING_PROFILES_ACTIVE=prod
export DB_USERNAME=printer_cloud
export DB_PASSWORD=your_password
export JWT_SECRET=your_jwt_secret_key
export FILE_UPLOAD_PATH=/opt/printer-cloud/uploads

# 前端环境变量
export VITE_API_BASE_URL=https://api.printer-cloud.com
export VITE_APP_TITLE=云打印管理后台
```

### SSL证书配置
```yaml
# application-prod.yml
server:
  port: 8443
  ssl:
    key-store: classpath:keystore.p12
    key-store-password: your_keystore_password
    key-store-type: PKCS12
    key-alias: printer-cloud
```

## 📊 监控和日志

### 应用监控
```yaml
# application-prod.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

### 日志配置
```yaml
# application-prod.yml
logging:
  level:
    com.printercloud: info
    org.springframework: warn
  file:
    name: /var/log/printer-cloud/application.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{50} - %msg%n"
```

## 🔒 安全配置

### 防火墙设置
```bash
# 开放必要端口
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw allow 8080/tcp  # 后端服务（内网）
sudo ufw enable
```

### 反向代理配置
```nginx
# 完整的Nginx配置
upstream backend {
    server localhost:8080;
}

server {
    listen 80;
    server_name api.printer-cloud.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name api.printer-cloud.com;
    
    ssl_certificate /etc/ssl/certs/printer-cloud.crt;
    ssl_certificate_key /etc/ssl/private/printer-cloud.key;
    
    location / {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 文件上传大小限制
        client_max_body_size 50M;
    }
}
```

## 🚀 CI/CD部署

### GitHub Actions示例
```yaml
# .github/workflows/deploy.yml
name: Deploy to Production

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 8
      uses: actions/setup-java@v2
      with:
        java-version: '8'
        distribution: 'adopt'
    
    - name: Build Backend
      run: |
        cd backend
        mvn clean package -DskipTests
    
    - name: Set up Node.js
      uses: actions/setup-node@v2
      with:
        node-version: '16'
    
    - name: Build Frontend
      run: |
        cd admin
        npm install
        npm run build
    
    - name: Deploy to Server
      run: |
        # 部署脚本
        scp -r backend/target/*.jar user@server:/opt/printer-cloud/
        scp -r admin/dist/* user@server:/var/www/printer-cloud-admin/
        ssh user@server 'sudo systemctl restart printer-cloud'
```

## 📋 部署检查清单

### 部署前检查
- [ ] 环境变量配置完成
- [ ] 数据库连接测试通过
- [ ] SSL证书配置正确
- [ ] 防火墙规则设置
- [ ] 域名DNS解析配置

### 部署后验证
- [ ] 后端服务健康检查
- [ ] 管理后台访问正常
- [ ] API接口响应正常
- [ ] 文件上传下载功能
- [ ] 数据库连接稳定

### 性能优化
- [ ] 数据库索引优化
- [ ] 静态资源CDN配置
- [ ] 应用缓存配置
- [ ] 负载均衡设置
- [ ] 监控告警配置

## 🆘 故障排除

### 常见问题
1. **端口占用**: 使用 `netstat -tlnp | grep :8080` 检查
2. **数据库连接失败**: 检查数据库服务状态和连接配置
3. **文件上传失败**: 检查文件权限和磁盘空间
4. **前端页面空白**: 检查API地址配置和网络连接

### 日志查看
```bash
# 查看应用日志
tail -f /var/log/printer-cloud/application.log

# 查看系统服务日志
sudo journalctl -u printer-cloud -f

# 查看Nginx日志
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log
```

## 📞 技术支持

如遇到部署问题，请检查：
1. 系统环境要求
2. 网络连接状态
3. 配置文件正确性
4. 日志错误信息

更多技术支持请参考项目文档或提交Issue。

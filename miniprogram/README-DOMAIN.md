# 微信小程序域名配置说明

## 问题描述
微信小程序上传文件时出现错误：`url not in domain list`

## 解决方案

### 1. 开发环境解决方案
在微信开发者工具中：
1. 点击右上角"详情"
2. 在"本地设置"中勾选"不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书"
3. 重新编译项目

### 2. 生产环境解决方案
需要在微信公众平台配置合法域名：

1. 登录微信公众平台 (https://mp.weixin.qq.com)
2. 进入"开发" -> "开发管理" -> "开发设置"
3. 在"服务器域名"中配置：

**request合法域名：**
```
https://your-domain.com
```

**uploadFile合法域名：**
```
https://your-domain.com
```

**downloadFile合法域名：**
```
https://your-domain.com
```

### 3. 当前配置
- 开发环境API地址：`http://localhost:8082/api`
- 生产环境需要配置HTTPS域名

### 4. 注意事项
1. 域名必须是HTTPS协议
2. 域名需要备案
3. 每月只能修改5次域名配置
4. 配置后需要重新发布小程序

### 5. 临时解决方案
如果暂时无法配置域名，可以：
1. 使用微信开发者工具的"不校验合法域名"选项进行开发测试
2. 部署后端到支持HTTPS的云服务器
3. 使用云开发等微信官方服务

## 后端部署建议

### 使用云服务器
推荐使用以下云服务器部署后端：
- 腾讯云
- 阿里云
- 华为云

### 配置HTTPS
1. 申请SSL证书
2. 配置Nginx反向代理
3. 确保API接口支持HTTPS访问

### 示例Nginx配置
```nginx
server {
    listen 443 ssl;
    server_name your-domain.com;
    
    ssl_certificate /path/to/your/certificate.crt;
    ssl_certificate_key /path/to/your/private.key;
    
    location /api/ {
        proxy_pass http://localhost:8082/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

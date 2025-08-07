# 云打印系统后端API服务

基于Spring Boot 2.7.18和Java 8的云打印系统后端API服务，为微信小程序提供完整的打印服务支持。

## 🚀 功能特性

- **统一响应格式**: 使用R类提供标准化的API响应
- **API文档**: 集成Swagger/OpenAPI自动生成文档
- **数据库支持**: 支持H2内存数据库（开发）和MySQL（生产）
- **健康检查**: 提供系统健康检查和监控端点
- **开发友好**: 热重载、详细日志、H2控制台

## 🛠️ 技术栈

- **Java**: 8
- **Spring Boot**: 2.7.18
- **Spring Data JPA**: 数据持久化
- **H2 Database**: 内存数据库（开发环境）
- **MySQL**: 生产数据库
- **Swagger**: API文档
- **Lombok**: 减少样板代码
- **Maven**: 项目构建

## 📋 系统要求

- **Java**: 8+
- **Maven**: 3.6+

## 🏗️ 项目结构

```
backend/
├── src/main/java/com/printercloud/
│   ├── PrinterCloudApplication.java     # 主启动类
│   ├── common/                          # 公共组件
│   │   └── R.java                      # 统一响应格式
│   ├── controller/                      # 控制器层
│   │   └── TestController.java         # 测试接口
│   ├── service/                         # 服务层
│   ├── entity/                          # 实体类
│   ├── repository/                      # 数据访问层
│   ├── dto/                            # 数据传输对象
│   └── config/                         # 配置类
├── src/main/resources/
│   └── application.yml                 # 配置文件
├── pom.xml                            # Maven配置
└── README.md                          # 项目说明
```

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd backend
```

### 2. 编译项目

```bash
mvn clean compile
```

### 3. 运行项目

```bash
mvn spring-boot:run
```

### 4. 验证部署

访问以下地址验证系统是否正常运行：

- **健康检查**: http://localhost:8080/api/test/health
- **系统信息**: http://localhost:8080/api/test/info
- **API文档**: http://localhost:8080/swagger-ui.html
- **H2控制台**: http://localhost:8080/h2-console
- **监控端点**: http://localhost:8080/actuator/health

## 📚 API接口

### 测试接口

- `GET /api/test/health` - 健康检查
- `GET /api/test/info` - 系统信息
- `GET /api/test/response` - 响应格式测试
- `GET /api/test/error` - 错误响应测试
- `GET /api/test/exception` - 异常处理测试

### 统一响应格式

所有API接口都使用统一的响应格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2024-12-07 14:30:00",
  "success": true,
  "error": false
}
```

## 🔧 配置说明

### 开发环境配置

- 使用H2内存数据库
- 启用H2控制台
- 详细的SQL日志输出
- 启用Swagger文档

### 数据库配置

开发环境使用H2内存数据库：
- **URL**: `jdbc:h2:mem:printercloud`
- **用户名**: `sa`
- **密码**: 空
- **控制台**: http://localhost:8080/h2-console

## 🧪 测试

### 健康检查测试

```bash
curl http://localhost:8080/api/test/health
```

### 系统信息测试

```bash
curl http://localhost:8080/api/test/info
```

### 响应格式测试

```bash
curl http://localhost:8080/api/test/response
```

## 📝 开发指南

### 添加新的API接口

1. 在`controller`包下创建控制器类
2. 使用`@RestController`和`@RequestMapping`注解
3. 使用统一的`R<T>`响应格式
4. 添加Swagger注解生成文档

### 示例控制器

```java
@RestController
@RequestMapping("/api/example")
@Tag(name = "示例接口", description = "示例API接口")
public class ExampleController {
    
    @GetMapping("/hello")
    @Operation(summary = "问候接口", description = "返回问候消息")
    public R<String> hello() {
        return R.success("Hello, PrinterCloud!");
    }
}
```

### 代码规范

- 使用Lombok减少样板代码
- 统一使用R类作为API响应格式
- 使用Swagger注解生成API文档
- 遵循RESTful API设计规范

## 🔍 监控和调试

### 应用监控

- **健康检查**: `/actuator/health`
- **应用信息**: `/actuator/info`
- **指标数据**: `/actuator/metrics`

### 数据库调试

- **H2控制台**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:printercloud`
- **用户名**: `sa`
- **密码**: 空

### 日志配置

- 应用日志级别: `DEBUG`
- SQL日志: 已启用
- 控制台输出: 彩色格式

## 🤝 贡献指南

1. Fork项目
2. 创建功能分支
3. 提交代码变更
4. 推送到分支
5. 创建Pull Request

## 📄 许可证

本项目采用MIT许可证。

## 📞 联系我们

- 项目地址: https://github.com/your-org/printercloud-backend
- 问题反馈: https://github.com/your-org/printercloud-backend/issues
- 邮箱: support@printercloud.com

---

**注意**: 这是一个基础版本的后端系统，包含了核心的框架和测试接口。您可以在此基础上添加更多的业务功能，如用户管理、订单管理、文件管理等。

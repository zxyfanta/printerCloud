# 云打印小程序后端测试指南

## 概述

本项目已配置完整的测试框架和API文档系统，包括：
- 单元测试
- 集成测试
- 代码覆盖率报告
- Swagger API文档

## 测试框架

### 测试类型

1. **单元测试** - 测试单个组件的功能
   - 位置：`src/test/java/com/printercloud/service/`
   - 位置：`src/test/java/com/printercloud/controller/`

2. **集成测试** - 测试完整的应用流程
   - 位置：`src/test/java/com/printercloud/integration/`

3. **应用启动测试** - 测试Spring Boot应用能否正常启动
   - 位置：`src/test/java/com/printercloud/PrinterCloudApplicationTests.java`

### 运行测试

#### 运行所有测试
```bash
cd backend
mvn test
```

#### 只运行单元测试
```bash
mvn test -Dtest="!**/integration/**"
```

#### 只运行集成测试
```bash
mvn test -Dtest="**/integration/**"
```

#### 运行特定测试类
```bash
mvn test -Dtest=UserServiceTest
mvn test -Dtest=AuthControllerTest
mvn test -Dtest=AuthIntegrationTest
```

#### 运行测试并生成覆盖率报告
```bash
mvn clean test jacoco:report
```

覆盖率报告将生成在：`target/site/jacoco/index.html`

### 测试配置

- **测试配置文件**：`src/test/resources/application-test.yml`
- **测试数据库**：H2内存数据库
- **测试端口**：随机端口（避免冲突）

## API文档 (OpenAPI 3 / Swagger)

### 访问Swagger UI

1. 启动应用程序：
```bash
cd backend
mvn spring-boot:run
```

2. 在浏览器中访问：
```
http://localhost:8080/swagger-ui/index.html
```

或者简化访问：
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI配置

- **配置文件**：`src/main/java/com/printercloud/config/SwaggerConfig.java`
- **API文档标题**：云打印小程序后端API
- **版本**：1.0.0
- **认证方式**：JWT Bearer Token
- **OpenAPI规范**：3.0

### 使用Swagger测试API

1. **无需认证的接口**：
   - 健康检查：`GET /health`
   - 公开价格：`GET /price/public`
   - 公开配置：`GET /system/public`

2. **需要认证的接口**：
   - 点击右上角的"Authorize"按钮
   - 选择"JWT (http, Bearer)"
   - 输入JWT Token（不需要Bearer前缀，系统会自动添加）
   - 或者先调用登录接口获取token

### OpenAPI JSON

可以通过以下URL获取OpenAPI规范的JSON格式：
```
http://localhost:8080/v3/api-docs
```

## 测试数据

### 默认测试用户

测试环境会自动创建以下用户：

1. **超级管理员**
   - 用户名：`superadmin`
   - 密码：`admin123`
   - 角色：`SUPER_ADMIN`

### 测试工具类

使用 `TestDataBuilder` 类可以快速创建测试数据：

```java
// 创建测试用户
User testUser = TestDataBuilder.createTestUser();

// 创建测试管理员
User testAdmin = TestDataBuilder.createTestAdmin();

// 创建测试文件
PrintFile testFile = TestDataBuilder.createTestFile();
```

## 持续集成

### Maven生命周期

```bash
# 编译
mvn compile

# 运行单元测试
mvn test

# 运行集成测试
mvn verify

# 打包（包含测试）
mvn package

# 跳过测试打包
mvn package -DskipTests
```

### 测试报告

测试完成后，可以在以下位置查看报告：

1. **Surefire测试报告**：`target/surefire-reports/`
2. **Failsafe测试报告**：`target/failsafe-reports/`
3. **JaCoCo覆盖率报告**：`target/site/jacoco/index.html`

## 最佳实践

### 编写测试

1. **命名规范**：
   - 测试类：`XxxTest.java`
   - 测试方法：`testMethodName_Scenario_ExpectedResult()`

2. **测试结构**：
   - Given（准备）- 设置测试数据
   - When（执行）- 调用被测试方法
   - Then（验证）- 断言结果

3. **Mock使用**：
   - 使用 `@MockBean` 模拟Spring Bean
   - 使用 `@Mock` 模拟普通对象
   - 使用 `when().thenReturn()` 设置Mock行为

### API文档

1. **Controller注解**（OpenAPI 3）：
   - `@Tag` - 描述Controller
   - `@Operation` - 描述接口方法
   - `@Parameter` - 描述参数

2. **响应注解**：
   - `@ApiResponse` - 描述响应状态
   - `@Schema` - 描述数据模型

## 快速开始

### 验证环境配置
```bash
# Windows
verify-setup.bat

# 或手动验证
mvn clean compile
mvn test -Dtest=PrinterCloudApplicationTests
```

### 运行测试
```bash
# Windows
run-tests.bat

# 或手动运行
mvn test
mvn jacoco:report
```

### 启动应用（带Swagger）
```bash
# Windows
start-with-swagger.bat

# 或手动启动
mvn spring-boot:run
```

## 故障排除

### 常见问题

1. **测试失败**：
   - 检查测试配置文件
   - 确认测试数据库连接
   - 查看测试日志

2. **Swagger无法访问**：
   - 确认应用已启动
   - 检查端口是否正确
   - 确认Swagger依赖已添加

3. **覆盖率报告为空**：
   - 确认JaCoCo插件配置正确
   - 运行 `mvn clean test jacoco:report`

### 调试技巧

1. **启用调试日志**：
```yaml
logging:
  level:
    com.printercloud: DEBUG
```

2. **使用测试切片**：
```java
@WebMvcTest(AuthController.class)  // 只测试Web层
@DataJpaTest  // 只测试JPA层
```

3. **使用TestContainers**（已配置）：
```java
@Testcontainers
class DatabaseIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13");
}
```

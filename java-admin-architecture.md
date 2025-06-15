# Java GUI管理端架构设计

## 🏗️ 项目结构

```
java-admin/
├── 📁 src/main/java/
│   └── 📁 com/printercloud/admin/
│       ├── 📁 config/                    # 配置类
│       │   ├── AppConfig.java
│       │   ├── HttpClientConfig.java
│       │   └── PrinterConfig.java
│       ├── 📁 controller/                # 控制器层
│       │   ├── MainController.java
│       │   ├── LoginController.java
│       │   ├── DashboardController.java
│       │   ├── FileController.java
│       │   ├── OrderController.java
│       │   └── PrinterController.java
│       ├── 📁 service/                   # 服务层
│       │   ├── ApiService.java
│       │   ├── AuthService.java
│       │   ├── FileService.java
│       │   ├── OrderService.java
│       │   ├── PrintService.java
│       │   └── NotificationService.java
│       ├── 📁 model/                     # 数据模型
│       │   ├── User.java
│       │   ├── PrintFile.java
│       │   ├── Order.java
│       │   ├── PrintJob.java
│       │   └── ApiResponse.java
│       ├── 📁 view/                      # 视图层
│       │   ├── MainView.java
│       │   ├── LoginView.java
│       │   ├── DashboardView.java
│       │   ├── FileManagementView.java
│       │   ├── OrderManagementView.java
│       │   ├── UserManagementView.java
│       │   └── PrinterManagementView.java
│       ├── 📁 component/                 # 自定义组件
│       │   ├── StatusCard.java
│       │   ├── DataTable.java
│       │   ├── PrinterPanel.java
│       │   └── NotificationPanel.java
│       ├── 📁 util/                      # 工具类
│       │   ├── HttpUtil.java
│       │   ├── PrintUtil.java
│       │   ├── FileUtil.java
│       │   └── ConfigUtil.java
│       └── 📁 PrinterCloudAdminApp.java  # 主启动类
├── 📁 src/main/resources/
│   ├── 📁 fxml/                          # FXML文件 (JavaFX)
│   │   ├── main.fxml
│   │   ├── login.fxml
│   │   ├── dashboard.fxml
│   │   └── ...
│   ├── 📁 css/                           # 样式文件
│   │   ├── application.css
│   │   ├── dark-theme.css
│   │   └── light-theme.css
│   ├── 📁 images/                        # 图片资源
│   │   ├── icons/
│   │   └── logos/
│   ├── application.properties            # 配置文件
│   └── logback-spring.xml               # 日志配置
├── 📁 src/test/java/                     # 测试代码
├── 📁 scripts/                           # 构建脚本
│   ├── build.sh
│   ├── package.sh
│   └── run.sh
├── pom.xml                               # Maven配置
└── README.md
```

## 🔧 核心技术组件

### 1. GUI框架选择

#### JavaFX方案 (推荐)
```xml
<dependencies>
    <!-- JavaFX -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>21.0.1</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>21.0.1</version>
    </dependency>
    
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
        <version>3.2.0</version>
    </dependency>
    
    <!-- HTTP客户端 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
        <version>3.2.0</version>
    </dependency>
</dependencies>
```

#### Swing方案 (备选)
```xml
<dependencies>
    <!-- FlatLaf现代化外观 -->
    <dependency>
        <groupId>com.formdev</groupId>
        <artifactId>flatlaf</artifactId>
        <version>3.2.5</version>
    </dependency>
    
    <!-- MigLayout布局管理器 -->
    <dependency>
        <groupId>com.miglayout</groupId>
        <artifactId>miglayout-swing</artifactId>
        <version>11.0</version>
    </dependency>
</dependencies>
```

### 2. 打印功能实现

#### 核心打印服务
```java
@Service
public class PrintService {
    
    // 获取系统打印机列表
    public List<PrinterInfo> getAvailablePrinters();
    
    // 打印PDF文件
    public boolean printPdf(String filePath, PrintSettings settings);
    
    // 打印图片文件
    public boolean printImage(String filePath, PrintSettings settings);
    
    // 打印Office文档
    public boolean printDocument(String filePath, PrintSettings settings);
    
    // 命令行打印接口
    public boolean printFromCommand(String[] args);
}
```

#### 支持的文件格式
- 📄 **PDF**: Apache PDFBox
- 🖼️ **图片**: Java BufferedImage
- 📝 **Word**: Apache POI
- 📊 **Excel**: Apache POI  
- 📋 **PowerPoint**: Apache POI
- 📄 **文本**: 直接打印

### 3. 命令行打印功能

#### 命令行参数设计
```bash
# 基本打印
java -jar printer-admin.jar --print --file=/path/to/file.pdf

# 指定打印机
java -jar printer-admin.jar --print --file=/path/to/file.pdf --printer="HP LaserJet"

# 设置打印参数
java -jar printer-admin.jar --print \
  --file=/path/to/file.pdf \
  --printer="HP LaserJet" \
  --copies=2 \
  --duplex=true \
  --paper=A4 \
  --color=false

# 批量打印
java -jar printer-admin.jar --print --batch=/path/to/print-jobs.json
```

#### 参数说明
- `--print`: 打印模式
- `--file`: 文件路径
- `--printer`: 打印机名称
- `--copies`: 打印份数
- `--duplex`: 双面打印
- `--paper`: 纸张大小 (A4, A3, Letter等)
- `--color`: 彩色打印
- `--quality`: 打印质量 (draft, normal, high)
- `--batch`: 批量打印配置文件

## 🎨 界面设计

### JavaFX界面特色
- 🎨 **现代化设计**: CSS样式支持
- 🌙 **主题切换**: 深色/浅色主题
- 📱 **响应式布局**: 自适应窗口大小
- 🎯 **Material Design**: 卡片式布局
- 📊 **图表支持**: JavaFX Charts

### 主要界面
1. **登录界面**: 简洁的登录表单
2. **主界面**: 侧边导航 + 内容区域
3. **仪表盘**: 统计卡片 + 图表
4. **文件管理**: 表格 + 操作按钮
5. **订单管理**: 列表 + 状态管理
6. **打印机管理**: 打印机列表 + 设置
7. **设置界面**: 系统配置选项

## 🔄 数据交互

### HTTP客户端
```java
@Service
public class ApiService {
    private final WebClient webClient;
    
    // 登录认证
    public Mono<LoginResponse> login(LoginRequest request);
    
    // 获取文件列表
    public Mono<PagedResult<PrintFile>> getFiles(FileFilter filter);
    
    // 获取订单列表  
    public Mono<PagedResult<Order>> getOrders(OrderFilter filter);
    
    // 文件下载
    public Mono<byte[]> downloadFile(Long fileId);
}
```

### 异步处理
- **WebClient**: 非阻塞HTTP客户端
- **CompletableFuture**: 异步任务处理
- **Platform.runLater()**: UI线程更新

## 🖨️ 打印机集成

### 打印机发现
```java
// 获取系统打印机
PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

// 获取默认打印机
PrintService defaultPrinter = PrintServiceLookup.lookupDefaultPrintService();

// 打印机能力查询
PrintServiceAttributeSet attributes = printService.getAttributes();
```

### 文件打印实现
```java
// PDF打印
PDDocument document = PDDocument.load(file);
PrinterJob job = PrinterJob.getPrinterJob();
job.setPageable(new PDFPageable(document));
job.print();

// 图片打印
BufferedImage image = ImageIO.read(file);
PrinterJob job = PrinterJob.getPrinterJob();
job.setPrintable(new ImagePrintable(image));
job.print();
```

## 📦 打包和分发

### 原生安装包
```xml
<!-- JavaFX Maven插件 -->
<plugin>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-maven-plugin</artifactId>
    <version>0.0.8</version>
    <configuration>
        <mainClass>com.printercloud.admin.PrinterCloudAdminApp</mainClass>
    </configuration>
</plugin>

<!-- JPackage插件 -->
<plugin>
    <groupId>org.panteleyev</groupId>
    <artifactId>jpackage-maven-plugin</artifactId>
    <version>1.6.0</version>
</plugin>
```

### 跨平台支持
- **Windows**: .exe安装包
- **macOS**: .dmg安装包  
- **Linux**: .deb/.rpm包
- **通用**: 可执行JAR包

## 🚀 开发优势

### 相比.NET的优势
- 🌍 **真正跨平台**: 一次编写，到处运行
- 🖨️ **更好的打印支持**: Java Print Service API
- 📚 **丰富的生态**: Apache POI, PDFBox等
- 🔧 **更好的Office集成**: 原生POI支持
- 💰 **无许可费用**: 完全开源

### 技术特色
- ⚡ **高性能**: JVM优化和多线程
- 🔒 **安全性**: Java安全模型
- 🧪 **易测试**: JUnit + Mockito
- 📊 **监控**: JMX + Micrometer
- 🔄 **热部署**: Spring DevTools

## 📋 实施计划

### Phase 1: 基础框架 (1周)
- ✅ 项目结构搭建
- ✅ Spring Boot配置
- ✅ JavaFX基础界面
- ✅ HTTP客户端集成

### Phase 2: 核心功能 (2周)  
- ✅ 登录认证系统
- ✅ 文件管理界面
- ✅ 订单管理功能
- ✅ 基础打印功能

### Phase 3: 高级功能 (2周)
- ✅ 打印机管理
- ✅ 命令行打印
- ✅ 批量处理
- ✅ 系统设置

### Phase 4: 优化部署 (1周)
- ✅ 性能优化
- ✅ 原生打包
- ✅ 安装程序
- ✅ 文档完善

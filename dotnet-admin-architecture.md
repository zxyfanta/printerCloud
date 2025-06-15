# .NET管理端架构设计方案

## 🏗️ 项目结构

```
PrinterCloud.Admin/
├── 📁 src/
│   ├── 📁 PrinterCloud.Admin.Core/              # 核心业务逻辑
│   │   ├── 📁 Models/                           # 数据模型
│   │   │   ├── User.cs
│   │   │   ├── PrintFile.cs
│   │   │   ├── Order.cs
│   │   │   └── PrintJob.cs
│   │   ├── 📁 Services/                         # 业务服务
│   │   │   ├── IApiService.cs
│   │   │   ├── ApiService.cs
│   │   │   ├── IPrintService.cs
│   │   │   ├── PrintService.cs
│   │   │   └── IFileService.cs
│   │   ├── 📁 DTOs/                            # 数据传输对象
│   │   └── 📁 Enums/                           # 枚举定义
│   │
│   ├── 📁 PrinterCloud.Admin.Infrastructure/    # 基础设施层
│   │   ├── 📁 Http/                            # HTTP客户端
│   │   ├── 📁 Printing/                        # 打印机集成
│   │   ├── 📁 Storage/                         # 本地存储
│   │   └── 📁 Configuration/                   # 配置管理
│   │
│   ├── 📁 PrinterCloud.Admin.UI/               # UI层（WPF/Avalonia）
│   │   ├── 📁 Views/                           # 视图
│   │   │   ├── MainWindow.xaml
│   │   │   ├── LoginWindow.xaml
│   │   │   ├── DashboardView.xaml
│   │   │   ├── FileManagementView.xaml
│   │   │   ├── OrderManagementView.xaml
│   │   │   ├── UserManagementView.xaml
│   │   │   ├── PrinterManagementView.xaml
│   │   │   └── SettingsView.xaml
│   │   ├── 📁 ViewModels/                      # 视图模型
│   │   │   ├── MainViewModel.cs
│   │   │   ├── LoginViewModel.cs
│   │   │   ├── DashboardViewModel.cs
│   │   │   ├── FileManagementViewModel.cs
│   │   │   ├── OrderManagementViewModel.cs
│   │   │   ├── UserManagementViewModel.cs
│   │   │   ├── PrinterManagementViewModel.cs
│   │   │   └── SettingsViewModel.cs
│   │   ├── 📁 Controls/                        # 自定义控件
│   │   ├── 📁 Converters/                      # 值转换器
│   │   ├── 📁 Resources/                       # 资源文件
│   │   └── App.xaml
│   │
│   └── 📁 PrinterCloud.Admin.Tests/            # 测试项目
│       ├── 📁 Unit/
│       ├── 📁 Integration/
│       └── 📁 UI/
│
├── 📁 docs/                                    # 文档
├── 📁 scripts/                                 # 构建脚本
├── PrinterCloud.Admin.sln                     # 解决方案文件
└── README.md
```

## 🖨️ 打印功能架构

### 打印服务设计
```csharp
public interface IPrintService
{
    Task<List<PrinterInfo>> GetAvailablePrintersAsync();
    Task<bool> PrintFileAsync(string filePath, PrintSettings settings);
    Task<PrintJobStatus> GetPrintJobStatusAsync(string jobId);
    event EventHandler<PrintJobEventArgs> PrintJobCompleted;
}

public class PrintSettings
{
    public string PrinterName { get; set; }
    public int Copies { get; set; }
    public bool DoubleSided { get; set; }
    public PaperSize PaperSize { get; set; }
    public PrintQuality Quality { get; set; }
    public ColorMode ColorMode { get; set; }
}
```

### 支持的文件格式
- 📄 PDF文件 (PdfSharp/iTextSharp)
- 📝 Word文档 (Microsoft.Office.Interop.Word)
- 📊 Excel表格 (Microsoft.Office.Interop.Excel)
- 🖼️ 图片文件 (System.Drawing)
- 📋 PowerPoint (Microsoft.Office.Interop.PowerPoint)

## 🔧 核心功能模块

### 1. 认证与授权模块
```csharp
public class AuthenticationService
{
    public async Task<LoginResult> LoginAsync(string username, string password);
    public async Task<bool> ValidateTokenAsync(string token);
    public async Task LogoutAsync();
}
```

### 2. 文件管理模块
```csharp
public class FileManagementService
{
    public async Task<PagedResult<PrintFile>> GetFilesAsync(FileFilter filter);
    public async Task<byte[]> DownloadFileAsync(long fileId);
    public async Task<bool> DeleteFileAsync(long fileId);
    public async Task<FilePreview> GeneratePreviewAsync(long fileId);
}
```

### 3. 订单管理模块
```csharp
public class OrderManagementService
{
    public async Task<PagedResult<Order>> GetOrdersAsync(OrderFilter filter);
    public async Task<bool> UpdateOrderStatusAsync(long orderId, OrderStatus status);
    public async Task<bool> ProcessPrintOrderAsync(long orderId);
    public async Task<OrderStatistics> GetOrderStatisticsAsync();
}
```

### 4. 打印机管理模块
```csharp
public class PrinterManagementService
{
    public async Task<List<PrinterInfo>> DiscoverPrintersAsync();
    public async Task<bool> TestPrinterConnectionAsync(string printerName);
    public async Task<PrinterStatus> GetPrinterStatusAsync(string printerName);
    public async Task<bool> ConfigurePrinterAsync(PrinterConfig config);
}
```

## 📊 数据可视化

### 图表组件选择
- **LiveCharts2**: 现代化的图表库，支持实时数据
- **OxyPlot**: 轻量级，性能优秀
- **ScottPlot**: 科学图表，适合数据分析

### 仪表盘功能
- 📈 订单趋势图
- 📊 文件类型分布
- 🖨️ 打印机状态监控
- 💰 收入统计
- 👥 用户活跃度

## 🔄 实时通信

### SignalR集成
```csharp
public class NotificationHub : Hub
{
    public async Task JoinAdminGroup()
    {
        await Groups.AddToGroupAsync(Context.ConnectionId, "Admins");
    }
}

public class NotificationService
{
    public async Task NotifyNewOrderAsync(Order order);
    public async Task NotifyPrintJobCompletedAsync(PrintJob job);
    public async Task NotifySystemStatusAsync(SystemStatus status);
}
```

## 🎨 UI设计原则

### 主题与样式
- 🌙 支持深色/浅色主题切换
- 🎨 Material Design风格
- 📱 响应式布局设计
- ♿ 无障碍访问支持

### 用户体验
- ⚡ 快速响应的UI
- 🔄 实时数据更新
- 📱 直观的操作界面
- 🎯 任务导向的工作流

## 🚀 部署与分发

### 构建配置
```xml
<PropertyGroup>
    <TargetFramework>net8.0-windows</TargetFramework>
    <UseWPF>true</UseWPF>
    <PublishSingleFile>true</PublishSingleFile>
    <SelfContained>true</SelfContained>
    <RuntimeIdentifier>win-x64</RuntimeIdentifier>
</PropertyGroup>
```

### 安装包制作
- **WiX Toolset**: 创建MSI安装包
- **Squirrel.Windows**: 自动更新支持
- **ClickOnce**: 简化部署

## 🔧 开发工具链

### 推荐IDE
- **Visual Studio 2022** (Windows)
- **JetBrains Rider** (跨平台)
- **Visual Studio Code** (轻量级)

### 包管理
```xml
<PackageReference Include="Microsoft.Extensions.Hosting" Version="8.0.0" />
<PackageReference Include="CommunityToolkit.Mvvm" Version="8.2.2" />
<PackageReference Include="ModernWpfUI" Version="0.9.6" />
<PackageReference Include="LiveChartsCore.SkiaSharpView.WPF" Version="2.0.0-rc2" />
<PackageReference Include="Refit" Version="7.0.0" />
<PackageReference Include="Serilog" Version="3.1.1" />
```

## 📋 开发计划

### Phase 1: 基础架构 (2-3周)
- ✅ 项目结构搭建
- ✅ 基础UI框架
- ✅ API客户端集成
- ✅ 认证系统

### Phase 2: 核心功能 (3-4周)
- ✅ 文件管理界面
- ✅ 订单管理界面
- ✅ 用户管理界面
- ✅ 基础打印功能

### Phase 3: 高级功能 (2-3周)
- ✅ 打印机管理
- ✅ 实时通知
- ✅ 数据可视化
- ✅ 系统设置

### Phase 4: 优化与部署 (1-2周)
- ✅ 性能优化
- ✅ 错误处理
- ✅ 安装包制作
- ✅ 文档完善

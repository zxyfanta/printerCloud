# .NET管理端实施指南

## 🚀 快速开始

### 1. 环境准备

#### macOS开发环境
```bash
# 安装.NET 8 SDK
brew install --cask dotnet

# 验证安装
dotnet --version

# 安装Visual Studio Code (可选)
brew install --cask visual-studio-code

# 安装JetBrains Rider (推荐)
brew install --cask rider
```

#### Windows部署环境
```powershell
# 下载并安装.NET 8 Runtime
# https://dotnet.microsoft.com/download/dotnet/8.0

# 验证安装
dotnet --version
```

### 2. 项目创建脚本

#### 创建解决方案结构
```bash
#!/bin/bash
# create-dotnet-admin.sh

# 创建解决方案
dotnet new sln -n PrinterCloud.Admin

# 创建项目
dotnet new classlib -n PrinterCloud.Admin.Core
dotnet new classlib -n PrinterCloud.Admin.Infrastructure  
dotnet new wpf -n PrinterCloud.Admin.UI
dotnet new xunit -n PrinterCloud.Admin.Tests

# 添加项目到解决方案
dotnet sln add src/PrinterCloud.Admin.Core/PrinterCloud.Admin.Core.csproj
dotnet sln add src/PrinterCloud.Admin.Infrastructure/PrinterCloud.Admin.Infrastructure.csproj
dotnet sln add src/PrinterCloud.Admin.UI/PrinterCloud.Admin.UI.csproj
dotnet sln add src/PrinterCloud.Admin.Tests/PrinterCloud.Admin.Tests.csproj

# 添加项目引用
dotnet add src/PrinterCloud.Admin.UI reference src/PrinterCloud.Admin.Core
dotnet add src/PrinterCloud.Admin.UI reference src/PrinterCloud.Admin.Infrastructure
dotnet add src/PrinterCloud.Admin.Infrastructure reference src/PrinterCloud.Admin.Core
dotnet add src/PrinterCloud.Admin.Tests reference src/PrinterCloud.Admin.Core
```

### 3. 核心包依赖

#### PrinterCloud.Admin.Core.csproj
```xml
<Project Sdk="Microsoft.NET.Sdk">
  <PropertyGroup>
    <TargetFramework>net8.0</TargetFramework>
    <Nullable>enable</Nullable>
  </PropertyGroup>

  <ItemGroup>
    <PackageReference Include="Microsoft.Extensions.DependencyInjection.Abstractions" Version="8.0.0" />
    <PackageReference Include="Microsoft.Extensions.Logging.Abstractions" Version="8.0.0" />
    <PackageReference Include="System.ComponentModel.Annotations" Version="5.0.0" />
  </ItemGroup>
</Project>
```

#### PrinterCloud.Admin.Infrastructure.csproj
```xml
<Project Sdk="Microsoft.NET.Sdk">
  <PropertyGroup>
    <TargetFramework>net8.0</TargetFramework>
    <Nullable>enable</Nullable>
  </PropertyGroup>

  <ItemGroup>
    <PackageReference Include="Refit" Version="7.0.0" />
    <PackageReference Include="Microsoft.Extensions.Http" Version="8.0.0" />
    <PackageReference Include="Microsoft.Extensions.Configuration" Version="8.0.0" />
    <PackageReference Include="Serilog" Version="3.1.1" />
    <PackageReference Include="Serilog.Extensions.Hosting" Version="8.0.0" />
    <PackageReference Include="Serilog.Sinks.File" Version="5.0.0" />
    <PackageReference Include="PdfSharp" Version="6.0.0" />
    <PackageReference Include="System.Drawing.Common" Version="8.0.0" />
  </ItemGroup>
</Project>
```

#### PrinterCloud.Admin.UI.csproj
```xml
<Project Sdk="Microsoft.NET.Sdk">
  <PropertyGroup>
    <OutputType>WinExe</OutputType>
    <TargetFramework>net8.0-windows</TargetFramework>
    <UseWPF>true</UseWPF>
    <Nullable>enable</Nullable>
    <ApplicationIcon>Resources\app.ico</ApplicationIcon>
  </PropertyGroup>

  <ItemGroup>
    <PackageReference Include="CommunityToolkit.Mvvm" Version="8.2.2" />
    <PackageReference Include="ModernWpfUI" Version="0.9.6" />
    <PackageReference Include="LiveChartsCore.SkiaSharpView.WPF" Version="2.0.0-rc2" />
    <PackageReference Include="Microsoft.Extensions.Hosting" Version="8.0.0" />
    <PackageReference Include="Microsoft.Extensions.DependencyInjection" Version="8.0.0" />
    <PackageReference Include="Microsoft.Xaml.Behaviors.Wpf" Version="1.1.77" />
  </ItemGroup>
</Project>
```

## 🏗️ 核心代码实现

### 1. API服务接口定义

#### IApiService.cs
```csharp
using Refit;

namespace PrinterCloud.Admin.Core.Services;

public interface IApiService
{
    [Post("/auth/login")]
    Task<ApiResponse<LoginResponse>> LoginAsync([Body] LoginRequest request);

    [Get("/file/list")]
    Task<ApiResponse<PagedResult<PrintFile>>> GetFilesAsync(
        [Query] int page = 1, 
        [Query] int size = 10);

    [Get("/orders")]
    Task<ApiResponse<PagedResult<Order>>> GetOrdersAsync(
        [Query] int page = 1, 
        [Query] int size = 10);

    [Get("/user/list")]
    Task<ApiResponse<PagedResult<User>>> GetUsersAsync(
        [Query] int page = 1, 
        [Query] int size = 10);

    [Post("/orders/{orderId}/complete")]
    Task<ApiResponse<object>> CompleteOrderAsync(long orderId);

    [Delete("/file/{fileId}")]
    Task<ApiResponse<object>> DeleteFileAsync(long fileId);
}
```

### 2. 打印服务实现

#### IPrintService.cs
```csharp
namespace PrinterCloud.Admin.Core.Services;

public interface IPrintService
{
    Task<List<PrinterInfo>> GetAvailablePrintersAsync();
    Task<bool> PrintFileAsync(string filePath, PrintSettings settings);
    Task<PrintJobStatus> GetPrintJobStatusAsync(string jobId);
    event EventHandler<PrintJobEventArgs> PrintJobCompleted;
}

public class PrintService : IPrintService
{
    private readonly ILogger<PrintService> _logger;

    public PrintService(ILogger<PrintService> logger)
    {
        _logger = logger;
    }

    public async Task<List<PrinterInfo>> GetAvailablePrintersAsync()
    {
        return await Task.Run(() =>
        {
            var printers = new List<PrinterInfo>();
            
            foreach (string printerName in PrinterSettings.InstalledPrinters)
            {
                var printer = new PrinterInfo
                {
                    Name = printerName,
                    IsDefault = printerName == PrinterSettings.Default?.PrinterName,
                    Status = GetPrinterStatus(printerName)
                };
                printers.Add(printer);
            }
            
            return printers;
        });
    }

    public async Task<bool> PrintFileAsync(string filePath, PrintSettings settings)
    {
        try
        {
            var extension = Path.GetExtension(filePath).ToLower();
            
            return extension switch
            {
                ".pdf" => await PrintPdfAsync(filePath, settings),
                ".jpg" or ".jpeg" or ".png" or ".bmp" => await PrintImageAsync(filePath, settings),
                ".doc" or ".docx" => await PrintWordDocumentAsync(filePath, settings),
                _ => throw new NotSupportedException($"不支持的文件格式: {extension}")
            };
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "打印文件失败: {FilePath}", filePath);
            return false;
        }
    }

    private async Task<bool> PrintPdfAsync(string filePath, PrintSettings settings)
    {
        // 使用PdfSharp打印PDF
        // 实现细节...
        return await Task.FromResult(true);
    }

    private async Task<bool> PrintImageAsync(string filePath, PrintSettings settings)
    {
        // 使用System.Drawing打印图片
        // 实现细节...
        return await Task.FromResult(true);
    }
}
```

### 3. MVVM视图模型

#### MainViewModel.cs
```csharp
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;

namespace PrinterCloud.Admin.UI.ViewModels;

public partial class MainViewModel : ObservableObject
{
    private readonly IApiService _apiService;
    private readonly ILogger<MainViewModel> _logger;

    [ObservableProperty]
    private string _currentView = "Dashboard";

    [ObservableProperty]
    private bool _isLoading;

    [ObservableProperty]
    private string _statusMessage = "就绪";

    public MainViewModel(IApiService apiService, ILogger<MainViewModel> logger)
    {
        _apiService = apiService;
        _logger = logger;
    }

    [RelayCommand]
    private void NavigateToView(string viewName)
    {
        CurrentView = viewName;
        _logger.LogInformation("导航到视图: {ViewName}", viewName);
    }

    [RelayCommand]
    private async Task RefreshDataAsync()
    {
        IsLoading = true;
        StatusMessage = "正在刷新数据...";
        
        try
        {
            // 刷新数据逻辑
            await Task.Delay(1000); // 模拟异步操作
            StatusMessage = "数据刷新完成";
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "刷新数据失败");
            StatusMessage = "刷新失败";
        }
        finally
        {
            IsLoading = false;
        }
    }
}
```

### 4. 依赖注入配置

#### App.xaml.cs
```csharp
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;

namespace PrinterCloud.Admin.UI;

public partial class App : Application
{
    private readonly IHost _host;

    public App()
    {
        _host = Host.CreateDefaultBuilder()
            .ConfigureServices(ConfigureServices)
            .ConfigureLogging(logging =>
            {
                logging.AddConsole();
                logging.AddDebug();
            })
            .Build();
    }

    private void ConfigureServices(HostBuilderContext context, IServiceCollection services)
    {
        // API服务
        services.AddRefitClient<IApiService>()
            .ConfigureHttpClient(c => c.BaseAddress = new Uri("http://localhost:8082/api"));

        // 业务服务
        services.AddSingleton<IPrintService, PrintService>();
        services.AddSingleton<IFileService, FileService>();

        // 视图模型
        services.AddTransient<MainViewModel>();
        services.AddTransient<DashboardViewModel>();
        services.AddTransient<FileManagementViewModel>();
        services.AddTransient<OrderManagementViewModel>();

        // 视图
        services.AddTransient<MainWindow>();
        services.AddTransient<LoginWindow>();
    }

    protected override async void OnStartup(StartupEventArgs e)
    {
        await _host.StartAsync();

        var mainWindow = _host.Services.GetRequiredService<MainWindow>();
        mainWindow.Show();

        base.OnStartup(e);
    }

    protected override async void OnExit(ExitEventArgs e)
    {
        using (_host)
        {
            await _host.StopAsync();
        }

        base.OnExit(e);
    }
}
```

## 📋 实施步骤

### 第一阶段：项目搭建 (1周)
1. ✅ 创建解决方案结构
2. ✅ 配置依赖注入
3. ✅ 实现基础API客户端
4. ✅ 创建主窗口框架

### 第二阶段：核心功能 (2-3周)
1. ✅ 实现登录认证
2. ✅ 开发文件管理界面
3. ✅ 开发订单管理界面
4. ✅ 实现基础打印功能

### 第三阶段：高级功能 (2周)
1. ✅ 打印机管理和配置
2. ✅ 实时通知系统
3. ✅ 数据可视化图表
4. ✅ 系统设置和配置

### 第四阶段：测试和部署 (1周)
1. ✅ 单元测试和集成测试
2. ✅ 性能优化
3. ✅ 创建安装包
4. ✅ 部署文档

## 🎯 关键优势

### 相比Vue3 Admin的优势
- 🖨️ **原生打印支持**: 直接调用系统打印API
- ⚡ **更好的性能**: 原生应用，无浏览器开销
- 🔧 **系统集成**: 更好的文件系统和硬件访问
- 📱 **离线工作**: 不依赖网络连接
- 🔒 **更高安全性**: 本地应用，减少网络攻击面

### 技术特色
- 🎨 **现代化UI**: Material Design风格
- 🔄 **实时更新**: SignalR集成
- 📊 **丰富图表**: LiveCharts2支持
- 🌙 **主题切换**: 深色/浅色模式
- ♿ **无障碍**: 完整的可访问性支持

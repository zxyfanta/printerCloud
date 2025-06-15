#!/bin/bash

# 创建.NET管理端项目结构脚本
echo "🚀 开始创建PrinterCloud.Admin项目结构..."

# 创建主目录
mkdir -p dotnet-admin
cd dotnet-admin

# 创建解决方案
echo "📁 创建解决方案..."
dotnet new sln -n PrinterCloud.Admin

# 创建src目录结构
mkdir -p src

# 创建核心项目
echo "🔧 创建核心业务层..."
cd src
dotnet new classlib -n PrinterCloud.Admin.Core
dotnet new classlib -n PrinterCloud.Admin.Infrastructure
dotnet new wpf -n PrinterCloud.Admin.UI
cd ..

# 创建测试项目
echo "🧪 创建测试项目..."
mkdir -p tests
cd tests
dotnet new xunit -n PrinterCloud.Admin.Tests
cd ..

# 添加项目到解决方案
echo "🔗 添加项目引用..."
dotnet sln add src/PrinterCloud.Admin.Core/PrinterCloud.Admin.Core.csproj
dotnet sln add src/PrinterCloud.Admin.Infrastructure/PrinterCloud.Admin.Infrastructure.csproj
dotnet sln add src/PrinterCloud.Admin.UI/PrinterCloud.Admin.UI.csproj
dotnet sln add tests/PrinterCloud.Admin.Tests/PrinterCloud.Admin.Tests.csproj

# 添加项目间引用
dotnet add src/PrinterCloud.Admin.UI reference src/PrinterCloud.Admin.Core
dotnet add src/PrinterCloud.Admin.UI reference src/PrinterCloud.Admin.Infrastructure
dotnet add src/PrinterCloud.Admin.Infrastructure reference src/PrinterCloud.Admin.Core
dotnet add tests/PrinterCloud.Admin.Tests reference src/PrinterCloud.Admin.Core

echo "✅ 项目结构创建完成！"
echo "📂 项目位置: $(pwd)"

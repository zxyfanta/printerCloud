#!/bin/bash

# PrinterCloud Java Admin 构建脚本
set -e

echo "🚀 开始构建 PrinterCloud Java Admin..."

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 函数定义
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查Java环境
check_java() {
    log_info "检查Java环境..."
    if ! command -v java &> /dev/null; then
        log_error "Java未安装，请先安装Java 17或更高版本"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    log_success "Java版本: $JAVA_VERSION"
    
    if ! command -v mvn &> /dev/null; then
        log_error "Maven未安装，请先安装Maven"
        exit 1
    fi
    
    MVN_VERSION=$(mvn -version | head -n 1)
    log_success "Maven版本: $MVN_VERSION"
}

# 清理构建目录
clean() {
    log_info "清理构建目录..."
    mvn clean
    log_success "清理完成"
}

# 编译项目
compile() {
    log_info "编译项目..."
    mvn compile
    log_success "编译完成"
}

# 运行测试
test() {
    log_info "运行测试..."
    mvn test
    log_success "测试完成"
}

# 打包项目
package() {
    log_info "打包项目..."
    mvn package -DskipTests
    log_success "打包完成"
}

# 运行应用程序
run() {
    log_info "运行应用程序..."
    mvn javafx:run
}

# 创建可执行JAR
create_executable_jar() {
    log_info "创建可执行JAR..."
    mvn spring-boot:repackage
    log_success "可执行JAR创建完成: target/printer-admin-1.0.0.jar"
}

# 创建原生安装包
create_native_package() {
    local platform=${1:-"auto"}
    
    log_info "创建原生安装包 (平台: $platform)..."
    
    # 确保已经打包
    if [ ! -f "target/printer-admin-1.0.0.jar" ]; then
        log_warning "未找到JAR文件，先执行打包..."
        package
        create_executable_jar
    fi
    
    # 使用jpackage创建原生安装包
    case "$platform" in
        "windows"|"win")
            mvn jpackage:jpackage -Djpackage.type=exe
            ;;
        "macos"|"mac")
            mvn jpackage:jpackage -Djpackage.type=dmg
            ;;
        "linux")
            mvn jpackage:jpackage -Djpackage.type=deb
            ;;
        "auto"|*)
            mvn jpackage:jpackage
            ;;
    esac
    
    log_success "原生安装包创建完成"
}

# 运行命令行打印测试
test_command_print() {
    local test_file=${1:-"test.txt"}
    
    log_info "测试命令行打印功能..."
    
    # 创建测试文件
    if [ ! -f "$test_file" ]; then
        echo "这是一个测试打印文件。" > "$test_file"
        echo "PrinterCloud Java Admin 命令行打印测试" >> "$test_file"
        echo "时间: $(date)" >> "$test_file"
    fi
    
    # 确保JAR文件存在
    if [ ! -f "target/printer-admin-1.0.0.jar" ]; then
        log_warning "未找到JAR文件，先执行打包..."
        package
        create_executable_jar
    fi
    
    # 测试命令行打印
    java -jar target/printer-admin-1.0.0.jar --print --file="$test_file" --copies=1
    
    # 清理测试文件
    rm -f "$test_file"
    
    log_success "命令行打印测试完成"
}

# 显示帮助信息
show_help() {
    echo "PrinterCloud Java Admin 构建脚本"
    echo ""
    echo "用法: $0 [命令] [选项]"
    echo ""
    echo "命令:"
    echo "  clean              清理构建目录"
    echo "  compile            编译项目"
    echo "  test               运行测试"
    echo "  package            打包项目"
    echo "  run                运行应用程序"
    echo "  jar                创建可执行JAR"
    echo "  native [platform]  创建原生安装包"
    echo "  test-print [file]  测试命令行打印"
    echo "  all                执行完整构建流程"
    echo "  help               显示此帮助信息"
    echo ""
    echo "原生打包平台:"
    echo "  windows            Windows .exe安装包"
    echo "  macos              macOS .dmg安装包"
    echo "  linux              Linux .deb安装包"
    echo "  auto               自动检测当前平台"
    echo ""
    echo "示例:"
    echo "  $0 all                    # 完整构建流程"
    echo "  $0 run                    # 运行应用程序"
    echo "  $0 native windows         # 创建Windows安装包"
    echo "  $0 test-print test.pdf    # 测试打印PDF文件"
}

# 完整构建流程
build_all() {
    log_info "开始完整构建流程..."
    check_java
    clean
    compile
    test
    package
    create_executable_jar
    log_success "完整构建流程完成！"
    
    echo ""
    echo "构建产物:"
    echo "  - JAR文件: target/printer-admin-1.0.0.jar"
    echo ""
    echo "运行方式:"
    echo "  - GUI模式: java -jar target/printer-admin-1.0.0.jar"
    echo "  - 命令行打印: java -jar target/printer-admin-1.0.0.jar --print --file=<文件路径>"
}

# 主逻辑
case "${1:-help}" in
    "clean")
        check_java
        clean
        ;;
    "compile")
        check_java
        compile
        ;;
    "test")
        check_java
        test
        ;;
    "package")
        check_java
        package
        ;;
    "run")
        check_java
        run
        ;;
    "jar")
        check_java
        package
        create_executable_jar
        ;;
    "native")
        check_java
        create_native_package "$2"
        ;;
    "test-print")
        check_java
        test_command_print "$2"
        ;;
    "all")
        build_all
        ;;
    "help"|"--help"|"-h")
        show_help
        ;;
    *)
        log_error "未知命令: $1"
        show_help
        exit 1
        ;;
esac

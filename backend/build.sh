#!/bin/bash

# 云打印系统后端构建脚本
# 支持编译、运行、测试等操作

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 显示横幅
show_banner() {
    echo -e "${BLUE}"
    echo "=================================================="
    echo "        云打印系统后端构建脚本 v1.0.0"
    echo "=================================================="
    echo -e "${NC}"
}

# 检查Java版本
check_java() {
    log_info "检查Java版本..."
    
    if ! command -v java &> /dev/null; then
        log_error "Java未安装，请先安装Java 8+"
        exit 1
    fi
    
    log_info "Java版本: $(java -version 2>&1 | head -n 1)"
}

# 检查Maven
check_maven() {
    log_info "检查Maven..."
    
    if ! command -v mvn &> /dev/null; then
        log_error "Maven未安装，请先安装Maven 3.6+"
        exit 1
    fi
    
    log_info "Maven版本: $(mvn -version | head -n 1)"
}

# 清理项目
clean_project() {
    log_info "清理项目..."
    mvn clean
    log_info "项目清理完成"
}

# 编译项目
compile_project() {
    log_info "编译项目..."
    mvn compile
    log_info "项目编译完成"
}

# 运行测试
run_tests() {
    log_info "运行测试..."
    mvn test
    log_info "测试完成"
}

# 打包项目
package_project() {
    log_info "打包项目..."
    mvn package -DskipTests
    log_info "项目打包完成"
}

# 运行应用
run_app() {
    log_info "启动应用..."
    mvn spring-boot:run
}

# 健康检查
health_check() {
    log_info "执行健康检查..."
    
    local url="http://localhost:8080/api/test/health"
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f -s "$url" > /dev/null 2>&1; then
            log_info "健康检查通过 ✓"
            curl -s "$url" | python3 -m json.tool 2>/dev/null || curl -s "$url"
            return 0
        fi
        
        log_info "健康检查失败，重试 $attempt/$max_attempts..."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    log_error "健康检查失败，服务可能未正常启动"
    return 1
}

# 显示帮助信息
show_help() {
    echo "云打印系统后端构建脚本"
    echo ""
    echo "用法: $0 [命令]"
    echo ""
    echo "命令:"
    echo "  clean       清理项目"
    echo "  compile     编译项目"
    echo "  test        运行测试"
    echo "  package     打包项目"
    echo "  build       完整构建（清理+编译+测试+打包）"
    echo "  run         运行应用"
    echo "  health      健康检查"
    echo "  help        显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 build       # 完整构建项目"
    echo "  $0 run         # 启动应用"
    echo "  $0 health      # 健康检查"
}

# 主函数
main() {
    show_banner
    
    # 默认命令
    COMMAND=${1:-help}
    
    case $COMMAND in
        clean)
            check_maven
            clean_project
            ;;
        compile)
            check_java
            check_maven
            compile_project
            ;;
        test)
            check_java
            check_maven
            run_tests
            ;;
        package)
            check_java
            check_maven
            package_project
            ;;
        build)
            check_java
            check_maven
            clean_project
            compile_project
            run_tests
            package_project
            log_info "完整构建完成 ✓"
            ;;
        run)
            check_java
            check_maven
            run_app
            ;;
        health)
            health_check
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            log_error "未知命令: $COMMAND"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"

# 部署说明（OnlyOffice + Backend + 前端小程序对接）

本说明指导你在生产或准生产环境部署 OnlyOffice Document Server 与后端应用，使小程序端具备：
- 精准的 Word/PDF 页数检测（通过 OnlyOffice 转 PDF + PDFBox 计页）；
- JWT 鉴权；
- 价格计算后端化；

## 1. 前置要求
- Docker 20+ / Docker Compose v2
- 服务器具备对外网络访问（拉取镜像、字体下载）
- 已准备好中文字体（思源系列 / Noto CJK）

## 2. 目录结构

- project-root/
  - backend/                # Spring Boot 后端
  - mini/                   # 小程序代码
  - fonts/                  # 放置中文/常用字体（ttf/otf），会挂载到 OnlyOffice 容器
  - docker-compose.yml      # 服务栈编排
  - DEPLOYMENT.md           # 本文档

## 3. 字体准备（强烈建议）
将以下字体文件拷贝到项目根目录的 fonts/ 目录：
- Noto Sans CJK / Noto Serif CJK（思源黑体/宋体开源版本）
- 常见替代字体：Liberation、DejaVu、等宽字体

OnlyOffice 容器启动时会执行 `documentserver-generate-allfonts.sh` 生成字体索引，确保分页一致性。

## 4. docker-compose 启动

1) 生成 JWT 秘钥（示例）
- Linux/Mac: `openssl rand -hex 32`
- 设置环境变量：`export JWT_SECRET=...`（也可写入 .env 文件）

2) 启动服务
```
docker compose up -d --build
```

3) 观察健康检查
- OnlyOffice: `curl http://localhost:8888/healthcheck` 返回 `true` 即健康
- Backend: `curl http://localhost:8080/actuator/health` 返回 `{"status":"UP"}` 即健康

> 注：compose 中后端端口映射为 `${BACKEND_PORT:-8080}:8080`，你也可统一改成 8082。

## 5. 后端环境变量
- JWT_SECRET：JWT 签名密钥（必须）
- JWT_EXPIRES：Token 过期秒数，默认 86400
- ONLYOFFICE_URL：OnlyOffice 服务地址，compose 已设置为 `http://documentserver:80`
- 其他（若启用数据库/Redis/MinIO 等）：参照 docker-compose.yml 中 backend 的 environment

## 6. 验证功能

- 登录
```
curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"code":"test-code"}' | jq
```
- 上传 PDF/Word
```
TOKEN=... # 上一步返回的 data.token
curl -s -X POST "http://localhost:8080/api/file/upload" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/document.docx" -F "fileName=document.docx" | jq
```
响应中的 data.pageCount 应为准确页数（PDF/Word）。若 pageCount=0，表示页数解析失败（仍保存文件），前端需提示用户输入页数。

- 价格计算
```
curl -s "http://localhost:8080/api/price/calculate?colorType=BW&paperSize=A4&duplex=SINGLE&pages=5&copies=2" | jq
```

## 7. 安全与生产建议
- 只在内网暴露 OnlyOffice（compose 中 8888:80 仅供排查，可移除或限定安全组）
- 后端应置于反向代理（nginx/ingress）后，启用 HTTPS
- 为 OnlyOffice 与 Backend 增加资源限制（CPU/内存），避免互相影响
- 配置日志采集与监控（容器日志、健康检查告警）
- 字体目录只读挂载，避免被篡改

## 8. 常见问题
- 页数不一致：检查字体是否完整；OnlyOffice 容器重启尝试重新生成字体缓存
- 转换失败：查看 documentserver 容器日志，检查 `ConvertService.ashx` 返回的 error code
- 小程序价格不显示：确认 config 页已输入页数且 /price/calculate 能访问

## 9. 进一步工作
- 若恢复 Excel 支持：在后端开启 doc/xls/xlsx → PDF 路线，同样通过 OnlyOffice 转换
- 增加端到端测试集：覆盖多种中文字体、复杂格式文档
- 接入网关/鉴权中间件：保护后端 API


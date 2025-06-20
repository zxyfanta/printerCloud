# 微信小程序测试指南

## 修复内容总结

### 1. ✅ 文件上传域名问题
**问题：** `url not in domain list`
**解决方案：**
- 修正API地址：`http://localhost:8080/api` → `http://localhost:8082/api`
- 在微信开发者工具中关闭域名校验

### 2. ✅ 文件上传后自动跳转
**问题：** 上传成功后没有自动跳转到配置页面
**解决方案：**
- 在 `upload.js` 中添加延迟1秒自动跳转逻辑

### 3. ✅ 服务统计迁移
**变更：** 从首页迁移到订单页面
**实现：**
- 后端新增 `/auth/user/stats` API接口
- 前端订单页面添加统计数据显示

### 4. ✅ 首页业务模块重构
**变更：** 四个新业务模块
- 文档打印（保留）
- 二手商品（新增）
- 优惠购（新增）
- 电子产品（新增）

## 测试步骤

### 准备工作
1. 启动后端服务（端口8082）
2. 在微信开发者工具中打开小程序项目
3. 在开发者工具中关闭域名校验：
   - 点击右上角"详情"
   - 勾选"不校验合法域名..."

### 测试1：文件上传功能
1. 点击首页"开始打印"按钮
2. 选择文件上传
3. 验证上传成功后自动跳转到配置页面

**预期结果：**
- 文件上传成功
- 显示"文件上传成功"提示
- 1秒后自动跳转到配置页面

### 测试2：订单页面统计
1. 切换到"订单"标签页
2. 查看页面顶部的统计信息

**预期结果：**
- 显示统计卡片：总订单数、打印页数、累计消费
- 如果后端数据库为空，显示模拟数据：5订单、128页、¥25.60

### 测试3：首页业务模块
1. 在首页查看四个业务模块
2. 点击各个模块测试

**预期结果：**
- 文档打印：跳转到上传页面
- 二手商品：显示"功能开发中"
- 优惠购：显示"功能开发中"
- 电子产品：显示"功能开发中"

### 测试4：页面样式
1. 检查各页面的商务风格设计
2. 测试交互动画效果

**预期结果：**
- 深蓝色商务风格主题
- 卡片悬浮效果
- 按钮点击动画
- 渐变色彩搭配

## 常见问题排查

### 问题1：统计数据显示0
**原因：** 数据库中没有订单数据
**解决：** 正常现象，新用户或测试环境会显示模拟数据

### 问题2：文件上传仍然失败
**检查：**
1. 后端服务是否在8082端口运行
2. 微信开发者工具是否关闭域名校验
3. 网络连接是否正常

### 问题3：页面样式异常
**检查：**
1. 清除小程序缓存重新编译
2. 检查wxss文件是否正确加载

### 问题4：API请求失败
**检查：**
1. 后端服务状态
2. API路径是否正确
3. 请求参数格式

## API接口列表

### 新增接口
- `GET /auth/user/stats` - 获取用户统计数据

### 现有接口
- `POST /auth/login` - 用户登录
- `GET /orders` - 获取订单列表
- `POST /upload` - 文件上传

## 数据库表结构

### 用户统计相关字段
- `print_order.user_id` - 用户ID
- `print_order.copies` - 打印份数
- `print_order.page_count` - 页数
- `print_order.amount` - 订单金额

## 后续开发建议

### 短期任务
1. 完善新业务模块的具体功能
2. 添加更多测试数据
3. 优化错误处理机制

### 长期规划
1. 实现二手商品交易功能
2. 开发优惠购商城
3. 添加电子产品销售
4. 完善用户体验

## 注意事项
1. 当前为开发环境配置
2. 生产环境需要配置HTTPS域名
3. 建议定期备份代码和数据
4. 新功能模块需要后续开发

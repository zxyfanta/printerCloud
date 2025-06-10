# 图标文件说明

## 缺失的图标文件

当前项目需要以下图标文件：

### TabBar 图标
- `home.png` - 首页图标 (81x81px)
- `home-active.png` - 首页选中图标 (81x81px)
- `order.png` - 订单图标 (81x81px)
- `order-active.png` - 订单选中图标 (81x81px)
- `profile.png` - 个人中心图标 (81x81px)
- `profile-active.png` - 个人中心选中图标 (81x81px)

### 其他图标
- `banner.png` - 首页横幅图片
- `upload.png` - 上传图标
- `price.png` - 价格图标
- `help.png` - 帮助图标
- `default-avatar.png` - 默认头像

## 图标规范

### TabBar 图标要求
- 尺寸：81x81px (3倍图)
- 格式：PNG
- 背景：透明
- 颜色：单色图标，系统会自动应用主题色

### 获取图标的方式

1. **使用 iconfont**
   - 访问 https://www.iconfont.cn/
   - 搜索相关图标
   - 下载 PNG 格式

2. **使用免费图标库**
   - Feather Icons: https://feathericons.com/
   - Heroicons: https://heroicons.com/
   - Tabler Icons: https://tabler-icons.io/

3. **自己设计**
   - 使用 Figma、Sketch 等设计工具
   - 保持简洁的线条风格
   - 确保在小尺寸下清晰可见

## 临时解决方案

当前已将 `app.json` 中的图标配置移除，使用纯文字 TabBar。
如需添加图标，请：

1. 将图标文件放入 `miniprogram/images/` 目录
2. 在 `app.json` 中恢复图标配置：

```json
{
  "pagePath": "pages/index/index",
  "text": "首页",
  "iconPath": "images/home.png",
  "selectedIconPath": "images/home-active.png"
}
```

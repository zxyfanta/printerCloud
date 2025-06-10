# 图标下载指南

## 快速获取图标

### 1. 使用 Iconfont (推荐)

访问 [iconfont.cn](https://www.iconfont.cn/)，搜索以下关键词：

- **首页图标**: 搜索 "home" 或 "首页"
- **订单图标**: 搜索 "order" 或 "订单" 或 "list"
- **个人中心图标**: 搜索 "user" 或 "profile" 或 "个人"

下载步骤：
1. 选择图标 → 加入购物车
2. 点击购物车 → 下载代码
3. 选择 PNG 格式
4. 设置尺寸为 81x81px
5. 下载并重命名文件

### 2. 使用 Feather Icons

访问 [feathericons.com](https://feathericons.com/)

推荐图标：
- 首页: `home`
- 订单: `file-text` 或 `list`
- 个人: `user`

### 3. 使用 Heroicons

访问 [heroicons.com](https://heroicons.com/)

推荐图标：
- 首页: `home`
- 订单: `document-text`
- 个人: `user`

## 图标命名规范

下载后请按以下方式重命名：

```
home.png              # 首页普通状态
home-active.png       # 首页选中状态
order.png             # 订单普通状态  
order-active.png      # 订单选中状态
profile.png           # 个人中心普通状态
profile-active.png    # 个人中心选中状态
```

## 恢复图标配置

下载图标后，在 `app.json` 中恢复以下配置：

```json
"tabBar": {
  "color": "#666666",
  "selectedColor": "#1890ff", 
  "backgroundColor": "#ffffff",
  "borderStyle": "black",
  "list": [
    {
      "pagePath": "pages/index/index",
      "text": "首页",
      "iconPath": "images/home.png",
      "selectedIconPath": "images/home-active.png"
    },
    {
      "pagePath": "pages/orders/orders", 
      "text": "订单",
      "iconPath": "images/order.png",
      "selectedIconPath": "images/order-active.png"
    },
    {
      "pagePath": "pages/profile/profile",
      "text": "我的", 
      "iconPath": "images/profile.png",
      "selectedIconPath": "images/profile-active.png"
    }
  ]
}
```

## 其他需要的图标

项目中还需要以下图标，可以同样方式获取：

- `banner.png` - 首页横幅 (建议 300x150px)
- `upload.png` - 上传图标 (64x64px)
- `price.png` - 价格图标 (64x64px) 
- `help.png` - 帮助图标 (64x64px)
- `default-avatar.png` - 默认头像 (100x100px)

## 注意事项

1. **尺寸要求**: TabBar 图标必须是 81x81px
2. **格式要求**: 必须是 PNG 格式
3. **背景要求**: 建议使用透明背景
4. **颜色要求**: 普通状态建议使用灰色，选中状态会自动应用主题色

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import webSocketService from './utils/websocket'
import ECharts from 'vue-echarts'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// 全局注册ECharts组件
app.component('VChart', ECharts)

// 初始化WebSocket连接
webSocketService.connect().catch(error => {
  console.error('WebSocket连接失败:', error)
})

// 在应用卸载时断开WebSocket连接
window.addEventListener('beforeunload', () => {
  webSocketService.disconnect()
})

app.mount('#app')

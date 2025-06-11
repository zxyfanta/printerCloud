import { Client } from '@stomp/stompjs'
import { ElNotification } from 'element-plus'

class WebSocketService {
  constructor() {
    this.client = null
    this.connected = false
    this.subscriptions = new Map()
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 5000
    this.enabled = true // 启用WebSocket
  }

  connect() {
    if (!this.enabled || this.connected) {
      return Promise.resolve()
    }

    return new Promise((resolve, reject) => {
      this.client = new Client({
        webSocketFactory: () => new WebSocket('ws://localhost:8080/api/ws'),
        connectHeaders: {},
        debug: (str) => {
          console.log('STOMP Debug:', str)
        },
        reconnectDelay: this.reconnectInterval,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: (frame) => {
          console.log('WebSocket连接成功:', frame)
          this.connected = true
          this.reconnectAttempts = 0
          this.setupSubscriptions()
          resolve()
        },
        onStompError: (frame) => {
          console.error('STOMP错误:', frame.headers['message'])
          console.error('详细信息:', frame.body)
          this.connected = false
          reject(new Error(frame.headers['message']))
        },
        onWebSocketClose: (event) => {
          console.log('WebSocket连接关闭:', event)
          this.connected = false
          if (this.enabled) {
            this.handleReconnect()
          }
        },
        onWebSocketError: (event) => {
          console.error('WebSocket错误:', event)
          this.connected = false
        }
      })

      this.client.activate()
    })
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate()
      this.connected = false
      this.subscriptions.clear()
    }
  }

  setEnabled(enabled) {
    this.enabled = enabled
    if (!enabled && this.connected) {
      this.disconnect()
    } else if (enabled && !this.connected) {
      this.connect().catch(error => {
        console.error('WebSocket连接失败:', error)
      })
    }
  }

  handleReconnect() {
    if (!this.enabled) return
    
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`)
      
      setTimeout(() => {
        this.connect().catch(error => {
          console.error('重连失败:', error)
        })
      }, this.reconnectInterval)
    } else {
      console.error('达到最大重连次数，停止重连')
      ElNotification({
        title: '连接失败',
        message: '无法连接到服务器，已切换到轮询模式',
        type: 'warning',
        duration: 5000
      })
    }
  }

  setupSubscriptions() {
    // 订阅新订单通知
    this.subscribe('/topic/newOrders', (message) => {
      const notification = JSON.parse(message.body)
      this.handleNewOrderNotification(notification)
    })

    // 订阅订单状态更新通知
    this.subscribe('/topic/orderUpdates', (message) => {
      const notification = JSON.parse(message.body)
      this.handleOrderUpdateNotification(notification)
    })

    // 订阅系统通知
    this.subscribe('/topic/system', (message) => {
      const notification = JSON.parse(message.body)
      this.handleSystemNotification(notification)
    })
  }

  subscribe(destination, callback) {
    if (!this.client || !this.connected) {
      console.warn('WebSocket未连接，无法订阅:', destination)
      return
    }

    const subscription = this.client.subscribe(destination, callback)
    this.subscriptions.set(destination, subscription)
    return subscription
  }

  unsubscribe(destination) {
    const subscription = this.subscriptions.get(destination)
    if (subscription) {
      subscription.unsubscribe()
      this.subscriptions.delete(destination)
    }
  }

  send(destination, body = {}) {
    if (!this.client || !this.connected) {
      console.warn('WebSocket未连接，无法发送消息')
      return
    }

    this.client.publish({
      destination,
      body: JSON.stringify(body)
    })
  }

  handleNewOrderNotification(notification) {
    ElNotification({
      title: notification.title,
      message: notification.message,
      type: 'success',
      duration: 8000,
      onClick: () => {
        // 跳转到订单详情
        this.navigateToOrderDetail(notification.orderId)
      }
    })

    // 触发自定义事件，让其他组件可以监听
    window.dispatchEvent(new CustomEvent('newOrder', { detail: notification }))
  }

  handleOrderUpdateNotification(notification) {
    ElNotification({
      title: notification.title,
      message: notification.message,
      type: 'info',
      duration: 5000
    })

    // 触发自定义事件
    window.dispatchEvent(new CustomEvent('orderUpdate', { detail: notification }))
  }

  handleSystemNotification(notification) {
    ElNotification({
      title: notification.title,
      message: notification.message,
      type: notification.type === 'ERROR' ? 'error' : 'info',
      duration: notification.type === 'ERROR' ? 0 : 5000
    })
  }

  navigateToOrderDetail(orderId) {
    // 这里需要根据路由系统来实现跳转
    // 可以通过事件或者直接操作路由
    window.dispatchEvent(new CustomEvent('navigateToOrder', { 
      detail: { orderId } 
    }))
  }



  // 获取连接状态
  isConnected() {
    return this.connected
  }

  // 获取启用状态
  isEnabled() {
    return this.enabled
  }
}

// 创建单例实例
const webSocketService = new WebSocketService()

export default webSocketService

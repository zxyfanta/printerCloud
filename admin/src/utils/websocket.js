import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { ElNotification } from 'element-plus'

/**
 * WebSocket服务类
 * 使用STOMP over SockJS实现稳定的WebSocket连接
 *
 * 特性：
 * - 自动重连机制
 * - 心跳检测
 * - 错误处理
 * - 环境适配
 */
class WebSocketService {
  constructor() {
    this.client = null
    this.connected = false
    this.connecting = false
    this.subscriptions = new Map()
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000 // 初始重连间隔3秒
    this.enabled = true
    this.connectionPromise = null

    // 根据环境配置WebSocket服务器地址
    this.serverUrl = this.getServerUrl()

    console.log('WebSocket服务初始化，服务器地址:', this.serverUrl)
  }

  getServerUrl() {
    // 开发环境
    if (import.meta.env.DEV) {
      return 'http://localhost:8082/api/ws'
    }

    // 生产环境，使用当前域名
    const protocol = window.location.protocol === 'https:' ? 'https:' : 'http:'
    const host = window.location.host
    return `${protocol}//${host}/api/ws`
  }

  connect() {
    // 如果未启用或已连接，直接返回
    if (!this.enabled) {
      return Promise.resolve()
    }

    if (this.connected) {
      return Promise.resolve()
    }

    // 如果正在连接中，返回现有的连接Promise
    if (this.connecting && this.connectionPromise) {
      return this.connectionPromise
    }

    this.connecting = true
    this.connectionPromise = new Promise((resolve, reject) => {
      try {
        console.log('开始建立WebSocket连接...')

        this.client = new Client({
          // 使用SockJS连接，确保与后端配置匹配
          webSocketFactory: () => {
            console.log('创建SockJS连接到:', this.serverUrl)
            return new SockJS(this.serverUrl)
          },

          // 连接头信息
          connectHeaders: {
            'Accept-Version': '1.0,1.1,2.0',
            'heart-beat': '10000,10000'
          },

          // 调试信息
          debug: (str) => {
            console.log('STOMP Debug:', str)
          },

          // 重连配置（禁用自动重连，我们手动控制）
          reconnectDelay: 0,

          // 心跳配置
          heartbeatIncoming: 10000,  // 10秒
          heartbeatOutgoing: 10000,  // 10秒

          // 连接成功回调
          onConnect: (frame) => {
            console.log('WebSocket连接成功:', frame)
            this.connected = true
            this.connecting = false
            this.reconnectAttempts = 0
            this.connectionPromise = null

            // 设置订阅
            this.setupSubscriptions()

            // 显示连接成功通知
            ElNotification({
              title: '连接成功',
              message: 'WebSocket连接已建立，实时通知已启用',
              type: 'success',
              duration: 3000
            })

            resolve()
          },

          // STOMP协议错误回调
          onStompError: (frame) => {
            console.error('STOMP错误:', frame.headers['message'])
            console.error('详细信息:', frame.body)
            this.connected = false
            this.connecting = false
            this.connectionPromise = null

            const errorMsg = frame.headers['message'] || '连接协议错误'

            // 显示STOMP错误通知
            ElNotification({
              title: 'STOMP连接错误',
              message: errorMsg,
              type: 'error',
              duration: 5000
            })

            reject(new Error(errorMsg))
          },

          // WebSocket连接关闭回调
          onWebSocketClose: (event) => {
            console.log('WebSocket连接关闭:', event)
            this.connected = false
            this.connecting = false
            this.connectionPromise = null

            // 如果启用且不是主动断开，则尝试重连
            if (this.enabled && !event.wasClean) {
              this.handleReconnect()
            }
          },

          // WebSocket连接错误回调
          onWebSocketError: (event) => {
            console.error('WebSocket错误:', event)
            this.connected = false
            this.connecting = false

            // 显示WebSocket错误通知
            ElNotification({
              title: 'WebSocket连接错误',
              message: '网络连接异常，正在尝试重连...',
              type: 'warning',
              duration: 3000
            })

            // 如果是在连接过程中出错，reject Promise
            if (this.connectionPromise) {
              reject(new Error('WebSocket连接失败'))
            }
          }
        })

        // 激活客户端连接
        this.client.activate()

      } catch (error) {
        console.error('创建WebSocket客户端失败:', error)
        this.connecting = false
        this.connectionPromise = null
        reject(error)
      }
    })

    return this.connectionPromise
  }

  disconnect() {
    console.log('断开WebSocket连接')
    this.enabled = false
    this.connecting = false
    this.connectionPromise = null

    if (this.client) {
      try {
        this.client.deactivate()
      } catch (error) {
        console.error('断开连接时出错:', error)
      }
    }

    this.connected = false
    this.subscriptions.clear()
    this.reconnectAttempts = 0

    console.log('WebSocket连接已断开')
  }

  setEnabled(enabled) {
    console.log('设置WebSocket启用状态:', enabled)

    if (this.enabled === enabled) {
      console.log('WebSocket启用状态未改变')
      return Promise.resolve()
    }

    this.enabled = enabled

    if (!enabled) {
      // 禁用WebSocket，断开连接
      this.disconnect()
      return Promise.resolve()
    } else {
      // 启用WebSocket，尝试连接
      if (!this.connected && !this.connecting) {
        return this.connect().catch(error => {
          console.error('启用WebSocket时连接失败:', error)
          throw error
        })
      }
      return Promise.resolve()
    }
  }

  handleReconnect() {
    // 如果未启用或正在连接中，不进行重连
    if (!this.enabled || this.connecting) {
      return
    }

    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`)

      // 使用指数退避策略，避免频繁重连
      const delay = Math.min(this.reconnectInterval * Math.pow(1.5, this.reconnectAttempts - 1), 30000)
      console.log(`${delay}ms后开始重连...`)

      setTimeout(() => {
        if (!this.enabled) {
          console.log('WebSocket已被禁用，取消重连')
          return
        }

        this.connect().catch(error => {
          console.error(`第${this.reconnectAttempts}次重连失败:`, error)

          // 如果达到最大重连次数，显示最终失败通知
          if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.error('达到最大重连次数，停止重连')
            ElNotification({
              title: '连接失败',
              message: '无法连接到服务器，请检查网络连接或联系管理员。系统将切换到轮询模式。',
              type: 'error',
              duration: 0, // 不自动关闭
              showClose: true
            })
          }
        })
      }, delay)
    } else {
      console.error('达到最大重连次数，停止重连')
      ElNotification({
        title: '连接失败',
        message: '无法连接到服务器，已切换到轮询模式。请检查后端服务是否正常运行。',
        type: 'warning',
        duration: 8000
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

  // 测试连接
  async testConnection() {
    try {
      console.log('测试WebSocket连接到:', this.serverUrl)

      // 创建测试连接
      const testSocket = new SockJS(this.serverUrl)

      return new Promise((resolve, reject) => {
        const timeout = setTimeout(() => {
          testSocket.close()
          reject(new Error('连接超时'))
        }, 10000)

        testSocket.onopen = () => {
          clearTimeout(timeout)
          testSocket.close()
          console.log('WebSocket连接测试成功')
          resolve(true)
        }

        testSocket.onerror = (error) => {
          clearTimeout(timeout)
          console.error('WebSocket连接测试失败:', error)
          reject(error)
        }

        testSocket.onclose = (event) => {
          if (event.wasClean) {
            console.log('WebSocket测试连接正常关闭')
          }
        }
      })
    } catch (error) {
      console.error('WebSocket连接测试异常:', error)
      throw error
    }
  }

  // 获取连接信息
  getConnectionInfo() {
    return {
      serverUrl: this.serverUrl,
      connected: this.connected,
      enabled: this.enabled,
      reconnectAttempts: this.reconnectAttempts,
      maxReconnectAttempts: this.maxReconnectAttempts,
      subscriptions: Array.from(this.subscriptions.keys())
    }
  }
}

// 创建单例实例
const webSocketService = new WebSocketService()

export default webSocketService

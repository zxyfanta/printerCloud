import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/utils/api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('adminToken') || '')
  const userInfo = ref(null)

  const isLoggedIn = computed(() => !!token.value)

  // 登录
  const login = async (username, password) => {
    try {
      const response = await api.post('/auth/login', {
        username,
        password,
        loginType: 'ADMIN'
      })
      
      if (response.data.code === 200) {
        token.value = response.data.data.token
        userInfo.value = response.data.data.userInfo
        localStorage.setItem('adminToken', token.value)
        return { success: true }
      } else {
        return { success: false, message: response.data.message }
      }
    } catch (error) {
      return { success: false, message: error.message }
    }
  }

  // 获取用户信息
  const getUserInfo = async () => {
    try {
      const response = await api.get('/auth/userinfo')
      if (response.data.code === 200) {
        userInfo.value = response.data.data
        return true
      } else {
        logout()
        return false
      }
    } catch (error) {
      logout()
      return false
    }
  }

  // 登出
  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('adminToken')
  }

  // 检查token有效性
  const checkToken = async () => {
    if (!token.value) return false
    return await getUserInfo()
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    logout,
    getUserInfo,
    checkToken
  }
})

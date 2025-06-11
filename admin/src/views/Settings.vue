<template>
  <div class="settings-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统设置</span>
          <el-button type="primary" @click="saveSettings" :loading="saving">
            <el-icon><Check /></el-icon>
            保存设置
          </el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" type="border-card">
        <!-- 实时更新设置 -->
        <el-tab-pane label="实时更新" name="polling">
          <el-form
            ref="pollingFormRef"
            :model="pollingSettings"
            :rules="pollingRules"
            label-width="120px"
            style="max-width: 600px"
          >
            <el-form-item label="更新方式" prop="updateMode">
              <el-radio-group v-model="pollingSettings.updateMode" @change="handleUpdateModeChange">
                <el-radio value="polling">轮询刷新</el-radio>
                <el-radio value="websocket" disabled>WebSocket实时推送（开发中）</el-radio>
              </el-radio-group>
              <div class="form-tip">
                当前使用轮询模式定期刷新数据，WebSocket功能正在开发中
              </div>
            </el-form-item>

            <el-form-item label="启用更新" prop="enabled">
              <el-switch
                v-model="pollingSettings.enabled"
                active-text="启用"
                inactive-text="禁用"
              />
              <div class="form-tip">
                启用后系统将自动获取最新数据
              </div>
            </el-form-item>

            <el-form-item
              label="轮询间隔"
              prop="interval"
              v-show="pollingSettings.updateMode === 'polling'"
            >
              <el-input-number
                v-model="pollingSettings.interval"
                :min="1000"
                :max="60000"
                :step="1000"
                :disabled="!pollingSettings.enabled || pollingSettings.updateMode !== 'polling'"
                style="width: 200px"
              />
              <span style="margin-left: 10px">毫秒</span>
              <div class="form-tip">
                设置自动刷新的时间间隔，建议设置为3-10秒（3000-10000毫秒）
              </div>
            </el-form-item>

            <el-form-item
              label="预设选项"
              v-show="pollingSettings.updateMode === 'polling'"
            >
              <el-radio-group
                v-model="pollingPreset"
                @change="applyPreset"
                :disabled="!pollingSettings.enabled || pollingSettings.updateMode !== 'polling'"
              >
                <el-radio :value="3000">3秒（快速）</el-radio>
                <el-radio :value="5000">5秒（推荐）</el-radio>
                <el-radio :value="10000">10秒（节能）</el-radio>
                <el-radio :value="30000">30秒（低频）</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 通知设置 -->
        <el-tab-pane label="通知设置" name="notification">
          <el-form
            ref="notificationFormRef"
            :model="notificationSettings"
            label-width="120px"
            style="max-width: 600px"
          >
            <el-form-item label="新订单通知">
              <el-switch
                v-model="notificationSettings.newOrder"
                active-text="启用"
                inactive-text="禁用"
              />
              <div class="form-tip">
                有新订单时显示浏览器通知
              </div>
            </el-form-item>

            <el-form-item label="订单状态更新">
              <el-switch
                v-model="notificationSettings.statusUpdate"
                active-text="启用"
                inactive-text="禁用"
              />
              <div class="form-tip">
                订单状态发生变化时显示通知
              </div>
            </el-form-item>

            <el-form-item label="声音提醒">
              <el-switch
                v-model="notificationSettings.sound"
                active-text="启用"
                inactive-text="禁用"
              />
              <div class="form-tip">
                通知时播放提示音
              </div>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 显示设置 -->
        <el-tab-pane label="显示设置" name="display">
          <el-form
            ref="displayFormRef"
            :model="displaySettings"
            label-width="120px"
            style="max-width: 600px"
          >
            <el-form-item label="默认视图模式">
              <el-radio-group v-model="displaySettings.defaultViewMode">
                <el-radio value="card">卡片视图</el-radio>
                <el-radio value="table">表格视图</el-radio>
              </el-radio-group>
              <div class="form-tip">
                设置订单列表的默认显示模式
              </div>
            </el-form-item>

            <el-form-item label="每页显示数量">
              <el-select v-model="displaySettings.pageSize" style="width: 200px">
                <el-option label="10条" :value="10" />
                <el-option label="20条" :value="20" />
                <el-option label="50条" :value="50" />
                <el-option label="100条" :value="100" />
              </el-select>
              <div class="form-tip">
                设置订单列表每页显示的数量
              </div>
            </el-form-item>

            <el-form-item label="自动刷新提示">
              <el-switch
                v-model="displaySettings.showRefreshTip"
                active-text="显示"
                inactive-text="隐藏"
              />
              <div class="form-tip">
                轮询刷新时是否显示提示信息
              </div>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'
import webSocketService from '@/utils/websocket'

const activeTab = ref('polling')
const saving = ref(false)
const pollingFormRef = ref()
const notificationFormRef = ref()
const displayFormRef = ref()

// 实时更新设置
const pollingSettings = reactive({
  enabled: true,
  interval: 5000,
  updateMode: 'websocket' // websocket | polling
})

const pollingPreset = ref(5000)

const pollingRules = {
  interval: [
    { required: true, message: '请设置轮询间隔', trigger: 'blur' },
    { type: 'number', min: 1000, max: 60000, message: '轮询间隔必须在1-60秒之间', trigger: 'blur' }
  ]
}

// 通知设置
const notificationSettings = reactive({
  newOrder: true,
  statusUpdate: true,
  sound: false
})

// 显示设置
const displaySettings = reactive({
  defaultViewMode: 'card',
  pageSize: 20,
  showRefreshTip: true
})

onMounted(() => {
  loadSettings()
})

const loadSettings = async () => {
  try {
    // 首先尝试从后端加载设置
    const response = await api.get('/config/admin/list')
    if (response.data.code === 200) {
      const configs = response.data.data

      // 解析后端配置
      configs.forEach(config => {
        switch (config.configKey) {
          case 'polling.enabled':
            pollingSettings.enabled = config.getBooleanValue()
            break
          case 'polling.interval':
            pollingSettings.interval = config.getIntValue() || 5000
            pollingPreset.value = config.getIntValue() || 5000
            break
          case 'notification.new_order':
            notificationSettings.newOrder = config.getBooleanValue()
            break
          case 'notification.status_update':
            notificationSettings.statusUpdate = config.getBooleanValue()
            break
          case 'notification.sound':
            notificationSettings.sound = config.getBooleanValue()
            break
          case 'display.default_view_mode':
            displaySettings.defaultViewMode = config.configValue || 'card'
            break
          case 'display.page_size':
            displaySettings.pageSize = config.getIntValue() || 20
            break
          case 'display.show_refresh_tip':
            displaySettings.showRefreshTip = config.getBooleanValue()
            break
        }
      })
    }
  } catch (error) {
    console.warn('从后端加载设置失败，使用本地设置:', error)
  }

  // 从localStorage加载设置作为备用
  const savedPollingEnabled = localStorage.getItem('pollingEnabled')
  if (savedPollingEnabled !== null) {
    pollingSettings.enabled = savedPollingEnabled === 'true'
  }

  const savedPollingInterval = localStorage.getItem('pollingFrequency')
  if (savedPollingInterval) {
    pollingSettings.interval = parseInt(savedPollingInterval)
    pollingPreset.value = parseInt(savedPollingInterval)
  }

  const savedViewMode = localStorage.getItem('orderViewMode')
  if (savedViewMode) {
    displaySettings.defaultViewMode = savedViewMode
  }

  const savedPageSize = localStorage.getItem('orderPageSize')
  if (savedPageSize) {
    displaySettings.pageSize = parseInt(savedPageSize)
  }

  // 加载通知设置
  const savedNotificationSettings = localStorage.getItem('notificationSettings')
  if (savedNotificationSettings) {
    Object.assign(notificationSettings, JSON.parse(savedNotificationSettings))
  }

  const savedDisplaySettings = localStorage.getItem('displaySettings')
  if (savedDisplaySettings) {
    Object.assign(displaySettings, JSON.parse(savedDisplaySettings))
  }
}

const applyPreset = (value) => {
  pollingSettings.interval = value
}

const handleUpdateModeChange = () => {
  if (pollingSettings.updateMode === 'websocket') {
    webSocketService.setEnabled(pollingSettings.enabled)
  } else {
    webSocketService.setEnabled(false)
  }
}

const saveSettings = async () => {
  try {
    saving.value = true

    // 验证表单
    if (pollingFormRef.value) {
      await pollingFormRef.value.validate()
    }

    // 准备要保存到后端的配置
    const configMap = {
      'polling.enabled': pollingSettings.enabled.toString(),
      'polling.interval': pollingSettings.interval.toString(),
      'notification.new_order': notificationSettings.newOrder.toString(),
      'notification.status_update': notificationSettings.statusUpdate.toString(),
      'notification.sound': notificationSettings.sound.toString(),
      'display.default_view_mode': displaySettings.defaultViewMode,
      'display.page_size': displaySettings.pageSize.toString(),
      'display.show_refresh_tip': displaySettings.showRefreshTip.toString()
    }

    try {
      // 尝试保存到后端
      const response = await api.post('/config/admin/batch-update', configMap)
      if (response.data.code !== 200) {
        throw new Error(response.data.message || '后端保存失败')
      }
    } catch (error) {
      console.warn('保存到后端失败，仅保存到本地:', error)
    }

    // 保存到localStorage作为备用
    localStorage.setItem('pollingEnabled', pollingSettings.enabled.toString())
    localStorage.setItem('pollingFrequency', pollingSettings.interval.toString())
    localStorage.setItem('orderViewMode', displaySettings.defaultViewMode)
    localStorage.setItem('orderPageSize', displaySettings.pageSize.toString())
    localStorage.setItem('notificationSettings', JSON.stringify(notificationSettings))
    localStorage.setItem('displaySettings', JSON.stringify(displaySettings))

    // 通知其他组件更新设置
    window.dispatchEvent(new CustomEvent('settingsUpdated', {
      detail: {
        polling: pollingSettings,
        notification: notificationSettings,
        display: displaySettings
      }
    }))

    ElMessage.success('设置保存成功')
  } catch (error) {
    ElMessage.error('设置保存失败')
    console.error('保存设置失败:', error)
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.settings-page {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}

:deep(.el-tabs__content) {
  padding: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-input-number) {
  width: 200px;
}
</style>

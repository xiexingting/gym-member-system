<template>
  <div class="view-page">
    <div class="page-heading">
      <div>
        <h1>到期提醒</h1>
        <p>每天自动扫描临期与已到期会员，可手动立即刷新名单</p>
      </div>
      <el-button type="primary" :icon="Refresh" :loading="scanning" @click="scanNow">立即扫描</el-button>
    </div>

    <div class="stat-grid">
      <div class="stat-card" v-for="card in statCards" :key="card.label">
        <div class="stat-icon" :style="{ background: card.bg, color: card.color }">
          <el-icon :size="22"><component :is="card.icon" /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-label">{{ card.label }}</span>
          <strong class="stat-value">{{ card.value }}</strong>
          <span class="stat-hint">{{ card.hint }}</span>
        </div>
      </div>
    </div>

    <section class="panel">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-select v-model="filterStatus" placeholder="处理状态" clearable class="filter-select" @change="loadReminders">
            <el-option label="未处理" :value="0" />
            <el-option label="已处理" :value="1" />
          </el-select>
          <el-select v-model="filterType" placeholder="提醒类型" clearable class="filter-select" @change="loadReminders">
            <el-option label="即将到期" :value="1" />
            <el-option label="已到期" :value="2" />
          </el-select>
          <el-button :icon="Refresh" @click="resetFilter">重置筛选</el-button>
        </div>
        <span class="auto-hint">定时任务每天 09:00 自动执行，同一天不会重复生成提醒</span>
      </div>

      <el-table
        :data="reminders"
        v-loading="loading"
        stripe
        style="width: 100%"
        empty-text="暂无提醒记录"
      >
        <el-table-column label="会员" min-width="130">
          <template #default="{ row }">
            <div class="name-cell">
              <span class="name-avatar">{{ (row.memberName || '?').slice(0, 1) }}</span>
              <span>{{ row.memberName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="提醒类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.reminderType === 2 ? 'danger' : 'warning'" effect="light" size="small">
              {{ row.reminderType === 2 ? '已到期' : '即将到期' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="到期时间" width="130">
          <template #default="{ row }">{{ formatDate(row.expireTime) || '—' }}</template>
        </el-table-column>
        <el-table-column label="提醒日期" width="130">
          <template #default="{ row }">{{ formatDate(row.reminderDate) || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain" size="small">
              {{ row.status === 1 ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="处理备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="130" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status !== 1"
              type="primary"
              link
              :icon="CircleCheck"
              @click="openHandle(row)"
            >标记处理</el-button>
            <span v-else class="handled-text">已完成</span>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <!-- 标记处理弹窗 -->
    <el-dialog v-model="handleVisible" title="标记提醒已处理" width="460px" :close-on-click-modal="false" destroy-on-close>
      <div class="handle-info" v-if="currentReminder">
        <strong>{{ currentReminder.memberName }}</strong>
        <span>{{ currentReminder.reminderType === 2 ? '已到期' : '即将到期' }} · 到期 {{ formatDate(currentReminder.expireTime) }}</span>
      </div>
      <el-form label-width="80px" style="margin-top: 16px">
        <el-form-item label="处理备注">
          <el-input v-model="handleRemark" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="例如：已电话通知续费 / 已联系确认不再续卡" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" :loading="handling" @click="submitHandle">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, CircleCheck, Bell, Clock, Calendar, User } from '@element-plus/icons-vue'
import request from '../utils/request'

const reminders = ref([])
const loading = ref(false)
const scanning = ref(false)
const filterStatus = ref(null)
const filterType = ref(null)
const handleVisible = ref(false)
const currentReminder = ref(null)
const handleRemark = ref('')
const handling = ref(false)

const statCards = computed(() => {
  const list = reminders.value
  return [
    { label: '全部提醒', value: list.length, hint: '当前筛选结果', icon: Bell, color: '#5b6cf6', bg: '#eef1ff' },
    { label: '未处理', value: list.filter((r) => r.status !== 1).length, hint: '需要跟进', icon: Clock, color: '#f59e0b', bg: '#fff6e6' },
    { label: '已到期', value: list.filter((r) => r.reminderType === 2).length, hint: '需尽快续费', icon: Calendar, color: '#ef4444', bg: '#fef0f0' },
    { label: '已处理', value: list.filter((r) => r.status === 1).length, hint: '已跟进完成', icon: User, color: '#16a34a', bg: '#e9f9f0' }
  ]
})

const formatDate = (val) => {
  if (!val) return ''
  const d = val instanceof Date ? val : new Date(val)
  if (Number.isNaN(d.getTime())) return ''
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

const loadReminders = async () => {
  loading.value = true
  try {
    const params = {}
    if (filterStatus.value !== null) params.status = filterStatus.value
    if (filterType.value !== null) params.type = filterType.value
    const res = await request.get('/api/reminder/list', { params })
    reminders.value = Array.isArray(res.data) ? res.data : []
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  filterStatus.value = null
  filterType.value = null
  loadReminders()
}

const scanNow = async () => {
  scanning.value = true
  try {
    const res = await request.post('/api/reminder/scan', {})
    ElMessage.success(res.message || '扫描完成')
    loadReminders()
  } catch (err) {
    console.error(err)
  } finally {
    scanning.value = false
  }
}

const openHandle = (row) => {
  currentReminder.value = row
  handleRemark.value = ''
  handleVisible.value = true
}

const submitHandle = async () => {
  if (!currentReminder.value) return
  handling.value = true
  try {
    await request.put(`/api/reminder/handle/${currentReminder.value.reminderId}`, null, {
      params: { remark: handleRemark.value }
    })
    ElMessage.success('已标记为已处理')
    handleVisible.value = false
    loadReminders()
  } catch (err) {
    console.error(err)
  } finally {
    handling.value = false
  }
}

onMounted(loadReminders)
</script>

<style scoped>
.filter-select { width: 140px; }
.auto-hint { font-size: 12px; color: #94a3b8; }
.handle-info { display: flex; flex-direction: column; gap: 4px; padding: 12px 14px; background: #f8f9fd; border-radius: 8px; }
.handle-info strong { color: #16213b; font-size: 14px; }
.handle-info span { color: #94a3b8; font-size: 12px; }
.handled-text { font-size: 12px; color: #94a3b8; }
</style>

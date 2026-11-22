<template>
  <div class="view-page">
    <div class="page-heading">
      <div>
        <h1>课程预约</h1>
        <p>为会员预约课程，系统自动校验重复预约与剩余名额</p>
      </div>
      <el-button type="primary" :icon="Refresh" plain @click="loadAll">刷新数据</el-button>
    </div>

    <div class="stat-grid">
      <div class="stat-card" v-for="c in statCards" :key="c.label">
        <div class="stat-icon" :style="{ background: c.bg, color: c.color }">
          <el-icon :size="22"><component :is="c.icon" /></el-icon>
        </div>
        <div class="stat-body">
          <span class="stat-label">{{ c.label }}</span>
          <strong class="stat-value">{{ c.value }}</strong>
          <span class="stat-hint">{{ c.hint }}</span>
        </div>
      </div>
    </div>

    <section class="panel">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-radio-group v-model="statusFilter">
            <el-radio-button :label="'all'">全部</el-radio-button>
            <el-radio-button :label="1">有效</el-radio-button>
            <el-radio-button :label="0">已取消</el-radio-button>
          </el-radio-group>
          <el-input
            v-model="keyword"
            placeholder="搜索会员或课程"
            clearable
            :prefix-icon="Search"
            class="search-input"
          />
        </div>
        <el-button type="primary" :icon="Plus" @click="openAdd">新增预约</el-button>
      </div>

      <el-table :data="filteredBookings" v-loading="loading" stripe style="width: 100%" empty-text="暂无预约记录">
        <el-table-column label="会员" min-width="120">
          <template #default="{ row }">
            <div class="name-cell">
              <span class="name-avatar booking-avatar"><el-icon><User /></el-icon></span>
              <span>{{ row.memberName || '—' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="courseName" label="课程" min-width="120" />
        <el-table-column label="上课时间" min-width="160" align="center">
          <template #default="{ row }">{{ formatDateTime(row.courseTime) }}</template>
        </el-table-column>
        <el-table-column prop="coachName" label="教练" min-width="100" align="center" />
        <el-table-column label="预约时间" min-width="160" align="center">
          <template #default="{ row }">{{ formatDateTime(row.bookingTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light" size="small">
              {{ row.status === 1 ? '有效' : '已取消' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              type="warning"
              link
              :icon="CircleClose"
              @click="handleCancel(row)"
            >取消预约</el-button>
            <span v-else class="muted-text">—</span>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <!-- 新增预约弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增预约" width="520px" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="选择会员" prop="memberId">
          <el-select v-model="form.memberId" style="width: 100%" placeholder="选择会员" filterable>
            <el-option
              v-for="m in memberOptions"
              :key="m.memberId"
              :label="m.memberName"
              :value="m.memberId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择课程" prop="courseId">
          <el-select v-model="form.courseId" style="width: 100%" placeholder="选择上架课程" @change="onCourseChange">
            <el-option
              v-for="c in availableCourses"
              :key="c.courseId"
              :label="`${c.courseName}（${formatDateTime(c.courseTime)}）`"
              :value="c.courseId"
              :disabled="c.bookedCount >= c.maxCapacity"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="名额情况" v-if="selectedCourse">
          <el-tag :type="selectedCourse.bookedCount >= selectedCourse.maxCapacity ? 'danger' : 'success'">
            已预约 {{ selectedCourse.bookedCount }} / {{ selectedCourse.maxCapacity }} 人
          </el-tag>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="选填" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, User, Tickets, CircleCheck, CircleClose, Calendar } from '@element-plus/icons-vue'
import request from '../utils/request'

const bookings = ref([])
const memberOptions = ref([])
const courseOptions = ref([])
const keyword = ref('')
const statusFilter = ref('all')
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({ memberId: null, courseId: null, remark: '' })

const rules = {
  memberId: [{ required: true, message: '请选择会员', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }]
}

const pad = (n) => String(n).padStart(2, '0')
const formatDateTime = (val) => {
  if (!val) return ''
  const d = val instanceof Date ? val : new Date(val)
  if (Number.isNaN(d.getTime())) return ''
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

// 只显示上架且未结束的课程
const availableCourses = computed(() =>
  courseOptions.value.filter((c) => c.status === 1 && new Date(c.courseTime).getTime() > Date.now())
)

const selectedCourse = computed(() =>
  courseOptions.value.find((c) => c.courseId === form.courseId)
)

const onCourseChange = () => {}

const filteredBookings = computed(() => {
  let list = bookings.value
  if (statusFilter.value !== 'all') {
    list = list.filter((b) => b.status === statusFilter.value)
  }
  const kw = keyword.value.trim().toLowerCase()
  if (kw) {
    list = list.filter((b) =>
      (b.memberName || '').toLowerCase().includes(kw) ||
      (b.courseName || '').toLowerCase().includes(kw)
    )
  }
  return list
})

const statCards = computed(() => {
  const list = bookings.value
  const today = new Date()
  const todayCount = list.filter((b) => {
    if (!b.courseTime) return false
    const d = new Date(b.courseTime)
    return d.getFullYear() === today.getFullYear() &&
      d.getMonth() === today.getMonth() &&
      d.getDate() === today.getDate()
  }).length
  return [
    { label: '预约总数', value: list.length, hint: '全部记录', icon: Tickets, color: '#5b6cf6', bg: '#eef1ff' },
    { label: '有效预约', value: list.filter((b) => b.status === 1).length, hint: '当前生效', icon: CircleCheck, color: '#16a34a', bg: '#e9f9f0' },
    { label: '已取消', value: list.filter((b) => b.status === 0).length, hint: '历史记录', icon: CircleClose, color: '#94a3b8', bg: '#f1f5f9' },
    { label: '今日课程', value: todayCount, hint: '今天要上课', icon: Calendar, color: '#e08b0b', bg: '#fff4e0' }
  ]
})

const loadAll = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/booking/list')
    bookings.value = Array.isArray(res.data) ? res.data : []
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  try {
    const [mRes, cRes] = await Promise.all([
      request.get('/api/member/list'),
      request.get('/api/course/list')
    ])
    memberOptions.value = Array.isArray(mRes.data) ? mRes.data : []
    courseOptions.value = Array.isArray(cRes.data) ? cRes.data : []
  } catch (err) {
    console.error(err)
  }
}

const openAdd = () => {
  form.memberId = null
  form.courseId = null
  form.remark = ''
  loadOptions()
  dialogVisible.value = true
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await request.post('/api/booking/add', form)
    ElMessage.success('预约成功')
    dialogVisible.value = false
    loadAll()
  } catch (err) {
    console.error(err)
  } finally {
    submitting.value = false
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定取消「${row.memberName}」对「${row.courseName}」的预约吗？名额将被释放。`,
      '取消确认',
      { type: 'warning', confirmButtonText: '确认取消', cancelButtonText: '再想想' }
    )
    await request.put(`/api/booking/cancel/${row.bookingId}`)
    ElMessage.success('已取消预约')
    loadAll()
  } catch (err) {
    if (err !== 'cancel' && err?.message !== 'cancel') console.error(err)
  }
}

onMounted(loadAll)
</script>

<style scoped>
.booking-avatar { background: #eef1ff; color: #5b6cf6; font-size: 13px; }
.muted-text { color: #94a3b8; font-size: 13px; }
</style>

<template>
  <div class="view-page">
    <div class="page-heading">
      <div>
        <h1>课程管理</h1>
        <p>安排团课排期与授课教练，预约模块基于这里的课程数据工作</p>
      </div>
      <el-button type="primary" :icon="Refresh" plain @click="loadCourses">刷新数据</el-button>
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
          <el-input
            v-model="keyword"
            placeholder="搜索课程名或教练"
            clearable
            :prefix-icon="Search"
            class="search-input"
          />
        </div>
        <el-button type="primary" :icon="Plus" @click="openAdd">新增课程</el-button>
      </div>

      <el-table :data="filteredCourses" v-loading="loading" stripe style="width: 100%" empty-text="暂无课程，先新增一节吧">
        <el-table-column prop="courseName" label="课程名称" min-width="130">
          <template #default="{ row }">
            <div class="name-cell">
              <span class="name-avatar course-avatar"><el-icon><Reading /></el-icon></span>
              <span>{{ row.courseName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="授课教练" min-width="110" align="center">
          <template #default="{ row }">{{ row.coachName || '—' }}</template>
        </el-table-column>
        <el-table-column label="上课时间" min-width="160" align="center">
          <template #default="{ row }">
            <div class="time-cell">
              <span>{{ formatDateTime(row.courseTime) }}</span>
              <small v-if="isPast(row.courseTime)" class="past-tag">已结束</small>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="时长" width="90" align="center">
          <template #default="{ row }">{{ row.duration || 60 }} 分钟</template>
        </el-table-column>
        <el-table-column label="名额（已约/上限）" min-width="140" align="center">
          <template #default="{ row }">
            <el-progress
              :percentage="capacityPercent(row)"
              :stroke-width="10"
              :format="() => `${row.bookedCount}/${row.maxCapacity}`"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑课程' : '新增课程'"
      width="580px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="form.courseName" placeholder="例如：晨间瑜伽、动感单车" maxlength="100" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="授课教练" prop="coachId">
              <el-select v-model="form.coachId" style="width: 100%" placeholder="选择在职教练">
                <el-option
                  v-for="h in coachOptions"
                  :key="h.coachId"
                  :label="h.coachName"
                  :value="h.coachId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="时长(分钟)">
              <el-input-number v-model="form.duration" :min="15" :max="240" :step="15" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="上课时间" prop="courseTime">
          <el-date-picker
            v-model="form.courseTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            format="YYYY-MM-DD HH:mm"
            style="width: 100%"
            placeholder="选择上课日期和时间"
          />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="最大容量" prop="maxCapacity">
              <el-input-number v-model="form.maxCapacity" :min="1" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">上架</el-radio>
                <el-radio :label="0">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="选填" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Reading, Calendar, Tickets, Clock, CircleCheck } from '@element-plus/icons-vue'
import request from '../utils/request'

const courses = ref([])
const coachOptions = ref([])
const keyword = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  courseId: null,
  courseName: '',
  coachId: null,
  courseTime: '',
  duration: 60,
  maxCapacity: 20,
  status: 1,
  remark: ''
})

const rules = {
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  coachId: [{ required: true, message: '请选择授课教练', trigger: 'change' }],
  courseTime: [{ required: true, message: '请选择上课时间', trigger: 'change' }],
  maxCapacity: [{ required: true, message: '请设置最大容量', trigger: 'change' }]
}

const pad = (n) => String(n).padStart(2, '0')

const formatDateTime = (val) => {
  if (!val) return ''
  const d = val instanceof Date ? val : new Date(val)
  if (Number.isNaN(d.getTime())) return ''
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const isPast = (val) => val && new Date(val).getTime() < Date.now()

const capacityPercent = (row) => {
  if (!row.maxCapacity) return 0
  return Math.min(100, Math.round((row.bookedCount / row.maxCapacity) * 100))
}

const filteredCourses = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return courses.value
  return courses.value.filter((c) =>
    (c.courseName || '').toLowerCase().includes(kw) ||
    (c.coachName || '').toLowerCase().includes(kw)
  )
})

const statCards = computed(() => {
  const list = courses.value
  const now = Date.now()
  const weekLater = now + 7 * 24 * 3600 * 1000
  const weekCount = list.filter((c) => {
    const t = new Date(c.courseTime).getTime()
    return t >= now && t <= weekLater
  }).length
  const totalSeats = list.filter((c) => c.status === 1).reduce((sum, c) => sum + (c.maxCapacity || 0), 0)
  return [
    { label: '课程总数', value: list.length, hint: '全部排期', icon: Calendar, color: '#5b6cf6', bg: '#eef1ff' },
    { label: '上架中', value: list.filter((c) => c.status === 1).length, hint: '可被预约', icon: CircleCheck, color: '#16a34a', bg: '#e9f9f0' },
    { label: '近7天', value: weekCount, hint: '一周内课程', icon: Clock, color: '#e08b0b', bg: '#fff4e0' },
    { label: '可约名额', value: totalSeats, hint: '上架课总容量', icon: Tickets, color: '#d946a0', bg: '#fdeef7' }
  ]
})

const loadCourses = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/course/list')
    courses.value = Array.isArray(res.data) ? res.data : []
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const loadCoachOptions = async () => {
  try {
    const res = await request.get('/api/coach/onjob')
    coachOptions.value = Array.isArray(res.data) ? res.data : []
  } catch (err) {
    console.error(err)
  }
}

const resetForm = (row) => {
  form.courseId = row?.courseId || null
  form.courseName = row?.courseName || ''
  form.coachId = row?.coachId || null
  form.courseTime = row?.courseTime ? formatDateTime(row.courseTime) + ':00' : ''
  form.duration = row?.duration || 60
  form.maxCapacity = row?.maxCapacity || 20
  form.status = row?.status ?? 1
  form.remark = row?.remark || ''
}

const openAdd = () => {
  isEdit.value = false
  resetForm(null)
  loadCoachOptions()
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  resetForm(row)
  loadCoachOptions()
  dialogVisible.value = true
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put('/api/course/update', form)
      ElMessage.success('修改成功')
    } else {
      await request.post('/api/course/add', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadCourses()
  } catch (err) {
    console.error(err)
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除课程「${row.courseName}」吗？相关预约记录也会受影响。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
    await request.delete(`/api/course/delete/${row.courseId}`)
    ElMessage.success('删除成功')
    loadCourses()
  } catch (err) {
    if (err !== 'cancel' && err?.message !== 'cancel') console.error(err)
  }
}

onMounted(loadCourses)
</script>

<style scoped>
.course-avatar { background: #fdeef7; color: #d946a0; font-size: 14px; }
.time-cell { display: flex; flex-direction: column; align-items: center; gap: 3px; white-space: nowrap; }
.past-tag { color: #94a3b8; font-size: 11px; }
</style>

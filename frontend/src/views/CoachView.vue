<template>
  <div class="view-page">
    <div class="page-heading">
      <div>
        <h1>教练管理</h1>
        <p>管理教练信息与在职状态，课程排期时可选择在职教练</p>
      </div>
      <el-button type="primary" :icon="Refresh" plain @click="loadCoaches">刷新数据</el-button>
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
            placeholder="搜索姓名或专长"
            clearable
            :prefix-icon="Search"
            class="search-input"
          />
        </div>
        <el-button type="primary" :icon="Plus" @click="openAdd">新增教练</el-button>
      </div>

      <el-table :data="filteredCoaches" v-loading="loading" stripe style="width: 100%" empty-text="暂无教练数据">
        <el-table-column label="姓名" min-width="130">
          <template #default="{ row }">
            <div class="name-cell">
              <span class="name-avatar coach-avatar"><el-icon><UserFilled /></el-icon></span>
              <span>{{ row.coachName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="80" align="center" />
        <el-table-column prop="phone" label="联系电话" min-width="130" align="center" />
        <el-table-column prop="specialty" label="专长" min-width="130" show-overflow-tooltip />
        <el-table-column label="入职日期" width="120" align="center">
          <template #default="{ row }">{{ formatDate(row.hireDate) || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light" size="small">
              {{ row.status === 1 ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="130" show-overflow-tooltip />
        <el-table-column label="操作" width="170" fixed="right" align="center">
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
      :title="isEdit ? '编辑教练' : '新增教练'"
      width="540px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="姓名" prop="coachName">
          <el-input v-model="form.coachName" placeholder="请输入教练姓名" maxlength="50" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="form.gender" style="width: 100%" placeholder="请选择">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" maxlength="20" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="专长">
          <el-input v-model="form.specialty" maxlength="100" placeholder="例如：瑜伽、力量训练、有氧" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="入职日期">
              <el-date-picker v-model="form.hireDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" placeholder="选择日期" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="在职状态">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">在职</el-radio>
                <el-radio :label="0">离职</el-radio>
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
import { Plus, Search, Refresh, Edit, Delete, UserFilled, Avatar, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import request from '../utils/request'

const coaches = ref([])
const keyword = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  coachId: null,
  coachName: '',
  gender: '',
  phone: '',
  specialty: '',
  hireDate: '',
  status: 1,
  remark: ''
})

const rules = {
  coachName: [{ required: true, message: '请输入教练姓名', trigger: 'blur' }]
}

const filteredCoaches = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return coaches.value
  return coaches.value.filter((c) =>
    (c.coachName || '').toLowerCase().includes(kw) ||
    (c.specialty || '').toLowerCase().includes(kw)
  )
})

const statCards = computed(() => {
  const list = coaches.value
  return [
    { label: '教练总数', value: list.length, hint: '全部教练', icon: Avatar, color: '#5b6cf6', bg: '#eef1ff' },
    { label: '在职', value: list.filter((c) => c.status === 1).length, hint: '可安排课程', icon: CircleCheck, color: '#16a34a', bg: '#e9f9f0' },
    { label: '离职', value: list.filter((c) => c.status === 0).length, hint: '历史人员', icon: CircleClose, color: '#94a3b8', bg: '#f1f5f9' }
  ]
})

const formatDate = (val) => {
  if (!val) return ''
  const d = val instanceof Date ? val : new Date(val)
  if (Number.isNaN(d.getTime())) return ''
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

const loadCoaches = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/coach/list')
    coaches.value = Array.isArray(res.data) ? res.data : []
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const resetForm = (row) => {
  form.coachId = row?.coachId || null
  form.coachName = row?.coachName || ''
  form.gender = row?.gender || ''
  form.phone = row?.phone || ''
  form.specialty = row?.specialty || ''
  form.hireDate = row?.hireDate ? formatDate(row.hireDate) : ''
  form.status = row?.status ?? 1
  form.remark = row?.remark || ''
}

const openAdd = () => {
  isEdit.value = false
  resetForm(null)
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  resetForm(row)
  dialogVisible.value = true
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put('/api/coach/update', form)
      ElMessage.success('修改成功')
    } else {
      await request.post('/api/coach/add', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadCoaches()
  } catch (err) {
    console.error(err)
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除教练「${row.coachName}」吗？删除后不可恢复。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
    await request.delete(`/api/coach/delete/${row.coachId}`)
    ElMessage.success('删除成功')
    loadCoaches()
  } catch (err) {
    if (err !== 'cancel' && err?.message !== 'cancel') console.error(err)
  }
}

onMounted(loadCoaches)
</script>

<style scoped>
.coach-avatar { background: #eef1ff; color: #5b6cf6; font-size: 14px; }
</style>

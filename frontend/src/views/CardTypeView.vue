<template>
  <div class="view-page">
    <div class="page-heading">
      <div>
        <h1>卡种套餐</h1>
        <p>管理健身房卡种与套餐，续费时可选择已上架卡种</p>
      </div>
      <el-button type="primary" :icon="Refresh" plain @click="loadCards">刷新数据</el-button>
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
          <el-input
            v-model="keyword"
            placeholder="搜索卡种名称"
            clearable
            :prefix-icon="Search"
            class="search-input"
          />
        </div>
        <el-button type="primary" :icon="Plus" @click="openAdd">新增卡种</el-button>
      </div>

      <el-table
        :data="filteredCards"
        v-loading="loading"
        stripe
        style="width: 100%"
        empty-text="暂无卡种数据"
      >
        <el-table-column label="卡种名称" min-width="140">
          <template #default="{ row }">
            <div class="name-cell">
              <span class="name-avatar card-avatar"><el-icon><Ticket /></el-icon></span>
              <span>{{ row.cardName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.cardType === '次卡' ? 'warning' : 'primary'" effect="plain" size="small">
              {{ row.cardType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="110" align="center">
          <template #default="{ row }"><strong class="price">¥{{ row.price }}</strong></template>
        </el-table-column>
        <el-table-column label="有效期" min-width="130" align="center">
          <template #default="{ row }">
            {{ row.cardType === '次卡' ? `${row.totalTimes} 次` : `${row.validDays} 天` }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="创建时间" width="120">
          <template #default="{ row }">{{ formatDate(row.createTime) || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              link
              @click="toggleStatus(row)"
            >{{ row.status === 1 ? '下架' : '上架' }}</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footnote">
        已被会员续费使用过的卡种无法删除，如需停用请改为「下架」。
      </div>
    </section>

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑卡种' : '新增卡种'"
      width="540px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="卡种名称" prop="cardName">
          <el-input v-model="form.cardName" placeholder="例如：年卡、月卡、20次卡" maxlength="50" />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="卡种类型" prop="cardType">
              <el-select v-model="form.cardType" style="width: 100%" @change="handleTypeChange">
                <el-option label="时长卡" value="时长卡" />
                <el-option label="次卡" value="次卡" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item v-if="form.cardType === '时长卡'" label="有效天数" prop="validDays">
          <el-input-number v-model="form.validDays" :min="1" :max="3650" step="1" step-strictly style="width: 100%" />
        </el-form-item>
        <el-form-item v-else label="总次数" prop="totalTimes">
          <el-input-number v-model="form.totalTimes" :min="1" :max="1000" step="1" step-strictly style="width: 100%" />
        </el-form-item>

        <el-form-item label="上架状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>

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
import { Plus, Search, Refresh, Edit, Delete, Ticket, CircleCheck, CircleClose, Coin } from '@element-plus/icons-vue'
import request from '../utils/request'

const cards = ref([])
const keyword = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  cardId: null,
  cardName: '',
  cardType: '时长卡',
  price: 0,
  validDays: 30,
  totalTimes: null,
  status: 1,
  remark: ''
})

const rules = {
  cardName: [{ required: true, message: '请输入卡种名称', trigger: 'blur' }],
  cardType: [{ required: true, message: '请选择卡种类型', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const filteredCards = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return cards.value
  return cards.value.filter((c) => (c.cardName || '').toLowerCase().includes(kw))
})

const statCards = computed(() => {
  const list = cards.value
  return [
    { label: '卡种总数', value: list.length, hint: '全部卡种', icon: Ticket, color: '#5b6cf6', bg: '#eef1ff' },
    { label: '已上架', value: list.filter((c) => c.status === 1).length, hint: '续费时可选', icon: CircleCheck, color: '#16a34a', bg: '#e9f9f0' },
    { label: '已下架', value: list.filter((c) => c.status === 0).length, hint: '暂不可选', icon: CircleClose, color: '#94a3b8', bg: '#f1f5f9' },
    { label: '次卡', value: list.filter((c) => c.cardType === '次卡').length, hint: '按次数计', icon: Coin, color: '#f59e0b', bg: '#fff6e6' }
  ]
})

const formatDate = (val) => {
  if (!val) return ''
  const d = val instanceof Date ? val : new Date(val)
  if (Number.isNaN(d.getTime())) return ''
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

const handleTypeChange = () => {
  if (form.cardType === '时长卡' && !form.validDays) form.validDays = 30
  if (form.cardType === '次卡' && !form.totalTimes) form.totalTimes = 20
}

const loadCards = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/card/list')
    cards.value = Array.isArray(res.data) ? res.data : []
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const resetForm = (row) => {
  form.cardId = row?.cardId || null
  form.cardName = row?.cardName || ''
  form.cardType = row?.cardType || '时长卡'
  form.price = row?.price ?? 0
  form.validDays = row?.validDays ?? 30
  form.totalTimes = row?.totalTimes ?? null
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
  if (form.cardType === '时长卡' && (!form.validDays || form.validDays <= 0)) {
    ElMessage.warning('有效天数必须大于 0')
    return
  }
  if (form.cardType === '次卡' && (!form.totalTimes || form.totalTimes <= 0)) {
    ElMessage.warning('总次数必须大于 0')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put('/api/card/update', form)
      ElMessage.success('修改成功')
    } else {
      await request.post('/api/card/add', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadCards()
  } catch (err) {
    console.error(err)
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (row) => {
  const target = row.status === 1 ? 0 : 1
  const action = target === 1 ? '上架' : '下架'
  try {
    await request.put('/api/card/update', { ...row, status: target })
    ElMessage.success(`${action}成功`)
    loadCards()
  } catch (err) {
    console.error(err)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除卡种「${row.cardName}」吗？删除后不可恢复。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
    await request.delete(`/api/card/delete/${row.cardId}`)
    ElMessage.success('删除成功')
    loadCards()
  } catch (err) {
    // 后端拦截（已被使用）时，request 拦截器已弹出错误提示，这里不再重复
    if (err !== 'cancel' && err?.message !== 'cancel') console.error(err)
  }
}

onMounted(loadCards)
</script>

<style scoped>
.card-avatar { background: #fff6e6; color: #f59e0b; font-size: 14px; }
.price { color: #16213b; }
.table-footnote { margin-top: 14px; font-size: 12px; color: #94a3b8; }
</style>

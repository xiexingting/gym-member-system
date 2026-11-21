<template>
  <el-dialog
    v-model="visible"
    title="会员续费"
    width="600px"
    :close-on-click-modal="false"
    destroy-on-close
    v-loading="loading"
  >
    <!-- 会员信息摘要 -->
    <div class="member-summary" v-if="member">
      <div class="summary-avatar">{{ (member.memberName || '?').slice(0, 1) }}</div>
      <div class="summary-info">
        <strong>{{ member.memberName }}</strong>
        <span>{{ member.phone }}</span>
      </div>
      <div class="summary-expire">
        <span>当前到期时间</span>
        <strong :class="{ expired: isExpired }">{{ formatDate(member.expireTime) || '未设置' }}</strong>
      </div>
    </div>

    <el-divider content-position="left">续费信息</el-divider>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="选择卡种">
        <el-select
          v-model="form.cardId"
          placeholder="选填，可关联已上架卡种"
          clearable
          style="width: 100%"
          @change="handleCardChange"
        >
          <el-option
            v-for="c in cardOptions"
            :key="c.cardId"
            :label="`${c.cardName}（${c.cardType}）· ¥${c.price}`"
            :value="c.cardId"
          />
        </el-select>
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="续费月数" prop="addMonth">
            <el-input-number
              v-model="form.addMonth"
              :min="1"
              :max="120"
              step="1"
              step-strictly
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="续费金额">
            <el-input-number
              v-model="form.rechargeMoney"
              :min="0"
              :precision="2"
              :step="100"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="操作人">
        <el-input v-model="form.operator" placeholder="选填，默认记为管理员" />
      </el-form-item>

      <el-form-item label="会员备注">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="2"
          maxlength="200"
          show-word-limit
          placeholder="选填：续费时可顺手更新备注，不修改请保持原样"
        />
      </el-form-item>

      <!-- 续费后到期时间预览，按后端规则算：未到期从到期日顺延，已过期从今天算 -->
      <div class="preview-box" v-if="member">
        <span>续费后到期时间：</span>
        <strong>{{ previewExpireText }}</strong>
      </div>
    </el-form>

    <el-divider content-position="left">历史续费记录</el-divider>
    <el-table :data="records" size="small" empty-text="暂无续费记录" class="record-table" v-loading="recordsLoading">
      <el-table-column label="续费时间" width="165">
        <template #default="{ row }">{{ formatDateTime(row.rechargeTime) }}</template>
      </el-table-column>
      <el-table-column label="增加月数" width="90" align="center">
        <template #default="{ row }">{{ row.addMonth }} 个月</template>
      </el-table-column>
      <el-table-column label="金额" width="100" align="center">
        <template #default="{ row }">¥{{ row.rechargeMoney }}</template>
      </el-table-column>
      <el-table-column prop="operator" label="操作人" min-width="100" />
    </el-table>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">确认续费</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const props = defineProps({
  visible: { type: Boolean, default: false },
  memberId: { type: Number, default: null },
  member: { type: Object, default: null }
})
const emit = defineEmits(['update:visible', 'renewed'])

const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const formRef = ref(null)
const loading = ref(false)
const recordsLoading = ref(false)
const submitting = ref(false)
const records = ref([])

const form = reactive({
  memberId: null,
  addMonth: 12,
  rechargeMoney: 1000,
  operator: '管理员',
  remark: '',
  cardId: null
})

const cardOptions = ref([])

/** 加载上架卡种，供续费下拉选择 */
const loadCardOptions = async () => {
  try {
    const res = await request.get('/api/card/online')
    cardOptions.value = Array.isArray(res.data) ? res.data : []
  } catch (err) {
    console.error(err)
    cardOptions.value = []
  }
}

/** 选了时长卡：按有效天数自动换算月数并带出价格；次卡只带出价格 */
const handleCardChange = (cardId) => {
  const card = cardOptions.value.find((c) => c.cardId === cardId)
  if (!card) return
  form.rechargeMoney = Number(card.price) || 0
  if (card.cardType === '时长卡' && card.validDays) {
    form.addMonth = Math.max(1, Math.round(card.validDays / 30))
  }
}

const rules = {
  addMonth: [{ required: true, message: '请输入续费月数', trigger: 'blur' }]
}

const toDate = (val) => {
  if (!val) return null
  const d = val instanceof Date ? val : new Date(val)
  return Number.isNaN(d.getTime()) ? null : d
}

const formatDate = (val) => {
  const d = toDate(val)
  if (!d) return ''
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

const formatDateTime = (val) => {
  const d = toDate(val)
  if (!d) return ''
  const p = (n) => String(n).padStart(2, '0')
  return `${formatDate(d)} ${p(d.getHours())}:${p(d.getMinutes())}`
}

const isExpired = computed(() => {
  const d = toDate(props.member?.expireTime)
  if (!d) return false
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return d < today
})

/**
 * 与后端 RechargeServiceImpl 保持一致：
 * 到期日在未来就从到期日顺延，已过期则从今天开始算。
 * 用 setDate(1) 避开月末溢出（如 1月31日 + 1月 变成 3月3日）。
 */
const previewExpireText = computed(() => {
  const months = Number(form.addMonth) || 0
  if (months <= 0) return '—'
  const now = new Date()
  const expire = toDate(props.member?.expireTime)
  let base = expire && expire > now ? new Date(expire) : now
  const day = base.getDate()
  base = new Date(base)
  base.setDate(1)
  base.setMonth(base.getMonth() + months)
  const lastDay = new Date(base.getFullYear(), base.getMonth() + 1, 0).getDate()
  base.setDate(Math.min(day, lastDay))
  return formatDate(base)
})

const loadRecords = async (id) => {
  recordsLoading.value = true
  try {
    const res = await request.get(`/api/member/recharge/list/${id}`)
    records.value = Array.isArray(res.data) ? res.data : []
  } catch (err) {
    console.error(err)
    records.value = []
  } finally {
    recordsLoading.value = false
  }
}

watch(
  () => [props.visible, props.memberId],
  ([isVisible, id]) => {
    if (!isVisible || !id) return
    form.memberId = id
    form.addMonth = 12
    form.rechargeMoney = 1000
    form.operator = '管理员'
    // 预填当前备注：不改就原样提交，改了就随续费一起更新，清空则清空
    form.remark = props.member?.remark || ''
    form.cardId = null
    loadCardOptions()
    formRef.value?.clearValidate()
    loadRecords(id)
  },
  { immediate: true }
)

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await request.post('/api/member/renew', form)
    ElMessage.success('续费成功')
    visible.value = false
    emit('renewed')
  } catch (err) {
    console.error(err)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.member-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: #f8f9fd;
  border-radius: 10px;
}
.summary-avatar {
  width: 40px; height: 40px; border-radius: 10px; flex-shrink: 0;
  background: #eef1ff; color: #5b6cf6;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; font-weight: 700;
}
.summary-info { display: flex; flex-direction: column; line-height: 1.5; }
.summary-info strong { color: #16213b; font-size: 14px; }
.summary-info span { color: #94a3b8; font-size: 12px; }
.summary-expire {
  margin-left: auto;
  display: flex; flex-direction: column; align-items: flex-end; line-height: 1.5;
}
.summary-expire span { color: #94a3b8; font-size: 12px; }
.summary-expire strong { color: #16213b; font-size: 14px; }
.summary-expire strong.expired { color: #ef4444; }

.preview-box {
  background: #eef1ff;
  border: 1px dashed #aab4f8;
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 13px;
  color: #475569;
  width: 100%;
}
.preview-box strong { color: #4f5bd5; font-size: 14px; margin-left: 6px; }

.record-table { border-radius: 8px; overflow: hidden; max-height: 180px; }
</style>

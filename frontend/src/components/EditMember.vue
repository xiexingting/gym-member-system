<template>
  <el-dialog
    v-model="visible"
    title="编辑会员"
    width="560px"
    :close-on-click-modal="false"
    destroy-on-close
    v-loading="loading"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="姓名" prop="memberName">
            <el-input v-model="form.memberName" placeholder="请输入会员姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="身份证" prop="idCard">
        <el-input v-model="form.idCard" placeholder="选填" maxlength="18" />
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="性别" prop="gender">
            <el-select v-model="form.gender" placeholder="请选择" clearable style="width: 100%">
              <el-option label="男" value="男" />
              <el-option label="女" value="女" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="到期时间" prop="expireTime">
            <el-date-picker
              v-model="form.expireTime"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择日期"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="选填" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="save">保存修改</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const props = defineProps({
  visible: { type: Boolean, default: false },
  memberId: { type: Number, default: null }
})
const emit = defineEmits(['update:visible', 'saved'])

const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)

const form = reactive({
  memberId: null,
  memberName: '',
  phone: '',
  idCard: '',
  gender: '',
  expireTime: '',
  remark: ''
})

const rules = {
  memberName: [{ required: true, message: '请输入会员姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

// 后端 Date 可能序列化成时间戳数字或 ISO 字符串，统一转成 input 需要的 YYYY-MM-DD
const toInputDate = (val) => {
  if (!val) return ''
  const d = val instanceof Date ? val : new Date(val)
  if (Number.isNaN(d.getTime())) return ''
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

const loadMember = async (id) => {
  loading.value = true
  try {
    const res = await request.get(`/api/member/get/${id}`)
    const data = res.data || {}
    Object.assign(form, {
      memberId: data.memberId,
      memberName: data.memberName || '',
      phone: data.phone || '',
      idCard: data.idCard || '',
      gender: data.gender || '',
      expireTime: toInputDate(data.expireTime),
      remark: data.remark || ''
    })
    formRef.value?.clearValidate()
  } catch (err) {
    console.error(err)
    visible.value = false
  } finally {
    loading.value = false
  }
}

// 弹窗打开、或切换编辑对象时，重新拉取数据
watch(
  () => [props.visible, props.memberId],
  ([isVisible, id]) => {
    if (isVisible && id) loadMember(id)
  },
  { immediate: true }
)

const save = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await request.put('/api/member/update', form)
    ElMessage.success('修改成功')
    visible.value = false
    emit('saved')
  } catch (err) {
    console.error(err)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <el-dialog
    v-model="visible"
    title="新增会员"
    width="560px"
    :close-on-click-modal="false"
    destroy-on-close
    @closed="resetForm"
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
            <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
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
        <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="选填，例如联系方式、健身目标等" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">确定新增</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const props = defineProps({
  visible: { type: Boolean, default: false }
})
const emit = defineEmits(['update:visible', 'added'])

// 用计算属性实现 v-model:visible 双向绑定
const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  memberName: '',
  phone: '',
  idCard: '',
  gender: '',
  expireTime: '',
  remark: ''
})

const rules = {
  memberName: [{ required: true, message: '请输入会员姓名', trigger: 'blur' }],
  // 与原逻辑保持一致：只校验必填，不限制号码格式，方便用测试数据录入
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

const resetForm = () => {
  Object.assign(form, {
    memberName: '', phone: '', idCard: '', gender: '', expireTime: '', remark: ''
  })
  formRef.value?.clearValidate()
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    // 注册时间、会员状态由后端自动填充，前端不需传
    await request.post('/api/member/add', form)
    ElMessage.success('新增成功')
    visible.value = false
    emit('added')
  } catch (err) {
    console.error(err)
  } finally {
    submitting.value = false
  }
}
</script>

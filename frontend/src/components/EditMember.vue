<template>
  <div class="mask" v-if="visible">
    <div class="dialog">
      <h3>编辑会员</h3>
      <div class="form-item">
        <label>姓名：</label>
        <input v-model="form.memberName"/>
      </div>
      <div class="form-item">
        <label>手机号：</label>
        <input v-model="form.phone"/>
      </div>
      <div class="form-item">
        <label>身份证：</label>
        <input v-model="form.idCard"/>
      </div>
      <div class="form-item">
        <label>性别：</label>
        <input v-model="form.gender"/>
      </div>
      <div class="form-item">
        <label>到期时间：</label>
        <input type="date" v-model="form.expireTime"/>
      </div>
      <div class="form-item">
        <label>备注：</label>
        <input v-model="form.remark"/>
      </div>
      <div class="btns">
        <button class="save-btn" @click="save">保存修改</button>
        <button class="cancel-btn" @click="close">取消</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import axios from 'axios'

const props = defineProps({
  memberId: { type: Number, default: null }
})
const emit = defineEmits(['saved', 'close'])

const visible = ref(false)
const form = reactive({
  memberId: null,
  memberName: '',
  phone: '',
  idCard: '',
  gender: '',
  expireTime: '',
  remark: ''
})

// 监听传入的 ID：一旦有值就请求该会员数据并弹窗
watch(() => props.memberId, async (id) => {
  if(!id){
    visible.value = false
    return
  }
  const res = await axios.get(`http://localhost:8080/api/member/get/${id}`)
  Object.assign(form, res.data)
  // 日期转成 input 需要的 年-月-日 格式
  const d = new Date(res.data.expireTime)
  const p = (n) => String(n).padStart(2,'0')
  form.expireTime = `${d.getFullYear()}-${p(d.getMonth()+1)}-${p(d.getDate())}`
  visible.value = true
})

const save = async () => {
  const res = await axios.put('http://localhost:8080/api/member/update', form)
  if(res.data === 'success'){
    alert('修改成功')
    close()
    emit('saved')
  } else {
    alert('修改失败')
  }
}

const close = () => {
  visible.value = false
  emit('close')
}
</script>

<style scoped>
.mask{
  position: fixed;
  top:0; left:0; right:0; bottom:0;
  background: rgba(0,0,0,0.4);
  display:flex;
  justify-content:center;
  align-items:center;
  z-index:999;
}
.dialog{
  background:#fff;
  padding:25px 35px;
  border-radius:8px;
  width:380px;
}
.form-item{
  margin:12px 0;
}
label{
  display:inline-block;
  width:90px;
}
input{
  padding:6px;
  width:220px;
}
.btns{
  text-align:center;
  margin-top:15px;
}
.btns button{
  padding:8px 20px;
  margin:0 8px;
  border:none;
  border-radius:4px;
  cursor:pointer;
  color:white;
}
.save-btn{
  background:#4472C4;
}
.cancel-btn{
  background:#999;
}
</style>

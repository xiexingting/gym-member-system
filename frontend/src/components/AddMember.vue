<template>
  <div class="form-box">
    <h3>新增会员</h3>
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
    <div class="form-item">
      <button @click="submit">提交新增</button>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import axios from 'axios'

const emit = defineEmits(['added'])

const form = reactive({
  memberName: '',
  phone: '',
  idCard: '',
  gender: '',
  expireTime: '',
  remark: ''
})

const submit = async () => {
  if(!form.memberName || !form.phone){
    alert("姓名和手机号不能为空！")
    return
  }
  try {
    await axios.post('http://localhost:8080/api/member/add', form)
    alert("新增成功！")
    emit('added')
    form.memberName = ''
    form.phone = ''
    form.idCard = ''
    form.gender = ''
    form.expireTime = ''
    form.remark = ''
  }catch(err){
    alert("新增失败！检查后端add接口")
    console.error(err)
  }
}
</script>

<style scoped>
.form-box{
  border:1px solid #ccc;
  padding:20px;
  border-radius:8px;
  margin-bottom:30px;
}
.form-item{
  margin:12px 0;
}
label{
  display: inline-block;
  width:90px;
}
input{
  padding:6px;
  width:220px;
}
button{
  padding:8px 20px;
  background:#4472C4;
  color:white;
  border:none;
  border-radius:4px;
  cursor:pointer;
}
button:hover{
  background:#365b9c;
}
h3{
  margin-top:0;
}
</style>

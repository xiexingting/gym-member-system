<template>
  <div class="container">
    <h2>健身房会员管理系统</h2>

    <AddMember @added="getMemberList" />

    <h3>会员列表 <span style="color:red">（红色行代表会员已到期）</span></h3>
    <table>
      <thead>
        <tr>
          <th>姓名</th>
          <th>手机号</th>
          <th>身份证</th>
          <th>性别</th>
          <th>到期时间</th>
          <th>备注</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in memberList" :key="item.memberId" :class="isExpire(item.expireTime) ? 'expire-row' : ''">
          <td>{{ item.memberName }}</td>
          <td>{{ item.phone }}</td>
          <td>{{ item.idCard }}</td>
          <td>{{ item.gender }}</td>
          <td>{{ formatDate(item.expireTime) }}</td>
          <td>{{ item.remark }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import AddMember from './components/AddMember.vue'

const memberList = ref([])

// 判断会员是否到期
const isExpire = (expireTime) => {
  if(!expireTime) return false
  return new Date(expireTime) < new Date()
}

// 时间格式化：只返回 年-月-日
const formatDate = (dateStr) => {
  if(!dateStr) return ''
  const date = new Date(dateStr)
  const p = (n) => String(n).padStart(2,'0')
  return `${date.getFullYear()}-${p(date.getMonth()+1)}-${p(date.getDate())}`
}

// 获取会员列表
const getMemberList = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/member/list')
    memberList.value = res.data
    console.log('会员数据：', res.data)
  } catch(err) {
    alert('接口请求失败！请确认SpringBoot后端已经启动')
    console.error(err)
  }
}

onMounted(()=>{
  getMemberList()
})
</script>

<style scoped>
.container {
  width: 90%;
  margin: 0 auto;
  padding:30px;
}
table {
  width:100%;
  border-collapse: collapse;
  margin-top:20px;
  table-layout: fixed;
}
th, td {
  border:1px solid #ccc;
  padding:10px;
  text-align:center;
  overflow:hidden;
  text-overflow:ellipsis;
}
th {
  background:#4472C4;
  color:#fff;
}
h2 {
  text-align:center;
}
h3{
  margin-top:0;
}
.expire-row{
  background-color: #ffdddd;
}
</style>

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
		  <th>操作</th>

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
		  <td>
		    <button class="del-btn" @click="handleDelete(item.memberId)">删除</button>
		  </td>

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
// 删除会员：先弹确认框，确认后调删除接口，再刷新列表
const handleDelete = async (id) => {
  if(!confirm('确定要删除这个会员吗？删除后不可恢复')) return
  try {
    const res = await axios.delete(`http://localhost:8080/api/member/delete/${id}`)
    if(res.data === 'success'){
      alert('删除成功')
      getMemberList()
    } else {
      alert('删除失败，未找到该会员')
    }
  } catch(err) {
    alert('删除请求失败，检查后端是否启动')
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

.del-btn{
  padding:5px 12px;
  background:#d9534f;
  color:white;
  border:none;
  border-radius:4px;
  cursor:pointer;
}
.del-btn:hover{
  background:#c9302c;
}

</style>

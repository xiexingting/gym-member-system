<template>
  <div class="login-page">
    <div class="login-card">
      <!-- 左侧品牌区 -->
      <div class="login-brand">
        <div class="brand-mark">G</div>
        <h2>健身房会员管理系统</h2>
        <p>GYM MEMBER SYSTEM</p>
        <ul class="brand-points">
          <li>会员与卡种一站式管理</li>
          <li>课程排期与在线预约</li>
          <li>到期自动提醒</li>
        </ul>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-form-wrap">
        <h3>欢迎登录</h3>
        <p class="form-sub">请使用管理员账号登录系统</p>

        <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入账号" :prefix-icon="User" clearable />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">登 录</el-button>
          </el-form-item>
        </el-form>

        <p class="login-tip">默认账号：admin　默认密码：123456</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import request from '../utils/request'

const emit = defineEmits(['login-success'])

const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await request.post('/api/auth/login', {
      username: form.username,
      password: form.password
    })
    localStorage.setItem('gym_token', res.data.token)
    emit('login-success')
  } catch (err) {
    // 错误提示已由 request 拦截器统一弹出
    console.error(err)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e2a44 0%, #2d3d63 100%);
  padding: 20px;
}

.login-card {
  width: 820px;
  max-width: 100%;
  background: #fff;
  border-radius: 18px;
  overflow: hidden;
  display: flex;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.3);
}

.login-brand {
  width: 46%;
  background: linear-gradient(160deg, #4658e8 0%, #3342a8 100%);
  color: #fff;
  padding: 48px 36px;
  display: flex;
  flex-direction: column;
}
.brand-mark {
  width: 46px; height: 46px; border-radius: 12px;
  background: rgba(255, 255, 255, 0.18);
  display: flex; align-items: center; justify-content: center;
  font-size: 24px; font-weight: 800;
}
.login-brand h2 { margin: 28px 0 6px; font-size: 22px; }
.login-brand > p { margin: 0; font-size: 11px; letter-spacing: 2px; color: #c6cdff; }
.brand-points { margin: 36px 0 0; padding: 0; list-style: none; }
.brand-points li {
  position: relative; padding-left: 18px; margin-bottom: 14px;
  font-size: 13px; color: #e6e9ff;
}
.brand-points li::before {
  content: ''; position: absolute; left: 0; top: 7px;
  width: 7px; height: 7px; border-radius: 50%; background: #9fb0ff;
}

.login-form-wrap { flex: 1; padding: 56px 48px; }
.login-form-wrap h3 { margin: 0; font-size: 21px; color: #16213b; }
.form-sub { margin: 8px 0 30px; font-size: 13px; color: #94a3b8; }
.login-btn { width: 100%; }
.login-tip { margin: 14px 0 0; text-align: center; font-size: 12px; color: #a8b3c5; }

@media (max-width: 720px) {
  .login-card { flex-direction: column; width: 420px; }
  .login-brand { width: 100%; padding: 32px; }
  .brand-points { display: none; }
  .login-form-wrap { padding: 36px 28px; }
}
</style>

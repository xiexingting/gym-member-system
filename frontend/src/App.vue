<template>
  <!-- 未登录：只显示登录页 -->
  <LoginView v-if="!isLoggedIn" @login-success="onLoginSuccess" />

  <!-- 已登录：主界面 -->
  <div class="layout" v-else>
    <!-- ========== 左侧菜单 ========== -->
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">G</div>
        <div class="brand-text">
          <strong>健身会员管理</strong>
          <span>GYM MEMBER SYSTEM</span>
        </div>
      </div>

      <div class="menu-label">工作台</div>
      <nav class="menu">
        <div
          v-for="m in menus"
          :key="m.key"
          class="menu-item"
          :class="{ active: currentMenu === m.key }"
          @click="currentMenu = m.key"
        >
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.name }}</span>
        </div>
        <div class="menu-item disabled" v-for="m in comingMenus" :key="m.name">
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.name }}</span>
          <em>待开发</em>
        </div>
      </nav>

      <div class="sidebar-footer">
        <div class="version-dot"></div>
        <span>系统运行中 · v0.2</span>
      </div>
    </aside>

    <!-- ========== 右侧主体 ========== -->
    <div class="main-wrap">
      <header class="topbar">
        <div class="breadcrumb">
          <el-icon><House /></el-icon>
          <span>工作台</span>
          <span class="sep">/</span>
          <strong>{{ currentMenuName }}</strong>
        </div>
        <div class="topbar-right">
          <span class="today">{{ todayText }}</span>
          <div class="avatar">{{ loginAdmin.realName?.charAt(0) || '管' }}</div>
          <span class="admin-name">{{ loginAdmin.realName || loginAdmin.username }}</span>
          <el-button type="danger" plain :icon="SwitchButton" size="small" @click="handleLogout">退出</el-button>
        </div>
      </header>

      <main class="content">
        <component :is="currentComponent" />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Ticket, Bell, House, UserFilled, Calendar, Clock, SwitchButton } from '@element-plus/icons-vue'
import MemberView from './views/MemberView.vue'
import CardTypeView from './views/CardTypeView.vue'
import CoachView from './views/CoachView.vue'
import CourseView from './views/CourseView.vue'
import BookingView from './views/BookingView.vue'
import ReminderView from './views/ReminderView.vue'
import LoginView from './views/LoginView.vue'
import request from './utils/request'

// 登录态：本地有 token 就认为已登录，具体有效性由后端拦截器把关
const isLoggedIn = ref(!!localStorage.getItem('gym_token'))
const loginAdmin = reactive({ username: '', realName: '' })

const onLoginSuccess = async () => {
  isLoggedIn.value = true
  await loadAdminInfo()
}

const loadAdminInfo = async () => {
  try {
    const res = await request.get('/api/auth/info')
    loginAdmin.username = res.data.username
    loginAdmin.realName = res.data.realName
  } catch (err) {
    console.error(err)
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
      type: 'warning', confirmButtonText: '退出', cancelButtonText: '取消'
    })
    await request.post('/api/auth/logout')
  } catch (err) {
    // 用户点取消则中止退出
    if (err === 'cancel' || err?.message === 'cancel') return
    console.error(err)
    return
  }
  localStorage.removeItem('gym_token')
  isLoggedIn.value = false
  ElMessage.success('已退出登录')
}

onMounted(() => {
  if (isLoggedIn.value) loadAdminInfo()
})

const menus = [
  { key: 'member', name: '会员管理', icon: User, component: MemberView },
  { key: 'card', name: '卡种套餐', icon: Ticket, component: CardTypeView },
  { key: 'coach', name: '教练管理', icon: UserFilled, component: CoachView },
  { key: 'course', name: '课程管理', icon: Calendar, component: CourseView },
  { key: 'booking', name: '课程预约', icon: Clock, component: BookingView },
  { key: 'reminder', name: '到期提醒', icon: Bell, component: ReminderView }
]

// 全部模块已开发完成，待开发菜单留空
const comingMenus = []

const currentMenu = ref('member')
const currentMenuName = computed(() =>
  menus.find((m) => m.key === currentMenu.value)?.name || '会员管理'
)
const currentComponent = computed(() =>
  menus.find((m) => m.key === currentMenu.value)?.component || MemberView
)

const todayText = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric', month: 'long', day: 'numeric', weekday: 'long'
}).format(new Date())
</script>

<style>
/* 全局主题变量：覆盖 Element Plus 默认色，与后台风格统一 */
:root {
  --el-color-primary: #5b6cf6;
  --el-border-radius-base: 8px;
  --el-font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
}

/* 以下为各视图共用的后台样式，放在全局供子视图直接复用 */
.layout { display: flex; min-height: 100vh; background: #f4f6fb; }

.sidebar {
  width: 210px; flex-shrink: 0;
  background: linear-gradient(180deg, #1e2a44 0%, #141d33 100%);
  color: #cbd5e1; display: flex; flex-direction: column;
  position: sticky; top: 0; height: 100vh;
}
.brand {
  height: 76px; display: flex; align-items: center; gap: 12px; padding: 0 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.07);
}
.brand-mark {
  width: 38px; height: 38px; border-radius: 10px;
  background: linear-gradient(135deg, #6d7dff, #4658e8);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 20px; font-weight: 800;
  box-shadow: 0 6px 16px rgba(91, 108, 246, 0.4);
}
.brand-text { display: flex; flex-direction: column; line-height: 1.4; }
.brand-text strong { color: #fff; font-size: 15px; letter-spacing: 0.5px; }
.brand-text span { font-size: 9px; color: #7f8ca8; letter-spacing: 1.2px; }

.menu-label { padding: 22px 22px 8px; font-size: 11px; color: #6c7b98; letter-spacing: 1px; }
.menu { padding: 0 12px; flex: 1; }
.menu-item {
  height: 42px; padding: 0 12px; margin-bottom: 4px; border-radius: 8px;
  display: flex; align-items: center; gap: 11px; font-size: 14px;
  transition: all 0.18s; cursor: pointer;
}
.menu-item .el-icon { font-size: 17px; }
.menu-item:hover:not(.disabled) { background: rgba(255, 255, 255, 0.06); color: #fff; }
.menu-item.active {
  background: linear-gradient(90deg, rgba(91, 108, 246, 0.32), rgba(91, 108, 246, 0.12));
  color: #fff; font-weight: 600; box-shadow: inset 3px 0 0 #6d7dff;
}
.menu-item.disabled { color: #7b88a3; cursor: default; }
.menu-item.disabled em {
  margin-left: auto; font-size: 10px; font-style: normal; color: #64748b;
  border: 1px solid #3b4763; border-radius: 4px; padding: 1px 5px;
}
.sidebar-footer {
  height: 56px; padding: 0 22px; border-top: 1px solid rgba(255, 255, 255, 0.07);
  display: flex; align-items: center; gap: 8px; font-size: 12px; color: #7f8ca8;
}
.version-dot {
  width: 7px; height: 7px; border-radius: 50%;
  background: #34d399; box-shadow: 0 0 0 3px rgba(52, 211, 153, 0.15);
}

.main-wrap { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.topbar {
  height: 76px; flex-shrink: 0; background: #fff; border-bottom: 1px solid #eef0f5;
  display: flex; align-items: center; justify-content: space-between; padding: 0 28px;
}
.breadcrumb { display: flex; align-items: center; gap: 8px; font-size: 14px; color: #94a3b8; }
.breadcrumb strong { color: #1e293b; font-weight: 600; }
.breadcrumb .sep { color: #cbd5e1; }
.topbar-right { display: flex; align-items: center; gap: 16px; }
.today { font-size: 13px; color: #64748b; }
.avatar {
  width: 34px; height: 34px; border-radius: 50%;
  background: linear-gradient(135deg, #eef1ff, #dde3ff); color: #4f5bd5;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700;
}
.admin-name { font-size: 13px; color: #475569; font-weight: 600; }

.content { padding: 22px 18px 30px; flex: 1; }
.page-heading { display: flex; justify-content: space-between; align-items: center; margin-bottom: 22px; }
.page-heading h1 { margin: 0; font-size: 22px; color: #16213b; font-weight: 700; }
.page-heading p { margin: 6px 0 0; font-size: 13px; color: #94a3b8; }

.stat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px; }
.stat-card {
  background: #fff; border: 1px solid #eef0f5; border-radius: 14px; padding: 16px;
  display: flex; align-items: center; gap: 16px;
  box-shadow: 0 2px 10px rgba(30, 41, 59, 0.03);
  transition: transform 0.18s, box-shadow 0.18s;
}
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 8px 22px rgba(30, 41, 59, 0.08); }
.stat-icon {
  width: 50px; height: 50px; border-radius: 12px; display: flex;
  align-items: center; justify-content: center; flex-shrink: 0;
}
.stat-body { display: flex; flex-direction: column; line-height: 1.35; }
.stat-label { font-size: 13px; color: #64748b; }
.stat-value { font-size: 26px; color: #16213b; font-weight: 700; margin: 2px 0; }
.stat-hint { font-size: 11px; color: #a8b3c5; }

.panel {
  background: #fff; border: 1px solid #eef0f5; border-radius: 14px; padding: 16px;
  box-shadow: 0 2px 10px rgba(30, 41, 59, 0.03);
}
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; gap: 12px; }
.toolbar-left { display: flex; align-items: center; gap: 10px; }
.search-input { width: 250px; }

.name-cell { display: flex; align-items: center; gap: 8px; }
.name-avatar {
  width: 26px; height: 26px; border-radius: 8px; flex-shrink: 0;
  background: #eef1ff; color: #5b6cf6;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700;
}
.name-meta { display: flex; flex-direction: column; line-height: 1.25; min-width: 0; }
.name-text { font-size: 13px; color: #1e293b; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.name-meta small { font-size: 11px; color: #94a3b8; }

.expire-cell { display: flex; flex-direction: column; line-height: 1.3; }
.expire-cell small { font-size: 11px; }
.expire-cell .hint-danger { color: #ef4444; }
.expire-cell .hint-warn { color: #e08b0b; }

.status-badge {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 3px 8px; border-radius: 12px; font-size: 12px; font-weight: 500; line-height: 1.4;
}
.status-dot { width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; }
.st-active { background: #ecfdf5; color: #059669; }
.st-active .status-dot { background: #10b981; }
.st-soon { background: #fffbeb; color: #d97706; }
.st-soon .status-dot { background: #f59e0b; }
.st-expired { background: #fef2f2; color: #dc2626; }
.st-expired .status-dot { background: #ef4444; }
.st-disabled { background: #f1f5f9; color: #64748b; }
.st-disabled .status-dot { background: #94a3b8; }

.member-table { border-radius: 10px; overflow: hidden; }
.member-table .el-table__header-wrapper th {
  background: #f8f9fd !important; color: #475569; font-weight: 600; font-size: 13px;
}
.member-table .el-table__cell { padding: 9px 0; }
.member-table .cell { padding-left: 8px; padding-right: 8px; }

/* 日期不换行，避免日期被拆成两行 */
.date-cell { white-space: nowrap; }

/* 操作列四个按钮一行平铺，不收进「更多」下拉 */
.operation-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: nowrap;
  gap: 2px;
}
.operation-cell .el-button {
  padding: 4px 2px;
  margin: 0;
  font-size: 12px;
}
.operation-cell .el-button + .el-button { margin-left: 0; }

.pagination-bar { display: flex; justify-content: space-between; align-items: center; margin-top: 18px; }
.total-text { font-size: 13px; color: #94a3b8; }

@media (max-width: 1100px) {
  .stat-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 800px) {
  .sidebar { width: 68px; }
  .brand-text, .menu-label, .menu-item span, .menu-item em, .sidebar-footer span { display: none; }
  .brand { padding: 0 15px; }
  .menu-item { justify-content: center; padding: 0; }
  .stat-grid { grid-template-columns: 1fr; }
}
</style>

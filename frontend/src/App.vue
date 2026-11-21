<template>
  <div class="layout">
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
        <div class="menu-item active">
          <el-icon><User /></el-icon>
          <span>会员管理</span>
        </div>
        <div class="menu-item disabled" v-for="m in comingMenus" :key="m.name">
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.name }}</span>
          <em>待开发</em>
        </div>
      </nav>

      <div class="sidebar-footer">
        <div class="version-dot"></div>
        <span>系统运行中 · v0.1</span>
      </div>
    </aside>

    <!-- ========== 右侧主体 ========== -->
    <div class="main-wrap">
      <header class="topbar">
        <div class="breadcrumb">
          <el-icon><House /></el-icon>
          <span>工作台</span>
          <span class="sep">/</span>
          <strong>会员管理</strong>
        </div>
        <div class="topbar-right">
          <span class="today">{{ todayText }}</span>
          <div class="avatar">管</div>
        </div>
      </header>

      <main class="content">
        <!-- 页头 -->
        <div class="page-heading">
          <div>
            <h1>会员管理</h1>
            <p>查看和管理健身房所有会员信息</p>
          </div>
          <el-button type="primary" :icon="Refresh" plain @click="getMemberList">刷新数据</el-button>
        </div>

        <!-- 统计卡片：数据全部由当前列表实时计算 -->
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

        <!-- 会员列表面板 -->
        <section class="panel">
          <div class="toolbar">
            <div class="toolbar-left">
              <el-input
                v-model="keyword"
                placeholder="搜索姓名 / 手机号"
                clearable
                :prefix-icon="Search"
                class="search-input"
                @keyup.enter="handleSearch"
                @clear="handleSearch"
              />
              <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
              <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
            </div>
            <el-button type="primary" :icon="Plus" @click="addVisible = true">新增会员</el-button>
          </div>

          <!-- 前端本地筛选 + 本地分页，后端搜索/分页接口留给分工任务 -->
          <el-table
            :data="pagedList"
            v-loading="loading"
            class="member-table"
            :row-class-name="rowClassName"
            row-key="memberId"
            stripe
            style="width: 100%"
            empty-text="暂无会员数据"
          >
            <el-table-column label="姓名" width="110">
              <template #default="{ row }">
                <div class="name-cell">
                  <span class="name-avatar">{{ (row.memberName || '?').slice(0, 1) }}</span>
                  <span>{{ row.memberName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="phone" label="手机号" width="130" />
            <el-table-column prop="idCard" label="身份证" min-width="170" show-overflow-tooltip />
            <el-table-column label="性别" width="70" align="center">
              <template #default="{ row }">
                <el-tag :type="row.gender === '女' ? 'danger' : 'primary'" effect="plain" size="small">
                  {{ row.gender || '—' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="注册时间" width="120">
              <template #default="{ row }">{{ formatDate(row.registerTime) || '—' }}</template>
            </el-table-column>
            <el-table-column label="到期时间" width="120">
              <template #default="{ row }">{{ formatDate(row.expireTime) || '—' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="memberStatus(row).type" effect="light" size="small">
                  {{ memberStatus(row).text }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
            <el-table-column label="操作" width="150" fixed="right" align="center">
              <template #default="{ row }">
                <el-button type="primary" link :icon="Edit" @click="openEdit(row.memberId)">编辑</el-button>
                <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-bar">
            <span class="total-text">共 {{ filteredList.length }} 条记录</span>
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[5, 10, 20, 50]"
              :total="filteredList.length"
              layout="sizes, prev, pager, next"
              background
            />
          </div>
        </section>
      </main>
    </div>

    <!-- 新增 / 编辑弹窗 -->
    <AddMember v-model:visible="addVisible" @added="getMemberList" />
    <EditMember v-model:visible="editVisible" :member-id="editId" @saved="getMemberList" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, User, House, TrendCharts, Calendar, Clock } from '@element-plus/icons-vue'
import AddMember from './components/AddMember.vue'
import EditMember from './components/EditMember.vue'
import request from './utils/request'

const comingMenus = [
  { name: '卡种套餐', icon: 'Ticket' },
  { name: '教练管理', icon: 'UserFilled' },
  { name: '课程管理', icon: 'Calendar' },
  { name: '课程预约', icon: 'Clock' }
]

const memberList = ref([])
const loading = ref(false)
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const addVisible = ref(false)
const editVisible = ref(false)
const editId = ref(null)

const todayText = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric', month: 'long', day: 'numeric', weekday: 'long'
}).format(new Date())

// ---------- 日期工具：兼容后端返回的时间戳数字 / ISO 字符串 ----------
const toDate = (val) => {
  if (!val) return null
  if (val instanceof Date) return val
  return new Date(val)
}

const formatDate = (val) => {
  const d = toDate(val)
  if (!d || Number.isNaN(d.getTime())) return ''
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

const daysUntilExpire = (val) => {
  const d = toDate(val)
  if (!d || Number.isNaN(d.getTime())) return null
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return Math.ceil((d.getTime() - today.getTime()) / 86400000)
}

const isExpire = (val) => {
  const days = daysUntilExpire(val)
  return days !== null && days < 0
}

// 状态：优先看停用标记，其次按到期时间判断
const memberStatus = (row) => {
  if (row.memberStatus === 2) return { text: '停用', type: 'info' }
  const days = daysUntilExpire(row.expireTime)
  if (days === null) return { text: '未设置', type: 'info' }
  if (days < 0) return { text: '已到期', type: 'danger' }
  if (days <= 7) return { text: '即将到期', type: 'warning' }
  return { text: '正常', type: 'success' }
}

const rowClassName = ({ row }) => (isExpire(row.expireTime) ? 'expired-row' : '')

// ---------- 统计卡片 ----------
const statCards = computed(() => {
  const list = memberList.value
  const now = new Date()
  const thisMonth = list.filter((m) => {
    const d = toDate(m.registerTime)
    return d && d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth()
  }).length
  const expiring = list.filter((m) => {
    const d = daysUntilExpire(m.expireTime)
    return d !== null && d >= 0 && d <= 7 && m.memberStatus !== 2
  }).length
  const expired = list.filter((m) => isExpire(m.expireTime)).length

  return [
    { label: '会员总数', value: list.length, hint: '系统全部会员', icon: User, color: '#5b6cf6', bg: '#eef1ff' },
    { label: '本月新增', value: thisMonth, hint: '本月新注册', icon: TrendCharts, color: '#16a34a', bg: '#e9f9f0' },
    { label: '即将到期', value: expiring, hint: '7 天内到期', icon: Clock, color: '#f59e0b', bg: '#fff6e6' },
    { label: '已到期', value: expired, hint: '需要续费提醒', icon: Calendar, color: '#ef4444', bg: '#fef0f0' }
  ]
})

// ---------- 搜索 + 本地分页 ----------
const appliedKeyword = ref('')

const filteredList = computed(() => {
  const kw = appliedKeyword.value.trim().toLowerCase()
  if (!kw) return memberList.value
  return memberList.value.filter((m) =>
    (m.memberName || '').toLowerCase().includes(kw) ||
    (m.phone || '').toLowerCase().includes(kw)
  )
})

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

const handleSearch = () => {
  appliedKeyword.value = keyword.value
  currentPage.value = 1
}

const resetSearch = () => {
  keyword.value = ''
  appliedKeyword.value = ''
  currentPage.value = 1
}

// ---------- 数据请求 ----------
const getMemberList = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/member/list')
    memberList.value = Array.isArray(res.data) ? res.data : []
    if (currentPage.value > Math.max(1, Math.ceil(filteredList.value.length / pageSize.value))) {
      currentPage.value = 1
    }
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const openEdit = (id) => {
  editId.value = id
  editVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除会员「${row.memberName}」吗？删除后不可恢复。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
    await request.delete(`/api/member/delete/${row.memberId}`)
    ElMessage.success('删除成功')
    getMemberList()
  } catch (err) {
    // 用户点取消时不提示
    if (err !== 'cancel' && err?.message !== 'cancel') console.error(err)
  }
}

onMounted(getMemberList)
</script>

<style>
/* 全局主题变量：覆盖 Element Plus 默认色，与后台风格统一 */
:root {
  --el-color-primary: #5b6cf6;
  --el-border-radius-base: 8px;
  --el-font-family: 'PingFang SC', 'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
}
</style>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
  background: #f4f6fb;
}

/* ---------- 侧边栏 ---------- */
.sidebar {
  width: 240px;
  flex-shrink: 0;
  background: linear-gradient(180deg, #1e2a44 0%, #141d33 100%);
  color: #cbd5e1;
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
}
.brand {
  height: 76px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.07);
}
.brand-mark {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: linear-gradient(135deg, #6d7dff, #4658e8);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 800;
  box-shadow: 0 6px 16px rgba(91, 108, 246, 0.4);
}
.brand-text { display: flex; flex-direction: column; line-height: 1.4; }
.brand-text strong { color: #fff; font-size: 15px; letter-spacing: 0.5px; }
.brand-text span { font-size: 9px; color: #7f8ca8; letter-spacing: 1.2px; }

.menu-label {
  padding: 22px 22px 8px;
  font-size: 11px;
  color: #6c7b98;
  letter-spacing: 1px;
}
.menu { padding: 0 12px; flex: 1; }
.menu-item {
  height: 42px;
  padding: 0 12px;
  margin-bottom: 4px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 11px;
  font-size: 14px;
  transition: all 0.18s;
}
.menu-item .el-icon { font-size: 17px; }
.menu-item.active {
  background: linear-gradient(90deg, rgba(91, 108, 246, 0.32), rgba(91, 108, 246, 0.12));
  color: #fff;
  font-weight: 600;
  box-shadow: inset 3px 0 0 #6d7dff;
}
.menu-item.disabled { color: #7b88a3; cursor: default; }
.menu-item.disabled em {
  margin-left: auto;
  font-size: 10px;
  font-style: normal;
  color: #64748b;
  border: 1px solid #3b4763;
  border-radius: 4px;
  padding: 1px 5px;
}
.sidebar-footer {
  height: 56px;
  padding: 0 22px;
  border-top: 1px solid rgba(255, 255, 255, 0.07);
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #7f8ca8;
}
.version-dot {
  width: 7px; height: 7px; border-radius: 50%;
  background: #34d399; box-shadow: 0 0 0 3px rgba(52, 211, 153, 0.15);
}

/* ---------- 顶栏 ---------- */
.main-wrap { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.topbar {
  height: 76px;
  flex-shrink: 0;
  background: #fff;
  border-bottom: 1px solid #eef0f5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
}
.breadcrumb { display: flex; align-items: center; gap: 8px; font-size: 14px; color: #94a3b8; }
.breadcrumb strong { color: #1e293b; font-weight: 600; }
.breadcrumb .sep { color: #cbd5e1; }
.topbar-right { display: flex; align-items: center; gap: 16px; }
.today { font-size: 13px; color: #64748b; }
.avatar {
  width: 34px; height: 34px; border-radius: 50%;
  background: linear-gradient(135deg, #eef1ff, #dde3ff);
  color: #4f5bd5; display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700;
}

/* ---------- 内容区 ---------- */
.content { padding: 26px 28px 34px; flex: 1; }
.page-heading { display: flex; justify-content: space-between; align-items: center; margin-bottom: 22px; }
.page-heading h1 { margin: 0; font-size: 22px; color: #16213b; font-weight: 700; }
.page-heading p { margin: 6px 0 0; font-size: 13px; color: #94a3b8; }

.stat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px; }
.stat-card {
  background: #fff;
  border: 1px solid #eef0f5;
  border-radius: 14px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 10px rgba(30, 41, 59, 0.03);
  transition: transform 0.18s, box-shadow 0.18s;
}
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 8px 22px rgba(30, 41, 59, 0.08); }
.stat-icon { width: 50px; height: 50px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.stat-body { display: flex; flex-direction: column; line-height: 1.35; }
.stat-label { font-size: 13px; color: #64748b; }
.stat-value { font-size: 26px; color: #16213b; font-weight: 700; margin: 2px 0; }
.stat-hint { font-size: 11px; color: #a8b3c5; }

.panel {
  background: #fff;
  border: 1px solid #eef0f5;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(30, 41, 59, 0.03);
}
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; gap: 12px; }
.toolbar-left { display: flex; align-items: center; gap: 10px; }
.search-input { width: 250px; }

.name-cell { display: flex; align-items: center; gap: 9px; }
.name-avatar {
  width: 28px; height: 28px; border-radius: 8px; flex-shrink: 0;
  background: #eef1ff; color: #5b6cf6;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700;
}

.member-table { border-radius: 10px; overflow: hidden; }
.member-table :deep(.el-table__header-wrapper th) {
  background: #f8f9fd !important;
  color: #475569;
  font-weight: 600;
  font-size: 13px;
}
.member-table :deep(.el-table__cell) { padding: 10px 0; }
:deep(.expired-row) { background: #fff5f5 !important; }
:deep(.el-table__body tr.expired-row:hover > td) { background: #ffecec !important; }

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

<template>
  <div class="view-page">
    <!-- 页头 -->
    <div class="page-heading">
      <div>
        <h1>会员管理</h1>
        <p>查看和管理健身房所有会员信息</p>
      </div>
      <el-button type="primary" :icon="Refresh" plain @click="refreshAll">刷新数据</el-button>
    </div>

    <!-- 统计卡片：基于 /list 全量数据计算，不受搜索/分页影响 -->
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

      <!-- 服务端搜索 + 服务端分页，数据来自 /api/member/page -->
      <el-table
        :data="memberList"
        v-loading="loading"
        class="member-table"
        row-key="memberId"
        stripe
        style="width: 100%"
        empty-text="暂无会员数据"
      >
        <el-table-column label="姓名" width="118">
          <template #default="{ row }">
            <div class="name-cell">
              <span class="name-avatar">{{ (row.memberName || '?').slice(0, 1) }}</span>
              <div class="name-meta">
                <span class="name-text">{{ row.memberName }}</span>
                <small>{{ row.gender || '—' }}</small>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column label="身份证" width="105" show-overflow-tooltip>
          <template #default="{ row }">{{ maskIdCard(row.idCard) }}</template>
        </el-table-column>
        <el-table-column label="注册时间" width="116">
          <template #default="{ row }"><span class="date-cell">{{ formatDate(row.registerTime) || '—' }}</span></template>
        </el-table-column>
        <el-table-column label="到期时间" width="122">
          <template #default="{ row }">
            <div class="expire-cell">
              <span class="date-cell">{{ formatDate(row.expireTime) || '—' }}</span>
              <small v-if="expireHint(row.expireTime)" :class="expireHint(row.expireTime).cls">
                {{ expireHint(row.expireTime).text }}
              </small>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="92" align="center">
          <template #default="{ row }">
            <span class="status-badge" :class="memberStatus(row).cls">
              <i class="status-dot"></i>{{ memberStatus(row).text }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="110" show-overflow-tooltip />
        <el-table-column label="操作" width="212" fixed="right" align="center">
          <template #default="{ row }">
            <div class="operation-cell">
              <el-button type="success" link size="small" :icon="Wallet" @click="openRenew(row)">续费</el-button>
              <el-button type="primary" link size="small" :icon="Edit" @click="openEdit(row.memberId)">编辑</el-button>
              <el-button
                :type="row.memberStatus === 2 ? 'success' : 'warning'"
                link
                size="small"
                :icon="row.memberStatus === 2 ? Unlock : Lock"
                @click="toggleStatus(row)"
              >{{ row.memberStatus === 2 ? '启用' : '停用' }}</el-button>
              <el-button type="danger" link size="small" :icon="Delete" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <span class="total-text">共 {{ total }} 条记录</span>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="total"
          layout="sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="getMemberPage"
        />
      </div>
    </section>

    <!-- 新增 / 编辑 / 续费弹窗 -->
    <AddMember v-model:visible="addVisible" @added="refreshAll" />
    <EditMember v-model:visible="editVisible" :member-id="editId" @saved="refreshAll" />
    <RenewMember
      v-model:visible="renewVisible"
      :member-id="renewMemberId"
      :member="renewMember"
      @renewed="refreshAll"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Refresh, Edit, Delete, Wallet, Lock, Unlock,
  User, TrendCharts, Calendar, Clock
} from '@element-plus/icons-vue'
import AddMember from '../components/AddMember.vue'
import EditMember from '../components/EditMember.vue'
import RenewMember from '../components/RenewMember.vue'
import request from '../utils/request'

const memberList = ref([])        // 当前页数据（来自 /page）
const allMembers = ref([])        // 全量数据（来自 /list），仅用于顶部统计卡
const total = ref(0)
const loading = ref(false)
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const addVisible = ref(false)
const editVisible = ref(false)
const editId = ref(null)
const renewVisible = ref(false)
const renewMemberId = ref(null)
const renewMember = ref(null)

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

const maskIdCard = (id) => {
  if (!id || id.length < 8) return '—'
  return `****${id.slice(-4)}`
}

const expireHint = (val) => {
  const days = daysUntilExpire(val)
  if (days === null) return null
  if (days < 0) return { text: `已过期 ${-days} 天`, cls: 'hint-danger' }
  if (days <= 7) return { text: days === 0 ? '今天到期' : `剩 ${days} 天`, cls: 'hint-warn' }
  return null
}

// 状态：先说会籍是否停用，再按到期时间说明；颜色和文案一一对应
const memberStatus = (row) => {
  if (row.memberStatus === 2) return { text: '停用', cls: 'st-disabled' }
  const days = daysUntilExpire(row.expireTime)
  if (days === null) return { text: '未设置', cls: 'st-disabled' }
  if (days < 0) return { text: '已到期', cls: 'st-expired' }
  if (days <= 7) return { text: '即将到期', cls: 'st-soon' }
  return { text: '正常', cls: 'st-active' }
}

// ---------- 统计卡片：基于全量数据 ----------
const statCards = computed(() => {
  const list = allMembers.value
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

// ---------- 服务端分页 + 搜索 ----------
const getMemberPage = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/member/page', {
      params: {
        keyword: keyword.value,
        pageNum: currentPage.value,
        pageSize: pageSize.value
      }
    })
    const data = res.data || {}
    memberList.value = Array.isArray(data.list) ? data.list : []
    total.value = Number(data.total) || 0
    const maxPage = Math.max(1, Math.ceil(total.value / pageSize.value))
    if (currentPage.value > maxPage) {
      currentPage.value = maxPage
      getMemberPage()
    }
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

/** 统计卡用的全量数据，单独走 /list，不受分页影响 */
const getAllMembers = async () => {
  try {
    const res = await request.get('/api/member/list')
    allMembers.value = Array.isArray(res.data) ? res.data : []
  } catch (err) {
    console.error(err)
  }
}

const refreshAll = () => {
  getMemberPage()
  getAllMembers()
}

const handleSearch = () => {
  currentPage.value = 1
  getMemberPage()
}

const resetSearch = () => {
  keyword.value = ''
  currentPage.value = 1
  getMemberPage()
}

const handleSizeChange = () => {
  currentPage.value = 1
  getMemberPage()
}

// ---------- 弹窗操作 ----------
const openEdit = (id) => {
  editId.value = id
  editVisible.value = true
}

const openRenew = (row) => {
  renewMemberId.value = row.memberId
  renewMember.value = row
  renewVisible.value = true
}

// ---------- 状态管理（A4）：启用 / 停用 ----------
const toggleStatus = async (row) => {
  const toDisable = row.memberStatus !== 2
  const action = toDisable ? '停用' : '启用'
  try {
    await ElMessageBox.confirm(
      `确定要${action}会员「${row.memberName}」吗？${toDisable ? '停用后该会员不会被到期提醒扫描。' : ''}`,
      `${action}确认`,
      { type: 'warning', confirmButtonText: `确认${action}`, cancelButtonText: '取消' }
    )
    await request.put(`/api/member/status/${row.memberId}`, null, {
      params: { status: toDisable ? 2 : 1 }
    })
    ElMessage.success(`${action}成功`)
    refreshAll()
  } catch (err) {
    if (err !== 'cancel' && err?.message !== 'cancel') console.error(err)
  }
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
    refreshAll()
  } catch (err) {
    if (err !== 'cancel' && err?.message !== 'cancel') console.error(err)
  }
}

onMounted(refreshAll)
</script>

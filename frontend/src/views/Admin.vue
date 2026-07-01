<template>
  <div class="admin-container">
    <div class="dynamic-background"></div>

    <!-- 顶栏 -->
    <header class="header">
      <div class="header-content">
        <div class="logo-area">
          <span class="logo">文档编辑器</span>
          <span class="admin-badge">管理后台</span>
        </div>
        <div class="user-info">
          <span class="admin-name">👤 {{ adminName }}</span>
          <button @click="logout" class="logout-btn">退出登录</button>
        </div>
      </div>
    </header>

    <main class="main-content">

      <!-- 统计卡片 -->
      <section class="stats-section">
        <h2 class="section-title">📊 站点概览</h2>
        <div v-if="statsLoading" class="loading">
          <div class="spinner"></div><p>加载中...</p>
        </div>
        <div v-else class="stats-grid">
          <div class="stat-card blue">
            <div class="stat-icon">👥</div>
            <div class="stat-info">
              <p class="stat-label">注册用户</p>
              <p class="stat-value">{{ stats.totalUsers ?? '—' }}</p>
              <p class="stat-sub">近 7 天新增 {{ stats.newUsersThisWeek ?? 0 }} 人</p>
            </div>
          </div>
          <div class="stat-card purple">
            <div class="stat-icon">📄</div>
            <div class="stat-info">
              <p class="stat-label">文档总数</p>
              <p class="stat-value">{{ stats.totalDocuments ?? '—' }}</p>
              <p class="stat-sub">近 7 天新增 {{ stats.newDocsThisWeek ?? 0 }} 篇</p>
            </div>
          </div>
          <div class="stat-card green">
            <div class="stat-icon">✍️</div>
            <div class="stat-info">
              <p class="stat-label">累计字数</p>
              <p class="stat-value">{{ formatNumber(stats.totalWords) }}</p>
              <p class="stat-sub">全站合计</p>
            </div>
          </div>
          <div class="stat-card orange">
            <div class="stat-icon">📈</div>
            <div class="stat-info">
              <p class="stat-label">人均文档</p>
              <p class="stat-value">{{ avgDocs }}</p>
              <p class="stat-sub">篇 / 用户</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 用户列表 -->
      <section class="users-section">
        <div class="section-header">
          <h2 class="section-title">👤 用户列表</h2>
          <div class="search-bar">
            <input
              v-model="searchQuery"
              placeholder="搜索用户名 / 邮箱 / 姓名…"
              class="search-input"
            />
          </div>
        </div>

        <div v-if="usersLoading" class="loading">
          <div class="spinner"></div><p>加载中...</p>
        </div>

        <div v-else-if="filteredUsers.length === 0" class="empty-state">
          <div class="empty-icon">🔍</div>
          <p>没有匹配的用户</p>
        </div>

        <div v-else class="table-wrapper">
          <table class="user-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>账号</th>
                <th>姓名</th>
                <th>邮箱</th>
                <th>角色</th>
                <th>文档数</th>
                <th>注册时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in filteredUsers" :key="user.id">
                <td class="td-id">{{ user.id }}</td>
                <td class="td-bold">{{ user.userId }}</td>
                <td>{{ user.name }}</td>
                <td class="td-email">{{ user.email }}</td>
                <td>
                  <span :class="['role-badge', user.role === 'admin' ? 'role-admin' : 'role-user']">
                    {{ user.role === 'admin' ? '管理员' : '用户' }}
                  </span>
                </td>
                <td class="td-center">{{ user.docCount }}</td>
                <td class="td-date">{{ formatDate(user.createdAt) }}</td>
                <td class="td-actions">
                  <button class="action-btn pwd-btn" @click="openResetPwd(user)" title="修改密码">🔑 改密</button>
                  <button
                    class="action-btn del-btn"
                    @click="openDelete(user)"
                    :disabled="user.id === currentAdminId"
                    :title="user.id === currentAdminId ? '不能删除自己' : '删除用户'"
                  >🗑 删除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

    </main>

    <!-- 删除确认弹窗 -->
    <transition name="modal-fade">
      <div v-if="deleteModal.show" class="modal-mask" @click.self="deleteModal.show = false">
        <div class="modal-box">
          <div class="modal-icon danger">🗑</div>
          <h3 class="modal-title">确认删除用户</h3>
          <p class="modal-desc">
            即将删除用户 <strong>{{ deleteModal.user?.userId }}</strong>（{{ deleteModal.user?.name }}），
            其名下 <strong>{{ deleteModal.user?.docCount }}</strong> 篇文档也将一并删除，此操作不可恢复。
          </p>
          <div class="modal-actions">
            <button class="modal-btn cancel" @click="deleteModal.show = false">取消</button>
            <button class="modal-btn danger" :disabled="deleteModal.loading" @click="confirmDelete">
              {{ deleteModal.loading ? '删除中…' : '确认删除' }}
            </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 重置密码弹窗 -->
    <transition name="modal-fade">
      <div v-if="pwdModal.show" class="modal-mask" @click.self="closePwdModal">
        <div class="modal-box">
          <div class="modal-icon primary">🔑</div>
          <h3 class="modal-title">重置密码</h3>
          <p class="modal-desc">为用户 <strong>{{ pwdModal.user?.userId }}</strong>（{{ pwdModal.user?.name }}）设置新密码</p>
          <div class="modal-field">
            <label>新密码（至少 6 位）</label>
            <div class="pwd-input-wrap">
              <input
                :type="pwdModal.visible ? 'text' : 'password'"
                v-model="pwdModal.password"
                placeholder="请输入新密码"
                class="modal-input"
                @keydown.enter="confirmResetPwd"
              />
              <button class="eye-btn" @click="pwdModal.visible = !pwdModal.visible">
                {{ pwdModal.visible ? '🙈' : '👁' }}
              </button>
            </div>
            <p v-if="pwdModal.error" class="field-error">{{ pwdModal.error }}</p>
          </div>
          <div class="modal-actions">
            <button class="modal-btn cancel" @click="closePwdModal">取消</button>
            <button class="modal-btn primary" :disabled="pwdModal.loading" @click="confirmResetPwd">
              {{ pwdModal.loading ? '保存中…' : '确认重置' }}
            </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 全局 Toast -->
    <transition name="toast-slide">
      <div v-if="toast.show" :class="['toast', toast.type]">{{ toast.msg }}</div>
    </transition>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import apiClient from '@/utils/api'

const router = useRouter()
const adminName      = ref('')
const currentAdminId = ref(null)   // 当前登录管理员的数字 id，用于禁止自删

const stats = ref({})
const statsLoading = ref(true)
const users = ref([])
const usersLoading = ref(true)
const searchQuery = ref('')

// 删除弹窗
const deleteModal = ref({ show: false, user: null, loading: false })

function openDelete(user) {
  deleteModal.value = { show: true, user, loading: false }
}

async function confirmDelete() {
  deleteModal.value.loading = true
  try {
    const res = await apiClient.delete(`/admin/users/${deleteModal.value.user.id}`)
    if (res.data?.success) {
      users.value = users.value.filter(u => u.id !== deleteModal.value.user.id)
      showToast('用户已删除', 'success')
      loadStats()   // 刷新统计卡片
    } else {
      showToast(res.data?.message || '删除失败', 'error')
    }
  } catch (e) {
    showToast('删除请求失败', 'error')
  } finally {
    deleteModal.value.loading = false
    deleteModal.value.show    = false
  }
}

// 改密弹窗
const pwdModal = ref({ show: false, user: null, password: '', visible: false, loading: false, error: '' })

function openResetPwd(user) {
  pwdModal.value = { show: true, user, password: '', visible: false, loading: false, error: '' }
}

function closePwdModal() {
  pwdModal.value.show = false
}

async function confirmResetPwd() {
  const pwd = pwdModal.value.password
  if (!pwd || pwd.length < 6) {
    pwdModal.value.error = '密码至少 6 位'
    return
  }
  pwdModal.value.error   = ''
  pwdModal.value.loading = true
  try {
    const res = await apiClient.put(`/admin/users/${pwdModal.value.user.id}/password`, { password: pwd })
    if (res.data?.success) {
      showToast('密码已重置', 'success')
      pwdModal.value.show = false
    } else {
      pwdModal.value.error = res.data?.message || '重置失败'
    }
  } catch (e) {
    pwdModal.value.error = '请求失败，请重试'
  } finally {
    pwdModal.value.loading = false
  }
}

// Toast 通知 
const toast = ref({ show: false, msg: '', type: 'success' })
let toastTimer = null
function showToast(msg, type = 'success') {
  if (toastTimer) clearTimeout(toastTimer)
  toast.value = { show: true, msg, type }
  toastTimer = setTimeout(() => { toast.value.show = false }, 3000)
}

const avgDocs = computed(() => {
  const u = Number(stats.value.totalUsers)
  const d = Number(stats.value.totalDocuments)
  if (!u) return '—'
  return (d / u).toFixed(1)
})

const filteredUsers = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return users.value
  return users.value.filter(u =>
    (u.userId || '').toLowerCase().includes(q) ||
    (u.email  || '').toLowerCase().includes(q) ||
    (u.name   || '').toLowerCase().includes(q)
  )
})

const formatNumber = (n) => {
  const num = Number(n)
  if (!num) return '0'
  if (num >= 10000) return (num / 10000).toFixed(1) + ' 万'
  return num.toLocaleString()
}

const formatDate = (dateString) => {
  if (!dateString) return '—'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

const loadStats = async () => {
  try {
    const res = await apiClient.get('/admin/stats')
    if (res.data?.success) stats.value = res.data.data || {}
  } catch (e) {
    console.error('加载统计失败:', e)
  } finally {
    statsLoading.value = false
  }
}

const loadUsers = async () => {
  try {
    const res = await apiClient.get('/admin/users')
    if (res.data?.success) users.value = res.data.data || []
  } catch (e) {
    console.error('加载用户失败:', e)
  } finally {
    usersLoading.value = false
  }
}

const logout = () => {
  localStorage.removeItem('user')
  localStorage.removeItem('token')
  router.push('/')
}

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (!userStr) { router.push('/'); return }
  try {
    const user = JSON.parse(userStr)
    if (user.role !== 'admin') { router.push('/home'); return }
    adminName.value      = user.name || user.userId
    currentAdminId.value = user.Id ?? user.id ?? null
    loadStats()
    loadUsers()
  } catch {
    localStorage.removeItem('user')
    router.push('/')
  }
})
</script>

<style scoped>
.admin-container {
  position: relative;
  min-height: 100vh;
  background: #f8fafc;
}

.dynamic-background {
  position: fixed;
  inset: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  opacity: 0.05;
  z-index: -1;
}

/* ── Header ── */
.header {
  background: white;
  border-bottom: 1px solid #e1e5e9;
  box-shadow: 0 2px 4px rgba(0,0,0,0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-area { display: flex; align-items: center; gap: 10px; }
.logo { font-size: 22px; font-weight: 700; color: #1e293b; }
.admin-badge {
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-radius: 20px;
  letter-spacing: 0.5px;
}

.user-info { display: flex; align-items: center; gap: 14px; }
.admin-name { color: #6b7280; font-size: 14px; }

.logout-btn {
  padding: 7px 14px;
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.2s;
}
.logout-btn:hover { background: #dc2626; }

/* ── Main ── */
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
  display: flex;
  flex-direction: column;
  gap: 36px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 18px;
}

/* ── Stats ── */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 18px;
}

.stat-card {
  background: white;
  border-radius: 14px;
  padding: 22px 20px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  border: 1px solid #e1e5e9;
  transition: transform 0.2s, box-shadow 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.08);
}

.stat-icon { font-size: 32px; line-height: 1; margin-top: 2px; }

.stat-info { flex: 1; }
.stat-label { font-size: 13px; color: #6b7280; margin: 0 0 4px; }
.stat-value { font-size: 28px; font-weight: 700; margin: 0 0 4px; line-height: 1.1; }
.stat-sub { font-size: 12px; color: #9ca3af; margin: 0; }

.stat-card.blue  .stat-value { color: #3b82f6; }
.stat-card.purple .stat-value { color: #7c3aed; }
.stat-card.green  .stat-value { color: #10b981; }
.stat-card.orange .stat-value { color: #f59e0b; }

/* ── Users ── */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
  flex-wrap: wrap;
  gap: 12px;
}
.section-header .section-title { margin-bottom: 0; }

.search-input {
  padding: 8px 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  width: 260px;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.search-input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59,130,246,0.12);
}

.table-wrapper {
  background: white;
  border-radius: 14px;
  border: 1px solid #e1e5e9;
  overflow: hidden;
}

.user-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.user-table thead {
  background: #f8fafc;
  border-bottom: 1px solid #e1e5e9;
}

.user-table th {
  padding: 12px 16px;
  text-align: left;
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.user-table td {
  padding: 13px 16px;
  color: #374151;
  border-bottom: 1px solid #f1f5f9;
}

.user-table tbody tr:last-child td { border-bottom: none; }

.user-table tbody tr:hover { background: #fafbff; }

.td-id { color: #9ca3af; font-size: 13px; }
.td-bold { font-weight: 600; color: #1e293b; }
.td-email { color: #6b7280; font-size: 13px; }
.td-center { text-align: center; font-weight: 600; }
.td-date { color: #9ca3af; font-size: 13px; white-space: nowrap; }

.role-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}
.role-admin {
  background: #ede9fe;
  color: #7c3aed;
}
.role-user {
  background: #dbeafe;
  color: #2563eb;
}

/* ── Loading / Empty ── */
.loading { text-align: center; padding: 48px; color: #6b7280; }
.spinner {
  width: 32px; height: 32px;
  border: 3px solid #e5e7eb;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 12px;
}
@keyframes spin { to { transform: rotate(360deg); } }

.empty-state { text-align: center; padding: 48px; color: #6b7280; }
.empty-icon { font-size: 48px; margin-bottom: 12px; }

/* ── Responsive ── */
@media (max-width: 768px) {
  .header-content { padding: 12px 16px; }
  .main-content { padding: 20px 16px; gap: 24px; }
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .section-header { flex-direction: column; align-items: flex-start; }
  .search-input { width: 100%; }
  .user-table { font-size: 13px; }
  .user-table th, .user-table td { padding: 10px 12px; }
  .td-email, .td-date { display: none; }
}

@media (max-width: 480px) {
  .stats-grid { grid-template-columns: 1fr; }
  .admin-badge { display: none; }
}

/* ── 操作按钮 ── */
.td-actions { white-space: nowrap; }

.action-btn {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 4px 10px; border-radius: 6px; font-size: 12px;
  font-weight: 500; cursor: pointer; border: 1px solid transparent;
  transition: all 0.15s; margin-right: 6px;
}
.action-btn:last-child { margin-right: 0; }
.action-btn:disabled { opacity: 0.4; cursor: not-allowed; }

.pwd-btn {
  background: #eff6ff; color: #2563eb; border-color: #bfdbfe;
}
.pwd-btn:hover:not(:disabled) { background: #dbeafe; border-color: #93c5fd; }

.del-btn {
  background: #fff1f2; color: #e11d48; border-color: #fecdd3;
}
.del-btn:hover:not(:disabled) { background: #ffe4e6; border-color: #fda4af; }

/* ── 弹窗 ── */
.modal-mask {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(2px);
}

.modal-box {
  background: white;
  border-radius: 16px;
  padding: 32px 28px;
  width: 420px;
  max-width: calc(100vw - 32px);
  box-shadow: 0 20px 60px rgba(0,0,0,0.18);
  display: flex; flex-direction: column; align-items: center; gap: 12px;
}

.modal-icon { font-size: 40px; line-height: 1; }
.modal-title { font-size: 18px; font-weight: 700; color: #1e293b; margin: 0; text-align: center; }
.modal-desc { font-size: 14px; color: #6b7280; text-align: center; margin: 0; line-height: 1.6; }
.modal-desc strong { color: #1e293b; }

.modal-field { width: 100%; display: flex; flex-direction: column; gap: 6px; }
.modal-field label { font-size: 13px; font-weight: 500; color: #374151; }

.pwd-input-wrap { position: relative; display: flex; align-items: center; }
.modal-input {
  width: 100%; padding: 9px 40px 9px 12px;
  border: 1px solid #d1d5db; border-radius: 8px;
  font-size: 14px; outline: none; box-sizing: border-box;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.modal-input:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.12); }

.eye-btn {
  position: absolute; right: 10px;
  background: none; border: none; cursor: pointer; font-size: 16px; line-height: 1;
  padding: 0; color: #9ca3af;
}

.field-error { font-size: 12px; color: #ef4444; margin: 0; }

.modal-actions { display: flex; gap: 10px; width: 100%; margin-top: 4px; }
.modal-btn {
  flex: 1; padding: 10px; border-radius: 8px;
  font-size: 14px; font-weight: 600; cursor: pointer;
  border: 1px solid transparent; transition: all 0.15s;
}
.modal-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.modal-btn.cancel { background: #f1f5f9; color: #374151; border-color: #e2e8f0; }
.modal-btn.cancel:hover:not(:disabled) { background: #e2e8f0; }
.modal-btn.danger { background: #ef4444; color: white; }
.modal-btn.danger:hover:not(:disabled) { background: #dc2626; }
.modal-btn.primary { background: #3b82f6; color: white; }
.modal-btn.primary:hover:not(:disabled) { background: #2563eb; }

/* ── Toast ── */
.toast {
  position: fixed; bottom: 28px; left: 50%; transform: translateX(-50%);
  padding: 11px 24px; border-radius: 24px;
  font-size: 14px; font-weight: 500;
  box-shadow: 0 4px 16px rgba(0,0,0,0.14);
  z-index: 2000; white-space: nowrap;
}
.toast.success { background: #10b981; color: white; }
.toast.error   { background: #ef4444; color: white; }

/* ── 动画 ── */
.modal-fade-enter-active, .modal-fade-leave-active { transition: opacity 0.2s; }
.modal-fade-enter-from,   .modal-fade-leave-to    { opacity: 0; }

.toast-slide-enter-active, .toast-slide-leave-active { transition: all 0.3s ease; }
.toast-slide-enter-from, .toast-slide-leave-to { opacity: 0; transform: translate(-50%, 12px); }
</style>
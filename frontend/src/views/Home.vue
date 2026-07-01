<template>
  <div class="home-container">
    <div class="dynamic-background"></div>

    <header class="header">
      <div class="header-content">
        <h1 class="logo">文档编辑器</h1>
        <div class="user-info">
          <span>{{ userInfo.name || userInfo.userId }}</span>
          <button @click="logout" class="logout-btn">退出登录</button>
        </div>
      </div>
    </header>

    <main class="main-content">
      <div class="content-wrapper">

        <!-- 新建文档 -->
        <section class="create-section">
          <h2>新建文档</h2>
          <div class="create-card" @click="createNewDocument">
            <div class="create-icon">+</div>
            <p>创建新文档</p>
          </div>
        </section>

        <!-- 文档列表 -->
        <section class="documents-section">
          <div class="section-header">
            <h2>我的文档</h2>
            <div class="header-right">
              <!-- ✅ 搜索框 -->
              <div class="search-wrapper">
                <span class="search-icon">🔍</span>
                <input
                  v-model="searchQuery"
                  class="search-input"
                  placeholder="搜索标题或内容…"
                  @input="onSearchInput"
                  @keydown.enter="doSearch"
                  @keydown.esc="clearSearch"
                />
                <button v-if="searchQuery" class="search-clear" @click="clearSearch" title="清除搜索">✕</button>
              </div>

              <!-- 视图切换 -->
              <div class="view-toggle">
                <button :class="['view-btn', { active: viewMode === 'grid' }]" @click="viewMode = 'grid'">网格</button>
                <button :class="['view-btn', { active: viewMode === 'list' }]" @click="viewMode = 'list'">列表</button>
              </div>
            </div>
          </div>

          <!-- 搜索状态提示 -->
          <div v-if="isSearchMode" class="search-status">
            <span v-if="searchLoading">🔍 搜索中…</span>
            <span v-else-if="searchTotal > 0">
              找到 <strong>{{ searchTotal }}</strong> 个与"<strong>{{ activeKeyword }}</strong>"相关的文档
              <button class="search-back" @click="clearSearch">← 返回全部</button>
            </span>
            <span v-else>
              未找到与"<strong>{{ activeKeyword }}</strong>"相关的文档
              <button class="search-back" @click="clearSearch">← 返回全部</button>
            </span>
          </div>

          <!-- 加载态 -->
          <div v-if="loading || searchLoading" class="loading">
            <div class="spinner"></div>
            <p>{{ searchLoading ? '搜索中…' : '加载文档中…' }}</p>
          </div>

          <!-- 空态 -->
          <div v-else-if="displayDocs.length === 0" class="empty-state">
            <div class="empty-icon">{{ isSearchMode ? '🔍' : '📄' }}</div>
            <p>{{ isSearchMode ? '没有找到匹配的文档' : '还没有文档，点击上方创建新文档开始编辑' }}</p>
          </div>

          <!-- 文档卡片 -->
          <div v-else :class="['documents-container', viewMode]">
            <div
              v-for="doc in displayDocs"
              :key="doc.id"
              class="document-card"
              @click="openDocument(doc)"
            >
              <div class="document-icon">📝</div>

              <div class="document-info">
                <!-- 标题行 -->
                <div class="title-wrapper" @click.stop>
                  <input
                    v-if="editingTitleId === doc.id"
                    :ref="el => { if (el) titleInputRefs[doc.id] = el }"
                    v-model="editingTitleValue"
                    class="title-edit-input"
                    maxlength="100"
                    placeholder="请输入标题"
                    @blur="confirmTitleEdit(doc)"
                    @keydown.enter.prevent="confirmTitleEdit(doc)"
                    @keydown.esc.prevent="cancelTitleEdit"
                  />
                  <template v-else>
                    <!-- 搜索模式下标题高亮 -->
                    <h3 class="document-title" v-html="highlight(doc.title || '未命名文档')"></h3>
                    <button class="title-edit-btn" title="修改标题" @click.stop="startTitleEdit(doc)">✏️</button>
                  </template>
                </div>

                <!-- 搜索模式下内容预览高亮 -->
                <p class="document-preview" v-html="highlight(getPreview(doc.content))"></p>

                <div class="document-meta">
                  <span class="date">{{ formatDate(doc.updatedAt) }}</span>
                  <span class="word-count">{{ doc.wordCount }} 字</span>
                </div>
              </div>

              <div class="document-actions">
                <button @click.stop="deleteDocument(doc)" class="delete-btn">删除</button>
              </div>
            </div>
          </div>
        </section>

      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { documentAPI } from '@/utils/api'

const router = useRouter()

const loading     = ref(true)
const documents   = ref([])
const viewMode    = ref('grid')
const userInfo    = ref({})

// 搜索相关状态
const searchQuery   = ref('')
const searchResults = ref([])
const searchLoading = ref(false)
const searchTotal   = ref(0)
const activeKeyword = ref('')   // 已执行搜索的关键词（用于高亮 + 状态提示）
let   searchDebounce = null

const isSearchMode = computed(() => activeKeyword.value !== '')
const displayDocs  = computed(() => isSearchMode.value ? searchResults.value : documents.value)

// 内联标题编辑
const editingTitleId    = ref(null)
const editingTitleValue = ref('')
const titleInputRefs    = ref({})

const getUserInfo = () => {
  const user = localStorage.getItem('user')
  if (user) userInfo.value = JSON.parse(user)
}

const loadDocuments = async () => {
  try {
    loading.value = true
    const response = await documentAPI.getDocumentList({ page: 1, limit: 20 })
    if (response.data?.success) documents.value = response.data.data?.list || []
  } catch (e) {
    console.error('加载文档失败:', e)
    documents.value = []
  } finally {
    loading.value = false
  }
}

// 防抖搜索：输入停顿 400ms 自动触发
const onSearchInput = () => {
  clearTimeout(searchDebounce)
  if (!searchQuery.value.trim()) {
    // 清空关键词时立即恢复全部列表
    activeKeyword.value = ''
    searchResults.value = []
    searchTotal.value   = 0
    return
  }
  searchDebounce = setTimeout(doSearch, 400)
}

// 执行搜索
const doSearch = async () => {
  const q = searchQuery.value.trim()
  if (!q) return

  searchLoading.value = true
  activeKeyword.value = q

  try {
    const response = await documentAPI.searchDocuments(q)
    if (response.data?.success) {
      searchResults.value = response.data.data?.list  || []
      searchTotal.value   = response.data.data?.total || 0
    }
  } catch (e) {
    console.error('搜索失败:', e)
    searchResults.value = []
    searchTotal.value   = 0
  } finally {
    searchLoading.value = false
  }
}

// 清除搜索，恢复全部文档列表
const clearSearch = () => {
  searchQuery.value   = ''
  activeKeyword.value = ''
  searchResults.value = []
  searchTotal.value   = 0
}

// 关键词高亮（替换匹配字符为 <mark>）
const highlight = (text) => {
  if (!activeKeyword.value || !text) return text || ''
  const escaped = activeKeyword.value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return text.replace(new RegExp(`(${escaped})`, 'gi'), '<mark>$1</mark>')
}

// ── 标题编辑 ──────────────────────────────────────────────────────────────────
const startTitleEdit = async (doc) => {
  editingTitleId.value    = doc.id
  editingTitleValue.value = doc.title || ''
  await nextTick()
  titleInputRefs.value[doc.id]?.focus()
  titleInputRefs.value[doc.id]?.select()
}

const cancelTitleEdit = () => {
  editingTitleId.value    = null
  editingTitleValue.value = ''
}

const confirmTitleEdit = async (doc) => {
  const newTitle = editingTitleValue.value.trim()
  if (!newTitle || newTitle === doc.title) { cancelTitleEdit(); return }

  const oldTitle = doc.title
  doc.title = newTitle
  cancelTitleEdit()

  try {
    const response = await documentAPI.updateDocumentTitle(doc.id, newTitle)
    if (!response.data?.success) {
      doc.title = oldTitle
      alert('标题更新失败：' + (response.data?.message || '未知错误'))
    }
  } catch (e) {
    doc.title = oldTitle
    alert('标题更新失败，请重试')
  }
}

// 其他操作 
const createNewDocument = () => {
  router.push({ name: 'edite', query: { new: 'true', docId: Date.now().toString() } })
}

const openDocument = (doc) => {
  if (editingTitleId.value === doc.id) return
  router.push({ name: 'edite', query: { docId: doc.id, load: 'true' } })
}

const deleteDocument = async (doc) => {
  if (!confirm(`确定要删除文档"${doc.title || '未命名文档'}"吗？`)) return
  try {
    await documentAPI.deleteDocument(doc.id)
    // 同时从两个列表中移除
    documents.value   = documents.value.filter(d => d.id !== doc.id)
    searchResults.value = searchResults.value.filter(d => d.id !== doc.id)
    searchTotal.value   = Math.max(0, searchTotal.value - 1)
  } catch (e) {
    console.error('删除文档失败:', e)
    alert('删除文档失败，请重试')
  }
}

const getPreview = (content) => {
  if (!content) return '空文档'
  const text = content.replace(/<[^>]*>/g, '')
  return text.length > 80 ? text.substring(0, 80) + '…' : text
}

const formatDate = (dateString) => {
  if (!dateString) return '未知时间'
  const date = new Date(dateString)
  const diff = Date.now() - date.getTime()
  if (diff < 60000)    return '刚刚'
  if (diff < 3600000)  return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)} 天前`
  return date.toLocaleDateString('zh-CN')
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
    JSON.parse(userStr)
    getUserInfo()
    loadDocuments()
  } catch {
    localStorage.removeItem('user')
    router.push('/')
  }
})
</script>

<style scoped>
.home-container { position: relative; min-height: 100vh; background: #f8fafc; }

.dynamic-background {
  position: fixed; inset: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  opacity: 0.05; z-index: -1;
}

.header {
  background: white; border-bottom: 1px solid #e1e5e9;
  box-shadow: 0 2px 4px rgba(0,0,0,0.08);
  position: sticky; top: 0; z-index: 100;
}
.header-content {
  max-width: 1200px; margin: 0 auto; padding: 16px 24px;
  display: flex; justify-content: space-between; align-items: center;
}
.logo { font-size: 22px; font-weight: 700; color: #1e293b; margin: 0; }
.user-info { display: flex; align-items: center; gap: 14px; }
.user-info span { color: #6b7280; font-size: 14px; }
.logout-btn {
  padding: 7px 14px; background: #ef4444; color: white;
  border: none; border-radius: 6px; cursor: pointer; font-size: 13px; transition: background 0.2s;
}
.logout-btn:hover { background: #dc2626; }

.main-content { max-width: 1200px; margin: 0 auto; padding: 32px 24px; }
.content-wrapper { display: flex; flex-direction: column; gap: 32px; }

/* ——新建—— */
.create-section h2 { font-size: 18px; color: #374151; margin-bottom: 14px; }
.create-card {
  background: white; border: 2px dashed #d1d5db; border-radius: 12px;
  padding: 40px; text-align: center; cursor: pointer; transition: all 0.2s; max-width: 260px;
}
.create-card:hover {
  border-color: #3b82f6; background: #f0f9ff;
  transform: translateY(-2px); box-shadow: 0 4px 12px rgba(59,130,246,0.12);
}
.create-icon { font-size: 44px; color: #3b82f6; margin-bottom: 12px; }
.create-card p { color: #6b7280; font-size: 15px; margin: 0; }

/* ——列表头—— */
.documents-section h2 { font-size: 18px; color: #374151; margin: 0; }
.section-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px; flex-wrap: wrap; gap: 12px;
}
.header-right { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }

/* ——搜索框—— */
.search-wrapper {
  position: relative; display: flex; align-items: center;
}
.search-icon {
  position: absolute; left: 10px; font-size: 14px;
  pointer-events: none; z-index: 1;
}
.search-input {
  padding: 7px 32px 7px 32px;
  border: 1px solid #d1d5db; border-radius: 8px;
  font-size: 14px; width: 240px; outline: none;
  background: white; transition: all 0.2s;
}
.search-input:focus {
  border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.12); width: 280px;
}
.search-clear {
  position: absolute; right: 8px;
  background: none; border: none; cursor: pointer;
  font-size: 13px; color: #9ca3af; padding: 2px;
  line-height: 1; transition: color 0.15s;
}
.search-clear:hover { color: #ef4444; }

/* ——搜索状态提示—— */
.search-status {
  margin-bottom: 14px; padding: 8px 14px;
  background: #eff6ff; border: 1px solid #bfdbfe;
  border-radius: 8px; font-size: 14px; color: #1d4ed8;
  display: flex; align-items: center; gap: 10px; flex-wrap: wrap;
}
.search-back {
  background: none; border: none; cursor: pointer;
  color: #3b82f6; font-size: 13px; text-decoration: underline; padding: 0;
}
.search-back:hover { color: #1d4ed8; }

/* ——视图切换—— */
.view-toggle { display: flex; gap: 6px; }
.view-btn {
  padding: 7px 12px; background: white; border: 1px solid #d1d5db;
  border-radius: 6px; cursor: pointer; font-size: 13px; transition: all 0.15s;
}
.view-btn.active { background: #3b82f6; color: white; border-color: #3b82f6; }
.view-btn:hover:not(.active) { background: #f3f4f6; }

/* ——加载/空态—— */
.loading, .empty-state { text-align: center; padding: 56px 24px; color: #6b7280; }
.spinner {
  width: 36px; height: 36px; border: 3px solid #e5e7eb;
  border-top-color: #3b82f6; border-radius: 50%;
  animation: spin 0.8s linear infinite; margin: 0 auto 14px;
}
@keyframes spin { to { transform: rotate(360deg); } }
.empty-icon { font-size: 56px; margin-bottom: 14px; }

/* ——文档容器—— */
.documents-container.grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(290px, 1fr)); gap: 18px;
}
.documents-container.list { display: flex; flex-direction: column; gap: 10px; }

/* ——文档卡片—— */
.document-card {
  background: white; border-radius: 12px; padding: 18px;
  border: 1px solid #e1e5e9; cursor: pointer; transition: all 0.2s; position: relative;
}
.document-card:hover {
  transform: translateY(-2px); box-shadow: 0 6px 18px rgba(0,0,0,0.09); border-color: #93c5fd;
}
.documents-container.list .document-card { display: flex; align-items: flex-start; gap: 14px; }
.document-icon { font-size: 28px; margin-bottom: 10px; flex-shrink: 0; }
.documents-container.list .document-icon { margin-bottom: 0; margin-top: 2px; }
.document-info { flex: 1; min-width: 0; }

/* ——标题行—— */
.title-wrapper {
  display: flex; align-items: center; gap: 6px; margin-bottom: 6px; min-height: 26px;
}
.document-title {
  font-size: 15px; font-weight: 600; color: #1e293b; margin: 0;
  line-height: 1.4; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; flex: 1;
}
.title-edit-btn {
  flex-shrink: 0; padding: 1px 5px; background: transparent;
  border: 1px solid transparent; border-radius: 4px; cursor: pointer;
  font-size: 12px; opacity: 0; transition: opacity 0.15s, background 0.15s; line-height: 1;
}
.document-card:hover .title-edit-btn { opacity: 1; }
.title-edit-btn:hover { background: #f1f5f9; border-color: #cbd5e1; }
.title-edit-input {
  flex: 1; font-size: 15px; font-weight: 600; color: #1e293b;
  border: 1px solid #3b82f6; border-radius: 5px; padding: 2px 7px; outline: none;
  background: #f8faff; box-shadow: 0 0 0 3px rgba(59,130,246,0.12); width: 100%;
}

/*  ——关键词高亮 mark 样式—— */
.document-title :deep(mark),
.document-preview :deep(mark) {
  background: #fef08a; color: #1e293b;
  border-radius: 2px; padding: 0 1px;
}

.document-preview {
  color: #6b7280; font-size: 13px; line-height: 1.5; margin: 0 0 10px;
  display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2;
  -webkit-box-orient: vertical; overflow: hidden;
}
.documents-container.list .document-preview { margin-bottom: 0; }
.document-meta { display: flex; justify-content: space-between; font-size: 11px; color: #9ca3af; }
.documents-container.list .document-meta { flex-direction: column; gap: 2px; }

.document-actions {
  position: absolute; top: 14px; right: 14px; opacity: 0; transition: opacity 0.15s;
}
.document-card:hover .document-actions { opacity: 1; }
.documents-container.list .document-actions { position: static; opacity: 1; }
.delete-btn {
  padding: 5px 10px; background: #ef4444; color: white;
  border: none; border-radius: 4px; cursor: pointer; font-size: 12px; transition: background 0.15s;
}
.delete-btn:hover { background: #dc2626; }

@media (max-width: 768px) {
  .header-content { padding: 12px 16px; }
  .main-content { padding: 20px 16px; }
  .content-wrapper { gap: 22px; }
  .create-card { padding: 28px; max-width: 100%; }
  .section-header { flex-direction: column; align-items: flex-start; }
  .header-right { width: 100%; }
  .search-input { width: 100%; }
  .search-input:focus { width: 100%; }
  .documents-container.grid { grid-template-columns: 1fr; }
  .documents-container.list .document-card { flex-direction: column; }
}
</style>
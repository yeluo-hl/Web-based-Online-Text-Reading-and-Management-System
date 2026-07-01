<template>
  <div class="rich-editor">
    <div v-if="isLoading" class="loading-overlay">
      <div class="spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 编辑器初始化错误提示 -->
    <div v-if="editorError" class="editor-error-banner">
      ⚠️ {{ editorError }}
    </div>

    <!-- ══ 工具栏 ══ -->
    <div class="toolbar" v-if="editor && !localReadonly">
      <div class="toolbar-row">

        <!-- 字体 -->
        <select class="tb-select" @change="e => editor.chain().focus().setFontFamily(e.target.value).run()" title="字体">
          <option value="">默认字体</option>
          <option value="Arial">Arial</option>
          <option value="Georgia">Georgia</option>
          <option value="Courier New">Courier New</option>
          <option value="SimSun">宋体</option>
          <option value="Microsoft YaHei">微软雅黑</option>
          <option value="KaiTi">楷体</option>
        </select>

        <!-- 字号 -->
        <select class="tb-select" @change="e => setFontSize(e.target.value)" title="字号">
          <option value="">字号</option>
          <option v-for="s in fontSizes" :key="s" :value="s">{{ s }}</option>
        </select>

        <span class="divider"></span>

        <!-- 格式 -->
        <button @click="editor.chain().focus().toggleBold().run()" :class="{ active: editor.isActive('bold') }" title="加粗 Ctrl+B"><b>B</b></button>
        <button @click="editor.chain().focus().toggleItalic().run()" :class="{ active: editor.isActive('italic') }" title="斜体 Ctrl+I"><i>I</i></button>
        <button @click="editor.chain().focus().toggleUnderline().run()" :class="{ active: editor.isActive('underline') }" title="下划线 Ctrl+U" style="text-decoration:underline">U</button>
        <button @click="editor.chain().focus().toggleStrike().run()" :class="{ active: editor.isActive('strike') }" title="删除线" style="text-decoration:line-through">S</button>
        <button @click="editor.chain().focus().toggleSubscript().run()" :class="{ active: editor.isActive('subscript') }" title="下标">X<sub>2</sub></button>
        <button @click="editor.chain().focus().toggleSuperscript().run()" :class="{ active: editor.isActive('superscript') }" title="上标">X<sup>2</sup></button>

        <span class="divider"></span>

        <!-- 颜色 -->
        <label class="color-btn" title="文字颜色">
          <span class="color-icon" :style="{ borderBottom: '3px solid ' + currentColor }">A</span>
          <input type="color" v-model="currentColor" @input="e => editor.chain().focus().setColor(e.target.value).run()" />
        </label>
        <label class="color-btn" title="背景高亮">
          <span class="color-icon" :style="{ background: currentHighlight }">🖊</span>
          <input type="color" v-model="currentHighlight" @input="e => editor.chain().focus().setHighlight({ color: e.target.value }).run()" />
        </label>
        <button @click="editor.chain().focus().unsetAllMarks().run()" title="清除格式">✕格式</button>

        <span class="divider"></span>

        <!-- 标题 -->
        <button @click="editor.chain().focus().toggleHeading({ level: 1 }).run()" :class="{ active: editor.isActive('heading', { level: 1 }) }">H1</button>
        <button @click="editor.chain().focus().toggleHeading({ level: 2 }).run()" :class="{ active: editor.isActive('heading', { level: 2 }) }">H2</button>
        <button @click="editor.chain().focus().toggleHeading({ level: 3 }).run()" :class="{ active: editor.isActive('heading', { level: 3 }) }">H3</button>
        <button @click="editor.chain().focus().setParagraph().run()" :class="{ active: editor.isActive('paragraph') }" title="正文">¶</button>

        <span class="divider"></span>

        <!-- 对齐 -->
        <button @click="editor.chain().focus().setTextAlign('left').run()" :class="{ active: editor.isActive({ textAlign: 'left' }) }" title="左对齐">⬅</button>
        <button @click="editor.chain().focus().setTextAlign('center').run()" :class="{ active: editor.isActive({ textAlign: 'center' }) }" title="居中">≡</button>
        <button @click="editor.chain().focus().setTextAlign('right').run()" :class="{ active: editor.isActive({ textAlign: 'right' }) }" title="右对齐">➡</button>
        <button @click="editor.chain().focus().setTextAlign('justify').run()" :class="{ active: editor.isActive({ textAlign: 'justify' }) }" title="两端对齐">☰</button>

        <span class="divider"></span>

        <!-- 列表 / 引用 / 代码 -->
        <button @click="editor.chain().focus().toggleBulletList().run()" :class="{ active: editor.isActive('bulletList') }" title="无序列表">• 列表</button>
        <button @click="editor.chain().focus().toggleOrderedList().run()" :class="{ active: editor.isActive('orderedList') }" title="有序列表">1. 列表</button>
        <button @click="editor.chain().focus().toggleBlockquote().run()" :class="{ active: editor.isActive('blockquote') }" title="引用块">" 引用</button>
        <button @click="editor.chain().focus().toggleCodeBlock().run()" :class="{ active: editor.isActive('codeBlock') }" title="代码块">&lt;/&gt; 代码</button>

        <span class="divider"></span>

        <!-- 表格 -->
        <button @click="insertTable" title="插入表格">⊞ 表格</button>
        <template v-if="editor.isActive('table')">
          <button @click="editor.chain().focus().addColumnAfter().run()" title="插入列">+列</button>
          <button @click="editor.chain().focus().addRowAfter().run()" title="插入行">+行</button>
          <button @click="editor.chain().focus().deleteColumn().run()" title="删除列">-列</button>
          <button @click="editor.chain().focus().deleteRow().run()" title="删除行">-行</button>
          <button @click="editor.chain().focus().deleteTable().run()" title="删除表格">删表</button>
        </template>

        <span class="divider"></span>

        <!-- 撤销/重做 -->
        <button @click="editor.chain().focus().undo().run()" :disabled="!editor.can().undo()" title="撤销 Ctrl+Z">↩</button>
        <button @click="editor.chain().focus().redo().run()" :disabled="!editor.can().redo()" title="重做 Ctrl+Y">↪</button>

        <span class="divider"></span>

        <!-- 图片 -->
        <button @click="addImage" title="插入图片">🖼 图片</button>

        <span class="divider"></span>

        <!-- 查找替换 -->
        <button @click="showFindReplace = !showFindReplace" :class="{ active: showFindReplace }" title="查找替换 Ctrl+H">🔍 查找</button>

        <span class="divider"></span>

        <!-- 文件 -->
        <button @click="triggerFileInput" title="导入文件">📂 导入</button>
        <input ref="fileInputRef" type="file" style="display:none"
          accept=".docx,.txt,.md,.html"
          @change="importFile" />
        <button @click="exportAsWord" title="导出 Word">📄 导出</button>

        <span class="divider"></span>

        <button @click="saveDocument()" class="save-btn">💾 保存</button>
        <button @click="goBack" class="back-btn">← 返回</button>
      </div>

      <!-- 查找替换面板 -->
      <div v-if="showFindReplace" class="find-replace-bar">
        <div class="fr-group">
          <label>查找</label>
          <input v-model="findText" placeholder="输入关键词..." @keydown.enter="findNext" @input="doSearch" />
          <span class="fr-count" v-if="findText">{{ currentMatch }}/{{ totalMatches }}</span>
        </div>
        <div class="fr-group">
          <label>替换</label>
          <input v-model="replaceText" placeholder="替换为..." @keydown.enter="replaceOne" />
        </div>
        <div class="fr-options">
          <label><input type="checkbox" v-model="caseSensitive" @change="doSearch" /> 区分大小写</label>
        </div>
        <div class="fr-actions">
          <button @click="findPrev">上一个</button>
          <button @click="findNext">下一个</button>
          <button @click="replaceOne">替换</button>
          <button @click="replaceAll">全部替换</button>
          <button @click="closeFindReplace" class="fr-close">✕</button>
        </div>
      </div>
    </div>

    <!-- 协同在线栏 -->
    <transition name="collab-slide">
      <div class="collab-bar" v-if="collaborators.length > 0 || remoteEditNotice">
        <div class="collab-left">
          <span class="collab-label">👥 协作中</span>
          <div class="collab-avatars">
            <span
              v-for="user in collaborators"
              :key="user.userId"
              class="collab-avatar"
              :style="{ background: user.color }"
              :title="user.userName + ' 正在协作'"
            >{{ (user.userName || user.userId).charAt(0).toUpperCase() }}</span>
          </div>
          <span class="collab-count" v-if="collaborators.length > 0">
            {{ collaborators.length }} 人在线
          </span>
        </div>
        <transition name="fade">
          <span v-if="remoteEditNotice" class="remote-toast">
            ✏️ {{ remoteEditNotice }}
          </span>
        </transition>
      </div>
    </transition>

    <!-- 标题输入区 -->
    <div class="title-bar">
      <input
        v-model="documentTitle"
        class="title-input"
        placeholder="请输入文档标题..."
        maxlength="100"
        @blur="handleTitleBlur"
        @keydown.enter.prevent="handleTitleEnter"
        :readonly="localReadonly"
      />
      <span class="title-hint" v-if="!localReadonly">按 Enter 或点击编辑区确认</span>
    </div>

    <!-- 编辑器内容 -->
    <EditorContent :editor="editor" class="editor-content" />

    <!-- 底部状态栏 -->
    <div class="info">
      <div class="stats">
        <span>字数：{{ wordCount }}</span>
        <span>字符数：{{ charCount }}</span>
        <span v-if="saveStatus" :class="['save-status', saveStatus]">
          {{ saveStatus === 'saving' ? '保存中...' : saveStatus === 'saved' ? '✓ 已保存' : '保存失败' }}
        </span>
      </div>
      <button @click="toggleMode" class="mode-toggle">
        {{ localReadonly ? '✏️ 切换编辑' : '👁 切换阅读' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, shallowRef, watch, computed, nextTick, onBeforeUnmount, onMounted } from 'vue'
import { EditorContent, Editor } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import { Underline } from '@tiptap/extension-underline'
import { TextStyle } from '@tiptap/extension-text-style'
import { Color } from '@tiptap/extension-color'
import { FontFamily } from '@tiptap/extension-font-family'
import { Highlight } from '@tiptap/extension-highlight'
import { Subscript } from '@tiptap/extension-subscript'
import { Superscript } from '@tiptap/extension-superscript'
import { TextAlign } from '@tiptap/extension-text-align'
import { Table } from '@tiptap/extension-table'
import { TableRow } from '@tiptap/extension-table-row'
import { TableHeader } from '@tiptap/extension-table-header'
import { TableCell } from '@tiptap/extension-table-cell'
import { Image } from '@tiptap/extension-image'
import { Extension } from '@tiptap/core'
import mammoth from 'mammoth'
import { marked } from 'marked'
import { useRouter, useRoute } from 'vue-router'
import {
  Document, Packer, Paragraph, TextRun, ImageRun,
  HeadingLevel, AlignmentType, ShadingType, WidthType,
  Table as DocxTable, TableRow as DocxTableRow, TableCell as DocxTableCell,
} from 'docx'
import { saveAs } from 'file-saver'
import { documentAPI } from '@/utils/api'

import * as Y from 'yjs'
import { WebsocketProvider } from 'y-websocket'
import { Collaboration } from '@tiptap/extension-collaboration'
// CollaborationCursor 用自定义 DOM 渲染替代，彻底避免 awareness 时序崩溃


// ── FontSize 自定义扩展 ────────────────────────────────────────────────────────
const FontSize = Extension.create({
  name: 'fontSize',
  addOptions() { return { types: ['textStyle'] } },
  addGlobalAttributes() {
    return [{
      types: this.options.types,
      attributes: {
        fontSize: {
          default: null,
          parseHTML: el => el.style.fontSize?.replace('px', '') || null,
          renderHTML: attrs => {
            if (!attrs.fontSize) return {}
            return { style: `font-size: ${attrs.fontSize}px` }
          }
        }
      }
    }]
  },
  addCommands() {
    return {
      setFontSize:   size => ({ chain }) => chain().setMark('textStyle', { fontSize: size }).run(),
      unsetFontSize: ()   => ({ chain }) => chain().setMark('textStyle', { fontSize: null }).removeEmptyTextStyle().run(),
    }
  }
})

// ── ResizableImage 扩展 ────────────────────────────────────────────────────────
const ResizableImage = Image.extend({
  renderHTML({ HTMLAttributes }) {
    const { width, height, ...rest } = HTMLAttributes
    const w = width  != null ? parseFloat(width)  : null
    const h = height != null ? parseFloat(height) : null
    const attrs = { ...rest }
    if (w || h) {
      attrs.style = (w ? `width:${w}px;` : '') +
                    (h ? `height:${h}px;` : '') +
                    'max-width:none;display:block;border-radius:4px'
      // data-* 에도 저장해서 parseHTML 복원 이중 보장
      if (w) attrs['data-width']  = String(w)
      if (h) attrs['data-height'] = String(h)
    }
    return ['img', attrs]
  },

  addAttributes() {
    return {
      ...this.parent?.(),
      width: {
        default: null,
        parseHTML: el => {
          const v = el.getAttribute('data-width') || el.style.width || el.getAttribute('width')
          return v ? parseFloat(v) : null
        },
        renderHTML: () => ({}),
      },
      height: {
        default: null,
        parseHTML: el => {
          const v = el.getAttribute('data-height') || el.style.height || el.getAttribute('height')
          return v ? parseFloat(v) : null
        },
        renderHTML: () => ({}),
      },
    }
  },

  addNodeView() {
    return ({ node, updateAttributes, editor }) => {
      const wrapper = document.createElement('span')
      wrapper.style.cssText = 'position:relative;display:inline-block;line-height:0;vertical-align:bottom'

      const img = document.createElement('img')
      img.src       = node.attrs.src || ''
      img.alt       = node.attrs.alt || ''
      img.draggable = false

      const applyStyle = (w, h) => {
        img.style.display      = 'block'
        img.style.borderRadius = '4px'
        img.style.maxWidth     = 'none'
        if (w) img.style.width  = w + 'px'
        if (h) img.style.height = h + 'px'
      }

      const initW = node.attrs.width  != null ? parseFloat(node.attrs.width)  : null
      const initH = node.attrs.height != null ? parseFloat(node.attrs.height) : null

      if (initW || initH) {
        applyStyle(initW, initH)
      } else {
        img.style.maxWidth = '100%'
        img.style.display  = 'block'
        img.onload = () => {
          const w = img.naturalWidth, h = img.naturalHeight
          applyStyle(w, h)
          setTimeout(() => { try { updateAttributes({ width: w, height: h }) } catch(e) {} }, 0)
        }
      }

      wrapper.appendChild(img)

      const overlay = document.createElement('span')
      overlay.style.cssText = [
        'position:absolute;inset:0',
        'border:2px solid transparent',
        'border-radius:4px',
        'pointer-events:none',
        'box-sizing:border-box',
      ].join(';')
      wrapper.appendChild(overlay)

      const HANDLES = [
        ['nw-resize',  0,   0,   -1, -1],
        ['n-resize',   50,  0,    0, -1],
        ['ne-resize',  100, 0,    1, -1],
        ['e-resize',   100, 50,   1,  0],
        ['se-resize',  100, 100,  1,  1],
        ['s-resize',   50,  100,  0,  1],
        ['sw-resize',  0,   100, -1,  1],
        ['w-resize',   0,   50,  -1,  0],
      ]

      const handleEls = HANDLES.map(([cursor, xPct, yPct, dw, dh]) => {
        const hEl = document.createElement('span')
        hEl.style.cssText = [
          'position:absolute',
          `left:${xPct}%`, `top:${yPct}%`,
          'transform:translate(-50%,-50%)',
          'width:10px;height:10px',
          'background:#3b82f6;border:2px solid #fff',
          'border-radius:2px',
          `cursor:${cursor}`,
          'opacity:0;transition:opacity .12s',
          'z-index:30;box-sizing:border-box',
        ].join(';')

        hEl.addEventListener('mousedown', e => {
          e.preventDefault(); e.stopPropagation()
          const startX = e.clientX, startY = e.clientY
          const startW = img.offsetWidth  || 200
          const startH = img.offsetHeight || 150
          document.body.style.userSelect = 'none'
          document.body.style.cursor     = cursor

          const onMove = ev => {
            const newW = dw !== 0 ? Math.max(40, startW + (ev.clientX - startX) * dw) : startW
            const newH = dh !== 0 ? Math.max(30, startH + (ev.clientY - startY) * dh) : startH
            img.style.width    = Math.round(newW) + 'px'
            img.style.height   = Math.round(newH) + 'px'
            img.style.maxWidth = 'none'
          }

          const onUp = () => {
            const w = Math.round(img.offsetWidth)
            const h = Math.round(img.offsetHeight)
            document.body.style.userSelect = ''
            document.body.style.cursor     = ''
            document.removeEventListener('mousemove', onMove)
            document.removeEventListener('mouseup',   onUp)

            try {
              updateAttributes({ width: w, height: h })
              // Collaboration 환경에서도 onUpdate 를 확실히 발생시킴
              editor.commands.command(({ tr, dispatch }) => {
                if (dispatch) dispatch(tr.setMeta('imageResize', true))
                return true
              })
            } catch(e) { console.error('resize save error:', e) }
          }

          document.addEventListener('mousemove', onMove)
          document.addEventListener('mouseup',   onUp)
        })

        wrapper.appendChild(hEl)
        return hEl
      })

      const show = () => {
        overlay.style.borderColor = '#3b82f6'
        handleEls.forEach(h => { h.style.opacity = '1' })
      }
      const hide = () => {
        overlay.style.borderColor = 'transparent'
        handleEls.forEach(h => { h.style.opacity = '0' })
      }
      wrapper.addEventListener('mouseenter', show)
      wrapper.addEventListener('mouseleave', hide)

      return {
        dom: wrapper,
        update(updatedNode) {
          if (updatedNode.type !== node.type) return false
          img.src = updatedNode.attrs.src || ''
          const w = updatedNode.attrs.width  != null ? parseFloat(updatedNode.attrs.width)  : null
          const h = updatedNode.attrs.height != null ? parseFloat(updatedNode.attrs.height) : null
          if (w || h) applyStyle(w, h)
          return true
        },
        destroy() {}
      }
    }
  }
})

// ─────────────────────────────────────────────────────────────────────────────

const router = useRouter()
const route  = useRoute()

const props = defineProps({
  modelValue: { type: String, default: '' },
  readonly:   { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue'])

const fileInputRef    = ref(null)
const localReadonly   = ref(props.readonly)
const content         = ref(props.modelValue || '<p>开始编辑...</p>')
const currentDocId    = ref(null)
const isLoading       = ref(false)
const saveStatus      = ref('')
const documentTitle   = ref('')

const currentColor     = ref('#000000')
const currentHighlight = ref('#ffff00')
const fontSizes = [10, 12, 14, 16, 18, 20, 24, 28, 32, 36, 48, 64]
const editorError      = ref('')   // ← 新增：捕获编辑器初始化错误

function setFontSize(size) {
  if (!size) { editor.value?.chain().focus().unsetFontSize().run(); return }
  editor.value?.chain().focus().setFontSize(size).run()
}

const showFindReplace = ref(false)
const findText        = ref('')
const replaceText     = ref('')
const caseSensitive   = ref(false)
const currentMatch    = ref(0)
const totalMatches    = ref(0)
let   matchPositions  = []

// 协同编辑状态（Y.js + WebSocket）
const collaborators    = ref([])
const remoteEditNotice = ref('')
// ydoc 提前创建并绑定给 editor，避免协同激活时销毁重建 editor
let   ydoc             = new Y.Doc()
// Y.js 共享文本，用于同步标题
const yTitle           = ydoc.getText('title')
let   provider         = null
let   remoteToastTimer = null
// 防止标题同步时触发循环写入
let   titleSyncLock    = false

// 根据 userId 生成稳定的标识色 
function generateUserColor(userId) {
  const palette = ['#3b82f6','#10b981','#f59e0b','#ef4444','#8b5cf6','#06b6d4','#ec4899','#f97316']
  let hash = 0
  for (let i = 0; i < (userId || '').length; i++) hash = userId.charCodeAt(i) + ((hash << 5) - hash)
  return palette[Math.abs(hash) % palette.length]
}

// 读取在线用户列表（来自 Y.js Awareness）
function updateCollaborators() {
  if (!provider?.awareness) return
  const states = Array.from(provider.awareness.getStates().entries())
  const myClientId = provider.awareness.clientID
  collaborators.value = states
    .filter(([clientId, state]) => state?.user && clientId !== myClientId)
    .map(([, state]) => ({
      userId:   state.user.userId,
      userName: state.user.name,
      color:    state.user.color,
    }))
}

/**
 * 初始化协同连接
 * - 只管理 provider，不销毁/重建 editor
 * - 标题通过 Y.js yTitle 文本同步
 * - 协同光标用 awareness + DOM 自定义渲染（彻底绕开 CollaborationCursor 时序问题）
 */
async function initCollaboration(docId) {
  if (!docId) return
  if (provider) { provider.destroy(); provider = null }

  const userStr = localStorage.getItem('user')
  if (!userStr) return
  const user    = JSON.parse(userStr)
  const myColor = generateUserColor(user.userId)

  const wsUrl = `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.hostname}:1234`
  provider = new WebsocketProvider(wsUrl, docId, ydoc)
  provider.on('status', e => console.log('WebSocket 协同状态:', e.status))

  // Awareness：设置当前用户信息
  provider.awareness.setLocalStateField('user', {
    userId: user.userId,
    name:   user.name || user.userId,
    color:  myColor,
  })

  // 在线用户列表 + 远端编辑提示 
  provider.awareness.on('change', () => {
    updateCollaborators()
    const states  = Array.from(provider.awareness.getStates().entries())
    const myId    = provider.awareness.clientID
    const others  = states.filter(([id]) => id !== myId)
    if (others.length > 0) {
      const last = others[others.length - 1][1]?.user
      if (last) showRemoteToast(last.name || last.userId)
    }
    renderRemoteCursors()
  })

  // 标题同步
  // 本地标题输入 → 写入 yTitle
  watch(documentTitle, (newVal) => {
    if (titleSyncLock) return
    const cur = yTitle.toString()
    if (cur !== newVal) {
      ydoc.transact(() => {
        yTitle.delete(0, yTitle.length)
        yTitle.insert(0, newVal)
      })
    }
  })
  // 远端 yTitle 变更 → 更新本地标题
  yTitle.observe(() => {
    const remote = yTitle.toString()
    if (remote !== documentTitle.value) {
      titleSyncLock = true
      documentTitle.value = remote
      nextTick(() => { titleSyncLock = false })
    }
  })

  // 自动保存（防抖 3s）
  ydoc.on('update', () => {
    if (autoSaveTimer) clearTimeout(autoSaveTimer)
    autoSaveTimer = setTimeout(() => {
      if (currentDocId.value) saveDocument(true)
    }, 3000)
  })

  // 5. 首次同步：始终以数据库内容为准
  provider.once('sync', (isSynced) => {
    if (!editor.value || !isSynced) return
    if (content.value && content.value !== '<p></p>') {
      editor.value.commands.setContent(content.value, false)
    }
    // 同步标题（服务端有内容时以远端为准）
    const remoteTitle = yTitle.toString()
    if (remoteTitle && remoteTitle !== documentTitle.value) {
      titleSyncLock = true
      documentTitle.value = remoteTitle
      nextTick(() => { titleSyncLock = false })
    } else if (!remoteTitle && documentTitle.value) {
      ydoc.transact(() => {
        yTitle.delete(0, yTitle.length)
        yTitle.insert(0, documentTitle.value)
      })
    }
  })
}

function disconnectCollaboration() {
  if (provider) {
    // 离开前清空光标，避免其他用户看到残留光标
    try { provider.awareness.setLocalStateField('cursor', null) } catch {}
    provider.destroy()
    provider = null
  }
  collaborators.value = []
  // 清除所有远端光标 DOM
  document.querySelectorAll('.yjs-cursor').forEach(el => el.remove())
}

function showRemoteToast(name) {
  remoteEditNotice.value = `${name} 正在协作编辑`
  if (remoteToastTimer) clearTimeout(remoteToastTimer)
  remoteToastTimer = setTimeout(() => { remoteEditNotice.value = '' }, 3000)
}

/**
 * 把其他用户的光标位置渲染到编辑器 DOM 中
 * 使用 awareness 里的 anchor（ProseMirror 文档坐标）映射到屏幕坐标
 */
function renderRemoteCursors() {
  if (!editor.value || !provider?.awareness) return
  // 清除旧光标
  document.querySelectorAll('.yjs-cursor').forEach(el => el.remove())

  const editorDom = editor.value.view.dom
  const container = editorDom.closest('.editor-content') || editorDom.parentElement
  if (!container) return

  const myId = provider.awareness.clientID
  provider.awareness.getStates().forEach((state, clientId) => {
    if (clientId === myId || !state?.user || !state?.cursor) return
    const { anchor } = state.cursor
    const { name, color } = state.user
    if (anchor == null) return

    try {
      const pos   = editor.value.view.state.doc.resolve(
        Math.min(anchor, editor.value.view.state.doc.content.size)
      )
      const coords = editor.value.view.coordsAtPos(pos.pos)
      const boxRect = container.getBoundingClientRect()

      const caret = document.createElement('span')
      caret.className = 'yjs-cursor'
      caret.style.cssText = [
        'position:absolute',
        `left:${coords.left - boxRect.left}px`,
        `top:${coords.top  - boxRect.top  + container.scrollTop}px`,
        `height:${coords.bottom - coords.top}px`,
        `border-left:2px solid ${color}`,
        'pointer-events:none',
        'z-index:50',
      ].join(';')

      const label = document.createElement('span')
      label.textContent = name
      label.style.cssText = [
        'position:absolute',
        'top:-1.4em', 'left:-1px',
        `background:${color}`,
        'color:#fff',
        'font-size:11px', 'font-weight:600',
        'padding:1px 6px',
        'border-radius:3px 3px 3px 0',
        'white-space:nowrap',
        'pointer-events:none',
      ].join(';')

      caret.appendChild(label)
      container.style.position = 'relative'
      container.appendChild(caret)
    } catch { /* 位置超出文档范围，忽略 */ }
  })
}

// Editor：只创建一次，Collaboration 从一开始就绑定 ydoc 
const editor = shallowRef(null)
try {
  editor.value = new Editor({
    content: '',
    editable: !localReadonly.value,
    extensions: [
      // Collaboration 必须在 StarterKit 前，同时禁用其 history
      Collaboration.configure({ document: ydoc }),
      StarterKit.configure({
        heading:  { levels: [1, 2, 3, 4, 5, 6] },
        underline: false,
        history:  false,
        undoRedo: false,
      }),
      Underline, TextStyle, Color, FontFamily, FontSize,
      Highlight.configure({ multicolor: true }),
      Subscript, Superscript,
      TextAlign.configure({ types: ['heading', 'paragraph'] }),
      Table.configure({ resizable: true }),
      TableRow, TableHeader, TableCell,
      ResizableImage.configure({ inline: false, allowBase64: true }),
    ],
    onUpdate: ({ editor: ed }) => {
      const html = ed.getHTML()
      content.value = html
      emit('update:modelValue', html)
      autoSave()
      if (findText.value) doSearch()
      // 内容变化时同步光标位置到 awareness
      if (provider?.awareness) {
        const { from, to } = ed.state.selection
        provider.awareness.setLocalStateField('cursor', { anchor: from, head: to })
      }
      renderRemoteCursors()
    },

    // 光标移动（未修改内容）时也同步到 awareness
    onSelectionUpdate: ({ editor: ed }) => {
      if (!provider?.awareness) return
      const { from, to } = ed.state.selection
      provider.awareness.setLocalStateField('cursor', { anchor: from, head: to })
      renderRemoteCursors()
    },

    onTransaction: ({ transaction }) => {
      if (transaction.getMeta('imageResize')) {
        const html = editor.value?.getHTML()
        if (html) {
          content.value = html
          if (autoSaveTimer) clearTimeout(autoSaveTimer)
          autoSaveTimer = setTimeout(() => {
            if (currentDocId.value) saveDocument(true)
          }, 500)
        }
      }
    }
  })
} catch (e) {
  editorError.value = `编辑器初始化失败：${e.message}`
  console.error('Editor 初始化失败:', e)
}

watch(() => props.modelValue, v => {
  if (v !== content.value && editor.value) {
    editor.value.commands.setContent(v || '<p>开始编辑...</p>')
    content.value = v
  }
})
watch(() => props.readonly, v => { localReadonly.value = v })
watch(localReadonly, v => { editor.value?.setEditable(!v) })

// onMounted 改为 async，确保 loadDocument 完成后再 initCollaboration
onMounted(async () => {
  if (!localStorage.getItem('user')) { router.push('/'); return }
  const { docId, new: isNew, load } = route.query

  if (isNew === 'true') {
    currentDocId.value = null
    documentTitle.value = ''
    const initContent = '<p>开始编辑...</p>'
    content.value = initContent
    editor.value?.commands.setContent(initContent, false)
    // 新文档在首次保存后（saveDocument 返回 id）再 initCollaboration
  } else if (load === 'true' && docId) {
    await loadDocument(docId)
    await initCollaboration(docId)      // 等待协同编辑器完全初始化
  }

  document.addEventListener('keydown', handleGlobalKey)
})

function handleGlobalKey(e) {
  if ((e.ctrlKey || e.metaKey) && e.key === 'h') {
    e.preventDefault()
    showFindReplace.value = !showFindReplace.value
    if (showFindReplace.value) setTimeout(() => document.querySelector('.fr-group input')?.focus(), 50)
  }
}

const wordCount = computed(() => editor.value?.getText().replace(/\s+/g, '').length ?? 0)
const charCount = computed(() => editor.value?.getText().length ?? 0)

function toggleMode() { localReadonly.value = !localReadonly.value }

async function handleTitleBlur() {
  if (currentDocId.value && documentTitle.value.trim()) await saveDocumentTitle()
}
function handleTitleEnter() { editor.value?.commands.focus() }
async function saveDocumentTitle() {
  if (!currentDocId.value || !documentTitle.value.trim()) return
  try { await documentAPI.updateDocumentTitle(currentDocId.value, documentTitle.value.trim()) }
  catch (e) { console.error('标题保存失败:', e) }
}

let autoSaveTimer = null
function autoSave() {
  // 单机模式下的自动保存（协同模式由 ydoc.on('update') 触发）
  if (provider) return   // 协同模式：交给 ydoc 监听器处理
  if (autoSaveTimer) clearTimeout(autoSaveTimer)
  autoSaveTimer = setTimeout(() => {
    // 用 editor 是否存在作判断，saveDocument 内部会直接读 getHTML()
    if (editor.value) saveDocument(true)
  }, 2000)
}

async function saveDocument(silent = false) {
  if (!silent) saveStatus.value = 'saving'
  // 每次保存都从编辑器直接读取最新 HTML，
  // 避免 content.value 在图片 resize / Collaboration 同步时落后于实际 ydoc 状态。
  const currentContent = editor.value?.getHTML() ?? content.value
  try {
    const response = await documentAPI.saveDocument({
      id:        currentDocId.value || undefined,
      title:     documentTitle.value.trim() || '未命名文档',
      content:   currentContent,
      wordCount: wordCount.value
    })
    if (response.data?.success) {
      const newId = response.data.data?.id
      // 新文档首次保存后获得 docId，启动 Y.js 协同
      if (!currentDocId.value && newId) {
        currentDocId.value = newId
        await initCollaboration(newId)
      }
      if (!silent) { saveStatus.value = 'saved'; setTimeout(() => { saveStatus.value = '' }, 2000) }
    } else {
      saveStatus.value = 'error'
      if (!silent) alert(response.data?.message || '保存失败')
    }
  } catch (error) {
    saveStatus.value = 'error'
    if (!silent) alert('保存失败，请重试')
  }
}

async function loadDocument(docId, silent = false) {
  try {
    if (!silent) isLoading.value = true
    const response = await documentAPI.getDocument(docId)
    if (response.data?.success) {
      const doc = response.data.data
      currentDocId.value = doc.id
      documentTitle.value = doc.title || ''
      // 从数据库拉取时也需要保护期，防止触发 onUpdate 回发
      const newContent = doc.content || '<p>开始编辑...</p>'
      // 只缓存到 content.value，不写入 ydoc。
      // 后续 initCollaboration → sync 回调会判断服务端 ydoc 是否为空，
      // 为空时才调用 setContent，确保 ydoc 里只有一份内容。
      content.value = newContent
    } else {
      if (!silent) alert('加载文档失败：' + (response.data?.message || '未知错误'))
    }
  } catch (error) {
    console.error('加载文档失败:', error)
    if (!silent) alert('加载文档失败，请重试')
  } finally {
    if (!silent) isLoading.value = false
  }
}

function goBack() {
  window.location.href = '/home'
}

function insertTable() {
  editor.value?.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run()
}

function addImage() {
  const input = document.createElement('input')
  input.type = 'file'; input.accept = 'image/*'
  input.onchange = () => {
    const file = input.files[0]
    const reader = new FileReader()
    reader.onload = () => editor.value.chain().focus().setImage({ src: reader.result }).run()
    reader.readAsDataURL(file)
  }
  input.click()
}

function triggerFileInput() { fileInputRef.value.click() }

async function importFile(e) {
  const file = e.target.files[0]
  if (!file) return
  const ext   = file.name.split('.').pop().toLowerCase()
  const title = file.name.replace(/\.[^.]+$/, '')

  try {
    if (ext === 'txt' || ext === 'md') {
      editor.value.commands.setContent(`<pre>${await file.text()}</pre>`)
      documentTitle.value = title
    } else if (ext === 'html') {
      editor.value.commands.setContent(await file.text())
      documentTitle.value = title
    } else if (ext === 'docx') {
      const result = await mammoth.convertToHtml({ arrayBuffer: await file.arrayBuffer() })
      if (result.messages?.length) console.warn('导入警告:', result.messages)
      if (!result.value || result.value.trim() === '') {
        alert('该文档内容为空，或文件已损坏无法解析。\n请确认文件未加密、未损坏后重试。')
        return
      }
      editor.value.commands.setContent(result.value)
      documentTitle.value = title
    } else if (ext === 'doc') {
      alert('不支持旧版 .doc 格式（Word 97-2003）。\n\n解决方法：\n在 Word 中打开该文件 → 另存为 → 选择 .docx 格式后重新导入。')
      return
    } else {
      alert(`不支持的文件格式：.${ext}\n\n当前支持导入的格式：.docx  .txt  .md  .html`)
      return
    }
  } catch (err) {
    console.error('导入失败:', err)
    alert('导入失败，文件可能已损坏或受密码保护。\n请检查文件后重试。')
  } finally {
    e.target.value = ''
  }
}

// 导出 Word：保留格式 + 图片
async function exportAsWord() {
  const children = await parseHtmlToDocxNodes(editor.value.getHTML())
  const doc = new Document({ sections: [{ children }] })
  const blob = await Packer.toBlob(doc)
  saveAs(blob, `${documentTitle.value || '文档'}.docx`)
}

async function parseHtmlToDocxNodes(html) {
  const div = document.createElement('div')
  div.innerHTML = html
  const nodes = []

  for (const el of div.childNodes) {
    if (el.nodeType !== 1) continue
    const tag = el.tagName?.toUpperCase() || ''

    if (tag === 'IMG' || el.querySelector?.('img')) {
      const imgs = tag === 'IMG' ? [el] : [...el.querySelectorAll('img')]
      for (const imgEl of imgs) {
        const run = await makeImageRun(imgEl)
        if (run) nodes.push(new Paragraph({ children: [run] }))
      }
      const clone = el.cloneNode(true)
      clone.querySelectorAll?.('img').forEach(i => i.remove())
      if (clone.innerText?.trim()) nodes.push(makeParagraph(el))
      continue
    }

    if (/^H[1-6]$/.test(tag)) {
      const lvlMap = {
        1: HeadingLevel.HEADING_1, 2: HeadingLevel.HEADING_2,
        3: HeadingLevel.HEADING_3, 4: HeadingLevel.HEADING_4,
        5: HeadingLevel.HEADING_5, 6: HeadingLevel.HEADING_6,
      }
      nodes.push(new Paragraph({ heading: lvlMap[parseInt(tag[1])], children: inlineRuns(el) }))
      continue
    }

    if (tag === 'UL' || tag === 'OL') {
      el.querySelectorAll('li').forEach((li, idx) => {
        nodes.push(new Paragraph({
          children: [new TextRun({
            text: (tag === 'OL' ? `${idx + 1}. ` : '\u2022 ') + (li.innerText || ''),
            size: 24,
          })]
        }))
      })
      continue
    }

    if (tag === 'TABLE') {
      const rows = [...el.querySelectorAll('tr')].map(tr => {
        const cells = [...tr.querySelectorAll('td,th')].map(td =>
          new DocxTableCell({
            children: [new Paragraph({ children: inlineRuns(td) })],
            shading: td.tagName === 'TH' ? { fill: 'E8EDF2', type: ShadingType.CLEAR } : undefined,
          })
        )
        return new DocxTableRow({ children: cells })
      })
      if (rows.length) nodes.push(new DocxTable({ width: { size: 100, type: WidthType.PERCENTAGE }, rows }))
      continue
    }

    if (tag === 'BLOCKQUOTE') {
      nodes.push(new Paragraph({
        indent: { left: 720 },
        children: [new TextRun({ text: el.innerText || '', size: 24, italics: true, color: '555555' })]
      }))
      continue
    }

    if (tag === 'PRE') {
      ;(el.innerText || '').split('\n').forEach(line => {
        nodes.push(new Paragraph({ children: [new TextRun({ text: line, font: 'Courier New', size: 20 })] }))
      })
      continue
    }

    nodes.push(makeParagraph(el))
  }

  if (nodes.length === 0) nodes.push(new Paragraph({ children: [new TextRun('')] }))
  return nodes
}

function inlineRuns(el) {
  const runs = []
  const parseStyle = (node, prop) => {
    const m = (node.getAttribute?.('style') || '').match(new RegExp(prop + '\\s*:\\s*([^;]+)'))
    return m ? m[1].trim() : null
  }
  const hexColor = (raw) => {
    if (!raw) return undefined
    if (raw.startsWith('#')) return raw.slice(1, 7)
    const m = raw.match(/\d+/g)
    return m ? m.slice(0, 3).map(n => parseInt(n).toString(16).padStart(2, '0')).join('') : undefined
  }
  const walk = (node, fmt = {}) => {
    if (node.nodeType === 3) {
      const text = node.textContent || ''
      if (!text) return
      runs.push(new TextRun({
        text,
        bold:      fmt.bold      || undefined,
        italics:   fmt.italic    || undefined,
        underline: fmt.underline ? {} : undefined,
        strike:    fmt.strike    || undefined,
        color:     fmt.color     || undefined,
        size:      fmt.fontSize  ? Math.round(fmt.fontSize * 2) : 24,
        font:      fmt.fontFamily || undefined,
      }))
      return
    }
    if (node.nodeType !== 1) return
    const t = node.tagName?.toUpperCase()
    let fontFamily = fmt.fontFamily
    const ff = parseStyle(node, 'font-family')
    if (ff) fontFamily = ff.replace(/['"]/g, '').split(',')[0].trim()
    let fontSize = fmt.fontSize
    const fs = parseStyle(node, 'font-size')
    if (fs) {
      const val = parseFloat(fs)
      if (fs.endsWith('pt')) fontSize = val
      else if (fs.endsWith('px')) fontSize = val * 0.75
      else if (fs.endsWith('em')) fontSize = val * 12
      else fontSize = val * 0.75
    }
    let color = fmt.color
    const fc = parseStyle(node, 'color')
    if (fc) color = hexColor(fc)
    const fw   = parseStyle(node, 'font-weight')
    const bold = fmt.bold || t === 'STRONG' || t === 'B' || fw === 'bold' || parseInt(fw) >= 600
    const next = {
      bold, italic: fmt.italic || t === 'EM' || t === 'I' || parseStyle(node, 'font-style') === 'italic',
      underline: fmt.underline || t === 'U',
      strike: fmt.strike || t === 'S' || t === 'DEL',
      color, fontSize, fontFamily,
    }
    node.childNodes.forEach(c => walk(c, next))
  }
  el.childNodes.forEach(c => walk(c, {}))
  return runs.length ? runs : [new TextRun({ text: el.innerText || '', size: 24 })]
}

function makeParagraph(el) {
  const sty     = el.getAttribute?.('style') || ''
  const getS    = (prop) => { const m = sty.match(new RegExp(prop + '\\s*:\\s*([^;]+)')); return m ? m[1].trim() : null }
  const align   = getS('text-align') || el.style?.textAlign || ''
  const alignMap = { center: AlignmentType.CENTER, right: AlignmentType.RIGHT, justify: AlignmentType.BOTH }
  return new Paragraph({ alignment: alignMap[align] || AlignmentType.LEFT, children: inlineRuns(el), spacing: { after: 120 } })
}

async function makeImageRun(imgEl) {
  try {
    const src = imgEl.src || imgEl.getAttribute('src') || ''
    if (!src) return null
    let base64, mimeType
    if (src.startsWith('data:')) {
      const [header, data] = src.split(',')
      mimeType = header.match(/data:(.*?);/)?.[1] || 'image/png'
      base64   = data
    } else {
      const resp = await fetch(src)
      const blob = await resp.blob()
      mimeType   = blob.type || 'image/png'
      const ab   = await blob.arrayBuffer()
      base64     = btoa(String.fromCharCode(...new Uint8Array(ab)))
    }
    const ext    = (mimeType.split('/')[1] || 'png').replace('jpeg', 'jpg').replace('svg+xml', 'png')
    const dispW  = imgEl.offsetWidth  || imgEl.naturalWidth  || 400
    const dispH  = imgEl.offsetHeight || imgEl.naturalHeight || 300
    const outW   = Math.min(dispW, 550)
    const outH   = Math.round(outW / dispW * dispH)
    const binary = atob(base64)
    const bytes  = new Uint8Array(binary.length)
    for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i)
    return new ImageRun({ data: bytes, transformation: { width: outW, height: outH }, type: ext })
  } catch (e) {
    console.warn('图片导出失败:', e)
    return null
  }
}

//  查找替换
function doSearch() {
  matchPositions = []
  currentMatch.value = 0
  totalMatches.value = 0
  if (!findText.value || !editor.value) return
  const text  = editor.value.getText()
  const query = caseSensitive.value ? findText.value : findText.value.toLowerCase()
  const src   = caseSensitive.value ? text : text.toLowerCase()
  const offsets = []
  let pos = src.indexOf(query)
  while (pos !== -1) { offsets.push(pos); pos = src.indexOf(query, pos + 1) }
  const doc = editor.value.state.doc
  offsets.forEach(offset => {
    let charCount = 0, found = false
    doc.descendants((node, nodePos) => {
      if (found || !node.isText) return
      const nodeLen = node.text.length
      if (charCount + nodeLen > offset) {
        const from = nodePos + (offset - charCount)
        matchPositions.push({ from, to: from + findText.value.length })
        found = true
      }
      charCount += nodeLen
    })
  })
  totalMatches.value = matchPositions.length
  if (matchPositions.length > 0) { currentMatch.value = 1; scrollToMatch(0) }
}

function scrollToMatch(idx) {
  const match = matchPositions[idx]
  if (!match) return
  editor.value.chain().focus().setTextSelection(match).run()
}

function findNext() {
  if (!matchPositions.length) { doSearch(); return }
  const next = currentMatch.value % matchPositions.length
  currentMatch.value = next + 1
  scrollToMatch(next)
}

function findPrev() {
  if (!matchPositions.length) { doSearch(); return }
  const prev = (currentMatch.value - 2 + matchPositions.length) % matchPositions.length
  currentMatch.value = prev + 1
  scrollToMatch(prev)
}

function replaceOne() {
  if (!matchPositions.length) return
  const match = matchPositions[currentMatch.value - 1]
  if (!match) return
  editor.value.chain().focus().setTextSelection(match).insertContent(replaceText.value).run()
  doSearch()
}

function replaceAll() {
  if (!matchPositions.length) return
  ;[...matchPositions].sort((a, b) => b.from - a.from).forEach(({ from, to }) => {
    editor.value.chain().setTextSelection({ from, to }).insertContent(replaceText.value).run()
  })
  doSearch()
}

function closeFindReplace() {
  showFindReplace.value = false
  findText.value = ''; replaceText.value = ''
  matchPositions = []; currentMatch.value = 0; totalMatches.value = 0
}

onBeforeUnmount(() => {
  if (autoSaveTimer)    clearTimeout(autoSaveTimer)
  if (remoteToastTimer) clearTimeout(remoteToastTimer)
  disconnectCollaboration()
  editor.value?.destroy()
  ydoc?.destroy()
  document.removeEventListener('keydown', handleGlobalKey)
})
</script>

<style scoped>
.rich-editor {
  border: 1px solid #e1e5e9;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 100vh;   /* ← Fix: was height:100%, which collapses when parent has no explicit height */
  position: relative;
}

.loading-overlay {
  position: absolute; inset: 0;
  background: rgba(255,255,255,0.92);
  display: flex; flex-direction: column;
  justify-content: center; align-items: center;
  z-index: 1000;
}

.editor-error-banner {
  background: #fee2e2; color: #991b1b;
  border: 1px solid #fca5a5; border-radius: 6px;
  padding: 12px 20px; margin: 12px; font-size: 13px;
  line-height: 1.5;
}
.spinner {
  width: 36px; height: 36px;
  border: 3px solid #e5e7eb;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 12px;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ── 工具栏 ── */
.toolbar { border-bottom: 1px solid #e1e5e9; background: #f8f9fa; flex-shrink: 0; }
.toolbar-row {
  padding: 6px 12px;
  display: flex; align-items: center; gap: 3px; flex-wrap: wrap;
}
.toolbar-row button {
  padding: 4px 9px; border: 1px solid #d1d5db; background: white; color: #374151;
  border-radius: 4px; cursor: pointer; font-size: 13px; font-weight: 500;
  transition: all 0.15s; white-space: nowrap; height: 28px;
}
.toolbar-row button:hover  { background: #f3f4f6; border-color: #9ca3af; }
.toolbar-row button.active { background: #dbeafe; border-color: #3b82f6; color: #1d4ed8; }
.toolbar-row button:disabled { opacity: 0.4; cursor: not-allowed; }
.tb-select {
  height: 28px; padding: 0 6px; border: 1px solid #d1d5db; border-radius: 4px;
  background: white; font-size: 13px; color: #374151; cursor: pointer;
}
.tb-select:focus { outline: none; border-color: #3b82f6; }
.color-btn {
  position: relative; display: inline-flex; align-items: center; justify-content: center;
  width: 32px; height: 28px; border: 1px solid #d1d5db; border-radius: 4px;
  background: white; cursor: pointer; overflow: hidden;
}
.color-btn:hover { border-color: #9ca3af; }
.color-icon { font-size: 13px; font-weight: 700; user-select: none; }
.color-btn input[type=color] { position: absolute; opacity: 0; width: 100%; height: 100%; cursor: pointer; }
.save-btn { background: #10b981 !important; color: white !important; border-color: #10b981 !important; }
.save-btn:hover { background: #059669 !important; }
.back-btn { background: #6b7280 !important; color: white !important; border-color: #6b7280 !important; }
.back-btn:hover { background: #4b5563 !important; }
.divider { width: 1px; height: 20px; background: #d1d5db; margin: 0 4px; flex-shrink: 0; }

/* ── 查找替换 ── */
.find-replace-bar {
  display: flex; align-items: center; flex-wrap: wrap; gap: 10px;
  padding: 8px 14px; background: #fff; border-top: 1px solid #e1e5e9; font-size: 13px;
}
.fr-group { display: flex; align-items: center; gap: 6px; }
.fr-group label { color: #6b7280; white-space: nowrap; }
.fr-group input { padding: 4px 8px; border: 1px solid #d1d5db; border-radius: 4px; font-size: 13px; width: 180px; outline: none; }
.fr-group input:focus { border-color: #3b82f6; box-shadow: 0 0 0 2px rgba(59,130,246,0.1); }
.fr-count { font-size: 12px; color: #6b7280; white-space: nowrap; min-width: 36px; }
.fr-options label { display: flex; align-items: center; gap: 4px; color: #374151; cursor: pointer; }
.fr-actions { display: flex; align-items: center; gap: 6px; margin-left: auto; }
.fr-actions button { padding: 4px 10px; border: 1px solid #d1d5db; border-radius: 4px; background: white; font-size: 12px; cursor: pointer; color: #374151; transition: background 0.15s; }
.fr-actions button:hover { background: #f3f4f6; }
.fr-close { border-color: #fca5a5 !important; color: #ef4444 !important; }
.fr-close:hover { background: #fee2e2 !important; }

/*── 协同栏 ── */
.collab-bar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 6px 16px;
  background: linear-gradient(90deg, #eff6ff, #f0fdf4);
  border-bottom: 1px solid #bfdbfe;
  flex-shrink: 0;
  min-height: 36px;
}
.collab-left { display: flex; align-items: center; gap: 8px; }
.collab-label { font-size: 12px; color: #6b7280; font-weight: 500; }
.collab-avatars { display: flex; gap: 4px; }
.collab-avatar {
  width: 26px; height: 26px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 700; color: white;
  border: 2px solid white;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
  cursor: default;
}
.collab-count { font-size: 12px; color: #6b7280; }
.remote-toast {
  font-size: 12px; color: #1d4ed8;
  background: #dbeafe; padding: 3px 10px; border-radius: 20px;
  border: 1px solid #bfdbfe;
}

/* ——过渡动画——*/
.collab-slide-enter-active, .collab-slide-leave-active { transition: all 0.25s ease; }
.collab-slide-enter-from, .collab-slide-leave-to { opacity: 0; max-height: 0; padding-top: 0; padding-bottom: 0; }
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* ── 标题栏 ── */
.title-bar { display: flex; align-items: center; gap: 12px; padding: 14px 24px 10px; border-bottom: 1px solid #f0f0f0; background: white; }
.title-input {
  flex: 1; font-size: 22px; font-weight: 700; color: #1a1a2e;
  border: none; outline: none; background: transparent;
  padding: 4px 0; border-bottom: 2px solid transparent; transition: border-color 0.2s;
}
.title-input:focus { border-bottom-color: #3b82f6; }
.title-input::placeholder { color: #c4c9d4; font-weight: 400; }
.title-input[readonly] { cursor: default; color: #374151; }
.title-hint { font-size: 11px; color: #b0b7c3; white-space: nowrap; flex-shrink: 0; }

/* ── 编辑区 ── */
.editor-content { flex: 1; padding: 20px 24px; overflow-y: auto; min-height: 360px; }
.editor-content :deep(.ProseMirror) { outline: none !important; font-size: 15px; line-height: 1.75; color: #374151; }
.editor-content :deep(.ProseMirror p) { margin-bottom: 0.9em; }
.editor-content :deep(.ProseMirror h1) { font-size: 1.9em; font-weight: 700; margin: 0.5em 0 0.3em; }
.editor-content :deep(.ProseMirror h2) { font-size: 1.45em; font-weight: 600; margin: 0.5em 0 0.3em; }
.editor-content :deep(.ProseMirror h3) { font-size: 1.2em; font-weight: 600; margin: 0.5em 0 0.3em; }
.editor-content :deep(.ProseMirror ul),
.editor-content :deep(.ProseMirror ol)  { margin: 0.8em 0; padding-left: 1.8em; }
.editor-content :deep(.ProseMirror blockquote) {
  border-left: 4px solid #3b82f6; margin: 1em 0; padding: 8px 16px;
  background: #f0f9ff; border-radius: 0 6px 6px 0; color: #1e40af;
}
.editor-content :deep(.ProseMirror pre) {
  background: #1e293b; color: #e2e8f0; padding: 14px 18px; border-radius: 6px;
  font-family: 'Courier New', monospace; font-size: 13px; overflow-x: auto; margin: 1em 0;
}
.editor-content :deep(.ProseMirror code) {
  background: #f1f5f9; color: #0f172a; padding: 1px 5px;
  border-radius: 3px; font-family: monospace; font-size: 0.9em;
}
.editor-content :deep(.ProseMirror table) { border-collapse: collapse; width: 100%; margin: 1em 0; table-layout: fixed; }
.editor-content :deep(.ProseMirror th),
.editor-content :deep(.ProseMirror td) { border: 1px solid #d1d5db; padding: 8px 12px; min-width: 60px; vertical-align: top; }
.editor-content :deep(.ProseMirror th) { background: #f8fafc; font-weight: 600; color: #1e293b; }
.editor-content :deep(.ProseMirror .selectedCell) { background: #dbeafe; }
.editor-content :deep(.ProseMirror .column-resize-handle) {
  position: absolute; right: -2px; top: 0; bottom: 0; width: 4px; background: #3b82f6; pointer-events: none;
}
.editor-content :deep(.ProseMirror img.ProseMirror-selectednode) { outline: 2px solid #3b82f6; border-radius: 4px; }
.editor-content :deep(.ProseMirror ::selection) { background: #fef08a; }

/* ── 状态栏 ── */
.info {
  padding: 10px 16px; border-top: 1px solid #e1e5e9; background: #f8f9fa;
  display: flex; justify-content: space-between; align-items: center; flex-shrink: 0;
}
.stats { display: flex; align-items: center; gap: 16px; font-size: 13px; color: #6b7280; }
.save-status { font-size: 12px; padding: 2px 8px; border-radius: 10px; }
.save-status.saving { background: #fef9c3; color: #92400e; }
.save-status.saved  { background: #dcfce7; color: #166534; }
.save-status.error  { background: #fee2e2; color: #991b1b; }
.mode-toggle { padding: 5px 14px; background: #3b82f6; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 13px; transition: background 0.2s; }
.mode-toggle:hover { background: #2563eb; }

@media (max-width: 768px) {
  .toolbar-row { padding: 6px 8px; gap: 2px; }
  .toolbar-row button { padding: 3px 6px; font-size: 12px; }
  .tb-select { height: 26px; font-size: 12px; }
  .collab-bar { padding: 5px 12px; }
  .collab-count { display: none; }
  .title-bar { padding: 10px 16px 8px; }
  .title-input { font-size: 18px; }
  .title-hint { display: none; }
  .editor-content { padding: 14px 16px; min-height: 280px; }
  .info { padding: 8px 12px; flex-direction: column; gap: 8px; }
  .find-replace-bar { flex-direction: column; align-items: flex-start; }
  .fr-group input { width: 140px; }
  .fr-actions { margin-left: 0; }
}

/* ——Y.js 协同光标样式—— */
/* ——其他用户的实时光标—— */
:deep(.collaboration-cursor__caret) {
  border-left: 2px solid;
  border-right: none;
  margin-left: -1px;
  margin-right: -1px;
  word-break: normal;
  pointer-events: none;
  position: relative;
}

/* ——光标上方的用户名标签 ——*/
:deep(.collaboration-cursor__label) {
  border-radius: 3px 3px 3px 0;
  color: #fff;
  font-size: 11px;
  font-style: normal;
  font-weight: 600;
  left: -1px;
  line-height: normal;
  padding: 2px 6px;
  position: absolute;
  top: -1.4em;
  user-select: none;
  white-space: nowrap;
  pointer-events: none;
}
</style>
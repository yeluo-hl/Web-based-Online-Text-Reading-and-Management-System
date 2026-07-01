import axios from 'axios'
import router from '@/router'

const apiClient = axios.create({
  baseURL: 'http://localhost:8088',
  withCredentials: false,
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000
})

apiClient.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
  },
  error => Promise.reject(error)
)

apiClient.interceptors.response.use(
  response => response,
  error => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          localStorage.removeItem('user')
          localStorage.removeItem('token')
          router.push('/')
          break
        case 403: error.message = '没有权限访问'; break
        case 404: error.message = '请求的资源不存在'; break
        case 500: error.message = '服务器内部错误'; break
        default:  error.message = error.response.data?.message || '请求失败'
      }
    } else if (error.request) {
      error.message = '网络连接失败，请检查网络'
    } else {
      error.message = '请求配置错误'
    }
    return Promise.reject(error)
  }
)

export const userAPI = {
  login:          (credentials) => apiClient.post('/login', credentials),
  register:       (userData)    => apiClient.post('/register', userData),
  getUserInfo:    ()            => apiClient.get('/user/profile'),
  updateUserInfo: (userData)    => apiClient.put('/user/profile', userData)
}

export const documentAPI = {
  saveDocument: (documentData) => {
    if (documentData.id) return apiClient.put(`/api/documents/${documentData.id}`, documentData)
    return apiClient.post('/api/documents', documentData)
  },

  updateDocumentTitle: (id, title) =>
    apiClient.patch(`/api/documents/${id}/title`, { title }),

  getDocumentList: (params) =>
    apiClient.get('/api/documents', {
      params: { page: params?.page || 1, limit: params?.limit || 20 }
    }),

  // 关键词搜索（标题 + 内容）
  searchDocuments: (q, page = 1, limit = 20) =>
    apiClient.get('/api/documents/search', { params: { q, page, limit } }),

  getDocument:          (id)  => apiClient.get(`/api/documents/${id}`),
  deleteDocument:       (id)  => apiClient.delete(`/api/documents/${id}`),
  batchDeleteDocuments: (ids) => apiClient.delete('/api/documents/batch', { data: ids })
}

export default apiClient
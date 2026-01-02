import axios from 'axios'
import { getToken, removeToken } from './auth'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000, // 默认30秒超时
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      // 确保multipart/form-data请求也能正确设置Authorization
      config.headers.Authorization = `Bearer ${token}`
    }
    // 如果是FormData，删除Content-Type让浏览器自动设置（包括boundary）
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type']
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      removeToken()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api


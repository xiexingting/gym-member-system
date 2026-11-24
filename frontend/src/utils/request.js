import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 统一的 axios 实例
 * - 接口地址只在这里写一次，组件里用相对路径即可
 * - 自动适配后端改造后的 { code, message, data } 结构
 * - 队友还没改造的模块返回原始数据时也能兼容，不会互相影响
 */
const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000
})

// 请求拦截器：自动带上登录 token（B2 登录权限）
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('gym_token')
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data

    // 新格式：{ code, message, data }
    if (res && typeof res === 'object' && !Array.isArray(res) && 'code' in res) {
      if (res.code === 200) return res
      ElMessage.error(res.message || '操作失败')
      return Promise.reject(new Error(res.message || '操作失败'))
    }

    // 旧格式（队友模块可能还没改）：原始数组 / success 字符串
    return { code: 200, message: 'ok', data: res }
  },
  (error) => {
    // 401：未登录或登录过期，清除 token 并回到登录页
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('gym_token')
      ElMessage.warning('登录已失效，请重新登录')
      window.location.reload()
      return Promise.reject(error)
    }
    // 403：只读角色执行了写操作，给出明确原因
    if (error.response && error.response.status === 403) {
      ElMessage.warning('当前账号为只读角色，无权执行此操作')
      return Promise.reject(error)
    }
    ElMessage.error('服务不可用，请确认后端已启动')
    return Promise.reject(error)
  }
)

export default request

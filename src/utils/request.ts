import axios from 'axios'
import { showToast } from 'vant'

function generateUUID(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

let deviceId = localStorage.getItem('device_id')
if (!deviceId) {
  deviceId = 'H5-' + generateUUID()
  localStorage.setItem('device_id', deviceId)
}

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 10000
})

request.interceptors.request.use((config) => {
  config.headers['App-Version'] = '1.0.0'
  config.headers['Platform'] = 'H5'
  config.headers['Device-ID'] = deviceId

  const token = localStorage.getItem('SWU_TOKEN')
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }

  if (config.method && ['post', 'put', 'delete'].includes(config.method.toLowerCase())) {
    if (config.data && typeof config.data === 'object') {
      config.data.requestId = config.data.requestId || generateUUID()
    } else if (!config.data) {
      config.data = { requestId: generateUUID() }
    }
  }

  return config
}, (error) => Promise.reject(error))

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      showToast(res.message || '系统繁忙')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res.data
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      localStorage.removeItem('SWU_TOKEN')
      localStorage.removeItem('SWU_ROLE')
      showToast('登录已过期，请重新登录')
      window.location.href = '/login'
    } else if (status === 403) {
      showToast('权限不足，无法执行该操作')
    } else if (status === 404) {
      showToast('未找到相关信息')
    } else if (status === 429) {
      showToast('操作太频繁，请稍后再试')
    } else if (status && status >= 500) {
      showToast('系统繁忙，请稍后再试')
    } else {
      showToast('网络连接异常，请检查网络')
    }
    return Promise.reject(error)
  }
)

export default request

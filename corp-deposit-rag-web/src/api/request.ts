import axios from 'axios'

export const request = axios.create({ timeout: 15_000 })

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('corp-deposit-token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  response => response,
  (error) => {
    if (error.response?.status === 401) localStorage.removeItem('corp-deposit-token')
    const message = error.response?.data?.message
      || (error.code === 'ECONNABORTED' ? '服务响应超时，请稍后重试。' : '服务暂时不可用，请检查后端服务。')
    return Promise.reject(new Error(message))
  }
)

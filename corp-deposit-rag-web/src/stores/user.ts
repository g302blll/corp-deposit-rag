import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('corp-deposit-token') || '')
  const userName = ref(localStorage.getItem('corp-deposit-user') || '')
  function login(username: string, password: string) {
    if (username !== 'zhangsan' || password !== '123456') throw new Error('工号或密码错误')
    token.value = 'mock-manager-token'
    userName.value = '张经理'
    localStorage.setItem('corp-deposit-token', token.value)
    localStorage.setItem('corp-deposit-user', userName.value)
  }
  function logout() {
    token.value = ''
    userName.value = ''
    localStorage.removeItem('corp-deposit-token')
    localStorage.removeItem('corp-deposit-user')
  }
  return { token, userName, login, logout }
})

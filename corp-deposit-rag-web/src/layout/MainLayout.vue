<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const user = useUserStore()
function logout() { user.logout(); router.push('/login') }
</script>

<template>
  <div class="app-frame">
    <header class="topbar">
      <router-link class="brand" to="/assistant"><b>存</b><span>对公存款智能服务助手<small>AI Copilot</small></span></router-link>
      <nav>
        <router-link to="/assistant">AI 工作台</router-link>
        <router-link to="/intentions">办理意向</router-link>
        <router-link to="/products">产品中心</router-link>
      </nav>
      <div class="manager"><span>{{ user.userName || '张经理' }}</span><button @click="logout">退出</button></div>
    </header>
    <router-view />
  </div>
</template>

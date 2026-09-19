<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
const router = useRouter(); const user = useUserStore()
const form = reactive({ username: 'zhangsan', password: '123456' }); const error = ref('')
function submit() { try { user.login(form.username, form.password); router.push('/assistant') } catch (cause) { error.value = (cause as Error).message } }
</script>
<template>
  <main class="login-page">
    <section class="login-story"><div class="brand-seal">存</div><p>Corporate Deposit AI Copilot</p><h1>让每一次客户资金配置<br>都有清晰依据</h1><ul><li>客户准入与执行利率确定性校验</li><li>自然语言需求解析与方案编排</li><li>方案依据可追溯，办理动作有边界</li></ul></section>
    <section class="login-form"><div><p class="eyebrow">WELCOME BACK</p><h2>客户经理登录</h2><p>登录后进入 AI 智能工作台</p><el-form label-position="top" @submit.prevent="submit"><el-form-item label="工号"><el-input v-model="form.username" size="large" /></el-form-item><el-form-item label="密码"><el-input v-model="form.password" type="password" show-password size="large" /></el-form-item><el-alert v-if="error" :title="error" type="error" :closable="false" /><el-button type="primary" size="large" native-type="submit">登录工作台</el-button></el-form><small>演示账号 zhangsan / 123456</small></div></section>
  </main>
</template>

<script setup lang="ts">
const model = defineModel<string>({ required: true })
defineProps<{ loading: boolean; disabled: boolean }>()
const emit = defineEmits<{ send: [] }>()
function keydown(event: KeyboardEvent) {
  if (event.key === 'Enter' && !event.shiftKey) { event.preventDefault(); emit('send') }
}
</script>
<template>
  <div class="assistant-input">
    <textarea v-model="model" rows="3" :disabled="loading" placeholder="描述客户的金额、期限、流动性和收益偏好…" @keydown="keydown"></textarea>
    <button :disabled="disabled || loading" @click="emit('send')">{{ loading ? '分析中…' : '发送需求' }}</button>
    <small>Enter 发送 · Shift + Enter 换行 · 金融结果由 Java 服务确定</small>
  </div>
</template>

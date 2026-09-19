import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ChatMessage, DepositPlan } from '@/types/assistant'

export const useConversationStore = defineStore('conversation', () => {
  const messages = ref<ChatMessage[]>([])
  const plans = ref<DepositPlan[]>([])
  const loading = ref(false)
  function appendMessage(message: ChatMessage) { messages.value.push(message) }
  function setPlans(value: DepositPlan[]) { plans.value = value }
  function reset() { messages.value = []; plans.value = []; loading.value = false }
  return { messages, plans, loading, appendMessage, setPlans, reset }
})

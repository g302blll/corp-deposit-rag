import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ChatMessage, DepositPlan } from '@/types/assistant'

export const useConversationStore = defineStore('conversation', () => {
  const messages = ref<ChatMessage[]>([])
  const plans = ref<DepositPlan[]>([])
  const loading = ref(false)
  const sessionVersion = ref(0)
  function appendMessage(message: ChatMessage) { messages.value.push(message) }
  function setPlans(value: DepositPlan[]) { plans.value = value }
  function reset() { messages.value = []; plans.value = []; loading.value = false; sessionVersion.value += 1 }
  return { messages, plans, loading, sessionVersion, appendMessage, setPlans, reset }
})

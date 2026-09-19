<script setup lang="ts">
import { ref } from 'vue'
import { getPlans } from '@/api/assistant'
import CustomerContextPanel from '@/components/customer/CustomerContextPanel.vue'
import AssistantInput from '@/components/assistant/AssistantInput.vue'
import DepositPlanCard from '@/components/assistant/DepositPlanCard.vue'
import DepositPlanDetailDrawer from '@/components/assistant/DepositPlanDetailDrawer.vue'
import IntentionConfirmDialog from '@/components/assistant/IntentionConfirmDialog.vue'
import { useCustomerStore } from '@/stores/customer'
import { useConversationStore } from '@/stores/conversation'
import type { DepositPlan, IntentionResult } from '@/types/assistant'

const customerStore = useCustomerStore(); const conversation = useConversationStore()
const input = ref('800万存一年，优先收益，可以长期不用')
const detailPlan = ref<DepositPlan | null>(null); const selectedPlan = ref<DepositPlan | null>(null)
const detailVisible = ref(false); const confirmVisible = ref(false); const lastRequirement = ref('')
const quickQuestions = ['800万存一年，优先收益', '500万，需要保持一定流动性', '这个客户能办理哪些产品？']
const messageId = () => `${Date.now()}-${Math.random()}`

async function send() {
  const text = input.value.trim()
  if (!text || conversation.loading) return
  if (!customerStore.currentCustomer) {
    conversation.appendMessage({ id: messageId(), role: 'USER', type: 'TEXT', text, createdAt: new Date().toISOString() })
    conversation.appendMessage({ id: messageId(), role: 'ASSISTANT', type: 'TEXT', text: '可以进行通用产品咨询；如需查询客户准入、执行利率或生成个性化方案，请先在左侧选择客户。当前通用咨询使用本地 Mock，不调用付费模型。', createdAt: new Date().toISOString() })
    input.value = ''
    return
  }
  const requestCustomerNo = customerStore.currentCustomer.customerNo
  const requestSessionVersion = conversation.sessionVersion
  conversation.appendMessage({ id: messageId(), role: 'USER', type: 'TEXT', text, createdAt: new Date().toISOString() })
  input.value = ''; lastRequirement.value = text; conversation.loading = true
  try {
    const response = await getPlans({ customerNo: requestCustomerNo, message: text })
    if (conversation.sessionVersion !== requestSessionVersion || customerStore.currentCustomer?.customerNo !== requestCustomerNo) return
    customerStore.selectCustomer({ ...customerStore.currentCustomer, ...response.customer })
    conversation.appendMessage({ id: messageId(), role: 'ASSISTANT', type: 'TEXT', text: response.message, createdAt: new Date().toISOString() })
    if (response.plans.length) conversation.appendMessage({ id: messageId(), role: 'ASSISTANT', type: 'PLAN', plans: response.plans, createdAt: new Date().toISOString() })
    conversation.setPlans(response.plans)
  } catch (cause) {
    if (conversation.sessionVersion !== requestSessionVersion) return
    conversation.appendMessage({ id: messageId(), role: 'ASSISTANT', type: 'ERROR', text: cause instanceof Error ? cause.message : '方案生成失败', createdAt: new Date().toISOString() })
  } finally {
    if (conversation.sessionVersion === requestSessionVersion) conversation.loading = false
  }
}
function showDetail(plan: DepositPlan) { detailPlan.value = plan; detailVisible.value = true }
function selectPlan(plan: DepositPlan) { selectedPlan.value = plan; confirmVisible.value = true }
function intentionSuccess(result: IntentionResult) { conversation.appendMessage({ id: messageId(), role: 'ASSISTANT', type: 'INTENTION_RESULT', result, text: '办理意向已成功创建。', createdAt: new Date().toISOString() }) }
</script>
<template>
  <main class="assistant-page">
    <CustomerContextPanel />
    <section class="conversation-panel">
      <header class="conversation-header"><div><p class="eyebrow">AI COPILOT</p><h1>智能存款顾问</h1></div><span><i></i> Mock 参数提取模式</span></header>
      <div class="message-list">
        <article class="message assistant"><div class="ai-avatar">AI</div><div class="bubble"><b>您好，张经理</b><p>我可以帮助分析客户资金需求、产品准入、执行利率与预计收益。请先选择客户，再告诉我资金安排。</p><div class="quick-list"><button v-for="question in quickQuestions" :key="question" @click="input = question">{{ question }}</button></div></div></article>
        <article v-for="message in conversation.messages" :key="message.id" class="message" :class="message.role.toLowerCase()">
          <div v-if="message.role === 'ASSISTANT'" class="ai-avatar">AI</div>
          <div class="bubble" :class="{ error: message.type === 'ERROR', result: message.type === 'INTENTION_RESULT' }">
            <template v-if="message.type === 'INTENTION_RESULT'"><b>办理意向已创建</b><p>交易流水号：{{ message.result?.intentionNo }}</p><el-tag type="success">状态 {{ message.result?.status }}</el-tag></template>
            <p v-else-if="message.type !== 'PLAN'">{{ message.text }}</p>
            <div v-else class="plan-list"><DepositPlanCard v-for="plan in message.plans" :key="plan.planId" :plan="plan" @detail="showDetail" @select="selectPlan" /></div>
          </div>
        </article>
        <article v-if="conversation.loading" class="message assistant"><div class="ai-avatar">AI</div><div class="bubble thinking"><i></i><i></i><i></i><span>正在查询客户准入与有效利率…</span></div></article>
      </div>
      <AssistantInput v-model="input" :loading="conversation.loading" :disabled="!input.trim()" @send="send" />
    </section>
    <DepositPlanDetailDrawer v-model="detailVisible" :plan="detailPlan" :customer="customerStore.currentCustomer" />
    <IntentionConfirmDialog v-model="confirmVisible" :plan="selectedPlan" :customer="customerStore.currentCustomer" :requirement-text="lastRequirement" @success="intentionSuccess" />
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { listCustomers } from '@/api/customer'
import { useCustomerStore } from '@/stores/customer'
import { useConversationStore } from '@/stores/conversation'
import type { CustomerSummary } from '@/types/assistant'

const customerStore = useCustomerStore()
const conversation = useConversationStore()
const drawer = ref(false)
const customers = ref<CustomerSummary[]>([])
onMounted(async () => { customers.value = await listCustomers() })

async function choose(customer: CustomerSummary) {
  if (customerStore.currentCustomer && customerStore.currentCustomer.customerNo !== customer.customerNo) {
    await ElMessageBox.confirm('切换客户将开启新的咨询会话，并清空当前会话内容。', '切换客户', { type: 'warning' })
    conversation.reset()
  }
  customerStore.selectCustomer(customer)
  drawer.value = false
}
</script>

<template>
  <aside class="context-panel">
    <div class="panel-title"><span>客户上下文</span><em>已连接业务服务</em></div>
    <template v-if="customerStore.currentCustomer">
      <div class="customer-avatar">{{ customerStore.currentCustomer.customerName.slice(0, 2) }}</div>
      <h2>{{ customerStore.currentCustomer.customerName }}</h2>
      <p class="customer-no">{{ customerStore.currentCustomer.customerNo }} · 等级 {{ customerStore.currentCustomer.level }}</p>
      <dl>
        <div><dt>客户大类</dt><dd>{{ customerStore.currentCustomer.largeCategoryName }}</dd></div>
        <div><dt>客户小类</dt><dd>{{ customerStore.currentCustomer.smallCategoryName }}</dd></div>
        <div><dt>客户经理</dt><dd>{{ customerStore.currentCustomer.managerName }}</dd></div>
      </dl>
      <div class="eligible"><b>可办理产品</b><span>单位定期存款</span><span>单位通知存款</span></div>
      <el-button class="switch-button" @click="drawer = true">切换客户</el-button>
    </template>
    <div v-else class="no-customer">
      <div>企</div><h2>当前未选择客户</h2><p>选择客户后可进行准入分析、利率查询和个性化产品推荐。</p>
      <el-button type="primary" @click="drawer = true">选择客户</el-button>
    </div>
    <el-drawer v-model="drawer" title="选择对公客户" size="520px">
      <div v-for="customer in customers" :key="customer.customerNo" class="customer-option" @click="choose(customer)">
        <div><b>{{ customer.customerName }}</b><span>{{ customer.customerNo }}</span></div>
        <p>{{ customer.largeCategoryName }} · {{ customer.smallCategoryName }}</p>
      </div>
    </el-drawer>
  </aside>
</template>

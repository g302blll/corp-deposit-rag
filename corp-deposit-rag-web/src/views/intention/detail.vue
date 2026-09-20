<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listCustomers } from '@/api/customer'
import { getIntention, type IntentionDetail } from '@/api/intention'
import { getProduct, type ProductDetail } from '@/api/product'
import { createCatalog } from '@/utils/catalog'
import { formatMoneyShort } from '@/utils/money'
import { formatRate } from '@/utils/rate'

const route = useRoute()
const router = useRouter()
const intention = ref<IntentionDetail | null>(null)
const catalog = ref(createCatalog([], []))
const loading = ref(false)
const error = ref('')
const referenceWarning = ref('')
const statusLabels: Record<number, string> = {
  0: '草稿', 1: '已确认', 2: '处理中', 3: '部分成功', 4: '成功', 5: '失败', 6: '已取消'
}
const detailStatusLabels: Record<number, string> = {
  0: '待处理', 1: '处理中', 2: '成功', 3: '失败', 4: '已取消'
}
const sourceLabels: Record<number, string> = {
  1: 'AI 助手', 2: '柜面', 3: '手机银行', 4: '企业网银', 9: '其他'
}

function formatDateTime(value: string) {
  return value.replace('T', ' ')
}

async function loadReferences(detail: IntentionDetail) {
  const productIds = [...new Set(detail.details.map(line => line.productId))]
  const [customerResult, ...productResults] = await Promise.allSettled([
    listCustomers(),
    ...productIds.map(productId => getProduct(productId))
  ])
  const customers = customerResult.status === 'fulfilled' ? customerResult.value : []
  const products = productResults
    .filter((result): result is PromiseFulfilledResult<ProductDetail> => result.status === 'fulfilled')
    .map(result => result.value)
  catalog.value = createCatalog(customers, products)
  if (customerResult.status === 'rejected' || productResults.some(result => result.status === 'rejected')) {
    referenceWarning.value = '部分名称暂时无法补全，页面将显示原始编号。'
  }
}

onMounted(async () => {
  loading.value = true
  try {
    const detail = await getIntention(String(route.params.id))
    intention.value = detail
    await loadReferences(detail)
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '办理意向详情加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <main class="content-page">
    <header>
      <p class="eyebrow">INTENTION DETAIL</p>
      <h1>意向详情</h1>
      <p>查看办理意向主单与产品明细。</p>
    </header>
    <section class="data-card" v-loading="loading">
      <el-button link type="primary" @click="router.push('/intentions')">← 返回意向列表</el-button>
      <el-alert v-if="referenceWarning" :title="referenceWarning" type="warning" show-icon :closable="false" class="page-alert" />
      <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" class="page-alert" />
      <template v-else-if="intention">
        <el-descriptions :column="3" border class="detail-summary">
          <el-descriptions-item label="意向编号">{{ intention.intentionNo }}</el-descriptions-item>
          <el-descriptions-item label="客户">{{ catalog.customerName(intention.customerNo) }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusLabels[intention.status] ?? intention.status }}</el-descriptions-item>
          <el-descriptions-item label="意向金额">{{ formatMoneyShort(intention.totalAmountInCents) }}</el-descriptions-item>
          <el-descriptions-item label="来源渠道">{{ sourceLabels[intention.sourceChannel] ?? intention.sourceChannel }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(intention.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="客户需求" :span="3">{{ intention.requirementText || '—' }}</el-descriptions-item>
        </el-descriptions>
        <h2>产品明细</h2>
        <el-table :data="intention.details">
          <el-table-column prop="detailNo" label="明细编号" min-width="210" />
          <el-table-column label="产品"><template #default="{ row }">{{ catalog.productName(row.productId) }}</template></el-table-column>
          <el-table-column label="期限"><template #default="{ row }">{{ catalog.termName(row.productTermId) }}</template></el-table-column>
          <el-table-column label="币种" width="90"><template #default="{ row }">{{ row.currencyCode === '001' ? '人民币' : row.currencyCode }}</template></el-table-column>
          <el-table-column label="金额"><template #default="{ row }">{{ formatMoneyShort(row.amountInCents) }}</template></el-table-column>
          <el-table-column label="执行利率"><template #default="{ row }">{{ formatRate(row.interestRate) }}</template></el-table-column>
          <el-table-column label="预期收益"><template #default="{ row }">{{ formatMoneyShort(row.expectedInterestInCents) }}</template></el-table-column>
          <el-table-column label="状态" width="100"><template #default="{ row }">{{ detailStatusLabels[row.status] ?? row.status }}</template></el-table-column>
        </el-table>
      </template>
    </section>
  </main>
</template>

<style scoped>
.detail-summary { margin: 20px 0 26px; }
.page-alert { margin: 16px 0; }
h2 { margin: 0 0 14px; font-size: 18px; }
</style>

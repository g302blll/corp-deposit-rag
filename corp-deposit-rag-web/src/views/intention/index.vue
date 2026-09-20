<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listCustomers } from '@/api/customer'
import { listIntentions, type IntentionListItem } from '@/api/intention'
import { getProduct, listProducts, type ProductDetail } from '@/api/product'
import { createCatalog } from '@/utils/catalog'
import { formatMoneyShort } from '@/utils/money'

const router = useRouter()
const intentions = ref<IntentionListItem[]>([])
const customerNo = ref('')
const status = ref<number | undefined>()
const loading = ref(false)
const error = ref('')
const referenceWarning = ref('')
const catalog = ref(createCatalog([], []))
const statusLabels: Record<number, string> = {
  0: '草稿', 1: '已确认', 2: '处理中', 3: '部分成功', 4: '成功', 5: '失败', 6: '已取消'
}

function formatDateTime(value: string) {
  return value.replace('T', ' ')
}

function openIntention(row: IntentionListItem) {
  router.push(`/intentions/${encodeURIComponent(row.intentionNo)}`)
}

async function loadCatalog() {
  const [customerResult, productResult] = await Promise.allSettled([listCustomers(), listProducts()])
  const customers = customerResult.status === 'fulfilled' ? customerResult.value : []
  const summaries = productResult.status === 'fulfilled' ? productResult.value : []
  const detailResults = await Promise.allSettled(summaries.map(product => getProduct(product.productId)))
  const products = detailResults
    .filter((result): result is PromiseFulfilledResult<ProductDetail> => result.status === 'fulfilled')
    .map(result => result.value)
  catalog.value = createCatalog(customers, products)
  if (customerResult.status === 'rejected' || productResult.status === 'rejected'
      || detailResults.some(result => result.status === 'rejected')) {
    referenceWarning.value = '部分名称暂时无法补全，页面将显示原始编号。'
  }
}

async function search() {
  loading.value = true
  error.value = ''
  try {
    intentions.value = await listIntentions({ customerNo: customerNo.value, status: status.value })
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '办理意向列表加载失败'
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  customerNo.value = ''
  status.value = undefined
  search()
}

onMounted(() => {
  loadCatalog()
  search()
})
</script>

<template>
  <main class="content-page">
    <header>
      <p class="eyebrow">BUSINESS INTENTIONS</p>
      <h1>办理意向</h1>
      <p>跟踪由 AI 工作台创建的客户办理意向。</p>
    </header>
    <section class="data-card">
      <div class="filters intention-filters">
        <el-input v-model="customerNo" clearable placeholder="客户编号" @keyup.enter="search" />
        <el-select v-model="status" clearable placeholder="全部状态">
          <el-option v-for="(label, code) in statusLabels" :key="code" :label="label" :value="Number(code)" />
        </el-select>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
      <el-alert v-if="referenceWarning" :title="referenceWarning" type="warning" show-icon :closable="false" class="page-alert" />
      <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" class="page-alert" />
      <el-table v-else v-loading="loading" :data="intentions" @row-click="openIntention">
        <el-table-column prop="intentionNo" label="意向编号" min-width="220" />
        <el-table-column label="客户"><template #default="{ row }">{{ catalog.customerName(row.customerNo) }}</template></el-table-column>
        <el-table-column label="意向金额"><template #default="{ row }">{{ formatMoneyShort(row.totalAmountInCents) }}</template></el-table-column>
        <el-table-column label="状态" width="110"><template #default="{ row }"><el-tag>{{ statusLabels[row.status] ?? row.status }}</el-tag></template></el-table-column>
        <el-table-column label="创建时间" min-width="170"><template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template></el-table-column>
        <el-table-column label="操作" width="100"><template #default><el-button link type="primary">查看</el-button></template></el-table-column>
      </el-table>
    </section>
  </main>
</template>

<style scoped>
.intention-filters { grid-template-columns: 260px 180px 90px 90px; }
.page-alert { margin-bottom: 16px; }
</style>

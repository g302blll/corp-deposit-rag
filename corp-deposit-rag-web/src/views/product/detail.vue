<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProduct, type ProductDetail } from '@/api/product'
import { formatMoneyShort } from '@/utils/money'

const route = useRoute()
const router = useRouter()
const product = ref<ProductDetail | null>(null)
const loading = ref(false)
const error = ref('')
const typeLabels: Record<string, string> = {
  DEMAND: '活期存款', TIME: '定期存款', NOTICE: '通知存款', LARGE_CERTIFICATE: '大额存单'
}

onMounted(async () => {
  loading.value = true
  try {
    product.value = await getProduct(String(route.params.id))
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '产品详情加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <main class="content-page">
    <header>
      <p class="eyebrow">PRODUCT DETAIL</p>
      <h1>产品详情</h1>
      <p>查看产品基本信息、可办理期限与最低金额要求。</p>
    </header>
    <section class="data-card" v-loading="loading">
      <el-button link type="primary" @click="router.push('/products')">← 返回产品中心</el-button>
      <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" />
      <template v-else-if="product">
        <el-descriptions :column="4" border class="detail-summary">
          <el-descriptions-item label="产品编码">{{ product.productCode }}</el-descriptions-item>
          <el-descriptions-item label="产品名称">{{ product.productName }}</el-descriptions-item>
          <el-descriptions-item label="产品类型">{{ typeLabels[product.depositType] ?? product.depositType }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag type="success">启用</el-tag></el-descriptions-item>
        </el-descriptions>
        <h2>可办理期限</h2>
        <el-table :data="product.terms">
          <el-table-column prop="termCode" label="期限编码" />
          <el-table-column prop="termName" label="期限名称" />
          <el-table-column prop="termDays" label="期限天数" />
          <el-table-column label="最低起存金额"><template #default="{ row }">{{ formatMoneyShort(row.minOpenAmountInCents) }}</template></el-table-column>
          <el-table-column label="最低留存金额"><template #default="{ row }">{{ formatMoneyShort(row.minRetainAmountInCents) }}</template></el-table-column>
          <el-table-column label="通知天数"><template #default="{ row }">{{ row.noticeDays ?? '—' }}</template></el-table-column>
        </el-table>
      </template>
    </section>
  </main>
</template>

<style scoped>
.detail-summary { margin: 20px 0 26px; }
h2 { margin: 0 0 14px; font-size: 18px; }
</style>

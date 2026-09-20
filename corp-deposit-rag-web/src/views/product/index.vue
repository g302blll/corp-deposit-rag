<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listProducts, type ProductListItem } from '@/api/product'

const router = useRouter()
const products = ref<ProductListItem[]>([])
const loading = ref(false)
const error = ref('')
const typeLabels: Record<string, string> = {
  DEMAND: '活期存款', TIME: '定期存款', NOTICE: '通知存款', LARGE_CERTIFICATE: '大额存单'
}

function typeLabel(type: string) {
  return typeLabels[type] ?? type
}

function openProduct(row: ProductListItem) {
  router.push(`/products/${encodeURIComponent(row.productId)}`)
}

onMounted(async () => {
  loading.value = true
  try {
    products.value = await listProducts()
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '产品列表加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <main class="content-page">
    <header>
      <p class="eyebrow">PRODUCT CENTER</p>
      <h1>产品中心</h1>
      <p>查看当前可用的对公存款产品，产品信息为只读。</p>
    </header>
    <section class="data-card">
      <el-alert v-if="error" :title="error" type="error" show-icon :closable="false" />
      <el-table v-else v-loading="loading" :data="products" @row-click="openProduct">
        <el-table-column prop="productCode" label="产品编码" />
        <el-table-column prop="productName" label="产品名称" />
        <el-table-column label="产品类型">
          <template #default="{ row }">{{ typeLabel(row.depositType) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="110">
          <template #default><el-button link type="primary">查看详情</el-button></template>
        </el-table-column>
      </el-table>
    </section>
  </main>
</template>

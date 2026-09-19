<script setup lang="ts">
import { onMounted, ref } from 'vue'; import { listProducts, type ProductListItem } from '@/api/product'
const products = ref<ProductListItem[]>([]); onMounted(async () => { products.value = await listProducts() })
</script>
<template><main class="content-page"><header><p class="eyebrow">PRODUCT CENTER</p><h1>产品中心</h1><p>查看当前可用的对公存款产品，产品信息为只读。</p></header><section class="data-card"><el-table :data="products"><el-table-column prop="productCode" label="产品编码" /><el-table-column prop="productName" label="产品名称" /><el-table-column prop="type" label="产品类型" /><el-table-column prop="currency" label="币种" /><el-table-column label="状态"><template #default><el-tag type="success">启用</el-tag></template></el-table-column></el-table></section></main></template>

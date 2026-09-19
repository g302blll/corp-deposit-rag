<script setup lang="ts">
import type { CustomerSummary, DepositPlan } from '@/types/assistant'
import { formatMoney } from '@/utils/money'
import { formatRate } from '@/utils/rate'
defineProps<{ plan: DepositPlan | null; customer: CustomerSummary | null }>()
const visible = defineModel<boolean>({ required: true })
</script>
<template>
  <el-drawer v-model="visible" title="方案依据与试算明细" size="540px">
    <template v-if="plan">
      <div class="evidence-section"><h3>客户准入依据</h3><p>{{ customer?.customerName }}（{{ customer?.largeCategoryName }} / {{ customer?.smallCategoryName }}）</p><el-tag type="success">准入校验通过</el-tag></div>
      <div v-for="detail in plan.details" :key="detail.productTermId" class="evidence-section">
        <h3>{{ detail.productName }}</h3>
        <dl class="evidence-grid"><div><dt>产品期限</dt><dd>{{ detail.termName }}（{{ detail.termDays }}天）</dd></div><div><dt>执行利率</dt><dd>{{ formatRate(detail.interestRate) }}</dd></div><div><dt>试算本金</dt><dd>{{ formatMoney(detail.amountInCents) }}</dd></div><div><dt>预计收益</dt><dd>{{ formatMoney(detail.expectedInterestInCents) }}</dd></div><div><dt>币种</dt><dd>{{ detail.currencyCode }} / CNY</dd></div><div><dt>利率状态</dt><dd>当前有效</dd></div></dl>
      </div>
      <el-alert title="以上准入、利率与试算结果均来自后端确定性业务服务，前端仅负责展示。" type="info" :closable="false" />
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import type { DepositPlan } from '@/types/assistant'
import { formatMoney, formatMoneyShort } from '@/utils/money'
import { formatRate } from '@/utils/rate'
defineProps<{ plan: DepositPlan }>()
defineEmits<{ detail: [plan: DepositPlan]; select: [plan: DepositPlan] }>()
</script>
<template>
  <article class="deposit-plan-card">
    <header><div><span>{{ plan.planType === 'YIELD' ? '收益优先' : '流动性优先' }}</span><h3>{{ plan.planName }}</h3></div><strong>{{ formatMoneyShort(plan.totalExpectedInterestInCents) }}<small>预计收益</small></strong></header>
    <div class="plan-summary"><span>总金额 <b>{{ formatMoneyShort(plan.totalAmountInCents) }}</b></span><span>{{ plan.details.length }} 项产品组合</span></div>
    <div v-for="detail in plan.details" :key="detail.productTermId" class="plan-detail-row">
      <div><b>{{ detail.productName }}</b><small>{{ detail.productCode }} · {{ detail.termName }}</small></div>
      <span>{{ formatMoney(detail.amountInCents) }}</span><span class="rate">{{ formatRate(detail.interestRate) }}</span>
    </div>
    <footer><el-button @click="$emit('detail', plan)">查看依据</el-button><el-button type="primary" @click="$emit('select', plan)">选择方案</el-button></footer>
  </article>
</template>

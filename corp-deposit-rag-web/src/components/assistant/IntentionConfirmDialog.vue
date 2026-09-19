<script setup lang="ts">
import { ref, watch } from 'vue'
import { createIntention } from '@/api/assistant'
import type { CustomerSummary, DepositPlan, IntentionResult } from '@/types/assistant'
import { createIdempotencyKey } from '@/utils/idempotency'
import { formatMoney, formatMoneyShort } from '@/utils/money'
import { formatRate } from '@/utils/rate'

const props = defineProps<{ plan: DepositPlan | null; customer: CustomerSummary | null; requirementText: string }>()
const emit = defineEmits<{ success: [result: IntentionResult] }>()
const visible = defineModel<boolean>({ required: true })
const submitting = ref(false)
const error = ref('')
const idempotencyKey = ref('')
watch(visible, value => { if (value && !idempotencyKey.value) idempotencyKey.value = createIdempotencyKey() })

async function submit() {
  if (!props.plan || !props.customer) return
  submitting.value = true; error.value = ''
  try {
    const result = await createIntention({
      idempotencyKey: idempotencyKey.value,
      customerNo: props.customer.customerNo,
      requirementText: props.requirementText,
      details: props.plan.details.map(detail => ({ productId: detail.productId, productTermId: detail.productTermId, amountInCents: detail.amountInCents, interestRate: detail.interestRate, expectedInterestInCents: detail.expectedInterestInCents, currencyCode: detail.currencyCode }))
    })
    visible.value = false; idempotencyKey.value = ''; emit('success', result)
  } catch (cause) { error.value = cause instanceof Error ? cause.message : '创建办理意向失败' }
  finally { submitting.value = false }
}
</script>
<template>
  <el-dialog v-model="visible" title="确认创建办理意向" width="640px" :close-on-click-modal="false">
    <template v-if="plan && customer">
      <el-alert title="本操作仅创建办理意向，不会直接执行真实存款交易。" type="warning" :closable="false" show-icon />
      <div class="confirm-head"><div><small>客户</small><b>{{ customer.customerName }}</b></div><div><small>总金额</small><b>{{ formatMoneyShort(plan.totalAmountInCents) }}</b></div><div><small>预计收益</small><b>{{ formatMoney(plan.totalExpectedInterestInCents) }}</b></div></div>
      <div v-for="detail in plan.details" :key="detail.productTermId" class="confirm-detail"><span>{{ detail.productName }} · {{ detail.termName }}</span><b>{{ formatRate(detail.interestRate) }}</b></div>
      <el-alert v-if="error" :title="error" type="error" :closable="false" />
    </template>
    <template #footer><el-button @click="visible = false">取消</el-button><el-button type="primary" :loading="submitting" @click="submit">确认创建意向</el-button></template>
  </el-dialog>
</template>

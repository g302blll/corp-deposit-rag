import { request } from './request'
import type { CreateIntentionRequest, DepositPlan, IntentionResult, PlanRequest, PlanResponse } from '@/types/assistant'

interface BackendPlan {
  productId: number | string
  productCode: string
  productName: string
  depositType: string
  productTermId: number | string
  termCode: string
  termName: string
  termDays: number
  principalInCents: number
  interestRate: number
  expectedInterestInCents: number
  currencyCode: string
}

interface BackendPlanResponse {
  customer: PlanResponse['customer']
  requirement: {
    amountInCents: number
    preferredDays?: number
    liquidityPreference?: string
    currencyCode: string
  }
  plans: BackendPlan[]
  explanation: string
}

export function adaptPlanResponse(raw: BackendPlanResponse): PlanResponse {
  const plans: DepositPlan[] = raw.plans.map((plan, index) => ({
    planId: `plan-${plan.productId}-${plan.productTermId}`,
    planName: `方案${String.fromCharCode(65 + index)}｜${plan.productName}`,
    planType: raw.requirement.liquidityPreference === 'HIGH' ? 'LIQUIDITY' : 'YIELD',
    description: `${plan.termName}存款方案`,
    totalAmountInCents: plan.principalInCents,
    totalExpectedInterestInCents: plan.expectedInterestInCents,
    details: [{
      ...plan,
      productId: String(plan.productId),
      productTermId: String(plan.productTermId),
      amountInCents: plan.principalInCents,
      eligible: true
    }]
  }))
  return {
    customer: raw.customer,
    extractedRequirement: {
      totalAmountInCents: raw.requirement.amountInCents,
      expectedDays: raw.requirement.preferredDays,
      liquidityLevel: raw.requirement.liquidityPreference,
      currencyCode: raw.requirement.currencyCode
    },
    message: raw.explanation,
    plans
  }
}

export async function getPlans(payload: PlanRequest): Promise<PlanResponse> {
  const baseURL = import.meta.env.VITE_ASSISTANT_API || ''
  const { data } = await request.post<BackendPlanResponse>('/api/v1/assistant/plans', payload, { baseURL })
  return adaptPlanResponse(data)
}

export async function createIntention(payload: CreateIntentionRequest): Promise<IntentionResult> {
  const baseURL = import.meta.env.VITE_ASSISTANT_API || ''
  const { data } = await request.post<IntentionResult>('/api/v1/assistant/intentions', payload, { baseURL })
  return data
}

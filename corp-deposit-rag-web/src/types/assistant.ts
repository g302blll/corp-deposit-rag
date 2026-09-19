export interface CustomerSummary {
  customerNo: string
  customerName: string
  largeCategoryCode: number
  largeCategoryName: string
  smallCategoryCode: number
  smallCategoryName: string
  level?: string
  managerName?: string
}

export interface DepositRequirement {
  totalAmountInCents: number
  expectedDays?: number
  liquidityLevel?: string
  currencyCode: string
}

export interface DepositPlanDetail {
  productId: string
  productCode: string
  productName: string
  depositType: string
  productTermId: string
  termCode: string
  termName: string
  termDays: number
  currencyCode: string
  amountInCents: number
  interestRate: number
  expectedInterestInCents: number
  eligible: boolean
}

export interface DepositPlan {
  planId: string
  planName: string
  planType: 'YIELD' | 'LIQUIDITY' | 'BALANCED'
  description: string
  totalAmountInCents: number
  totalExpectedInterestInCents: number
  details: DepositPlanDetail[]
}

export interface PlanRequest { customerNo?: string; message: string }
export interface PlanResponse {
  customer: CustomerSummary
  extractedRequirement: DepositRequirement
  message: string
  plans: DepositPlan[]
}

export interface CreateIntentionRequest {
  idempotencyKey: string
  customerNo: string
  requirementText: string
  details: Array<Pick<DepositPlanDetail, 'productId' | 'productTermId' | 'amountInCents' | 'interestRate' | 'expectedInterestInCents' | 'currencyCode'>>
}

export interface IntentionResult {
  intentionNo: string
  customerNo: string
  status: number
  detailNos: string[]
}

export type MessageType = 'TEXT' | 'PLAN' | 'ERROR' | 'SYSTEM' | 'INTENTION_RESULT'
export interface ChatMessage {
  id: string
  role: 'USER' | 'ASSISTANT'
  type: MessageType
  text?: string
  plans?: DepositPlan[]
  result?: IntentionResult
  createdAt: string
}

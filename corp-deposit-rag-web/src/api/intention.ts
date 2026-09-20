import { request } from './request'

export interface IntentionListItem {
  intentionNo: string
  customerNo: string
  requirementText?: string | null
  totalAmountInCents: number
  status: number
  sourceChannel: number
  createdAt: string
  updatedAt: string
}

export interface IntentionDetailLine {
  detailNo: string
  productId: string
  productTermId: string
  currencyCode: string
  amountInCents: number
  interestRate: number
  expectedInterestInCents: number
  status: number
  createdAt: string
  updatedAt: string
}

export interface IntentionDetail extends IntentionListItem {
  details: IntentionDetailLine[]
}

export interface IntentionFilters {
  customerNo?: string
  status?: number
}

type RawIntentionDetailLine = Omit<IntentionDetailLine, 'productId' | 'productTermId'> & {
  productId: string | number
  productTermId: string | number
}
type RawIntentionDetail = Omit<IntentionDetail, 'details'> & { details: RawIntentionDetailLine[] }

export async function listIntentions(filters: IntentionFilters = {}): Promise<IntentionListItem[]> {
  const params: IntentionFilters = {}
  const customerNo = filters.customerNo?.trim()
  if (customerNo) params.customerNo = customerNo
  if (filters.status !== undefined) params.status = filters.status
  const { data } = await request.get<IntentionListItem[]>('/business-api/v1/intentions', { params })
  return data
}

export async function getIntention(intentionNo: string): Promise<IntentionDetail> {
  const { data } = await request.get<RawIntentionDetail>(
    `/business-api/v1/intentions/${encodeURIComponent(intentionNo)}`
  )
  return {
    ...data,
    details: data.details.map(detail => ({
      ...detail,
      productId: String(detail.productId),
      productTermId: String(detail.productTermId)
    }))
  }
}

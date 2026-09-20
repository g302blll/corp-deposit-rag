import type { CustomerSummary } from '@/types/assistant'
import { request } from './request'

export async function listCustomers(): Promise<CustomerSummary[]> {
  const { data } = await request.get<CustomerSummary[]>('/customer-api/v1/customers')
  return data
}

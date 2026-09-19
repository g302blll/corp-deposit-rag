import type { CustomerSummary } from '@/types/assistant'

const mockCustomers: CustomerSummary[] = [
  { customerNo: 'CUST001', customerName: '华东建设集团有限公司', largeCategoryCode: 3, largeCategoryName: '国有企业', smallCategoryCode: 301, smallCategoryName: '国有独资/控股企业', level: 'A', managerName: '张经理' },
  { customerNo: 'CUST002', customerName: '滨江市人民医院', largeCategoryCode: 2, largeCategoryName: '事业单位', smallCategoryCode: 201, smallCategoryName: '医院及科研事业单位', level: 'A', managerName: '张经理' }
]

export async function listCustomers(): Promise<CustomerSummary[]> {
  return Promise.resolve(mockCustomers)
}

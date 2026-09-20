import { describe, expect, it } from 'vitest'
import type { CustomerSummary } from '@/types/assistant'
import type { ProductDetail } from '@/api/product'
import { createCatalog } from './catalog'

describe('catalog aggregation', () => {
  it('resolves customer, product and term names', () => {
    const customers: CustomerSummary[] = [{
      customerNo: 'CUST001', customerName: '华星科技有限公司',
      largeCategoryCode: 4, largeCategoryName: '民营企业',
      smallCategoryCode: 401, smallCategoryName: '科技型民营企业'
    }]
    const products: ProductDetail[] = [{
      productId: '2', productCode: 'TIME', productName: '单位定期存款', depositType: 'TIME', status: 1,
      terms: [{ productTermId: '23', termCode: '1Y', termName: '一年', termDays: 365,
        minOpenAmountInCents: 1_000_000, minRetainAmountInCents: 1_000_000, noticeDays: null }]
    }]

    const catalog = createCatalog(customers, products)

    expect(catalog.customerName('CUST001')).toBe('华星科技有限公司')
    expect(catalog.productName('2')).toBe('单位定期存款')
    expect(catalog.termName('23')).toBe('一年')
  })

  it('falls back to raw ids and does not mutate inputs', () => {
    const customers: CustomerSummary[] = []
    const products: ProductDetail[] = []
    const customerSnapshot = JSON.stringify(customers)
    const productSnapshot = JSON.stringify(products)

    const catalog = createCatalog(customers, products)

    expect(catalog.customerName('CUST404')).toBe('CUST404')
    expect(catalog.productName('999')).toBe('999')
    expect(catalog.termName('998')).toBe('998')
    expect(JSON.stringify(customers)).toBe(customerSnapshot)
    expect(JSON.stringify(products)).toBe(productSnapshot)
  })

  it('uses the last name for duplicate ids', () => {
    const first: ProductDetail = {
      productId: '2', productCode: 'TIME', productName: '旧名称', depositType: 'TIME', status: 1, terms: []
    }
    const second: ProductDetail = { ...first, productName: '新名称' }

    expect(createCatalog([], [first, second]).productName('2')).toBe('新名称')
  })
})

import { afterEach, describe, expect, it, vi } from 'vitest'
import { listCustomers } from './customer'
import { getIntention, listIntentions } from './intention'
import { getProduct, listProducts } from './product'
import { request } from './request'

afterEach(() => vi.restoreAllMocks())

describe('query APIs', () => {
  it('loads customers through the customer service proxy', async () => {
    const customers = [{
      customerNo: 'CUST001', customerName: '华星科技有限公司',
      largeCategoryCode: 4, largeCategoryName: '民营企业',
      smallCategoryCode: 401, smallCategoryName: '科技型民营企业'
    }]
    const get = vi.spyOn(request, 'get').mockResolvedValue({ data: customers })

    await expect(listCustomers()).resolves.toEqual(customers)
    expect(get).toHaveBeenCalledWith('/customer-api/v1/customers')
  })

  it('loads products and preserves exact string bigint ids', async () => {
    const get = vi.spyOn(request, 'get').mockResolvedValue({ data: [{
      productId: '9007199254740993', productCode: 'TIME', productName: '单位定期存款',
      depositType: 'TIME', status: 1
    }] })

    const products = await listProducts()

    expect(get).toHaveBeenCalledWith('/product-api/v1/products')
    expect(products[0]?.productId).toBe('9007199254740993')
  })

  it('loads an encoded product detail and adapts numeric ids', async () => {
    const get = vi.spyOn(request, 'get').mockResolvedValue({ data: {
      productId: 2, productCode: 'TIME', productName: '单位定期存款', depositType: 'TIME', status: 1,
      terms: [{
        productTermId: 23, termCode: '1Y', termName: '一年', termDays: 365,
        minOpenAmountInCents: 1_000_000, minRetainAmountInCents: 1_000_000, noticeDays: null
      }]
    } })

    const product = await getProduct('TIME/2')

    expect(get).toHaveBeenCalledWith('/product-api/v1/products/TIME%2F2')
    expect(product.productId).toBe('2')
    expect(product.terms[0]?.productTermId).toBe('23')
  })

  it('sends only defined intention filters and trims customer numbers', async () => {
    const get = vi.spyOn(request, 'get').mockResolvedValue({ data: [] })

    await listIntentions()
    await listIntentions({ customerNo: ' CUST001 ', status: 1 })
    await listIntentions({ customerNo: '   ' })

    expect(get).toHaveBeenNthCalledWith(1, '/business-api/v1/intentions', { params: {} })
    expect(get).toHaveBeenNthCalledWith(2, '/business-api/v1/intentions', {
      params: { customerNo: 'CUST001', status: 1 }
    })
    expect(get).toHaveBeenNthCalledWith(3, '/business-api/v1/intentions', { params: {} })
  })

  it('loads an encoded intention detail and adapts line ids', async () => {
    const get = vi.spyOn(request, 'get').mockResolvedValue({ data: {
      intentionNo: 'INT001', customerNo: 'CUST001', requirementText: '800万存一年',
      totalAmountInCents: 800_000_000, status: 1, sourceChannel: 1,
      createdAt: '2026-09-20T10:00:00', updatedAt: '2026-09-20T10:00:00',
      details: [{
        detailNo: 'DET001', productId: '9007199254740993', productTermId: 23,
        currencyCode: '001', amountInCents: 800_000_000, interestRate: 15_000,
        expectedInterestInCents: 12_000_000, status: 0,
        createdAt: '2026-09-20T10:00:00', updatedAt: '2026-09-20T10:00:00'
      }]
    } })

    const intention = await getIntention('INT/001')

    expect(get).toHaveBeenCalledWith('/business-api/v1/intentions/INT%2F001')
    expect(intention.details[0]?.productId).toBe('9007199254740993')
    expect(intention.details[0]?.productTermId).toBe('23')
  })

  it('does not swallow normalized request errors', async () => {
    vi.spyOn(request, 'get').mockRejectedValue(new Error('客户不存在'))
    await expect(listCustomers()).rejects.toThrow('客户不存在')
  })
})

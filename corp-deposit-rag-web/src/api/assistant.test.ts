import { describe, expect, it } from 'vitest'
import { adaptPlanResponse } from './assistant'

describe('assistant response adapter', () => {
  it('converts backend bigint ids to strings and wraps details as a plan', () => {
    const result = adaptPlanResponse({
      customer: { customerNo: 'CUST001', customerName: '某企业', largeCategoryCode: 3, largeCategoryName: '企业', smallCategoryCode: 301, smallCategoryName: '国企' },
      requirement: { amountInCents: 800_000_000, preferredDays: 365, liquidityPreference: 'LOW', currencyCode: '001' },
      explanation: '推荐一年期产品',
      plans: [{
        productId: 2, productCode: 'CD001', productName: '单位定期存款', depositType: 'TIME',
        productTermId: 23, termCode: '1Y', termName: '一年', termDays: 365,
        principalInCents: 800_000_000, interestRate: 15_000, expectedInterestInCents: 12_000_000, currencyCode: '001'
      }]
    })

    expect(result.plans).toHaveLength(1)
    const plan = result.plans[0]!
    expect(plan.details).toHaveLength(1)
    expect(plan.details[0]!.productId).toBe('2')
    expect(plan.details[0]!.productTermId).toBe('23')
    expect(plan.totalExpectedInterestInCents).toBe(12_000_000)
  })
})

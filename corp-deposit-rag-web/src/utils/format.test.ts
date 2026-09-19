import { describe, expect, it } from 'vitest'
import { formatMoney, formatMoneyShort } from './money'
import { formatRate } from './rate'

describe('financial display formatting', () => {
  it('formats cents without page-level arithmetic', () => {
    expect(formatMoney(800_000_000)).toContain('8,000,000.00')
    expect(formatMoneyShort(800_000_000)).toBe('800万元')
  })

  it('formats scaled interest rates', () => {
    expect(formatRate(15_000)).toBe('1.50%')
  })
})

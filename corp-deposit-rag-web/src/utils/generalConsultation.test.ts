import { describe, expect, it } from 'vitest'
import { answerGeneralConsultation } from './generalConsultation'

describe('general consultation mock', () => {
  it('answers product questions without inventing customer eligibility', () => {
    const answer = answerGeneralConsultation('有哪些对公存款产品？')
    expect(answer).toContain('单位定期存款')
    expect(answer).toContain('需要先选择客户')
  })

  it('keeps execution rates behind customer-aware business queries', () => {
    expect(answerGeneralConsultation('现在利率是多少？')).toContain('Java 业务服务')
  })
})

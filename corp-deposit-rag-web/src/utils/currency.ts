const currencies: Record<string, { name: string; iso: string; symbol: string }> = {
  '001': { name: '人民币', iso: 'CNY', symbol: '¥' }
}

export function currencyInfo(code: string) {
  return currencies[code] ?? { name: code, iso: code, symbol: '' }
}

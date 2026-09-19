export function formatMoney(cents: number, currency = 'CNY'): string {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency', currency, minimumFractionDigits: 2
  }).format(cents / 100)
}

export function formatMoneyShort(cents: number): string {
  const yuan = cents / 100
  return yuan >= 10_000 ? `${new Intl.NumberFormat('zh-CN', { maximumFractionDigits: 2 }).format(yuan / 10_000)}万元` : formatMoney(cents)
}

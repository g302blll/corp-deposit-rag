export function formatRate(rate: number): string {
  return `${(rate / 10_000).toFixed(2)}%`
}

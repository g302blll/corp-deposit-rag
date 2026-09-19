export interface ProductListItem { productCode: string; productName: string; type: string; currency: string; status: string }
export async function listProducts(): Promise<ProductListItem[]> {
  return [
    { productCode: 'CD-TIME', productName: '单位定期存款', type: '定期', currency: '人民币', status: '启用' },
    { productCode: 'CD-NOTICE', productName: '单位通知存款', type: '通知', currency: '人民币', status: '启用' }
  ]
}

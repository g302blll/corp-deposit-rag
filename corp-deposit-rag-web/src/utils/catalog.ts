import type { ProductDetail } from '@/api/product'
import type { CustomerSummary } from '@/types/assistant'

export function createCatalog(customers: CustomerSummary[], products: ProductDetail[]) {
  const customerNames = new Map(customers.map(customer => [customer.customerNo, customer.customerName]))
  const productNames = new Map(products.map(product => [product.productId, product.productName]))
  const termNames = new Map(products.flatMap(product =>
    product.terms.map(term => [term.productTermId, term.termName] as const)
  ))

  return {
    customerName: (customerNo: string) => customerNames.get(customerNo) ?? customerNo,
    productName: (productId: string) => productNames.get(productId) ?? productId,
    termName: (productTermId: string) => termNames.get(productTermId) ?? productTermId
  }
}

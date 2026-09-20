import { request } from './request'

export interface ProductTerm {
  productTermId: string
  termCode: string
  termName: string
  termDays: number
  minOpenAmountInCents: number
  minRetainAmountInCents: number
  noticeDays?: number | null
}

export interface ProductListItem {
  productId: string
  productCode: string
  productName: string
  depositType: string
  status: number
}

export interface ProductDetail extends ProductListItem {
  terms: ProductTerm[]
}

type RawProductListItem = Omit<ProductListItem, 'productId'> & { productId: string | number }
type RawProductTerm = Omit<ProductTerm, 'productTermId'> & { productTermId: string | number }
type RawProductDetail = Omit<ProductDetail, 'productId' | 'terms'> & {
  productId: string | number
  terms: RawProductTerm[]
}

function adaptProduct(raw: RawProductListItem): ProductListItem {
  return { ...raw, productId: String(raw.productId) }
}

export async function listProducts(): Promise<ProductListItem[]> {
  const { data } = await request.get<RawProductListItem[]>('/product-api/v1/products')
  return data.map(adaptProduct)
}

export async function getProduct(productId: string): Promise<ProductDetail> {
  const { data } = await request.get<RawProductDetail>(
    `/product-api/v1/products/${encodeURIComponent(productId)}`
  )
  return {
    ...adaptProduct(data),
    terms: data.terms.map(term => ({ ...term, productTermId: String(term.productTermId) }))
  }
}

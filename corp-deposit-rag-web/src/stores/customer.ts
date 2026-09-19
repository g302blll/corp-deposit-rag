import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { CustomerSummary } from '@/types/assistant'

export const useCustomerStore = defineStore('customer', () => {
  const currentCustomer = ref<CustomerSummary | null>(null)
  function selectCustomer(customer: CustomerSummary) { currentCustomer.value = customer }
  function clearCustomer() { currentCustomer.value = null }
  return { currentCustomer, selectCustomer, clearCustomer }
})

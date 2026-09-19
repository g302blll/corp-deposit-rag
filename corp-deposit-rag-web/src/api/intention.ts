export interface IntentionListItem { intentionNo: string; customerName: string; amount: string; status: string; createdAt: string }
export async function listIntentions(): Promise<IntentionListItem[]> { return [] }

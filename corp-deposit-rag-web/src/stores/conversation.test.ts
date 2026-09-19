import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useConversationStore } from './conversation'

describe('conversation session isolation', () => {
  beforeEach(() => setActivePinia(createPinia()))

  it('increments the session version when customer context resets', () => {
    const store = useConversationStore()
    const version = store.sessionVersion
    store.reset()
    expect(store.sessionVersion).toBe(version + 1)
  })
})

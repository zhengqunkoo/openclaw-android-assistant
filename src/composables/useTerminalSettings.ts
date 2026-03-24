import { computed, ref, watch } from 'vue'

const TTYD_URL_STORAGE_KEY = 'openclaw.ttyd-url.v1'
const TTYD_PARAMS_STORAGE_KEY = 'openclaw.ttyd-params.v1'

export interface TtydParam {
  key: string
  value: string
}

function loadUrl(): string {
  if (typeof window === 'undefined') return ''
  return window.localStorage.getItem(TTYD_URL_STORAGE_KEY) ?? ''
}

function loadParams(): TtydParam[] {
  if (typeof window === 'undefined') return []
  try {
    const raw = window.localStorage.getItem(TTYD_PARAMS_STORAGE_KEY)
    if (!raw) return []
    const parsed = JSON.parse(raw)
    if (Array.isArray(parsed)) return parsed as TtydParam[]
  } catch {
    // Invalid JSON in localStorage — fall back to empty array
  }
  return []
}

// Module-level reactive state shared across all consumers
const ttydBaseUrl = ref<string>(loadUrl())
const ttydParams = ref<TtydParam[]>(loadParams())

watch(ttydBaseUrl, (val) => {
  if (typeof window !== 'undefined') {
    window.localStorage.setItem(TTYD_URL_STORAGE_KEY, val)
  }
})

watch(
  ttydParams,
  (val) => {
    if (typeof window !== 'undefined') {
      window.localStorage.setItem(TTYD_PARAMS_STORAGE_KEY, JSON.stringify(val))
    }
  },
  { deep: true },
)

export function buildTtydUrl(base: string, params: TtydParam[]): string {
  const trimmed = base.trim()
  if (!trimmed) return ''
  const active = params.filter((p) => p.key.trim())
  if (active.length === 0) return trimmed
  const query = active
    .map((p) => `${encodeURIComponent(p.key)}=${encodeURIComponent(p.value)}`)
    .join('&')
  const separator = trimmed.includes('?') ? '&' : '?'
  return `${trimmed}${separator}${query}`
}

export function useTerminalSettings() {
  const ttydUrl = computed(() => buildTtydUrl(ttydBaseUrl.value, ttydParams.value))
  return { ttydBaseUrl, ttydParams, ttydUrl }
}

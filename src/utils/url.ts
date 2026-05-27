const rawApiBaseUrl = (import.meta.env.VITE_API_BASE_URL || '/api/v1') as string
const rawResourceBaseUrl = (import.meta.env.VITE_RESOURCE_BASE_URL || '') as string

const trimTrailingSlash = (value: string) => value.replace(/\/+$/, '')

export const API_BASE_URL = trimTrailingSlash(rawApiBaseUrl)

const deriveOriginFromApiBase = () => {
  try {
    const url = new URL(API_BASE_URL)
    return url.origin
  } catch {
    return ''
  }
}

export const RESOURCE_BASE_URL = trimTrailingSlash(rawResourceBaseUrl || deriveOriginFromApiBase())

export const isAbsoluteUrl = (value: string) => /^(https?:|data:|blob:|file:|capacitor:)/i.test(value)

export const resolveResourceUrl = (value?: string | null) => {
  if (!value) {
    return ''
  }

  if (isAbsoluteUrl(value)) {
    return value
  }

  if (!RESOURCE_BASE_URL) {
    return value
  }

  if (value.startsWith('/')) {
    return `${RESOURCE_BASE_URL}${value}`
  }

  return `${RESOURCE_BASE_URL}/${value}`
}

export const normalizeResourceUrls = <T>(data: T): T => {
  if (Array.isArray(data)) {
    return data.map((item) => normalizeResourceUrls(item)) as T
  }

  if (!data || typeof data !== 'object') {
    return data
  }

  const result: Record<string, unknown> = {}
  for (const [key, value] of Object.entries(data as Record<string, unknown>)) {
    if (typeof value === 'string' && value.startsWith('/download/')) {
      result[key] = resolveResourceUrl(value)
    } else if (Array.isArray(value) || (value && typeof value === 'object')) {
      result[key] = normalizeResourceUrls(value)
    } else {
      result[key] = value
    }
  }

  return result as T
}

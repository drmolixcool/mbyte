import { useMemo } from 'react'
import { createStoreApi } from './storeApi'
import type { TokenProvider } from './fetchWithAuth'

// Now accepts an optional baseUrlOverride so the UI can pass a per-user store base URL.
export function useStoreApi(tokenProvider: TokenProvider, baseUrlOverride?: string) {
  const api = useMemo(() => createStoreApi(tokenProvider, baseUrlOverride ? { baseUrlOverride } : {}), [tokenProvider, baseUrlOverride])

  const wrap = <T extends (...args: any[]) => Promise<any>>(fn: T) => {
    return (async (...args: Parameters<T>): Promise<ReturnType<T>> => {
      try {
        // @ts-ignore
        return await fn(...args)
      } catch (err: any) {
        const msg = err?.message ?? String(err)
        // Dispatch a global event so `App.tsx` can show a toast
        globalThis.dispatchEvent(new CustomEvent('mbyte-toast', { detail: { message: msg } }))
        throw err
      }
    }) as T
  }

  return useMemo(() => {
    const out: any = {}
    Object.keys(api).forEach((k) => {
      const v: any = (api as any)[k]
      if (typeof v === 'function') {
        out[k] = wrap(v.bind(api))
      } else {
        out[k] = v
      }
    })
    return out as typeof api
  }, [api])
}

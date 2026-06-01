import { Capacitor, registerPlugin } from '@capacitor/core'

interface NativeTtsPlugin {
  speak(options: { text: string; rate?: number }): Promise<void>
  stop(): Promise<void>
  getStatus(): Promise<{ ready: boolean; hasEngine: boolean; defaultEngine: string; engineCount: number }>
  openSettings(): Promise<void>
}

const NativeTts = registerPlugin<NativeTtsPlugin>('NativeTts')

export interface NativeTtsResult {
  ok: boolean
  code?: string
  message?: string
}

export const speakNativeTts = async (text: string, rate = 1) => {
  if (Capacitor.getPlatform() !== 'android') {
    return { ok: false, code: 'NOT_ANDROID' } as NativeTtsResult
  }

  try {
    await NativeTts.speak({ text, rate })
    return { ok: true } as NativeTtsResult
  } catch (error: any) {
    return {
      ok: false,
      code: error?.code || 'NATIVE_TTS_FAILED',
      message: error?.message || ''
    } as NativeTtsResult
  }
}

export const stopNativeTts = async () => {
  if (Capacitor.getPlatform() !== 'android') {
    return
  }

  try {
    await NativeTts.stop()
  } catch {
    // Native TTS may be unavailable in browser preview or before plugin init.
  }
}

export const getNativeTtsStatus = async () => {
  if (Capacitor.getPlatform() !== 'android') {
    return null
  }

  try {
    return await NativeTts.getStatus()
  } catch {
    return null
  }
}

export const openNativeTtsSettings = async () => {
  if (Capacitor.getPlatform() !== 'android') {
    return false
  }

  try {
    await NativeTts.openSettings()
    return true
  } catch {
    return false
  }
}

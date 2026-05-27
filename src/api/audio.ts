import request from '../utils/request'
import type { AudioDetail, AudioSettings } from '../types/api'

// GET /api/v1/audio/detail
export const getAudioDetail = (audioId: string): Promise<AudioDetail> => {
  return request.get('/audio/detail', { params: { audioId } }) as Promise<AudioDetail>
}

// POST /api/v1/audio/report-progress
export const reportProgress = (
  audioId: string,
  progress: number,
  isComplete: boolean
): Promise<null> => {
  return request.post('/audio/report-progress', { audioId, progress, isComplete }) as Promise<null>
}

// POST /api/v1/audio/save-settings
export const saveAudioSettings = (settings: AudioSettings): Promise<null> => {
  return request.post('/audio/save-settings', settings) as Promise<null>
}

// GET /api/v1/audio/get-settings
export const getAudioSettings = (): Promise<AudioSettings> => {
  return request.get('/audio/get-settings') as Promise<AudioSettings>
}

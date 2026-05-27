import request from '../utils/request'
import type { AppConfig } from '../types/api'

// GET /api/v1/system/config
export const getAppConfig = (): Promise<AppConfig> => {
  return request.get('/system/config')
}

import request from '../utils/request'
import type { ScenicSpot, ScenicSpotLocation } from '../types/api'

// POST /api/v1/scenic/current
export const fetchCurrentScenic = (latitude: number, longitude: number): Promise<ScenicSpot | null> => {
  return request.post('/scenic/current', { latitude, longitude }) as Promise<ScenicSpot | null>
}

// GET /api/v1/scenic/search
export const searchScenic = (keyword: string): Promise<ScenicSpot[]> => {
  return request.get('/scenic/search', { params: { keyword } }) as Promise<ScenicSpot[]>
}

// GET /api/v1/scenic/list
export const listAllScenic = (): Promise<ScenicSpot[]> => {
  return request.get('/scenic/list') as Promise<ScenicSpot[]>
}

// GET /api/v1/scenic/location/{scenicId}
export const getScenicLocation = (scenicId: string): Promise<ScenicSpotLocation> => {
  return request.get(`/scenic/location/${scenicId}`) as Promise<ScenicSpotLocation>
}

import request from '../utils/request'
import type { AuthResponse, ScenicDetail, AdminScenicItem, AdminUserItem, PaginatedList } from '../types/api'

// POST /api/v1/admin/login
export const adminLoginApi = (username: string, password: string): Promise<AuthResponse> => {
  return request.post('/admin/login', { username, password }) as Promise<AuthResponse>
}

// GET /api/v1/admin/scenic/list
export const getAdminScenicList = (
  keyword?: string,
  page: number = 1,
  pageSize: number = 50
): Promise<PaginatedList<AdminScenicItem>> => {
  return request.get('/admin/scenic/list', { params: { keyword, page, pageSize } }) as Promise<PaginatedList<AdminScenicItem>>
}

// GET /api/v1/admin/scenic/detail/{id}
export const getAdminScenicDetail = (scenicId: string): Promise<ScenicDetail> => {
  return request.get(`/admin/scenic/detail/${scenicId}`) as Promise<ScenicDetail>
}

// PUT /api/v1/admin/scenic
export const updateScenic = (data: {
  id: number
  name: string
  latitude: number
  longitude: number
  radius: number
  description: string
  imageUrl?: string
  audioUrl?: string
}): Promise<null> => {
  return request.put('/admin/scenic', data) as Promise<null>
}

// POST /api/v1/admin/scenic
export const createScenic = (data: {
  name: string
  latitude: number
  longitude: number
  radius: number
  description: string
  imageUrl?: string
  audioUrl?: string
}): Promise<null> => {
  return request.post('/admin/scenic', data) as Promise<null>
}

// POST /api/v1/admin/scenic/upload-image
export const uploadScenicImage = (file: File): Promise<{ url: string }> => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/admin/scenic/upload-image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }) as Promise<{ url: string }>
}

// GET /api/v1/admin/users
export const getAdminUserList = (
  keyword?: string,
  page: number = 1,
  pageSize: number = 50
): Promise<PaginatedList<AdminUserItem>> => {
  return request.get('/admin/users', { params: { keyword, page, pageSize } }) as Promise<PaginatedList<AdminUserItem>>
}

// PUT /api/v1/admin/users/{id}/status
export const updateAdminUserStatus = (
  userId: string,
  status: 1 | 2 | 3
): Promise<null> => {
  return request.put(`/admin/users/${userId}/status`, { status }) as Promise<null>
}

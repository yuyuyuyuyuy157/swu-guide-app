import request from '../utils/request'
import type { AuthResponse, UserInfo } from '../types/api'

// POST /api/v1/user/login
export const loginApi = (phone: string, password: string): Promise<AuthResponse> => {
  return request.post('/user/login', { phone, password }) as Promise<AuthResponse>
}

// POST /api/v1/user/register
export const registerApi = (
  phone: string,
  password: string,
  confirm_password: string,
  invite_code: string
): Promise<AuthResponse> => {
  return request.post('/user/register', { phone, password, confirmPassword: confirm_password, invitationCode: invite_code }) as Promise<AuthResponse>
}

// GET /api/v1/user/info/{userId}
export const getUserProfile = (userId: string): Promise<UserInfo> => {
  return request.get(`/user/info/${userId}`) as Promise<UserInfo>
}

// POST /api/v1/user/change-password
export const changePasswordApi = (
  oldPassword: string,
  newPassword: string,
  confirmNewPassword: string
): Promise<null> => {
  return request.post('/user/change-password', {
    oldPassword,
    newPassword,
    confirmNewPassword
  }) as Promise<null>
}

// ============================================================
// 类型定义 - 严格对齐后端 Java VO/DTO（camelCase）
// ============================================================

// ---- 用户信息 ----
export interface UserInfo {
  userId: string
  phone: string
  avatar: string
  role: 'user' | 'admin'
}

// ---- 登录/注册响应中的播放设置 ----
export interface PlaySettings {
  autoPlay: boolean
  repeatMode: 1 | 2
  playSwitchMode: 1 | 2 | 3
  backgroundPlay: boolean
  playSpeed: number
  backwardForwardDuration: number
}

// ---- 登录/注册响应 ----
export interface AuthResponse {
  token: string
  userId: string
  phone: string
  avatar: string
  role: 'user' | 'admin'
  playSettings: PlaySettings
}

// ---- 景点信息（对齐后端 ScenicSpotVO） ----
export interface ScenicSpot {
  scenicId: string
  name: string
  image: string
  intro: string
  hasAudio: boolean
  audioId?: string
  distance: string
  inductionRange: number
  latitude?: number
  longitude?: number
}

export interface ScenicSpotLocation {
  scenicId: string
  name: string
  latitude: number
  longitude: number
}

// ---- 景点详情（管理员用，对齐后端 ScenicSpotDTO） ----
export interface ScenicDetail {
  id?: number
  name: string
  latitude: number
  longitude: number
  radius?: number
  inductionRange: number
  description?: string
  intro: string
  imageUrl?: string
  image: string
  audioUrl?: string
  audioId?: string
}

// ---- 管理员景点列表项（对齐后端 AdminScenicSpotVO） ----
export interface AdminScenicItem {
  scenicId: string
  name: string
  intro: string
  lastModifier: string
  modifyTime: string
}

export interface AdminUserItem {
  userId: string
  phone: string
  avatar?: string
  role: 'USER' | 'ADMIN' | 'user' | 'admin'
  status: 1 | 2 | 3
  createdAt: string
}

// ---- 音频设置（对齐后端 AudioSettingsDTO） ----
export interface AudioSettings {
  autoPlay: boolean
  repeatMode: 1 | 2
  playSwitchMode: 1 | 2 | 3
  backgroundPlay: boolean
  playSpeed: number
  backwardForwardDuration: number
}

// ---- 音频详情（对齐后端 AudioDetailVO） ----
export interface AudioDetail {
  audioUrl: string
  duration: number
  title: string
  lastProgress: number
}

// ---- 全局配置（对齐后端 SystemController） ----
export interface AppConfig {
  defaultLatitude: number
  defaultLongitude: number
  defaultScenicId: string
  userAgreementUrl: string
  privacyPolicyUrl: string
}

// ---- 通用分页（对齐后端 PageResult） ----
export interface PaginatedList<T> {
  total: number
  records: T[]
}

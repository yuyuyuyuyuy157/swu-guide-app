import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('SWU_TOKEN') || '',
    role: (localStorage.getItem('SWU_ROLE') || '') as 'user' | 'admin' | 'guest' | '',
    userId: localStorage.getItem('SWU_USER_ID') || '',
    phone: localStorage.getItem('SWU_PHONE') || '',
    avatar: localStorage.getItem('SWU_AVATAR') || ''
  }),

  getters: {
    isLoggedIn: (state) => !!state.token && state.role !== 'guest',
    isAdmin: (state) => state.role === 'admin',
    isGuest: (state) => state.role === 'guest'
  },

  actions: {
    loginSuccess(token: string, role: string, userId?: string, phone?: string, avatar?: string) {
      this.token = token
      this.role = role as 'user' | 'admin'
      this.userId = userId || ''
      this.phone = phone || ''
      this.avatar = avatar || ''
      localStorage.setItem('SWU_TOKEN', token)
      localStorage.setItem('SWU_ROLE', role)
      if (userId) localStorage.setItem('SWU_USER_ID', userId)
      if (phone) localStorage.setItem('SWU_PHONE', phone)
      if (avatar) localStorage.setItem('SWU_AVATAR', avatar)
    },

    setGuest() {
      this.role = 'guest'
      this.token = ''
      this.userId = ''
      this.phone = ''
      this.avatar = ''
      localStorage.setItem('SWU_ROLE', 'guest')
      localStorage.removeItem('SWU_TOKEN')
      localStorage.removeItem('SWU_USER_ID')
      localStorage.removeItem('SWU_PHONE')
      localStorage.removeItem('SWU_AVATAR')
    },

    updateProfile(phone: string, avatar: string) {
      this.phone = phone
      this.avatar = avatar
      localStorage.setItem('SWU_PHONE', phone)
      localStorage.setItem('SWU_AVATAR', avatar)
    },

    logout() {
      this.token = ''
      this.role = ''
      this.userId = ''
      this.phone = ''
      this.avatar = ''
      localStorage.removeItem('SWU_TOKEN')
      localStorage.removeItem('SWU_ROLE')
      localStorage.removeItem('SWU_USER_ID')
      localStorage.removeItem('SWU_PHONE')
      localStorage.removeItem('SWU_AVATAR')
    }
  }
})

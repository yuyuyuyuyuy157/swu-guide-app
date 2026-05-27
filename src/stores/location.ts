import { defineStore } from 'pinia'

export const useLocationStore = defineStore('location', {
  state: () => ({
    latitude: 29.815,
    longitude: 106.425,
    hasPermission: false,
    currentScenicId: '',
    defaultLatitude: 29.815,
    defaultLongitude: 106.425,
    defaultScenicId: '',
    userAgreementUrl: '',
    privacyPolicyUrl: ''
  }),

  actions: {
    updateLocation(lat: number, lng: number) {
      this.latitude = lat
      this.longitude = lng
      this.hasPermission = true
    },

    setConfig(config: {
      defaultLatitude: number
      defaultLongitude: number
      defaultScenicId: string
      userAgreementUrl: string
      privacyPolicyUrl: string
    }) {
      this.defaultLatitude = config.defaultLatitude
      this.defaultLongitude = config.defaultLongitude
      this.defaultScenicId = config.defaultScenicId
      this.userAgreementUrl = config.userAgreementUrl
      this.privacyPolicyUrl = config.privacyPolicyUrl
      if (!this.hasPermission) {
        this.latitude = config.defaultLatitude
        this.longitude = config.defaultLongitude
      }
    }
  }
})

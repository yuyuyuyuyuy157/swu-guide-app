<template>
  <div class="map-page">
    <div id="map-container" class="map-container"></div>

    <div v-if="showLocationBanner" class="location-banner">
      <van-icon name="warning-o" color="#fff" size="16" />
      <span>无法获取定位，请在系统设置中开启定位权限</span>
      <van-icon name="cross" color="#fff" size="16" class="banner-close" @click="showLocationBanner = false" />
    </div>

    <ScenicCard :selected-spot="selectedSpot" @open-admin="handleOpenAdmin" />

    <div class="top-shell">
      <div class="floating-avatar" @click="openProfile">
        <van-image round width="44px" height="44px" :src="avatarSrc" :class="{ 'admin-avatar': authStore.isAdmin }" />
      </div>

      <div class="floating-search" @click="openSearch">
        <van-icon name="search" size="18" color="#176b87" />
        <span>搜索校园景点</span>
      </div>
    </div>

    <div class="floating-setting" @click="openSettings">
      <van-icon name="setting-o" size="24" color="#fff" />
    </div>

    <van-popup v-model:show="showProfile" position="left" :style="{ width: '75%', height: '100%' }">
      <div class="profile-drawer">
        <div class="profile-header">
          <van-image round width="64px" height="64px" :src="avatarSrc" />
          <h3 class="phone-text">{{ maskedPhone }}</h3>
          <van-tag v-if="authStore.isAdmin" type="danger">管理员</van-tag>
          <van-tag v-else-if="authStore.isGuest" type="default">游客模式</van-tag>
          <van-tag v-else type="primary" color="#39a9ed">普通用户</van-tag>
        </div>

        <div class="profile-menu">
          <van-cell v-if="authStore.isAdmin" title="景点管理" icon="orders-o" is-link @click="router.push('/admin/scenic-list')" />
          <van-cell v-if="authStore.isAdmin" title="用户管理" icon="friends-o" is-link @click="router.push('/admin/users')" />
          <van-cell v-if="authStore.isGuest" title="去登录 / 注册" icon="user-circle-o" is-link @click="router.push('/login')" />
          <van-cell v-else title="修改密码" icon="lock" is-link @click="showPasswordModal = true" />
          <van-cell v-if="!authStore.isGuest" title="退出登录" icon="revoke" is-link @click="handleLogout" />
        </div>
      </div>
    </van-popup>

    <van-popup v-model:show="showSettings" position="bottom" round :style="{ height: '75%' }">
      <AudioSettings />
    </van-popup>

    <AdminDrawer v-model:show="showAdminDrawer" :spotData="currentEditSpot" />
    <SearchPanel v-model:show="showSearch" @select="handleSearchSelect" />
    <PasswordModal v-model:show="showPasswordModal" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import AMapLoader from '@amap/amap-jsapi-loader'
import { showConfirmDialog, showToast } from 'vant'
import ScenicCard from '../../components/ScenicCard.vue'
import AudioSettings from '../../components/AudioSettings.vue'
import AdminDrawer from '../../components/AdminDrawer.vue'
import SearchPanel from '../../components/SearchPanel.vue'
import PasswordModal from '../../components/PasswordModal.vue'
import { useLocationStore } from '../../stores/location'
import { useAuthStore } from '../../stores/auth'
import { useAudioStore } from '../../stores/audio'
import { getUserProfile } from '../../api/user'
import { getAppConfig } from '../../api/system'
import { getScenicLocation } from '../../api/scenic'

const router = useRouter()
const locationStore = useLocationStore()
const authStore = useAuthStore()
const audioStore = useAudioStore()

const showProfile = ref(false)
const showSettings = ref(false)
const showAdminDrawer = ref(false)
const showSearch = ref(false)
const showPasswordModal = ref(false)
const showLocationBanner = ref(false)
const currentEditSpot = ref<any>(null)
const selectedSpot = ref<any>(null)

const defaultAvatar = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
const avatarSrc = computed(() => authStore.avatar || defaultAvatar)
const maskedPhone = computed(() => (authStore.isGuest ? '未登录' : authStore.phone || '***'))

let map: any = null
let geolocation: any = null
let spotMarker: any = null
let locationWatchId: number | null = null

const openProfile = () => {
  showProfile.value = true
}

const openSettings = () => {
  showSettings.value = true
}

const openSearch = () => {
  if (!authStore.isLoggedIn) {
    showConfirmDialog({
      title: '需要登录后使用搜索',
      message: '登录后可搜索景点、自动定位到景点并触发讲解',
      confirmButtonText: '去登录',
      cancelButtonText: '暂不登录'
    }).then(() => router.push('/login')).catch(() => {})
    return
  }
  showSearch.value = true
}

const handleSearchSelect = async (spot: any) => {
  let lat = spot.latitude ?? spot.lat
  let lng = spot.longitude ?? spot.lng

  if ((lat == null || lng == null) && spot.scenicId) {
    try {
      const location = await getScenicLocation(spot.scenicId)
      lat = location.latitude
      lng = location.longitude
    } catch {
      // keep fallback below
    }
  }

  selectedSpot.value = { ...spot, latitude: lat, longitude: lng, distance: '0' }

  if (map && lat != null && lng != null) {
    map.setCenter([lng, lat])
    map.setZoom(18)
    if (spotMarker) {
      spotMarker.setPosition([lng, lat])
    }
    locationStore.updateLocation(lat, lng)
    showToast(`已定位到：${spot.name}`)
  } else {
    showToast('该景点暂无可用坐标，无法定位')
  }
}

const handleOpenAdmin = (spotData: any) => {
  currentEditSpot.value = spotData
  showAdminDrawer.value = true
}

const handleLogout = () => {
  showConfirmDialog({
    title: '退出登录',
    message: '确定退出当前账号吗？'
  }).then(() => {
    authStore.logout()
    authStore.setGuest()
    showProfile.value = false
    showToast('已退出登录，当前为游客模式')
  }).catch(() => {})
}

;(window as any)._AMapSecurityConfig = {
  securityJsCode: 'c88b8e54bede34a6c5414091a3a4aae2'
}

const getMapStyleByRole = (role: string) => (role === 'guest' ? 'amap://styles/light' : 'amap://styles/normal')

onMounted(() => {
  getAppConfig().then((config) => locationStore.setConfig(config)).catch(() => {})

  if (authStore.isLoggedIn) {
    getUserProfile(authStore.userId).then((profile) => {
      authStore.updateProfile(profile.phone, profile.avatar)
    }).catch(() => {})
    audioStore.fetchSettings()
  }

  AMapLoader.load({
    key: '96d2fa132d6d1be0b15be4a4351ebf6c',
    version: '2.0',
    plugins: ['AMap.Geolocation']
  }).then((AMap) => {
    map = new AMap.Map('map-container', {
      viewMode: '2D',
      zoom: 16,
      center: [locationStore.longitude, locationStore.latitude],
      mapStyle: getMapStyleByRole(authStore.role)
    })

    geolocation = new AMap.Geolocation({
      enableHighAccuracy: true,
      timeout: 10000,
      position: 'RB',
      buttonOffset: new AMap.Pixel(10, 140),
      zoomToAccuracy: true,
      markerOptions: {
        content: '<div style="width: 18px; height: 18px; background-color: #1989fa; border-radius: 50%; border: 3px solid white; box-shadow: 0 0 8px rgba(25, 137, 250, 0.6);"></div>',
        offset: new AMap.Pixel(-9, -9)
      },
      circleOptions: {
        fillColor: 'rgba(25, 137, 250, 0.1)',
        strokeColor: 'rgba(25, 137, 250, 0.2)',
        strokeWeight: 1
      }
    })
    map.addControl(geolocation)

    spotMarker = new AMap.Marker({
      position: [locationStore.longitude, locationStore.latitude],
      offset: new AMap.Pixel(-12, -12),
      content: '<div class="spot-marker"></div>'
    })
    map.add(spotMarker)

    watch(
      [() => audioStore.isPlaying, () => audioStore.currentSpot],
      ([isPlaying, spot]) => {
        if (spotMarker) {
          spotMarker.setContent(
            isPlaying && spot ? '<div class="spot-marker playing-ripple"></div>' : '<div class="spot-marker"></div>'
          )
        }
      },
      { immediate: true }
    )

    geolocation.getCurrentPosition((status: string, result: any) => {
      if (status === 'complete') {
        locationStore.updateLocation(result.position.lat, result.position.lng)
        showLocationBanner.value = false
      } else {
        showLocationBanner.value = true
      }
    })
  }).catch(() => {
    showLocationBanner.value = true
  })

  if (navigator.geolocation) {
    locationWatchId = navigator.geolocation.watchPosition(
      (pos) => {
        locationStore.updateLocation(pos.coords.latitude, pos.coords.longitude)
      },
      () => {},
      { enableHighAccuracy: true, maximumAge: 5000, timeout: 10000 }
    )
  }
})

watch(() => authStore.role, (newRole) => {
  if (map) {
    map.setMapStyle(getMapStyleByRole(newRole))
  }
})

onUnmounted(() => {
  map?.destroy()
  audioStore.pause()
  if (locationWatchId !== null && navigator.geolocation) {
    navigator.geolocation.clearWatch(locationWatchId)
  }
})
</script>

<style scoped>
.map-page { position: relative; width: 100%; height: 100vh; overflow: hidden; }
.map-container { width: 100%; height: 100vh; }
.location-banner { position: absolute; top: 0; left: 0; right: 0; z-index: 200; background: rgba(216,66,66,0.94); color: #fff; padding: 12px var(--space-md); display: flex; align-items: center; gap: var(--space-sm); font-size: var(--font-sm); backdrop-filter: blur(10px); }
.banner-close { cursor: pointer; margin-left: auto; }
.top-shell { position: absolute; top: 42px; left: var(--space-md); right: var(--space-md); z-index: 100; display: grid; grid-template-columns: 50px 1fr; gap: 10px; align-items: center; }
.floating-avatar { box-shadow: var(--shadow-md); border-radius: 50%; background: rgba(255,255,255,0.92); backdrop-filter: blur(14px); padding: 3px; cursor: pointer; display: flex; justify-content: center; align-items: center; border: 1px solid rgba(255,255,255,0.85); }
.admin-avatar { border-color: var(--color-danger) !important; }
.floating-search { height: 46px; background: rgba(255,255,255,0.94); backdrop-filter: blur(14px); border-radius: var(--radius-full); box-shadow: var(--shadow-md); display: flex; align-items: center; gap: 8px; padding: 0 var(--space-md); cursor: pointer; border: 1px solid rgba(255,255,255,0.85); color: var(--color-text-secondary); font-size: var(--font-sm); font-weight: 500; }
.floating-setting { position: absolute; top: 45%; right: var(--space-md); z-index: 100; width: 48px; height: 48px; background: var(--gradient-primary); border-radius: 50%; display: flex; justify-content: center; align-items: center; box-shadow: var(--shadow-float); cursor: pointer; backdrop-filter: blur(4px); transition: transform var(--duration-fast); }
.floating-setting:active { transform: scale(0.9); }
.profile-drawer { display: flex; flex-direction: column; height: 100%; background: var(--color-bg); }
.profile-header { padding: 64px var(--space-lg) var(--space-xl); background: var(--gradient-primary); color: white; text-align: center; }
.phone-text { margin: 12px 0 var(--space-sm); font-size: var(--font-lg); font-weight: 600; letter-spacing: 1px; }
.profile-menu { flex: 1; margin-top: var(--space-sm); }
</style>

<style>
.spot-marker {
  width: 24px;
  height: 24px;
  background-color: #1989fa;
  border-radius: 50%;
  border: 3px solid white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.3);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}
@keyframes mapRipple {
  0% { box-shadow: 0 0 0 0 rgba(7, 193, 96, 0.6); }
  70% { box-shadow: 0 0 0 16px rgba(7, 193, 96, 0); }
  100% { box-shadow: 0 0 0 0 rgba(7, 193, 96, 0); }
}
.playing-ripple {
  animation: mapRipple 1.6s infinite !important;
  background-color: #07c160 !important;
  transform: scale(1.08);
}
</style>

<template>
  <div v-if="visible" class="scenic-card">
    <button class="card-close" type="button" @click.stop="closeCard">
      <van-icon name="cross" size="16" color="#6f7c80" />
    </button>

    <div v-if="loading" class="loading-box">
      <van-loading type="spinner" color="#1989fa" />
      <span class="loading-text">正在查找附近景点...</span>
    </div>

    <div v-else-if="scenicData" class="card-content" @click="goToDetail">
      <img :src="scenicData.image" alt="景点图片" class="scenic-img" />

      <div class="scenic-info">
        <div class="title-row">
          <h3 class="name">
            {{ scenicData.name }}
            <span v-if="authStore.isAdmin" class="admin-badge">管理</span>
          </h3>
          <span class="distance">距您 {{ scenicData.distance || '?' }}m</span>
        </div>

        <p v-if="!isCurrentPlaying" class="intro">{{ scenicData.intro }}</p>

        <div v-else class="audio-control-panel" @click.stop>
          <div class="time-text">{{ formatTime(audioStore.currentTime) }} / {{ formatTime(audioStore.duration) }}</div>
          <van-slider
            v-model="sliderValue"
            :max="audioStore.duration || 100"
            active-color="#1989fa"
            button-size="16px"
            @change="onSliderChange"
          />
          <div class="skip-btns">
            <van-button size="mini" plain type="primary" @click.stop="audioStore.fastRewind()">
              &lt;&lt; {{ audioStore.settings.backwardForwardDuration }}s
            </van-button>
            <van-button size="mini" plain type="primary" @click.stop="audioStore.fastForward()">
              {{ audioStore.settings.backwardForwardDuration }}s &gt;&gt;
            </van-button>
          </div>
        </div>

        <div class="action-row">
          <van-button
            v-if="authStore.isAdmin"
            round
            type="warning"
            size="small"
            icon="setting-o"
            @click.stop="manageScenic"
          >
            管理此景点
          </van-button>

          <template v-else-if="authStore.isGuest">
            <van-button round type="primary" size="small" icon="user-circle-o" @click.stop="goLoginForAudio">
              登录后讲解
            </van-button>
          </template>

          <template v-else>
            <van-button
              v-if="scenicData.hasAudio"
              round
              :type="isPlaying ? 'success' : 'primary'"
              size="small"
              :icon="isPlaying ? 'pause-circle-o' : 'play-circle-o'"
              @click.stop="playAudio"
            >
              {{ isPlaying ? '播放中' : '听讲解' }}
            </van-button>

            <van-button
              round
              type="primary"
              size="small"
              class="ai-btn"
              :icon="aiSpeaking ? 'pause-circle-o' : 'volume-o'"
              @click.stop="aiPlay"
            >
              {{ aiSpeaking ? '停止讲解' : 'AI 讲解' }}
            </van-button>
          </template>
        </div>
      </div>
    </div>

    <div v-else class="empty-box">
      <van-icon name="location-o" size="24" color="#c8c9cc" />
      <p class="empty-text">附近暂无景点</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { useAuthStore } from '../stores/auth'
import { useLocationStore } from '../stores/location'
import { useAudioStore } from '../stores/audio'
import { fetchCurrentScenic } from '../api/scenic'
import type { ScenicSpot } from '../types/api'

const props = defineProps<{ selectedSpot?: ScenicSpot | null }>()
const emit = defineEmits(['open-admin'])

const router = useRouter()
const authStore = useAuthStore()
const locationStore = useLocationStore()
const audioStore = useAudioStore()

const loading = ref(false)
const visible = ref(true)
const scenicData = ref<ScenicSpot | null>(null)
const sliderValue = ref(0)
const aiSpeaking = ref(false)

let fetchTimer: ReturnType<typeof setTimeout> | null = null
let pollTimer: ReturnType<typeof setInterval> | null = null
let currentUtterance: SpeechSynthesisUtterance | null = null
let keepSpotUntil = 0
let nullMissCount = 0

const isCurrentPlaying = computed(() => audioStore.currentSpot?.scenicId === scenicData.value?.scenicId)
const isPlaying = computed(() => isCurrentPlaying.value && audioStore.isPlaying)

watch(
  () => audioStore.currentTime,
  (newTime) => {
    sliderValue.value = newTime
  }
)

watch(
  () => props.selectedSpot,
  (spot) => {
    if (!spot) {
      return
    }
    applyScenicSpot(spot, true)
  },
  { deep: true }
)

watch(
  () => [locationStore.latitude, locationStore.longitude, locationStore.hasPermission],
  ([lat, lng, hasPermission]) => {
    if (!hasPermission || !lat || !lng) {
      return
    }
    if (fetchTimer) {
      clearTimeout(fetchTimer)
    }
    fetchTimer = setTimeout(() => {
      refreshCurrentScenic(lat as number, lng as number)
    }, 500)
  },
  { immediate: true }
)

const refreshCurrentScenic = async (lat: number, lng: number) => {
  const prevSpotId = scenicData.value?.scenicId
  loading.value = true
  try {
    const data = await fetchCurrentScenic(lat, lng)
    if (data) {
      scenicData.value = data
      visible.value = true
      keepSpotUntil = Date.now() + 120000
      nullMissCount = 0
      if (prevSpotId !== data.scenicId) {
        await autoAnnounce(data, false)
      }
      return
    }

    nullMissCount += 1
    const keepExpired = Date.now() > keepSpotUntil
    if (keepExpired && nullMissCount >= 3 && !isCurrentPlaying.value && !aiSpeaking.value) {
      scenicData.value = null
    }
  } catch {
    // Keep current scenic card when transient request fails.
  } finally {
    loading.value = false
  }
}

const applyScenicSpot = async (spot: ScenicSpot, forceAnnounce: boolean) => {
  if (fetchTimer) {
    clearTimeout(fetchTimer)
  }
  loading.value = false
  visible.value = true
  scenicData.value = spot
  keepSpotUntil = Date.now() + 120000
  nullMissCount = 0
  await autoAnnounce(spot, forceAnnounce)
}

const autoAnnounce = async (spot: ScenicSpot, force: boolean) => {
  if (!force && !audioStore.settings.autoPlay) {
    return
  }
  if (authStore.isGuest) {
    return
  }

  if (spot.hasAudio) {
    const played = await audioStore.playScenicAudio(spot, { autoTrigger: true })
    if (!played) {
      speakIntro(spot)
    }
    return
  }

  speakIntro(spot)
}

const speakIntro = (spot: ScenicSpot) => {
  if (!('speechSynthesis' in window)) {
    showToast('当前浏览器不支持 AI 语音朗读')
    return
  }

  audioStore.pause()
  window.speechSynthesis.cancel()

  const intro = spot.intro?.trim() || '暂无简介'
  currentUtterance = new SpeechSynthesisUtterance(`${spot.name}。${intro}`)
  currentUtterance.lang = 'zh-CN'
  currentUtterance.rate = audioStore.settings.playSpeed || 1
  currentUtterance.pitch = 1
  currentUtterance.onend = () => {
    aiSpeaking.value = false
  }
  currentUtterance.onerror = () => {
    aiSpeaking.value = false
  }

  aiSpeaking.value = true
  window.speechSynthesis.speak(currentUtterance)
}

const formatTime = (seconds: number) => {
  if (!seconds || Number.isNaN(seconds)) {
    return '00:00'
  }
  const m = Math.floor(seconds / 60).toString().padStart(2, '0')
  const s = Math.floor(seconds % 60).toString().padStart(2, '0')
  return `${m}:${s}`
}

const goToDetail = () => {
  if (scenicData.value?.scenicId) {
    router.push(`/detail/${scenicData.value.scenicId}`)
  }
}

const onSliderChange = (value: number | number[]) => {
  audioStore.seek(value as number)
}

const closeCard = () => {
  visible.value = false
}

const playAudio = async () => {
  if (!scenicData.value) {
    return
  }
  await audioStore.playScenicAudio(scenicData.value)
}

const aiPlay = () => {
  if (!scenicData.value) {
    return
  }
  if (aiSpeaking.value) {
    window.speechSynthesis.cancel()
    aiSpeaking.value = false
    return
  }
  speakIntro(scenicData.value)
}

const goLoginForAudio = () => {
  showConfirmDialog({
    title: '请先登录',
    message: '登录后可使用语音讲解与自动播报',
    confirmButtonText: '去登录',
    cancelButtonText: '取消'
  }).then(() => {
    router.push('/login')
  }).catch(() => {})
}

const manageScenic = () => {
  emit('open-admin', scenicData.value)
}

onMounted(() => {
  pollTimer = setInterval(() => {
    if (!locationStore.hasPermission) {
      return
    }
    if (!locationStore.latitude || !locationStore.longitude) {
      return
    }
    refreshCurrentScenic(locationStore.latitude, locationStore.longitude)
  }, 10000)
})

onUnmounted(() => {
  if (fetchTimer) {
    clearTimeout(fetchTimer)
  }
  if (pollTimer) {
    clearInterval(pollTimer)
  }
  if ('speechSynthesis' in window) {
    window.speechSynthesis.cancel()
  }
})
</script>

<style scoped>
.scenic-card { position: absolute; bottom: 24px; left: 14px; right: 14px; background: rgba(255,255,255,0.96); backdrop-filter: blur(18px); -webkit-backdrop-filter: blur(18px); border-radius: var(--radius-xl); padding: 14px; box-shadow: var(--shadow-lg); z-index: 999; min-height: 104px; transition: all 0.3s ease; border: 1px solid rgba(255,255,255,0.88); }
.card-close { position: absolute; top: 8px; right: 10px; z-index: 10; width: 28px; height: 28px; border-radius: 50%; background: rgba(244,247,246,0.94); border: 1px solid rgba(223,232,232,0.9); display: flex; align-items: center; justify-content: center; cursor: pointer; padding: 0; }
.loading-box { display: flex; justify-content: center; align-items: center; height: 80px; gap: 8px; }
.loading-text { color: #999; font-size: 13px; }
.empty-box { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 60px; }
.empty-text { margin-top: 8px; font-size: 12px; color: #bbb; }
.card-content { display: grid; grid-template-columns: 88px 1fr; gap: 12px; align-items: stretch; }
.scenic-img { width: 88px; height: 100%; min-height: 92px; border-radius: var(--radius-md); object-fit: cover; flex-shrink: 0; box-shadow: var(--shadow-sm); }
.scenic-info { flex: 1; display: flex; flex-direction: column; overflow: hidden; min-width: 0; }
.title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; gap: 8px; }
.name { margin: 0; font-size: 16px; font-weight: 700; color: var(--color-text); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; letter-spacing: 0; }
.admin-badge { font-size: 10px; background: #ffe8e8; color: #ee0a24; padding: 1px 5px; border-radius: 4px; margin-left: 4px; vertical-align: text-bottom; flex-shrink: 0; }
.distance { font-size: 11px; color: var(--color-primary); background: var(--color-primary-bg); padding: 3px 8px; border-radius: 12px; font-weight: 600; white-space: nowrap; flex-shrink: 0; }
.intro { margin: 0; font-size: 12px; color: var(--color-text-secondary); display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; line-height: 1.55; }
.audio-control-panel { margin-top: 4px; padding: 8px 10px; background: var(--color-surface-soft); border-radius: var(--radius-md); }
.time-text { font-size: 11px; color: #999; text-align: right; margin-bottom: 4px; font-variant-numeric: tabular-nums; }
.skip-btns { display: flex; justify-content: center; gap: 6px; margin-top: 4px; }
.action-row { margin-top: auto; display: flex; justify-content: flex-end; align-items: center; padding-top: 10px; gap: 6px; flex-wrap: wrap; }
.ai-btn { background: var(--gradient-ai) !important; border: none !important; color: #fff !important; }
</style>

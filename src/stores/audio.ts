import { defineStore } from 'pinia'
import { showLoadingToast, showToast } from 'vant'
import request from '../utils/request'
import { resolveResourceUrl } from '../utils/url'
import type { AudioSettings, ScenicSpot } from '../types/api'

const audio = new Audio()
let heartbeatTimer: ReturnType<typeof setInterval> | null = null

export const useAudioStore = defineStore('audio', {
  state: () => ({
    currentSpot: null as ScenicSpot | null,
    isPlaying: false,
    currentTime: 0,
    duration: 0,
    initialized: false,

    settings: {
      autoPlay: true,
      repeatMode: 1 as 1 | 2,
      playSwitchMode: 1 as 1 | 2 | 3,
      backgroundPlay: false,
      playSpeed: 1.0,
      backwardForwardDuration: 15
    } as AudioSettings,

    playedSpotIds: new Set<string>()
  }),

  actions: {
    initAudio() {
      if (this.initialized) {
        return
      }
      this.initialized = true

      audio.addEventListener('timeupdate', () => {
        this.currentTime = audio.currentTime
      })
      audio.addEventListener('loadedmetadata', () => {
        this.duration = audio.duration
      })
      audio.addEventListener('ended', () => {
        this.isPlaying = false
        this.currentTime = 0
        this.stopHeartbeat()
        if (this.currentSpot?.audioId) {
          request.post('/audio/report-progress', {
            audioId: this.currentSpot.audioId,
            progress: Math.floor(this.duration),
            isComplete: true
          }).catch(() => {})
        }
      })
      audio.addEventListener('error', () => {
        this.isPlaying = false
        this.stopHeartbeat()
        showToast('音频加载失败，请稍后重试')
      })
    },

    async playScenicAudio(spot: ScenicSpot, options?: { autoTrigger?: boolean }): Promise<boolean> {
      if (!spot.hasAudio || !spot.audioId) {
        if (!options?.autoTrigger) {
          showToast('该景点暂无语音讲解')
        }
        return false
      }

      if (this.settings.repeatMode === 1 && this.playedSpotIds.has(spot.scenicId) && options?.autoTrigger) {
        return false
      }

      const sameSpot = this.currentSpot?.scenicId === spot.scenicId && audio.src
      const canResumeCurrentAudio = this.currentTime > 0 && (!this.duration || this.currentTime < this.duration)
      if (sameSpot && (this.isPlaying || canResumeCurrentAudio)) {
        this.togglePlay()
        return true
      }

      this.pause()
      this.currentSpot = spot
      this.currentTime = 0
      this.duration = 0

      const toast = showLoadingToast({ message: '加载语音中...', forbidClick: true })

      try {
        const detailData = (await request.get('/audio/detail', {
          params: { audioId: spot.audioId },
          timeout: 60000
        })) as any

        audio.src = resolveResourceUrl(detailData.audioUrl)
        audio.playbackRate = this.settings.playSpeed

        if (detailData.lastProgress > 0 && detailData.lastProgress < detailData.duration) {
          audio.currentTime = detailData.lastProgress
        } else {
          audio.currentTime = 0
        }

        await audio.play()
        this.isPlaying = true
        this.playedSpotIds.add(spot.scenicId)
        this.startHeartbeat()
        return true
      } catch {
        this.currentSpot = null
        this.isPlaying = false
        if (options?.autoTrigger) {
          showToast('自动播报受系统限制，请点击“听讲解”')
        } else {
          showToast('播放失败，请重试')
        }
        return false
      } finally {
        toast.close()
      }
    },

    togglePlay() {
      if (this.isPlaying) {
        this.pause()
      } else if (audio.src) {
        audio.play().then(() => {
          this.isPlaying = true
          this.startHeartbeat()
        }).catch(() => {
          showToast('播放失败，请重试')
        })
      }
    },

    pause() {
      audio.pause()
      this.isPlaying = false
      this.stopHeartbeat()
    },

    seek(time: number) {
      audio.currentTime = time
      this.currentTime = time
    },

    cycleSpeed() {
      const speedMap: Record<number, number> = {
        0.5: 1.0,
        1.0: 1.2,
        1.2: 1.5,
        1.5: 2.0,
        2.0: 0.5
      }
      const nextSpeed = speedMap[this.settings.playSpeed] || 1.0
      this.settings.playSpeed = nextSpeed
      audio.playbackRate = nextSpeed
      showToast({ message: `已切换为 ${nextSpeed}x`, position: 'top' })
    },

    fastForward() {
      const target = Math.min(this.currentTime + this.settings.backwardForwardDuration, this.duration || 0)
      this.seek(target)
      showToast({ message: `快进 ${this.settings.backwardForwardDuration}s`, position: 'top', duration: 800 })
    },

    fastRewind() {
      const target = Math.max(this.currentTime - this.settings.backwardForwardDuration, 0)
      this.seek(target)
      showToast({ message: `快退 ${this.settings.backwardForwardDuration}s`, position: 'top', duration: 800 })
    },

    startHeartbeat() {
      this.stopHeartbeat()
      heartbeatTimer = setInterval(async () => {
        if (!this.currentSpot?.audioId) {
          return
        }
        try {
          await request.post('/audio/report-progress', {
            audioId: this.currentSpot.audioId,
            progress: Math.floor(this.currentTime),
            isComplete: false
          })
        } catch {
          // Ignore heartbeat failure.
        }
      }, 10000)
    },

    stopHeartbeat() {
      if (heartbeatTimer) {
        clearInterval(heartbeatTimer)
        heartbeatTimer = null
      }
    },

    async fetchSettings() {
      try {
        const settings = await request.get('/audio/get-settings') as any
        if (settings) {
          Object.assign(this.settings, settings)
        }
      } catch {
        // Keep local defaults.
      }
    },

    async persistSettings() {
      try {
        await request.post('/audio/save-settings', { ...this.settings })
      } catch {
        // Ignore temporary persistence failures.
      }
    }
  }
})

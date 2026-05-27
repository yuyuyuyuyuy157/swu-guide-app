<template>
  <div class="settings-panel">
    <div class="panel-header">
      <h3>智能导览设置</h3>
      <p class="subtitle">控制自动播报、切换策略和后台定位</p>
    </div>

    <van-cell-group inset class="master-switch-group">
      <van-cell center title="开启自动触发语音" label="到达景点感应区后自动播放">
        <template #right-icon>
          <van-switch v-model="audioStore.settings.autoPlay" size="24px" @change="onSettingChanged" />
        </template>
      </van-cell>
    </van-cell-group>

    <div :class="audioStore.settings.autoPlay ? 'active-area' : 'disabled-area'">
      <div class="section-title">重复频次策略</div>
      <van-radio-group v-model="audioStore.settings.repeatMode" @change="onSettingChanged">
        <van-cell-group inset>
          <van-cell title="每个景点只播放一次" clickable @click="audioStore.settings.repeatMode = 1">
            <template #right-icon><van-radio :name="1" /></template>
          </van-cell>
          <van-cell title="允许重复触发播放" clickable @click="audioStore.settings.repeatMode = 2">
            <template #right-icon><van-radio :name="2" /></template>
          </van-cell>
        </van-cell-group>
      </van-radio-group>

      <div class="section-title">播报切换策略</div>
      <van-radio-group v-model="audioStore.settings.playSwitchMode" @change="onSettingChanged">
        <van-cell-group inset>
          <van-cell title="播完当前介绍再切换" clickable @click="audioStore.settings.playSwitchMode = 1">
            <template #right-icon><van-radio :name="1" /></template>
          </van-cell>
          <van-cell title="随位置实时打断切换" clickable @click="audioStore.settings.playSwitchMode = 2">
            <template #right-icon><van-radio :name="2" /></template>
          </van-cell>
          <van-cell title="到达新景点弹窗询问" clickable @click="audioStore.settings.playSwitchMode = 3">
            <template #right-icon><van-radio :name="3" /></template>
          </van-cell>
        </van-cell-group>
      </van-radio-group>

      <div class="section-title">快进/快退时长</div>
      <van-cell-group inset>
        <van-cell title="跳过时长（秒）" :value="`${audioStore.settings.backwardForwardDuration}s`">
          <template #right-icon>
            <van-stepper :model-value="audioStore.settings.backwardForwardDuration" min="15" max="60" step="15" @change="onSkipDurationChange" />
          </template>
        </van-cell>
      </van-cell-group>
    </div>

    <div class="section-title">系统级设置</div>
    <van-cell-group inset style="margin-bottom: 30px;">
      <van-cell center title="允许后台播放与定位" label="锁屏时仍可正常追踪导览">
        <template #right-icon>
          <van-switch v-model="audioStore.settings.backgroundPlay" size="24px" @change="handleBgPlayChange" />
        </template>
      </van-cell>
    </van-cell-group>
  </div>
</template>

<script setup lang="ts">
import { useAudioStore } from '../stores/audio'
import { showConfirmDialog, showToast } from 'vant'

const audioStore = useAudioStore()

const handleBgPlayChange = (value: boolean) => {
  if (value) {
    showConfirmDialog({
      title: '系统权限申请',
      message: '需要系统后台定位权限以保证锁屏时正常讲解，请在设置中开启。',
      confirmButtonText: '去开启',
      cancelButtonText: '拒绝'
    }).then(() => {
      showToast('已模拟跳转到手机系统设置页')
      audioStore.persistSettings()
    }).catch(() => {
      audioStore.settings.backgroundPlay = false
    })
  } else {
    audioStore.persistSettings()
  }
}

const onSettingChanged = () => { audioStore.persistSettings() }

const onSkipDurationChange = (value: number | string) => {
  audioStore.settings.backwardForwardDuration = Number(value) as 15 | 30 | 60
  audioStore.persistSettings()
}
</script>

<style scoped>
.settings-panel { padding: 18px 0 28px; background-color: var(--color-bg); min-height: 100%; }
.panel-header { text-align: left; margin: 0 var(--space-md) 18px; padding: 16px; border-radius: var(--radius-lg); background: var(--gradient-primary); color: #fff; box-shadow: var(--shadow-float); }
.panel-header h3 { margin: 0; font-size: 19px; color: #fff; letter-spacing: 0; }
.subtitle { margin: 6px 0 0; font-size: 12px; color: rgba(255,255,255,0.78); }
.section-title { margin: 24px 16px 8px; font-size: 13px; color: var(--color-text-secondary); font-weight: 600; }
.master-switch-group { margin-top: 10px; }
.active-area { opacity: 1; transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1); }
.disabled-area { opacity: 0.4; filter: grayscale(80%); pointer-events: none; transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1); transform: scale(0.98); }
</style>

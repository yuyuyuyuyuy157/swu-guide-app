<template>
  <div class="detail-page">
    <van-nav-bar title="景点详情" left-text="返回" left-arrow @click-left="goBack" fixed placeholder />

    <div v-if="loading" class="loading-state">
      <div class="skeleton-img"></div>
      <div class="skeleton-info">
        <div class="skeleton skeleton-text" style="width: 60%; height: 24px;"></div>
        <div class="skeleton skeleton-text" style="width: 100%; height: 16px; margin-top: 12px;"></div>
        <div class="skeleton skeleton-text" style="width: 100%; height: 16px; margin-top: 8px;"></div>
        <div class="skeleton skeleton-text" style="width: 80%; height: 16px; margin-top: 8px;"></div>
      </div>
    </div>

    <div v-else-if="detail" class="content" style="animation: fadeInUp 0.4s var(--ease-out)">
      <van-swipe v-if="images.length > 0" class="swipe" :autoplay="3000" indicator-color="#1989fa">
        <van-swipe-item v-for="(img, idx) in images" :key="idx">
          <img :src="img" class="swipe-img" @error="onImageError(idx)" />
        </van-swipe-item>
      </van-swipe>
      <img v-else :src="fallbackImage" class="hero-img" />

      <div class="info-card">
        <div class="spot-header">
          <h2 class="spot-name">{{ detail.name }}</h2>
          <van-tag v-if="detail.inductionRange" type="primary" size="medium" round>
            感应范围 {{ detail.inductionRange }}m
          </van-tag>
        </div>
        <div class="divider"></div>
        <p class="spot-intro">{{ detail.intro }}</p>
      </div>
    </div>

    <van-empty v-else description="景点信息加载失败" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listAllScenic } from '../../api/scenic'
import type { ScenicSpot } from '../../types/api'

const router = useRouter()
const route = useRoute()
const scenicId = route.params.id as string
const loading = ref(true)
const detail = ref<ScenicSpot | null>(null)
const images = ref<string[]>([])
const fallbackImage = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'

const goBack = () => router.back()

const onImageError = (idx: number) => {
  if (images.value[idx]) {
    images.value.splice(idx, 1)
  }
}

onMounted(async () => {
  try {
    const all = await listAllScenic()
    const spot = (all || []).find((item) => item.scenicId === scenicId) || null
    detail.value = spot
    if (spot?.image) {
      images.value = [spot.image]
    }
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.detail-page { min-height: 100vh; background: var(--color-bg); }
.swipe, .hero-img { width: 100%; height: 270px; object-fit: cover; }
.swipe-img { width: 100%; height: 270px; object-fit: cover; }

.info-card { margin: var(--space-md); background: #fff; border-radius: var(--radius-lg); padding: var(--space-lg); box-shadow: var(--shadow-card); }
.spot-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: var(--space-md); gap: var(--space-md); }
.spot-name { font-size: var(--font-xl); font-weight: 700; color: var(--color-text); margin: 0; letter-spacing: 0; }
.divider { height: 1px; background: var(--color-border); margin-bottom: var(--space-md); }
.spot-intro { font-size: var(--font-md); color: var(--color-text-secondary); line-height: 1.9; margin: 0; text-indent: 2em; white-space: pre-wrap; }

.loading-state { padding: var(--space-md); }
.skeleton-img { width: 100%; height: 280px; background: #e8e8e8; border-radius: var(--radius-md); }
.skeleton-info { padding: var(--space-lg); }
.skeleton-text { margin-bottom: var(--space-sm); }
</style>

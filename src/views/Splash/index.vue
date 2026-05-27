<template>
  <div class="splash">
    <div class="splash-content">
      <div class="logo-ring">
        <div class="logo-inner">
          <van-icon name="location-o" size="56" color="#fff" />
        </div>
      </div>
      <h1 class="title">西南大学智慧导览</h1>
      <p class="subtitle">边走边听，智游校园</p>
      <div class="loader-dots">
        <span class="dot"></span><span class="dot"></span><span class="dot"></span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onMounted(() => {
  setTimeout(() => router.replace('/map'), 2200)
})
</script>

<style scoped>
.splash { width: 100%; height: 100vh; background: var(--gradient-primary); display: flex; justify-content: center; align-items: center; position: relative; overflow: hidden; }
.splash-content { text-align: center; position: relative; z-index: 1; }
.logo-ring { width: 100px; height: 100px; border-radius: 28px; border: 1px solid rgba(255,255,255,0.34); display: flex; align-items: center; justify-content: center; margin: 0 auto 24px; animation: scaleIn 0.6s var(--ease-out); box-shadow: 0 18px 42px rgba(0,0,0,0.14); }
.logo-inner { width: 80px; height: 80px; border-radius: 22px; background: rgba(255,255,255,0.18); display: flex; align-items: center; justify-content: center; }
.title { color: #fff; font-size: var(--font-2xl); font-weight: 700; margin: 0 0 8px; letter-spacing: 0; animation: fadeInUp 0.6s 0.2s var(--ease-out) both; }
.subtitle { color: rgba(255,255,255,0.72); font-size: var(--font-sm); margin: 0 0 32px; letter-spacing: 2px; animation: fadeInUp 0.6s 0.4s var(--ease-out) both; }
.loader-dots { display: flex; justify-content: center; gap: 8px; animation: fadeIn 0.6s 0.6s var(--ease-out) both; }
.dot { width: 8px; height: 8px; border-radius: 50%; background: rgba(255,255,255,0.6); }
.dot:nth-child(1) { animation: bounce 1s 0s infinite; }
.dot:nth-child(2) { animation: bounce 1s 0.2s infinite; }
.dot:nth-child(3) { animation: bounce 1s 0.4s infinite; }
@keyframes bounce {
  0%, 80%, 100% { transform: translateY(0); opacity: 0.4; }
  40% { transform: translateY(-12px); opacity: 1; }
}
</style>

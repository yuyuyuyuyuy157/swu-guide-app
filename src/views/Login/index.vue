<template>
  <div class="login-page">
    <div class="hero">
      <div class="hero-icon">
        <van-icon name="location-o" size="44" color="#fff" />
      </div>
      <h1 class="hero-title">西南大学智慧导览</h1>
      <p class="hero-subtitle">边走边听，智游校园</p>
    </div>

    <div class="login-card">
      <h2 class="card-title">欢迎回来</h2>
      <p class="card-subtitle">登录后开启搜索、讲解和自动导览</p>
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            v-model="phone"
            type="tel"
            label="手机号"
            placeholder="请输入11位手机号"
            maxlength="11"
            :rules="[{ required: true, message: '请填写手机号' }]"
          />
          <van-field
            v-model="password"
            type="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请填写密码' }]"
          />
        </van-cell-group>
        <div style="margin: 24px 16px 0;">
          <van-button round block type="primary" native-type="submit" :loading="loading" size="large">
            登录
          </van-button>
          <van-button round block plain type="primary" @click="goGuest" style="margin-top: 12px;">
            游客模式，先逛逛
          </van-button>
        </div>
      </van-form>
      <div class="footer-link" @click="goRegister">还没有账号？立即注册</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showFailToast, showSuccessToast, showToast } from 'vant'
import { useAuthStore } from '../../stores/auth'
import { useAudioStore } from '../../stores/audio'
import { loginApi } from '../../api/user'

const router = useRouter()
const authStore = useAuthStore()
const audioStore = useAudioStore()

const phone = ref('')
const password = ref('')
const loading = ref(false)

const onSubmit = async () => {
  loading.value = true
  try {
    const res = await loginApi(phone.value, password.value)
    authStore.loginSuccess(res.token, res.role, res.userId, res.phone, res.avatar)
    if (res.playSettings && Object.keys(res.playSettings).length > 0) {
      Object.assign(audioStore.settings, res.playSettings)
    }
    showSuccessToast('登录成功')
    router.push('/map')
  } catch (error: any) {
    showFailToast(error?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const goGuest = () => {
  authStore.setGuest()
  showToast('已进入游客模式')
  router.push('/map')
}

const goRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-page { min-height: 100vh; background: var(--gradient-primary); display: flex; flex-direction: column; max-width: 430px; margin: 0 auto; box-shadow: 0 0 0 1px rgba(23,37,42,0.04), 0 20px 50px rgba(23,37,42,0.12); }
.hero { padding: 58px 0 30px; text-align: center; }
.hero-icon { width: 74px; height: 74px; border-radius: 22px; background: rgba(255,255,255,0.18); display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; box-shadow: 0 14px 32px rgba(0,0,0,0.12); }
.hero-title { color: #fff; font-size: 26px; font-weight: 700; margin: 0 0 6px; letter-spacing: 0; }
.hero-subtitle { color: rgba(255,255,255,0.76); font-size: 13px; margin: 0; letter-spacing: 2px; }
.login-card { flex: 1; background: var(--color-bg); border-radius: 18px 18px 0 0; padding: 26px 0 0; }
.card-title { font-size: 21px; font-weight: 700; color: var(--color-text); margin: 0 0 4px; padding: 0 20px; letter-spacing: 0; }
.card-subtitle { margin: 0 0 18px; padding: 0 20px; color: var(--color-text-muted); font-size: var(--font-sm); }
.footer-link { text-align: center; margin-top: 20px; font-size: 13px; color: var(--color-primary); cursor: pointer; padding-bottom: 40px; }
@media (max-width: 430px) {
  .login-page { max-width: none; box-shadow: none; }
}
</style>

<template>
  <div class="register-page">
    <van-nav-bar title="注册账号" left-arrow @click-left="goBack" />
    <div class="register-heading">
      <h2 class="title">创建导览账号</h2>
      <p>邀请码注册后即可保存播放设置</p>
    </div>
    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field v-model="phone" type="tel" label="手机号" placeholder="请输入11位手机号" maxlength="11" :rules="[{ required: true }]" />
        <van-field v-model="password" type="password" label="密码" placeholder="设置8-16位密码" :rules="[{ required: true }]" />
        <van-field v-model="confirmPwd" type="password" label="确认密码" placeholder="请再次输入密码" :rules="[{ required: true }]" />
        <van-field v-model="inviteCode" label="邀请码" placeholder="请输入6位邀请码" maxlength="6" :rules="[{ required: true }]" />
      </van-cell-group>
      <div style="margin: 24px 16px 0;">
        <van-button round block type="primary" native-type="submit" :loading="loading" size="large">立即注册</van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast, showFailToast } from 'vant'
import { useAuthStore } from '../../stores/auth'
import { useAudioStore } from '../../stores/audio'
import { registerApi } from '../../api/user'

const router = useRouter()
const authStore = useAuthStore()
const audioStore = useAudioStore()
const phone = ref(''); const password = ref(''); const confirmPwd = ref(''); const inviteCode = ref('')
const loading = ref(false)
const goBack = () => router.back()

const onSubmit = async () => {
  if (password.value !== confirmPwd.value) { showFailToast('两次输入的密码不一致！'); return }
  loading.value = true
  try {
    const res = await registerApi(phone.value, password.value, confirmPwd.value, inviteCode.value)
    authStore.loginSuccess(res.token, res.role, res.userId, res.phone, res.avatar)
    if (res.playSettings && Object.keys(res.playSettings).length) Object.assign(audioStore.settings, res.playSettings)
    showSuccessToast('注册成功！'); router.replace('/map')
  } catch (error: any) { showFailToast(error.message || '注册失败') }
  finally { loading.value = false }
}
</script>

<style scoped>
.register-page { min-height: 100vh; background: var(--color-bg); padding-bottom: 40px; max-width: 430px; margin: 0 auto; box-shadow: 0 0 0 1px rgba(23,37,42,0.04), 0 20px 50px rgba(23,37,42,0.10); }
.register-heading { padding: 26px 20px 20px; }
.title { margin: 0 0 6px; color: var(--color-text); font-size: 22px; font-weight: 700; letter-spacing: 0; }
.register-heading p { margin: 0; color: var(--color-text-muted); font-size: var(--font-sm); }
@media (max-width: 430px) {
  .register-page { max-width: none; box-shadow: none; }
}
</style>

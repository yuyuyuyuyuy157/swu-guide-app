<template>
  <van-popup v-model:show="isVisible" position="bottom" :style="{ height: '100%', backgroundColor: '#f7f8fa' }">
    <van-nav-bar title="修改密码" left-arrow @click-left="closeModal" />
    <div style="padding: 20px;">
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field v-model="oldPassword" :type="showOld ? 'text' : 'password'" label="原密码" placeholder="请输入原密码" :right-icon="showOld ? 'eye-o' : 'closed-eye'" @click-right-icon="showOld = !showOld" :rules="[{ required: true }]" />
          <van-field v-model="newPassword" :type="showNew ? 'text' : 'password'" label="新密码" placeholder="请输入新密码" :right-icon="showNew ? 'eye-o' : 'closed-eye'" @click-right-icon="showNew = !showNew" :rules="[{ required: true }]" />
          <van-field v-model="confirmPassword" :type="showConfirm ? 'text' : 'password'" label="确认密码" placeholder="请再次输入" :right-icon="showConfirm ? 'eye-o' : 'closed-eye'" @click-right-icon="showConfirm = !showConfirm" :rules="[{ required: true }, { validator: (v: string) => v === newPassword, message: '两次输入不一致' }]" />
        </van-cell-group>
        <div style="margin: 24px 16px 0;">
          <van-button round block type="primary" native-type="submit" :loading="loading" size="large">确认修改</van-button>
        </div>
      </van-form>
    </div>
  </van-popup>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { showToast, showFailToast } from 'vant'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { changePasswordApi } from '../api/user'

const props = defineProps({ show: Boolean })
const emit = defineEmits(['update:show'])
const router = useRouter()
const authStore = useAuthStore()
const isVisible = computed({ get: () => props.show, set: (val) => emit('update:show', val) })

const oldPassword = ref(''); const newPassword = ref(''); const confirmPassword = ref('')
const showOld = ref(false); const showNew = ref(false); const showConfirm = ref(false)
const loading = ref(false)
const closeModal = () => { isVisible.value = false }

const onSubmit = async () => {
  loading.value = true
  try {
    await changePasswordApi(oldPassword.value, newPassword.value, confirmPassword.value)
    showToast({ message: '密码修改成功，请重新登录', type: 'success' })
    setTimeout(() => { authStore.logout(); router.push('/login') }, 1500)
  } catch (error: any) { showFailToast(error.message || '修改失败') }
  finally { loading.value = false }
}
</script>

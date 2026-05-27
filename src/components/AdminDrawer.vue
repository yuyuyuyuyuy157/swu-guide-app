<template>
  <van-popup v-model:show="isVisible" position="right" :style="{ width: '85%', height: '100%', backgroundColor: '#f7f8fa' }" :before-close="handleBeforeClose">
    <van-nav-bar title="编辑景点信息" left-arrow @click-left="closeDrawer" />

    <div v-if="formData" class="form-container">
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field v-model="formData.name" name="name" label="景点名称" placeholder="请输入名称" :rules="[{ required: true, message: '名称不能为空' }]" />
          <van-field v-model="formData.latitude" name="latitude" label="纬度 (Lat)" placeholder="例如: 29.815" :rules="[{ pattern: /^\d+(\.\d+)?$/, message: '请输入正确的地理坐标格式' }]" />
          <van-field v-model="formData.longitude" name="longitude" label="经度 (Lng)" placeholder="例如: 106.425" :rules="[{ pattern: /^\d+(\.\d+)?$/, message: '请输入正确的地理坐标格式' }]" />
          <van-field v-model="formData.inductionRange" name="inductionRange" type="digit" label="感应半径(米)" placeholder="默认 50" />
          <van-field v-model="formData.intro" rows="4" autosize label="介绍文本" type="textarea" placeholder="请输入景点的详细介绍..." />
        </van-cell-group>

        <div class="audit-stream">
          <p>上次修改人：{{ formData.lastModifier || '-' }}</p>
          <p>修改时间：{{ formData.modifyTime || '-' }}</p>
        </div>

        <div style="margin: 30px 16px;">
          <van-button round block type="primary" native-type="submit" :loading="saving">保存修改</van-button>
        </div>
      </van-form>
    </div>
  </van-popup>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { showConfirmDialog, showSuccessToast, showFailToast } from 'vant'
import { updateScenic } from '../api/admin'

const props = defineProps({ show: Boolean, spotData: Object })
const emit = defineEmits(['update:show', 'refresh'])

const isVisible = computed({
  get: () => props.show,
  set: (val) => emit('update:show', val)
})

const formData = ref<any>(null)
const originalDataStr = ref('')
const saving = ref(false)

watch(() => props.show, (newVal) => {
  if (newVal && props.spotData) {
    formData.value = JSON.parse(JSON.stringify(props.spotData))
    originalDataStr.value = JSON.stringify(formData.value)
  }
})

const handleBeforeClose = () => {
  const isModified = JSON.stringify(formData.value) !== originalDataStr.value
  if (isModified) {
    return new Promise<boolean>((resolve) => {
      showConfirmDialog({
        title: '放弃修改？',
        message: '内容已修改，直接关闭将丢失当前编辑的数据。',
        confirmButtonText: '放弃保存',
        cancelButtonText: '继续编辑',
        confirmButtonColor: '#ee0a24'
      }).then(() => resolve(true)).catch(() => resolve(false))
    })
  }
  return true
}

const closeDrawer = () => { isVisible.value = false }

const onSubmit = async () => {
  saving.value = true
  try {
    // 适配后端 ScenicSpotDTO 字段名
    await updateScenic({
      id: parseInt(formData.value.scenicId) || 0,
      name: formData.value.name,
      latitude: parseFloat(formData.value.latitude),
      longitude: parseFloat(formData.value.longitude),
      radius: parseInt(formData.value.inductionRange) || 50,
      description: formData.value.intro
    })
    showSuccessToast('修改成功')
    originalDataStr.value = JSON.stringify(formData.value)
    isVisible.value = false
    emit('refresh')
  } catch {
    showFailToast('保存失败，请重试')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.form-container { padding-top: 16px; }
.audit-stream { margin: 16px 24px; font-size: 11px; color: #c8c9cc; line-height: 1.4; text-align: right; }
.audit-stream p { margin: 2px 0; }
</style>

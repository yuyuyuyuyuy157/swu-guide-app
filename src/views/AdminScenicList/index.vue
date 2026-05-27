<template>
  <div class="admin-page">
    <van-nav-bar title="景点管理" left-text="返回" left-arrow @click-left="goBack" fixed placeholder>
      <template #right>
        <van-icon name="plus" size="20" @click="openCreate" />
      </template>
    </van-nav-bar>

    <div class="admin-header">
      <div>
        <h2>景点数据</h2>
        <p>维护坐标、图片和介绍文本</p>
      </div>
      <van-button size="small" type="primary" icon="plus" round @click="openCreate">新增</van-button>
    </div>

    <div class="search-area">
      <div class="search-box">
        <van-icon name="search" color="#9e9eb8" size="18" />
        <input v-model="keyword" placeholder="搜索景点名称..." class="search-input" @keyup.enter="onSearch" />
        <van-icon v-if="keyword" name="clear" color="#9e9eb8" size="16" @click="keyword=''; onSearch()" style="cursor:pointer" />
      </div>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh" class="list-area">
      <van-list v-model:loading="loadingMore" :finished="finished" finished-text="没有更多了" @load="onLoad">
        <div v-for="item in list" :key="item.scenicId" class="scenic-row" @click="openEdit(item)">
          <div class="row-left">
            <h4 class="row-name">{{ item.name }}</h4>
            <p class="row-intro">{{ item.intro }}</p>
            <span class="row-audit">{{ item.lastModifier || '-' }} · {{ formatTime(item.modifyTime) }}</span>
          </div>
          <van-icon name="arrow" color="#c8c9cc" size="16" />
        </div>
      </van-list>
      <van-empty v-if="!loadingMore && list.length === 0" description="暂无景点数据" />
    </van-pull-refresh>

    <!-- Edit/Create Drawer -->
    <van-popup v-model:show="showDrawer" position="right" :style="{ width: '90%', height: '100%', backgroundColor: '#f4f7f6' }">
      <van-nav-bar :title="drawerMode === 'create' ? '新增景点' : '编辑景点'" left-arrow @click-left="showDrawer = false" />
      <div v-if="editForm" class="drawer-form">
        <div class="map-picker">
          <div id="admin-map-picker" class="map-picker-canvas"></div>
          <div class="map-picker-tip">点击地图自动录入经纬度</div>
        </div>
        <van-form @submit="onSave">
          <van-cell-group inset>
            <van-field v-model="editForm.name" label="名称" :rules="[{ required: true }]" />
            <van-field v-model="editForm.latitude" label="纬度" :rules="[{ pattern: /^\d+(\.\d+)?$/ }]" />
            <van-field v-model="editForm.longitude" label="经度" :rules="[{ pattern: /^\d+(\.\d+)?$/ }]" />
            <van-field v-model="editForm.radius" label="感应半径(m)" type="digit" />
            <van-field label="景点图片">
              <template #input>
                <van-uploader
                  v-model="imageFiles"
                  :max-count="1"
                  :deletable="true"
                  :preview-options="{ closeable: true, closeIcon: 'cross', closeIconPosition: 'top-right', closeOnPopstate: true }"
                  :after-read="afterReadImage"
                  @delete="onDeleteImage"
                  :preview-size="86"
                  upload-text="上传图片"
                />
              </template>
            </van-field>
            <van-field v-model="editForm.description" label="介绍" type="textarea" rows="5" autosize />
          </van-cell-group>
          <div class="save-area">
            <van-button round block type="primary" native-type="submit" :loading="saving" size="large">
              {{ drawerMode === 'create' ? '新增景点' : '保存修改' }}
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast, showFailToast } from 'vant'
import AMapLoader from '@amap/amap-jsapi-loader'
import { getAdminScenicList, getAdminScenicDetail, updateScenic, createScenic, uploadScenicImage } from '../../api/admin'

const router = useRouter()
const goBack = () => router.back()

const keyword = ref('')
const list = ref<any[]>([])
const page = ref(1)
const loadingMore = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showDrawer = ref(false)
const editForm = ref<any>(null)
const saving = ref(false)
const drawerMode = ref<'create' | 'edit'>('edit')
const imageFiles = ref<any[]>([])

let pickerMap: any = null
let pickerMarker: any = null

const formatTime = (t: string) => t ? new Date(t).toLocaleDateString('zh-CN') : '-'

const fetchList = async (reset = false) => {
  if (reset) { page.value = 1; finished.value = false }
  try {
    const res = await getAdminScenicList(keyword.value, page.value, 20)
    if (reset) list.value = res.records || []
    else list.value.push(...(res.records || []))
    finished.value = list.value.length >= res.total
    if (!finished.value) page.value++
  } catch { finished.value = true }
}

const onSearch = () => fetchList(true)
const onRefresh = async () => { refreshing.value = true; await fetchList(true); refreshing.value = false }
const onLoad = async () => {
  if (finished.value) return
  loadingMore.value = true
  await fetchList(false)
  loadingMore.value = false
}

const initPickerMap = async () => {
  await nextTick()
  const lat = parseFloat(editForm.value.latitude) || 29.815
  const lng = parseFloat(editForm.value.longitude) || 106.425
  ;(window as any)._AMapSecurityConfig = {
    securityJsCode: 'c88b8e54bede34a6c5414091a3a4aae2',
  }
  const AMap = await AMapLoader.load({ key: '96d2fa132d6d1be0b15be4a4351ebf6c', version: '2.0' })
  if (pickerMap) pickerMap.destroy()
  pickerMap = new AMap.Map('admin-map-picker', { viewMode: '2D', zoom: 17, center: [lng, lat] })
  pickerMarker = new AMap.Marker({ position: [lng, lat] })
  pickerMap.add(pickerMarker)
  pickerMap.on('click', (event: any) => {
    const point = event.lnglat
    editForm.value.longitude = Number(point.lng).toFixed(7)
    editForm.value.latitude = Number(point.lat).toFixed(7)
    pickerMarker.setPosition([point.lng, point.lat])
  })
}

const openCreate = async () => {
  drawerMode.value = 'create'
  editForm.value = {
    name: '',
    latitude: '29.8150000',
    longitude: '106.4250000',
    radius: '50',
    imageUrl: '',
    description: ''
  }
  imageFiles.value = []
  showDrawer.value = true
  initPickerMap().catch(() => showFailToast('地图选点加载失败'))
}

const openEdit = async (item: any) => {
  try {
    drawerMode.value = 'edit'
    const detail = await getAdminScenicDetail(item.scenicId)
    editForm.value = {
      id: parseInt(item.scenicId),
      name: detail.name,
      latitude: String(detail.latitude),
      longitude: String(detail.longitude),
      radius: String(detail.radius || detail.inductionRange || 50),
      imageUrl: detail.imageUrl || detail.image || '',
      audioUrl: detail.audioUrl || '',
      description: detail.description || detail.intro || ''
    }
    imageFiles.value = editForm.value.imageUrl ? [{ url: editForm.value.imageUrl }] : []
    showDrawer.value = true
    initPickerMap().catch(() => showFailToast('地图选点加载失败'))
  } catch { showFailToast('获取详情失败') }
}

const afterReadImage = async (fileItem: any) => {
  fileItem.status = 'uploading'
  fileItem.message = '上传中...'
  try {
    const rawFile = Array.isArray(fileItem) ? fileItem[0]?.file : fileItem.file
    const res = await uploadScenicImage(rawFile)
    editForm.value.imageUrl = res.url
    fileItem.status = 'done'
    fileItem.message = ''
  } catch {
    fileItem.status = 'failed'
    fileItem.message = '上传失败'
    showFailToast('图片上传失败')
  }
}

const onDeleteImage = () => {
  imageFiles.value = []
  if (editForm.value) {
    editForm.value.imageUrl = ''
  }
}

const onSave = async () => {
  saving.value = true
  try {
    const payload = {
      name: editForm.value.name,
      latitude: parseFloat(editForm.value.latitude),
      longitude: parseFloat(editForm.value.longitude),
      radius: parseInt(editForm.value.radius) || 50,
      description: editForm.value.description,
      imageUrl: editForm.value.imageUrl,
      audioUrl: editForm.value.audioUrl
    }
    if (drawerMode.value === 'create') {
      await createScenic(payload)
      showSuccessToast('新增成功')
    } else {
      await updateScenic({ id: editForm.value.id, ...payload })
      showSuccessToast('修改成功')
    }
    showDrawer.value = false
    fetchList(true)
  } catch { showFailToast('保存失败') }
  finally { saving.value = false }
}
</script>

<style scoped>
.admin-page { min-height: 100vh; background: var(--color-bg); max-width: 860px; margin: 0 auto; box-shadow: 0 0 0 1px rgba(23,37,42,0.04); }
.admin-header { display: flex; align-items: center; justify-content: space-between; gap: var(--space-md); padding: var(--space-lg) var(--space-md) var(--space-md); background: #fff; }
.admin-header h2 { margin: 0 0 4px; font-size: 22px; color: var(--color-text); letter-spacing: 0; }
.admin-header p { margin: 0; font-size: var(--font-sm); color: var(--color-text-muted); }
.search-area { padding: 0 var(--space-md) var(--space-md); background: #fff; position: sticky; top: 46px; z-index: 10; box-shadow: 0 8px 18px rgba(23,37,42,0.04); }
.search-box { display: flex; align-items: center; gap: var(--space-sm); background: var(--color-bg); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 10px var(--space-md); }
.search-input { flex: 1; border: none; background: transparent; font-size: var(--font-sm); outline: none; color: var(--color-text); }
.search-input::placeholder { color: var(--color-text-muted); }
.list-area { padding: var(--space-md); }
.scenic-row { display: flex; align-items: center; background: #fff; border-radius: var(--radius-md); padding: 14px var(--space-md); margin-bottom: var(--space-sm); box-shadow: var(--shadow-sm); cursor: pointer; transition: all var(--duration-fast); border: 1px solid rgba(223,232,232,0.8); }
.scenic-row:active { transform: scale(0.98); }
.row-left { flex: 1; min-width: 0; }
.row-name { font-size: var(--font-md); font-weight: 600; color: var(--color-text); margin: 0 0 4px; }
.row-intro { font-size: var(--font-xs); color: var(--color-text-secondary); margin: 0 0 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.row-audit { font-size: 10px; color: var(--color-text-muted); }
.drawer-form { padding-top: var(--space-md); padding-bottom: var(--space-xl); }
.map-picker { margin: 0 var(--space-md) var(--space-md); background: #fff; border-radius: var(--radius-md); overflow: hidden; box-shadow: var(--shadow-sm); }
.map-picker-canvas { width: 100%; height: 220px; }
.map-picker-tip { padding: 8px 12px; color: var(--color-text-secondary); font-size: var(--font-xs); }
.save-area { margin: var(--space-xl) var(--space-md); }
@media (max-width: 860px) {
  .admin-page { max-width: none; box-shadow: none; }
}
</style>

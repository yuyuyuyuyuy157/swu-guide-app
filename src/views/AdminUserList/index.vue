<template>
  <div class="admin-user-page">
    <van-nav-bar title="用户管理" left-text="返回" left-arrow @click-left="goBack" fixed placeholder />

    <div class="admin-header">
      <h2>用户数据</h2>
      <p>管理员身份由系统底层维护；此处只管理账号状态。</p>
    </div>

    <div class="search-area">
      <div class="search-box">
        <van-icon name="search" color="#8b9ca2" size="18" />
        <input v-model="keyword" placeholder="搜索用户手机号..." class="search-input" @keyup.enter="onSearch" />
        <van-icon v-if="keyword" name="clear" color="#8b9ca2" size="16" @click="keyword=''; onSearch()" />
      </div>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh" class="list-area">
      <van-list v-model:loading="loadingMore" :finished="finished" finished-text="没有更多用户" @load="onLoad">
        <button v-for="user in userList" :key="user.userId" class="user-row" type="button" @click="openStatusEditor(user)">
          <van-image round width="42px" height="42px" :src="user.avatar || defaultAvatar" />
          <div class="row-left">
            <h4 class="row-name">{{ user.phone }}</h4>
            <span class="row-audit">{{ formatRole(user.role) }} · {{ formatTime(user.createdAt) }}</span>
          </div>
          <van-tag :type="statusTagType(user.status)" round>{{ formatStatus(user.status) }}</van-tag>
          <van-icon name="arrow" color="#9aa8ad" />
        </button>
      </van-list>
      <van-empty v-if="!loadingMore && userList.length === 0" description="暂无用户数据" />
    </van-pull-refresh>

    <van-popup v-model:show="showStatusEditor" position="bottom" round safe-area-inset-bottom>
      <div v-if="editUser" class="editor-panel">
        <div class="editor-head">
          <h3>账号状态</h3>
          <span>{{ editUser.phone }} · {{ formatRole(editUser.role) }}</span>
        </div>

        <van-radio-group v-model="editStatus">
          <van-cell-group inset>
            <van-cell title="正常" label="用户可以登录并正常使用功能" clickable @click="editStatus = 1">
              <template #right-icon><van-radio :name="1" /></template>
            </van-cell>
            <van-cell title="冻结" label="临时禁用账号，用户不能登录；已有 token 也会被拦截" clickable @click="editStatus = 2">
              <template #right-icon><van-radio :name="2" /></template>
            </van-cell>
            <van-cell title="注销" label="账号进入不可用状态，通常用于用户退出系统后的保留记录" clickable @click="editStatus = 3">
              <template #right-icon><van-radio :name="3" /></template>
            </van-cell>
          </van-cell-group>
        </van-radio-group>

        <div class="editor-actions">
          <van-button block plain type="default" @click="showStatusEditor = false">取消</van-button>
          <van-button block type="primary" :loading="saving" @click="saveStatus">保存</van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showFailToast, showSuccessToast } from 'vant'
import { getAdminUserList, updateAdminUserStatus } from '../../api/admin'
import type { AdminUserItem } from '../../types/api'

const router = useRouter()
const goBack = () => router.back()

const keyword = ref('')
const userList = ref<AdminUserItem[]>([])
const page = ref(1)
const loadingMore = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showStatusEditor = ref(false)
const editUser = ref<AdminUserItem | null>(null)
const editStatus = ref<1 | 2 | 3>(1)
const saving = ref(false)
const defaultAvatar = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'

const formatTime = (t: string) => t ? new Date(t).toLocaleDateString('zh-CN') : '-'
const formatRole = (role: string) => role?.toUpperCase() === 'ADMIN' ? '管理员' : '普通用户'
const formatStatus = (status: number) => status === 1 ? '正常' : status === 2 ? '冻结' : '注销'
const statusTagType = (status: number) => status === 1 ? 'success' : status === 2 ? 'warning' : 'danger'

const fetchList = async (reset = false) => {
  if (reset) {
    page.value = 1
    finished.value = false
    userList.value = []
  }
  try {
    const res = await getAdminUserList(keyword.value, page.value, 20)
    const records = res.records || []
    userList.value = reset ? records : [...userList.value, ...records]
    finished.value = userList.value.length >= res.total || records.length === 0
    if (!finished.value) page.value++
  } catch (error: any) {
    finished.value = true
    showFailToast(error.message || '用户列表加载失败')
  }
}

const onSearch = () => fetchList(true)
const onRefresh = async () => { refreshing.value = true; await fetchList(true); refreshing.value = false }
const onLoad = async () => { if (finished.value) return; loadingMore.value = true; await fetchList(false); loadingMore.value = false }

const openStatusEditor = (user: AdminUserItem) => {
  editUser.value = user
  editStatus.value = user.status
  showStatusEditor.value = true
}

const saveStatus = async () => {
  if (!editUser.value) return
  saving.value = true
  try {
    await updateAdminUserStatus(editUser.value.userId, editStatus.value)
    showSuccessToast('账号状态已更新')
    showStatusEditor.value = false
    await fetchList(true)
  } catch (error: any) {
    showFailToast(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.admin-user-page { min-height: 100vh; background: var(--color-bg); max-width: 860px; margin: 0 auto; box-shadow: 0 0 0 1px rgba(23,37,42,0.04); }
.admin-header { padding: var(--space-lg) var(--space-md) var(--space-md); background: #fff; }
.admin-header h2 { margin: 0 0 4px; font-size: 22px; color: var(--color-text); letter-spacing: 0; }
.admin-header p { margin: 0; font-size: var(--font-sm); color: var(--color-text-muted); line-height: 1.5; }
.search-area { padding: 0 var(--space-md) var(--space-md); background: #fff; position: sticky; top: 46px; z-index: 10; box-shadow: 0 8px 18px rgba(23,37,42,0.04); }
.search-box { display: flex; align-items: center; gap: var(--space-sm); background: var(--color-bg); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 10px var(--space-md); }
.search-input { flex: 1; border: none; background: transparent; font-size: var(--font-sm); outline: none; color: var(--color-text); }
.search-input::placeholder { color: var(--color-text-muted); }
.list-area { min-height: calc(100vh - 170px); padding: var(--space-md); }
.user-row { width: 100%; display: flex; align-items: center; gap: var(--space-md); background: #fff; border-radius: var(--radius-md); padding: 14px var(--space-md); margin-bottom: var(--space-sm); box-shadow: var(--shadow-sm); border: 1px solid rgba(223,232,232,0.8); text-align: left; cursor: pointer; }
.row-left { flex: 1; min-width: 0; }
.row-name { font-size: var(--font-md); font-weight: 600; color: var(--color-text); margin: 0 0 4px; }
.row-audit { font-size: 11px; color: var(--color-text-muted); }
.editor-panel { padding: 22px 0 18px; background: #fff; }
.editor-head { margin: 0 18px 16px; }
.editor-head h3 { margin: 0 0 4px; font-size: 19px; color: var(--color-text); }
.editor-head span { color: var(--color-text-muted); font-size: var(--font-sm); }
.editor-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; padding: 18px 18px 0; }
@media (max-width: 860px) {
  .admin-user-page { max-width: none; box-shadow: none; }
}
</style>

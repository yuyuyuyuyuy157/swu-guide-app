<template>
  <van-popup v-model:show="isVisible" position="bottom" round :style="{ height: '85%', backgroundColor: '#f7f8fa' }">
    <form action="/" @submit.prevent="onSearch()">
      <van-search v-model="keyword" placeholder="搜索西南大学内的景点..." autofocus @search="onSearch">
        <template #action>
          <button class="search-action" type="submit">搜索</button>
        </template>
      </van-search>
    </form>

    <div class="search-content">
      <van-empty v-if="hasSearched && results.length === 0" image="search" description="未找到相关地点，请换个词试试" />

      <template v-else-if="results.length > 0">
        <van-cell
          v-for="item in results"
          :key="item.scenicId"
          :title="item.name"
          :label="item.intro"
          icon="location-o"
          is-link
          @click="onSelect(item)"
        />
      </template>

      <div v-else class="initial-tips">
        <p class="tips-title">大家都在搜</p>
        <div class="tags">
          <button class="tag" type="button" @click="fastSearch('共青团花园')">共青团花园</button>
          <button class="tag" type="button" @click="fastSearch('崇德湖')">崇德湖</button>
          <button class="tag" type="button" @click="fastSearch('图书馆')">图书馆</button>
          <button class="tag" type="button" @click="fastSearch('31教')">31教</button>
        </div>
      </div>
    </div>
  </van-popup>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { showFailToast, showLoadingToast } from 'vant'
import { listAllScenic, searchScenic } from '../api/scenic'
import type { ScenicSpot } from '../types/api'

const props = defineProps({ show: Boolean })
const emit = defineEmits(['update:show', 'select'])
const isVisible = computed({ get: () => props.show, set: (val) => emit('update:show', val) })

const keyword = ref('')
const results = ref<ScenicSpot[]>([])
const hasSearched = ref(false)

const normalize = (value?: string) => (value || '').toLowerCase().replace(/\s+/g, '')

const localSearch = async (kw: string) => {
  const all = await listAllScenic()
  const key = normalize(kw)
  return (all || []).filter((spot) => {
    const name = normalize(spot.name)
    const intro = normalize(spot.intro)
    return name.includes(key) || intro.includes(key)
  })
}

const onSearch = async (val?: string) => {
  const kw = (val || keyword.value).trim()
  if (!kw) return

  const toast = showLoadingToast({ message: '搜索中...', forbidClick: true })
  hasSearched.value = false

  try {
    const remoteResults = await searchScenic(kw) || []
    results.value = remoteResults.length > 0 ? remoteResults : await localSearch(kw)
  } catch (error: any) {
    try {
      results.value = await localSearch(kw)
      if (results.value.length === 0) {
        showFailToast(error.message || '搜索失败')
      }
    } catch {
      results.value = []
      showFailToast(error.message || '搜索失败')
    }
  } finally {
    hasSearched.value = true
    toast.close()
  }
}

const fastSearch = (val: string) => {
  keyword.value = val
  onSearch(val)
}

const onSelect = (item: ScenicSpot) => {
  emit('select', item)
  isVisible.value = false
}
</script>

<style scoped>
.search-content { padding: 10px 0; }
.search-action { border: none; background: transparent; color: var(--color-primary); font-size: var(--font-sm); font-weight: 600; padding: 0 4px; cursor: pointer; }
.initial-tips { padding: 24px 20px; }
.tips-title { font-size: 13px; color: #999; margin: 0 0 16px; }
.tags { display: flex; gap: 10px; flex-wrap: wrap; }
.tag { border: none; font-size: 13px; color: var(--color-primary); background: var(--color-primary-bg); padding: 8px 16px; border-radius: 20px; cursor: pointer; font-weight: 500; transition: all 0.2s; }
.tag:active { background: var(--color-primary); color: #fff; }
</style>

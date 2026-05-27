<template>
  <div class="app-container">
    <router-view></router-view>
  </div>
</template>

<style>
/* 清除浏览器默认边距，并锁死滚动条防止页面乱跑 */
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  overflow: auto; 
  background-color: #f7f8fa;
}

/* 让页面内容区占满整个屏幕 */
.app-container {
  min-height: 100vh;
  width: 100vw;
  position: relative;
}
</style>

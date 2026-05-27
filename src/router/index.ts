import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/splash' },
  { path: '/splash', name: 'Splash', component: () => import('../views/Splash/index.vue') },
  { path: '/login', name: 'Login', component: () => import('../views/Login/index.vue') },
  { path: '/map', name: 'Map', component: () => import('../views/Map/index.vue') },
  { path: '/detail/:id', name: 'Detail', component: () => import('../views/Detail/index.vue'), meta: { hideTabBar: true } },
  { path: '/register', name: 'Register', component: () => import('../views/Register/index.vue'), meta: { hideTabBar: true } },
  { path: '/admin/scenic-list', name: 'AdminScenicList', component: () => import('../views/AdminScenicList/index.vue') },
  { path: '/admin/users', name: 'AdminUserList', component: () => import('../views/AdminUserList/index.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

# 西南大学智慧导览 - 开发文档

> 版本 0.0.0 | 2026-05-18

---

## 目录

- [一、项目概述](#一项目概述)
- [二、技术栈](#二技术栈)
- [三、目录结构](#三目录结构)
- [四、前端功能清单](#四前端功能清单)
  - [4.1 认证系统](#41-认证系统)
  - [4.2 地图主页](#42-地图主页)
  - [4.3 景点卡片](#43-景点卡片)
  - [4.4 音频系统](#44-音频系统)
  - [4.5 搜索功能](#45-搜索功能)
  - [4.6 管理员功能](#46-管理员功能)
  - [4.7 音频设置](#47-音频设置)
  - [4.8 密码修改](#48-密码修改)
  - [4.9 注册页](#49-注册页)
  - [4.10 详情页](#410-详情页)
- [五、路由表](#五路由表)
- [六、状态管理](#六状态管理)
- [七、类型定义](#七类型定义)
- [八、后端接口需求（重要）](#八后端接口需求重要)
  - [8.1 接口总览](#81-接口总览)
  - [8.2 接口详细规范](#82-接口详细规范)
  - [8.3 数据库表建议](#83-数据库表建议)
  - [8.4 非功能性需求](#84-非功能性需求)
- [九、前端待办事项](#九前端待办事项)

---

## 一、项目概述

**西南大学智慧导览 (SWU Guide App)** 是一款面向西南大学校园的移动端 Web 导览应用。用户可以使用地图定位、自动音频导览、景点搜索等功能游览校园。

**核心角色：**

| 角色 | 说明 | 权限 |
|------|------|------|
| 游客 (guest) | 免登录浏览 | 仅查看地图，不能使用搜索和音频 |
| 普通用户 (user) | 注册登录后使用 | 地图、搜索、音频导览、修改密码 |
| 管理员 (admin) | 系统管理员 | 用户全部权限 + 编辑景点信息 |

---

## 二、技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | ^3.5.34 | 前端框架 (Composition API + `<script setup>`) |
| TypeScript | ~6.0.2 | 类型系统 |
| Vite | ^8.0.12 | 构建工具与开发服务器 |
| Vue Router | ^4.6.4 | 客户端路由 |
| Pinia | ^3.0.4 | 状态管理 |
| Vant 4 | ^4.9.24 | 移动端 UI 组件库 |
| Axios | ^1.16.1 | HTTP 客户端 |
| AMap Loader | ^1.0.1 | 高德地图 JSAPI 加载器 |

---

## 三、目录结构

```
swu-guide-app/
├── public/                     # 静态资源
│   ├── favicon.svg
│   └── icons.svg
├── src/
│   ├── api/                    # API 接口层（当前为 mock 数据）
│   │   ├── scenic.ts           # 景点相关接口
│   │   └── user.ts             # 用户相关接口
│   ├── assets/                 # 图片 / 字体等资源
│   ├── components/             # 可复用组件
│   │   ├── AdminDrawer.vue     # 管理员编辑景点抽屉
│   │   ├── AudioSettings.vue   # 音频播放设置面板
│   │   ├── PasswordModal.vue   # 修改密码弹窗
│   │   ├── ScenicCard.vue      # 景点信息卡片
│   │   └── SearchPanel.vue     # 搜索面板
│   ├── router/
│   │   └── index.ts            # 路由定义
│   ├── stores/                 # Pinia 状态管理
│   │   ├── audio.ts            # 音频播放状态
│   │   ├── auth.ts             # 认证 / 角色状态
│   │   └── location.ts         # 定位坐标状态
│   ├── types/
│   │   └── api.d.ts            # 全局 TypeScript 类型定义
│   ├── utils/                  # 工具函数（待创建 request.ts）
│   ├── views/                  # 页面组件
│   │   ├── Detail/index.vue    # 景点详情页
│   │   ├── Login/index.vue     # 登录页
│   │   ├── Map/index.vue       # 主地图页
│   │   └── Register/index.vue  # 注册页
│   ├── App.vue                 # 根组件
│   ├── main.ts                 # 应用入口
│   └── style.css               # 全局样式 + CSS 变量
├── index.html
├── package.json
├── vite.config.ts
└── tsconfig.json
```

---

## 四、前端功能清单

### 4.1 认证系统

**涉及文件：** [src/stores/auth.ts](src/stores/auth.ts)、[src/api/user.ts](src/api/user.ts)、[src/views/Login/index.vue](src/views/Login/index.vue)

**实现状态：** UI 完成，逻辑使用 mock 数据

**功能点：**

- 手机号 + 密码登录表单（Vant Form 组件）
- 登录 loading 状态
- 成功/失败 Toast 提示
- "游客先逛逛" 免登录入口
- Token 和角色信息持久化到 `localStorage`
  - key: `SWU_TOKEN` / `SWU_ROLE`
- 角色分三类：`guest` / `user` / `admin`
- 登录成功后自动跳转地图页 `/map`

**当前 mock 数据：**
- 管理员：`13900139000` / `12345678`
- 普通用户：`13800138000` / `12345678`

---

### 4.2 地图主页

**涉及文件：** [src/views/Map/index.vue](src/views/Map/index.vue)

**实现状态：** 核心框架完成，景点标记为硬编码

**功能点：**

- 高德地图 2D 视图，中心坐标 `[106.425, 29.815]`（西南大学）
- 根据角色切换地图样式：
  - 游客 → `light`
  - 用户 → `normal`
  - 管理员 → `darkblue`
- 地理定位控件（高精度模式，含精度圆环和自定义用户标记）
- 景点 Marker（当前硬编码"共青团花园"坐标），播放音频时切换为绿色脉冲动画
- 四个浮动按钮：
  - **左上头像** → 打开左侧个人中心抽屉
  - **搜索栏** → 游客点此弹出登录引导，用户点此打开搜索面板
  - **右侧设置齿轮** → 打开音频设置弹窗
- 角色切换时自动更新地图样式

**安全密钥：**
- AMap Key: `96d2fa132d6d1be0b15be4a4351ebf6c`
- 安全密钥: `c88b8e54bede34a6c5414091a3a4aae2`
- 当前硬编码在源码中，后续需移至环境变量

---

### 4.3 景点卡片

**涉及文件：** [src/components/ScenicCard.vue](src/components/ScenicCard.vue)

**实现状态：** UI 完成，数据为 mock

**功能点：**

- 底部浮动卡片，展示当前附近景点信息
- 内容：景点图片、名称、距离（米）、简介
- **游客态** → 显示"登录解锁语音"按钮，点击弹 ActionSheet 引导登录
- **用户态** → 显示"听讲解"按钮（播放/暂停切换）+ 倍速切换按钮
- **管理员态** → 显示"管理此景点"按钮，点击打开编辑抽屉
- 播放中时卡片切换为音频进度条模式：
  - 实时时间显示 `mm:ss / mm:ss`
  - Vant Slider 拖动跳转进度
- 点击卡片主体跳转至详情页 `/detail/:id`

---

### 4.4 音频系统

**涉及文件：** [src/stores/audio.ts](src/stores/audio.ts)

**实现状态：** 核心功能完成，自动触发播放未联动

**功能点：**

- 基于原生 `Audio()` 对象的播放器封装
- **播放/暂停** → `playScenicAudio()` / `pause()` / `togglePlay()`
- **进度追踪** → 监听 `timeupdate` / `loadedmetadata` / `ended` 事件
- **进度跳转** → `seek(time)` 方法
- **倍速切换** → `cycleSpeed()` 循环切换 `1.0x → 1.2x → 1.5x → 2.0x → 1.0x`
- **重复策略** → 可选"仅播一次"（记录已播景点 ID）或"允许重复"
- 事件监听器防重复注册（`_isListenerAdded` 标记）
- 播放失败时 Toast 提示

**未完成：**
- 位置监控与自动触发播放的联动
- 切换策略的实际执行逻辑（播完再切 / 随位置切 / 弹窗提醒）

---

### 4.5 搜索功能

**涉及文件：** [src/components/SearchPanel.vue](src/components/SearchPanel.vue)

**实现状态：** UI 完成，搜索结果 mock

**功能点：**

- 底部弹出式搜索面板（85% 高度）
- 搜索框自动聚焦
- 未搜索时展示热门推荐标签：共青团花园、崇德湖、中心图书馆
- 点击标签快速搜索
- 搜索结果列表（Vant Cell 列表）
- 空结果缺省页（Vant Empty 组件）
- 选中结果后关闭面板并移动地图中心至对应坐标
- 取消按钮关闭面板并重置状态

---

### 4.6 管理员功能

**涉及文件：** [src/components/AdminDrawer.vue](src/components/AdminDrawer.vue)

**实现状态：** UI 完成，保存为 mock

**功能点：**

- 右侧滑出抽屉编辑景点信息
- 可编辑字段：景点名称、纬度、经度、感应半径（米）、介绍文本
- 表单验证：
  - 名称为必填项
  - 经纬度需匹配数字格式正则
- **防误触保护** → 内容被修改后关闭抽屉会弹出二次确认对话框
- 修改审计信息展示（修改人 ID + 修改时间）
- 保存 loading 状态 + 成功 Toast

---

### 4.7 音频设置

**涉及文件：** [src/components/AudioSettings.vue](src/components/AudioSettings.vue)

**实现状态：** UI 完成，联动逻辑完成

**功能点：**

- **主开关** → "开启自动触发语音"（到达景点感应区后自动播放）
- **重复频次策略** → 单选：每个景点只播放一次 / 允许重复触发播放
- **播报切换策略** → 单选：播完当前介绍再切换 / 随位置实时打断切换 / 到达新景点弹窗询问
- **系统级设置** → 后台播放与定位开关
- 主开关关闭时，下方策略区域自动置灰 + 禁用交互（CSS `filter: grayscale` + `pointer-events: none`）
- 后台播放开关开启时弹权限引导对话框，拒绝后自动回弹关闭

---

### 4.8 密码修改

**涉及文件：** [src/components/PasswordModal.vue](src/components/PasswordModal.vue)

**实现状态：** UI 完成，修改为 mock

**功能点：**

- 全屏底部弹出式表单
- 三个字段：原密码、新密码、确认密码
- 每个字段右侧有眼睛图标切换密码明文/密文显示
- 确认密码与新版密码一致性校验（`validator`）
- 模拟修改成功后自动登出并跳转登录页

---

### 4.9 注册页

**涉及文件：** [src/views/Register/index.vue](src/views/Register/index.vue)

**实现状态：** UI 完成，注册为 mock

**功能点：**

- 顶部导航栏（含返回按钮）
- 表单字段：手机号、密码、确认密码、6 位邀请码
- 两次密码一致性校验
- 模拟注册成功后跳回登录页

---

### 4.10 详情页

**涉及文件：** [src/views/Detail/index.vue](src/views/Detail/index.vue)

**实现状态：** 仅骨架，待开发

**当前内容：**
- 从路由参数 `route.params.id` 获取景点 ID
- 展示占位文字提示"等后端接口"
- 顶部导航栏（返回按钮）

---

## 五、路由表

**涉及文件：** [src/router/index.ts](src/router/index.ts)

| 路径 | 名称 | 页面 | hideTabBar | 说明 |
|------|------|------|------------|------|
| `/` | - | - | - | 自动重定向至 `/map` |
| `/login` | Login | Login | - | 登录页 |
| `/map` | Map | Map | - | 主地图页 |
| `/detail/:id` | Detail | Detail | true | 景点详情页 |
| `/register` | Register | Register | true | 注册页 |

路由模式：`createWebHistory()`（HTML5 History）

---

## 六、状态管理

### authStore（[stores/auth.ts](src/stores/auth.ts)）

| 字段/方法 | 类型 | 说明 |
|-----------|------|------|
| `token` | `string` | JWT token，持久化到 `localStorage` |
| `role` | `string` | 角色：`user` / `guest` / `admin` |
| `loginSuccess(token, role)` | action | 登录成功，保存凭据 |
| `setGuest()` | action | 设为游客模式 |
| `logout()` | action | 清空凭据 |

### audioStore（[stores/audio.ts](src/stores/audio.ts)）

| 字段/方法 | 类型 | 说明 |
|-----------|------|------|
| `player` | `Audio` | 原生 Audio 实例 |
| `currentSpot` | `ScenicSpot \| null` | 当前播放的景点 |
| `isPlaying` | `boolean` | 播放状态 |
| `currentTime` | `number` | 当前播放进度（秒） |
| `duration` | `number` | 总时长（秒） |
| `settings` | `AudioSettings` | 播放设置 |
| `playedSpotIds` | `Set<number>` | 已播景点 ID 集合 |
| `playScenicAudio(spot)` | action | 播放景点音频 |
| `pause()` | action | 暂停 |
| `togglePlay()` | action | 播放/暂停切换 |
| `seek(time)` | action | 跳转到指定时间 |
| `cycleSpeed()` | action | 循环切换倍速 |

### locationStore（[stores/location.ts](src/stores/location.ts)）

| 字段/方法 | 类型 | 说明 |
|-----------|------|------|
| `latitude` | `number` | 当前纬度 |
| `longitude` | `number` | 当前经度 |
| `hasPermission` | `boolean` | 是否已获取定位权限 |
| `currentScenicId` | `string` | 当前选中景点 ID |
| `updateLocation(lat, lng)` | action | 更新位置坐标 |

---

## 七、类型定义

**涉及文件：** [src/types/api.d.ts](src/types/api.d.ts)

```typescript
// 用户信息（对应 users 表）
interface UserInfo {
  id: number;
  phone: string;
  avatar_url: string | null;
  role: 'USER' | 'ADMIN';
  status: 1 | 2 | 3;  // 1-正常, 2-冻结, 3-注销
}

// 景点信息（对应 scenic_spots 表）
interface ScenicSpot {
  id: number;
  name: string;
  description: string;
  image_url: string;
  audio_url: string;
  latitude: number;
  longitude: number;
  radius: number;   // 触发播放的感应半径（米）
}

// 用户播放设置（对应 user_audio_settings 表）
interface AudioSettings {
  auto_play_enabled: 0 | 1;       // 0-关闭, 1-开启
  repeat_policy: 1 | 2;           // 1-播一次, 2-播多次
  switch_policy: 1 | 2 | 3;       // 1-播完再切, 2-随位置切, 3-弹窗提醒
  background_play_enabled: 0 | 1; // 0-关闭, 1-开启
  default_speed: 1.0 | 1.2 | 1.5 | 2.0;
}

// 通用响应格式（所有接口统一包裹）
interface BaseResponse<T = any> {
  code: number;
  message: string;
  data: T;
}
```

---

## 八、后端接口需求（重要）

> 当前前端所有 API 调用均为 `setTimeout` 模拟的 mock 数据。
> 后端接口就绪后，前端只需修改 [src/api/](src/api/) 目录下的文件，将 mock 替换为真实 Axios 请求即可。
> 所有接口统一使用 `BaseResponse<T>` 格式包裹响应。

### 8.1 接口总览

| 序号 | 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|------|
| 1 | POST | `/api/v1/user/login` | 用户登录 | 否 |
| 2 | POST | `/api/v1/user/register` | 用户注册 | 否 |
| 3 | PUT | `/api/v1/user/password` | 修改密码 | 是 |
| 4 | GET | `/api/v1/user/profile` | 获取用户信息 | 是 |
| 5 | POST | `/api/v1/scenic/current` | 获取当前位置附近景点 | 是 |
| 6 | GET | `/api/v1/scenic/list` | 获取全部景点列表 | 否 |
| 7 | GET | `/api/v1/scenic/detail/:id` | 获取景点详情 | 否 |
| 8 | GET | `/api/v1/scenic/search` | 搜索景点 | 否 |
| 9 | PUT | `/api/v1/scenic/update` | 更新景点信息 | 管理员 |
| 10 | GET | `/api/v1/audio/settings` | 获取用户音频设置 | 是 |
| 11 | PUT | `/api/v1/audio/settings` | 更新用户音频设置 | 是 |

### 8.2 接口详细规范

---

#### 1. 用户登录

```
POST /api/v1/user/login
```

**请求体：**
```json
{
  "phone": "13800138000",
  "password": "12345678"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "role": "user"
  }
}
```

**说明：**
- `role` 取值：`user` / `admin`
- token 建议使用 JWT，过期时间 >= 7 天
- 前端将 token 存入 `localStorage` 的 `SWU_TOKEN` 键

---

#### 2. 用户注册

```
POST /api/v1/user/register
```

**请求体：**
```json
{
  "phone": "13800138000",
  "password": "12345678",
  "invite_code": "ABC123"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {}
}
```

**说明：**
- 邀请码为 6 位，用于限制注册。需后端维护有效的邀请码列表。
- 密码建议前端传明文，后端 bcrypt 加密存储。
- 手机号需唯一性校验。

---

#### 3. 修改密码

```
PUT /api/v1/user/password
Authorization: Bearer <token>
```

**请求体：**
```json
{
  "old_password": "12345678",
  "new_password": "newpass123"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": {}
}
```

**说明：**
- 需要验证原密码正确性
- 修改成功后，前端会清空 token 并跳转登录页

---

#### 4. 获取用户信息

```
GET /api/v1/user/profile
Authorization: Bearer <token>
```

**响应：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "phone": "13800138000",
    "avatar_url": "https://example.com/avatar.jpg",
    "role": "USER",
    "status": 1
  }
}
```

**说明：**
- 前端个人中心页面（左侧抽屉）目前未调用此接口，预留用于展示用户头像和手机号
- `status`：1=正常, 2=冻结, 3=注销

---

#### 5. 获取当前位置附近景点

```
POST /api/v1/scenic/current
Authorization: Bearer <token>
```

**请求体：**
```json
{
  "latitude": 29.815,
  "longitude": 106.425
}
```

**响应：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "scenic_id": 1,
    "name": "共青团花园",
    "image_url": "https://example.com/scenic.jpg",
    "description": "西南大学标志性景观之一...",
    "audio_url": "https://example.com/audio.mp3",
    "distance": 50,
    "latitude": 29.815,
    "longitude": 106.425,
    "radius": 50,
    "has_audio": true
  }
}
```

**说明：**
- 后端根据用户坐标，计算并返回距离最近的景点（在感应半径内）
- `distance` 为用户与景点的直线距离，单位米
- `has_audio` 表示该景点是否配置了音频讲解
- 如果用户不在任何景点的感应范围内，`data` 返回 `null`

---

#### 6. 获取全部景点列表

```
GET /api/v1/scenic/list
```

**响应：**
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 1,
      "name": "共青团花园",
      "description": "...",
      "image_url": "...",
      "audio_url": "...",
      "latitude": 29.815,
      "longitude": 106.425,
      "radius": 50
    },
    {
      "id": 2,
      "name": "崇德湖",
      "description": "...",
      "image_url": "...",
      "audio_url": "...",
      "latitude": 29.818,
      "longitude": 106.428,
      "radius": 80
    }
  ]
}
```

**说明：**
- 用于地图上渲染所有景点 Marker
- 前端目前仅硬编码了一个景点，此接口就绪后可展示全部景点

---

#### 7. 获取景点详情

```
GET /api/v1/scenic/detail/:id
```

**响应：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "name": "共青团花园",
    "description": "详细的长文本介绍，可能包含多段落...",
    "image_url": "https://example.com/scenic.jpg",
    "images": [
      "https://example.com/scenic1.jpg",
      "https://example.com/scenic2.jpg"
    ],
    "audio_url": "https://example.com/audio.mp3",
    "latitude": 29.815,
    "longitude": 106.425,
    "radius": 50
  }
}
```

**说明：**
- `images` 为轮播图数组，支持多张图片展示
- `description` 为完整的介绍文本（非摘要）
- 详情页目前仅有骨架，需前后端同步开发

---

#### 8. 搜索景点

```
GET /api/v1/scenic/search?keyword=花园
```

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 是 | 搜索关键词 |

**响应：**
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 1,
      "name": "共青团花园",
      "description": "西南大学标志性景观之一...",
      "latitude": 29.815,
      "longitude": 106.425,
      "radius": 50
    }
  ]
}
```

**说明：**
- 支持模糊匹配景点名称
- 返回结果按匹配度排序

---

#### 9. 更新景点信息（管理员）

```
PUT /api/v1/scenic/update
Authorization: Bearer <token>  （需管理员角色）
```

**请求体：**
```json
{
  "id": 1,
  "name": "共青团花园",
  "latitude": 29.815,
  "longitude": 106.425,
  "radius": 50,
  "description": "更新后的介绍文本..."
}
```

**响应：**
```json
{
  "code": 200,
  "message": "修改成功",
  "data": {}
}
```

**说明：**
- 需要校验 token 对应的角色为 `admin`
- 返回修改后的完整景点数据或仅返回成功状态均可
- `updated_by` 和 `updated_at` 由后端自动记录

---

#### 10. 获取用户音频设置

```
GET /api/v1/audio/settings
Authorization: Bearer <token>
```

**响应：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "auto_play_enabled": 1,
    "repeat_policy": 1,
    "switch_policy": 1,
    "background_play_enabled": 0,
    "default_speed": 1.0
  }
}
```

---

#### 11. 更新用户音频设置

```
PUT /api/v1/audio/settings
Authorization: Bearer <token>
```

**请求体：**
```json
{
  "auto_play_enabled": 1,
  "repeat_policy": 2,
  "switch_policy": 2,
  "background_play_enabled": 1,
  "default_speed": 1.5
}
```

**响应：**
```json
{
  "code": 200,
  "message": "保存成功",
  "data": {}
}
```

**说明：**
- 前端目前设置保存在 Pinia store 中（内存），刷新后丢失
- 后端接口就绪后，前端会在设置变更时同步到服务端，并在登录后从服务端拉取

---

### 8.3 数据库表建议

> 以下为建议的表结构，字段名与前端的 TypeScript 类型对齐。

**users（用户表）**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int / bigint (PK) | 用户 ID |
| phone | varchar(11) | 手机号，唯一索引 |
| password_hash | varchar(255) | bcrypt 加密后的密码 |
| avatar_url | varchar(500) | 头像 URL，可空 |
| role | enum('USER','ADMIN') | 角色 |
| status | tinyint | 1=正常, 2=冻结, 3=注销 |
| created_at | datetime | 注册时间 |
| updated_at | datetime | 最后更新时间 |

**scenic_spots（景点表）**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int / bigint (PK) | 景点 ID |
| name | varchar(100) | 景点名称 |
| description | text | 详细介绍 |
| image_url | varchar(500) | 封面图 URL |
| audio_url | varchar(500) | 语音讲解 MP3 URL，可空 |
| latitude | decimal(10,7) | 纬度 |
| longitude | decimal(10,7) | 经度 |
| radius | int | 感应半径（米） |
| updated_by | int | 最后修改人 ID |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 最后更新时间 |

**user_audio_settings（用户音频设置表）**

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| id | int / bigint (PK) | - | 主键 |
| user_id | int (FK → users.id) | - | 用户 ID，唯一 |
| auto_play_enabled | tinyint | 0 | 0=关, 1=开 |
| repeat_policy | tinyint | 1 | 1=播一次, 2=播多次 |
| switch_policy | tinyint | 1 | 1=播完再切, 2=随位置切, 3=弹窗 |
| background_play_enabled | tinyint | 0 | 0=关, 1=开 |
| default_speed | decimal(2,1) | 1.0 | 1.0 / 1.2 / 1.5 / 2.0 |
| updated_at | datetime | - | 最后更新时间 |

**invite_codes（邀请码表）**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int (PK) | 主键 |
| code | varchar(6) | 邀请码，唯一 |
| is_used | tinyint | 0=未使用, 1=已使用 |
| used_by | int | 使用者 ID |
| created_at | datetime | 创建时间 |

**scenic_images（景点轮播图表）**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int (PK) | 主键 |
| scenic_id | int (FK) | 关联景点 ID |
| image_url | varchar(500) | 图片 URL |
| sort_order | int | 排序序号 |

---

### 8.4 非功能性需求

1. **认证方式：** 使用 JWT Bearer Token，前端在请求头 `Authorization: Bearer <token>` 中传递
2. **响应格式：** 所有接口统一使用 `BaseResponse<T>` 格式，其中 `code: 200` 表示成功，其他 code 表示业务错误
3. **错误码建议：**
   - `200` → 成功
   - `401` → 未认证 / token 过期
   - `403` → 权限不足
   - `404` → 资源不存在
   - `422` → 参数校验失败
   - `500` → 服务器错误
4. **文件存储：** 图片和音频建议使用对象存储（OSS / COS），数据库仅存 URL
5. **HTTPS：** 生产环境需强制 HTTPS（地理定位 API 要求安全上下文）
6. **CORS：** 开发环境需配置允许跨域，或前端 Vite 配置代理转发

---

## 九、前端待办事项

以下是后端接口就绪后前端需要对接的工作：

| 序号 | 事项 | 优先级 | 说明 |
|------|------|--------|------|
| 1 | 创建 `src/utils/request.ts` | 高 | 封装 Axios 实例，配置 baseURL、拦截器、token 注入 |
| 2 | 替换 `api/user.ts` 中的 mock | 高 | 将 loginApi 改为真实 Axios 请求 |
| 3 | 替换 `api/scenic.ts` 中的 mock | 高 | 将 fetchCurrentScenic 改为真实请求 |
| 4 | 完善详情页 `Detail/index.vue` | 高 | 接入景点详情接口，展示轮播图、完整介绍 |
| 5 | 地图展示全部景点 Marker | 中 | 接入景点列表接口，动态渲染所有 Marker |
| 6 | 注册页接入真实 API | 中 | 替换 `setTimeout` mock |
| 7 | 音频设置持久化 | 中 | 接入设置读写接口，登录后拉取远端设置 |
| 8 | 位置-音频自动联动 | 中 | 监听位置变化，自动检测是否进入景点感应区并触发播放 |
| 9 | 切换策略实现 | 低 | 实现三种切换策略的完整逻辑 |
| 10 | 搜索接入真实 API | 中 | 替换 mock 搜索逻辑 |
| 11 | 管理员编辑接入真实 API | 中 | 对接景点更新接口 |
| 12 | 密码修改接入真实 API | 中 | 替换 mock |
| 13 | Vite 配置代理 | 中 | 开发环境配置 `/api` 代理转发到后端 |
| 14 | AMap Key 环境变量化 | 低 | 将硬编码的 Key 和安全密钥移至 `.env` 文件 |

---

> **文档维护说明：** 本文档随项目迭代更新。前后端接口规范如有变更，请同步修改本文档对应章节。

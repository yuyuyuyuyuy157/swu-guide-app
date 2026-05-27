# 全面合规检查报告

> 检查日期：2026-05-24
> 对比依据：接口文档5.18.1.docx、业务逻辑.md、整体构思.md、nav.sql
> 检查对象：swu-guide-app 全部前端源码

---

## 目录

- [一、检查概览](#一检查概览)
- [二、接口文档对照检查（13 个接口逐一比对）](#二接口文档对照检查13-个接口逐一比对)
- [三、nav.sql 数据库对照检查](#三navsql-数据库对照检查)
- [四、业务逻辑对照检查](#四业务逻辑对照检查)
- [五、整体构思对照检查](#五整体构思对照检查)
- [六、严重问题汇总](#六严重问题汇总)
- [七、整改优先级建议](#七整改优先级建议)

---

## 一、检查概览

| 检查维度 | 对标文档 | 发现的问题数 |
|----------|----------|--------------|
| 接口字段匹配 | 接口文档5.18.1.docx | **12 个严重 + 5 个一般** |
| 数据库表结构 | nav.sql | **4 个严重** |
| 业务逻辑 | 业务逻辑.md | **6 个缺失功能** |
| 整体构思 | 整体构思.md | **4 个缺失功能** |

**总体结论：前端代码与接口文档存在大量字段名不一致、路径不一致、缺失接口调用等问题。在联调前必须完成字段对齐。**

---

## 二、接口文档对照检查（13 个接口逐一比对）

### 接口 1：用户密码登录 `POST /api/v1/user/login`

**前端涉及文件：** [api/user.ts](src/api/user.ts)、[views/Login/index.vue](src/views/Login/index.vue)、[stores/auth.ts](src/stores/auth.ts)

| 检查项 | 接口文档 | 当前前端 | 状态 |
|--------|----------|----------|------|
| 请求字段 | `phone`, `password`, `request_id` | 仅 `phone`, `password` | **缺失 `request_id`** |
| 响应 `data.token` | string | 已使用 | OK |
| 响应 `data.user_id` | string | **未使用** | **缺失** |
| 响应 `data.phone` | 中间4位打码 | 返回完整号码 | **不一致** |
| 响应 `data.avatar` | 头像 URL | **未使用** | **缺失** |
| 响应 `data.play_settings` | 播放设置对象 | **未使用**（登录后未同步设置） | **缺失** |
| 返回 role 值 | `"user"` / `"admin"` | 前端 mock 返回 `"user"` / `"admin"` | OK |

**需要修改：**
1. `api/user.ts` 的 `loginApi` 需要增加 `request_id`（UUID v4）
2. `stores/auth.ts` 需要存储 `user_id`、`avatar`、`phone`（脱敏后）
3. `stores/auth.ts` 登录成功后需将 `play_settings` 同步到 `audioStore`

---

### 接口 2：用户注册 `POST /api/v1/user/register`

**前端涉及文件：** [views/Register/index.vue](src/views/Register/index.vue)

| 检查项 | 接口文档 | 当前前端 | 状态 |
|--------|----------|----------|------|
| 请求字段 | `phone`, `password`, `confirm_password`, `invite_code`, `request_id` | `phone`, `password`, `confirmPwd`, `inviteCode` | **缺少 `request_id`；字段名风格不一致** |
| 响应 | 返回 token/user_id/phone/avatar/role/play_settings | 仅 showToast 后跳转 | **未使用响应数据，注册后应自动登录** |

**需要修改：**
1. 增加 `request_id` 生成逻辑
2. 字段名改为下划线风格（`confirm_password`、`invite_code`）或确认后端接受驼峰
3. 注册成功后使用返回的 token 自动登录，而非跳回登录页

---

### 接口 3：获取个人信息 `GET /api/v1/user/profile`

**前端涉及文件：** [views/Map/index.vue](src/views/Map/index.vue)（个人中心抽屉）、[stores/auth.ts](src/stores/auth.ts)

| 检查项 | 接口文档 | 当前前端 | 状态 |
|--------|----------|----------|------|
| 整体接口 | GET 请求，返回 user_id/phone/avatar/create_time | **未调用此接口** | **缺失** |

**当前问题：**
- 个人抽屉中的手机号是硬编码的（`'13800138000'` / `'13900139000'`）
- 头像使用固定 URL（猫图）
- 缺少 `create_time` 展示

**需要修改：**
1. `api/user.ts` 新增 `getUserProfile` 接口函数
2. `Map/index.vue` 个人抽屉加载时调用该接口
3. `authStore` 增加 `phone` 和 `avatar` 字段

---

### 接口 4：修改密码 `POST /api/v1/user/change-password`

**前端涉及文件：** [components/PasswordModal.vue](src/components/PasswordModal.vue)

| 检查项 | 接口文档 | 当前前端 | 状态 |
|--------|----------|----------|------|
| 接口路径 | `/api/v1/user/change-password` | mock：无真实请求 | **路径未定义** |
| 请求字段 | `old_password`, `new_password`, `confirm_new_password`, `request_id` | `oldPassword`, `newPassword`, `confirmPassword` | **字段名不一致 + 缺少 `request_id`** |
| 限流 | 1分钟最多3次 | 未处理 | 缺失 |

**需要修改：**
1. `api/user.ts` 新增 `changePassword` 接口函数，路径使用文档中的 `/api/v1/user/change-password`
2. 字段名对齐为下划线风格
3. 增加 `request_id`

---

### 接口 5：上传头像 `POST /api/v1/user/upload-avatar`

**前端涉及文件：** 无

| 检查项 | 状态 |
|--------|------|
| 整个接口 | **完全未实现** |

**需要修改：**
- 前端目前头像不可更换，需在个人中心增加头像上传功能
- 使用 `multipart/form-data` 格式上传
- 上传成功后更新界面头像

---

### 接口 6：获取当前位置景点信息 `POST /api/v1/scenic/current`

**前端涉及文件：** [api/scenic.ts](src/api/scenic.ts)、[components/ScenicCard.vue](src/components/ScenicCard.vue)

| 检查项 | 接口文档 | 当前前端 | 状态 |
|--------|----------|----------|------|
| 响应字段名 | `scenic_id` | `scenic_id` | OK |
| 响应字段名 | `image` | mock 用 `image`，ScenicCard 用 `image_url` | **不一致** |
| 响应字段名 | `intro` | mock 用 `intro`，类型定义用 `description` | **不一致** |
| 响应字段名 | `induction_range` | mock 用 `radius`/`distance` | **不一致** |
| 景点 ID 类型 | `string` | mock 用 `'s001'`（字符串），ScenicCard 用 `id: 1`（数字） | **类型不一致** |
| 权限 | 接口文档标注"无需登录" | 前端在 ScenicCard 中未强制登录 | OK |

**需要修改：**
1. 统一类型 `ScenicSpot` 与接口文档对齐
2. `image_url` → `image`
3. `description` → `intro`
4. `radius` → `induction_range`
5. `id: number` → `scenic_id: string`

---

### 接口 7：搜索景点 `GET /api/v1/scenic/search`

**前端涉及文件：** [components/SearchPanel.vue](src/components/SearchPanel.vue)

| 检查项 | 接口文档 | 当前前端 | 状态 |
|--------|----------|----------|------|
| 请求参数 | `keyword`, `page`, `page_size` | 仅 `keyword` | **缺少分页参数** |
| 响应格式 | `{ total, list[] }` | 直接使用数组 `results` | **响应解析需改造** |
| 响应字段名 | `scenic_id`, `image`, `distance` | 前端使用 `id`, 无 `distance` 展示 | **不一致** |
| 权限 | 接口文档标注"需要登录" | 前端已限制游客不可搜索 | OK |

**需要修改：**
1. 增加 `page`、`page_size` 参数
2. 搜索结果解析适配 `{ total, list }` 格式
3. 字段名对齐

---

### 接口 8：手动设置位置 `POST /api/v1/scenic/set-position`

**前端涉及文件：** 无

| 检查项 | 状态 |
|--------|------|
| 整个接口 | **完全未实现** |

**当前问题：** 整体构思.md 提到"没有定位权限时可手动输入位置"，但前端无此功能。

**需要修改：**
1. 新增手动选择景点的功能（搜索选择景点后设置当前位置）
2. `api/scenic.ts` 新增 `setPosition` 接口

---

### 接口 9：获取景点语音详情 `GET /api/v1/audio/detail`

**前端涉及文件：** [stores/audio.ts](src/stores/audio.ts)

| 检查项 | 接口文档 | 当前前端 | 状态 |
|--------|----------|----------|------|
| 整体接口 | 通过 `audio_id` 获取音频 URL、时长、上次进度 | 直接从景点数据中的 `audio_url` 播放 | **未调用此接口** |
| 断点续播 | 返回 `last_progress` | **未实现** | **缺失** |

**需要修改：**
1. `api/scenic.ts` 或新建 `api/audio.ts`，增加 `getAudioDetail` 接口
2. 播放音频前先调用此接口获取真实 URL 和上次进度
3. 实现断点续播功能（从 `last_progress` 开始播放）

---

### 接口 10：上报播放进度 `POST /api/v1/audio/report-progress`

**前端涉及文件：** [stores/audio.ts](src/stores/audio.ts)

| 检查项 | 状态 |
|--------|------|
| 整个接口 | **完全未实现** |

**需要修改：**
1. 新增 `api/audio.ts`，实现 `reportProgress` 接口
2. 在 `audioStore` 中增加定时上报逻辑（限流：每 10 秒最多 1 次）
3. 在音频播放 `ended` 事件中上报 `is_complete: true`

---

### 接口 11：保存播放设置 `POST /api/v1/audio/save-settings`

**前端涉及文件：** [stores/audio.ts](src/stores/audio.ts)、[types/api.d.ts](src/types/api.d.ts)

| 检查项 | 接口文档字段 | 当前前端字段 | 状态 |
|--------|-------------|-------------|------|
| 自动播放 | `auto_play` (boolean) | `auto_play_enabled` (0\|1) | **字段名和类型都不同** |
| 重复模式 | `repeat_mode` (int) | `repeat_policy` (1\|2) | **字段名不同** |
| 切换模式 | `play_switch_mode` (int) | `switch_policy` (1\|2\|3) | **字段名不同** |
| 后台播放 | `background_play` (boolean) | `background_play_enabled` (0\|1) | **字段名和类型都不同** |
| 倍速 | `play_speed` (float) | `default_speed` (float) | **字段名不同** |
| 快进退时长 | `backward_forward_duration` (int) | **不存在** | **完全缺失** |
| 倍速可选值 | 0.5/1.0/1.2/1.5/2.0 | 1.0/1.2/1.5/2.0 | **缺 0.5x** |
| 请求字段 | 含 `request_id` | 无 | **缺失** |

**需要修改：这是差异最大的接口，所有字段需重新对齐！**
1. `types/api.d.ts` 中的 `AudioSettings` 整个接口需重构
2. `stores/audio.ts` 中 settings 初始化和读写全部更新
3. 新增 `backward_forward_duration` 的快进/快退 UI 功能
4. 倍速增加 0.5x 选项

---

### 接口 12：获取播放设置 `GET /api/v1/audio/get-settings`

**前端涉及文件：** 无

| 检查项 | 状态 |
|--------|------|
| APP 启动时获取设置 | **未实现** |

**需要修改：**
1. App 启动时（`App.vue` 或路由守卫中），若已登录则调用此接口
2. 将返回的设置同步到 `audioStore.settings`

---

### 接口 13：获取 APP 全局配置 `GET /api/v1/system/config`

**前端涉及文件：** [stores/location.ts](src/stores/location.ts)、[views/Map/index.vue](src/views/Map/index.vue)

| 检查项 | 状态 |
|--------|------|
| 默认坐标、协议地址等全局配置 | **硬编码在前端代码中** |

**当前问题：**
- `location.ts` 默认坐标 `[106.425, 29.815]` 写死
- `Map/index.vue` 地图中心也是写死的坐标
- 用户协议和隐私政策地址未定义

**需要修改：**
1. `api/system.ts`（新建）实现 `getAppConfig`
2. App 启动时调用，将配置注入 `locationStore` 或单独的 config store

---

### 接口 14-18：管理员模块（5 个接口）

**前端涉及文件：** [components/AdminDrawer.vue](src/components/AdminDrawer.vue)

| 接口 | 状态 | 说明 |
|------|------|------|
| `POST /api/v1/admin/login` | **未实现** | 前端用同一 `/api/v1/user/login` 处理管理员 |
| `GET /api/v1/admin/scenic/list` | **未实现** | 前端无管理员景点列表页 |
| `GET /api/v1/admin/scenic/detail` | **未实现** | 前端直接复用地图片区的景点数据 |
| `POST /api/v1/admin/scenic/update` | **字段不一致** | 接口用 `induction_range`/`intro`，前端用 `radius`/`description` |
| 审计日志 | **未使用** | SQL 有 `scenic_spots_audit_log` 表，前端未展示变更历史 |

**需要修改：**
1. 确认管理员是否走独立登录还是共用 `/api/v1/user/login`
2. 实现管理员景点列表页
3. 字段名对齐

---

## 三、nav.sql 数据库对照检查

### 3.1 表结构与前端类型对比

**表：users ↔ 前端类型 UserInfo**

| SQL 字段 | 前端类型 UserInfo | 状态 |
|----------|-------------------|------|
| `id` BIGINT | `id: number` | **类型不匹配**（BIGINT > number 安全范围） |
| `phone` VARCHAR(20) | `phone: string` | OK |
| `avatar_url` VARCHAR(512) | `avatar_url: string \| null` | OK |
| `role` VARCHAR(20) | `role: 'USER' \| 'ADMIN'` | OK |
| `status` TINYINT | `status: 1 \| 2 \| 3` | OK |
| `password_last_changed_at` | **不存在** | **缺失字段** |
| `is_deleted` | **不存在** | **缺失字段** |
| `created_at` | **不存在** | **缺失字段**（接口文档有 `create_time`） |
| `updated_at` | **不存在** | **缺失字段** |
| `deleted_at` | **不存在** | **缺失字段** |

**表：scenic_spots ↔ 前端类型 ScenicSpot**

| SQL 字段 | 前端类型 ScenicSpot | 状态 |
|----------|---------------------|------|
| `id` BIGINT | `id: number` | **类型不匹配** |
| `name` VARCHAR(100) | `name: string` | OK |
| `description` TEXT | `description: string` | OK（但接口文档用 `intro`） |
| `image_url` VARCHAR(512) | `image_url: string` | OK（但接口文档用 `image`） |
| `audio_url` VARCHAR(512) | `audio_url: string` | OK |
| `location` POINT | **不存在** | **缺失（空间坐标专用字段）** |
| `radius` INT | `radius: number` | OK（但接口文档用 `induction_range`） |
| `updated_by` BIGINT | **不存在** | **缺失字段** |
| `is_deleted` | **不存在** | **缺失字段** |
| `deleted_at` | **不存在** | **缺失字段** |

**表：user_audio_settings ↔ 前端类型 AudioSettings**

| SQL 字段 | 前端类型 AudioSettings | 与接口文档差异 |
|----------|----------------------|----------------|
| `user_id` | 缺失 | 前端 store 不存 user_id |
| `auto_play_enabled` | 有 | 接口文档叫 `auto_play`(boolean) |
| `repeat_policy` | 有 | 接口文档叫 `repeat_mode`(int) |
| `switch_policy` | 有 | 接口文档叫 `play_switch_mode`(int) |
| `background_play_enabled` | 有 | 接口文档叫 `background_play`(boolean) |
| `default_speed` | 有 | 接口文档叫 `play_speed`(float)，且多了 0.5x |

**表：user_playback_history**

| 状态 | 说明 |
|------|------|
| **前端无对应实现** | 前端用 `playedSpotIds: Set<number>` 内存存储，刷新后丢失 |

**表：scenic_spots_audit_log**

| 状态 | 说明 |
|------|------|
| **前端无对应实现** | 审计日志表已设计但前端未展示变更历史 |

**表：invitation_codes**

| 状态 | 说明 |
|------|------|
| **前端未对接** | 注册页仅校验邀请码非空，不处理"已过期""已作废"等状态 |

---

### 3.2 SQL 发现的其他问题

**SQL 语法错误：** `nav.sql` 第 94 行外键约束语法有误：
```sql
-- 当前（错误）：
CONSTRAINT `fk_history_spot_id` FOREIGN KEY (`scenic_spots`.`id` 对应的旧外键已解绑) REFERENCES ...

-- 应改为：
CONSTRAINT `fk_history_spot_id` FOREIGN KEY (`spot_id`) REFERENCES `scenic_spots` (`id`) ON DELETE CASCADE
```

---

## 四、业务逻辑对照检查

**对标文档：** [业务逻辑.md](业务逻辑.md)

| 业务逻辑要求 | 前端实现 | 状态 |
|-------------|----------|------|
| 启动屏 | 无 | **缺失** |
| 权限申请授权弹窗 | 直接显示地图 | **缺失** — 无定位权限引导 |
| 首次触发自动播放 Modal | 无"是否开启自动讲解"询问 | **缺失** — 直接播放 |
| 触碰震动反馈 (Haptic) | 无 | **缺失** |
| 无定位权限红色 Banner | 无（静默使用默认坐标） | **缺失** |
| 搜索无结果插画 | Vant Empty 组件 | OK |
| 弱网超时重试 | 音频加载失败 toast，无重试 | **部分缺失** |
| 管理员列表页 | 无（仅单景点编辑抽屉） | **缺失** |
| 经纬度输入格式校验 | 正则 `/^\d+(\.\d+)?$/` | OK |
| 防误触二次确认 | showConfirmDialog | OK |
| 手机号脱敏 | `138****8000` | OK |
| 密码明暗文切换 | eye-o / closed-eye 图标 | OK |
| 强制重登流转 | 延迟 1.5s 跳转 | OK |
| 音频设置总开关联动置灰 | filter grayscale + pointer-events | OK |
| 后台权限引导弹窗 | showConfirmDialog 模拟 | **部分实现**（未真正跳转系统设置） |
| 切换策略"弹窗提醒"联动 | UI 选项存在但未连接实际逻辑 | **缺失** |

---

## 五、整体构思对照检查

**对标文档：** [整体构思.md](整体构思.md)

| 构思中的功能 | 前端实现 | 状态 |
|-------------|----------|------|
| 手动输入位置 / 手动修改当前位置 | 无 | **缺失** |
| AI 播放 | 无 | **缺失** |
| 卡片自动覆盖（搜索后自动弹卡片） | 搜索仅移动地图中心 | **缺失** |
| 地图坐标讲解灰色（未登录状态） | 无此视觉状态 | **缺失** |
| 无定位权限时手动输入 | 使用默认坐标兜底 | **偏差** |

---

## 六、严重问题汇总

### 严重（阻塞联调）

| # | 问题 | 涉及接口 |
|---|------|----------|
| 1 | **AudioSettings 字段全量不一致** — 7 个字段名与接口文档完全不同，类型也不同 | 接口 11、12 |
| 2 | **ScenicSpot 类型字段名混乱** — `image_url`/`image`、`description`/`intro`、`radius`/`induction_range` 三套命名混用 | 接口 6、7、8 |
| 3 | **缺少 `request_id` 生成** — 所有写操作接口都缺失此字段 | 接口 1、2、4、11、18 |
| 4 | **缺少 6 个完整接口调用** — 接口存在但前端未调用 | 接口 3、5、8、9、10、13、16、17 |
| 5 | **播放倍速缺 0.5x** — 接口文档支持 0.5 倍速，前端不支持 | 接口 11 |

### 一般（可后续修复）

| # | 问题 |
|---|------|
| 6 | 管理员模块：接口文档有独立 `/api/v1/admin/login`，前端与用户登录混用 |
| 7 | 管理员列表页未实现 |
| 8 | 断点续播未实现（接口 9 有 `last_progress` 支持） |
| 9 | 启动屏和权限引导弹窗缺失 |
| 10 | Haptic 震动反馈缺失 |
| 11 | 无定位权限的降级策略不完善（缺少手动选择位置） |
| 12 | `style.css` 中残留 Vite 模板样式（1126px 宽度限制等），与移动端应用冲突 |

---

## 七、整改优先级建议

### P0 — 联调前必须完成（阻塞后端对接）

1. **重构 [types/api.d.ts](src/types/api.d.ts) 的 `AudioSettings`**
   - 所有字段名对齐接口文档
   - 新增 `backward_forward_duration` 字段
   - 类型从 `0|1` 改为 `boolean`（或确认后端接受 0/1）

2. **统一 ScenicSpot 类型字段名**
   - 与后端确认最终命名规范（文档用 `intro`/`image`/`induction_range`/`scenic_id`）
   - 全局替换所有引用

3. **创建 [utils/request.ts](src/utils/request.ts)**
   - Axios 实例 + 拦截器 + token 注入 + `request_id` 自动生成

4. **重写 [api/](src/api/) 目录所有文件**
   - 替换 mock 为真实请求
   - 补齐缺失接口（profile、change-password、upload-avatar、audio/*、system/config、admin/*）

### P1 — 核心功能补充

5. 实现音频进度上报（接口 10）
6. 实现断点续播（接口 9）
7. 实现 APP 启动获取全局配置 + 用户设置（接口 12、13）
8. 实现管理员列表页

### P2 — 体验优化

9. 启动屏 + 权限引导
10. 手动选择位置功能
11. 弱网重试机制
12. 清理 `style.css` 残留样式

---

> **建议：** 将本报告同步给后端开发者，先与后端就字段命名规范达成一致（接口文档 vs SQL 已有部分差异，如 `intro` vs `description`），再开始前端整改。

# 后端代码审阅报告

> 审阅日期：2026-05-24
> 后端项目：nav-take-out-master（Spring Boot 3.2.5 + MyBatis-Plus）
> 前端项目：swu-guide-app（Vue 3 + Vant 4）

---

## 一、后端项目概况

| 维度 | 详情 |
|------|------|
| 框架 | Spring Boot 3.2.5 + Java 21 + 虚拟线程 |
| ORM | MyBatis-Plus 3.5.7 |
| 数据库 | MySQL 8.3 + Redis |
| 空间计算 | JTS + MySQL ST_Distance_Sphere（地理围栏） |
| 认证 | JWT（Auth0 java-jwt），7 天有效期 |
| 密码加密 | MD5(密码 + 手机号) — **不安全，建议改为 BCrypt** |
| API 文档 | Knife4j（Swagger） |
| 地图服务 | 百度地图（逆向地理编码） |

**模块结构：** `nav-common`（工具类）+ `nav-pojo`（实体/DTO/VO）+ `nav-server`（业务逻辑）

---

## 二、接口逐项对照（关键）

### 2.1 用户模块

| 功能 | 前端期望 | 后端实际 | 一致性 |
|------|----------|----------|--------|
| **登录** | `POST /api/v1/user/login` | `POST /api/v1/user/login` | 路径一致 |
| | 请求 `phone, password` | 请求 `phone, password, requestId` | 基本一致 |
| | 响应 `token, user_id, phone, avatar, role, play_settings` (snake_case) | 响应 `token, userId, phone, avatar, role, playSettings` (camelCase) | **字段命名风格不同** |
| **注册** | `POST /api/v1/user/register` | `POST /api/v1/user/register` | 路径一致 |
| | 请求 `phone, password, confirm_password, invite_code` | 请求 `phone, password, invitationCode` | **缺 confirm_password、字段名不同** |
| | 响应 `AuthResponse`（含 token 自动登录） | 响应 `"注册成功"`（纯字符串） | **严重：注册后无法自动登录** |
| **获取用户信息** | `GET /api/v1/user/profile` | `GET /api/v1/user/info/{userId}` | **路径不同、需要路径参数** |
| **修改密码** | `POST /api/v1/user/change-password` | **不存在** | **整个接口缺失** |
| **上传头像** | `POST /api/v1/user/upload-avatar` | **不存在** | **整个接口缺失** |
| **管理员登录** | `POST /api/v1/admin/login` | **AdminController.java 是空文件** | **整个接口缺失** |

### 2.2 景点模块

| 功能 | 前端期望 | 后端实际 | 一致性 |
|------|----------|----------|--------|
| **附近景点** | `POST /api/v1/scenic/current` | `POST /api/v1/scenic/current` | 路径一致 |
| | 响应字段 `scenic_id, image, intro, has_audio, audio_id, induction_range` (snake_case) | 响应字段 `scenicId, image, intro, hasAudio, audioId, inductionRange` (camelCase) | **字段命名风格不同** |
| **搜索景点** | `GET /api/v1/scenic/search?keyword=&page=&page_size=` | **不存在** | **整个接口缺失**（仅有 `/scenic/list` 返回全部） |
| **手动设位置** | `POST /api/v1/scenic/set-position` | **不存在** | **整个接口缺失** |

### 2.3 管理员模块

| 功能 | 前端期望 | 后端实际 | 一致性 |
|------|----------|----------|--------|
| **景点列表** | `GET /api/v1/admin/scenic/list` | `GET /api/v1/admin/scenic/list` | 路径一致 |
| **景点详情** | `GET /api/v1/admin/scenic/detail?scenic_id=` | `GET /api/v1/admin/scenic/detail/{id}` | **参数方式不同**（query vs path） |
| **更新景点** | `POST /api/v1/admin/scenic/update` | `PUT /api/v1/admin/scenic` | **方法不同、路径不同** |
| | 请求 `scenic_id, name, latitude, longitude, induction_range, intro` | 请求 `id, name, latitude, longitude, radius, description, imageUrl, audioUrl` | **字段名完全不同** |
| **新增景点** | 未对接 | `POST /api/v1/admin/scenic` | 后端已有，前端未用 |
| **删除景点** | 未对接 | `DELETE /api/v1/admin/scenic?ids=` | 后端已有，前端未用 |

### 2.4 音频模块

| 功能 | 前端期望 | 后端实际 |
|------|----------|----------|
| 获取音频详情 | `GET /api/v1/audio/detail` | **不存在** |
| 上报播放进度 | `POST /api/v1/audio/report-progress` | **不存在** |
| 保存播放设置 | `POST /api/v1/audio/save-settings` | **不存在** |
| 获取播放设置 | `GET /api/v1/audio/get-settings` | **不存在** |

**整个音频模块后端未实现。**

### 2.5 系统模块

| 功能 | 前端期望 | 后端实际 |
|------|----------|----------|
| 全局配置 | `GET /api/v1/system/config` | **不存在** |

---

## 三、编译问题（阻塞运行）

后端代码有 **2 个缺失类**，无法编译：

1. **`PageResult`** — `AdminScenicSpotServiceImpl` 和 `AdminScenicSpotController` 中引用，但项目中不存在
2. **`ScenicSpotPageQueryDTO`** — `AdminScenicSpotServiceImpl` 中引用，但项目中不存在

另有：
3. **`mapper/EmployeeMapper.xml`** 的 namespace 写的是 `AdminScenicSpotMapper`，不是 `EmployeeMapper`，文件名与内容不匹配
4. **`AdminController.java`** 是空文件，管理员登录接口未实现

---

## 四、安全问题

| 问题 | 严重程度 | 说明 |
|------|----------|------|
| **MD5 密码加密** | 高 | 使用 `MD5(password + phone)` 作为密码哈希，MD5 已被认为不安全。建议改为 BCrypt |
| **百度地图 AK 明文** | 中 | `baidu.map.ak: hw3a0UxUnQ6mHkEJPC7WcKxrJNA0s3Ap` 硬编码在 `application.yml` |
| **数据库密码明文** | 中 | `application-dev.yml` 中 `root/hwy1002kn.` 明文存储 |
| **无修改密码接口** | 中 | `UserServiceImpl` 有 `updatePassword()` 方法但没有对应的 Controller 暴露 |

---

## 五、字段命名风格差异汇总

前后端最大的协作障碍：**后端全部使用 camelCase，前端类型定义使用 snake_case。**

| 前端类型字段 | 后端返回字段 | 需统一 |
|-------------|-------------|--------|
| `scenic_id` | `scenicId` | 二选一 |
| `has_audio` | `hasAudio` | 二选一 |
| `audio_id` | `audioId` | 二选一 |
| `induction_range` | `inductionRange` | 二选一 |
| `user_id` | `userId` | 二选一 |
| `play_settings` | `playSettings` | 二选一 |
| `image_url` | `imageUrl` | 二选一 |
| `audio_url` | `audioUrl` | 二选一 |

> **建议**：后端 `application.yml` 已配置 `map-underscore-to-camel-case: true`，这意味着数据库的 `scenic_id` 会自动映射为 Java 的 `scenicId`，再通过 Jackson 序列化为 JSON 的 `scenicId`。让前端适配后端 camelCase 最简单——只需改前端类型定义即可。

---

## 六、缺失功能清单（给后端同学）

| 优先级 | 缺失内容 | 说明 |
|--------|----------|------|
| **P0** | `PageResult` 和 `ScenicSpotPageQueryDTO` 类 | 编译阻塞 |
| **P0** | 修改密码接口 | `UserServiceImpl.updatePassword()` 方法已写好，只需加 Controller 暴露 |
| **P0** | 音频模块（4 个接口） | `GET /audio/detail`、`POST /audio/report-progress`、`POST /audio/save-settings`、`GET /audio/get-settings` |
| **P1** | 搜索景点接口 | `GET /api/v1/scenic/search?keyword=&page=&page_size=` |
| **P1** | 管理员登录接口 | `AdminController.java` 是空文件 |
| **P1** | 注册接口返回完整数据 | 目前只返回字符串，应返回 token 等用于自动登录 |
| **P2** | 手动设置位置接口 | `POST /api/v1/scenic/set-position` |
| **P2** | 上传头像接口 | `POST /api/v1/user/upload-avatar` |
| **P2** | 全局配置接口 | `GET /api/v1/system/config` |
| **P2** | MD5 改 BCrypt | 安全加固 |

---

## 七、建议的协作对齐步骤

1. **统一字段命名风格** — 前后端商定 camelCase 还是 snake_case（推荐后端 camelCase，前端改类型定义适配）
2. **后端补齐 P0 缺失** — 编译问题 + 修改密码接口 + 音频模块
3. **确认景点字段名** — 后端用 `description/imageUrl/radius`，接口文档 5.18.1 用 `intro/image/induction_range`，需要统一
4. **注册接口返回 token** — 否则注册完还要手动登录一次
5. **修正 XML mapper 文件名** — `EmployeeMapper.xml` 应改名或修正 namespace

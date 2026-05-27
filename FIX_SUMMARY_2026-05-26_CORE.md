# 2026-05-26 核心功能排查与修复总结

## 一、核心问题根因

1. 搜索与自动播报失效的主因不是前端开关，而是后端 `scenic` 链路在运行时抛异常。  
   具体为：`NoSuchMethodError: ScenicSpotVO.setLatitude/setLongitude`，导致 `/api/v1/scenic/list`、`/api/v1/scenic/current` 返回 500。
2. 异常会连锁影响：
   - 搜索接口即使有数据，也可能前端显示为空或无法联动；
   - 当前景点识别失败，自动播报当然无法触发。

## 二、本次已完成修复

1. 后端 `ScenicSpotServiceImpl` 重写并兼容旧版 `ScenicSpotVO`：
   - 经纬度写入改为“反射可选写入”，即使运行时类里没有 `setLatitude/setLongitude` 也不会崩溃；
   - `/scenic/list`、`/scenic/current` 恢复稳定返回。
2. 搜索跳转链路补强：
   - 新增接口：`GET /api/v1/scenic/location/{scenicId}`；
   - 前端搜索结果点击时，若结果里无坐标，自动补查坐标再定位。
3. 地图页与景点卡片核心逻辑清理：
   - `Map/index.vue`：搜索点击后强制定位、中心跳转、地图 marker 同步；
   - `ScenicCard.vue`：范围触发自动播报、无音频走 AI 朗读、轮询与定位变化联动。
4. 管理员账号状态能力保持可用：
   - 接口 `PUT /api/v1/admin/users/{id}/status` 可直接操作管理员账号状态；
   - 管理员角色本身不允许在前台被改动（仅状态可改，符合你的要求）。

## 三、实测验证结果

1. 编译通过：
   - `mvn compile` 成功；
   - `npm run build` 成功。
2. 搜索链路验证：
   - `/api/v1/scenic/list`：200，返回 31 条景点；
   - 登录后 `/api/v1/scenic/search?keyword=31教`：200，命中 31 教；
   - 非登录访问搜索：401（符合“未登录不可搜索”）。
3. 自动识别链路验证：
   - `/api/v1/scenic/current` 在 31 教坐标可正确返回 31 教；
   - 对数据库中全部 31 个景点逐点校验，`current` 识别结果 **0 个不匹配**（全量通过）。
4. 管理员状态验证：
   - 管理员 token 调用 `/api/v1/admin/users/1/status` 返回 200，状态更新成功。

## 四、改动文件（本轮）

- `D:/SwuProject/swu-guide-app/nav-take-out-master/nav-server/src/main/java/com/nav/service/ScenicSpotService.java`
- `D:/SwuProject/swu-guide-app/nav-take-out-master/nav-server/src/main/java/com/nav/service/impl/ScenicSpotServiceImpl.java`
- `D:/SwuProject/swu-guide-app/nav-take-out-master/nav-server/src/main/java/com/nav/controller/ScenicSpotController.java`
- `D:/SwuProject/swu-guide-app/nav-take-out-master/nav-server/src/main/java/com/nav/vo/ScenicSpotLocationVO.java`
- `D:/SwuProject/swu-guide-app/src/api/scenic.ts`
- `D:/SwuProject/swu-guide-app/src/types/api.d.ts`
- `D:/SwuProject/swu-guide-app/src/views/Map/index.vue`
- `D:/SwuProject/swu-guide-app/src/components/ScenicCard.vue`

## 五、针对本轮 7 个测试问题的修复点

1. 范围内未自动播报  
   - `ScenicCard` 增强为：定位成功后持续轮询识别；命中新景点立即自动播报；音频自动播放失败时回退到 AI 朗读，避免“无声失败”。
2. 游客点击播报闪退  
   - 游客模式不再直接执行播放逻辑，改为“登录后讲解”按钮，点击后弹确认并跳转登录页。
3. 详情页图片加载失败  
   - 详情页不再走管理员详情接口，改为走公开景点列表按 `scenicId` 查详情；并加图片 `error` 回退处理。
4. 用户登录提示“网络繁忙”  
   - 重写 `UserServiceImpl` 异常链路，登录失败改为业务异常（400）返回明确原因，不再被兜底成 500“系统繁忙”。
5. 默认定位问题  
   - 保留地图默认中心仅用于展示，但景点识别和自动播报现在要求 `hasPermission=true`，不再拿默认坐标当“真实定位”触发逻辑。
6. 讲解进度条消失  
   - 在 `main.ts` 启动时初始化 `audioStore.initAudio()`，恢复音频时间事件监听，进度条实时更新。
7. 讲解结束后景点卡片消失  
   - `ScenicCard` 对定位漂移做“保留窗口”与多次空识别判定，不会因为短暂 GPS 抖动立即变成“附近暂无景点”。

## 六、后端启动冲突处理（端口 8080 占用）

- 根因：历史 `java/mvn` 进程残留导致 Spring Boot 报 `Port 8080 was already in use`。  
- 处理：清理残留 Java 进程后重新启动。  
- 当前状态：后端已成功监听 `8080`，`/api/v1/scenic/list` 与 `/api/v1/user/login` 实测均返回 `200`。

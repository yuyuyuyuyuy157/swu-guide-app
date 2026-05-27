# 2026-05-25 补充修复总结

- 修复图片无法显示：`vite.config.ts` 新增 `/download` 代理，将前端开发环境的图片访问转发到后端 `http://localhost:8080/download/**`。
- 修复景点/用户列表无法继续滑动：全局页面从强制 `overflow: hidden` 调整为允许页面滚动，地图页单独保持全屏固定体验。
- 修复用户管理只能查看不能编辑：新增 `PUT /api/v1/admin/users/{id}`，管理员可在独立用户管理页编辑用户角色和账号状态。
- 优化用户管理页面交互：点击用户行打开底部编辑面板，支持普通用户/管理员、正常/冻结/注销状态切换，保存后自动刷新列表。
- 修复搜索功能不稳定：`GET /api/v1/scenic/search` 放行访问，并增强空关键词、大小写和空字段处理。
- 修复默认自动播放：前端默认 `autoPlay=true`，后端没有用户设置时也返回自动播放开启；登录/注册时改为合并播放设置，避免空对象覆盖默认值。
- 清理并重写部分后端控制器/音频服务文件，避免乱码注释挤压代码造成维护和编译风险。
- 修复 Vite 开发环境无法解析 `.vue` 文件：将配置从 `vite.config.ts` 调整为 `vite.config.js`，并同步 `tsconfig.node.json`，确保 dev server 和 build 都能加载 Vue 插件。
- 修复搜索景点链路：搜索入口不再拦截游客，搜索面板右侧按钮改为明确“搜索”，点击结果后立即定位地图、弹出景点卡片，并按规则自动播报。
- 新增搜索后自动播报：有正式音频且非游客时播放景点音频；游客或没有音频时使用浏览器语音朗读景点名称和简介。
- 修复管理员编辑景点图片无法移除：景点编辑上传控件显示删除按钮，删除后同步清空 `imageUrl`，保存后生效。
- 强化搜索兜底：搜索接口为空或异常时，前端自动拉取全量景点并本地过滤；支持“1教/一教”等数字写法匹配。
- 修复管理员编辑图片预览无法关闭：上传图片预览层增加关闭按钮，避免点击图片后卡在预览层。
- 全面修复搜索数据库链路：后端新增 `ScenicSpotMapper.searchByKeyword`，通过 SQL `LIKE` 直接查询 `scenic_spots` 表；`ScenicSpotService` 和 `ScenicSpotController` 改为调用数据库搜索。
- 修复 Mapper XML 未进入运行包：`nav-server/pom.xml` 增加 `src/main/java/**/*.xml` 资源复制规则，确保 `ScenicSpotMapper.xml` 的搜索 SQL 会被打进 `target/classes`。
- 恢复游客搜索权限限制：非登录状态点击搜索会提示登录，不再打开搜索面板。
- 修复自动播放历史数据：将已有 `user_audio_settings.auto_play_enabled=0` 的旧数据纠正为 `1`，避免历史默认值覆盖当前“默认自动播放”策略。

## 验证结果

- `npm run build` 通过。
- `mvn compile` 通过。
- `http://127.0.0.1:5173/src/App.vue` 返回 200，确认开发环境已能正确转换 `.vue` 文件。
- 追加搜索联动与图片删除修复后，`npm run build` 再次通过。
- 追加搜索兜底与图片预览关闭修复后，`npm run build` 再次通过。
- 追加数据库搜索链路与 Mapper 资源复制修复后，`npm run build`、`mvn compile` 均通过，并确认 `target/classes/com/nav/mapper/ScenicSpotMapper.xml` 已包含 `searchByKeyword`。

## 2026-05-26 搜索、识别与用户状态修复

- 搜索功能做双保险：登录后先调用数据库搜索接口；如果接口异常或返回空，再调用全量景点列表并在前端本地匹配，保证已入库景点可搜索。
- 后端搜索服务重写为稳定编码实现，支持 `31教`、`三十一教`、`31教学楼`、景点名片段和简介关键词匹配。
- 全量校验 31 个景点：经纬度不为空、空间字段不为空、坐标合法，每个景点用自身坐标测距均为 0 米。
- 景点识别半径增加 GPS 容错：后端当前位置匹配使用 `GREATEST(radius, 150)`，减少人在楼内但 GPS 漂移导致无法识别的问题。
- 无音频景点自动播报：登录用户进入景点范围后，如果没有音频文件，会自动使用浏览器语音朗读景点简介。
- 用户管理恢复账号状态管理，但不允许修改角色。冻结表示账号临时禁用，用户不能登录，已有 token 也会被后端拦截；注销表示账号不可用但保留记录。

验证：
- `npm run build` 通过。
- `mvn compile` 通过。
- `target/classes/com/nav/mapper/ScenicSpotMapper.xml` 已包含 `searchByKeyword` 和 `GREATEST(radius, 150)`。

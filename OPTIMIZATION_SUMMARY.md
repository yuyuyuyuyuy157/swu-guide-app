# 项目全面优化变更总结

> 日期：2026-05-25  
> 范围：前端 `swu-guide-app` 与后端 `nav-take-out-master`

## 一、后端优化

### 0. 管理端上传与用户管理

- 新增景点图片上传接口：
  - `POST /api/v1/admin/scenic/upload-image`
  - 管理员上传图片后，后端保存到 `D:/nav-uploads/scenic/yyyy/mm/dd/`。
  - 返回 `/download/scenic/...` 可访问地址，复用已有静态资源映射。
- 新增管理员用户列表接口：
  - `GET /api/v1/admin/users`
  - 支持手机号关键词搜索、分页查看。
  - 返回手机号脱敏、头像、角色、状态、注册时间。
- 管理员权限校验从 `/api/v1/admin/scenic/**` 扩展为 `/api/v1/admin/**`，避免用户管理等新增后台接口被普通用户访问。

### 1. 权限与游客模式

- 放行游客可用的基础景点接口：
  - `GET /api/v1/scenic/list`
  - `POST /api/v1/scenic/current`
- 管理端接口增加角色校验：
  - `/api/v1/admin/**` 只允许 JWT 中 `role=ADMIN` 的账号访问。
  - 普通用户访问会返回 `403`。

### 2. 景点定位与搜索数据

- 修复 `ScenicSpotMapper.getByCurrentLocation` 参数绑定问题，显式增加 `@Param("longitude")` 和 `@Param("latitude")`。
- `ScenicSpotVO` 增加 `latitude`、`longitude` 字段，前端搜索选中景点后可以直接移动地图中心。
- 景点列表和当前景点接口都会返回经纬度，便于前端地图定位。

### 3. 音频模块稳定性

- 修复 `AudioServiceImpl.getAudioDetail` 中无意义且危险的空经纬度查询。
- 播放历史查询增加 `user_id` 条件，避免不同用户之间读取到错误播放记录。

### 4. 管理端景点维护

- 修复管理端新增景点 SQL 中不存在的 `created_by` 字段引用。
- 修复 `AutoFillAspect` 对不存在的 `setCreatedBy` 方法反射调用。
- 管理端分页参数增加默认值与上限：
  - 默认 `page=1`
  - 默认 `pageSize=20`
  - 最大 `pageSize=50`
- 修复 `ScenicSpotDTO(Double)` 与 `ScenicSpot Entity(BigDecimal)` 之间不能自动复制的问题，改为显式映射，避免经纬度丢失。

## 二、前端优化

### 0. UI 视觉体系优化

- 重做全局设计 Token：
  - 主色由模板蓝调整为更贴合校园导览的青绿体系。
  - 增加 `--color-surface-soft`、`--shadow-card` 等可复用视觉变量。
  - 统一 Vant 主按钮、导航栏、表单分组的视觉风格。
  - 同步 Vant 全局颜色变量，避免按钮、标签继续回落到默认蓝色。
- 修复 `src/main.ts` 未引入全局 `style.css` 的问题，让设计 Token、全局动画、Vant 覆盖样式真正生效。
- 降低大面积模板化圆角和单一蓝色观感，让页面更稳、更像真实导览产品。
- 统一标题字距，避免部分中文标题因过大字距显得松散。

### 1. 管理员地图选点新增景点

- 管理端景点列表页新增右上角 `+` 按钮。
- 点击后打开“新增景点”抽屉。
- 抽屉内新增地图选点区域：
  - 管理员点击地图后，系统自动回填经纬度。
  - 管理员只需填写景点名称、上传图片、简介和感应半径。
- 编辑已有景点时也复用地图选点能力，可通过点击地图微调经纬度。
- 景点图片由“填写 URL”改为“直接上传图片”。
- 移除管理员手填音频 URL，降低维护成本。
- 新增前端接口：
  - `createScenic() -> POST /api/v1/admin/scenic`
  - `uploadScenicImage() -> POST /api/v1/admin/scenic/upload-image`

### 1.1 独立用户管理

- 用户管理从景点管理页中拆出，独立为 `/admin/users` 页面。
- 管理员个人中心增加“用户管理”入口，与“景点管理”并列。
- 景点管理页恢复为只维护景点数据，避免后台信息架构混杂。
- 用户管理页支持按手机号搜索。
- 用户列表展示脱敏手机号、角色、账号状态、注册时间和头像。
- 当前先以查看为主，为后续冻结/解冻、重置密码等操作预留位置。

### 2. 管理端字段适配

- 管理端详情读取同时兼容：
  - 后端实际字段：`description`、`radius`、`imageUrl`、`audioUrl`
  - 前端旧字段：`intro`、`inductionRange`、`image`、`audioId`
- 修复编辑抽屉中简介、半径等字段可能为空或回落默认值的问题。

### 3. 地图与音频联动

- 搜索景点后可根据接口返回的经纬度移动地图中心。
- 离开所有景点感应范围后，景点卡片会清空，不再残留旧景点。
- 自动播放开启后，进入有音频的景点会自动播放。
- 播放切换策略为“随位置实时切换”时，到达新景点会立即切换音频。
- `AI 讲演` 改为使用浏览器语音合成朗读景点名称和简介。
- 没有音频文件的景点也可以使用 `AI 讲演`，不再依赖管理员导入音频 URL。

### 4. 错误提示优化

- 前端请求拦截器将 `403` 提示从“请登录后使用该功能”调整为“权限不足，无法执行该操作”，更适合管理员权限场景。

### 5. 主要场景 UI 优化

- 地图页：
  - 将头像和搜索框整合为顶部工具条，减少浮层互相挤压。
  - 设置按钮改为更突出的圆形青绿渐变按钮。
  - 定位权限 Banner 颜色和层级更克制。
- 景点卡片：
  - 改为更稳定的图片 + 信息双列布局。
  - 优化图片比例、标题、距离标签、简介和操作按钮间距。
  - 音频播放面板增加轻量底色，进度区域更容易识别。
- 自动播放设置：
  - 顶部改为信息头图式面板，突出这是高阶设置区域。
  - 分区标题加粗，设置层级更清晰。
- 登录/注册/启动页：
  - 登录页增加辅助说明，减少空白感。
  - 登录/注册页在桌面预览下限制移动端宽度并居中，避免表单横向拉满。
  - 注册页从居中标题改为信息头，和产品整体风格统一。
  - 启动页去掉泛化扩散装饰，改为更稳的品牌图标块。
- 详情页：
  - 兼容 `description/radius` 与旧字段。
  - 优化标题、标签和正文排版，减少拥挤。
- 管理端列表：
  - 增加工作台标题区和“新增”按钮。
  - 管理端宽屏预览限制最大宽度，移动端保持全屏工作台。
  - 搜索框改为更清楚的输入控件样式。
  - 列表项增加边界和更紧凑的信息层级。

## 三、涉及文件

### 后端

- `nav-server/src/main/java/com/nav/config/WebMvcConfiguration.java`
- `nav-server/src/main/java/com/nav/controller/AdminController.java`
- `nav-server/src/main/java/com/nav/controller/admin/AdminScenicSpotController.java`
- `nav-server/src/main/java/com/nav/interceptor/JwtTokenInterceptor.java`
- `nav-server/src/main/java/com/nav/mapper/ScenicSpotMapper.java`
- `nav-server/src/main/java/com/nav/service/impl/ScenicSpotServiceImpl.java`
- `nav-server/src/main/java/com/nav/service/impl/AudioServiceImpl.java`
- `nav-server/src/main/java/com/nav/service/impl/AdminScenicSpotServiceImpl.java`
- `nav-server/src/main/java/com/nav/aspect/AutoFillAspect.java`
- `nav-server/src/main/resources/mapper/AdminScenicSpotMapper.xml`
- `nav-server/src/main/java/com/nav/vo/AdminUserVO.java`
- `nav-pojo/src/main/java/com/nav/vo/ScenicSpotVO.java`

### 前端

- `src/api/admin.ts`
- `src/types/api.d.ts`
- `src/views/AdminScenicList/index.vue`
- `src/views/AdminUserList/index.vue`
- `src/components/ScenicCard.vue`
- `src/components/AudioSettings.vue`
- `src/views/Login/index.vue`
- `src/views/Register/index.vue`
- `src/views/Splash/index.vue`
- `src/views/Detail/index.vue`
- `src/views/Map/index.vue`
- `src/style.css`
- `src/main.ts`
- `src/utils/request.ts`

## 四、后续建议

- 将接口文档中的 snake_case 字段统一更新为当前实际使用的 camelCase。
- 将密码加密从 `MD5(password + phone)` 升级为 BCrypt。
- 将地图 Key、JWT Secret、数据库密码迁移到环境变量。
- 用户管理后续可继续补充冻结/解冻、重置密码、注销账号等管理操作。

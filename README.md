# 西南大学校园智慧导览 App

这是一个面向校园景点导览的前后端一体项目。项目包含 Vue 3 前端、Spring Boot 后端、MySQL 数据库和 Capacitor Android App 工程，支持游客、普通用户和管理员三类使用场景。

当前重点能力包括：地图定位、景点搜索、进入景点范围自动弹出卡片、自动播放讲解、景点详情、管理员景点管理、图片上传、地图点选经纬度、用户管理、服务端自动生成讲解音频、Android 真机调试。

## 项目结构

```text
swu-guide-app/
├─ src/                         # Vue 前端源码
│  ├─ api/                      # 接口封装
│  ├─ components/               # 景点卡片、搜索、设置、弹窗等组件
│  ├─ router/                   # 前端路由
│  ├─ stores/                   # Pinia 状态：登录、定位、音频
│  ├─ utils/                    # 请求、资源路径、原生 TTS 辅助
│  └─ views/                    # 地图、登录、详情、管理页
├─ android/                     # Capacitor 生成的 Android 工程
├─ nav-take-out-master/         # Spring Boot 后端 Maven 多模块工程
│  ├─ nav-common/               # 公共工具和常量
│  ├─ nav-pojo/                 # DTO / Entity / VO
│  ├─ nav-server/               # 后端主服务
│  ├─ setup.sql                 # 建表脚本
│  └─ test-data.sql             # 测试账号和基础数据
├─ import_teaching_scenic.sql   # 教学楼景点导入脚本
├─ nav.sql                      # 完整数据库参考脚本
├─ ANDROID_APP_GUIDE.md         # Android 打包和调试说明
├─ FIX_SUMMARY_2026-05-28_APP_AUDIO.md
└─ README.md
```

## 技术栈

前端：

- Vue 3
- TypeScript
- Vite
- Vue Router
- Pinia
- Vant 4
- Axios
- 高德地图 JS API
- Capacitor Android

后端：

- Java 21
- Spring Boot 3.2.5
- Maven
- MySQL 8
- MyBatis-Plus
- Druid
- JWT
- OpenAPI / Knife4j
- Windows SAPI 服务端语音生成

## 环境要求

建议版本：

```text
Node.js 18+
npm 9+
JDK 21
Maven 3.9+
MySQL 8.0+
Git for Windows
Android Studio
Android SDK Platform-Tools
```

注意：当前“服务端自动生成语音”依赖 Windows 的 `System.Speech` / SAPI 语音能力。如果后端部署到 Linux，需要改成云 TTS 服务，或者管理员提前上传音频文件。

## 快速启动

### 1. 安装前端依赖

```powershell
cd D:\SwuProject\swu-guide-app
npm install
```

### 2. 初始化数据库

先创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS smart_guide_db
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

导入建表脚本和测试数据：

```powershell
cd D:\SwuProject\swu-guide-app
cmd /c "mysql -u root -p smart_guide_db < nav-take-out-master\setup.sql"
cmd /c "mysql -u root -p smart_guide_db < nav-take-out-master\test-data.sql"
```

如果需要导入整理好的教学楼景点数据：

```powershell
cmd /c "mysql -u root -p smart_guide_db < import_teaching_scenic.sql"
```

### 3. 修改后端数据库配置

打开：

```text
nav-take-out-master/nav-server/src/main/resources/application-dev.yml
```

根据自己的 MySQL 修改：

```yaml
nav:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    host: localhost
    port: 3306
    database: smart_guide_db
    username: root
    password: 你的数据库密码
```

### 4. 准备上传和语音缓存目录

后端默认把图片、生成音频等资源映射到：

```text
D:\nav-uploads
```

请至少确认这些目录存在，且运行后端的 Windows 用户有写入权限：

```powershell
New-Item -ItemType Directory -Force -Path D:\nav-uploads\scenic
New-Item -ItemType Directory -Force -Path D:\nav-uploads\tts
```

静态资源访问路径为：

```text
http://localhost:8080/download/...
```

### 5. 启动后端

```powershell
cd D:\SwuProject\swu-guide-app\nav-take-out-master
mvn -pl nav-server spring-boot:run
```

后端默认地址：

```text
http://localhost:8080
```

常用检查接口：

```text
GET http://localhost:8080/api/v1/scenic/list
GET http://localhost:8080/api/v1/system/config
```

### 6. 启动前端网页

新开一个终端：

```powershell
cd D:\SwuProject\swu-guide-app
npm run dev
```

前端默认地址：

```text
http://localhost:5173
```

## 测试账号

测试数据来自 `nav-take-out-master/test-data.sql`：

```text
管理员账号：13900139000
管理员密码：12345678

普通用户账号：13800138000
普通用户密码：12345678
```

## Android App 调试

当前 Android App 通过 Capacitor 打包 Vue 前端。详细说明见：

```text
ANDROID_APP_GUIDE.md
```

### USB 调试模式

当前 `.env.production` 使用 USB 调试地址：

```text
VITE_API_BASE_URL=http://127.0.0.1:8080/api/v1
VITE_RESOURCE_BASE_URL=http://127.0.0.1:8080
```

手机连接 USB 后，需要执行：

```powershell
D:\AndroidSDK\platform-tools\adb.exe devices
D:\AndroidSDK\platform-tools\adb.exe reverse tcp:8080 tcp:8080
```

然后同步并安装 App：

```powershell
cd D:\SwuProject\swu-guide-app
npm run cap:sync:android
```

在 Android Studio 中打开 `android/`，点击 Run 安装到真机。

如果手机拔插、Android Studio 重新安装、电脑休眠或 ADB 重启，`adb reverse` 可能会丢失。App 报“网络异常”时，优先重新执行：

```powershell
D:\AndroidSDK\platform-tools\adb.exe reverse tcp:8080 tcp:8080
```

### 真正发布 App 时

不能继续使用 `127.0.0.1`。需要把后端部署到公网服务器，然后修改 `.env.production`：

```text
VITE_API_BASE_URL=http://服务器公网IP:8080/api/v1
VITE_RESOURCE_BASE_URL=http://服务器公网IP:8080
```

再重新执行：

```powershell
npm run cap:sync:android
```

## 核心功能说明

- 游客模式：可以查看地图和景点卡片，但搜索、播放讲解等核心导览能力会提示登录。
- 用户模式：支持景点搜索、点击搜索结果定位、进入景点范围自动弹卡片、自动播放讲解、查看详情、修改密码。
- 管理员模式：支持景点管理、用户管理、图片上传、地图点选经纬度、修改景点简介和感应半径。
- 用户管理：管理员可以冻结或注销普通用户，但不能操作管理员账号状态。
- 景点搜索：从数据库查询景点，点击结果后地图定位到该景点，并弹出卡片触发讲解。
- 自动播报：App 获取当前位置后，根据景点经纬度和半径判断是否进入范围，进入后自动播放讲解。
- 服务端语音生成：管理员不必上传音频。景点没有 `audio_url` 但有简介时，后端会自动生成 WAV 讲解音频并缓存到 `D:\nav-uploads\tts`。
- 图片上传：管理员上传图片后，后端保存到 `D:\nav-uploads\scenic`，前端通过 `/download/**` 加载。

## 服务端自动语音逻辑

音频接口：

```text
GET /api/v1/audio/detail?audioId=景点ID
```

后端处理规则：

1. 如果景点有管理员上传的 `audio_url`，优先返回上传音频。
2. 如果没有上传音频，但有景点名称和简介，后端使用 Windows SAPI 生成 WAV 文件。
3. 生成文件保存到：

```text
D:\nav-uploads\tts
```

4. 文件名包含景点 ID 和简介哈希。管理员修改简介后，下次请求会生成新的音频文件。
5. 手机端只播放普通音频文件，不再依赖手机是否支持系统 TTS。

## 常见问题

### 1. App 显示网络异常

优先检查：

- 后端是否启动。
- 手机是否连接 USB。
- `adb devices` 是否能看到设备。
- `adb reverse --list` 是否包含 `tcp:8080 tcp:8080`。
- `.env.production` 是否和当前调试方式一致。

常用恢复命令：

```powershell
D:\AndroidSDK\platform-tools\adb.exe reverse tcp:8080 tcp:8080
```

### 2. 后端提示 8080 端口占用

```powershell
netstat -ano | findstr :8080
Stop-Process -Id 进程PID -Force
```

然后重新启动后端。

### 3. 景点图片无法显示

检查：

- 数据库 `image_url` 是否是 `/download/...`。
- 后端是否运行。
- `D:\nav-uploads\scenic` 下是否有对应图片。
- `VITE_RESOURCE_BASE_URL` 是否指向正确后端地址。

### 4. 搜索不到景点

检查：

- 是否已经登录，游客模式不能使用搜索。
- 数据库 `scenic_spots` 是否有数据。
- 景点 `is_deleted` 是否为 `0`。
- 后端接口是否能返回数据：

```text
GET /api/v1/scenic/search?keyword=31
```

### 5. 进入景点范围没有自动播报

检查：

- 手机是否允许定位权限。
- 景点经纬度和半径是否正确。
- 设置页“开启自动触发语音”是否开启。
- 用户是否已经登录。
- 是否已经播放过该景点，默认策略是每个景点自动播放一次。

### 6. 生成语音失败

检查：

- `D:\nav-uploads\tts` 是否存在。
- 后端运行用户是否有写入权限。
- Windows 是否安装中文语音，例如 `Microsoft Huihui Desktop`。
- 后端是否运行在 Windows。如果部署到 Linux，需要替换为云 TTS 服务或手动上传音频。

## 测试前自检命令

后端编译：

```powershell
cd D:\SwuProject\swu-guide-app\nav-take-out-master
mvn compile -pl nav-server
```

前端类型检查和构建：

```powershell
cd D:\SwuProject\swu-guide-app
npx vue-tsc --noEmit -p tsconfig.app.json --incremental false
npx vite build
```

检查后端接口：

```powershell
Invoke-WebRequest -UseBasicParsing http://127.0.0.1:8080/api/v1/scenic/list
```

检查 Android USB 转发：

```powershell
D:\AndroidSDK\platform-tools\adb.exe devices
D:\AndroidSDK\platform-tools\adb.exe reverse --list
```

## 更新日志

### 2026-06-01

- 完成测试前自检流程整理。
- README 重写为最新可复现版本，补充 Android 调试、服务端语音生成、常见问题和 GitHub 推送说明。

### 2026-05-28

- 新增 Capacitor Android App 工程。
- 新增 Android 真机调试配置。
- 新增服务端自动生成讲解音频能力。
- 景点没有上传音频时，后端可根据景点名称和简介生成 WAV 音频。
- 生成音频缓存到 `D:\nav-uploads\tts`。
- 前端统一播放后端音频文件，减少不同手机 TTS 支持差异。
- 修复景点卡片刷新闪烁问题。
- 修复自动播放默认值读取错误。
- 优化景点卡片讲解按钮，同一景点只保留一个讲解入口。

### 2026-05-26

- 修复搜索景点无法返回结果的问题。
- 搜索结果点击后可定位到景点、弹出景点卡片并触发讲解。
- 修复进入景点范围后卡片消失、进度条异常、游客点击讲解闪退等问题。
- 优化自动播报逻辑和卡片保留策略。
- 修复后端 8080 端口占用排查流程。

### 2026-05-25

- 新增管理员景点管理。
- 新增管理员用户管理。
- 新增景点图片上传。
- 新增地图点选经纬度录入。
- 新增用户冻结和注销能力。
- 整理教学楼景点导入数据。
- 优化 UI、景点列表滚动、图片预览关闭、搜索交互等体验问题。

## 推送到 GitHub

当前仓库远程地址：

```text
ssh://git@ssh.github.com:443/yuyuyuyuyuy157/swu-guide-app.git
```

推荐推送步骤：

```bash
cd /d/SwuProject/swu-guide-app
git status
git add .
git commit -m "Update Android app guide and audio narration workflow"
git push origin main
```

如果遇到 Git 安全目录提示：

```bash
git config --global --add safe.directory D:/SwuProject/swu-guide-app
```

如果遇到 SSH 权限问题，请先确认 GitHub SSH key 已配置，并测试：

```bash
ssh -T -p 443 git@ssh.github.com
```

## 给队友的提醒

- 每个人都要修改自己的 `application-dev.yml` 数据库账号密码。
- 如果没有公网服务器，手机 App 真机测试需要 USB + `adb reverse`。
- 如果要真正不插线使用 App，需要把后端和 MySQL 部署到公网服务器，并重新配置 `.env.production`。
- Windows 本机后端支持自动生成语音；部署到 Linux 时需要改成云 TTS 或管理员上传音频。

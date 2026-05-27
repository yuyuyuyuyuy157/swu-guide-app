# 西南大学校园智能导览系统

这是一个面向校园景点导览的前后端一体项目。前端使用 Vue 3 + Vite + Vant，后端使用 Spring Boot 3 + MySQL 8，主要功能包括游客/用户/管理员模式、地图定位、景点搜索、进入景点范围自动讲解、景点详情、管理员景点管理、用户管理、图片上传和经纬度录入。

## 项目结构

```text
swu-guide-app/
├─ src/                         # 前端源码
│  ├─ api/                      # 前端接口封装
│  ├─ components/               # 通用组件：搜索、景点卡片、设置等
│  ├─ router/                   # 前端路由
│  ├─ stores/                   # Pinia 状态：登录、定位、音频
│  ├─ utils/                    # Axios 请求封装
│  └─ views/                    # 页面：地图、登录、详情、管理端等
├─ public/                      # 前端静态资源
├─ nav-take-out-master/         # 后端 Maven 多模块项目
│  ├─ nav-common/               # 公共常量、异常、工具类
│  ├─ nav-pojo/                 # DTO、Entity、VO
│  ├─ nav-server/               # Spring Boot 主服务
│  ├─ setup.sql                 # 建表脚本
│  └─ test-data.sql             # 测试账号和基础数据
├─ 教学楼文件夹_完整版/          # 原始景点图片和简介资料
├─ nav.sql                      # 早期完整数据库脚本，保留作参考
├─ import_teaching_scenic.sql   # 教学楼景点导入脚本
├─ 业务逻辑.md                  # 业务逻辑说明
├─ 整体构思.md                  # 项目整体构思
├─ 接口文档5.18.1.docx          # 接口文档
├─ FIX_SUMMARY_2026-05-26_CORE.md
└─ AI_HANDOFF_PROMPT.md         # 给其它大模型接手项目的提示词
```

前端和后端已经放在同一个仓库根目录下，不建议再随意移动目录，否则 Vite、Maven 和文档中的路径都需要同步调整。

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

后端：

- Java 21
- Spring Boot 3.2.5
- Maven 多模块
- MyBatis-Plus
- MySQL 8
- Druid
- JWT
- Knife4j / OpenAPI

## 环境要求

建议使用以下版本：

```text
Node.js 18+
npm 9+
JDK 21
Maven 3.9+
MySQL 8.0+
Chrome / Edge
Git for Windows
```

如果 Maven、Node 或 MySQL 没有加入系统环境变量，需要先配置 PATH。

## 快速启动

### 1. 安装前端依赖

```powershell
cd D:\SwuProject\swu-guide-app
npm install
```

### 2. 初始化数据库

先登录 MySQL，创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS smart_guide_db
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

然后在项目根目录执行。Windows PowerShell 推荐使用 `cmd /c` 导入 SQL：

```powershell
cmd /c "mysql -u root -p smart_guide_db < nav-take-out-master\setup.sql"
cmd /c "mysql -u root -p smart_guide_db < nav-take-out-master\test-data.sql"
```

如果要导入整理后的教学楼景点数据，再执行：

```powershell
cmd /c "mysql -u root -p smart_guide_db < import_teaching_scenic.sql"
```

注意：`import_teaching_scenic.sql` 依赖图片文件访问路径 `/download/scenic/teaching-import/...`，需要保证图片已经放到后端配置的上传目录中。

### 3. 修改后端数据库配置

打开：

```text
nav-take-out-master/nav-server/src/main/resources/application-dev.yml
```

根据自己的 MySQL 修改：

```yaml
nav:
  datasource:
    host: localhost
    port: 3306
    database: smart_guide_db
    username: root
    password: 你的数据库密码
```

### 4. 启动后端

```powershell
cd D:\SwuProject\swu-guide-app\nav-take-out-master
mvn compile
mvn -pl nav-server spring-boot:run
```

后端默认地址：

```text
http://localhost:8080
```

常用验证接口：

```text
GET http://localhost:8080/api/v1/scenic/list
GET http://localhost:8080/api/v1/system/config
```

### 5. 启动前端

新开一个终端：

```powershell
cd D:\SwuProject\swu-guide-app
npm run dev
```

前端默认地址：

```text
http://localhost:5173
```

Vite 已经配置代理，前端开发环境下 `/api` 和 `/download` 会转发到 `http://localhost:8080`。

## 测试账号

测试数据来自 `nav-take-out-master/test-data.sql`：

```text
管理员账号：13900139000
管理员密码：12345678

普通用户账号：13800138000
普通用户密码：12345678
```

管理员入口：

```text
/login
```

登录后会根据角色进入对应页面。

## 核心功能

- 游客模式：可以查看地图和景点基础信息，但搜索和讲解需要登录。
- 用户模式：支持景点搜索、景点卡片、详情查看、进入景点范围后自动讲解。
- 管理员模式：支持景点列表、景点新增、景点编辑、图片上传、地图点选录入经纬度、用户管理。
- 景点搜索：通过后端 `/api/v1/scenic/search` 查询数据库中的景点。
- 自动播报：前端获取浏览器定位后，请求 `/api/v1/scenic/current` 判断当前是否进入景点范围。
- 图片访问：上传图片通过 `/download/**` 访问，后端默认映射到 `D:/nav-uploads/`。

## 局域网手机测试

如果没有服务器，也可以用电脑临时当后端服务器。电脑和手机需要连同一个 Wi-Fi 或热点。

1. 查询电脑 IPv4：

```powershell
ipconfig
```

例如电脑 IP 是：

```text
192.168.1.23
```

2. 手机访问后端时不能用 `127.0.0.1`，要用：

```text
http://192.168.1.23:8080
```

3. 如果手机无法访问，检查 Windows 防火墙是否允许 Java 或 8080 端口访问。

## 常见问题

### 1. 后端提示 Port 8080 was already in use

说明已经有一个后端进程占用了 8080。先查端口：

```powershell
netstat -ano | findstr :8080
```

找到 PID 后停止：

```powershell
Stop-Process -Id 进程PID -Force
```

再重新运行后端。

### 2. 前端登录提示网络繁忙

优先检查：

- 后端是否已经启动。
- MySQL 是否运行。
- `application-dev.yml` 的数据库账号密码是否正确。
- 前端是否通过 `http://localhost:5173` 访问。
- Vite 代理是否仍然指向 `http://localhost:8080`。

### 3. 图片无法显示

检查：

- 数据库中的 `image_url` 是否是 `/download/...` 或可访问 URL。
- 后端是否运行。
- 上传目录是否存在，默认是 `D:/nav-uploads/`。
- `WebMvcConfiguration` 中 `/download/**` 是否正确映射到上传目录。

### 4. 搜索不到景点

检查：

- 是否已经登录，游客模式不允许搜索。
- 数据库 `scenic_spots` 表是否有数据。
- 景点 `is_deleted` 是否为 `0`。
- 后端接口 `/api/v1/scenic/search?keyword=31教` 是否能返回数据。

### 5. 进入景点范围没有自动播报

检查：

- 浏览器是否允许定位权限。
- 当前经纬度是否真的落在景点半径内。
- `scenic_spots` 表的 `latitude`、`longitude`、`location` 是否同步。
- 用户是否已经登录。
- 手机浏览器或 WebView 可能限制自动播放，需要先有一次用户点击交互。

## 上传到 Gitee 前建议

不要上传这些目录或文件：

```text
node_modules/
dist/
target/
*.log
.idea/
.vscode/
```

上传前建议重点检查：

- `application-dev.yml` 中是否有个人数据库密码。
- 地图 Key 是否需要替换成队伍统一 Key。
- 是否要保留 `教学楼文件夹_完整版/` 原始素材。
- 是否要保留历史修复文档和导入报告。

如果是上传到队友仓库：

```powershell
cd D:\SwuProject\swu-guide-app
git init
git add .
git commit -m "整合前后端校园导览系统"
git remote add origin 队友Gitee仓库地址
git branch -M main
git push -u origin main
```

如果队友仓库已有代码，建议推送到新分支：

```powershell
git checkout -b dev-your-name
git push -u origin dev-your-name
```

## 给其它大模型的接手提示

让其它大模型接手时，建议先把下面这些文件发给它或让它优先阅读：

```text
README.md
AI_HANDOFF_PROMPT.md
业务逻辑.md
整体构思.md
接口文档5.18.1.docx
FIX_SUMMARY_2026-05-26_CORE.md
src/utils/request.ts
src/views/Map/index.vue
src/components/ScenicCard.vue
src/components/SearchPanel.vue
nav-take-out-master/nav-server/src/main/java/com/nav/controller/
nav-take-out-master/nav-server/src/main/java/com/nav/service/impl/
```

如果它需要先复现项目，请让它严格按 README 的“快速启动”部分执行。

## 后续做成 App 的建议路线

当前项目已加入 Capacitor 基础配置，详细步骤见：

```text
ANDROID_APP_GUIDE.md
```

核心流程：

```powershell
npm install @capacitor/core @capacitor/cli @capacitor/android
npm run build
npm run cap:add:android
npm run cap:open:android
```

打包 App 前要先解决两个问题：

- 复制 `.env.app.example` 为 `.env.production`，把后端接口地址改成电脑局域网 IP 或服务器地址。
- 定位、语音播放、图片上传在 App WebView 中需要额外检查权限。

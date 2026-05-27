# 给其它大模型的项目接手提示词

你现在接手的是一个“西南大学校园智能导览系统”项目。请先不要急着改代码，先按下面顺序理解项目。

## 你的任务

请帮助我复现、排查或继续开发这个项目。项目是前后端一体仓库：

- 前端：Vue 3 + Vite + TypeScript + Vant + Pinia + 高德地图
- 后端：Spring Boot 3.2.5 + Java 21 + Maven 多模块 + MySQL 8
- 项目目标：校园地图导览、景点搜索、进入景点范围自动讲解、管理员景点管理、用户管理、图片上传、地图点选经纬度。

## 必须先阅读的文件

请优先阅读：

```text
README.md
业务逻辑.md
整体构思.md
FIX_SUMMARY_2026-05-26_CORE.md
src/utils/request.ts
src/router/index.ts
src/views/Map/index.vue
src/components/SearchPanel.vue
src/components/ScenicCard.vue
src/stores/audio.ts
src/stores/location.ts
nav-take-out-master/nav-server/src/main/resources/application.yml
nav-take-out-master/nav-server/src/main/resources/application-dev.yml
nav-take-out-master/nav-server/src/main/java/com/nav/controller/
nav-take-out-master/nav-server/src/main/java/com/nav/service/impl/
```

如果需要查接口，请看：

```text
接口文档5.18.1.docx
nav-take-out-master/nav-server/src/main/java/com/nav/controller/
```

## 项目目录关系

```text
swu-guide-app/                 # 仓库根目录，也是前端根目录
├─ src/                        # 前端源码
├─ package.json                # 前端依赖和启动脚本
├─ vite.config.js              # 前端代理配置
├─ nav-take-out-master/        # 后端根目录
│  ├─ pom.xml                  # 后端父 POM
│  ├─ nav-common/
│  ├─ nav-pojo/
│  └─ nav-server/              # Spring Boot 启动模块
└─ nav-take-out-master/setup.sql
```

## 复现步骤

1. 安装前端依赖：

```powershell
cd D:\SwuProject\swu-guide-app
npm install
```

2. 初始化 MySQL 数据库：

```sql
CREATE DATABASE IF NOT EXISTS smart_guide_db
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

```powershell
cmd /c "mysql -u root -p smart_guide_db < nav-take-out-master\setup.sql"
cmd /c "mysql -u root -p smart_guide_db < nav-take-out-master\test-data.sql"
```

3. 修改数据库配置：

```text
nav-take-out-master/nav-server/src/main/resources/application-dev.yml
```

4. 启动后端：

```powershell
cd D:\SwuProject\swu-guide-app\nav-take-out-master
mvn compile
mvn -pl nav-server spring-boot:run
```

5. 启动前端：

```powershell
cd D:\SwuProject\swu-guide-app
npm run dev
```

6. 打开：

```text
http://localhost:5173
```

## 测试账号

```text
管理员：13900139000 / 12345678
普通用户：13800138000 / 12345678
```

## 排查重点

如果搜索无结果，请优先检查：

- 前端是否登录。
- `/api/v1/scenic/search` 是否被调用。
- 数据库 `scenic_spots` 是否有数据。
- 后端 `ScenicSpotServiceImpl.searchScenicSpots`。

如果进入景点范围不播报，请优先检查：

- 浏览器定位权限。
- 前端 `locationStore` 是否拿到真实定位。
- 后端 `/api/v1/scenic/current` 是否返回景点。
- `scenic_spots.location` 是否和 `latitude`、`longitude` 同步。
- 浏览器是否限制自动播放。

如果图片无法显示，请优先检查：

- `image_url` 字段。
- `/download/**` 静态资源映射。
- `D:/nav-uploads/` 是否存在对应文件。

如果后端启动失败并提示 8080 占用，请先执行：

```powershell
netstat -ano | findstr :8080
Stop-Process -Id 进程PID -Force
```

## 开发要求

修改代码时请注意：

- 不要随意移动前后端目录。
- 不要提交 `node_modules`、`dist`、`target`、日志文件。
- 后端优先保持现有 Controller / Service / Mapper 分层。
- 前端优先保持现有 Vue + Pinia + Vant 风格。
- 涉及地图定位、搜索、自动播报的改动，要同时验证前端调用和后端接口。

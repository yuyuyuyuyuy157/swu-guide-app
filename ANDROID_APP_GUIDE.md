# Android App 打包与测试说明

本项目使用 Capacitor 把 Vue 前端打包成 Android App。后端仍然运行在电脑或服务器上，App 通过局域网 IP 访问后端。

## 1. Android Studio 需要配置什么

打开 Android Studio 后确认这些组件已安装：

- Android SDK Platform，建议 Android 13 或 Android 14。
- Android SDK Platform-Tools。
- Android SDK Build-Tools。
- Android Emulator，使用模拟器时需要。
- 一台真机或一个模拟器。

如果用真机测试：

- 手机和电脑连接同一个 Wi-Fi，或手机开热点给电脑。
- 手机开启开发者选项和 USB 调试。
- Windows 防火墙允许 Java 或 8080 端口访问。

## 2. 先让电脑后端可被手机访问

在电脑上查 IPv4：

```powershell
ipconfig
```

假设电脑 IPv4 是：

```text
192.168.1.23
```

启动后端：

```powershell
cd D:\SwuProject\swu-guide-app\nav-take-out-master
mvn -pl nav-server spring-boot:run
```

手机浏览器测试：

```text
http://192.168.1.23:8080/api/v1/scenic/list
```

如果手机打不开，优先检查电脑防火墙、手机和电脑是否在同一网络。

## 3. 配置 App 的后端地址

复制配置模板：

```powershell
cd D:\SwuProject\swu-guide-app
copy .env.app.example .env.production
```

打开 `.env.production`，把 IP 改成你的电脑 IPv4：

```text
VITE_API_BASE_URL=http://192.168.1.23:8080/api/v1
VITE_RESOURCE_BASE_URL=http://192.168.1.23:8080
```

说明：

- `VITE_API_BASE_URL` 用于登录、搜索、景点列表等接口。
- `VITE_RESOURCE_BASE_URL` 用于图片和音频，例如 `/download/...`。
- 不要写 `127.0.0.1`，手机里的 `127.0.0.1` 指手机自己。

## 4. 安装 Capacitor

```powershell
cd D:\SwuProject\swu-guide-app
npm install @capacitor/core @capacitor/cli @capacitor/android --save-dev
```

## 5. 生成 Android 工程

第一次执行：

```powershell
npm run build
npm run cap:add:android
```

之后每次前端代码有改动，执行：

```powershell
npm run cap:sync:android
```

打开 Android Studio：

```powershell
npm run cap:open:android
```

## 6. Android 权限

生成 `android/` 目录后，打开：

```text
android/app/src/main/AndroidManifest.xml
```

确认 `manifest` 节点下有这些权限：

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

如果使用 `http://192.168.x.x:8080`，还要确认 `application` 节点允许明文 HTTP：

```xml
<application
    android:usesCleartextTraffic="true"
    ...>
```

## 7. Android Studio 里怎么运行

1. 打开 Android Studio。
2. 选择 `D:\SwuProject\swu-guide-app\android`。
3. 等 Gradle Sync 完成。
4. 选择真机或模拟器。
5. 点击 Run。

首次启动 App 后，允许定位权限。

## 8. 重点测试顺序

建议按这个顺序测：

1. App 能打开地图。
2. 用户账号能登录。
3. 景点列表或搜索有结果。
4. 景点图片能显示。
5. 点击搜索结果能定位并弹出景点卡片。
6. 点击讲解不会闪退。
7. 真机走到景点范围内能自动弹出卡片和播报。
8. 管理员能新增/编辑景点并上传图片。

## 9. 常见问题

### 登录提示网络异常

检查 `.env.production` 是否配置成电脑 IPv4，后端是否启动，手机是否能打开后端接口。

### 图片加载失败

检查 `VITE_RESOURCE_BASE_URL` 是否是 `http://电脑IP:8080`，并确认后端 `/download/**` 可访问。

### 定位没有反应

检查 App 定位权限、手机系统定位开关、AndroidManifest 权限。

### 自动播报没有声音

手机 WebView 可能限制自动播放。先手动点击一次讲解按钮，让系统建立音频播放权限，再测试自动播报。


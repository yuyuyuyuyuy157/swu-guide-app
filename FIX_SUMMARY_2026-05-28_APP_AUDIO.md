# 2026-05-28 App 端语音与自动播放修复记录

## 本次处理的问题

1. 手机 App 中点击 AI 讲解提示“不支持 AI 语音”，导致无音频景点无法播报。
2. 部分景点卡片同时出现“听讲解”和“AI 讲解”两个按钮，交互不清晰。
3. 设置页中的自动播放应默认开启。
4. 语音讲解需要做到不同手机都能自动播放，不能依赖手机是否自带 TTS 引擎。

## 主要变更

- 新增 Android 原生 TTS 插件：`android/app/src/main/java/com/swu/guide/NativeTtsPlugin.java`
  - 使用系统 `TextToSpeech` 朗读景点名称和简介。
  - 支持播放和停止。
  - 避免依赖 WebView 的 `speechSynthesis`，解决手机端不支持 AI 语音的问题。
  - 已增强首个景点播报逻辑：如果系统 TTS 还在初始化，会等待初始化完成后再播放，不再直接失败。

- 注册原生插件：`android/app/src/main/java/com/swu/guide/MainActivity.java`
  - App 启动时注册 `NativeTts` 插件，供前端调用。

- 新增前端调用封装：`src/utils/nativeTts.ts`
  - Android App 环境优先调用原生 TTS。
  - 浏览器预览环境继续使用浏览器语音能力作为兜底。

- 优化景点卡片：`src/components/ScenicCard.vue`
  - 已上传真实音频的景点只显示一个“听讲解”按钮。
  - 未上传音频但有简介的景点，也会显示“听讲解”按钮。
  - 自动播报逻辑优先播放真实音频，失败或无音频时改用 AI 朗读简介。
  - 前端不再把手机系统 TTS 作为主链路，而是优先请求后端生成的讲解音频。
  - 定位轮询刷新改为后台静默刷新，避免景点卡片每隔几秒闪烁成 loading 状态。
  - 服务端音频播放失败时不再自动退回手机 TTS，避免同一景点前后出现不同声音。

- 新增服务端通用语音生成链路：`nav-take-out-master/nav-server/src/main/java/com/nav/service/impl/AudioServiceImpl.java`
  - 景点没有 `audio_url` 时，后端用景点名称和简介生成 WAV 音频。
  - 生成文件缓存到 `D:/nav-uploads/tts`，返回 `/download/tts/...wav` 给 App 播放。
  - 手机端只播放音频文件，不再依赖手机是否安装文字转语音引擎。

- 更新景点列表可播放判定：`ScenicSpotServiceImpl.java`
  - 只要景点有上传音频或有简介文本，就标记为可播放。

- 修改自动播放默认值：
  - `nav.sql`
  - `nav-take-out-master/setup.sql`
  - 当前本地数据库 `user_audio_settings.auto_play_enabled` 默认值已同步调整为 `1`。
  - 修复后端读取 `TINYINT(1)` 时把 `Boolean true` 误判为 `false` 的问题。

## 已验证

- `npm run build` 通过。
- `npm run cap:sync:android` 通过，新的前端资源已同步到 Android 工程。
- `mvn compile -pl nav-server` 通过。
- 已确认当前 Windows 系统存在中文语音 `Microsoft Huihui Desktop`，可用于服务端生成讲解音频。
- 已验证 31 教音频详情接口返回 `/download/tts/scenic-21-c68eea5f658de2ff.wav`，静态下载返回 `200`。

## 测试提醒

1. Android Studio 里重新 Run 安装新版 App。
2. 后端必须重启一次，才能加载 `AudioServiceImpl` 的自动播放修复。
3. 确认后端运行用户可以写入 `D:/nav-uploads/tts`，首次播放无音频景点时会自动生成缓存文件。
4. 手机重新连接 USB 后，如果继续使用 `127.0.0.1:8080` 调后端，需要重新执行：

```bash
adb reverse tcp:8080 tcp:8080
```

5. 测试无音频景点时，点击“听讲解”应由后端生成音频后播放。
6. 测试有音频景点时，卡片应只出现一个“听讲解”按钮。

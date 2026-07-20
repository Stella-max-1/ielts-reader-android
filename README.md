# IELTS Reader for Android

一个离线优先的雅思英语阅读学习应用。首版不抓取或转载受版权保护的外刊全文，内置原创练习文章，也支持粘贴你有权阅读的文章。

## 功能

- 英文文章阅读与阅读进度保存
- 安卓系统 TTS 英音优先自动逐句朗读
- 0.8× / 1.0× / 1.2× / 1.5× 语速
- 雅思重点词黄色高亮
- 点击查看音标与中文释义、单词发音和收藏
- 粘贴自定义文章，数据仅保存在手机本地

## 构建

要求 JDK 17、Android SDK 35、Gradle 8.7：

```bash
gradle :app:assembleDebug
```

APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。

推送到 GitHub 后，Actions 会自动构建。可在仓库的 **Releases → Latest test build** 下载 APK，也可在对应 Actions 任务的 Artifacts 中下载。

## 安装提示

这是调试签名 APK。安卓手机首次安装时需要允许浏览器或文件管理器“安装未知应用”。朗读使用手机系统自带 TTS；如果设备没有英音语音包，请在系统文字转语音设置中下载。

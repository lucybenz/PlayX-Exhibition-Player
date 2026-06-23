# PlayX - 展陈行业专业播放器 (Exhibition Player)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)

**PlayX** 是一款专为线下展厅、展陈行业设计的沉浸式安卓播放器。它针对无人值守场景进行了深度优化，旨在提供极简、稳定且高格调的视觉体验。

## ✨ 核心特性

*   **🎬 沉浸式体验**：支持无封面模式，实现开机自动进入播放，全黑背景过渡，无缝衔接。
*   **🧠 自动化运行**：具备“无人值守”逻辑，异常退出或返回后支持 4s 自动重播，确保展项永不黑屏。
*   **🔐 隐蔽式配置**：
    *   **双击交互**：屏幕任意位置双击唤起加密配置面板。
    *   **热键入口**：左上角隐藏式热触控区域。
    *   **安全加固**：支持自定义数字管理密码，自动拉起原生键盘。
*   **🎨 极简 UI 设计**：
    *   **细线模式**：1.5dp 精致进度条与音量控制线。
    *   **呼吸动画**：动态引导提示，提升展位科技感。
*   **🛠 硬件适配**：针对 MuMu 等模拟器及多种安卓展牌硬件进行了绿屏修复（TextureView 渲染优化）。

## 🚀 快速开始

1.  **安装**：下载 APK 并安装至安卓设备。
2.  **配置**：双击屏幕任意位置，输入默认密码 `2026`。
3.  **选择资源**：在配置面板中选择您的视频源及封面图。
4.  **进阶**：开启“无封面模式”以获得极致的自动化播放体验。

## 🛠 技术栈

*   **UI 框架**：Jetpack Compose (Modern Android UI)
*   **播放引擎**：Media3 ExoPlayer
*   **数据存储**：Jetpack DataStore (Preferences)
*   **图片加载**：Coil

## 📄 开源协议

本项目采用 [Apache License 2.0](LICENSE) 协议。

---
*助力每一场展陈，让数字内容更有温度。*

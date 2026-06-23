# PlayX 播放器配置说明

本播放器支持两种运行模式：**常规模式（有封面）** 和 **无封面模式（自动播放）**。

## 1. 模式切换
在应用主页，点击左上角的 **齿轮图标** 进入设置页面。

### 无封面模式 (直接播放)
- **开关说明**：开启此模式后，应用启动时将跳过封面图展示，直接进入视频播放页面。
- **业务逻辑**：
  - 开启：启动 -> 检查视频文件 -> 自动开始播放。
  - 关闭：启动 -> 显示封面图 -> 点击封面 -> 开始播放。

### 循环模式 (Loop Mode)
在设置页面可以自由切换以下三种循环模式：
1. **不循环**：视频播放结束后停止。
2. **单曲循环**：单个视频不断重复播放。
3. **列表循环**：播放列表中的所有视频（目前支持单个主视频的循环）。

## 2. 外部配置文件方式 (手动配置)
如果您希望在打包安装后，不通过 UI 而是通过文件来配置播放器，可以按照以下步骤操作：

### 配置文件位置
将一个名为 `config.json` 的文件放入手机的以下目录：
`/Android/data/com.example.playx/files/config.json`

### 配置文件内容示例
创建一个文本文件，内容如下：
```json
{
  "no_cover_mode": true,
  "repeat_mode": 2,
  "video_path": "/sdcard/Movies/my_video.mp4",
  "image_path": "/sdcard/Pictures/my_cover.jpg"
}
```

### 字段说明：
*   `no_cover_mode`: (Boolean) `true` 开启无封面模式，`false` 关闭。
*   `repeat_mode`: (Int) `0`: 不循环, `1`: 单曲循环, `2`: 全部循环。
*   `video_path`: (String) 视频文件的绝对路径。
*   `image_path`: (String) 封面图片的绝对路径。

**注意**：使用 `video_path` 和 `image_path` 时，请确保应用拥有访问该路径文件的权限。在 Android 11+ 系统上，建议将媒体文件也放在 `/Android/data/com.example.playx/files/` 目录下以确保 100% 可访问。

## 3. 技术实现说明 (PreferencesManager)
本应用的内部配置信息存储在 `DataStore` 中...
当 `no_cover_mode` 为 `true` 且已配置有效的视频路径时，`MainViewModel` 会在初始化时自动将页面状态切换至 `VIDEO` 模式并触发播放器。

# SMTC JNI 使用指南

## 简介

`SmtcUtils` 是一个封装了 SMTC JNI 库调用的工具类，可以方便地获取 Windows 媒体播放信息。

## 基本使用

### 1. 获取媒体信息

```java
import utils.tech.blinkfix.SmtcUtils;

// 获取当前媒体信息
SmtcUtils.MediaInfo info = SmtcUtils.getMediaInfo();

if (info.hasMedia()) {
    // 获取标题
    String title = info.getTitle();
    
    // 获取播放进度（秒）
    long position = info.getPosition();
    
    // 获取总时长（秒）
    long duration = info.getDuration();
    
    // 获取 Base64 编码的封面图
    String base64 = info.getBase64();
    
    // 获取格式化的时间字符串
    String formattedPos = info.getFormattedPosition(); // "03:45"
    String formattedDur = info.getFormattedDuration(); // "04:20"
    
    // 获取播放进度百分比
    double percent = info.getProgressPercent(); // 0.0 - 100.0
}
```

### 2. 快速检查

```java
// 检查是否有媒体正在播放
if (SmtcUtils.isMediaPlaying()) {
    // 有媒体正在播放
}

// 获取当前播放的标题
String title = SmtcUtils.getCurrentTitle();
```

## 在模块中使用示例

### 示例：在 Scoreboard 模块中显示当前播放的音乐

```java
import utils.tech.blinkfix.SmtcUtils;

@EventTarget
public void onRender(EventRender2D e) {
    // 获取媒体信息
    SmtcUtils.MediaInfo mediaInfo = SmtcUtils.getMediaInfo();
    
    if (mediaInfo.hasMedia()) {
        // 渲染媒体信息到屏幕上
        String displayText = String.format(
            "♪ %s | %s / %s",
            mediaInfo.getTitle(),
            mediaInfo.getFormattedPosition(),
            mediaInfo.getFormattedDuration()
        );
        
        // 使用你的渲染工具类渲染文本
        Fonts.harmony.render(
            e.getStack(),
            displayText,
            10.0,  // x
            10.0,  // y
            Color.WHITE,
            true,
            1.0    // scale
        );
    }
}
```

### 示例：创建一个显示媒体信息的 HUD 模块

```java
package com.heypixel.heypixelmod.modules.impl.render;

import modules.tech.blinkfix.Category;
import modules.tech.blinkfix.Module;
import modules.tech.blinkfix.ModuleInfo;
import utils.tech.blinkfix.SmtcUtils;
import api.events.tech.blinkfix.EventTarget;
import impl.events.tech.blinkfix.EventRender2D;
import renderer.utils.tech.blinkfix.Fonts;
import java.awt.Color;

@ModuleInfo(
    name = "MediaDisplay",
    description = "显示当前播放的媒体信息",
    category = Category.RENDER
)
public class MediaDisplay extends Module {
    
    @EventTarget
    public void onRender(EventRender2D e) {
        if (!this.isEnabled()) return;
        
        SmtcUtils.MediaInfo info = SmtcUtils.getMediaInfo();
        
        if (info.hasMedia()) {
            // 渲染标题
            String title = "♪ " + info.getTitle();
            Fonts.harmony.render(
                e.getStack(),
                title,
                10.0, 10.0,
                Color.WHITE,
                true,
                1.0
            );
            
            // 渲染进度
            String progress = String.format(
                "%s / %s (%.1f%%)",
                info.getFormattedPosition(),
                info.getFormattedDuration(),
                info.getProgressPercent()
            );
            Fonts.harmony.render(
                e.getStack(),
                progress,
                10.0, 25.0,
                Color.GRAY,
                true,
                0.8
            );
        }
    }
}
```

## 注意事项

1. **Windows 专用**：SMTC JNI 只能在 Windows 系统上使用
2. **JAR 依赖**：确保 SMTC JNI 的 JAR 文件已正确添加到项目依赖中
3. **反射查找**：工具类使用反射自动查找 `SmtcLoader` 类，如果找不到会返回 `hasMedia = false`
4. **性能**：`getMediaInfo()` 方法会调用 JNI，建议不要过于频繁调用（例如每帧调用）

## 故障排除

如果返回 `"SMTC not found"`：
- 检查 JAR 是否已正确添加到依赖
- 检查 `SmtcLoader` 类是否在类路径中
- 如果 `SmtcLoader` 在不常见的包中，可以修改 `SmtcUtils.java` 中的 `possiblePackageNames` 数组，添加实际的包名


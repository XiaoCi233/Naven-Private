package tech.blinkfix.utils;

import java.lang.reflect.Method;

/**
 * SMTC JNI 工具类
 * 用于获取 Windows 媒体播放信息
 * 
 * 使用前请确保 SMTC JNI 的 JAR 已添加到项目依赖中
 * 本工具类使用反射方式调用，无需提前知道确切的包名
 */
public class SmtcUtils {
    private static Class<?> smtcLoaderClass;
    private static Method getSmtcInfoMethod;
    private static boolean initialized = false;
    
    /**
     * 初始化 SMTC Loader，使用反射查找类
     */
    private static void initialize() {
        if (initialized) return;
        
        try {
            // 尝试常见的包名（包括实际的 dsj.smtc）
            String[] possiblePackageNames = {
                "dsj.smtc", // 实际的包名
                "", // 默认包
                "smtc",
                "com.smtc",
                "net.smtc",
                "org.smtc",
                "io.github.smtc",
                "dev.smtc"
            };
            
            for (String pkg : possiblePackageNames) {
                String className = pkg.isEmpty() ? "SmtcLoader" : pkg + ".SmtcLoader";
                try {
                    smtcLoaderClass = Class.forName(className);
                    getSmtcInfoMethod = smtcLoaderClass.getMethod("getSmtcInfo");
                    initialized = true;
                    return;
                } catch (ClassNotFoundException | NoSuchMethodException e) {
                    // 继续尝试下一个
                }
            }
            
            // 如果在常见包名中找不到，尝试搜索类路径中的所有类
            // 这需要遍历类加载器，比较复杂，暂时跳过
            
        } catch (Exception e) {
            // 初始化失败，后续调用会返回空信息
        } finally {
            initialized = true;
        }
    }
    /**
     * 媒体信息数据类
     */
    public static class MediaInfo {
        private final String title;
        private final long position; // 当前播放进度（秒）
        private final long duration; // 总时长（秒）
        private final String base64; // Base64 编码的封面图
        
        private final boolean hasMedia;
        
        public MediaInfo(String title, long position, long duration, String base64, boolean hasMedia) {
            this.title = title;
            this.position = position;
            this.duration = duration;
            this.base64 = base64;
            this.hasMedia = hasMedia;
        }
        
        public String getTitle() {
            return title;
        }
        
        public long getPosition() {
            return position;
        }
        
        public long getDuration() {
            return duration;
        }
        
        public String getBase64() {
            return base64;
        }
        
        public boolean hasMedia() {
            return hasMedia;
        }
        
        /**
         * 获取格式化的播放进度（MM:SS）
         */
        public String getFormattedPosition() {
            return String.format("%02d:%02d", position / 60, position % 60);
        }
        
        /**
         * 获取格式化的总时长（MM:SS）
         */
        public String getFormattedDuration() {
            return String.format("%02d:%02d", duration / 60, duration % 60);
        }
        
        /**
         * 获取播放进度百分比（0-100）
         */
        public double getProgressPercent() {
            if (duration == 0) return 0.0;
            return (double) position / duration * 100.0;
        }
    }
    
    /**
     * 获取当前媒体信息
     * @return MediaInfo 对象，如果没有媒体播放则 hasMedia 为 false
     */
    public static MediaInfo getMediaInfo() {
        initialize();
        
        if (smtcLoaderClass == null || getSmtcInfoMethod == null) {
            // 如果找不到 SmtcLoader 类，返回空信息
            return new MediaInfo("SMTC not found", 0, 0, "", false);
        }
        
        try {
            // 使用反射调用 getSmtcInfo 方法
            Object result = getSmtcInfoMethod.invoke(null);
            if (result == null) {
                return new MediaInfo("No media", 0, 0, "", false);
            }
            
            String info = result.toString();
            if (info == null || info.isEmpty()) {
                return new MediaInfo("No media", 0, 0, "", false);
            }
            
            String[] parts = info.split("\\|", -1);
            
            if (parts.length >= 4 && !"No media".equals(parts[0]) && parts[0] != null && !parts[0].isEmpty()) {
                try {
                    String title = parts[0];
                    long pos = Long.parseLong(parts[1]);
                    long dur = Long.parseLong(parts[2]);
                    String base64 = parts.length > 3 ? parts[3] : "";
                    
                    // 确保值在合理范围内
                    pos = Math.max(0, pos);
                    dur = Math.max(0, dur);
                    
                    return new MediaInfo(title, pos, dur, base64, true);
                } catch (NumberFormatException e) {
                    // 解析数字失败
                    return new MediaInfo("No media", 0, 0, "", false);
                }
            } else {
                return new MediaInfo("No media", 0, 0, "", false);
            }
        } catch (Exception e) {
            // 如果库未加载或出现错误，返回空信息（不打印堆栈，避免日志污染）
            return new MediaInfo("No media", 0, 0, "", false);
        }
    }
    
    /**
     * 检查是否有媒体正在播放
     * @return true 如果有媒体正在播放
     */
    public static boolean isMediaPlaying() {
        return getMediaInfo().hasMedia();
    }
    
    /**
     * 获取当前播放的媒体标题
     * @return 媒体标题，如果没有则返回 "No media"
     */
    public static String getCurrentTitle() {
        return getMediaInfo().getTitle();
    }
}


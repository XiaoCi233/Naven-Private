package tech.blinkfix.utils;

import dev.yalan.live.LiveClient;
import dev.yalan.live.LiveUser;

public class PermissionUtils {
    
    /**
     * 检查用户是否有权限使用客户端
     * 只有 ADMINISTRATOR 或 BETA 级别的用户才有权限
     * @return true 如果有权限，否则 false
     */
    public static boolean hasPermission() {
        try {
            LiveClient client = LiveClient.INSTANCE;
            if (client == null || client.liveUser == null) {
                return false;
            }
            LiveUser user = client.liveUser;
            return user.getLevel() == LiveUser.Level.ADMINISTRATOR || 
                   user.getLevel() == LiveUser.Level.BETA;
        } catch (Throwable ignored) {
            return false;
        }
    }
    
    /**
     * 检查权限，如果没有权限则崩溃客户端
     * @param reason 崩溃原因
     */
    public static void checkPermissionOrCrash(String reason) {
        if (!hasPermission()) {
            crashClient(reason);
        }
    }
    
    /**
     * 崩溃客户端并显示错误信息
     * @param reason 崩溃原因
     */
    public static void crashClient(String reason) {
        System.err.println("==============================================");
        System.err.println("Client Permission Check Failed!");
        System.err.println("Reason: " + reason);
        System.err.println("Only ADMINISTRATOR or BETA users can use this client.");
        System.err.println("==============================================");
        
        throw new RuntimeException("Permission denied: " + reason);
    }
}



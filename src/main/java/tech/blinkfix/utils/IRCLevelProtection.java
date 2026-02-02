package tech.blinkfix.utils;

import dev.yalan.live.LiveClient;
import dev.yalan.live.LiveUser;
import net.minecraft.world.entity.player.Player;

/**
 * IRC等级保护工具类
 * 实现规则：当用户的level为General时，无法攻击level为Administrator和Beta的用户
 */
public class IRCLevelProtection {
    
    /**
     * 检查是否可以攻击目标玩家（基于IRC等级）
     * 
     * @param target 目标玩家
     * @return true 如果可以攻击，false 如果受保护不能攻击
     */
    public static boolean canAttack(Player target) {
        try {
            LiveClient live = LiveClient.INSTANCE;
            
            // 如果LiveClient未初始化，允许攻击
            if (live == null || live.liveUser == null) {
                return true;
            }
            
            // 获取当前玩家的等级
            LiveUser.Level myLevel = live.liveUser.getLevel();
            
            // 如果当前玩家不是General，可以攻击任何人
            if (myLevel != LiveUser.Level.GENERAL) {
                return true;
            }
            
            // 当前玩家是General，检查目标玩家的等级
            if (live.getLiveUserMap() != null) {
                LiveUser targetUser = live.getLiveUserMap().get(target.getUUID());
                
                if (targetUser != null) {
                    if (targetUser.isBlinkFixUser()) {
                        LiveUser.Level targetLevel = targetUser.getLevel();

                        // General用户不能攻击Administrator和Beta用户
                        if (targetLevel == LiveUser.Level.ADMINISTRATOR || targetLevel == LiveUser.Level.BETA) {
                            return false;
                        }
                    } else if (targetUser.isNavenUser()) {
                        return false;
                    }
                }
            }
            
            return true;
            
        } catch (Throwable e) {
            // 出错时默认允许攻击
            return true;
        }
    }
    
    /**
     * 获取当前玩家的IRC等级
     * 
     * @return 当前玩家的等级，如果未登录则返回null
     */
    public static LiveUser.Level getMyLevel() {
        try {
            LiveClient live = LiveClient.INSTANCE;
            if (live != null && live.liveUser != null) {
                return live.liveUser.getLevel();
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
    
    /**
     * 获取目标玩家的IRC等级
     * 
     * @param target 目标玩家
     * @return 目标玩家的等级，如果未查询到则返回null
     */
    public static LiveUser.Level getTargetLevel(Player target) {
        try {
            LiveClient live = LiveClient.INSTANCE;
            if (live != null && live.getLiveUserMap() != null) {
                LiveUser targetUser = live.getLiveUserMap().get(target.getUUID());
                if (targetUser != null && targetUser.isBlinkFixUser()) {
                    return targetUser.getLevel();
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}


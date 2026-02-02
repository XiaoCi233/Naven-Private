//package com.heypixel.heypixelmod.modules.impl.misc;
//
//import api.events.tech.blinkfix.EventTarget;
//import impl.events.tech.blinkfix.EventPacket;
//import impl.events.tech.blinkfix.EventUpdate;
//import impl.events.tech.blinkfix.WorldChangeEvent;
//import modules.tech.blinkfix.Category;
//import modules.tech.blinkfix.Module;
//import modules.tech.blinkfix.ModuleInfo;
//import utils.tech.blinkfix.ChatUtils;
//import net.minecraft.network.protocol.Packet;
//import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
//
///**
// * SilentDisconnect - 拦截断开连接界面，将踢出原因显示在聊天栏而继续留在游戏中
// */
//@ModuleInfo(name = "SilentDisconnect", description = "Stay in game when kicked", category = Category.MISC)
//public class SilentDisconnect extends Module {
//    public boolean kick = false;
//    public String kickMessage = null;
//
//    @EventTarget
//    private void onUpdate(EventUpdate event) {
//        // 如果有待显示的踢出消息，显示在聊天栏
//        if (this.kick && this.kickMessage != null) {
//            ChatUtils.addChatMessage(this.kickMessage);
//            this.kickMessage = null; // 只显示一次
//        }
//    }
//
//    @EventTarget
//    private void onPacket(EventPacket event) {
//        Packet<?> packet = event.getPacket();
//        // 收到位置包说明重新连接或respawn，清除踢出状态
//        if (packet instanceof ClientboundPlayerPositionPacket && this.kick) {
//            this.kick = false;
//            this.kickMessage = null;
//        }
//    }
//
//    @EventTarget
//    private void onWorld(WorldChangeEvent event) {
//        // 切换世界时清除状态
//        this.kick = false;
//        this.kickMessage = null;
//    }
//
//    @Override
//    public void onDisable() {
//        this.kick = false;
//        this.kickMessage = null;
//    }
//
//    // 由 mixin 调用
//    public void setKick(boolean kick) {
//        this.kick = kick;
//    }
//
//    // 由 mixin 调用，设置踢出消息
//    public void setKickMessage(String message) {
//        this.kickMessage = message;
//    }
//}

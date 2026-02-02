// Decompiled with: CFR 0.152
// Class Version: 17
package xyz.gay.mixin;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.events.impl.EventServerSetPosition;
import tech.blinkfix.utils.HttpUtils;

import java.io.IOException;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPacketListener.class})
public class MixinClientPacketListener {
    @Redirect(
            method = {"handleMovePlayer"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;)V",
                    ordinal = 1
            )
    )
    public void onSendPacket(Connection instance, Packet<?> pPacket) {
        EventServerSetPosition event = new EventServerSetPosition(pPacket);
        BlinkFix.getInstance().getEventManager().call(event);
        instance.send(event.getPacket());
    }

    @Inject(
            method = {"handleLogin"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/telemetry/WorldSessionTelemetryManager;onPlayerInfoReceived(Lnet/minecraft/world/level/GameType;Z)V",
                    shift = At.Shift.AFTER
            )},
            cancellable = true
    )
    private void onLogin(ClientboundLoginPacket p_105030_, CallbackInfo ci) {
        // ========== 已注释：权限检查 ==========
        // 原先这里限制只有 ADMINISTRATOR 或 BETA 用户才能进入服务器
        // General 用户会因此崩溃，现已禁用此检查
        // utils.tech.blinkfix.PermissionUtils.checkPermissionOrCrash("Attempted to join server without permission");
        // ====================================
        
        try {
            HttpUtils.get("http://127.0.0.1:23233/api/setHook?hook=0");
        } catch (IOException var4) {
        }
    }
}
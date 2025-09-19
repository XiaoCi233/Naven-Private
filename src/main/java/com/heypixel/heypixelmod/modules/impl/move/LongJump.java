// Decompiled with CFR 0.152
// Class Version: 17
package com.heypixel.heypixelmod.modules.impl.move;

import com.heypixel.heypixelmod.BlinkFix;
import com.heypixel.heypixelmod.events.api.EventTarget;
import com.heypixel.heypixelmod.events.api.types.EventType;
import com.heypixel.heypixelmod.events.impl.EventMotion;
import com.heypixel.heypixelmod.events.impl.EventPacket;
import com.heypixel.heypixelmod.events.impl.EventRender2D;
import com.heypixel.heypixelmod.events.impl.EventUpdate;
import com.heypixel.heypixelmod.modules.Category;
import com.heypixel.heypixelmod.modules.Module;
import com.heypixel.heypixelmod.modules.ModuleInfo;
import com.heypixel.heypixelmod.ui.notification.Notification;
import com.heypixel.heypixelmod.ui.notification.NotificationLevel;
import com.heypixel.heypixelmod.utils.ChatUtils;
import com.heypixel.heypixelmod.utils.MoveUtils;
import com.heypixel.heypixelmod.utils.RenderUtils;
import com.heypixel.heypixelmod.utils.SmoothAnimationTimer;
import com.heypixel.heypixelmod.utils.renderer.Fonts;
import com.heypixel.heypixelmod.utils.renderer.text.CustomTextRenderer;
import com.heypixel.heypixelmod.utils.rotation.Rotation;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

@ModuleInfo(name="LongJump", category=Category.MOVEMENT, description="Allows you to use fireball longjump")
public class LongJump
        extends Module {
    public static Rotation rotation = null;
    public static LongJump instance;
    private final SmoothAnimationTimer progress = new SmoothAnimationTimer(0.0f, 0.0f, 0.2f);
    private static final int mainColor = new Color(150, 45, 45, 255).getRGB();
    private static final int backgroundColor = Integer.MIN_VALUE;
    private boolean notMoving = false;
    private boolean enabled = false;
    private int rotateTick = 0;
    private int lastSlot = -1;
    private boolean delayed = false;
    private boolean shouldDisableAndRelease = false;
    private boolean isUsingItem = false;
    private boolean mouse4Pressed = false;
    private boolean mouse5Pressed = false;
    private long delayStartTime = 0L;
    private int usedFireballCount = 0;
    private int receivedKnockbacks = 0;
    private int initialFireballCount = 0;
    private int releasedKnockbacks = 0;
    private final List<Integer> knockbackPositions = new ArrayList<Integer>();
    private final LinkedBlockingQueue<Packet<?>> packets = new LinkedBlockingQueue();

    public LongJump() {
        instance = this;
    }

    private void releaseAll() {
        while (!this.packets.isEmpty()) {
            try {
                Packet<ClientPacketListener> packet = (Packet<ClientPacketListener>) this.packets.poll();
                if (packet == null || mc.getConnection() == null) continue;
                packet.handle(mc.getConnection());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void releaseToKnockback(int knockbackIndex) {
        if (knockbackIndex >= this.knockbackPositions.size()) {
            return;
        }
        int targetPosition = this.knockbackPositions.get(knockbackIndex);
        int releasedCount = 0;
        while (!this.packets.isEmpty() && releasedCount <= targetPosition) {
            try {
                Packet<ClientPacketListener> packet = (Packet<ClientPacketListener>) this.packets.poll();
                if (packet != null && mc.getConnection() != null) {
                    packet.handle(mc.getConnection());
                }
                ++releasedCount;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (int i = knockbackIndex + 1; i < this.knockbackPositions.size(); ++i) {
            this.knockbackPositions.set(i, this.knockbackPositions.get(i) - (targetPosition + 1));
        }
    }

    private void updateProgressBar() {
        float remainingKnockbacks;
        this.progress.target = this.receivedKnockbacks == 0 ? 0.0f : ((remainingKnockbacks = (float)(this.receivedKnockbacks - this.releasedKnockbacks)) > 0.0f ? Mth.clamp(remainingKnockbacks / (float)this.receivedKnockbacks * 100.0f, 0.0f, 100.0f) : 0.0f);
    }

    private int getFireballSlot() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = LongJump.mc.player.getInventory().getItem(i);
            if (stack.isEmpty() || stack.getItem() != Items.FIRE_CHARGE) continue;
            return i;
        }
        return -1;
    }

    private int getFireballCount() {
        int count = 0;
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = LongJump.mc.player.getInventory().getItem(i);
            if (itemStack.getItem() != Items.FIRE_CHARGE) continue;
            count += itemStack.getCount();
        }
        return count;
    }

    private int setupFireballSlot() {
        int fireballSlot = this.getFireballSlot();
        if (fireballSlot == -1) {
            Notification notification = new Notification(NotificationLevel.ERROR, "No FireBall Found!", 3000L);
            BlinkFix.getInstance().getNotificationManager().addNotification(notification);
            this.setEnabled(false);
        }
        return fireballSlot;
    }

    @Override
    public void onEnable() {
        this.releaseAll();
        this.rotateTick = 0;
        this.enabled = true;
        this.lastSlot = -1;
        this.notMoving = false;
        this.delayed = false;
        this.isUsingItem = false;
        rotation = null;
        this.shouldDisableAndRelease = false;
        this.mouse4Pressed = false;
        this.mouse5Pressed = false;
        this.delayStartTime = 0L;
        this.usedFireballCount = 0;
        this.receivedKnockbacks = 0;
        this.initialFireballCount = 0;
        this.releasedKnockbacks = 0;
        this.knockbackPositions.clear();
        this.progress.target = 0.0f;
        this.progress.value = 0.0f;
        ChatUtils.addChatMessage("§aLongJump enabled! Press Mouse4 to jump & use fireball, Mouse5 to release each knockback");
    }

    @Override
    public void onDisable() {
        this.releaseAll();
        if (this.lastSlot != -1 && LongJump.mc.player != null) {
            LongJump.mc.player.getInventory().selected = this.lastSlot;
        }
        LongJump.mc.options.keyUse.setDown(false);
        LongJump.mc.options.keyJump.setDown(false);
        rotation = null;
        this.isUsingItem = false;
        this.shouldDisableAndRelease = false;
        this.mouse4Pressed = false;
        this.mouse5Pressed = false;
        this.delayStartTime = 0L;
        this.usedFireballCount = 0;
        this.receivedKnockbacks = 0;
        this.initialFireballCount = 0;
        this.releasedKnockbacks = 0;
        this.knockbackPositions.clear();
        this.progress.target = 0.0f;
        this.progress.value = 0.0f;
        super.onDisable();
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        boolean currentMouse5;
        Notification notification;
        boolean currentMouse4;
        if (!this.isEnabled()) {
            return;
        }
        if (this.shouldDisableAndRelease) {
            this.setEnabled(false);
            return;
        }
        if (this.enabled) {
            if (!MoveUtils.isMoving()) {
                this.notMoving = true;
            }
            this.enabled = false;
        }
        boolean bl = currentMouse4 = GLFW.glfwGetMouseButton(mc.getWindow().getWindow(), 3) == 1;
        if (currentMouse4 && !this.mouse4Pressed) {
            int fireballSlot;
            this.mouse4Pressed = true;
            if (!this.isUsingItem && this.rotateTick == 0 && (fireballSlot = this.setupFireballSlot()) != -1) {
                this.lastSlot = LongJump.mc.player.getInventory().selected;
                LongJump.mc.player.getInventory().selected = fireballSlot;
                this.rotateTick = 1;
                notification = new Notification(NotificationLevel.SUCCESS, "§eStarting fireball usage #" + (this.usedFireballCount + 1), 3000L);
                com.heypixel.heypixelmod.BlinkFix.getInstance().getNotificationManager().addNotification(notification);
            }
        } else if (!currentMouse4) {
            this.mouse4Pressed = false;
        }
        boolean bl2 = currentMouse5 = GLFW.glfwGetMouseButton(mc.getWindow().getWindow(), 4) == 1;
        if (currentMouse5 && !this.mouse5Pressed) {
            this.mouse5Pressed = true;
            if (this.delayed && this.releasedKnockbacks < this.receivedKnockbacks) {
                notification = new Notification(NotificationLevel.SUCCESS, "§aReleasing " + (this.releasedKnockbacks + 1) + "/" + this.receivedKnockbacks, 3000L);
                com.heypixel.heypixelmod.BlinkFix.getInstance().getNotificationManager().addNotification(notification);
                this.releaseToKnockback(this.releasedKnockbacks);
                ++this.releasedKnockbacks;
                this.updateProgressBar();
                if (this.releasedKnockbacks >= this.receivedKnockbacks) {
                    this.delayed = false;
                    this.setEnabled(false);
                }
            } else if (!this.delayed) {
                notification = new Notification(NotificationLevel.ERROR, "No intercepted packets", 3000L);
                com.heypixel.heypixelmod.BlinkFix.getInstance().getNotificationManager().addNotification(notification);
                this.setEnabled(false);
            } else {
                ChatUtils.addChatMessage("§cAll already released");
            }
        } else if (!currentMouse5) {
            this.mouse5Pressed = false;
        }
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        if (!this.isEnabled()) {
            return;
        }
        CustomTextRenderer font = Fonts.opensans;
        int screenWidth = mc.getWindow().getWidth();
        int screenHeight = mc.getWindow().getHeight();
        int progressX = screenWidth / 2 - 60;
        int progressY = screenHeight / 2 + 35;
        int progressWidth = 120;
        int progressHeight = 6;
        this.progress.update(true);
        if (this.receivedKnockbacks > 0) {
            this.updateProgressBar();
            RenderUtils.drawRoundedRect(event.getStack(), progressX, progressY, progressWidth, progressHeight, 2.0f, Integer.MIN_VALUE);
            float progressFill = this.progress.value / 100.0f * (float)progressWidth;
            if (progressFill > 0.0f) {
                RenderUtils.drawRoundedRect(event.getStack(), progressX, progressY, progressFill, progressHeight, 2.0f, mainColor);
            }
            String progressText = String.format("§fKnockbacks: %d/%d", this.receivedKnockbacks - this.releasedKnockbacks, this.receivedKnockbacks);
            float progressTextX = (float)screenWidth / 2.0f - (float)LongJump.mc.font.width(progressText) / 2.0f;
            float progressTextY = progressY + progressHeight + 6;
            font.render(event.getStack(), progressText, (int)progressTextX + 5, (int)progressTextY, Color.WHITE, true, 0.4);
        } else {
            String progressText = "Waiting for knockback...";
            float progressTextX = (float)screenWidth / 2.0f - (float)LongJump.mc.font.width(progressText) / 2.0f;
            float progressTextY = progressY + progressHeight + 6;
            RenderUtils.drawRoundedRect(event.getStack(), progressX, progressY, progressWidth, progressHeight, 2.0f, Integer.MIN_VALUE);
            font.render(event.getStack(), progressText, (int)progressTextX + 5, (int)progressTextY, Color.WHITE, true, 0.4);
        }
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (!this.isEnabled() || LongJump.mc.level == null) {
            if (this.delayed) {
                mc.execute(() -> {
                    this.releaseAll();
                    this.delayed = false;
                });
            }
            return;
        }
        if (this.delayed && event.getType() == EventType.RECEIVE) {
            ClientboundSetEntityMotionPacket motionPacket;
            Packet<?> packet = event.getPacket();
            if (packet instanceof ClientboundPlayerPositionPacket) {
                this.shouldDisableAndRelease = true;
                event.setCancelled(true);
                return;
            }
            if (packet instanceof ClientboundSetEntityMotionPacket && (motionPacket = (ClientboundSetEntityMotionPacket)((Object)packet)).getId() == LongJump.mc.player.getId()) {
                ++this.receivedKnockbacks;
                this.knockbackPositions.add(this.packets.size());
                this.updateProgressBar();
                mc.execute(() -> ChatUtils.addChatMessage("§e" + this.receivedKnockbacks + " received"));
            }
            event.setCancelled(true);
            this.packets.add(packet);
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof ClientboundSetEntityMotionPacket) {
            ClientboundSetEntityMotionPacket packet2 = (ClientboundSetEntityMotionPacket)((Object)packet);
            if (event.getType() == EventType.RECEIVE && packet2.getId() == LongJump.mc.player.getId() && this.usedFireballCount > 0 && !this.delayed) {
                ++this.receivedKnockbacks;
                this.knockbackPositions.add(this.packets.size());
                mc.execute(() -> ChatUtils.addChatMessage("§eReceived #" + this.receivedKnockbacks + ", starting packet interception"));
                event.setCancelled(true);
                this.packets.add(event.getPacket());
                this.delayed = true;
                this.delayStartTime = System.currentTimeMillis();
                this.updateProgressBar();
                mc.execute(() -> ChatUtils.addChatMessage("§ePacket interception started, press Mouse5 to release each"));
            }
        }
    }

    @EventTarget
    public void onMotion(EventMotion event) {
        if (!this.isEnabled()) {
            return;
        }
        if (event.getType() == EventType.PRE) {
            if (this.rotateTick > 0) {
                if (this.rotateTick == 1) {
                    float pitch;
                    float yaw;
                    ++this.usedFireballCount;
                    ChatUtils.addChatMessage("§aJumping for fireball #" + this.usedFireballCount);
                    LongJump.mc.options.keyJump.setDown(true);
                    if (!this.notMoving) {
                        yaw = LongJump.mc.player.getYRot() - 180.0f;
                        pitch = 88.0f;
                    } else {
                        yaw = LongJump.mc.player.getYRot();
                        pitch = 90.0f;
                    }
                    rotation = new Rotation(yaw, pitch);
                }
                if (this.rotateTick >= 2) {
                    this.rotateTick = 0;
                    int fireballSlot = this.setupFireballSlot();
                    if (fireballSlot != -1) {
                        LongJump.mc.player.getInventory().selected = fireballSlot;
                        this.initialFireballCount = this.getFireballCount();
                        LongJump.mc.options.keyUse.setDown(true);
                        this.isUsingItem = true;
                        ChatUtils.addChatMessage("§eFireball #" + this.usedFireballCount + " started, initial count: " + this.initialFireballCount);
                    } else {
                        this.setEnabled(false);
                    }
                }
                if (this.rotateTick != 0) {
                    ++this.rotateTick;
                }
            }
        } else if (this.isUsingItem) {
            int currentFireballCount = this.getFireballCount();
            if (currentFireballCount < this.initialFireballCount) {
                LongJump.mc.options.keyUse.setDown(false);
                LongJump.mc.options.keyJump.setDown(false);
                rotation = null;
                this.isUsingItem = false;
                ChatUtils.addChatMessage("§eFireball #" + this.usedFireballCount + " used! Count: " + this.initialFireballCount + " -> " + currentFireballCount + ", waiting for next input");
            } else if (this.getFireballSlot() == -1) {
                LongJump.mc.options.keyUse.setDown(false);
                LongJump.mc.options.keyJump.setDown(false);
                rotation = null;
                this.isUsingItem = false;
                ChatUtils.addChatMessage("§cNo more fireballs available!");
            }
        }
    }
}
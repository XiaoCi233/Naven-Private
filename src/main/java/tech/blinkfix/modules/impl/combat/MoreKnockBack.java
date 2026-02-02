package tech.blinkfix.modules.impl.combat;

import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.impl.EventAttack;
import tech.blinkfix.events.impl.EventMoveInput;
import tech.blinkfix.events.impl.EventRunTicks;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.FloatValue;
import tech.blinkfix.values.impl.ModeValue;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@ModuleInfo(
        name = "MoreKnockBack",
        description = "Make your attack target knock back further(WTap)",
        category = Category.COMBAT
)
public class MoreKnockBack extends Module {

    private final ModeValue modeValue = ValueBuilder.create(this, "Mode")
            .setDefaultModeIndex(1)
            .setModes("Wtap", "Legit", "Packet")
            .build()
            .getModeValue();

    private final FloatValue hurtTime = ValueBuilder.create(this, "HurtTime")
            .setDefaultFloatValue(10.0F)
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(10.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();

    public int tick;
    private boolean wasWKeyPressed = false;
    private final Minecraft mc = Minecraft.getInstance();

    @EventTarget
    private void onAttack(EventAttack event) {
        if (mc.player == null || mc.level == null) return;

        Entity target = event.getTarget();
        if (!(target instanceof LivingEntity entity)) return;

        if (entity.hurtTime >= hurtTime.getCurrentValue()) {
            String mode = modeValue.getCurrentMode();

            switch (mode) {
                case "Legit", "Wtap" -> tick = 2;

                case "Packet" -> {
                    var player = mc.player;
                    if (player != null && player.connection != null) {
                        if (player.isSprinting()) {
                            player.connection.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
                        }

                        player.connection.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
                        player.connection.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
                        player.connection.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));

                        player.setSprinting(true);
                        try {
                            player.getClass().getMethod("setWasSprinting", boolean.class).invoke(player, true);
                        } catch (Exception e) {
                            // Method might not exist in all versions
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void onMoveInput(EventMoveInput event) {
        if (mc.player == null) return;

        if (modeValue.getCurrentMode().equals("Wtap")) {
            if (tick == 2) {
                wasWKeyPressed = mc.options.keyUp.isDown();
                event.setForward(0.0f);
                tick = 1;
            } else if (tick == 1) {
                if (!wasWKeyPressed) {
                    event.setForward(1.0f);
                } else {
                    event.setForward(1.0f);
                }
                tick = 0;
                wasWKeyPressed = false;
            }
        }
    }

    @EventTarget
    public void onUpdate(EventRunTicks eventUpdate) {
        if (mc.player == null) return;
        if (eventUpdate.getType() != EventType.PRE) return;

        if (modeValue.getCurrentMode().equals("Legit")) {
            if (tick == 2 && mc.player != null) {
                mc.player.setSprinting(false);
                tick = 1;
            } else if (tick == 1 && mc.player != null) {
                mc.player.setSprinting(true);
                tick = 0;
            }
        }
    }

    @Override
    public void onEnable() {
        tick = 0;
        wasWKeyPressed = false;
    }

    @Override
    public void onDisable() {
        tick = 0;
        if (mc.options != null && mc.options.keyUp != null) {
            if (!wasWKeyPressed) {
                mc.options.keyUp.setDown(true);
            } else {
                mc.options.keyUp.setDown(true);
            }
        }
        wasWKeyPressed = false;
    }
}
package com.heypixel.heypixelmod.obsoverlay.modules.impl.combat;

import com.heypixel.heypixelmod.obsoverlay.Naven;
import com.heypixel.heypixelmod.obsoverlay.events.api.EventTarget;
import com.heypixel.heypixelmod.obsoverlay.events.api.types.EventType;
import com.heypixel.heypixelmod.obsoverlay.events.impl.EventMotion;
import com.heypixel.heypixelmod.obsoverlay.modules.Category;
import com.heypixel.heypixelmod.obsoverlay.modules.Module;
import com.heypixel.heypixelmod.obsoverlay.modules.ModuleInfo;
import com.heypixel.heypixelmod.obsoverlay.modules.impl.combat.Aura;
import com.heypixel.heypixelmod.obsoverlay.modules.impl.misc.Teams;
import com.heypixel.heypixelmod.obsoverlay.utils.TickTimeHelper;
import com.heypixel.heypixelmod.obsoverlay.values.ValueBuilder;
import com.heypixel.heypixelmod.obsoverlay.values.impl.BooleanValue;
import com.heypixel.heypixelmod.obsoverlay.values.impl.FloatValue;
import java.util.Comparator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

@ModuleInfo(
        name = "ThrowableAura",
        description = "AutoThrow uses items to KB nearby players within FoV, ignoring teammates and bots",
        category = Category.COMBAT
)
public class ThrowableAura extends Module {
    TickTimeHelper timer = new TickTimeHelper();
    BooleanValue useFishingRod = ValueBuilder.create(this, "Use Fishing Rod").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue useSnowball = ValueBuilder.create(this, "Use Snowball").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue checkTeams = ValueBuilder.create(this, "Check Teams").setDefaultBooleanValue(true).build().getBooleanValue();
    BooleanValue filterBots = ValueBuilder.create(this, "Filter Bots").setDefaultBooleanValue(true).build().getBooleanValue();
    FloatValue range = ValueBuilder.create(this, "Range")
            .setDefaultFloatValue(6.0F)
            .setFloatStep(0.1F)
            .setMinFloatValue(3.0F)
            .setMaxFloatValue(8.0F)
            .build()
            .getFloatValue();
    FloatValue cooldown = ValueBuilder.create(this, "Cooldown(ms)")
            .setDefaultFloatValue(50.0F)
            .setFloatStep(5.0F)
            .setMinFloatValue(1.0F)
            .setMaxFloatValue(50.0F)
            .build()
            .getFloatValue();
    FloatValue fov = ValueBuilder.create(this, "Fov")
            .setDefaultFloatValue(90.0F)
            .setFloatStep(5.0F)
            .setMinFloatValue(30.0F)
            .setMaxFloatValue(180.0F)
            .build()
            .getFloatValue();
    private static final Minecraft mc = Minecraft.getInstance();
    private int lastSlot = -1;
    private boolean shouldRestore = false;
    private boolean rodThrown = false;
    private int rodThrowTick = 0;

    @EventTarget
    public void onMotion(EventMotion event) {
        if (event.getType() == EventType.PRE) {
            if (this.rodThrown) {
                if (mc.player != null && mc.player.tickCount - this.rodThrowTick >= 6) {
                    int rodSlot = this.getHotbarItemSlot(Items.FISHING_ROD);
                    if (rodSlot != -1) {
                        mc.player.getInventory().selected = rodSlot;
                        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                    }

                    this.rodThrown = false;
                    this.shouldRestore = true;
                }

            } else {
                if (this.shouldRestore && this.lastSlot != -1 && mc.player != null) {
                    mc.player.getInventory().selected = this.lastSlot;
                    this.lastSlot = -1;
                    this.shouldRestore = false;
                }

                if (!this.isKillAuraActive()) {
                    if (this.timer.delay(this.cooldown.getCurrentValue())) {
                        if (mc.player != null && mc.level != null) {
                            Player target = this.findTarget();
                            if (target != null) {
                                boolean used = false;
                                if (this.useFishingRod.getCurrentValue()) {
                                    used = this.useFishingRod();
                                }

                                if (!used && this.useSnowball.getCurrentValue()) {
                                    used = this.useSnowball();
                                }

                                if (used) {
                                    this.timer.reset();
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isKillAuraActive() {
        Aura killAura = (Aura)Naven.getInstance().getModuleManager().getModule(Aura.class);
        return killAura != null && killAura.isEnabled() && Aura.target != null;
    }

    public void onDisable() {
        this.timer.reset();
        this.shouldRestore = false;
        this.lastSlot = -1;
        if (this.rodThrown) {
            if (mc.player != null) {
                int rodSlot = this.getHotbarItemSlot(Items.FISHING_ROD);
                if (rodSlot != -1) {
                    mc.player.getInventory().selected = rodSlot;
                    mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                }
            }

            this.rodThrown = false;
        }

    }

    private Player findTarget() {
        ClientLevel level = mc.level;
        return level != null && mc.player != null ?
                (Player)level.players().stream()
                        .filter(p -> p != mc.player)
                        .filter(p -> !p.isSpectator())
                        .filter(p -> mc.player.distanceTo(p) <= this.range.getCurrentValue())
                        .filter(p -> mc.player.hasLineOfSight(p))
                        .filter(p -> this.isInFOV(p))
                        .filter(p -> !this.isTeammate(p))
                        .filter(p -> !this.isBot(p))
                        .min(Comparator.comparingDouble(p -> mc.player.distanceTo(p)))
                        .orElse(null) :
                null;
    }

    private boolean isTeammate(Player target) {
        return this.checkTeams.getCurrentValue() ? Teams.isSameTeam(target) : false;
    }

    private boolean isBot(Player target) {
        return this.filterBots.getCurrentValue() ? false : false;
    }

    private boolean isInFOV(Player target) {
        if (mc.player == null) {
            return false;
        } else {
            Vec3 playerLook = mc.player.getViewVector(1.0F);
            Vec3 toTarget = new Vec3(
                    target.getX() - mc.player.getX(),
                    target.getY() - mc.player.getY(),
                    target.getZ() - mc.player.getZ()
            ).normalize();
            double dotProduct = playerLook.dot(toTarget);
            double fovCos = Math.cos(Math.toRadians(this.fov.getCurrentValue() / 2.0D));
            return dotProduct >= fovCos;
        }
    }

    private boolean useFishingRod() {
        int slot = this.getHotbarItemSlot(Items.FISHING_ROD);
        if (slot == -1) {
            return false;
        } else {
            if (this.lastSlot == -1) {
                this.lastSlot = mc.player.getInventory().selected;
            }

            mc.player.getInventory().selected = slot;
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            this.rodThrown = true;
            this.rodThrowTick = mc.player.tickCount;
            return true;
        }
    }

    private boolean useSnowball() {
        int slot = this.getHotbarItemSlot(Items.SNOWBALL);
        if (slot == -1) {
            return false;
        } else {
            if (this.lastSlot == -1) {
                this.lastSlot = mc.player.getInventory().selected;
            }

            mc.player.getInventory().selected = slot;
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            this.shouldRestore = true;
            return true;
        }
    }

    private int getHotbarItemSlot(Item item) {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() == item && !stack.isEmpty()) {
                return i;
            }
        }
        return -1;
    }
}
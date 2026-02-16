package com.surface.mod.fight;

import com.cubk.event.annotations.EventPriority;
import com.cubk.event.annotations.EventTarget;
import com.surface.Wrapper;
import com.surface.events.*;
import com.surface.interfaces.ItemChecker;
import com.surface.interfaces.TargetFilter;
import com.surface.mod.Mod;
import com.surface.util.TimerUtils;
import com.surface.util.player.RaytraceUtils;
import com.surface.util.player.RotationUtils;
import com.surface.util.render.RenderUtils;
import com.surface.util.render.WorldToScreenUtils;
import com.surface.util.render.shader.ShaderElement;
import com.surface.util.struct.Rotation;
import com.surface.value.impl.*;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.utils.PacketUtil;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import de.florianmichael.viamcp.fixes.AttackOrder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import renderassist.rendering.BasicRendering;

import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZiYue Dai
 */
public class KillAuraModule extends Mod implements TargetFilter, ItemChecker {
    private final ModeValue movement = new ModeValue("Movement", "Proper", new String[]{"Proper", "No Correct"});
    private final ModeValue sort = new ModeValue("Target Sort", "Proximity", new String[]{"Proximity", "Angle", "Health"});

    private final NumberValue cps = new NumberValue("CPS", 10.0D, 1.0D, 20.0D, 0.5D);
    private final NumberValue range = new NumberValue("Range", 3.1D, 1.0D, 6.0D, 0.1D);
    private final NumberValue rotationSpeed = new NumberValue("Rotation Speed", 180, 10D, 180D, 10D);

    private final BooleanValue keepSprint = new BooleanValue("Keep Sprint", false);
    private final BooleanValue autoBlock = new BooleanValue("Auto Block", false);
    private final BooleanValue multi = new BooleanValue("Multi Select", false);
    public final BooleanValue sbteams = new BooleanValue("ScoreboardTeam", false);
    public final BooleanValue armorteams = new BooleanValue("ArmorTeam", false);
    private final BooleanValue targetESP = new BooleanValue("Target ESP", true);
    private final BooleanValue blurESP = new BooleanValue("Blur", true) {
        @Override
        public boolean isVisible() {
            return targetESP.getValue();
        }
    };
    private final BooleanValue dynamicAlpha = new BooleanValue("Dynamic Alpha", true) {
        @Override
        public boolean isVisible() {
            return targetESP.getValue();
        }
    };
    private final ColorValue espColor = new ColorValue("ESP Color", new Color(255, 0, 0)) {
        @Override
        public boolean isVisible() {
            return targetESP.getValue();
        }
    }.rainbow(true);

    private final FilterValue<Entity> filter = getNewFilter();
    public List<EntityLivingBase> targets = new ArrayList<>();
    public EntityLivingBase target;
    private final TimerUtils timer = new TimerUtils();
    public boolean blocking = false;

    public KillAuraModule() {
        super("Kill Aura", Category.FIGHT);
        registerValues(movement, sort, cps, range, rotationSpeed, keepSprint, autoBlock, multi, sbteams,armorteams,filter, targetESP, blurESP, dynamicAlpha, espColor);
    }

    @Override
    protected void onEnable() {
        target = null;
    }

    @Override
    public String getModTag() {
        return sort.getValue();
    }

    @EventTarget
    private void onRender2D(Event2D event) {
        espColor.alpha(!dynamicAlpha.getValue());
        if (targetESP.getValue() && target != null) {
            if (multi.getValue()) {
                for (EntityLivingBase entityLivingBase : targets) {
                    draw(event, entityLivingBase);
                }
            } else {
                draw(event, target);
            }
        }
    }

    private void draw(Event2D event, EntityLivingBase entity) {
        WorldToScreenUtils.onRender2D(event, entity, (x, y, finalX, finalY) -> {
            if (blurESP.getValue()) {
                ShaderElement.blurArea(x, y, finalX - x, finalY - y);
            }
            BasicRendering.drawRect(x, y, finalX - x, finalY - y, dynamicAlpha.getValue() ? RenderUtils.reAlpha(espColor.getRGB(), 100 + entity.hurtTime * 4) : espColor.getRGB());
            GlStateManager.resetColor();
        });
    }

    @EventTarget
    private void onPre(EventUpdate event) {
        if (Wrapper.Instance.getModManager().getModFromName("Scaffold").getState()) return;
        if (Wrapper.Instance.getModManager().getModFromName("Blink").getState()) return;
        if (targets.isEmpty()) {

            if (autoBlock.getValue() && !mc.thePlayer.isBlocking()) {
                unblock(true);
            }
            return;
        }
        target = targets.get(0);

        if (multi.getValue()) {
            if (targets.size() > 1) {
                for (EntityLivingBase entity : targets) {
                    if (entity.hurtTime == 0)
                        target = entity;
                }
                if (target == null)
                    target = targets.get(0);
            } else {
                target = targets.get(0);
            }
        }
        if (target != null) {
            Vector3d targetPos; // i paste
            final double yDist = target.posY - mc.thePlayer.posY;
            if (yDist >= 1.7) {
                targetPos = new Vector3d(target.posX, target.posY, target.posZ);
            } else if (yDist <= -1.7) {
                targetPos = new Vector3d(target.posX, target.posY + target.getEyeHeight(), target.posZ);
            } else {
                targetPos = new Vector3d(target.posX, target.posY + target.getEyeHeight() / 2, target.posZ);
            }
            Rotation current = RotationUtils.getRotationFromEyeToPoint(targetPos);
            Wrapper.Instance.getRotationManager().setRotation(current, rotationSpeed.getValue().floatValue(), movement.isCurrentMode("Proper"));
        }
    }

    public boolean shouldAttack() {
        return (mc.thePlayer.canEntityBeSeen(target) ? mc.thePlayer.getClosestDistanceToEntity(target) : mc.thePlayer.getDistanceToEntity(target)) <= range.getValue();
    }

    @EventTarget
    @EventPriority(12)
    public void onPre(EventPreUpdate event) {
        if (Wrapper.Instance.getModManager().getModFromName("Scaffold").getState()) return;
        if (Wrapper.Instance.getModManager().getModFromName("Blink").getState()) return;
        if(mc.playerController.isBreakingBlock())return;

        if (autoBlock.getValue() && !targets.isEmpty() && blocking)
            unblock(false);

        if (target != null) {
            if (timer.hasTimeElapsed((long) (1000 / (cps.getValue() * 1.5))) && shouldAttack()) {
                if (target != null && (!movement.isCurrentMode("Proper") || !(mc.thePlayer.ticksSprint <= 1 && mc.thePlayer.isSprinting()))) {
                    if (keepSprint.getValue()) {
                        Wrapper.Instance.getEventManager().call(new EventAttack(target, true));
                        mc.getNetHandler().getNetworkManager().sendPacket(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));
                        mc.thePlayer.swingItem();
                        mc.thePlayer.onEnchantmentCritical(target);
                        Wrapper.Instance.getEventManager().call(new EventAttack(target, false));
                    } else {
                        Wrapper.Instance.getEventManager().call(new EventAttack(target, true));
                        AttackOrder.sendFixedAttack(mc.thePlayer, target);
                        Wrapper.Instance.getEventManager().call(new EventAttack(target, false));
                    }
                    timer.reset();
                }
            }
        }
    }

    @EventTarget
    private void onPost(EventPostUpdate e) {
        sortTargets();
        if (targets.isEmpty()) {
            target = null;
        }
        if (autoBlock.getValue() && !targets.isEmpty() && !blocking)
            block();
    }

    @Override
    public void onDisable() {
        targets.clear();
        if (mc.thePlayer != null && blocking) {
            unblock(true);
        }
    }

    public boolean armorTeam(EntityLivingBase entityPlayer) {
        if (entityPlayer instanceof EntityPlayer) {
            if (mc.thePlayer.inventory.armorInventory[3] != null && ((EntityPlayer) entityPlayer).inventory.armorInventory[3] != null) {
                ItemStack myHead = mc.thePlayer.inventory.armorInventory[3];
                ItemArmor myItemArmor = (ItemArmor) myHead.getItem();
                ItemStack entityHead = ((EntityPlayer) entityPlayer).inventory.armorInventory[3];
                ItemArmor entityItemArmor = (ItemArmor) entityHead.getItem();
                if (String.valueOf(entityItemArmor.getColor(entityHead)).equals("10511680")) {
                    return true;
                }
                return myItemArmor.getColor(myHead) == entityItemArmor.getColor(entityHead);
            }
        }
        return false;
    }

    public  boolean isOnSameTeam(Entity entity) {
        try {
            String self = Minecraft.getMinecraft().thePlayer.getDisplayName().getUnformattedText();
            String target = entity.getDisplayName().getUnformattedText();
            if (self.startsWith("\u00a7")) {
                if (!target.contains("\u00a7")) {
                    return true;
                }
                if (self.length() <= 2 || target.length() <= 2) {
                    return false;
                }
                return self.substring(0, 2).equals(target.substring(0, 2));
            }
        } catch (Throwable ignored) {}
        return false;

    }

    public void sortTargets() {
        targets.clear();
        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase entLiving = (EntityLivingBase) entity;
                if (mc.thePlayer.getClosestDistanceToEntity(entLiving) < range.getValue() && entLiving != mc.thePlayer && !entLiving.isDead && filter.isValid(entLiving)) {
                    targets.add(entLiving);
                }
                if(isOnSameTeam(entLiving) && sbteams.getValue()){
                    targets.remove(entLiving);
                }
                if(armorTeam(entLiving) && armorteams.getValue()){
                    targets.remove(entLiving);
                }
            }
        }
        if (!targets.isEmpty()) {
            if (sort.isCurrentMode("Proximity")) {
                targets.sort((o1, o2) -> (int) (o1.getDistanceToEntity(mc.thePlayer) - o2.getDistanceToEntity(mc.thePlayer)));
            } else if (sort.isCurrentMode("Angle")) {
                targets.sort((o1, o2) -> (int) (RotationUtils.getRotationDifference(mc.thePlayer.rotationYaw, RotationUtils.getRotationFromEyeToEntity(o1).getYaw()) - RotationUtils.getRotationDifference(mc.thePlayer.rotationYaw, RotationUtils.getRotationFromEyeToEntity(o2).getYaw())));
            } else if (sort.isCurrentMode("Health")) {
                targets.sort((o1, o2) -> (int) (o1.getHealth() - o2.getHealth()));
            }
        }
    }

    private void block() {
        if (hasSword()) {
            PacketWrapper useItem = PacketWrapper.create(29, null,
                    Via.getManager().getConnectionManager().getConnections().iterator().next());
            useItem.write(Type.VAR_INT, 1);
            PacketUtil.sendToServer(useItem, Protocol1_8To1_9.class, true, true);
            mc.getNetHandler().getNetworkManager().sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getCurrentEquippedItem()));
            mc.thePlayer.itemInUseCount = mc.thePlayer.getHeldItem().getMaxItemUseDuration();
            blocking = true;
        }
    }

    private void unblock(boolean view) {
        if (hasSword()) {
            mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            if (view)
                mc.playerController.onStoppedUsingItem(mc.thePlayer);
            blocking = false;
        }
    }
}

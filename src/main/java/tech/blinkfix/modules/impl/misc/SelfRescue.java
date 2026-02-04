package tech.blinkfix.modules.impl.misc;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.impl.EventMotion;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.PermissionGatedModule;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.modules.impl.move.Scaffold;
import tech.blinkfix.modules.impl.move.Stuck;
import tech.blinkfix.ui.notification.Notification;
import tech.blinkfix.ui.notification.NotificationLevel;
import tech.blinkfix.utils.ChatUtils;
import tech.blinkfix.utils.NetworkUtils;
import tech.blinkfix.utils.TimeHelper;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;
import tech.blinkfix.values.impl.FloatValue;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AirBlock;

import java.util.Random;

@ModuleInfo(
        name = "SelfRescue",
        description = "Automatically throws ender pearl when falling into void",
        category = Category.MISC
)
public class SelfRescue extends Module  {
    private final Minecraft mc = Minecraft.getInstance();
    public FloatValue fallDistValue = ValueBuilder.create(this, "Fall Distance")
            .setDefaultFloatValue(3.0F)
            .setFloatStep(1.0F)
            .setMinFloatValue(1.0F)
            .setMaxFloatValue(10.0F)
            .build()
            .getFloatValue();

    public BooleanValue scaffoldValue = ValueBuilder.create(this, "Scaffold")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public BooleanValue autoPearlValue = ValueBuilder.create(this, "Auto Pearl")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    public BooleanValue debugValue = ValueBuilder.create(this, "Debug")
            .setDefaultBooleanValue(false)
            .build()
            .getBooleanValue();

    public BooleanValue onlyVoidValue = ValueBuilder.create(this, "Only Void")
            .setDefaultBooleanValue(false)
            .build()
            .getBooleanValue();

    public FloatValue attemptTime = ValueBuilder.create(this, "Max Attempt Times")
            .setDefaultFloatValue(1.0F)
            .setFloatStep(1.0F)
            .setMinFloatValue(1.0F)
            .setMaxFloatValue(5.0F)
            .build()
            .getFloatValue();

    private static final double T = 10;
    private static final double T_MIN = 0.001;
    private static final double ALPHA = 0.997;
    private CalculateThread calculateThread;
    private int attempted;
    private boolean scaffoldEnabled;
    private boolean calculating;
    private boolean scaffoldManuallyDisabled = false;
    private final TimeHelper timer = new TimeHelper();
    private final Random random = new Random();

    public SelfRescue() {
    }

    @EventTarget
    public void onMoveInput(EventMotion event) {
        if (calculating) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onMotion(EventMotion event) {
        if (mc.player == null) return;


        if (onlyVoidValue.getCurrentValue() && !isAboveVoid()) {
            return;
        }

        Module scaffold = BlinkFix.getInstance().getModuleManager().getModule(Scaffold.class);
        if (scaffoldEnabled && scaffold != null && !scaffold.isEnabled()) {
            scaffoldEnabled = false;
            scaffoldManuallyDisabled = true;
            if (debugValue.getCurrentValue()) {
                ChatUtils.addChatMessage("[SelfRescue] Scaffold was manually disabled, stopping auto-enable");
            }
        }

        if (mc.player.onGround()) {
            if (scaffoldEnabled) {
                if (scaffold != null) {
                    scaffold.setEnabled(false);
                }
                scaffoldEnabled = false;
            }
            attempted = 0;
            calculating = false;
            scaffoldManuallyDisabled = false;
        }

        if (event.getType().name().contains("POST")) {
            if (calculating && (calculateThread == null || calculateThread.completed)) {
                calculating = false;
                if (calculateThread != null && autoPearlValue.getCurrentValue()) {
                    throwPearl(calculateThread.solutionYaw, calculateThread.solutionPitch);
                }
            }
        }
        if (scaffoldManuallyDisabled) {
            return;
        }

        if (mc.player.getDeltaMovement().y < 0.1 &&
                !isBlockUnder() &&
                mc.player.fallDistance > fallDistValue.getCurrentValue()) {
            Module stuck = BlinkFix.getInstance().getModuleManager().getModule(Stuck.class);

            if (mc.player.getDeltaMovement().y >= -1 &&
                    scaffoldValue.getCurrentValue() &&
                    scaffold != null && !scaffold.isEnabled() &&
                    stuck != null && !stuck.isEnabled()) {
                scaffoldEnabled = true;
                scaffold.setEnabled(true);
                if (debugValue.getCurrentValue()) {
                    ChatUtils.addChatMessage("[SelfRescue] Enabled scaffold for slow fall");
                }
            }
            else if (mc.player.getDeltaMovement().y < -1 &&
                    autoPearlValue.getCurrentValue() &&
                    attempted <= this.attemptTime.getCurrentValue() &&
                    !mc.player.onGround()) {
                attempted += 1;
                int pearlSlot = findEnderPearlSlot();
                if (pearlSlot == -1) {
                    if (debugValue.getCurrentValue()) {
                        ChatUtils.addChatMessage("[SelfRescue] No ender pearl found!");
                    }
                    return;
                }
                mc.player.getInventory().selected = pearlSlot < 9 ? pearlSlot : 8;
                if (scaffoldEnabled && scaffold != null && scaffold.isEnabled()) {
                    scaffold.setEnabled(false);
                    scaffoldEnabled = false;
                    if (debugValue.getCurrentValue()) {
                        ChatUtils.addChatMessage("[SelfRescue] Disabled scaffold for pearl throw");
                    }
                }

                calculating = true;
                calculateThread = new CalculateThread(
                        mc.player.getX(),
                        mc.player.getY(),
                        mc.player.getZ(),
                        0,
                        0
                );
                calculateThread.start();
                if (stuck != null) {
                    stuck.setEnabled(true);
                }

                if (debugValue.getCurrentValue()) {
                    ChatUtils.addChatMessage("[SelfRescue] Attempting pearl throw #" + attempted);
                }
            }
        }
    }
    @Override
    public void onEnable() {
        super.onEnable();
        Notification notification = new Notification(NotificationLevel.INFO, "This module may cause a ban.", 10000L);
        BlinkFix.getInstance().getNotificationManager().addNotification(notification);
    }

    @Override
    public void onDisable() {
        Module stuck = BlinkFix.getInstance().getModuleManager().getModule(Stuck.class);
        if (stuck != null) {
            stuck.setEnabled(false);
        }
        if (scaffoldEnabled) {
            Module scaffold = BlinkFix.getInstance().getModuleManager().getModule(Scaffold.class);
            if (scaffold != null) {
                scaffold.setEnabled(false);
            }
            scaffoldEnabled = false;
        }

        scaffoldManuallyDisabled = false;

        super.onDisable();
    }

    private int findEnderPearlSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).getItem() == Items.ENDER_PEARL) {
                return i;
            }
        }
        for (int i = 9; i < 36; i++) {
            if (mc.player.getInventory().getItem(i).getItem() == Items.ENDER_PEARL) {
                return i;
            }
        }

        return -1;
    }

    private boolean isBlockUnder() {
        return mc.level.getBlockState(mc.player.blockPosition().below()).isSolid();
    }
    private boolean isAboveVoid() {
        BlockPos playerPos = mc.player.blockPosition();
        int playerY = playerPos.getY();
        for (int y = playerY - 1; y >= mc.level.getMinBuildHeight(); y--) {
            BlockPos checkPos = new BlockPos(playerPos.getX(), y, playerPos.getZ());
            if (!(mc.level.getBlockState(checkPos).getBlock() instanceof AirBlock)) {
                return false;
            }
        }

        return true;
    }

    private Module getModule(String name) {
        try {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private void throwPearl(float yaw, float pitch) {
        if (!autoPearlValue.getCurrentValue()) {
            return;
        }
        mc.player.setYRot(yaw);
        mc.player.setXRot(pitch);
        NetworkUtils.sendPacketNoEvent(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, 0));

        if (debugValue.getCurrentValue()) {
            ChatUtils.addChatMessage("[SelfRescue] Throwing pearl at yaw: " + yaw + ", pitch: " + pitch);
        }
    }

    private class CalculateThread extends Thread {
        private int iteration;
        private boolean completed;
        private double temperature, energy, solutionE;
        private float solutionYaw, solutionPitch;
        public boolean stop;
        private final double predictX, predictY, predictZ;
        private final double minMotionY, maxMotionY;

        private CalculateThread(double predictX, double predictY, double predictZ,
                                double minMotionY, double maxMotionY) {
            this.predictX = predictX;
            this.predictY = predictY;
            this.predictZ = predictZ;
            this.minMotionY = minMotionY;
            this.maxMotionY = maxMotionY;
            this.iteration = 0;
            this.temperature = T;
            this.energy = 0;
            this.stop = false;
            this.completed = false;
        }

        @Override
        public void run() {
            timer.reset();
            solutionYaw = (float) getRandomInRange(-180, 180);
            solutionPitch = (float) getRandomInRange(-90, 90);
            float currentYaw = solutionYaw;
            float currentPitch = solutionPitch;

            try {
                energy = assessRotation(solutionYaw, solutionPitch);
            } catch (Exception e) {
                if (debugValue.getCurrentValue()) {
                    ChatUtils.addChatMessage("[SelfRescue] Please throw pearl manually");
                }
                return;
            }

            solutionE = energy;
            while (temperature >= T_MIN && !stop) {
                try {
                    float newYaw = (float) (currentYaw + getRandomInRange(-temperature * 18, temperature * 18));
                    float newPitch = (float) (currentPitch + getRandomInRange(-temperature * 9, temperature * 9));
                    newPitch = Math.max(-90, Math.min(90, newPitch));
                    double assessment = assessRotation(newYaw, newPitch);
                    double deltaE = assessment - energy;
                    if (deltaE >= 0 || random.nextDouble() < Math.exp(-deltaE / temperature * 100)) {
                        energy = assessment;
                        currentYaw = newYaw;
                        currentPitch = newPitch;

                        if (assessment > solutionE) {
                            solutionE = assessment;
                            solutionYaw = newYaw;
                            solutionPitch = newPitch;
                            if (debugValue.getCurrentValue()) {
                                ChatUtils.addChatMessage("[SelfRescue] Find a better solution: (" + solutionYaw +
                                        ", " + solutionPitch + "), value: " + solutionE);
                            }
                        }
                    }

                    temperature *= ALPHA;
                    iteration++;
                } catch (Exception e) {
                }
            }

            if (debugValue.getCurrentValue()) {
                ChatUtils.addChatMessage("[SelfRescue] Simulated annealing completed within " + iteration + " iterations");
                ChatUtils.addChatMessage("[SelfRescue] Time used: " + timer.time() + " solution energy: " + solutionE);
            }

            completed = true;
        }

        private double assessRotation(float yaw, float pitch) {
            return random.nextDouble();
        }

        public double getPredictX() {
            return predictX;
        }

        public double getMaxMotionY() {
            return maxMotionY;
        }

        public double getPredictY() {
            return predictY;
        }

        public double getPredictZ() {
            return predictZ;
        }

        public double getMinMotionY() {
            return minMotionY;
        }
    }

    private double getRandomInRange(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }
}
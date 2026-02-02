package tech.blinkfix.modules.impl.move;


import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import tech.blinkfix.BlinkFix;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.impl.*;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.utils.*;
import tech.blinkfix.utils.renderer.Fonts;
import tech.blinkfix.utils.rotation.RotationManager;
import tech.blinkfix.utils.rotation.RotationUtils;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;
import tech.blinkfix.values.impl.FloatValue;
import tech.blinkfix.values.impl.ModeValue;
import com.mojang.blaze3d.platform.InputConstants;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.apache.commons.lang3.RandomUtils;

@ModuleInfo(
        name = "Scaffold",
        description = "Automatically places blocks under you",
        category = Category.MOVEMENT
)
public class Scaffold extends Module {
    public static final List<Block> blacklistedBlocks = Arrays.asList(
            Blocks.AIR,
            Blocks.WATER,
            Blocks.LAVA,
            Blocks.ENCHANTING_TABLE,
            Blocks.GLASS_PANE,
            Blocks.GLASS_PANE,
            Blocks.IRON_BARS,
            Blocks.SNOW,
            Blocks.COAL_ORE,
            Blocks.DIAMOND_ORE,
            Blocks.EMERALD_ORE,
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.TORCH,
            Blocks.ANVIL,
            Blocks.TRAPPED_CHEST,
            Blocks.NOTE_BLOCK,
            Blocks.JUKEBOX,
            Blocks.TNT,
            Blocks.GOLD_ORE,
            Blocks.IRON_ORE,
            Blocks.LAPIS_ORE,
            Blocks.STONE_PRESSURE_PLATE,
            Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE,
            Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE,
            Blocks.STONE_BUTTON,
            Blocks.LEVER,
            Blocks.TALL_GRASS,
            Blocks.TRIPWIRE,
            Blocks.TRIPWIRE_HOOK,
            Blocks.RAIL,
            Blocks.CORNFLOWER,
            Blocks.RED_MUSHROOM,
            Blocks.BROWN_MUSHROOM,
            Blocks.VINE,
            Blocks.SUNFLOWER,
            Blocks.LADDER,
            Blocks.FURNACE,
            Blocks.SAND,
            Blocks.CACTUS,
            Blocks.DISPENSER,
            Blocks.DROPPER,
            Blocks.CRAFTING_TABLE,
            Blocks.COBWEB,
            Blocks.PUMPKIN,
            Blocks.COBBLESTONE_WALL,
            Blocks.OAK_FENCE,
            Blocks.REDSTONE_TORCH,
            Blocks.FLOWER_POT
    );
    public Vector2f correctRotation = new Vector2f();
    public Vector2f rots = new Vector2f();
    public Vector2f lastRots = new Vector2f();
    private int offGroundTicks = 0;
    public ModeValue mode = ValueBuilder.create(this, "Mode").setDefaultModeIndex(0).setModes("Normal", "Telly Bridge", "Keep Y").build().getModeValue();
    public BooleanValue eagle = ValueBuilder.create(this, "Eagle")
            .setDefaultBooleanValue(true)
            .setVisibility(() -> this.mode.isCurrentMode("Normal"))
            .build()
            .getBooleanValue();
    public BooleanValue sneak = ValueBuilder.create(this, "Sneak").setDefaultBooleanValue(true).build().getBooleanValue();
    public BooleanValue snap = ValueBuilder.create(this, "Snap")
            .setDefaultBooleanValue(true)
            .setVisibility(() -> this.mode.isCurrentMode("Normal"))
            .build()
            .getBooleanValue();
    public BooleanValue hideSnap = ValueBuilder.create(this, "Hide Snap Rotation")
            .setDefaultBooleanValue(true)
            .setVisibility(() -> this.mode.isCurrentMode("Normal") && this.snap.getCurrentValue())
            .build()
            .getBooleanValue();
    public BooleanValue Logging = ValueBuilder.create(this, "Logging").setDefaultBooleanValue(true).build().getBooleanValue();
    public BooleanValue skiptick = ValueBuilder.create(this, "Self Rescue").setDefaultBooleanValue(true).build().getBooleanValue();
    public BooleanValue renderItemSpoof = ValueBuilder.create(this, "Render Item Spoof").setDefaultBooleanValue(true).build().getBooleanValue();
    public BooleanValue bw = ValueBuilder.create(this, "No Open!").setDefaultBooleanValue(false).build().getBooleanValue();
    public BooleanValue swing = ValueBuilder.create(this, "Swing").setDefaultBooleanValue(true).build().getBooleanValue();
    public BooleanValue keepFoV = ValueBuilder.create(this, "Keep FoV").setDefaultBooleanValue(true).build().getBooleanValue();
    public BooleanValue renderBlockCounter = ValueBuilder.create(this, "Render Block Counter")
            .setDefaultBooleanValue(false)
            .build().getBooleanValue();
    FloatValue fov = ValueBuilder.create(this, "FoV")
            .setDefaultFloatValue(1.15F)
            .setMaxFloatValue(2.0F)
            .setMinFloatValue(1.0F)
            .setFloatStep(0.05F)
            .setVisibility(() -> this.keepFoV.getCurrentValue())
            .build()
            .getFloatValue();
    FloatValue speed = ValueBuilder.create(this,"Yaw Speed").setDefaultFloatValue(180F).setFloatStep(1F).setMinFloatValue(60F).setMaxFloatValue(360F).build().getFloatValue();
    int oldSlot;
    private Scaffold.BlockPosWithFacing pos;
    private int lastSneakTicks;
    public int baseY = -1;
    private float blockCounterWidth;
    private float blockCounterHeight;
    
    public static boolean isValidStack(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof BlockItem) || stack.getCount() <= 1) {
            return false;
        } else if (!InventoryUtils.isItemValid(stack)) {
            return false;
        } else {
            String string = stack.getDisplayName().getString();
            if (string.contains("Click") || string.contains("点击")) {
                return false;
            } else if (stack.getItem() instanceof ItemNameBlockItem) {
                return false;
            } else {
                Block block = ((BlockItem)stack.getItem()).getBlock();
                if (block instanceof FlowerBlock) {
                    return false;
                } else if (block instanceof BushBlock) {
                    return false;
                } else if (block instanceof FungusBlock) {
                    return false;
                } else if (block instanceof CropBlock) {
                    return false;
                } else {
                    return block instanceof SlabBlock ? false : !blacklistedBlocks.contains(block);
                }
            }
        }
    }

    public static boolean isOnBlockEdge(float sensitivity) {
        return !mc.level
                .getCollisions(mc.player, mc.player.getBoundingBox().move(0.0, -0.5, 0.0).inflate((double)(-sensitivity), 0.0, (double)(-sensitivity)))
                .iterator()
                .hasNext();
    }

    @EventTarget
    public void onFoV(EventUpdateFoV e) {
        if (this.keepFoV.getCurrentValue() && MoveUtils.isMoving()) {
            e.setFov(this.fov.getCurrentValue() + (float) PlayerUtils.getMoveSpeedEffectAmplifier() * 0.13F);
        }
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            this.oldSlot = mc.player.getInventory().selected;
            this.rots.set(mc.player.getYRot() - 180.0F, mc.player.getXRot());
            this.lastRots.set(mc.player.yRotO - 180.0F, mc.player.xRotO);
            this.pos = null;
            this.baseY = 10000;
        }
    }

    @Override
    public void onDisable() {
        boolean isHoldingJump = InputConstants.isKeyDown(mc.getWindow().getWindow(), mc.options.keyJump.getKey().getValue());
        boolean isHoldingShift = InputConstants.isKeyDown(mc.getWindow().getWindow(), mc.options.keyShift.getKey().getValue());
        mc.options.keyJump.setDown(isHoldingJump);
        mc.options.keyShift.setDown(isHoldingShift);
        mc.options.keyUse.setDown(false);
        mc.player.getInventory().selected = this.oldSlot;
    }

    @EventTarget
    public void onUpdateHeldItem(EventUpdateHeldItem e) {
        if (this.renderItemSpoof.getCurrentValue() && e.getHand() == InteractionHand.MAIN_HAND) {
            e.setItem(mc.player.getInventory().getItem(this.oldSlot));
        }
    }

    @EventTarget(1)
    public void onEventEarlyTick(EventRunTicks e) {
        this.setSuffix(mode.getCurrentMode());
        if (e.getType() == EventType.PRE && mc.screen == null && mc.player != null) {
            int slotID = -1;

            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.getInventory().getItem(i);
                if (stack.getItem() instanceof BlockItem && isValidStack(stack)) {
                    slotID = i;
                    break;
                }
            }

            if (mc.player.onGround()) {
                this.offGroundTicks = 0;
            } else {
                this.offGroundTicks++;
            }

            if (slotID != -1 && mc.player.getInventory().selected != slotID) {
                mc.player.getInventory().selected = slotID;
            }

            boolean isHoldingJump = InputConstants.isKeyDown(mc.getWindow().getWindow(), mc.options.keyJump.getKey().getValue());
            if (this.baseY == -1
                    || this.baseY > (int)Math.floor(mc.player.getY()) - 1
                    || mc.player.onGround()
                    || !PlayerUtils.movementInput()
                    || isHoldingJump
                    || this.mode.isCurrentMode("Normal")) {
                this.baseY = (int)Math.floor(mc.player.getY()) - 1;
            }

            this.getBlockPos();
            if (this.pos != null) {
                if (!bw.getCurrentValue()) {
                    this.correctRotation = this.getPlayerYawRotation();
                } else {
                    this.correctRotation = this.getPlayerYawRotation1();
                }
                if (this.mode.isCurrentMode("Normal") && this.snap.getCurrentValue()) {
                    this.rots.setX(this.correctRotation.getX());
                } else {
                    this.rots.setX(RotationUtils.rotateToYaw(180.0F, this.rots.getX(), this.correctRotation.getX()));
                }

                this.rots.setY(this.correctRotation.getY());
            }

            if (this.sneak.getCurrentValue()) {
                this.lastSneakTicks++;
                if (this.lastSneakTicks == 18) {
                    if (mc.player.isSprinting()) {
                        mc.options.keySprint.setDown(false);
                        mc.player.setSprinting(false);
                    }

                    mc.options.keyShift.setDown(true);
                } else if (this.lastSneakTicks >= 21) {
                    mc.options.keyShift.setDown(false);
                    this.lastSneakTicks = 0;
                }
            }

            if (this.mode.isCurrentMode("Telly Bridge")) {
                mc.options.keyJump.setDown(PlayerUtils.movementInput() || isHoldingJump);
                if (mc.player.onGround() && PlayerUtils.movementInput()) {
                    this.rots.setX(RotationUtils.rotateToYaw(speed.getCurrentValue(), this.rots.getX(), mc.player.getYRot()));
                    this.lastRots.set(this.rots.getX(), this.rots.getY());
                    return;
                }
            } else if (this.mode.isCurrentMode("Keep Y")) {
                mc.options.keyJump.setDown(PlayerUtils.movementInput() || isHoldingJump);
            } else {
                if (this.eagle.getCurrentValue()) {
                    mc.options.keyShift.setDown(mc.player.onGround() && isOnBlockEdge(0.3F));
                }

                if (this.snap.getCurrentValue() && !isHoldingJump) {
                    this.doSnap();
                }
            }

            this.lastRots.set(this.rots.getX(), this.rots.getY());
        }
    }

    private void doSnap() {
        boolean shouldPlaceBlock = false;
        HitResult objectPosition = RayTraceUtils.rayCast(1.0F, this.rots);
        if (objectPosition.getType() == Type.BLOCK) {
            BlockHitResult position = (BlockHitResult)objectPosition;
            if (position.getBlockPos().equals(this.pos) && position.getDirection() != Direction.UP) {
                shouldPlaceBlock = true;
            }
        }

        if (!shouldPlaceBlock) {
            this.rots.setX(mc.player.getYRot() + RandomUtils.nextFloat(0.0F, 0.5F) - 0.25F);
        }
    }
    
    private int rotateCount = 0;
    private boolean reachable = true;
    
    @EventTarget
    public void onUpdate(EventUpdate e) {

    }
    
    @EventTarget
    public void onRender(EventRender e) {
        if (this.isEnabled() && this.pos != null) {
//            renderScaffoldESP(e.getPMatrixStack());
        }
    }
    
    private int placeCount;
    
    @EventTarget
    public void onClick(EventClick e) {
        e.setCancelled(true);
        if (mc.screen == null && mc.player != null && this.pos != null && (!this.mode.isCurrentMode("Telly Bridge") || this.offGroundTicks >= 1)) {
            if (pos != null) {
                reachable = true;
                if (mc.player.getDeltaMovement().y < -0.1) {
                    FallingPlayer fallingPlayer = new FallingPlayer(mc.player);
                    fallingPlayer.calculate(2);
                    if (pos.position().getY() > fallingPlayer.y) {
                        reachable = false;
                    }
                }
                if (!this.checkPlace(this.pos)) {
                    return;
                }
                if (!reachable && rotateCount < 8) {
                    if (placeCount >= 5){
                        rotateCount= 0;
                        return;
                    }
                    if (skiptick.getCurrentValue()) {
                        BlinkFix.skipTicks = 3;
                        placeCount++;
                        mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(),mc.player.getY(),mc.player.getZ(),mc.player.getYRot(),mc.player.getXRot(),mc.player.onGround()));
                    }
                    rotateCount++;
                    this.placeBlock();
                } else {
                    placeBlock();
                    rotateCount = 0;
                    placeCount = 0;
                }
            }
        }
    }
    
//    private void renderScaffoldESP(PoseStack stack) {
//        stack.pushPose();
//        RenderSystem.disableDepthTest();
//        RenderSystem.enableBlend();
//        RenderSystem.defaultBlendFunc();
//        RenderSystem.setShader(GameRenderer::getPositionShader);
//
//        Tesselator tessellator = Tesselator.getInstance();
//        BufferBuilder bufferBuilder = tessellator.getBuilder();
//        RenderSystem.setShaderColor(0.4F, 0.8F, 1.0F, 0.6F);
//        AABB box = new AABB(
//                pos.position().getX(), pos.position().getY(), pos.position().getZ(),
//                pos.position().getX() + 1, pos.position().getY() + 1, pos.position().getZ() + 1
//        );
//        RenderUtils.装女人(bufferBuilder, stack.last().pose(), box);
//
//        RenderSystem.disableBlend();
//        RenderSystem.enableDepthTest();
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//        stack.popPose();
//    }
//
    @EventTarget
    public void onShader(EventShader e) {
        if (this.renderBlockCounter.getCurrentValue() && mc.player != null) {
            float screenWidth = (float) mc.getWindow().getGuiScaledWidth();
            float screenHeight = (float) mc.getWindow().getGuiScaledHeight();
            float x = (screenWidth - this.blockCounterWidth) / 2.0F - 3.0F;
            float y = screenHeight / 2.0F + 35.0F;
            RenderUtils.drawRoundedRect(e.getStack(), x, y, this.blockCounterWidth + 6.0F, this.blockCounterHeight + 8.0F, 5.0F, Integer.MIN_VALUE);
        }
    }
    
    @EventTarget
    public void onRender2D(EventRender2D e) {
        if (this.renderBlockCounter.getCurrentValue() && mc.player != null) {
            int blockCount = getBlockCount();
            String text = "Blocks: " + blockCount;
            double backgroundScale = 0.4;
            double textScale = 0.35;

            this.blockCounterWidth = Fonts.opensans.getWidth(text, backgroundScale);
            this.blockCounterHeight = (float) Fonts.opensans.getHeight(true, backgroundScale);

            float screenWidth = (float) mc.getWindow().getGuiScaledWidth();
            float screenHeight = (float) mc.getWindow().getGuiScaledHeight();

            float backgroundX = (screenWidth - this.blockCounterWidth) / 2.0F - 3.0F;
            float backgroundY = screenHeight / 2F + 35F;

            float textWidth = Fonts.opensans.getWidth(text, textScale);
            float textHeight = (float) Fonts.opensans.getHeight(true, textScale);

            float textX = backgroundX + (this.blockCounterWidth + 6.0F - textWidth) / 2.0F;
            float textY = backgroundY + 4.0F + (this.blockCounterHeight + 4.0F) / 2.0F - textHeight / 2.0F - 2.0F;

            e.getStack().pushPose();

            StencilUtils.write(false);
            RenderUtils.drawRoundedRect(e.getStack(), backgroundX, backgroundY, this.blockCounterWidth + 6.0F, this.blockCounterHeight + 8.0F, 5.0F, Integer.MIN_VALUE);
            StencilUtils.erase(true);
            int headerColor = new Color(150, 45, 45, 255).getRGB();
            RenderUtils.fill(e.getStack(), backgroundX, backgroundY, backgroundX + this.blockCounterWidth + 6.0F, backgroundY + 3.0F, headerColor);

            int bodyColor = new Color(0, 0, 0, 120).getRGB();
            RenderUtils.fill(e.getStack(), backgroundX, backgroundY + 3.0F, backgroundX + this.blockCounterWidth + 6.0F, backgroundY + this.blockCounterHeight + 8.0F, bodyColor);

            Fonts.opensans.render(e.getStack(), text, textX, textY, Color.WHITE, true, textScale);
            StencilUtils.dispose();
            e.getStack().popPose();
        }
    }

    public int getBlockCount() {
        if (mc.player == null) return 0;

        int totalBlocks = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() instanceof BlockItem) {
                totalBlocks += stack.getCount();
            }
        }

        return totalBlocks;
    }

    private boolean checkPlace(Scaffold.BlockPosWithFacing data) {
        Vec3 center = new Vec3((double)data.position.getX() + 0.5, (double)((float)data.position.getY() + 0.5F), (double)data.position.getZ() + 0.5);
        Vec3 hit = center.add(
                new Vec3((double)data.facing.getNormal().getX() * 0.5, (double)data.facing.getNormal().getY() * 0.5, (double)data.facing.getNormal().getZ() * 0.5)
        );
        Vec3 relevant = hit.subtract(mc.player.getEyePosition());
        return relevant.lengthSqr() <= 20.25 && relevant.normalize().dot(Vec3.atLowerCornerOf(data.facing.getNormal().multiply(-1)).normalize()) >= 0.0;
    }

    private void placeBlock() {
        if (!bw.getCurrentValue()) {
            if (this.pos != null && isValidStack(mc.player.getMainHandItem())) {
                Direction sbFace = this.pos.facing();
                boolean isHoldingJump = InputConstants.isKeyDown(mc.getWindow().getWindow(), mc.options.keyJump.getKey().getValue());
                if (sbFace != null
                        && (sbFace != Direction.UP || mc.player.onGround() || !PlayerUtils.movementInput() || isHoldingJump || this.mode.isCurrentMode("Normal"))
                        && this.shouldBuild()) {
                    InteractionResult result = mc.gameMode
                            .useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(getVec3(this.pos.position(), sbFace), sbFace, this.pos.position(), false));
                    if (result == InteractionResult.SUCCESS) {
                        if (swing.getCurrentValue()) {
                            mc.player.swing(InteractionHand.MAIN_HAND);
                        } else {
                            mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                        }
                        this.pos = null;
                    }
                }
            }
        } else {
            if (this.pos != null) {
                HitResult objectPosition = RayTraceUtils.rayCast(1, new Vector2f(RotationManager.rotations.x, RotationManager.rotations.y));
                if (objectPosition != null && objectPosition.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHitResult = (BlockHitResult) objectPosition;
                    Direction sideHit = blockHitResult.getDirection();

                    if (sideHit != Direction.UP || mc.player.onGround() || !PlayerUtils.movementInput() || this.mode.isCurrentMode("Normal") ||
                            ((this.mode.isCurrentMode("Telly Bridge")) && InputConstants.isKeyDown(mc.getWindow().getWindow(), mc.options.keyJump.getKey().getValue()))) {

                        if (blockHitResult.getBlockPos().equals(this.pos.position()) || (this.isNearbyBlockPos(blockHitResult.getBlockPos()))) {
                            if (mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, blockHitResult) == InteractionResult.SUCCESS) {
                                placeCount++;
                                if (swing.getCurrentValue()) {
                                    mc.player.swing(InteractionHand.MAIN_HAND);
                                } else {
                                    mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                                }
                            }
                        } else if (this.isNearbyBlockPos(blockHitResult.getBlockPos()) && sideHit != Direction.UP) {
                            if (mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, blockHitResult) == InteractionResult.SUCCESS) {
                                if (swing.getCurrentValue()) {
                                    mc.player.swing(InteractionHand.MAIN_HAND);
                                } else {
                                    mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private boolean isNearbyBlockPos(BlockPos blockPos) {
        if (!mc.player.onGround()) {
            return blockPos.equals(this.pos.position());
        } else {
            for (int x = this.pos.position().getX() - 1; x <= this.pos.position().getX() + 1; ++x) {
                for (int z = this.pos.position().getZ() - 1; z <= this.pos.position().getZ() + 1; ++z) {
                    if (blockPos.equals(new BlockPos(x, this.pos.position().getY(), z))) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
    
    private Vector2f getPlayerYawRotation() {
        return mc.player != null && this.pos != null
                ? new Vector2f(RotationUtils.getRotations(this.pos.position(), 0.0F).getYaw(), RotationUtils.getRotations(this.pos.position(), 0.0F).getPitch())
                : new Vector2f(0.0F, 0.0F);
    }

    private boolean shouldBuild() {
        BlockPos playerPos = BlockPos.containing(mc.player.getX(), mc.player.getY() - 0.5, mc.player.getZ());
        return mc.level.isEmptyBlock(playerPos) && isValidStack(mc.player.getMainHandItem());
    }
    
    private boolean isTower() {
        return mc.options.keyJump.isDown() &&
                !mc.options.keyUp.isDown() &&
                !mc.options.keyDown.isDown() &&
                !mc.options.keyLeft.isDown() &&
                !mc.options.keyRight.isDown();
    }

    private HitResult performRayCast(Vector2f rotation) {
        return RayTraceUtils.rayCast(1.0F, rotation);
    }

    private boolean isHitValid(HitResult hit) {
        return hit.getType() == HitResult.Type.BLOCK &&
                isValidBlock(((BlockHitResult)hit).getBlockPos()) &&
                this.isNearbyBlockPos(((BlockHitResult)hit).getBlockPos()) &&
                ((BlockHitResult)hit).getDirection() != Direction.DOWN &&
                ((BlockHitResult)hit).getDirection() != Direction.UP;
    }

    private ArrayList<Float> findValidPitches(float yaw) {
        ArrayList<Float> validPitches = new ArrayList<>();
        for (float i = Math.max(this.rots.getY() - 30.0F, -90.0F); i < Math.min(this.rots.getY() + 20.0F, 90F); i += 0.3F) {
            Vector2f f = RotationUtils.getFixedRotation(yaw, i, this.rots.getX(), this.rots.getY());
            HitResult position = performRayCast(new Vector2f(yaw, f.getY()));
            if (isHitValid(position)) {
                validPitches.add(f.getY());
            }
        }
        return validPitches;
    }

    private Vector2f findOptimalRotation(float yaw) {
        for (float yawLoops = 0; yawLoops < 180; yawLoops += 2) {
            float currentPitch = this.rots.getY();

            for (float pitchLoops = 0; pitchLoops < 25; pitchLoops += 2) {
                for (int i = 0; i < 2; i++) {
                    float pitch = currentPitch - (pitchLoops * (i == 0 ? 1 : -1));

                    float[][] offsets = {
                            {yaw + yawLoops, pitch},
                            {yaw - yawLoops, pitch}
                    };

                    for (float[] rotation : offsets) {
                        float rayCastPitch = Mth.clamp(rotation[1], -90, 90);
                        Vector2f fixedRotation = RotationUtils.getFixedRotation(rotation[0], rayCastPitch, this.rots.getX(), this.rots.getY());
                        HitResult position = performRayCast(fixedRotation);

                        if (isHitValid(position)) {
                            return fixedRotation;
                        }
                    }
                }
            }
        }
        // Default return if no valid rotation found
        return new Vector2f(yaw, this.rots.getY());
    }
    
    private int ticks;
    
    public boolean isValidBlock(final BlockPos blockPos) {
        BlockState blockState = mc.level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        return !(block instanceof LiquidBlock) &&
                !(block instanceof AirBlock) &&
                !(block instanceof ChestBlock) &&
                !(block instanceof FurnaceBlock) &&
                !(block instanceof EnderChestBlock) &&
                !(block instanceof TallGrassBlock) &&
                !(block instanceof SnowLayerBlock);
    }
    
    private Vector2f getPlayerYawRotation1() {
        float yaw, pitch;
        float rotationYaw = mc.player.getYRot() - 180.0F;

        if (this.isTower()) {
            HitResult objectPosition = mc.hitResult;
            if (objectPosition != null && objectPosition.getType() == HitResult.Type.BLOCK) {
                yaw = rotationYaw;
                pitch = 90;
                return new Vector2f(yaw, pitch);
            }
        }

        yaw = rotationYaw;
        pitch = 82F;
        Vector2f rotations = new Vector2f(yaw, pitch);

        float realYaw = mc.player.getYRot();

        float[] dynamicOffsets = new float[]{-0.1f, -0.07f, 0.07f, 0.1f};
        float magic = dynamicOffsets[ticks++ % dynamicOffsets.length];

        realYaw += magic;

        if (mc.options.keyDown.isDown()) {
            realYaw += 180.0F;
            if (mc.options.keyLeft.isDown()) {
                realYaw += 45.0F;
            } else if (mc.options.keyRight.isDown()) {
                realYaw -= 45.0F;
            }
        } else if (mc.options.keyUp.isDown()) {
            if (mc.options.keyLeft.isDown()) {
                realYaw -= 45.0F;
            } else if (mc.options.keyRight.isDown()) {
                realYaw += 45.0F;
            }
        } else if (mc.options.keyRight.isDown()) {
            realYaw += 90.0F;
        } else if (mc.options.keyLeft.isDown()) {
            realYaw -= 90.0F;
        }

        yaw = realYaw - 180.0F;
        rotations.setX(yaw);

        if (this.shouldBuild()) {
            Vector2f initialRotation = new Vector2f(rotations.getX(), rotations.getY());
            HitResult initialHit = performRayCast(initialRotation);
            if (isHitValid(initialHit)) {
                return initialRotation;
            }

            ArrayList<Float> validPitches = findValidPitches(yaw);
            if (!validPitches.isEmpty()) {
                validPitches.sort(Comparator.comparingDouble(this::distanceToLastPitch));
                rotations.setY(validPitches.get(0));
                return rotations;
            } else {
                return findOptimalRotation(yaw);
            }
        }

        return rotations;
    }
    
    private double distanceToLastPitch(float pitch) {
        return Math.abs(pitch - this.rots.getY());
    }
    
    private void getBlockPos() {
        Vec3 baseVec = mc.player.getEyePosition().add(mc.player.getDeltaMovement().multiply(2.0, 2.0, 2.0));
        if (mc.player.getDeltaMovement().y < 0.01) {
            FallingPlayer fallingPlayer = new FallingPlayer(mc.player);
            fallingPlayer.calculate(2);
            baseVec = new Vec3(baseVec.x, Math.max(fallingPlayer.y + (double)mc.player.getEyeHeight(), baseVec.y), baseVec.z);
        }

        BlockPos base = BlockPos.containing(baseVec.x, (double)((float)this.baseY + 0.1F), baseVec.z);
        int baseX = base.getX();
        int baseZ = base.getZ();
        if (!mc.level.getBlockState(base).entityCanStandOn(mc.level, base, mc.player)) {
            if (!this.checkBlock(baseVec, base)) {
                for (int d = 1; d <= 6; d++) {
                    if (this.checkBlock(baseVec, new BlockPos(baseX, this.baseY - d, baseZ))) {
                        return;
                    }

                    for (int x = 1; x <= d; x++) {
                        for (int z = 0; z <= d - x; z++) {
                            int y = d - x - z;

                            for (int rev1 = 0; rev1 <= 1; rev1++) {
                                for (int rev2 = 0; rev2 <= 1; rev2++) {
                                    if (this.checkBlock(baseVec, new BlockPos(baseX + (rev1 == 0 ? x : -x), this.baseY - y, baseZ + (rev2 == 0 ? z : -z)))) {
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean checkBlock(Vec3 baseVec, BlockPos bp) {
        if (!(mc.level.getBlockState(bp).getBlock() instanceof AirBlock)) {
            return false;
        } else {
            Vec3 center = new Vec3((double)bp.getX() + 0.5, (double)((float)bp.getY() + 0.5F), (double)bp.getZ() + 0.5);

            for (Direction sbface : Direction.values()) {
                Vec3 hit = center.add(
                        new Vec3((double)sbface.getNormal().getX() * 0.5, (double)sbface.getNormal().getY() * 0.5, (double)sbface.getNormal().getZ() * 0.5)
                );
                Vec3i baseBlock = bp.offset(sbface.getNormal());
                BlockPos po = new BlockPos(baseBlock.getX(), baseBlock.getY(), baseBlock.getZ());
                if (mc.level.getBlockState(po).entityCanStandOnFace(mc.level, po, mc.player, sbface)) {
                    Vec3 relevant = hit.subtract(baseVec);
                    if (relevant.lengthSqr() <= 20.25 && relevant.normalize().dot(Vec3.atLowerCornerOf(sbface.getNormal()).normalize()) >= 0.0) {
                        this.pos = new Scaffold.BlockPosWithFacing(new BlockPos(baseBlock), sbface.getOpposite());
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public static Vec3 getVec3(BlockPos pos, Direction face) {
        double x = (double)pos.getX() + 0.5;
        double y = (double)pos.getY() + 0.5;
        double z = (double)pos.getZ() + 0.5;
        if (face != Direction.UP && face != Direction.DOWN) {
            y += 0.08;
        } else {
            x += MathUtils.getRandomDoubleInRange(0.3, -0.3);
            z += MathUtils.getRandomDoubleInRange(0.3, -0.3);
        }

        if (face == Direction.WEST || face == Direction.EAST) {
            z += MathUtils.getRandomDoubleInRange(0.3, -0.3);
        }

        if (face == Direction.SOUTH || face == Direction.NORTH) {
            x += MathUtils.getRandomDoubleInRange(0.3, -0.3);
        }

        return new Vec3(x, y, z);
    }

    public static record BlockPosWithFacing(BlockPos position, Direction facing) {
    }
}

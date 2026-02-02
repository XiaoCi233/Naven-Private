package tech.blinkfix.utils;

import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;

/**
 * Utility class for module names.
 * Provides constants for all module names and helper methods to get module names.
 */
public class ModuleNameUtils {

    // Combat Modules
    public static final String AIM_ASSIST = "AimAssist";
    public static final String ANTI_BOT = "AntiBot";
    public static final String ATTACK_CRYSTAL = "AttackCrystal";
    public static final String KILL_AURA = "KillAura";
    public static final String AUTO_CLICKER = "AutoClicker";
    public static final String AUTO_HEAL = "AutoHeal";
    public static final String AUTO_ROD = "AutoRod";
    public static final String FAKE_LAG = "FakeLag";
    public static final String VELOCITY = "Velocity";
    public static final String BACK_TRACK = "BackTrack";
    public static final String ANTI_KB = "AntiKB";
    public static final String DELAY_TRACK = "DelayTrack";
    public static final String THROWABLE_AURA = "ThrowableAura";
    public static final String CRITICAL = "Critical";
    public static final String MORE_KNOCK_BACK = "MoreKnockBack";
    public static final String AURA = "Aura";

    // Movement Modules
    public static final String ANTI_FIREBALL = "AntiFireball";
    public static final String SCAFFOLD = "Scaffold";
    public static final String AUTO_MLG = "AutoMLG";
    public static final String NO_JUMP_DELAY = "NoJumpDelay";
    public static final String SAFE_WALK = "SafeWalk";
    public static final String BLINK = "Blink";
    public static final String FAST_WEB = "FastWeb";
    public static final String SPRINT = "Sprint";
    public static final String TARGET_STRAFE = "TargetStrafe";
    public static final String INVENTORY_MOVE = "InventoryMove";
    public static final String NO_SLOW = "NoSlow";
    public static final String LONG_JUMP = "LongJump";
    public static final String NO_FALL = "NoFall";
    public static final String SPEED = "Speed";
    public static final String STUCK = "Stuck";
    public static final String FLY = "Fly";

    // Misc Modules
    public static final String CLIENT_FRIEND = "ClientFriend";
    public static final String AUTO_REPORT = "AutoReport";
    public static final String CONTAINER_STEALER = "ContainerStealer";
    public static final String INVENTORY_CLEANER = "InventoryCleaner";
    public static final String TEAMS = "Teams";
    public static final String TNT_WARNING = "TNTWarning";
    public static final String MID_PEARL = "MidPearl";
    public static final String AUTO_PLAY = "AutoPlay";
    public static final String FAST_PLACE = "FastPlace";
    public static final String BED_AURA = "BedAura";
    public static final String KILLER_DETECTION = "KillerDetection";
    public static final String SELF_RESCUE = "SelfRescue";
    public static final String HELPER = "Helper";
    public static final String AUTO_TOOLS = "AutoTools";
    public static final String DISABLER = "Disabler";
    public static final String GHOST_HAND = "GhostHand";
    public static final String ITEM_TRACKER = "ItemTracker";
    public static final String SILENT_DISCONNECT = "SilentDisconnect";
    public static final String ANTI_WEB = "AntiWeb";
    public static final String SPAMMER = "Spammer";
    public static final String KILL_SAY = "KillSay";
    public static final String ZHAGN_TIE_NAN_CHEST_STEALER = "ZhagnTieNanChestStealer";

    // Render Modules
    public static final String ANIMATIONS = "Animations";
    public static final String ISLAND = "Island";
    public static final String PEARL_PREDICTION = "PearlPrediction";
    public static final String PROJECTILE = "Projectile";
    public static final String TIME_CHANGER = "TimeChanger";
    public static final String FULL_BRIGHT = "FullBright";
    public static final String NAME_PROTECT = "NameProtect";
    public static final String NO_HURT_CAM = "NoHurtCam";
    public static final String ANTI_BLINDNESS = "AntiBlindness";
    public static final String ANTI_NAUSEA = "AntiNausea";
    public static final String SCOREBOARD = "Scoreboard";
    public static final String COMPASS = "Compass";
    public static final String POST_PROCESS = "PostProcess";
    public static final String MOTION_BLUR = "MotionBlur";
    public static final String EFFECT_DISPLAY = "EffectDisplay";
    public static final String NO_RENDER = "NoRender";
    public static final String ITEM_TAGS = "ItemTags";
    public static final String SCOREBOARD_SPOOF = "ScoreboardSpoof";
    public static final String NAME_TAGS = "NameTags";
    public static final String CHEST_ESP = "ChestESP";
    public static final String CLICK_GUI = "ClickGUI";
    public static final String GLOW = "Glow";
    public static final String ITEM_PHYSICS = "ItemPhysics";
    public static final String BED_PLATES = "BedPlates";
    public static final String HUD = "HUD";
    public static final String CAMERA = "Camera";
    public static final String LOW_FIRE = "LowFire";
    public static final String HIT_COLOR = "HitColor";
    public static final String VIEW_CLIP = "ViewClip";
    public static final String KILL_EFFECT = "KillEffect";

    /**
     * Gets the module name from the ModuleInfo annotation of the given module class.
     * 
     * @param moduleClass The module class
     * @return The module name from @ModuleInfo annotation, or null if not found
     */
    public static String getModuleName(Class<? extends Module> moduleClass) {
        if (moduleClass.isAnnotationPresent(ModuleInfo.class)) {
            ModuleInfo moduleInfo = moduleClass.getAnnotation(ModuleInfo.class);
            return moduleInfo.name();
        }
        return null;
    }

    /**
     * Gets the module name from a Module instance.
     * 
     * @param module The module instance
     * @return The module name from @ModuleInfo annotation, or null if not found
     */
    public static String getModuleName(Module module) {
        return getModuleName(module.getClass());
    }

    /**
     * Gets the module name from a Module instance, falling back to the module's getName() method.
     * 
     * @param module The module instance
     * @return The module name
     */
    public static String getModuleNameSafe(Module module) {
        String name = getModuleName(module);
        if (name != null) {
            return name;
        }
        return module.getName();
    }

    /**
     * Checks if the given module class matches the expected module name.
     * 
     * @param moduleClass The module class
     * @param expectedName The expected module name
     * @return true if the module name matches, false otherwise
     */
    public static boolean matchesModuleName(Class<? extends Module> moduleClass, String expectedName) {
        String actualName = getModuleName(moduleClass);
        return actualName != null && actualName.equals(expectedName);
    }

    /**
     * Gets all module name constants as an array.
     * 
     * @return Array of all module name constants
     */
    public static String[] getAllModuleNames() {
        return new String[] {
            // Combat
            AIM_ASSIST, ANTI_BOT, ATTACK_CRYSTAL, KILL_AURA, AUTO_CLICKER, AUTO_HEAL, AUTO_ROD,
            FAKE_LAG, VELOCITY, BACK_TRACK, ANTI_KB, DELAY_TRACK, THROWABLE_AURA, CRITICAL,
            MORE_KNOCK_BACK, AURA,
            // Movement
            ANTI_FIREBALL, SCAFFOLD, AUTO_MLG, NO_JUMP_DELAY, SAFE_WALK, BLINK, FAST_WEB,
            SPRINT, TARGET_STRAFE, INVENTORY_MOVE, NO_SLOW, LONG_JUMP, NO_FALL, SPEED, STUCK, FLY,
            // Misc
            CLIENT_FRIEND, AUTO_REPORT, CONTAINER_STEALER, INVENTORY_CLEANER, TEAMS, TNT_WARNING,
            MID_PEARL, AUTO_PLAY, FAST_PLACE, BED_AURA, KILLER_DETECTION, SELF_RESCUE, HELPER,
            AUTO_TOOLS, DISABLER, GHOST_HAND, ITEM_TRACKER, SILENT_DISCONNECT, ANTI_WEB,
            SPAMMER, KILL_SAY, ZHAGN_TIE_NAN_CHEST_STEALER,
            // Render
            ANIMATIONS, ISLAND, PEARL_PREDICTION, PROJECTILE, TIME_CHANGER, FULL_BRIGHT,
            NAME_PROTECT, NO_HURT_CAM, ANTI_BLINDNESS, ANTI_NAUSEA, SCOREBOARD, COMPASS,
            POST_PROCESS, MOTION_BLUR, EFFECT_DISPLAY, NO_RENDER, ITEM_TAGS, SCOREBOARD_SPOOF,
            NAME_TAGS, CHEST_ESP, CLICK_GUI, GLOW, ITEM_PHYSICS, BED_PLATES, HUD, CAMERA,
            LOW_FIRE, HIT_COLOR, VIEW_CLIP, KILL_EFFECT
        };
    }
}


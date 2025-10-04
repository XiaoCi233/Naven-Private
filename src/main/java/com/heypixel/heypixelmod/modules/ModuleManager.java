package com.heypixel.heypixelmod.modules;

import com.heypixel.heypixelmod.BlinkFix;
import com.heypixel.heypixelmod.events.api.EventTarget;
import com.heypixel.heypixelmod.events.impl.EventKey;
import com.heypixel.heypixelmod.events.impl.EventMouseClick;
import com.heypixel.heypixelmod.exceptions.NoSuchModuleException;
import com.heypixel.heypixelmod.modules.impl.combat.*;
import com.heypixel.heypixelmod.modules.impl.misc.*;
import com.heypixel.heypixelmod.modules.impl.move.*;
import com.heypixel.heypixelmod.modules.impl.render.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModuleManager {
    private static final Logger log = LogManager.getLogger(ModuleManager.class);
    private final List<Module> modules = new ArrayList<>();
    private final Map<Class<? extends Module>, Module> classMap = new HashMap<>();
    private final Map<String, Module> nameMap = new HashMap<>();

    public ModuleManager() {
        try {
            this.initModules();
            this.modules.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        } catch (Exception var2) {
            log.error("Failed to initialize modules", var2);
            throw new RuntimeException(var2);
        }

        BlinkFix.getInstance().getEventManager().register(this);
    }

    private void initModules() {
        this.registerModule(
//                new AntiKB_Debug(),
//                new JumpReset(),
                new AntiWeb(),
                new BackTrack(),
                new Aura(),
                new HUD(),
                new Camera(),
                new DelayTrack(),
//                new Test(),
//                new KillEffect(),
                new NameTags(),
                new ContainerStealer(),
                new InventoryCleaner(),
                new Scaffold(),
                new ItemPhysics(),
                new AntiBots(),
                new Sprint(),
                new TargetStrafe(),
                new ChestESP(),
                new ClickGUIModule(),
                new Teams(),
                new Glow(),
                new ItemTracker(),
                new AutoMLG(),
                new NoJumpDelay(),
                new FastPlace(),
                new AntiFireball(),
                new Fly(),
//                new Clip(),
                new Stuck(),
                new ScoreboardSpoof(),
                new AutoTools(),
//                new ViewClip(),
                new Disabler(),
//                new Debug(),
                new Projectile(),
                new TimeChanger(),
                new FullBright(),
                new NameProtect(),
                new NoHurtCam(),
                new AutoClicker(),
                new AntiBlindness(),
                new AntiNausea(),
                new Scoreboard(),
                new Compass(),
                new Spammer(),
                new KillSay(),
                new Blink(),
                new FastWeb(),
                new PostProcess(),
                new AttackCrystal(),
                new EffectDisplay(),
                new NoRender(),
                new ItemTags(),
                new SafeWalk(),
                new AimAssist(),
                new MotionBlur(),
//                new BlueArchive(),
                new Language(),
                new InventoryMove(),
                new BedAura(),
                new KillerDetection(),
                new Widget(),
                new SelfRescue(),
//                new TargetHUD(),
                new Helper(),
                new LowFire(),
                new BedPlates(),
//                new AutoSoup(),
                new TNTWarning(),
                new ZhagnTieNanChestStealer(),
                new NoSlow(),
                new LongJump(),
//                new SafeMode(),
                new Critical(),
                new AutoHeal(),
                new MoreKnockBack(),
//                new Protocol(),
//                new GhostHand(),
                new Speed(),
//                new StrafeFix(),
                new MidPearl(),
                new AutoWeapon(),
                new ThrowableAura(),
//                new ProtocolModule(),
                new Velocity(),
                new FakeLag(),
                new AutoPlay(),
                new Animations(),
                new AutoRod(),
                new NoFall(),
                new Island()

        );
    }

    private void registerModule(Module... modules) {
        for (Module module : modules) {
            this.registerModule(module);
        }
    }

    private void registerModule(Module module) {
        module.initModule();
        this.modules.add(module);
        this.classMap.put((Class<? extends Module>)module.getClass(), module);
        this.nameMap.put(module.getName().toLowerCase(), module);
    }

    public List<Module> getModulesByCategory(Category category) {
        List<Module> modules = new ArrayList<>();

        for (Module module : this.modules) {
            if (module.getCategory() == category) {
                modules.add(module);
            }
        }

        return modules;
    }

    public Module getModule(Class<? extends Module> clazz) {
        Module module = this.classMap.get(clazz);
        if (module == null) {
            throw new NoSuchModuleException();
        } else {
            return module;
        }
    }

    public Module getModule(String name) {
        Module module = this.nameMap.get(name.toLowerCase());
        if (module == null) {
            throw new NoSuchModuleException();
        } else {
            return module;
        }
    }

    @EventTarget
    public void onKey(EventKey e) {
        if (e.isState() && Minecraft.getInstance().screen == null) {
            for (Module module : this.modules) {
                if (module.getKey() == e.getKey()) {
                    module.toggle();
                }
            }
        }
    }

    @EventTarget
    public void onKey(EventMouseClick e) {
        if (!e.isState() && (e.getKey() == 3 || e.getKey() == 4)) {
            for (Module module : this.modules) {
                if (module.getKey() == -e.getKey()) {
                    module.toggle();
                }
            }
        }
    }

    public List<Module> getModules() {
        return this.modules;
    }
}

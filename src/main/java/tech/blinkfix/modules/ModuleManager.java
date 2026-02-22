package tech.blinkfix.modules;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.impl.EventKey;
import tech.blinkfix.events.impl.EventMouseClick;
import tech.blinkfix.exceptions.NoSuchModuleException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.blinkfix.modules.impl.combat.*;
import tech.blinkfix.modules.impl.misc.*;
import tech.blinkfix.modules.impl.move.*;
import tech.blinkfix.modules.impl.render.*;

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

 //Combat
                new AimAssist(),
                new AntiBots(),
                new AttackCrystal(),
                new Aura(),
                new AutoClicker(),
                new AutoHeal(),
                new AutoRod(),
                new FakeLag(),
                new Velocity(),
//                new BackTrack(),
                new AntiKB(),
//                new Critical(),
                new DelayTrack(),
//                new MoreKnockBack(),
                new ThrowableAura(),
//                new AutoWeapon(),

//Movement
                new AntiFireball(),
                new Scaffold(),
                new AutoMLG(),
                new NoJumpDelay(),
                new SafeWalk(),
                new Blink(),
                new FastWeb(),
                new Sprint(),
                new TargetStrafe(),
                new InventoryMove(),
                new NoSlow(),
                new LongJump(),
                new NoFall(),
                new Speed(),
                new Stuck(),
              //new StrafeFix(),
              //new AntiKB_Debug(),
              //new JumpReset(),
                  new Fly(),

//Misc
                new ClientFriend(),
                new AutoReport(),
//                new AntiWeb(),
//                new SilentDisconnect(),
                new ContainerStealer(),
                new InventoryCleaner(),
                new Teams(),
                new TNTWarning(),
                // new ZhagnTieNanChestStealer(),
                new MidPearl(),
                new AutoPlay(),
                new FastPlace(),
                new BedAura(),
                new KillerDetection(),
//                new Widget(),
                new SelfRescue(),
                new Helper(),
                new AutoTools(),
                new Disabler(),
              //new Protocol(),
              //new ProtocolModule(),
              new GhostHand(),
              //new AutoSoup(),
              //new Debug(),


                new Animations(),
                new Island(),
                new PearlPrediction(),
                new Projectile(),
                new TimeChanger(),
                new FullBright(),
                new NameProtect(),
                new NoHurtCam(),
                new AntiBlindness(),
                new AntiNausea(),
                new Scoreboard(),
                new Compass(),
                new Spammer(),
                new KillSay(),
                new PostProcess(),
                new MotionBlur(),
                new EffectDisplay(),
                new NoRender(),
                new ItemTags(),
                new ScoreboardSpoof(),
                new NameTags(),
                new ChestESP(),
                new ClickGUIModule(),
                new Glow(),
                new ItemPhysics(),
                new ItemTracker(),
                new BedPlates(),
                new HUD(),
                new WaterMark2(),
                new Camera(),
              // new Language(),
                new LowFire(),
                new HitColor()
              //new Clip(),
              //new ViewClip(),
              //new KillEffect(),
              //new TargetHUD(),

              //new BlueArchive(),
              //new SafeMode(),
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

package tech.blinkfix;

import tech.blinkfix.commands.CommandManager;
import tech.blinkfix.events.api.EventManager;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.impl.EventRunTicks;
import tech.blinkfix.events.impl.EventShutdown;
import tech.blinkfix.files.FileManager;
import tech.blinkfix.modules.ModuleManager;
import tech.blinkfix.modules.impl.render.ClickGUIModule;
import tech.blinkfix.ui.Island.CommandPaletteContent;
import tech.blinkfix.ui.notification.NotificationManager;
import tech.blinkfix.ui.CooldownBar.CooldownBarManager;
import tech.blinkfix.utils.EntityWatcher;
import tech.blinkfix.utils.EventWrapper;
import tech.blinkfix.utils.LogUtils;
import tech.blinkfix.utils.NetworkUtils;
import tech.blinkfix.utils.ServerUtils;
import tech.blinkfix.utils.TickTimeHelper;
import tech.blinkfix.utils.renderer.Fonts;
import tech.blinkfix.utils.renderer.PostProcessRenderer;
import tech.blinkfix.utils.renderer.Shaders;
import tech.blinkfix.utils.rotation.RotationManager;
import tech.blinkfix.values.HasValueManager;
import tech.blinkfix.values.ValueManager;
import tech.blinkfix.events.impl.EventPacket;
import tech.blinkfix.modules.impl.misc.AutoPlay;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import java.awt.FontFormatException;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import dev.yalan.live.LiveClient;
import net.minecraftforge.common.MinecraftForge;

public class BlinkFix {
    public static final String CLIENT_NAME = "Stray-NextGeneration";
    public static final String CLIENT_DISPLAY_NAME = "Best";
    public static final String CLIENT_VERSION = "Alpha-E";
    private static BlinkFix instance;
    private final EventManager eventManager;
    private final EventWrapper eventWrapper;
    private final ValueManager valueManager;
    private final HasValueManager hasValueManager;
    private final RotationManager rotationManager;
    public final ModuleManager moduleManager;
    private final CommandManager commandManager;
    private final FileManager fileManager;
    private final NotificationManager notificationManager;
    private final CooldownBarManager cooldownBarManager;
    public static float TICK_TIMER = 1.0F;
    public static Queue<Runnable> skipTasks = new ConcurrentLinkedQueue<>();
    public static int skipTicks = 0;

    private BlinkFix() {
        System.out.println("BlinkFix Init");
        instance = this;
        this.eventManager = new EventManager();
        Shaders.init();
        PostProcessRenderer.init();

        try {
            Fonts.loadFonts();
        } catch (IOException var2) {
            throw new RuntimeException(var2);
        } catch (FontFormatException var3) {
            throw new RuntimeException(var3);
        }

        this.eventWrapper = new EventWrapper();
        this.valueManager = new ValueManager();
        this.hasValueManager = new HasValueManager();
        this.moduleManager = new ModuleManager();
        this.rotationManager = new RotationManager();
        this.commandManager = new CommandManager();
        this.fileManager = new FileManager();
        this.notificationManager = new NotificationManager();
        this.cooldownBarManager = new CooldownBarManager();
        this.fileManager.load();
        this.moduleManager.getModule(ClickGUIModule.class).setEnabled(false);
        this.eventManager.register(getInstance());
        this.eventManager.register(this.eventWrapper);
        this.eventManager.register(new RotationManager());
        this.eventManager.register(new NetworkUtils());
        this.eventManager.register(new ServerUtils());
        this.eventManager.register(new EntityWatcher());
        this.eventManager.register(this.cooldownBarManager);
        MinecraftForge.EVENT_BUS.register(this.eventWrapper);

        try {
            CommandPaletteContent commandPalette = CommandPaletteContent.getInstance();
            if (commandPalette != null) {
                commandPalette.registerEvents();
            } else {
                CommandPaletteContent newPalette = new CommandPaletteContent();
                newPalette.registerEvents();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void modRegister() {
        try {
            new BlinkFix();
        } catch (Exception var1) {
            System.err.println("Failed to load client");
            var1.printStackTrace(System.err);
        }
    }
    @EventTarget
    public void onPacket(EventPacket event) {
        if (event.getType() == EventType.RECEIVE) {
            Packet<?> packet = event.getPacket();
//
//            if (packet instanceof ClientboundSetTitleTextPacket) {
//                AutoPlay autoPlayModule = (AutoPlay) this.moduleManager.getModule(AutoPlay.class);
//                if (autoPlayModule != null && autoPlayModule.isEnabled()) {
//                    autoPlayModule.onTitlePacket((ClientboundSetTitleTextPacket) packet);
//                }
//            } else if (packet instanceof ClientboundSetSubtitleTextPacket) {
//                AutoPlay autoPlayModule = (AutoPlay) this.moduleManager.getModule(AutoPlay.class);
//                if (autoPlayModule != null && autoPlayModule.isEnabled()) {
//                    autoPlayModule.onSubtitlePacket((ClientboundSetSubtitleTextPacket) packet);
//                }
//            } else
                if (packet instanceof ClientboundSystemChatPacket) {
                AutoPlay autoPlayModule = (AutoPlay) this.moduleManager.getModule(AutoPlay.class);
                if (autoPlayModule != null && autoPlayModule.isEnabled()) {
                    autoPlayModule.onSystemChatPacket((ClientboundSystemChatPacket) packet);
                }
            }
        }
    }

    @EventTarget
    public void onShutdown(EventShutdown e) {
        LiveClient.INSTANCE.shutdown();
        this.fileManager.save();
        LogUtils.close();
    }

    @EventTarget(0)
    public void onEarlyTick(EventRunTicks e) {
        if (e.getType() == EventType.PRE) {
            TickTimeHelper.update();
        }
    }

    public static BlinkFix getInstance() {
        return instance;
    }

    public EventManager getEventManager() {
        return this.eventManager;
    }

    public EventWrapper getEventWrapper() {
        return this.eventWrapper;
    }

    public ValueManager getValueManager() {
        return this.valueManager;
    }

    public HasValueManager getHasValueManager() {
        return this.hasValueManager;
    }

    public RotationManager getRotationManager() {
        return this.rotationManager;
    }

    public ModuleManager getModuleManager() {
        return this.moduleManager;
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    public FileManager getFileManager() {
        return this.fileManager;
    }

    public NotificationManager getNotificationManager() {
        return this.notificationManager;
    }

    public CooldownBarManager getCooldownBarManager() {
        return this.cooldownBarManager;
    }
}

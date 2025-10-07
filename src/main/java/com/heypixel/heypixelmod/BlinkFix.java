package com.heypixel.heypixelmod;

import ca.weblite.objc.Client;
import com.heypixel.heypixelmod.commands.CommandManager;
import com.heypixel.heypixelmod.events.api.EventManager;
import com.heypixel.heypixelmod.events.api.EventTarget;
import com.heypixel.heypixelmod.events.api.types.EventType;
import com.heypixel.heypixelmod.events.impl.EventRunTicks;
import com.heypixel.heypixelmod.events.impl.EventShutdown;
import com.heypixel.heypixelmod.files.FileManager;
import com.heypixel.heypixelmod.modules.ModuleManager;
import com.heypixel.heypixelmod.modules.impl.render.ClickGUIModule;
import com.heypixel.heypixelmod.ui.notification.NotificationManager;
import com.heypixel.heypixelmod.utils.EntityWatcher;
import com.heypixel.heypixelmod.utils.EventWrapper;
import com.heypixel.heypixelmod.utils.LogUtils;
import com.heypixel.heypixelmod.utils.NetworkUtils;
import com.heypixel.heypixelmod.utils.ServerUtils;
import com.heypixel.heypixelmod.utils.TickTimeHelper;
import com.heypixel.heypixelmod.utils.renderer.Fonts;
import com.heypixel.heypixelmod.utils.renderer.PostProcessRenderer;
import com.heypixel.heypixelmod.utils.renderer.Shaders;
import com.heypixel.heypixelmod.utils.rotation.RotationManager;
import com.heypixel.heypixelmod.values.HasValueManager;
import com.heypixel.heypixelmod.values.ValueManager;
import com.heypixel.heypixelmod.events.impl.EventPacket;
import com.heypixel.heypixelmod.modules.impl.misc.AutoPlay;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import java.awt.FontFormatException;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import dev.yalan.live.LiveClient;
import net.minecraftforge.common.MinecraftForge;

public class BlinkFix {
    public static final String CLIENT_NAME = "BlinkFix-NextGeneration";
    public static final String CLIENT_DISPLAY_NAME = "Best";
    public static final String CLIENT_VERSION = "Alpha-B";
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
    public static float TICK_TIMER = 1.0F;
    public static Queue<Runnable> skipTasks = new ConcurrentLinkedQueue<>();

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
        this.fileManager.load();
        this.moduleManager.getModule(ClickGUIModule.class).setEnabled(false);
        this.eventManager.register(getInstance());
        this.eventManager.register(this.eventWrapper);
        this.eventManager.register(new RotationManager());
        this.eventManager.register(new NetworkUtils());
        this.eventManager.register(new ServerUtils());
        this.eventManager.register(new EntityWatcher());
        MinecraftForge.EVENT_BUS.register(this.eventWrapper);
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

            if (packet instanceof ClientboundSetTitleTextPacket) {
                AutoPlay autoPlayModule = (AutoPlay) this.moduleManager.getModule(AutoPlay.class);
                if (autoPlayModule != null && autoPlayModule.isEnabled()) {
                    autoPlayModule.onTitlePacket((ClientboundSetTitleTextPacket) packet);
                }
            } else if (packet instanceof ClientboundSetSubtitleTextPacket) {
                AutoPlay autoPlayModule = (AutoPlay) this.moduleManager.getModule(AutoPlay.class);
                if (autoPlayModule != null && autoPlayModule.isEnabled()) {
                    autoPlayModule.onSubtitlePacket((ClientboundSetSubtitleTextPacket) packet);
                }
            } else if (packet instanceof ClientboundSystemChatPacket) {
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
}

package com.surface;

import com.cubk.event.EventManager;
import com.surface.command.CommandManager;
import com.surface.config.ConfigManager;
import com.surface.management.InventoryManager;
import com.surface.management.RotationManager;
import com.surface.mod.Mod;
import com.surface.mod.ModManager;
import com.surface.mod.fight.AntiBotModule;
import com.surface.mod.fight.KillAuraModule;
import com.surface.mod.fight.TickBaseModule;
import com.surface.mod.fight.VelocityModule;
import com.surface.mod.move.*;
import com.surface.mod.player.*;
import com.surface.mod.visual.*;
import com.surface.mod.world.*;
import com.surface.render.clickgui.ClickGui;
import com.surface.render.font.FontManager;
import com.surface.render.notification.NotificationManager;
import com.surface.util.render.shader.ShaderBubble;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.viamcp.ViaMCP;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

public class Wrapper {
    public static Wrapper Instance;
    public String VERSION = "231230";
    public static int delta;
    private final Object[] managers = new Object[9];
    public ShaderBubble bubbleShader;
    public static final Minecraft mc = Minecraft.getMinecraft();

    public Wrapper() {
        Instance = this;
        fuck();
    }

    public void registerMods(ModManager modManager) {

        modManager.register(new AntiBotModule(), new KillAuraModule(), new TickBaseModule(), new VelocityModule());

        modManager.register(new SprintModule(), new NoSlowModule(), new ScreenMoveModule(), new VClipModule(), new SpeedModule(), new FlightModule());

        modManager.register(new InterfaceModule(), new ClickGuiModule(), new RotationAnimationModule(), new WidgetsModule(), new ESPModule(), new BlockAnimationsModule(), new NoHurtCamModule() ,new ProjectileModule(),new BedPlatesModule(),new ItemPhysicModule());

        modManager.register(new ChestStealerModule(), new BlinkModule(),new AutoArmorModule(), new InvCleanerModule(), new AutoSoupModule(), new RefillModule(), new SpeedMineModule(),new InsultsModule());

        modManager.register(new ScaffoldModule(), new DisablerModule(), new BlockAnimationsModule(), new EagleModule(), new AutoToolModule(), new HackerDetectorModule());

        for (Mod mod : modManager.getMods()) {
            mod.onPostInit();
        }
    }

    private void fuck() {
        managers[0] = LogManager.getLogger("Wrapper");

        Logger logger = (Logger) managers[0];

        try {
            VERSION = IOUtils.toString(Wrapper.class.getResourceAsStream("/.build-info")).split("\n")[0];
        } catch (Exception e) {
            logger.info("No version files detected, developer mode is enabled.");
        }

        try {
            FontManager.init();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't init FontManager.", e);
        }

        logger.info("Starting surface {}...", VERSION);

        managers[1] = new EventManager();
        managers[3] = new ModManager();
        registerMods((ModManager) managers[3]);
        managers[4] = new CommandManager();
        ((CommandManager) managers[4]).registerCommands();

        managers[2] = new InventoryManager();
        managers[6] = new RotationManager();

        managers[8] = new NotificationManager();
        managers[7] = new ConfigManager();
        managers[5] = new ClickGui();

        //viamcp
        try {
            ViaMCP.create();
            ViaMCP.INSTANCE.initAsyncSlider();
            ViaLoadingBase.getInstance().reload(ProtocolVersion.v1_12_2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Display.setTitle(Display.getTitle() + " | Surface (master/" + VERSION + ")");
        load();
    }

    private void load() {
        ((ConfigManager) managers[7]).read("modules");
    }

    // TODO: get client username
    public String getUsername() {
        return "Insane1337";
    }

    public CommandManager getCommandManager() {
        return (CommandManager) managers[4];
    }

    public static void sendMessage(String s) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA.toString() + EnumChatFormatting.BOLD + "Surface " + EnumChatFormatting.WHITE + ">> " + EnumChatFormatting.GRAY + s));
    }

    public static void sendMessageWith(String name, String s) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA.toString() + EnumChatFormatting.BOLD + name + " " + EnumChatFormatting.WHITE + ">> " + EnumChatFormatting.GRAY + s));
    }

    public EventManager getEventManager() {
        return (EventManager) managers[1];
    }

    public ModManager getModManager() {
        return (ModManager) managers[3];
    }

    public Logger getLogger() {
        return (Logger) managers[0];
    }

    public InventoryManager getInventoryManager() {
        return (InventoryManager) managers[2];
    }

    public ClickGui getClickGui() {
        return (ClickGui) managers[5];
    }

    public RotationManager getRotationManager() {
        return (RotationManager) managers[6];
    }

    public ConfigManager getConfigManager() {
        return (ConfigManager) managers[7];
    }

    public NotificationManager getNotificationManager() {
        return (NotificationManager) managers[8];
    }
}

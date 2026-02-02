package tech.blinkfix.modules.impl.render;

import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.impl.EventRender2D;
import tech.blinkfix.events.impl.EventShader;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.ui.Island.ChestContent;
import tech.blinkfix.ui.Island.CommandPaletteContent;
import tech.blinkfix.ui.Island.ErrorMessageContent;
import tech.blinkfix.ui.Island.IslandManager;
import tech.blinkfix.ui.Island.ModuleToggleContent;
import tech.blinkfix.ui.Island.PlayerListContent;
import tech.blinkfix.ui.Island.ScaffoldContent;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.FloatValue;

@ModuleInfo(
   name = "Island",
   description = "Dynamic Island",
   category = Category.RENDER
)
public class Island extends Module {
    private final IslandManager islandManager = new IslandManager();
    private final ModuleToggleContent moduleToggleContent = new ModuleToggleContent();
    private final ScaffoldContent scaffoldContent = new ScaffoldContent();
    private final CommandPaletteContent commandPaletteContent = new CommandPaletteContent();
    private final ErrorMessageContent errorMessageContent = new ErrorMessageContent();
    private final ChestContent chestContent = new ChestContent();
    private final PlayerListContent playerListContent = new PlayerListContent();
    
    public final FloatValue xOffset = ValueBuilder.create(this, "X Offset")
            .setMinFloatValue(-1000.0F)
            .setMaxFloatValue(1000.0F)
            .setDefaultFloatValue(0.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();
    
    public final FloatValue yOffset = ValueBuilder.create(this, "Y Offset")
            .setMinFloatValue(-1000.0F)
            .setMaxFloatValue(1000.0F)
            .setDefaultFloatValue(0.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();

    public Island() {
        islandManager.addContent(moduleToggleContent);
        islandManager.addContent(scaffoldContent);
        islandManager.addContent(commandPaletteContent);
        islandManager.addContent(errorMessageContent);
        islandManager.addContent(chestContent);
        islandManager.addContent(playerListContent);
        
        // 注册命令面板的事件监听
        commandPaletteContent.registerEvents();
    }

    @EventTarget
    public void onRender(EventRender2D e) {
        islandManager.render(e.getGuiGraphics());
    }

    @EventTarget
    public void onShader(EventShader e) {
        islandManager.renderShader(e.getGraphics());
    }

    @Override
    public void onEnable() {
        // 命令面板事件由 Naven 初始化时注册，这里不需要重复注册
    }

    @Override
    public void onDisable() {
        // 命令面板应该始终保持可用，即使 Island 模块禁用也不注销
    }

    /**
     * 公开方法，供 Module 类调用以通知模块切换
     */
    public void notifyModuleToggled(Module module) {
        if (moduleToggleContent != null) {
            moduleToggleContent.onModuleToggled(module);
        }
    }
}

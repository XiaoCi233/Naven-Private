package tech.blinkfix.modules;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.modules.impl.render.ClickGUIModule;
import tech.blinkfix.modules.impl.render.HUD;
import tech.blinkfix.ui.notification.Notification;
import tech.blinkfix.ui.notification.NotificationLevel;
import tech.blinkfix.utils.SmoothAnimationTimer;
//import com.heypixel.heypixelmod.utils.localization.ModuleLanguageManager;
import tech.blinkfix.values.HasValue;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import tech.blinkfix.modules.impl.render.Island;
import tech.blinkfix.values.Value;
import java.util.List;

public abstract class Module extends HasValue {
    public static final Minecraft mc = Minecraft.getInstance();
    public static boolean update = true;
    private final SmoothAnimationTimer animation = new SmoothAnimationTimer(100.0F);
    private String name;
    private String prettyName;
    private String description;
    private String suffix;
    private Category category;
    private boolean enabled;
    private int minPermission = 0;
    private int key;
    protected final BooleanValue hideInArrayList = ValueBuilder
            .create(this, "Hide in ArrayList (Only Arrylistmode New)")
            .setDefaultBooleanValue(false)
            .build()
            .getBooleanValue();

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        super.setName(name);
        this.setPrettyName();
    }

    public void setSuffix(String suffix) {
        if (suffix == null) {
            this.suffix = null;
            update = true;
        } else if (!suffix.equals(this.suffix)) {
            this.suffix = suffix;
            update = true;
        }
    }

    private void setPrettyName() {
        StringBuilder builder = new StringBuilder();
        char[] chars = this.name.toCharArray();

        for (int i = 0; i < chars.length - 1; i++) {
            if (Character.isLowerCase(chars[i]) && Character.isUpperCase(chars[i + 1])) {
                builder.append(chars[i]).append(" ");
            } else {
                builder.append(chars[i]);
            }
        }

        builder.append(chars[chars.length - 1]);
        this.prettyName = builder.toString();
    }

    protected void initModule() {
        if (this.getClass().isAnnotationPresent(ModuleInfo.class)) {
            ModuleInfo moduleInfo = this.getClass().getAnnotation(ModuleInfo.class);
            this.name = moduleInfo.name();
            this.description = moduleInfo.description();
            this.category = moduleInfo.category();
            super.setName(this.name);
            this.setPrettyName();
            BlinkFix.getInstance().getHasValueManager().registerHasValue(this);
        }
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void setEnabled(boolean enabled) {
        try {
            BlinkFix naven = BlinkFix.getInstance();
            if (enabled) {
                if (this instanceof PermissionGatedModule) {
                    PermissionGatedModule gated = (PermissionGatedModule) this;
                    if (!gated.hasPermission()) {
                        this.enabled = false;
                        // notify and refuse enabling
                        Notification notification = new Notification(NotificationLevel.INFO,
                                gated.getPermissionDenyMessage(), 3000L);
                        naven.getNotificationManager().addNotification(notification);
                        return;
                    }
                }
                this.enabled = true;
                naven.getEventManager().register(this);
                this.onEnable();
                if (!(this instanceof ClickGUIModule)) {
                    HUD module = (HUD) BlinkFix.getInstance().getModuleManager().getModule(HUD.class);
                    if (module.moduleToggleSound.getCurrentValue()) {
                        mc.player.playSound(SoundEvents.WOODEN_BUTTON_CLICK_ON, 0.5F, 1.3F);
                    }

                    Notification notification = new Notification(NotificationLevel.SUCCESS, this.name + " Enabled!",
                            3000L);
                    naven.getNotificationManager().addNotification(notification);

                    // 通知 Island 模块模块已切换
                    notifyIslandModuleToggle(this);
                }
            } else {
                this.enabled = false;
                naven.getEventManager().unregister(this);
                this.onDisable();
                if (!(this instanceof ClickGUIModule)) {
                    HUD module = (HUD) BlinkFix.getInstance().getModuleManager().getModule(HUD.class);
                    if (module.moduleToggleSound.getCurrentValue()) {
                        mc.player.playSound(SoundEvents.WOODEN_BUTTON_CLICK_OFF, 0.5F, 0.8F);
                    }

                    Notification notification = new Notification(NotificationLevel.ERROR, this.name + " Disabled!",
                            3000L);
                    naven.getNotificationManager().addNotification(notification);

                    // 通知 Island 模块模块已切换
                    notifyIslandModuleToggle(this);
                }
            }
        } catch (Exception var5) {
        }
    }

    /**
     * 通知 Island 模块有模块切换
     */
    private void notifyIslandModuleToggle(Module module) {
        try {
            Island island = (Island) BlinkFix.getInstance()
                    .getModuleManager().getModule(Island.class);
            if (island != null) {
                island.notifyModuleToggled(module);
            }
        } catch (Exception e) {
            // 如果 Island 模块不存在或未启用，忽略
        }
    }

    public void toggle() {
        this.setEnabled(!this.enabled);
    }

    public SmoothAnimationTimer getAnimation() {
        return this.animation;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String getPrettyName() {
        return this.prettyName;
    }

    // public String getDisplayName() {
    // return ModuleLanguageManager.getTranslation("module." +
    // this.name.toLowerCase());
    // }

    public String getDescription() {
        return this.description;
    }

    // public String getDisplayDescription() {
    // return ModuleLanguageManager.getTranslation("module." +
    // this.name.toLowerCase() + ".desc");
    // }

    public String getSuffix() {
        return this.suffix;
    }

    public Category getCategory() {
        return this.category;
    }

    // public String getDisplayCategory() {
    // return ModuleLanguageManager.getTranslation("category." +
    // this.category.name().toLowerCase());
    // }

    public boolean isEnabled() {
        return this.enabled;
    }

    public int getMinPermission() {
        return this.minPermission;
    }

    public int getKey() {
        return this.key;
    }

    public Module() {
    }

    public void setMinPermission(int minPermission) {
        this.minPermission = minPermission;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public BooleanValue getHideInArrayList() {
        return this.hideInArrayList;
    }

    public List<Value> getValues() {
        return BlinkFix.getInstance().getValueManager().getValuesByHasValue(this);
    }
}
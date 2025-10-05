package com.heypixel.heypixelmod.modules;

import com.heypixel.heypixelmod.BlinkFix;
import com.heypixel.heypixelmod.modules.impl.render.ClickGUIModule;
import com.heypixel.heypixelmod.modules.impl.render.HUD;
import com.heypixel.heypixelmod.ui.notification.Notification;
import com.heypixel.heypixelmod.ui.notification.NotificationLevel;
import com.heypixel.heypixelmod.utils.SmoothAnimationTimer;
import com.heypixel.heypixelmod.utils.localization.ModuleLanguageManager;
import com.heypixel.heypixelmod.values.HasValue;
import com.heypixel.heypixelmod.values.ValueBuilder;
import com.heypixel.heypixelmod.values.impl.BooleanValue;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;

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
    protected final BooleanValue hideInArrayList = ValueBuilder.create(this, "Hide in ArrayList")
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
            com.heypixel.heypixelmod.BlinkFix.getInstance().getHasValueManager().registerHasValue(this);
        }
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void setEnabled(boolean enabled) {
        try {
            BlinkFix blinkFix = com.heypixel.heypixelmod.BlinkFix.getInstance();
            if (enabled) {
                this.enabled = true;
                blinkFix.getEventManager().register(this);
                this.onEnable();
                if (!(this instanceof ClickGUIModule)) {
                    HUD module = (HUD) com.heypixel.heypixelmod.BlinkFix.getInstance().getModuleManager().getModule(HUD.class);
                    if (module.moduleToggleSound.getCurrentValue()) {
                        mc.player.playSound(SoundEvents.WOODEN_BUTTON_CLICK_ON, 0.5F, 1.3F);
                    }

                    String message = ModuleLanguageManager.getTranslation("module." + this.name.toLowerCase()) + " " +
                            ModuleLanguageManager.getTranslation("setting.enabled");
                    Notification notification = new Notification(NotificationLevel.SUCCESS, message, 3000L);
                    blinkFix.getNotificationManager().addNotification(notification);
                }
            } else {
                this.enabled = false;
                blinkFix.getEventManager().unregister(this);
                this.onDisable();
                if (!(this instanceof ClickGUIModule)) {
                    HUD module = (HUD) BlinkFix.getInstance().getModuleManager().getModule(HUD.class);
                    if (module.moduleToggleSound.getCurrentValue()) {
                        mc.player.playSound(SoundEvents.WOODEN_BUTTON_CLICK_OFF, 0.5F, 0.8F);
                    }

                    String message = ModuleLanguageManager.getTranslation("module." + this.name.toLowerCase()) + " " +
                            ModuleLanguageManager.getTranslation("setting.disabled");
                    Notification notification = new Notification(NotificationLevel.ERROR, message, 3000L);
                    blinkFix.getNotificationManager().addNotification(notification);
                }
            }
        } catch (Exception var5) {
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

    public String getDisplayName() {
        return ModuleLanguageManager.getTranslation("module." + this.name.toLowerCase());
    }

    public String getDescription() {
        return this.description;
    }

    public String getDisplayDescription() {
        return ModuleLanguageManager.getTranslation("module." + this.name.toLowerCase() + ".desc");
    }

    public String getSuffix() {
        return this.suffix;
    }

    public Category getCategory() {
        return this.category;
    }

    public String getDisplayCategory() {
        return ModuleLanguageManager.getTranslation("category." + this.category.name().toLowerCase());
    }

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
}
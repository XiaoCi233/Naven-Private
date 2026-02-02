package tech.blinkfix.utils.localization;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class GUITranslationManager {
    private static final Map<String, String> translationMap = new HashMap<>();
    private static final Pattern MODULE_NAME_PATTERN = Pattern.compile("([A-Z][a-z]*)([A-Z][a-z]*)");

    static {
        initTranslations();
    }

    private static void initTranslations() {
        // 按钮和通用文本
        translationMap.put("Save", "button.save");
        translationMap.put("Cancel", "button.cancel");
        translationMap.put("Back", "button.back");
        translationMap.put("Apply", "button.apply");
        translationMap.put("Reset", "button.reset");
        translationMap.put("Add", "button.add");
        translationMap.put("Delete", "button.delete");
        translationMap.put("Edit", "button.edit");
        translationMap.put("Confirm", "button.confirm");
        translationMap.put("Enabled", "setting.enabled");
        translationMap.put("Disabled", "setting.disabled");
        translationMap.put("On", "setting.on");
        translationMap.put("Off", "setting.off");

        // 屏幕标题
        translationMap.put("Settings", "menu.settings");
        translationMap.put("Modules", "menu.modules");
        translationMap.put("Configuration", "menu.config");
        translationMap.put("Language", "menu.language");
        translationMap.put("Profiles", "menu.profiles");
        translationMap.put("Accounts", "menu.accounts");
        translationMap.put("HUD Settings", "menu.hud");
        translationMap.put("Themes", "menu.themes");
        translationMap.put("Keybinds", "menu.keybinds");
        translationMap.put("Friends", "menu.friends");

        // 模块名称（自动检测驼峰命名）
        translationMap.put("KillAura", "module.killaura");
        translationMap.put("Scaffold", "module.scaffold");
        translationMap.put("Fly", "module.fly");
        translationMap.put("Speed", "module.speed");
        // 添加其他模块...
    }

    public static Component translateComponent(Component original) {
        String text = original.getString();

        // 如果是空文本，直接返回
        if (text.isEmpty()) {
            return original;
        }

//        // 检查直接匹配
//        if (translationMap.containsKey(text)) {
//            String translated = ModuleLanguageManager.getTranslation(translationMap.get(text));
//            if (!translated.equals(translationMap.get(text))) {
//                return Component.literal(translated);
//            }
//        }
//
//        // 检查模块名称（驼峰命名）
//        if (isLikelyModuleName(text)) {
//            String translationKey = "module." + text.toLowerCase();
//            String translated = ModuleLanguageManager.getTranslation(translationKey);
//            if (!translated.equals(translationKey)) {
//                return Component.literal(translated);
//            }
//        }

        return original;
    }

    private static boolean isLikelyModuleName(String text) {
        // 简单的模块名称检测逻辑
        return text.chars().anyMatch(Character::isUpperCase) &&
                text.length() > 2 &&
                text.length() < 20;
    }

    // 用于直接翻译字符串（在非Mixin环境中使用）
    public static String translateString(String text) {
        Component component = translateComponent(Component.literal(text));
        return component.getString();
    }
}
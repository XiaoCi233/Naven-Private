//package com.heypixel.heypixelmod.utils.localization;
//
//import net.minecraft.client.Minecraft;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class CustomLanguageHelper {
//    private static final Map<String, Map<String, String>> customTranslations = new HashMap<>();
//
//    static {
//        // 初始化自定义翻译
//        initChineseTranslations();
//        initJapaneseTranslations();
//    }
//
//    private static void initChineseTranslations() {
//        Map<String, String> chinese = new HashMap<>();
//        chinese.put("module.killaura", "杀戮光环");
//        chinese.put("module.scaffold", "脚手架");
//        chinese.put("module.fly", "飞行");
//        chinese.put("module.speed", "速度");
//        chinese.put("module.language", "语言");
//        chinese.put("setting.enabled", "已启用");
//        chinese.put("setting.disabled", "已禁用");
//        chinese.put("category.combat", "战斗");
//        chinese.put("category.movement", "移动");
//        chinese.put("category.misc", "杂项");
//        customTranslations.put("zh_cn", chinese);
//    }
//
//    private static void initJapaneseTranslations() {
//        Map<String, String> japanese = new HashMap<>();
//        japanese.put("module.killaura", "キルオーラ");
//        japanese.put("module.scaffold", "足場");
//        japanese.put("module.fly", "飛行");
//        japanese.put("module.speed", "速度");
//        japanese.put("module.language", "言語");
//        japanese.put("setting.enabled", "有効");
//        japanese.put("setting.disabled", "無効");
//        japanese.put("category.combat", "戦闘");
//        japanese.put("category.movement", "移動");
//        japanese.put("category.misc", "その他");
//        customTranslations.put("ja_jp", japanese);
//    }
//
//    public static String getCustomTranslation(String key) {
//        String currentLang = getCurrentLanguage();
//        Map<String, String> translations = customTranslations.get(currentLang);
//
//        if (translations != null && translations.containsKey(key)) {
//            return translations.get(key);
//        }
//
//        // 如果没有找到自定义翻译，返回key或者尝试英语
//        Map<String, String> englishTranslations = customTranslations.get("en_us");
//        if (englishTranslations != null && englishTranslations.containsKey(key)) {
//            return englishTranslations.get(key);
//        }
//
//        return key;
//    }
//
//    public static String getCurrentLanguage() {
//        Minecraft mc = Minecraft.getInstance();
//        if (mc != null && mc.getLanguageManager() != null) {
//            return mc.getLanguageManager().getSelected();
//        }
//        return "en_us";
//    }
//
//    public static boolean isChinese() {
//        return "zh_cn".equals(getCurrentLanguage());
//    }
//
//    public static boolean isJapanese() {
//        return "ja_jp".equals(getCurrentLanguage());
//    }
//}
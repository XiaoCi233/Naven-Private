//package com.heypixel.heypixelmod.modules.impl.render;
//
//import com.heypixel.heypixelmod.events.api.EventTarget;
//import com.heypixel.heypixelmod.events.impl.EventRunTicks;
//import com.heypixel.heypixelmod.events.api.types.EventType;
//import com.heypixel.heypixelmod.modules.Category;
//import com.heypixel.heypixelmod.modules.Module;
//import com.heypixel.heypixelmod.modules.ModuleInfo;
//import com.heypixel.heypixelmod.utils.localization.ModuleLanguageManager;
//import com.heypixel.heypixelmod.values.ValueBuilder;
//import com.heypixel.heypixelmod.values.impl.ModeValue;
//
//@ModuleInfo(name = "Language", description = "Change module language", category = Category.RENDER)
//public class Language extends Module {
//
//    public final ModeValue languageMode = ValueBuilder.create(this, "Language")
//            .setModes("English", "Chinese", "Japanese")
//            .setDefaultModeIndex(0)
//            .build()
//            .getModeValue();
//
//    private String currentLanguage = "English";
//
//    @EventTarget
//    public void onTick(EventRunTicks event) {
//        if (event.getType() == EventType.PRE) {
//            String selected = languageMode.getCurrentMode();
//            if (!currentLanguage.equals(selected)) {
//                updateLanguage(selected);
//                currentLanguage = selected;
//            }
//        }
//    }
//
//    private void updateLanguage(String languageName) {
//        String languageCode;
//        switch (languageName) {
//            case "Chinese": languageCode = "zh_cn"; break;
//            case "Japanese": languageCode = "ja_jp"; break;
//            case "English": default: languageCode = "en_us"; break;
//        }
//
//        ModuleLanguageManager.setLanguage(languageCode);
//    }
//}
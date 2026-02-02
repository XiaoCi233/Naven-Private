//package com.heypixel.heypixelmod.modules.impl.render;
//
//import api.events.tech.blinkfix.EventTarget;
//import impl.events.tech.blinkfix.EventRunTicks;
//import types.api.events.tech.blinkfix.EventType;
//import modules.tech.blinkfix.Category;
//import modules.tech.blinkfix.Module;
//import modules.tech.blinkfix.ModuleInfo;
//import com.heypixel.heypixelmod.utils.localization.ModuleLanguageManager;
//import values.tech.blinkfix.ValueBuilder;
//import impl.values.tech.blinkfix.ModeValue;
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
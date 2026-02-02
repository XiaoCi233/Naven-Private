//package com.heypixel.heypixelmod.modules.impl.combat;
//
//import tech.blinkfix.BlinkFix;
//import api.events.tech.blinkfix.EventTarget;
//import impl.events.tech.blinkfix.EventKey;
//import impl.events.tech.blinkfix.EventRunTicks;
//import modules.tech.blinkfix.Category;
//import modules.tech.blinkfix.Module;
//import modules.tech.blinkfix.ModuleInfo;
//import values.tech.blinkfix.ValueBuilder;
//import impl.values.tech.blinkfix.ModeValue;
//
//import java.util.HashSet;
//import java.util.Set;
//
//@ModuleInfo(
//        name = "PreferWeapon",
//        description = "Prioritizes a specific weapon for InventoryManager's sword slot",
//        category = Category.MISC
//)
//public class PreferWeapon extends Module {
//    private final ModeValue weaponPriority = ValueBuilder.create(this, "Priority")
//            .setModes("Sword", "God Axe", "KB Ball", "End Crystal")
//            .build()
//            .getModeValue();
//
//    private final Set<Integer> pressedKeys = new HashSet<>();
//
//    public PreferWeapon() {
//        this.setToggleableWithKey(false);
//    }
//
//    @EventTarget
//    public void onMotion(EventRunTicks event) {
//        this.setSuffix(weaponPriority.getCurrentMode());
//    }
//
//    @EventTarget
//    public void onKey(EventKey event) {
//        if (this.isEnabled() && this.getKey() == event.getKey()) {
//            if (event.isState()) {
//                if (pressedKeys.add(event.getKey())) {
//                    int currentIndex = weaponPriority.getCurrentValue();
//                    int nextIndex = (currentIndex + 1) % weaponPriority.getValues().length;
//                    weaponPriority.setCurrentValue(nextIndex);
//                }
//            } else {
//                pressedKeys.remove(event.getKey());
//            }
//        }
//    }
//
//    public static String getPriority() {
//        PreferWeapon module = (PreferWeapon) BlinkFix.getInstance().getModuleManager().getModule(PreferWeapon.class);
//
//        if (module != null && module.isEnabled()) {
//            return module.weaponPriority.getCurrentMode();
//        }
//
//        return "Sword";
//    }
//}

//package com.heypixel.heypixelmod.commands.impl;
//
//import tech.blinkfix.BlinkFix;
//import commands.tech.blinkfix.Command;
//import commands.tech.blinkfix.CommandInfo;
//import api.events.tech.blinkfix.EventTarget;
//import types.api.events.tech.blinkfix.EventType;
//import impl.events.tech.blinkfix.EventMotion;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.screens.LanguageSelectScreen;
//
//@CommandInfo(
//   name = "language",
//   description = "Open language gui.",
//   aliases = {"lang"}
//)
//public class CommandLanguage extends Command {
//   @Override
//   public void onCommand(String[] args) {
//      BlinkFix.getInstance().getEventManager().register(new Object() {
//         @EventTarget
//         public void onMotion(EventMotion e) {
//            if (e.getType() == EventType.PRE) {
//               Minecraft.getInstance().setScreen(new LanguageSelectScreen(null, Minecraft.getInstance().options, Minecraft.getInstance().getLanguageManager()));
//               tech.blinkfix.BlinkFix.getInstance().getEventManager().unregister(this);
//            }
//         }
//      });
//   }
//
//   @Override
//   public String[] onTab(String[] args) {
//      return new String[0];
//   }
//}

package com.heypixel.heypixelmod.commands.impl;

import com.heypixel.heypixelmod.BlinkFix;
import com.heypixel.heypixelmod.commands.Command;
import com.heypixel.heypixelmod.commands.CommandInfo;
import com.heypixel.heypixelmod.events.api.EventTarget;
import com.heypixel.heypixelmod.events.api.types.EventType;
import com.heypixel.heypixelmod.events.impl.EventMotion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LanguageSelectScreen;

@CommandInfo(
   name = "language",
   description = "Open language gui.",
   aliases = {"lang"}
)
public class CommandLanguage extends Command {
   @Override
   public void onCommand(String[] args) {
      BlinkFix.getInstance().getEventManager().register(new Object() {
         @EventTarget
         public void onMotion(EventMotion e) {
            if (e.getType() == EventType.PRE) {
               Minecraft.getInstance().setScreen(new LanguageSelectScreen(null, Minecraft.getInstance().options, Minecraft.getInstance().getLanguageManager()));
               com.heypixel.heypixelmod.BlinkFix.getInstance().getEventManager().unregister(this);
            }
         }
      });
   }

   @Override
   public String[] onTab(String[] args) {
      return new String[0];
   }
}

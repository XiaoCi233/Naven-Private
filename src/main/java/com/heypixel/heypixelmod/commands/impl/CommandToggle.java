package com.heypixel.heypixelmod.commands.impl;

import com.heypixel.heypixelmod.BlinkFix;
import com.heypixel.heypixelmod.commands.Command;
import com.heypixel.heypixelmod.commands.CommandInfo;
import com.heypixel.heypixelmod.exceptions.NoSuchModuleException;
import com.heypixel.heypixelmod.modules.Module;
import com.heypixel.heypixelmod.utils.ChatUtils;

@CommandInfo(
   name = "toggle",
   description = "Toggle a module",
   aliases = {"t"}
)
public class CommandToggle extends Command {
   @Override
   public void onCommand(String[] args) {
      if (args.length == 1) {
         String moduleName = args[0];

         try {
            Module module = BlinkFix.getInstance().getModuleManager().getModule(moduleName);
            if (module != null) {
               module.toggle();
            } else {
               ChatUtils.addChatMessage("Stupid you entered it wrong.");
            }
         } catch (NoSuchModuleException var4) {
            ChatUtils.addChatMessage("Stupid you entered it wrong.");
         }
      }
   }

   @Override
   public String[] onTab(String[] args) {
      return com.heypixel.heypixelmod.BlinkFix.getInstance()
         .getModuleManager()
         .getModules()
         .stream()
         .map(Module::getName)
         .filter(name -> name.toLowerCase().startsWith(args.length == 0 ? "" : args[0].toLowerCase()))
         .toArray(String[]::new);
   }
}

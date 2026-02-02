package tech.blinkfix.commands.impl;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.commands.Command;
import tech.blinkfix.commands.CommandInfo;
import tech.blinkfix.exceptions.NoSuchModuleException;
import tech.blinkfix.modules.Module;
import tech.blinkfix.utils.ChatUtils;

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
      return BlinkFix.getInstance()
         .getModuleManager()
         .getModules()
         .stream()
         .map(Module::getName)
         .filter(name -> name.toLowerCase().startsWith(args.length == 0 ? "" : args[0].toLowerCase()))
         .toArray(String[]::new);
   }
}

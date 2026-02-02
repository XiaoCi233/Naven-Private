package tech.blinkfix.commands.impl;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.commands.Command;
import tech.blinkfix.commands.CommandInfo;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.impl.EventKey;
import tech.blinkfix.exceptions.NoSuchModuleException;
import tech.blinkfix.modules.Module;
import tech.blinkfix.utils.ChatUtils;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;

@CommandInfo(
   name = "bind",
   description = "Bind a command to a key",
   aliases = {"b"}
)
public class CommandBind extends Command {
   @Override
   public void onCommand(String[] args) {
      if (args.length == 1) {
         final String moduleName = args[0];

         try {
            final Module module = BlinkFix.getInstance().getModuleManager().getModule(moduleName);
            if (module != null) {
               ChatUtils.addChatMessage("Press a key to bind " + moduleName + " to.");
               BlinkFix.getInstance().getEventManager().register(new Object() {
                  @EventTarget
                  public void onKey(EventKey e) {
                     if (e.isState()) {
                        module.setKey(e.getKey());
                        Key key = InputConstants.getKey(e.getKey(), 0);
                        String keyName = key.getDisplayName().getString().toUpperCase();
                        ChatUtils.addChatMessage("绑定 " + moduleName + " 到 " + keyName + ".");
                        BlinkFix.getInstance().getEventManager().unregister(this);
                        BlinkFix.getInstance().getFileManager().save();
                     }
                  }
               });
            } else {
               ChatUtils.addChatMessage("Stupid you entered it wrong.");
            }
         } catch (NoSuchModuleException var7) {
            ChatUtils.addChatMessage("Stupid you entered it wrong.");
         }
      } else if (args.length == 2) {
         String moduleName = args[0];
         String keyName = args[1];

         try {
            Module module = BlinkFix.getInstance().getModuleManager().getModule(moduleName);
            if (module != null) {
               if (keyName.equalsIgnoreCase("none")) {
                  module.setKey(InputConstants.UNKNOWN.getValue());
                  ChatUtils.addChatMessage("Unbound " + moduleName + ".");
                  BlinkFix.getInstance().getFileManager().save();
               } else {
                  Key key = InputConstants.getKey("key.keyboard." + keyName.toLowerCase());
                  if (key != InputConstants.UNKNOWN) {
                     module.setKey(key.getValue());
                     ChatUtils.addChatMessage("Bound " + moduleName + " to " + keyName.toUpperCase() + ".");
                     BlinkFix.getInstance().getFileManager().save();
                  } else {
                     ChatUtils.addChatMessage("Stupid you entered it wrong.");
                  }
               }
            } else {
               ChatUtils.addChatMessage("Stupid you entered it wrong.");
            }
         } catch (NoSuchModuleException var6) {
            ChatUtils.addChatMessage("Stupid you entered it wrong.");
         }
      } else {
         ChatUtils.addChatMessage("Usage: .bind <module> [key]");
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

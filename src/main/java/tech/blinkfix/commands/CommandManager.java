package tech.blinkfix.commands;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.commands.impl.*;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.impl.EventClientChat;
import tech.blinkfix.utils.ChatUtils;
import java.util.HashMap;
import java.util.Map;

public class CommandManager {
   public static final String PREFIX = ".";
   public final Map<String, Command> aliasMap = new HashMap<>();

   public CommandManager() {
      try {
         this.initCommands();
      } catch (Exception var2) {
         throw new RuntimeException(var2);
      }

      BlinkFix.getInstance().getEventManager().register(this);
   }

   private void initCommands() {
       this.registerCommand(new CommandZen());
      this.registerCommand(new CommandBind());
      this.registerCommand(new CommandBinds());
      this.registerCommand(new CommandToggle());
      this.registerCommand(new CommandConfig());
//      this.registerCommand(new CommandLanguage());
      this.registerCommand(new CommandProxy());
       this.registerCommand(new CommandSetName());
   }

   private void registerCommand(Command command) {
      command.initCommand();
      this.aliasMap.put(command.getName().toLowerCase(), command);

      for (String alias : command.getAliases()) {
         this.aliasMap.put(alias.toLowerCase(), command);
      }
   }

   @EventTarget
   public void onChat(EventClientChat e) {
      if (e.getMessage().startsWith(".")) {
         e.setCancelled(true);
         String chatMessage = e.getMessage().substring(".".length());
         String[] arguments = chatMessage.split(" ");
         if (arguments.length < 1) {
            ChatUtils.addChatMessage("Stupid you entered it wrong.");
            return;
         }

         String alias = arguments[0].toLowerCase();
         Command command = this.aliasMap.get(alias);
         if (command == null) {
            ChatUtils.addChatMessage("Stupid you entered it wrong.");
            return;
         }

         String[] args = new String[arguments.length - 1];
         System.arraycopy(arguments, 1, args, 0, args.length);
         command.onCommand(args);
      }
   }
}

package xyz.gay.mixin;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.modules.impl.move.Scaffold;
import tech.blinkfix.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({ClientLevel.class})
public class MixinClientLevel {
   @Redirect(
      method = {"tickNonPassenger"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/Entity;tick()V"
      )
   )
   public void hookSkipTicks(Entity instance) {
      Minecraft mc = Minecraft.getInstance();
      
      // 如果是玩家实体且skipTicks > 0，则跳过tick并递减计数
      if (instance == mc.player && BlinkFix.skipTicks > 0) {
         int currentTick = BlinkFix.skipTicks;
         
         // 检查Scaffold模块是否启用以及Logging选项
         Scaffold scaffold = (Scaffold) BlinkFix.getInstance().getModuleManager().getModule(Scaffold.class);
         if (scaffold != null && scaffold.isEnabled() && scaffold.Logging.getCurrentValue()) {
            ChatUtils.addChatMessage("Self Rescue: " + currentTick);
         }
         
         BlinkFix.skipTicks--;
      } else if (!BlinkFix.skipTasks.isEmpty() && instance == mc.player) {
         // 处理skipTasks队列中的任务
         Runnable task = BlinkFix.skipTasks.poll();
         if (task != null) {
            task.run();
         }
      } else {
         // 正常执行tick
         instance.tick();
      }
   }
}

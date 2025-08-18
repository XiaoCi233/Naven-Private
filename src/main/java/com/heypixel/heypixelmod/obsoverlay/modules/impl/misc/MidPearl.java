package com.heypixel.heypixelmod.obsoverlay.modules.impl.misc;

import com.heypixel.heypixelmod.obsoverlay.modules.Category;
import com.heypixel.heypixelmod.obsoverlay.modules.Module;
import com.heypixel.heypixelmod.obsoverlay.modules.ModuleInfo;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.MinecraftForge;

@ModuleInfo(
        name = "MidPearl",
        category = Category.MISC,
        description = "Allows you to use fireball longjump"
)
public class MidPearl extends Module {

    private boolean attempted;
    private int bestPearlSlot;

    @Override
    public void onEnable() {
        this.attempted = false;

        // 使用MinecraftForge.EVENT_BUS注册事件监听器
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onDisable() {
        // 禁用时取消注册
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    // 事件处理方法
    @SubscribeEvent
    public void onMouseButtonEvent(InputEvent.MouseButton event) {
        // 打印鼠标事件信息，确保事件被捕获
        System.out.println("Mouse button event: " + event.getButton() + " Action: " + event.getAction());

        if (event.getButton() == 2 && event.getAction() == 1) {  // 2表示鼠标中键，1表示按下事件
            event.setCanceled(true);  // 取消事件传递

            if (!this.attempted) {
                this.attempted = true;

                // 查找末影珍珠
                for (int slot = 5; slot < 45; ++slot) {
                    ItemStack stack = Minecraft.getInstance().player.getInventory().getItem(slot);
                    if (stack.isEmpty() || stack.getItem() != Items.ENDER_PEARL || slot < 36)
                        continue;
                    this.bestPearlSlot = slot;
                    Minecraft.getInstance().player.getInventory().selected = this.bestPearlSlot - 36;
                    break;
                }
                if (this.bestPearlSlot != 0) {
                    ItemStack stack = Minecraft.getInstance().player.getInventory().getItem(this.bestPearlSlot - 36);
                    if (!stack.isEmpty() && stack.getItem() == Items.ENDER_PEARL) {
                        throwPearl();
                    }
                }
            }
        }
    }

    private void throwPearl() {
        LocalPlayer player = Minecraft.getInstance().player;
        player.getInventory().selected = this.bestPearlSlot - 36;  // 选中末影珍珠
        player.swing(InteractionHand.MAIN_HAND);  // 玩家挥动手中的物品

        // 执行末影珍珠的丢掷
        Minecraft.getInstance().gameMode.useItem(player, InteractionHand.MAIN_HAND);
    }
}

package tech.blinkfix.ui.CooldownBar;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.impl.EventRender2D;
import tech.blinkfix.utils.ChatUtils;

import java.util.LinkedList;

public class CooldownBarManager {
    private static final Minecraft mc = Minecraft.getInstance();
    private final LinkedList<CooldownBar> bars = new LinkedList<>();

    public void addBar(CooldownBar bar) {
        if (!bars.contains(bar)) {
            bars.addLast(bar);
            ChatUtils.addChatMessage("1");
        }
    }

    @EventTarget(3)
    public void onRender(EventRender2D e) {
        bars.removeIf(CooldownBar::isExpired);
        if (bars.isEmpty()) return;

        PoseStack poseStack = e.getStack();
        GuiGraphics guiGraphics = e.getGuiGraphics();

        poseStack.pushPose();
        int scaledWidth = mc.getWindow().getGuiScaledWidth();
        int scaledHeight = mc.getWindow().getGuiScaledHeight();
        poseStack.translate(scaledWidth / 2f - 50, scaledHeight / 2f - 150, 0);

        int counter = 0;
        for (CooldownBar bar : bars) {
            poseStack.pushPose();
            bar.getYAnimation().target = counter++ * 25f;
            bar.getYAnimation().update(true);
            poseStack.translate(0, bar.getYAnimation().value, 0);

            bar.render(guiGraphics, poseStack);

            poseStack.popPose();
        }
        poseStack.popPose();
    }
}

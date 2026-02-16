package com.surface.mod.visual;

import com.cubk.event.annotations.EventTarget;
import com.surface.Wrapper;
import com.surface.events.Event2D;
import com.surface.interfaces.ModuleFormatter;
import com.surface.mod.Mod;
import com.surface.mod.world.ScaffoldModule;
import com.surface.render.font.FontDrawer;
import com.surface.render.font.FontManager;
import com.surface.util.player.PlayerUtils;
import com.surface.util.render.RenderUtils;
import com.surface.util.render.shader.ShaderElement;
import com.surface.value.impl.BooleanValue;
import com.surface.value.impl.ColorValue;
import com.surface.value.impl.ModeValue;
import com.surface.value.impl.TextValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import renderassist.animations.LinearAnimation;
import renderassist.rendering.BasicRendering;
import renderassist.rendering.StencilUtils;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class InterfaceModule extends Mod implements ModuleFormatter {

    public final ColorValue colorValue = new ColorValue("Color (Global)", new Color(63, 81, 181));
    private final ModeValue colorMode = new ModeValue("Color Mode", "Rainbow", new String[]{"Static", "Rainbow"});
    private final BooleanValue watermark = new BooleanValue("Watermark", true);
    private final TextValue text = new TextValue("Watermark Text", "Surface") {
        @Override
        public boolean isVisible() {
            return watermark.getValue();
        }
    };
    private final ModeValue watermarkMode = new ModeValue("Watermark Mode", "Fancy", new String[]{"Fancy", "Simple"}) {
        @Override
        public boolean isVisible() {
            return watermark.getValue();
        }
    };
    private final BooleanValue arrayList = new BooleanValue("Array List", true);
    private final BooleanValue hiderender = new BooleanValue("Hide Render Modules", false) {
        @Override
        public boolean isVisible() {
            return arrayList.getValue();
        }
    };
    private final BooleanValue background = new BooleanValue("Background", true) {
        @Override
        public boolean isVisible() {
            return arrayList.getValue();
        }
    };
    public final BooleanValue backgroundBlur = new BooleanValue("Background Blur", true) {
        @Override
        public boolean isVisible() {
            return background.isVisible() && background.getValue();
        }
    };
    public final BooleanValue sideBar = new BooleanValue("Side bar", true) {
        @Override
        public boolean isVisible() {
            return background.isVisible() && background.getValue();
        }
    };
    public final ColorValue backgroundColor = new ColorValue("Background Color", new Color(0, 0, 0, 144)) {
        @Override
        public boolean isVisible() {
            return background.isVisible() && background.getValue();
        }
    }.alpha(true);
    private final BooleanValue userInfo = new BooleanValue("User Info", true);
    private final String[] trashMessages = new String[]{"Sleep for GrimAC Edition", "Fake Autoblock Edition", "Sleep > Faiths Edition"};
    private final String trashMessage = trashMessages[new Random().nextInt(trashMessages.length)];
    public static FontDrawer fontDrawer = FontManager.TAHOMA;
    private float scale;
    public InterfaceModule() {
        super("Interface", Category.VISUAL);
        setState(true);
        ModeValue fontMode = new ModeValue("Font", "Smooth", new String[]{"Smooth", "Vanilla"}) {
            @Override
            public void setValue(String value) {
                if (value.equalsIgnoreCase("smooth"))
                    fontDrawer = FontManager.TAHOMA;
                if (value.equalsIgnoreCase("vanilla"))
                    fontDrawer = mc.fontRendererObj;
                super.setValue(value);
            }
        };
        registerValues(colorValue, colorMode, fontMode, watermark, text, watermarkMode, arrayList, hiderender,background, backgroundBlur, backgroundColor, userInfo);
    }

    @EventTarget
    private void onR2D(Event2D event2D) {
        scale = LinearAnimation.animate(scale, Wrapper.Instance.getModManager().getModFromName("Scaffold").isEnable() ? 100 : 0, 0.5f);


        int slot = ((ScaffoldModule) Wrapper.Instance.getModManager().getModFromName("Scaffold")).getBlockSlot();
        ItemStack heldItem = slot == -1 ? null : mc.thePlayer.inventory.mainInventory[slot];
        int count = slot == -1 ? 0 : ((ScaffoldModule) Wrapper.Instance.getModManager().getModFromName("Scaffold")).getBlockCount();
        String countStr = String.valueOf(count);
        float blockWH = heldItem != null ? 15 : -2;
        int spacing = 3;
        float x, y;
        ScaledResolution sr = new ScaledResolution(mc);
        String text = "§l" + countStr + "§r block" + (count != 1 ? "s" : "");
        FontManager.WQY.setFontSize(18);
        float textWidth = FontManager.WQY.getStringWidth(text);

        float totalWidth = ((textWidth + blockWH + spacing) + 6);
        x = sr.getScaledWidth() / 2f - (totalWidth / 2f);
        y = sr.getScaledHeight() - (sr.getScaledHeight() / 2f - 20);
        float height = 20;

        if (scale < 2) return;

        RenderUtils.scaleStart(x + totalWidth / 2f, y + height / 2f, scale / 100);

        RenderUtils.scissorStart(x - 1.5, y - 1.5, totalWidth + 3, height + 3);


        StencilUtils.initStencilToWrite();
        RenderUtils.drawRound(x, y, totalWidth, height, 5, true, RenderUtils.tripleColor(20, .45f), true, true, true, true);
        StencilUtils.readStencilBuffer(1);
        ShaderElement.renderBlur(8);
        StencilUtils.endStencilBuffer();
        RenderUtils.drawRound(x, y, totalWidth, height, 5, true, RenderUtils.tripleColor(20, .45f), true, true, true, true);

        FontManager.WQY.drawString(text, x + 10 + blockWH + spacing, y + FontManager.WQY.getMiddleOfBox(height) - .5f, -1);

        if (heldItem != null) {
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(heldItem, (int) x + 7, (int) (y + 10 - (blockWH / 2)));
            RenderHelper.disableStandardItemLighting();
        }
        RenderUtils.scissorEnd();

        RenderUtils.scaleEnd();
    }

    @EventTarget
    public void on2d(Event2D event) {
        if (watermark.getValue()) {
            String fancymark = "SURFACE";
            if (watermarkMode.isCurrentMode("Fancy")) {

                String info = (Wrapper.Instance.getUsername() + EnumChatFormatting.GRAY + " | " + EnumChatFormatting.WHITE + Minecraft.getDebugFPS() + " FPS" + EnumChatFormatting.GRAY + " | " + EnumChatFormatting.WHITE + (mc.isSingleplayer() ? "singleplayer" : mc.getCurrentServerData().serverIP));


                FontManager.MUSEO900.setFontSize(18);
                FontManager.MUSEO700.setFontSize(16);

                //logo
                ShaderElement.addBlurTask(() -> RenderUtils.drawRoundedRect(6, 6, (float) FontManager.MUSEO700.getStringWidthD(fancymark) + 15, FontManager.MUSEO900.getHeight() + 8, 4, new Color(0, 0, 0, 144).getRGB()));
                RenderUtils.drawRoundedRect(6, 6, (float) FontManager.MUSEO700.getStringWidthD(fancymark) + 15, FontManager.MUSEO900.getHeight() + 8, 4, new Color(0, 0, 0, 144).getRGB());

                // part 2
                ShaderElement.addBlurTask(() -> RenderUtils.drawRoundedRect(6 + (float) FontManager.MUSEO700.getStringWidthD(fancymark) + 20, 6, (float) FontManager.MUSEO700.getStringWidth2(info) + 10, FontManager.MUSEO900.getHeight() + 8, 4, new Color(0, 0, 0, 144).getRGB()));
                RenderUtils.drawRoundedRect(6 + (float) FontManager.MUSEO700.getStringWidthD(fancymark) + 20, 6, (float) FontManager.MUSEO700.getStringWidth2(info) + 10, FontManager.MUSEO900.getHeight() + 8, 4, new Color(0, 0, 0, 144).getRGB());

                //logo string
                FontManager.MUSEO900.drawString(fancymark, 10, 9.5f, -1);

                // part 2 string
                FontManager.MUSEO700.drawString(info, 6 + (float) FontManager.MUSEO700.getStringWidthD(fancymark) + 20 + 5, 10f, -1);
            }
            if (watermarkMode.isCurrentMode("Simple")) {
                FontManager.WQY.setFontSize(18);
                fontDrawer.drawStringWithShadow(text.getValue() + EnumChatFormatting.WHITE + " - " + trashMessage, 4, 4, colorValue.getRGB());
            }
        }

        if (arrayList.getValue()) {
            drawArrayList(event);
        }

        if (userInfo.getValue()) {
            FontManager.WQY.setFontSize(18);
            String build = "Surface - " + EnumChatFormatting.RESET + Wrapper.Instance.VERSION.toLowerCase();
            final String info = EnumChatFormatting.GRAY + build + EnumChatFormatting.GRAY + " - " + EnumChatFormatting.WHITE + Wrapper.Instance.getUsername();
            FontManager.WQY.drawStringWithShadow(info, event.getScaledResolution().getScaledWidth() - FontManager.WQY.getStringWidth2(info) - 2, event.getScaledResolution().getScaledHeight() - FontManager.WQY.getHeight() - 2, -1);
        }
        FontManager.WQY.setFontSize(18);
        FontManager.WQY.drawStringWithShadow(EnumChatFormatting.GRAY + "Speed: " + EnumChatFormatting.WHITE + PlayerUtils.calculateBPS(mc.thePlayer), 2, event.getScaledResolution().getScaledHeight() - FontManager.WQY.getHeight() - 2, -1);

    }

    private void drawArrayList(Event2D event) {
        int counter = 1;
        final List<Mod> modules = Wrapper.Instance.getModManager().getModsSorted();
        float y = 2 + 6;

        FontManager.TAHOMA.setFontSize(16);

        for (Mod m : modules) {
            if (hiderender.getValue() && m.getCategory() == Category.VISUAL) continue;

            int color = colorMode.isCurrentMode("Rainbow") ? new Color(Color.HSBtoRGB((float) ((double) this.mc.thePlayer.ticksExisted / 50.0 + Math.sin((double) counter / 50.0 * 1.6)) % 1.0f, 0.6f, 1.0f)).getRGB() : colorValue.getRGB();
            int moduleWidth = fontDrawer.getStringWidth2(formatModule(m));
            m.setArrayX(LinearAnimation.animate(m.getArrayX(), m.getState() ? (event.getScaledResolution().getScaledWidth() - moduleWidth - 6 - 3) * 10 : event.getScaledResolution().getScaledWidth() * 10 + 20, 0.2f));
            m.setArrayY(LinearAnimation.animate(m.getArrayY(), m.getState() ? y * 10 : 0, 0.2f));
            if (background.getValue()) {
                if (backgroundBlur.getValue()) {
                    ShaderElement.blurArea(m.getArrayX() / 10 - 2, m.getArrayY() / 10 - 2, moduleWidth + 4, fontDrawer.getHeight() + 6);
                    ShaderElement.addBloomTask(() -> BasicRendering.drawRect(m.getArrayX() / 10 - 2, m.getArrayY() / 10 - 2, moduleWidth + 4, fontDrawer.getHeight() + 6, -1));
                }
                BasicRendering.drawRect(m.getArrayX() / 10 - 2, m.getArrayY() / 10 - 2, moduleWidth + 4, fontDrawer.getHeight() + 6, backgroundColor.getRGB());

                if (sideBar.getValue())
                    BasicRendering.drawRect(m.getArrayX() / 10 - 2 + moduleWidth + 4, m.getArrayY() / 10 - 2, 1, fontDrawer.getHeight() + 6, color);
            }
            fontDrawer.drawStringWithShadow(formatModule(m), m.getArrayX() / 10, m.getArrayY() / 10 + 1, color);
            if (m.getState())
                y += fontDrawer.getHeight() + 6;
            counter++;
        }
    }

}

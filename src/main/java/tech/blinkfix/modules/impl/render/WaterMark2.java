package tech.blinkfix.modules.impl.render;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.impl.EventRender2D;
import tech.blinkfix.events.impl.EventUpdate;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.modules.ModuleManager;
import tech.blinkfix.modules.impl.move.Scaffold;
import tech.blinkfix.utils.skia.Skia;
import tech.blinkfix.utils.skia.context.SkiaContext;
import tech.blinkfix.utils.skia.font.Fonts;
import tech.blinkfix.utils.shader.impl.KawaseBlur;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;
import tech.blinkfix.values.impl.FloatValue;
import tech.blinkfix.values.impl.ModeValue;
import tech.blinkfix.values.impl.StringValue;
import io.github.humbleui.skija.Font;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import com.mojang.blaze3d.platform.Lighting;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(
        name = "WaterMark2",
        description = "Skija-based watermark and notifications",
        category = Category.RENDER
)
public class WaterMark2 extends Module {

    private final StringValue clientName = ValueBuilder.create(this, "ClientName")
            .setDefaultStringValue("Neko")
            .build()
            .getStringValue();

    private final FloatValue animTension = ValueBuilder.create(this, "BounceTension")
            .setDefaultFloatValue(0.05f)
            .setMinFloatValue(0.01f)
            .setMaxFloatValue(1.0f)
            .setFloatStep(0.01f)
            .build()
            .getFloatValue();

    private final FloatValue animFriction = ValueBuilder.create(this, "BounceFriction")
            .setDefaultFloatValue(0.3f)
            .setMinFloatValue(0.01f)
            .setMaxFloatValue(1.0f)
            .setFloatStep(0.01f)
            .build()
            .getFloatValue();

    private final ModeValue styles = ValueBuilder.create(this, "Styles")
            .setModes("None", "Normal", "Opal", "New Opai")
            .setDefaultModeIndex(1)
            .build()
            .getModeValue();

    private final BooleanValue customIp = ValueBuilder.create(this, "customIP")
            .setDefaultBooleanValue(false)
            .build()
            .getBooleanValue();

    private final StringValue ip = ValueBuilder.create(this, "IP")
            .setDefaultStringValue("hidden.ip")
            .setVisibility(customIp::getCurrentValue)
            .build()
            .getStringValue();

    private final FloatValue colorR = ValueBuilder.create(this, "Red")
            .setDefaultFloatValue(255.0f)
            .setMinFloatValue(0.0f)
            .setMaxFloatValue(255.0f)
            .setFloatStep(1.0f)
            .build()
            .getFloatValue();

    private final FloatValue colorG = ValueBuilder.create(this, "Green")
            .setDefaultFloatValue(255.0f)
            .setMinFloatValue(0.0f)
            .setMaxFloatValue(255.0f)
            .setFloatStep(1.0f)
            .build()
            .getFloatValue();

    private final FloatValue colorB = ValueBuilder.create(this, "Blue")
            .setDefaultFloatValue(255.0f)
            .setMinFloatValue(0.0f)
            .setMaxFloatValue(255.0f)
            .setFloatStep(1.0f)
            .build()
            .getFloatValue();

    private final FloatValue backgroundAlpha = ValueBuilder.create(this, "BackGroundAlpha")
            .setDefaultFloatValue(160.0f)
            .setMinFloatValue(0.0f)
            .setMaxFloatValue(255.0f)
            .setFloatStep(1.0f)
            .build()
            .getFloatValue();

    private final BooleanValue shadowCheck = ValueBuilder.create(this, "Shadow")
            .setDefaultBooleanValue(false)
            .build()
            .getBooleanValue();

    private final FloatValue shadowRadius = ValueBuilder.create(this, "Shadow-Radius")
            .setDefaultFloatValue(15.0f)
            .setMinFloatValue(1.0f)
            .setMaxFloatValue(50.0f)
            .setFloatStep(1.0f)
            .build()
            .getFloatValue();

    private final BooleanValue blurCheck = ValueBuilder.create(this, "Blur")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    private final FloatValue blurRadius = ValueBuilder.create(this, "BlurStrength")
            .setDefaultFloatValue(10.0f)
            .setMinFloatValue(1.0f)
            .setMaxFloatValue(50.0f)
            .setFloatStep(1.0f)
            .build()
            .getFloatValue();

    private final FloatValue notifyDuration = ValueBuilder.create(this, "NotifyTime(ms)")
            .setDefaultFloatValue(3000.0f)
            .setMinFloatValue(1000.0f)
            .setMaxFloatValue(10000.0f)
            .setFloatStep(100.0f)
            .build()
            .getFloatValue();

    private final StringValue versionNameUp = ValueBuilder.create(this, "VersionName")
            .setDefaultStringValue("development")
            .setVisibility(() -> styles.isCurrentMode("Opal"))
            .build()
            .getStringValue();

    private final FloatValue buttonColorR = ValueBuilder.create(this, "Button-Color-Red")
            .setDefaultFloatValue(20.0f)
            .setMinFloatValue(0.0f)
            .setMaxFloatValue(255.0f)
            .setFloatStep(1.0f)
            .build()
            .getFloatValue();

    private final FloatValue buttonColorG = ValueBuilder.create(this, "Button-Color-Green")
            .setDefaultFloatValue(150.0f)
            .setMinFloatValue(0.0f)
            .setMaxFloatValue(255.0f)
            .setFloatStep(1.0f)
            .build()
            .getFloatValue();

    private final FloatValue buttonColorB = ValueBuilder.create(this, "Button-Color-Blue")
            .setDefaultFloatValue(180.0f)
            .setMinFloatValue(0.0f)
            .setMaxFloatValue(255.0f)
            .setFloatStep(1.0f)
            .build()
            .getFloatValue();

    private final FloatValue buttonColorA = ValueBuilder.create(this, "Button-Color-Alpha")
            .setDefaultFloatValue(255.0f)
            .setMinFloatValue(0.0f)
            .setMaxFloatValue(255.0f)
            .setFloatStep(1.0f)
            .build()
            .getFloatValue();

    private final BooleanValue moduleNotify = ValueBuilder.create(this, "Notification")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    private final BooleanValue isScaffold = ValueBuilder.create(this, "Scaffold")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    private final FloatValue scaffoldThemeR = ValueBuilder.create(this, "ScaffoldTheme-Red")
            .setDefaultFloatValue(65.0f)
            .setMinFloatValue(0.0f)
            .setMaxFloatValue(255.0f)
            .setFloatStep(1.0f)
            .build()
            .getFloatValue();

    private final FloatValue scaffoldThemeG = ValueBuilder.create(this, "ScaffoldTheme-Green")
            .setDefaultFloatValue(130.0f)
            .setMinFloatValue(0.0f)
            .setMaxFloatValue(255.0f)
            .setFloatStep(1.0f)
            .build()
            .getFloatValue();

    private final FloatValue scaffoldThemeB = ValueBuilder.create(this, "ScaffoldTheme-Blue")
            .setDefaultFloatValue(225.0f)
            .setMinFloatValue(0.0f)
            .setMaxFloatValue(255.0f)
            .setFloatStep(1.0f)
            .build()
            .getFloatValue();

    private final FloatValue maxBlocks = ValueBuilder.create(this, "maxBlocks")
            .setDefaultFloatValue(576.0f)
            .setMinFloatValue(64.0f)
            .setMaxFloatValue(576.0f)
            .setFloatStep(1.0f)
            .build()
            .getFloatValue();

    private final BooleanValue chestTheme = ValueBuilder.create(this, "Chest")
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();

    private final FloatValue chestRounded = ValueBuilder.create(this, "ChestRoundRadius")
            .setDefaultFloatValue(4.0f)
            .setMinFloatValue(0.0f)
            .setMaxFloatValue(8.0f)
            .setFloatStep(0.1f)
            .build()
            .getFloatValue();

    private final FloatValue bpsUpdateInterval = ValueBuilder.create(this, "BPS-Update-Interval(ms)")
            .setDefaultFloatValue(100.0f)
            .setMinFloatValue(50.0f)
            .setMaxFloatValue(500.0f)
            .setFloatStep(10.0f)
            .build()
            .getFloatValue();

    private static final ResourceLocation BLOCK_ICON = new ResourceLocation("blinkfix", "textures/watermark_images/block.png");
    private static final ResourceLocation FPS_ICON = new ResourceLocation("blinkfix", "textures/watermark_images/fps.png");
    private static final ResourceLocation LOGO_ICON = new ResourceLocation("blinkfix", "textures/watermark_images/logo_icon.png");
    private static final ResourceLocation MS_ICON = new ResourceLocation("blinkfix", "textures/watermark_images/ms.png");
    private static final ResourceLocation USER_ICON = new ResourceLocation("blinkfix", "textures/watermark_images/user.png");

    private static final float ITEM_NOTIFY_HEIGHT = 38.0f;
    private static final float NORMAL_WATERMARK_HEIGHT = 28.0f;

    private final Map<Module, Boolean> prevModuleStates = new HashMap<>();
    private final CopyOnWriteArrayList<WaterMark2ToggleNotification> notifications = new CopyOnWriteArrayList<>();
    private final WaterMark2BpsTracker bpsTracker = new WaterMark2BpsTracker(100L);
    private boolean blurFailed = false;

    private float animGlobalX = 0.0f;
    private float animGlobalWidth = 100.0f;
    private float animGlobalHeight = 28.0f;
    private float velGlobalX = 0.0f;
    private float velGlobalWidth = 0.0f;
    private float velGlobalHeight = 0.0f;
    private float progressBarAnimationWidth = 0.0f;
    private float velProgressBar = 0.0f;
    private double animatedBps = 0.0;

    private int screenWidth;
    private int screenHeight;
    private float startY;

    @Override
    public void onEnable() {
        prevModuleStates.clear();
        notifications.clear();
    }

    @Override
    public void onDisable() {
        prevModuleStates.clear();
        notifications.clear();
    }

    @EventTarget
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.level == null) {
            return;
        }

        bpsTracker.setInterval((long) bpsUpdateInterval.getCurrentValue());
        bpsTracker.update(mc.player.getX(), mc.player.getZ(), System.currentTimeMillis());

        if (moduleNotify.getCurrentValue()) {
            ModuleManager manager = BlinkFix.getInstance().getModuleManager();
            for (Module module : manager.getModules()) {
                Boolean prevState = prevModuleStates.get(module);
                if (prevState == null) {
                    prevModuleStates.put(module, module.isEnabled());
                    continue;
                }

                boolean currentState = module.isEnabled();
                if (prevState != currentState) {
                    prevModuleStates.put(module, currentState);
                    String titleText = "Module Toggled";
                    String modName = "§a" + module.getName();
                    String stateText = currentState ? "§aEnabled" : "§cDisabled";
                    String message = modName + " §fhas been " + stateText + "§f!";
                    showToggleNotification(titleText, message, currentState, module.getName());
                }
            }
        }

        updateNotifications();
    }

    @EventTarget
    public void onRender2D(EventRender2D e) {
        if (mc.player == null || mc.level == null) {
            return;
        }

        updateScreen();

        boolean scaffoldEnabled = isScaffoldEnabled();
        boolean hasNotifications = moduleNotify.getCurrentValue() && !notifications.isEmpty();
        boolean chestOpen = chestTheme.getCurrentValue() && mc.player.containerMenu instanceof ChestMenu;
        String style = styles.getCurrentMode();

        RenderMode renderMode = RenderMode.NONE;
        float targetWidth = 0.0f;
        float targetHeight = 0.0f;
        float targetX = 0.0f;

        if (scaffoldEnabled && isScaffold.getCurrentValue()) {
            renderMode = RenderMode.SCAFFOLD;
            targetWidth = 190.0f;
            targetHeight = 58.0f;
            targetX = (screenWidth - targetWidth) / 2.0f;
        } else if (hasNotifications && styles.isCurrentMode("New Opai")) {
            renderMode = RenderMode.NOTIFY_STACK;
            float maxWidth = calcMaxNotificationWidth();
            targetWidth = Math.max(maxWidth, 180.0f);
            targetHeight = notifications.size() * ITEM_NOTIFY_HEIGHT;
            targetX = (screenWidth - targetWidth) / 2.0f;
        } else if (styles.isCurrentMode("New Opai")) {
            renderMode = RenderMode.NORMAL_OPAI;
            Normal3Info info = calcNormal3Info();
            targetWidth = info.width;
            targetHeight = NORMAL_WATERMARK_HEIGHT;
            targetX = (screenWidth - targetWidth) / 2.0f;
        }

        if (renderMode != RenderMode.NONE) {
            WaterMark2Spring.Result w = WaterMark2Spring.step(animGlobalWidth, targetWidth, velGlobalWidth,
                    animTension.getCurrentValue(), animFriction.getCurrentValue());
            animGlobalWidth = Math.max(0.0f, w.position);
            velGlobalWidth = w.velocity;

            WaterMark2Spring.Result h = WaterMark2Spring.step(animGlobalHeight, targetHeight, velGlobalHeight,
                    animTension.getCurrentValue(), animFriction.getCurrentValue());
            animGlobalHeight = Math.max(0.0f, h.position);
            velGlobalHeight = h.velocity;

            WaterMark2Spring.Result x = WaterMark2Spring.step(animGlobalX, targetX, velGlobalX,
                    animTension.getCurrentValue(), animFriction.getCurrentValue());
            animGlobalX = x.position;
            velGlobalX = x.velocity;
        }

        if (blurCheck.getCurrentValue() && !blurFailed) {
            try {
                int blurStrength = Math.max(1, Math.min(20, Math.round(blurRadius.getCurrentValue())));
                KawaseBlur.INGAME_BLUR.draw(blurStrength);
            } catch (Exception ignored) {
                blurFailed = true;
            }
        }

        final RenderMode currentRenderMode = renderMode;
        try {
            SkiaContext.draw(canvas -> {
                if (styles.isCurrentMode("Normal")) {
                    drawNormal();
                    if (hasNotifications) {
                        drawOldStyleNotifications(0.0f);
                    }
                } else if (styles.isCurrentMode("Opal")) {
                    drawNormal2();
                    if (hasNotifications) {
                        drawOldStyleNotifications(32.0f);
                    }
                }

                if (currentRenderMode != RenderMode.NONE) {
                    float drawX = animGlobalX;
                    float drawY = startY;
                    float drawW = animGlobalWidth;
                    float drawH = animGlobalHeight;
                    float radius = drawH > 30.0f ? 8.0f : drawH / 2.0f;

                    if (shadowCheck.getCurrentValue()) {
                        Skia.drawShadow(drawX, drawY, drawW, drawH, shadowRadius.getCurrentValue(),
                                new Color(0, 0, 0, 120));
                    }

                    if (blurCheck.getCurrentValue() && !blurFailed) {
                        try {
                            Skia.drawRoundedBlur(drawX, drawY, drawW, drawH, radius);
                        } catch (Exception ignored) {
                            blurFailed = true;
                        }
                    }

                    Skia.drawRoundedRect(drawX, drawY, drawW, drawH, radius,
                            new Color(0, 0, 0, clampColor(backgroundAlpha.getCurrentValue())));

                    if (currentRenderMode == RenderMode.SCAFFOLD) {
                        renderScaffoldContent(drawX, drawY, drawW, drawH);
                    } else if (currentRenderMode == RenderMode.NOTIFY_STACK) {
                        renderNotificationStack(drawX, drawY);
                    } else if (currentRenderMode == RenderMode.NORMAL_OPAI) {
                        renderNormal3Content(drawX, drawY, drawW, drawH);
                    }
                }

                if (chestOpen) {
                    ChestLayout layout = getChestLayout();
                    if (layout != null) {
                        Skia.drawRoundedRect(layout.x, layout.y, layout.width, layout.height,
                                chestRounded.getCurrentValue(),
                                new Color(0, 0, 0, clampColor(backgroundAlpha.getCurrentValue())));
                    }
                }
            });
        } catch (Exception ignored) {
        }

        if (chestOpen) {
            drawChestItems(e.getGuiGraphics());
        }
    }

    private void updateScreen() {
        screenWidth = mc.getWindow().getGuiScaledWidth();
        screenHeight = mc.getWindow().getGuiScaledHeight();
        startY = screenHeight / 20.0f;
    }

    private boolean isScaffoldEnabled() {
        try {
            Scaffold scaffold = (Scaffold) BlinkFix.getInstance().getModuleManager().getModule(Scaffold.class);
            return scaffold != null && scaffold.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    private int getSafePing() {
        if (mc.player == null || mc.getConnection() == null) {
            return 0;
        }
        var info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        return info != null ? info.getLatency() : 0;
    }

    private void renderScaffoldContent(float x, float y, float w, float h) {
        if (mc.player == null) {
            return;
        }

        int totalBlockCount = 0;
        for (ItemStack stack : mc.player.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                totalBlockCount += stack.getCount();
            }
        }

        float percentage = Math.min(1.0f, totalBlockCount / Math.max(1.0f, maxBlocks.getCurrentValue()));
        double targetBps = bpsTracker.getDisplayedBps();
        double speedFactor = Math.min(2.0, Math.max(0.1, Minecraft.getInstance().getFps() / 20.0));
        animatedBps += (targetBps - animatedBps) * 0.15 * speedFactor;

        float padding = 10.0f;
        float cornerRadius = 6.0f;
        float iconSize = 32.0f;
        float iconBgX = x + padding;
        float iconBgY = y + padding;

        Color themeColor = new Color(clampColor(scaffoldThemeR.getCurrentValue()),
                clampColor(scaffoldThemeG.getCurrentValue()),
                clampColor(scaffoldThemeB.getCurrentValue()), 200);

        Skia.drawRoundedRect(iconBgX, iconBgY, iconSize, iconSize, cornerRadius - 1.0f, themeColor);
        drawResourceImage(BLOCK_ICON, iconBgX + (iconSize - 24) / 2.0f, iconBgY + (iconSize - 24) / 2.0f + 1.0f, 24, 24,
                Color.WHITE);

        Font titleFont = Fonts.getUrbanistVariable(16.0f);
        Font subFont = Fonts.getUrbanistVariable(13.0f);

        float textX = iconBgX + iconSize + 8.0f;
        float contentTop = y + padding;
        float barHeight = 6.0f;
        float barY = y + h - barHeight - padding;
        float contentHeight = Math.max(0.0f, barY - contentTop - 4.0f);
        float textBlockHeight = WaterMark2TextMetrics.getFontHeight(titleFont) + WaterMark2TextMetrics.getFontHeight(subFont) + 2.0f;
        float textTop = contentTop + (contentHeight - textBlockHeight) / 2.0f;
        float titleTop = textTop;
        float subTop = textTop + WaterMark2TextMetrics.getFontHeight(titleFont) + 2.0f;

        Skia.drawText("Scaffold Toggled", textX, titleTop, Color.WHITE, titleFont);
        String bpsText = String.format("%.2f", animatedBps < 0.01 ? 0.00 : animatedBps);
        Skia.drawText(totalBlockCount + " blocks - " + bpsText + " block/s", textX, subTop,
                new Color(200, 200, 200), subFont);

        float maxBarWidth = w - (padding * 2.0f);
        float targetBarWidth = maxBarWidth * percentage;
        WaterMark2Spring.Result bar = WaterMark2Spring.step(progressBarAnimationWidth, targetBarWidth, velProgressBar,
                animTension.getCurrentValue(), animFriction.getCurrentValue());
        progressBarAnimationWidth = Math.min(maxBarWidth, Math.max(0.0f, bar.position));
        velProgressBar = bar.velocity;

        Skia.drawRoundedRect(x + padding, barY, maxBarWidth, barHeight, 3.0f, new Color(60, 60, 70, 180));
        Color lighter = new Color(
                Math.min(255, themeColor.getRed() + 50),
                Math.min(255, themeColor.getGreen() + 50),
                Math.min(255, themeColor.getBlue() + 50), 255);
        Skia.drawRoundedRect(x + padding, barY, progressBarAnimationWidth, barHeight, 3.0f, lighter);
    }

    private void renderNotificationStack(float x, float y) {
        float currentY = 0.0f;
        float centerXOffset = 10.0f;
        for (WaterMark2ToggleNotification notify : notifications) {
            float rowY = y + currentY;
            notify.draw(x + centerXOffset, rowY);
            currentY += ITEM_NOTIFY_HEIGHT;
        }
    }

    private void renderNormal3Content(float x, float y, float w, float h) {
        Normal3Info info = calcNormal3Info();
        Font textFont = Fonts.getUrbanistVariable(15.0f);
        float centerY = y + h / 2.0f;
        float textTop = WaterMark2TextMetrics.getTopY(centerY, textFont);
        float iconSize = 15.0f;
        float iconY = centerY - iconSize / 2.0f;
        float cx = x + info.padding;

        Color iconColor = new Color(clampColor(colorR.getCurrentValue()), clampColor(colorG.getCurrentValue()),
                clampColor(colorB.getCurrentValue()), 255);
        drawResourceImage(LOGO_ICON, cx, iconY, iconSize, iconSize, iconColor);
        cx += iconSize + info.elementSpacing;
        Skia.drawText(clientName.getCurrentValue(), cx, textTop, iconColor, textFont);
        cx += info.clientNameWidth + info.dotSpacing;
        drawCenteredDot(cx, textTop, textFont);
        cx += 4.0f;

        drawResourceImage(USER_ICON, cx, iconY, iconSize, iconSize, Color.WHITE);
        cx += iconSize + info.elementSpacing;
        Skia.drawText(info.username, cx - 1.0f, textTop, Color.WHITE, textFont);
        cx += info.usernameWidth + info.dotSpacing;
        drawCenteredDot(cx, textTop, textFont);
        cx += 4.0f;

        drawResourceImage(MS_ICON, cx, iconY, iconSize, iconSize, Color.GREEN);
        cx += iconSize + info.elementSpacing;
        Skia.drawText(info.pingStr, cx, textTop, Color.GREEN, textFont);
        cx += info.pingTextWidth;
        Skia.drawText("  to  ", cx, textTop, Color.WHITE, textFont);
        cx += info.toTextWidth;
        Skia.drawText(info.ipStr, cx, textTop, Color.WHITE, textFont);
        cx += info.serverIpWidth + info.dotSpacing;
        drawCenteredDot(cx - 1.0f, textTop, textFont);
        cx += 3.0f;

        drawResourceImage(FPS_ICON, cx, iconY, iconSize, iconSize, Color.WHITE);
        cx += iconSize + info.elementSpacing;
        Skia.drawText(info.fpsStr, cx, textTop, Color.WHITE, textFont);
    }

    private void drawOldStyleNotifications(float startYOffset) {
        float currentY = startY + startYOffset;
        float padding = 3.0f;
        Font titleFont = Fonts.getUrbanistVariable(15.0f);
        Font descFont = Fonts.getUrbanistVariable(12.0f);

        for (WaterMark2ToggleNotification notify : notifications) {
            float titleW = Skia.getStringWidth(notify.title, titleFont);
            float descW = Skia.getStringWidth(notify.message, descFont);
            float w = 35.0f + Math.max(titleW, descW) + 20.0f;
            float x = (screenWidth - w) / 2.0f;

            Skia.drawRoundedRect(x, currentY, w, ITEM_NOTIFY_HEIGHT, 10.0f,
                    new Color(0, 0, 0, clampColor(backgroundAlpha.getCurrentValue())));
            notify.draw(x + padding, currentY + 2.0f);
            currentY += ITEM_NOTIFY_HEIGHT + 2.0f;
        }
    }

    private void drawNormal() {
        if (mc.player == null) {
            return;
        }
        String username = mc.player.getGameProfile().getName();
        int fps = getDisplayedFps();
        int ping = getSafePing();
        Color colorRGB = new Color(clampColor(colorR.getCurrentValue()), clampColor(colorG.getCurrentValue()),
                clampColor(colorB.getCurrentValue()), 255);
        String text = " | " + username + " | " + fps + "fps | " + ping + "ms";
        String mainText = clientName.getCurrentValue();

        Font font = Fonts.getUrbanistVariable(16.0f);
        float h = 38.0f;
        float wCalc = 20.0f + 18.0f + 5.0f + Skia.getStringWidth(mainText + text, font) + 10.0f;
        float x = (screenWidth - wCalc) / 2.0f;
        float y = startY;

        WaterMark2Spring.Result xAnim = WaterMark2Spring.step(animGlobalX, x, velGlobalX, animTension.getCurrentValue(),
                animFriction.getCurrentValue());
        animGlobalX = xAnim.position;
        velGlobalX = xAnim.velocity;

        if (shadowCheck.getCurrentValue()) {
            Skia.drawShadow(x, y, wCalc, h, shadowRadius.getCurrentValue(), new Color(0, 0, 0, 120));
        }
        Skia.drawRoundedRect(x, y, wCalc, h, h / 2.0f,
                new Color(10, 10, 10, clampColor(backgroundAlpha.getCurrentValue())));
        float centerY = y + h / 2.0f;
        float textTop = WaterMark2TextMetrics.getTopY(centerY, font);
        drawResourceImage(LOGO_ICON, x + 5.0f, centerY - 9.0f, 18, 18, colorRGB);
        Skia.drawText(mainText, x + 28.0f, textTop, colorRGB, font);
        Skia.drawText(text, x + 28.0f + Skia.getStringWidth(mainText, font), textTop, Color.WHITE, font);
    }

    private void drawNormal2() {
    }

    private void drawCenteredDot(float x, float textTop, Font font) {
        Skia.drawText("·", x - 3.0f, textTop, new Color(180, 180, 180, 255), font);
    }

    private Normal3Info calcNormal3Info() {
        String username = mc.getUser() != null ? mc.getUser().getName() : "Unknown";
        int fps = getDisplayedFps();
        int ping = getSafePing();
        String ipStr = customIp.getCurrentValue() ? ip.getCurrentValue() : getServerIp();
        Font font = Fonts.getUrbanistVariable(15.0f);

        float clientNameWidth = Skia.getStringWidth(clientName.getCurrentValue(), font);
        float usernameWidth = Skia.getStringWidth(username, font) - 1.0f;
        String pingStr = ping + "ms";
        float pingTextWidth = Skia.getStringWidth(pingStr, font);
        float toTextWidth = Skia.getStringWidth("  to  ", font);
        float serverIpWidth = Skia.getStringWidth(ipStr, font);
        String fpsStr = fps + "fps";
        float fpsTextWidth = Skia.getStringWidth(fpsStr, font);

        float padding = 12.0f;
        float icon = 15.0f;
        float space = 6.0f;
        float dot = 10.0f;
        float dotW = 4.0f;

        float w = padding + icon + space + clientNameWidth + dot + dotW +
                icon + space + usernameWidth + dot + dotW +
                icon + space + pingTextWidth + toTextWidth + serverIpWidth + dot + 3.0f +
                icon + space + fpsTextWidth + padding;

        return new Normal3Info(w, padding, space, dot, username, clientNameWidth, usernameWidth, pingStr,
                pingTextWidth, toTextWidth, ipStr, serverIpWidth, fpsStr, fpsTextWidth);
    }

    private float calcMaxNotificationWidth() {
        if (notifications.isEmpty()) {
            return 0.0f;
        }

        Font titleFont = Fonts.getUrbanistVariable(15.0f);
        Font descFont = Fonts.getUrbanistVariable(12.0f);
        float maxWidth = 0.0f;
        float fixedElementWidth = 30.0f + 15.0f + 30.0f;
        for (WaterMark2ToggleNotification notif : notifications) {
            float titleWidth = Skia.getStringWidth(notif.title, titleFont);
            float descWidth = Skia.getStringWidth(notif.message, descFont);
            float textWidth = Math.max(titleWidth, descWidth);
            float totalWidth = fixedElementWidth + textWidth;
            maxWidth = Math.max(maxWidth, totalWidth);
        }
        return maxWidth;
    }

    private void drawResourceImage(ResourceLocation resource, float x, float y, float width, float height, Color tint) {
        if (tint.getRed() == 255 && tint.getGreen() == 255 && tint.getBlue() == 255 && tint.getAlpha() == 255) {
            Skia.drawImage(resource, x, y, width, height);
        } else {
            Skia.drawImage(resource, x, y, width, height, tint);
        }
    }

    private String getServerIp() {
        ServerData server = mc.getCurrentServer();
        if (server == null) {
            return "SinglePlayer";
        }
        return server.ip;
    }

    private int getDisplayedFps() {
        try {
            int actualFps = Integer.parseInt(StringUtils.split(mc.fpsString, " ")[0]);
            return actualFps;
        } catch (Exception e) {
            return Minecraft.getInstance().getFps();
        }
    }

    private void showToggleNotification(String title, String message, boolean enabled, String moduleName) {
        WaterMark2ToggleNotification existing = notifications.stream()
                .filter(n -> n.moduleName.equals(moduleName))
                .findFirst()
                .orElse(null);
        long duration = (long) notifyDuration.getCurrentValue();
        if (existing != null) {
            existing.updateState(message, enabled, duration);
        } else {
            notifications.add(new WaterMark2ToggleNotification(title, message, duration, enabled, moduleName,
                    this::drawToggleRow));
        }
    }

    private void updateNotifications() {
        long now = System.currentTimeMillis();
        for (WaterMark2ToggleNotification notification : notifications) {
            notification.update(now);
        }
        notifications.removeIf(n -> n.isMarkedForDelete);
    }

    private ChestLayout getChestLayout() {
        if (!(mc.player.containerMenu instanceof ChestMenu menu)) {
            return null;
        }
        int columns = 9;
        int rows = menu.getRowCount();
        int slotSize = 16;
        int padding = 8;
        float w = columns * slotSize + padding * 2.0f;
        float h = rows * slotSize + padding * 2.0f;
        float x = (screenWidth - w) / 2.0f;
        float y = Math.min(Math.max(startY, 5.0f), screenHeight - h - 5.0f);
        return new ChestLayout(x, y, w, h, padding, slotSize, rows);
    }

    private void drawChestItems(GuiGraphics graphics) {
        if (!(mc.player.containerMenu instanceof ChestMenu menu)) {
            return;
        }

        ChestLayout layout = getChestLayout();
        if (layout == null) {
            return;
        }

        try {
            Lighting.setupForFlatItems();
            int containerSlotCount = menu.getRowCount() * 9;
            for (int i = 0; i < containerSlotCount; i++) {
                Slot slot = menu.getSlot(i);
                if (slot == null) {
                    continue;
                }
                ItemStack stack = slot.getItem();
                if (stack.isEmpty()) {
                    continue;
                }
                int col = i % 9;
                int row = i / 9;
                int x = (int) (layout.x + layout.padding + col * layout.slotSize);
                int y = (int) (layout.y + layout.padding + row * layout.slotSize);
                graphics.renderItem(stack, x, y);
                graphics.renderItemDecorations(mc.font, stack, x, y);
            }
        } catch (Exception ignored) {
        } finally {
            Lighting.setupFor3DItems();
        }
    }

    private int clampColor(float value) {
        return Math.max(0, Math.min(255, Math.round(value)));
    }

    private enum RenderMode {
        NONE,
        SCAFFOLD,
        NOTIFY_STACK,
        NORMAL_OPAI
    }

    private static class Normal3Info {
        final float width;
        final float padding;
        final float elementSpacing;
        final float dotSpacing;
        final String username;
        final float clientNameWidth;
        final float usernameWidth;
        final String pingStr;
        final float pingTextWidth;
        final float toTextWidth;
        final String ipStr;
        final float serverIpWidth;
        final String fpsStr;
        final float fpsTextWidth;

        Normal3Info(float width, float padding, float elementSpacing, float dotSpacing, String username,
                    float clientNameWidth, float usernameWidth, String pingStr, float pingTextWidth, float toTextWidth,
                    String ipStr, float serverIpWidth, String fpsStr, float fpsTextWidth) {
            this.width = width;
            this.padding = padding;
            this.elementSpacing = elementSpacing;
            this.dotSpacing = dotSpacing;
            this.username = username;
            this.clientNameWidth = clientNameWidth;
            this.usernameWidth = usernameWidth;
            this.pingStr = pingStr;
            this.pingTextWidth = pingTextWidth;
            this.toTextWidth = toTextWidth;
            this.ipStr = ipStr;
            this.serverIpWidth = serverIpWidth;
            this.fpsStr = fpsStr;
            this.fpsTextWidth = fpsTextWidth;
        }
    }

    private static class ChestLayout {
        final float x;
        final float y;
        final float width;
        final float height;
        final int padding;
        final int slotSize;
        final int rows;

        ChestLayout(float x, float y, float width, float height, int padding, int slotSize, int rows) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.padding = padding;
            this.slotSize = slotSize;
            this.rows = rows;
        }
    }

    private void drawToggleRow(float startX, float startY, boolean moduleState,
                               WaterMark2Animation.SwitchAnimationState animationState, String title, String message) {
        drawToggleButton(startX, startY, moduleState, animationState);
        drawToggleText(startX, startY, title, message);
    }

    private void drawToggleButton(float startX, float startY, boolean moduleState, WaterMark2Animation.SwitchAnimationState animationState) {
        float btnH = 19.0f;
        float btnW = 30.0f;
        float margin = 3.0f;
        float radius = btnH / 2.0f;
        float btnStartY = startY + (ITEM_NOTIFY_HEIGHT - btnH) / 2.0f;
        animationState.updateState(moduleState);
        double anim = animationState.getOutput();
        Color trackColor = moduleState ? new Color(clampColor(buttonColorR.getCurrentValue()),
                clampColor(buttonColorG.getCurrentValue()),
                clampColor(buttonColorB.getCurrentValue()),
                clampColor(buttonColorA.getCurrentValue())) : new Color(45, 45, 45, 255);
        Skia.drawRoundedRect(startX, btnStartY, btnW, btnH, radius, trackColor);
        float knobSize = btnH - margin * 2.0f;
        float knobX = startX + margin + (btnW - margin * 2.0f - knobSize) * (float) anim;
        Color knobColor = moduleState ? Color.WHITE : new Color(100, 100, 100, 255);
        Skia.drawRoundedRect(knobX, btnStartY + margin, knobSize, knobSize, knobSize / 2.0f, knobColor);
    }

    private void drawToggleText(float startX, float startY, String title, String message) {
        float textStartX = startX + 30.0f + 8.0f;
        float center = startY + ITEM_NOTIFY_HEIGHT / 2.0f;
        Font titleFont = Fonts.getUrbanistVariable(15.0f);
        Font descFont = Fonts.getUrbanistVariable(12.0f);
        float totalHeight = WaterMark2TextMetrics.getFontHeight(titleFont) + WaterMark2TextMetrics.getFontHeight(descFont) + 2.0f;
        float top = center - totalHeight / 2.0f;
        float titleTop = top;
        float descTop = top + WaterMark2TextMetrics.getFontHeight(titleFont) + 2.0f;
        Skia.drawText(title, textStartX, titleTop, Color.WHITE, titleFont);
        Skia.drawText(message, textStartX, descTop, Color.WHITE, descFont);
    }
}

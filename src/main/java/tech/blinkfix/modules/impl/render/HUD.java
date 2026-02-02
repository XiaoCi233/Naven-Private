package tech.blinkfix.modules.impl.render;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.impl.EventAttack;
import tech.blinkfix.events.impl.EventRender2D;
import tech.blinkfix.events.impl.EventShader;
import tech.blinkfix.events.impl.FpsConfig;
import tech.blinkfix.events.impl.SettingChangeEvent;
import tech.blinkfix.ui.targethud.TargetHUD;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.modules.ModuleManager;
import tech.blinkfix.ui.Watermark.Watermark;
import tech.blinkfix.utils.RenderUtils;
import tech.blinkfix.utils.SmoothAnimationTimer;
import tech.blinkfix.utils.StencilUtils;
import tech.blinkfix.utils.SmtcUtils;
//import com.heypixel.heypixelmod.utils.localization.ModuleLanguageManager;
import tech.blinkfix.utils.renderer.Fonts;
import tech.blinkfix.utils.renderer.text.CustomTextRenderer;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;
import tech.blinkfix.values.impl.FloatValue;
import tech.blinkfix.values.impl.ModeValue;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector4f;

@ModuleInfo(
        name = "HUD",
        description = "Displays information on your screen",
        category = Category.RENDER
)
public class HUD extends Module {
    public static final int headerColor = new Color(150, 45, 45, 255).getRGB();
    public static final int bodyColor = new Color(0, 0, 0, 120).getRGB();
    public static final int backgroundColor = new Color(0, 0, 0, 60).getRGB();
    //    public static final int backgroundColor = new Color(0, 0, 0, 40).getRGB();
    private static final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    public BooleanValue waterMark = ValueBuilder.create(this, "Water Mark").setDefaultBooleanValue(true).build().getBooleanValue();
    public ModeValue watermarkStyle = ValueBuilder.create(this, "Watermark Style")
            .setVisibility(this.waterMark::getCurrentValue)
            .setDefaultModeIndex(0)
            .setModes("Rainbow", "Classic", "Capsule", "exhibition", "skeet", "Client", "Symmetry","BlinkFix","WanFan","Jello")
            .build()
            .getModeValue();
//    public FloatValue arrayListSpacing = ValueBuilder.create(this, "ArrayList Spacing")
//            .setVisibility(this.arrayList::getCurrentValue)
//            .setDefaultFloatValue(2.0F)
//            .setFloatStep(0.5F)
//            .setMinFloatValue(0.0F)
//            .setMaxFloatValue(10.0F)
//            .build()
//            .getFloatValue();
    public BooleanValue arrayList = ValueBuilder.create(this, "Array List").setDefaultBooleanValue(true).build().getBooleanValue();
//    public ModeValue arrayListMode = ValueBuilder.create(this, "ArrayList Mode")
//            .setVisibility(this.arrayList::getCurrentValue)
//            .setDefaultModeIndex(0)
//            .setModes("Normal", "FDP")
//            .build()
//            .getModeValue();

//    public BooleanValue arrayListCapsule = ValueBuilder.create(this, "ArrayList Capsule")
//            .setVisibility(this.arrayList::getCurrentValue)
//            .setDefaultBooleanValue(true)
//            .build()
//            .getBooleanValue();
    public FloatValue watermarkSize = ValueBuilder.create(this, "Watermark Size")
            .setVisibility(this.waterMark::getCurrentValue)
            .setDefaultFloatValue(0.4F)
            .setFloatStep(0.01F)
            .setMinFloatValue(0.1F)
            .setMaxFloatValue(1.0F)
            .build()
            .getFloatValue();
    public ModeValue rainbowMode = ValueBuilder.create(this, "Rainbow Mode")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.rainbow.getCurrentValue())
            .setDefaultModeIndex(0)
            .setModes("Gradient", "Solid", "Two Colors", "Three Colors")
            .build()
            .getModeValue();
    public BooleanValue moduleToggleSound = ValueBuilder.create(this, "Module Toggle Sound").setDefaultBooleanValue(true).build().getBooleanValue();
    public BooleanValue notification = ValueBuilder.create(this, "Notification").setDefaultBooleanValue(true).build().getBooleanValue();
    public ModeValue notificationStyle = ValueBuilder.create(this, "Notification Style")
            .setVisibility(this.notification::getCurrentValue)
            .setDefaultModeIndex(0)
            .setModes("Classic", "Capsule", "Jello", "New","Smooth")
            .build()
            .getModeValue();
    public BooleanValue fakeFps = ValueBuilder.create(this, "Fake FPS").setDefaultBooleanValue(true).build().getBooleanValue();
    public FloatValue fakefpssize = ValueBuilder.create(this, "Fake FPS Size")
            .setVisibility(this.fakeFps::getCurrentValue)
            .setDefaultFloatValue(200.0F)
            .setFloatStep(10.0F)
            .setMinFloatValue(1.0F)
            .setMaxFloatValue(500.0F)
            .build()
            .getFloatValue();
    public FloatValue watermarkCornerRadius = ValueBuilder.create(this, "Watermark Corner Radius")
            .setVisibility(this.waterMark::getCurrentValue)
            .setDefaultFloatValue(5.0F)
            .setFloatStep(0.5F)
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(20.0F)
            .build()
            .getFloatValue();
    public FloatValue watermarkVPadding = ValueBuilder.create(this, "Watermark V-Padding")
            .setVisibility(this.waterMark::getCurrentValue)
            .setDefaultFloatValue(4.0F)
            .setFloatStep(0.5F)
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(10.0F)
            .build()
            .getFloatValue();
    public BooleanValue renderBlackBackground = ValueBuilder.create(this, "RenderBlackBackGround")
            .setVisibility(() -> this.waterMark.getCurrentValue() && this.watermarkStyle.isCurrentMode("Capsule"))
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();
    public BooleanValue blackFont = ValueBuilder.create(this, "BlackFont")
            .setVisibility(() -> this.waterMark.getCurrentValue() && this.watermarkStyle.isCurrentMode("Capsule"))
            .setDefaultBooleanValue(false)
            .build()
            .getBooleanValue();
    public BooleanValue prettyModuleName = ValueBuilder.create(this, "Pretty Module Name")
            .setOnUpdate(value -> Module.update = true)
            .setVisibility(this.arrayList::getCurrentValue)
            .setDefaultBooleanValue(false)
            .build()
            .getBooleanValue();
    public BooleanValue hideRenderModules = ValueBuilder.create(this, "Hide Render Modules")
            .setOnUpdate(value -> Module.update = true)
            .setVisibility(this.arrayList::getCurrentValue)
            .setDefaultBooleanValue(false)
            .build()
            .getBooleanValue();
    public BooleanValue rainbow = ValueBuilder.create(this, "Rainbow")
            .setDefaultBooleanValue(true)
            .setVisibility(this.arrayList::getCurrentValue)
            .build()
            .getBooleanValue();
    public ModeValue arraymode = ValueBuilder.create(this, "ArrayList Mode")
            .setVisibility(this.arrayList::getCurrentValue)
            .setDefaultModeIndex(0)
            .setModes(new String[]{"Normal", "FDP","New","Jello"})
            .build()
            .getModeValue();
    public FloatValue rainbowSpeed = ValueBuilder.create(this, "Rainbow Speed")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.rainbow.getCurrentValue() && this.rainbowMode.isCurrentMode("Gradient"))
            .setMinFloatValue(1.0F)
            .setMaxFloatValue(20.0F)
            .setDefaultFloatValue(10.0F)
            .setFloatStep(0.1F)
            .build()
            .getFloatValue();
    public FloatValue rainbowOffset = ValueBuilder.create(this, "Rainbow Offset")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.rainbow.getCurrentValue() && this.rainbowMode.isCurrentMode("Gradient"))
            .setMinFloatValue(1.0F)
            .setMaxFloatValue(20.0F)
            .setDefaultFloatValue(10.0F)
            .setFloatStep(0.1F)
            .build()
            .getFloatValue();
    public FloatValue solidColorRed = ValueBuilder.create(this, "Solid Color Red")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.rainbow.getCurrentValue() && this.rainbowMode.isCurrentMode("Solid"))
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(255.0F)
            .setDefaultFloatValue(255.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();
    public FloatValue solidColorGreen = ValueBuilder.create(this, "Solid Color Green")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.rainbow.getCurrentValue() && this.rainbowMode.isCurrentMode("Solid"))
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(255.0F)
            .setDefaultFloatValue(255.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();
    public FloatValue solidColorBlue = ValueBuilder.create(this, "Solid Color Blue")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.rainbow.getCurrentValue() && this.rainbowMode.isCurrentMode("Solid"))
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(255.0F)
            .setDefaultFloatValue(255.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();
    public FloatValue twoColorsFirstRed = ValueBuilder.create(this, "Two Colors First Red")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.rainbow.getCurrentValue() && this.rainbowMode.isCurrentMode("Two Colors"))
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(255.0F)
            .setDefaultFloatValue(14.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();
    public FloatValue twoColorsFirstGreen = ValueBuilder.create(this, "Two Colors First Green")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.rainbow.getCurrentValue() && this.rainbowMode.isCurrentMode("Two Colors"))
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(255.0F)
            .setDefaultFloatValue(190.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();
    public FloatValue twoColorsFirstBlue = ValueBuilder.create(this, "Two Colors First Blue")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.rainbow.getCurrentValue() && this.rainbowMode.isCurrentMode("Two Colors"))
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(255.0F)
            .setDefaultFloatValue(255.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();
    public FloatValue twoColorsSecondRed = ValueBuilder.create(this, "Two Colors Second Red")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.rainbow.getCurrentValue() && this.rainbowMode.isCurrentMode("Two Colors"))
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(255.0F)
            .setDefaultFloatValue(255.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();
    public FloatValue twoColorsSecondGreen = ValueBuilder.create(this, "Two Colors Second Green")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.rainbow.getCurrentValue() && this.rainbowMode.isCurrentMode("Two Colors"))
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(255.0F)
            .setDefaultFloatValue(66.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();
    public FloatValue twoColorsSecondBlue = ValueBuilder.create(this, "Two Colors Second Blue")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.rainbow.getCurrentValue() && this.rainbowMode.isCurrentMode("Two Colors"))
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(255.0F)
            .setDefaultFloatValue(179.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();
    public FloatValue threeColorsFirstRed = ValueBuilder.create(this, "Three Colors First Red")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.rainbow.getCurrentValue() && this.rainbowMode.isCurrentMode("Three Colors"))
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(255.0F)
            .setDefaultFloatValue(255.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();
    public FloatValue threeColorsFirstGreen = ValueBuilder.create(this, "Three Colors First Green")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.rainbow.getCurrentValue() && this.rainbowMode.isCurrentMode("Three Colors"))
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(255.0F)
            .setDefaultFloatValue(0.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();
    public FloatValue threeColorsFirstBlue = ValueBuilder.create(this, "Three Colors First Blue")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.rainbow.getCurrentValue() && this.rainbowMode.isCurrentMode("Three Colors"))
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(255.0F)
            .setDefaultFloatValue(0.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();
    public FloatValue threeColorsSecondRed = ValueBuilder.create(this, "Three Colors Second Red")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.rainbow.getCurrentValue() && this.rainbowMode.isCurrentMode("Three Colors"))
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(255.0F)
            .setDefaultFloatValue(0.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();
    public FloatValue threeColorsSecondGreen = ValueBuilder.create(this, "Three Colors Second Green")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.rainbow.getCurrentValue() && this.rainbowMode.isCurrentMode("Three Colors"))
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(255.0F)
            .setDefaultFloatValue(255.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();
    public FloatValue threeColorsSecondBlue = ValueBuilder.create(this, "Three Colors Second Blue")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.rainbow.getCurrentValue() && this.rainbowMode.isCurrentMode("Three Colors"))
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(255.0F)
            .setDefaultFloatValue(0.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();
    public FloatValue threeColorsThirdRed = ValueBuilder.create(this, "Three Colors Third Red")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.rainbow.getCurrentValue() && this.rainbowMode.isCurrentMode("Three Colors"))
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(255.0F)
            .setDefaultFloatValue(0.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();
    public FloatValue threeColorsThirdGreen = ValueBuilder.create(this, "Three Colors Third Green")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.rainbow.getCurrentValue() && this.rainbowMode.isCurrentMode("Three Colors"))
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(255.0F)
            .setDefaultFloatValue(0.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();
    public FloatValue threeColorsThirdBlue = ValueBuilder.create(this, "Three Colors Third Blue")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.rainbow.getCurrentValue() && this.rainbowMode.isCurrentMode("Three Colors"))
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(255.0F)
            .setDefaultFloatValue(255.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();
    public ModeValue arrayListDirection = ValueBuilder.create(this, "ArrayList Direction")
            .setVisibility(this.arrayList::getCurrentValue)
            .setDefaultModeIndex(0)
            .setModes("Right", "Left")
            .build()
            .getModeValue();
    public FloatValue xOffset = ValueBuilder.create(this, "X Offset")
            .setVisibility(this.arrayList::getCurrentValue)
            .setMinFloatValue(-100.0F)
            .setMaxFloatValue(100.0F)
            .setDefaultFloatValue(1.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();
    public FloatValue yOffset = ValueBuilder.create(this, "Y Offset")
            .setVisibility(this.arrayList::getCurrentValue)
            .setMinFloatValue(1.0F)
            .setMaxFloatValue(100.0F)
            .setDefaultFloatValue(1.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();
    public FloatValue arrayListSize = ValueBuilder.create(this, "ArrayList Size")
            .setVisibility(this.arrayList::getCurrentValue)
            .setDefaultFloatValue(0.4F)
            .setFloatStep(0.01F)
            .setMinFloatValue(0.1F)
            .setMaxFloatValue(1.0F)
            .build()
            .getFloatValue();
    public FloatValue arrayListMargin = ValueBuilder.create(this, "ArrayList Margin")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.arraymode.isCurrentMode("Jello"))
            .setDefaultFloatValue(7.5F)
            .setFloatStep(0.1F)
            .setMinFloatValue(1.0F)
            .setMaxFloatValue(10.0F)
            .build()
            .getFloatValue();
    public BooleanValue arrayListShortLine = ValueBuilder.create(this, "ArrayList Short Line")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.arraymode.isCurrentMode("Jello"))
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();
    public BooleanValue arrayListRound = ValueBuilder.create(this, "ArrayList Round")
            .setVisibility(() -> this.arrayList.getCurrentValue() && this.arraymode.isCurrentMode("Jello"))
            .setDefaultBooleanValue(true)
            .build()
            .getBooleanValue();
    public BooleanValue music = ValueBuilder.create(this, "Music").setDefaultBooleanValue(true).build().getBooleanValue();
    public FloatValue musicX = ValueBuilder.create(this, "Music X")
            .setVisibility(this.music::getCurrentValue)
            .setDefaultFloatValue(10.0F)
            .setFloatStep(1.0F)
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(2000.0F)
            .build()
            .getFloatValue();
    public FloatValue musicY = ValueBuilder.create(this, "Music Y")
            .setVisibility(this.music::getCurrentValue)
            .setDefaultFloatValue(10.0F)
            .setFloatStep(1.0F)
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(2000.0F)
            .build()
            .getFloatValue();
    public FloatValue musicSize = ValueBuilder.create(this, "Music Size")
            .setVisibility(this.music::getCurrentValue)
            .setDefaultFloatValue(0.35F)
            .setFloatStep(0.01F)
            .setMinFloatValue(0.2F)
            .setMaxFloatValue(0.6F)
            .build()
            .getFloatValue();
    List<Module> renderModules;
    float width;
    float watermarkHeight;
    List<Vector4f> blurMatrices = new ArrayList<>();
    
    // Music progress bar animation
    private static final SmoothAnimationTimer musicProgressAnimation = new SmoothAnimationTimer(0, 0.2f);
    private static double lastMusicProgress = 0.0;

    public String getModuleDisplayName(Module module) {
        String name = this.prettyModuleName.getCurrentValue() ? module.getPrettyName() : module.getName();
        return name + (module.getSuffix() == null ? "" : " §7" + module.getSuffix());
    }

    private String formatPrettyName(String name) {
        StringBuilder builder = new StringBuilder();
        char[] chars = name.toCharArray();

        for (int i = 0; i < chars.length - 1; i++) {
            if (Character.isLowerCase(chars[i]) && Character.isUpperCase(chars[i + 1])) {
                builder.append(chars[i]).append(" ");
            } else {
                builder.append(chars[i]);
            }
        }

        builder.append(chars[chars.length - 1]);
        return builder.toString();
    }

    @EventTarget
    public void onSettingChange(SettingChangeEvent e) {
        if (e.getSetting() == this.fakeFps || e.getSetting() == this.fakefpssize) {
            FpsConfig.setFakeFps(
                    this.fakeFps.getCurrentValue(),
                    this.fakefpssize.getCurrentValue()
            );
        }
    }

    @EventTarget
    public void onAttack(EventAttack e) {
        if (e.getTarget() instanceof net.minecraft.world.entity.player.Player) {
            TargetHUD.setLastNavenTarget((net.minecraft.world.entity.player.Player) e.getTarget());
        }
    }
    
    @EventTarget
    public void notification(EventRender2D e) {
        if (this.notification.getCurrentValue()) {
            BlinkFix.getInstance().getNotificationManager().onRender(e);
        }
    }
    @EventTarget
    public void onShader(EventShader e) {
        if (e.getType() != EventType.SHADOW) {
            return;
        }

        // Notification Shadow
        if (this.notification.getCurrentValue()) {
            BlinkFix.getInstance().getNotificationManager().onRenderShadow(e);
        }

        // WaterMark Shadow
        if (this.waterMark.getCurrentValue()) {
            Watermark.onShader(e, this.watermarkStyle.getCurrentMode(), this.watermarkCornerRadius.getCurrentValue(), this.watermarkSize.getCurrentValue(), this.watermarkVPadding.getCurrentValue(), this.renderBlackBackground.getCurrentValue(), this.blackFont.getCurrentValue(), this.fakeFps.getCurrentValue(), this.fakefpssize.getCurrentValue());
        }

        // Music Shadow
        if (this.music.getCurrentValue()) {
            SmtcUtils.MediaInfo info = SmtcUtils.getMediaInfo();
            if (info.hasMedia()) {
                float musicWidth = Math.max(Fonts.harmony.getWidth(info.getTitle(), (double)this.musicSize.getCurrentValue()) + 10.0F, 60.0F);
                RenderUtils.drawRoundedRect(e.getStack(), this.musicX.getCurrentValue(), this.musicY.getCurrentValue(), musicWidth, 30.0F, 5.0F, Integer.MIN_VALUE);
            }
        }

        // ArrayList Shadow - 为所有模块背景渲染Shadow
        if (this.arrayList.getCurrentValue()) {
            for (Vector4f blurMatrix : this.blurMatrices) {
                RenderUtils.drawRoundedRect(e.getStack(), blurMatrix.x(), blurMatrix.y(), blurMatrix.z(), blurMatrix.w(), 3.0F, Integer.MIN_VALUE);
            }
        }
    }

    @EventTarget
    public void onRender(EventRender2D e) {
        if (this.waterMark.getCurrentValue()) {

            Watermark.onRender(e, this.watermarkSize.getCurrentValue(), this.watermarkStyle.getCurrentMode(), this.rainbow.getCurrentValue(), this.rainbowSpeed.getCurrentValue(), this.rainbowOffset.getCurrentValue(), this.watermarkCornerRadius.getCurrentValue(), this.watermarkVPadding.getCurrentValue(), this.renderBlackBackground.getCurrentValue(), this.blackFont.getCurrentValue(), this.fakeFps.getCurrentValue(), this.fakefpssize.getCurrentValue());
        }

//        if (this.arrayList.getCurrentValue()) {
//            ArrayList.ui.tech.blinkfix.ArrayList.onRender(e,this.arrayListMode.isCurrentMode("Exhibition") ? ArrayList.ui.tech.blinkfix.ArrayList.Mode.Exhibition : ArrayList.ui.tech.blinkfix.ArrayList.Mode.Normal, this.arrayListCapsule.getCurrentValue(), this.prettyModuleName.getCurrentValue(), this.hideRenderModules.getCurrentValue(), this.rainbow.getCurrentValue(), this.rainbowSpeed.getCurrentValue(), this.rainbowOffset.getCurrentValue(), this.arrayListDirection.getCurrentMode(), this.xOffset.getCurrentValue(), this.yOffset.getCurrentValue(), this.arrayListSize.getCurrentValue(), this.arrayListSpacing.getCurrentValue());
//        }
//    }
//
//    @Override
//    public void onEnable() {
//        super.onEnable();
//    }
//}

//    @EventTarget
//    public void onShader(EventShader e) {
//        if (this.notification.getCurrentValue() && e.getType() == EventType.SHADOW) {
//            tech.blinkfix.BlinkFix.getInstance().getNotificationManager().onRenderShadow(e);
//        }
//
//        if (this.waterMark.getCurrentValue()) {
//            RenderUtils.drawRoundedRect(e.getStack(), 5.0F, 5.0F, this.width, this.watermarkHeight + 8.0F, 5.0F, Integer.MIN_VALUE);
//        }
//
//        if (this.arrayList.getCurrentValue()) {
//            for (Vector4f blurMatrix : this.blurMatrices) {
//                RenderUtils.fillBound(e.getStack(), blurMatrix.x(), blurMatrix.y(), blurMatrix.z(), blurMatrix.w(), 1073741824);
//            }
//        }
//    }
//
//    @EventTarget
//    public void onRender(EventRender2D e) {
        CustomTextRenderer font = Fonts.harmony;
//        if (this.waterMark.getCurrentValue()) {
//            e.getStack().pushPose();
//            String username = LiveUtils.getCurrentUsername();
//            int actualFps = Integer.parseInt(StringUtils.split(mc.fpsString, " ")[0]);
//            int displayedFps = actualFps;
//            if (this.fakeFps.getCurrentValue()) {
//                displayedFps = actualFps + (int) this.fakefpssize.getCurrentValue();
//            }
//            String text = "BlinkFix | " + username + " | " + displayedFps + " FPS | " + format.format(new Date());
//            this.width = font.getWidth(text, (double) this.watermarkSize.getCurrentValue()) + 14.0F;
//            this.watermarkHeight = (float) font.getHeight(true, (double) this.watermarkSize.getCurrentValue());
//            StencilUtils.write(false);
//            RenderUtils.drawRoundedRect(e.getStack(), 5.0F, 5.0F, this.width, this.watermarkHeight + 8.0F, 5.0F, Integer.MIN_VALUE);
//            StencilUtils.erase(true);
//            RenderUtils.fill(e.getStack(), 5.0F, 5.0F, 9.0F + this.width, 8.0F, headerColor);
//            RenderUtils.fill(e.getStack(), 5.0F, 8.0F, 9.0F + this.width, 16.0F + this.watermarkHeight, bodyColor);
//            font.render(e.getStack(), text, 12.0, 10.0, Color.WHITE, true, (double) this.watermarkSize.getCurrentValue());
//            StencilUtils.dispose();
//            e.getStack().popPose();
//        }



        this.blurMatrices.clear();
        if (this.arrayList.getCurrentValue() && this.arraymode.isCurrentMode("FDP")) {
            e.getStack().pushPose();
            ModuleManager moduleManager = BlinkFix.getInstance().getModuleManager();
            if (update || this.renderModules == null) {
                this.renderModules = new ArrayList(moduleManager.getModules());
                if (this.hideRenderModules.getCurrentValue()) {
                    this.renderModules.removeIf((modulex) -> modulex.getCategory() == Category.RENDER);
                }

                this.renderModules.sort((o1, o2) -> {
                    String displayName1 = this.getModuleDisplayName(o1);
                    String displayName2 = this.getModuleDisplayName(o2);
                    float o1Width = font.getWidth(displayName1, (double) this.arrayListSize.getCurrentValue());
                    float o2Width = font.getWidth(displayName2, (double) this.arrayListSize.getCurrentValue());
                    return Float.compare(o2Width, o1Width);
                });
            }

            float maxWidth = this.renderModules.isEmpty() ? 0.0F :
                    font.getWidth(this.getModuleDisplayName((Module) this.renderModules.get(0)), (double) this.arrayListSize.getCurrentValue());

            float arrayListX = this.arrayListDirection.isCurrentMode("Right") ?
                    (float) mc.getWindow().getGuiScaledWidth() - maxWidth - 6.0F + this.xOffset.getCurrentValue() :
                    3.0F + this.xOffset.getCurrentValue();

            float arrayListY = this.yOffset.getCurrentValue();
            float height = 0.0F;
            double fontHeight = font.getHeight(true, (double) this.arrayListSize.getCurrentValue());

            for (Module module : this.renderModules) {
                SmoothAnimationTimer animation = module.getAnimation();
                if (module.isEnabled()) {
                    animation.target = 100.0F;
                } else {
                    animation.target = 0.0F;
                }

                animation.update(true);
                if (animation.value > 0.0F) {
                    String displayName = this.getModuleDisplayName(module);
                    float stringWidth = font.getWidth(displayName, (double) this.arrayListSize.getCurrentValue());
                    float left = -stringWidth * (1.0F - animation.value / 100.0F);
                    float right = maxWidth - stringWidth * (animation.value / 100.0F);
                    float innerX = this.arrayListDirection.isCurrentMode("Left") ? left : right;
                    float moduleHeight = (float) ((double) (animation.value / 100.0F) * fontHeight);
                    float moduleWidth = stringWidth + 6.0F;

                    RenderUtils.drawRoundedRect(e.getStack(), arrayListX + innerX, arrayListY + height + 2.0F, moduleWidth, moduleHeight, 3.0F, backgroundColor);
                    this.blurMatrices.add(new Vector4f(arrayListX + innerX, arrayListY + height + 2.0F, moduleWidth, moduleHeight));

                    int color = -1;
                    if (this.rainbow.getCurrentValue()) {
                        color = RenderUtils.getRainbowOpaque((int) (-height * this.rainbowOffset.getCurrentValue()), 1.0F, 1.0F, (21.0F - this.rainbowSpeed.getCurrentValue()) * 1000.0F);
                    }

                    float rectHeight = moduleHeight / 1.5F + 0.2F;
                    float rectY = (arrayListY + height + 2.0F) - (rectHeight - moduleHeight) / 2;
                    float rectX;
                    if (this.arrayListDirection.isCurrentMode("Right")) {
                        rectX = arrayListX + innerX + moduleWidth + 1.5F;
                    } else {
                        rectX = arrayListX + innerX - 3.0F - 1.5F;
                    }
                    RenderUtils.drawRoundedRect(e.getStack(), rectX, rectY, 3.0F, rectHeight, 3.0F, color);

                    float alpha = animation.value / 100.0F;
                    font.setAlpha(alpha);
                    float textX = arrayListX + innerX + (moduleWidth - stringWidth) / 2.0F;
                    float textY = arrayListY + height + 2.0F + (moduleHeight - (float) fontHeight) / 2.0F;

                    font.render(e.getStack(), displayName, (double) textX, (double) textY, new Color(color), true, (double) this.arrayListSize.getCurrentValue());
                    height += (float) ((double) (animation.value / 100.0F) * fontHeight) + 2.0F;
                }
            }

            font.setAlpha(1.0F);
            e.getStack().popPose();
        } else if (this.arrayList.getCurrentValue() && this.arraymode.isCurrentMode("Normal")) {
            e.getStack().pushPose();
            ModuleManager moduleManager = BlinkFix.getInstance().getModuleManager();
            if (update || this.renderModules == null) {
                this.renderModules = new ArrayList<>(moduleManager.getModules());
                if (this.hideRenderModules.getCurrentValue()) {
                    this.renderModules.removeIf(modulex -> modulex.getCategory() == Category.RENDER);
                }

                this.renderModules.sort((o1, o2) -> {
                    float o1Width = font.getWidth(this.getModuleDisplayName(o1), (double) this.arrayListSize.getCurrentValue());
                    float o2Width = font.getWidth(this.getModuleDisplayName(o2), (double) this.arrayListSize.getCurrentValue());
                    return Float.compare(o2Width, o1Width);
                });
            }

            float maxWidth = this.renderModules.isEmpty()
                    ? 0.0F
                    : font.getWidth(this.getModuleDisplayName(this.renderModules.get(0)), (double) this.arrayListSize.getCurrentValue());
            float arrayListX = this.arrayListDirection.isCurrentMode("Right")
                    ? (float) mc.getWindow().getGuiScaledWidth() - maxWidth - 6.0F + this.xOffset.getCurrentValue()
                    : 3.0F + this.xOffset.getCurrentValue();
            float arrayListY = this.yOffset.getCurrentValue();
            float height = 0.0F;
            double fontHeight = font.getHeight(true, (double) this.arrayListSize.getCurrentValue());

            for (Module module : this.renderModules) {
                SmoothAnimationTimer animation = module.getAnimation();
                if (module.isEnabled()) {
                    animation.target = 100.0F;
                } else {
                    animation.target = 0.0F;
                }

                animation.update(true);
                if (animation.value > 0.0F) {
                    String displayName = this.getModuleDisplayName(module);
                    float stringWidth = font.getWidth(displayName, (double) this.arrayListSize.getCurrentValue());
                    float left = -stringWidth * (1.0F - animation.value / 100.0F);
                    float right = maxWidth - stringWidth * (animation.value / 100.0F);
                    float innerX = this.arrayListDirection.isCurrentMode("Left") ? left : right;
                    RenderUtils.fillBound(
                            e.getStack(),
                            arrayListX + innerX,
                            arrayListY + height + 2.0F,
                            stringWidth + 3.0F,
                            (float) ((double) (animation.value / 100.0F) * fontHeight),
                            backgroundColor
                    );
                    this.blurMatrices
                            .add(
                                    new Vector4f(arrayListX + innerX, arrayListY + height + 2.0F, stringWidth + 3.0F, (float) ((double) (animation.value / 100.0F) * fontHeight))
                            );
                    int color = -1;
                    if (this.rainbow.getCurrentValue()) {
                        color = RenderUtils.getRainbowOpaque(
                                (int) (-height * this.rainbowOffset.getCurrentValue()), 1.0F, 1.0F, (21.0F - this.rainbowSpeed.getCurrentValue()) * 1000.0F
                        );
                    }

                    float alpha = animation.value / 100.0F;
                    font.setAlpha(alpha);
                    font.render(
                            e.getStack(),
                            displayName,
                            (double) (arrayListX + innerX + 1.5F),
                            (double) (arrayListY + height + 1.0F),
                            new Color(color),
                            true,
                            (double) this.arrayListSize.getCurrentValue()
                    );
                    height += (float) ((double) (animation.value / 100.0F) * fontHeight);
                }
            }

            font.setAlpha(1.0F);
            e.getStack().popPose();
        }else  if (this.arrayList.getCurrentValue() && this.arraymode.isCurrentMode("New")) {
            e.getStack().pushPose();
            ModuleManager moduleManager = BlinkFix.getInstance().getModuleManager();
            if (update || this.renderModules == null) {
                this.renderModules = new ArrayList<>(moduleManager.getModules());
                if (this.hideRenderModules.getCurrentValue()) {
                    this.renderModules.removeIf(modulex -> modulex.getCategory() == Category.RENDER);
                }
                this.renderModules.removeIf(modulex -> modulex.getHideInArrayList().getCurrentValue());

                this.renderModules.sort((o1, o2) -> {
                    float o1Width = font.getWidth(this.getModuleDisplayName(o1), (double)this.arrayListSize.getCurrentValue());
                    float o2Width = font.getWidth(this.getModuleDisplayName(o2), (double)this.arrayListSize.getCurrentValue());
                    return Float.compare(o2Width, o1Width);
                });
            }

            float maxWidth = this.renderModules.isEmpty()
                    ? 0.0F
                    : font.getWidth(this.getModuleDisplayName(this.renderModules.get(0)), (double)this.arrayListSize.getCurrentValue());
            float arrayListX = this.arrayListDirection.isCurrentMode("Right")
                    ? (float)mc.getWindow().getGuiScaledWidth() - maxWidth - 6.0F + this.xOffset.getCurrentValue()
                    : 3.0F + this.xOffset.getCurrentValue();
            float arrayListY = this.yOffset.getCurrentValue();
            float height = 0.0F;
            double fontHeight = font.getHeight(true, (double)this.arrayListSize.getCurrentValue());

            for (Module module : this.renderModules) {
                SmoothAnimationTimer animation = module.getAnimation();
                if (module.isEnabled()) {
                    animation.target = 100.0F;
                } else {
                    animation.target = 0.0F;
                }

                animation.update(true);
                if (animation.value > 0.0F) {
                    String displayName = this.getModuleDisplayName(module);
                    float stringWidth = font.getWidth(displayName, (double)this.arrayListSize.getCurrentValue());
                    float left = -stringWidth * (1.0F - animation.value / 100.0F);
                    float right = maxWidth - stringWidth * (animation.value / 100.0F);
                    float innerX = this.arrayListDirection.isCurrentMode("Left") ? left : right;
                    RenderUtils.drawRoundedRect(
                            e.getStack(),
                            arrayListX + innerX,
                            arrayListY + height + 2.0F,
                            stringWidth + 5.0F, // 增加2像素宽度以包含竖线
                            (float)((double)(animation.value / 100.0F) * fontHeight),
                            5.0F, // 圆角半径
                            backgroundColor
                    );
                    this.blurMatrices
                            .add(
                                    new Vector4f(arrayListX + innerX, arrayListY + height + 2.0F, stringWidth + 5.0F, (float)((double)(animation.value / 100.0F) * fontHeight))
                            );
                    int color = -1;
                    if (this.rainbow.getCurrentValue()) {
                        String mode = this.rainbowMode.getCurrentMode();
                        float speed = (21.0F - this.rainbowSpeed.getCurrentValue()) * 1000.0F;
                        int index = (int)(-height * this.rainbowOffset.getCurrentValue());

                        switch (mode) {
                            case "Gradient":
                                color = RenderUtils.getCustomGradientOpaque(index, speed);
                                break;
                            case "Solid":
                                color = RenderUtils.getSolidColor(
                                        (int)this.solidColorRed.getCurrentValue(),
                                        (int)this.solidColorGreen.getCurrentValue(),
                                        (int)this.solidColorBlue.getCurrentValue()
                                );
                                break;
                            case "Two Colors":
                                Color color1 = new Color(
                                        (int)this.twoColorsFirstRed.getCurrentValue(),
                                        (int)this.twoColorsFirstGreen.getCurrentValue(),
                                        (int)this.twoColorsFirstBlue.getCurrentValue()
                                );
                                Color color2 = new Color(
                                        (int)this.twoColorsSecondRed.getCurrentValue(),
                                        (int)this.twoColorsSecondGreen.getCurrentValue(),
                                        (int)this.twoColorsSecondBlue.getCurrentValue()
                                );
                                color = RenderUtils.getTwoColorsGradient(index, speed, color1, color2);
                                break;
                            case "Three Colors":
                                Color color1_3 = new Color(
                                        (int)this.threeColorsFirstRed.getCurrentValue(),
                                        (int)this.threeColorsFirstGreen.getCurrentValue(),
                                        (int)this.threeColorsFirstBlue.getCurrentValue()
                                );
                                Color color2_3 = new Color(
                                        (int)this.threeColorsSecondRed.getCurrentValue(),
                                        (int)this.threeColorsSecondGreen.getCurrentValue(),
                                        (int)this.threeColorsSecondBlue.getCurrentValue()
                                );
                                Color color3_3 = new Color(
                                        (int)this.threeColorsThirdRed.getCurrentValue(),
                                        (int)this.threeColorsThirdGreen.getCurrentValue(),
                                        (int)this.threeColorsThirdBlue.getCurrentValue()
                                );
                                color = RenderUtils.getThreeColorsGradient(index, speed, color1_3, color2_3, color3_3);
                                break;
                            default:
                                color = RenderUtils.getCustomGradientOpaque(index, speed);
                                break;
                        }
                    }

                    float alpha = animation.value / 100.0F;
                    font.setAlpha(alpha);
                    font.render(
                            e.getStack(),
                            displayName,
                            (double)(arrayListX + innerX + 1.5F),
                            (double)(arrayListY + height + 1.0F),
                            new Color(color),
                            true,
                            (double)this.arrayListSize.getCurrentValue()
                    );
                    float lineWidth = 2.0F;
                    float lineX = arrayListX + innerX + stringWidth + 5.0F - lineWidth;
                    float lineY = arrayListY + height + 2.0F + 2.0F;
                    float lineHeight = (float)((double)(animation.value / 100.0F) * fontHeight) - 4.0F;
                    if (lineHeight > 0) {
                        RenderUtils.drawRoundedRect(e.getStack(), lineX, lineY, lineWidth, lineHeight, 1.0F, color);
                    }

                    height += (float)((double)(animation.value / 100.0F) * fontHeight);
                    height += 2.0F;
                }
            }

            font.setAlpha(1.0F);
            e.getStack().popPose();
        } else if (this.arrayList.getCurrentValue() && this.arraymode.isCurrentMode("Jello")) {
            // Jello mode ArrayList rendering
            CustomTextRenderer jelloFont = Fonts.harmony; // Use harmony instead of MiSans_Medium
            e.getStack().pushPose();
            ModuleManager moduleManager = BlinkFix.getInstance().getModuleManager();
            if (update || this.renderModules == null) {
                this.renderModules = new ArrayList<>(moduleManager.getModules());
                if (this.hideRenderModules.getCurrentValue()) {
                    this.renderModules.removeIf(modulex -> modulex.getCategory() == Category.RENDER);
                }

                this.renderModules.sort((o1, o2) -> {
                    float o1Width = jelloFont.getWidth(this.getModuleDisplayName(o1), this.arrayListSize.getCurrentValue());
                    float o2Width = jelloFont.getWidth(this.getModuleDisplayName(o2), this.arrayListSize.getCurrentValue());
                    return Float.compare(o2Width, o1Width);
                });
            }

            float maxWidth = this.renderModules.isEmpty()
                    ? 0.0F
                    : jelloFont.getWidth(this.getModuleDisplayName(this.renderModules.get(0)), this.arrayListSize.getCurrentValue());
            float arrayListX = this.arrayListDirection.isCurrentMode("Right")
                    ? (float) mc.getWindow().getGuiScaledWidth() - maxWidth - 6.0F + this.xOffset.getCurrentValue()
                    : 3.0F + this.xOffset.getCurrentValue();
            float arrayListY = this.yOffset.getCurrentValue();
            float height = 0.0F;
            double fontHeight = jelloFont.getHeight(true, this.arrayListSize.getCurrentValue());

            for (Module module : this.renderModules) {
                SmoothAnimationTimer animation = module.getAnimation();
                if (module.isEnabled()) {
                    animation.target = 100.0F;
                } else {
                    animation.target = 0.0F;
                }

                animation.update(true);
                if (animation.value > 0.0F) {
                    String displayName = this.getModuleDisplayName(module);
                    float stringWidth = jelloFont.getWidth(displayName, this.arrayListSize.getCurrentValue());
                    float left = -stringWidth * (1.0F - animation.value / 100.0F);
                    float right = maxWidth - stringWidth * (animation.value / 100.0F);
                    float innerX = this.arrayListDirection.isCurrentMode("Left") ? left : right;
                    float margin = arrayListSize.getCurrentValue() * arrayListMargin.getCurrentValue();

                    if (arrayListRound.getCurrentValue()) {
                        StencilUtils.write(false);
                        if (this.arrayListDirection.isCurrentMode("Right")) {
                            RenderUtils.drawRoundedRect(e.getStack(), arrayListX + innerX, arrayListY + height, stringWidth + margin * 2.0f + 1.0f + margin, (float) ((animation.value / 100.0F) * (fontHeight + margin * 2.0f)), margin, -1);
                        } else {
                            RenderUtils.drawRoundedRect(e.getStack(), arrayListX + innerX - margin, arrayListY + height, stringWidth + margin * 2.0f + 1.0f + margin, (float) ((animation.value / 100.0F) * (fontHeight + margin * 2.0f)), margin, -1);
                        }
                        StencilUtils.erase(true);
                        RenderUtils.fillBound(e.getStack(), arrayListX + innerX, arrayListY + height, stringWidth + margin * 2.0f + 1.0f, (float) ((animation.value / 100.0F) * (fontHeight + margin * 2.0f)), bodyColor);
                        StencilUtils.dispose();
                    } else {
                        RenderUtils.fillBound(e.getStack(), arrayListX + innerX, arrayListY + height, stringWidth + margin * 2.0f + 1.0f, (float) ((animation.value / 100.0F) * (fontHeight + margin * 2.0f)), bodyColor);
                    }

                    this.blurMatrices
                            .add(
                                    new Vector4f(
                                            arrayListX + innerX,
                                            arrayListY + height,
                                            stringWidth + margin * 2.0f + 1.0f,
                                            (float) ((animation.value / 100.0F) * (fontHeight + margin * 2.0f))
                                    )
                            );
                    int color = -1;
                    if (this.rainbow.getCurrentValue()) {
                        color = RenderUtils.getRainbowOpaque(
                                (int) (-height * this.rainbowOffset.getCurrentValue()), 1.0F, 1.0F, (21.0F - this.rainbowSpeed.getCurrentValue()) * 1000.0F
                        );
                    }

                    if (arrayListShortLine.getCurrentValue()) {
                        RenderUtils.fillBound(e.getStack(), arrayListX + innerX + stringWidth + margin * 2.0f, arrayListY + height + margin, 2.0F, (float) ((animation.value / 100.0F) * fontHeight), color);
                    }

                    float alpha = animation.value / 100.0F;
                    jelloFont.setAlpha(alpha);
                    jelloFont.render(
                            e.getStack(),
                            displayName,
                            arrayListX + innerX + margin,
                            arrayListY + height + margin,
                            new Color(color),
                            true,
                            this.arrayListSize.getCurrentValue()
                    );
                    height += (float) ((double) (animation.value / 100.0F) * (fontHeight + margin * 2.0f));
                }
            }

            jelloFont.setAlpha(1.0F);
            e.getStack().popPose();
        }
        
        // Render Music Display
        if (this.music.getCurrentValue()) {
            renderMusicDisplay(e);
        }
    }
    
    /**
     * 渲染音乐显示组件（Naven 风格）
     */
    private void renderMusicDisplay(EventRender2D e) {
        try {
            SmtcUtils.MediaInfo info = SmtcUtils.getMediaInfo();
            if (info == null) {
                // 如果 info 为 null，可能是 SmtcUtils 初始化失败
                // 可以在这里显示一个提示信息
                return;
            }
            
            // 检查是否有有效的媒体信息
            if (!info.hasMedia()) {
                // 没有媒体播放，直接返回
                return;
            }
            
            String title = info.getTitle();
            if (title == null || title.isEmpty() || title.equals("SMTC not found") || title.equals("No media") || title.startsWith("Error")) {
                return;
            }
            
            float scale = this.musicSize.getCurrentValue();
            float x = this.musicX.getCurrentValue();
            float y = this.musicY.getCurrentValue();
            
            // 自适应宽度
            float titleWidth = 0.0F;
            try {
                titleWidth = Fonts.harmony.getWidth(title, (double)scale);
            } catch (Exception ex) {
                // 如果获取宽度失败，使用默认值
                titleWidth = 100.0F;
            }
            float width = Math.max(titleWidth + 10.0F, 60.0F);
            float height = 30.0F;
            
            com.mojang.blaze3d.vertex.PoseStack stack = e.getStack();
            boolean pushed = false;
            // 计算进度百分比，确保在有效范围内
            double progress = 0.0;
            try {
                double percent = info.getProgressPercent();
                progress = Math.max(0.0, Math.min(1.0, percent / 100.0));
            } catch (Exception ex) {
                progress = 0.0;
            }
            
            try {
                stack.pushPose();
                pushed = true;
                
                // 更新进度条动画（类似血量减少动画）
                // 当进度跳变（比如切歌）时，使用动画平滑过渡
                try {
                    if (Math.abs(progress - lastMusicProgress) > 0.1) {
                        // 进度跳变时，使用动画平滑过渡
                        musicProgressAnimation.target = (float)progress;
                        musicProgressAnimation.update(true);
                    } else {
                        // 正常播放时，平滑跟随
                        musicProgressAnimation.target = (float)progress;
                        musicProgressAnimation.update(true);
                    }
                    lastMusicProgress = progress;
                } catch (Exception ex) {
                    // 动画更新失败，使用当前进度
                    musicProgressAnimation.target = (float)progress;
                    musicProgressAnimation.value = (float)progress;
                    lastMusicProgress = progress;
                }
            
                // 使用 Stencil 绘制圆角背景（Naven 风格）
                try {
                StencilUtils.write(false);
                RenderUtils.drawRoundedRect(stack, x, y, width, height, 5.0F, headerColor);
                StencilUtils.erase(true);
                RenderUtils.fillBound(stack, x, y, width, height, bodyColor);
                
                // 绘制进度条背景（灰色）
                RenderUtils.drawRoundedRect(stack, x, y, width, 3.0F, 2.0F, new Color(100, 100, 100, 150).getRGB());
                
                // 绘制减少的进度条（灰色，类似血量减少效果）
                float currentProgress = Math.max(0.0F, Math.min(1.0F, musicProgressAnimation.value));
                float targetProgress = (float)progress;
                if (currentProgress > targetProgress) {
                    float damageWidth = width * (currentProgress - targetProgress);
                    float damageX = x + width * targetProgress;
                    if (damageWidth > 0) {
                        damageWidth += 2.0F;
                        damageX -= 1.0F;
                        if (damageWidth < 4.0F) damageWidth = 4.0F;
                        RenderUtils.drawRoundedRect(stack, damageX, y, damageWidth, 3.0F, 2.0F, new Color(150, 150, 150, 180).getRGB());
                    }
                }
                
                // 绘制当前进度条（红色，类似 Naven 风格的血量条）
                float progressBarWidth = width * targetProgress;
                if (progressBarWidth > 0) {
                    if (progressBarWidth < 4.0F) progressBarWidth = 4.0F;
                    float extendedWidth = progressBarWidth + 1.0F;
                    RenderUtils.drawRoundedRect(stack, x, y, extendedWidth, 3.0F, 2.0F, headerColor);
                }
                
                    StencilUtils.dispose();
                } catch (Exception ex) {
                    // 渲染失败时清理 Stencil
                    try {
                        StencilUtils.dispose();
                    } catch (Exception ignored) {}
                }
                
                // 渲染标题
                try {
                    Fonts.harmony.render(stack, title, (double)(x + 5.0F), (double)(y + 6.0F), Color.WHITE, true, (double)scale);
                } catch (Exception ex) {
                    // 标题渲染失败，跳过
                }
                
                // 渲染时间信息
                try {
                    String timeText = info.getFormattedPosition() + " / " + info.getFormattedDuration();
                    Fonts.harmony.render(
                        stack,
                        timeText,
                        (double)(x + 5.0F),
                        (double)(y + 17.0F),
                        Color.WHITE,
                        true,
                        (double)scale
                    );
                } catch (Exception ex) {
                    // 时间渲染失败，跳过
                }
            } catch (Exception ex) {
                // 整个渲染过程出错，打印错误但不崩溃
                // 不打印堆栈，避免日志污染（如果 SMTC 库未安装会频繁报错）
            } finally {
                // 确保 PoseStack 被正确恢复
                try {
                    if (pushed) {
                        e.getStack().popPose();
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ex) {
            // 最外层异常处理，确保不会崩溃
        }
    }
}
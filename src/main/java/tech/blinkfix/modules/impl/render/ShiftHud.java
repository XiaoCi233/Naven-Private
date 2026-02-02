//package com.heypixel.heypixelmod.modules.impl.render;
//
//import tech.blinkfix.BlinkFix;
//import api.events.tech.blinkfix.EventTarget;
//import types.api.events.tech.blinkfix.EventType;
//import impl.events.tech.blinkfix.EventRender2D;
//import impl.events.tech.blinkfix.EventShader;
//import impl.events.tech.blinkfix.FpsConfig;
//import impl.events.tech.blinkfix.SettingChangeEvent;
//import modules.tech.blinkfix.Category;
//import modules.tech.blinkfix.Module;
//import modules.tech.blinkfix.ModuleInfo;
//import modules.tech.blinkfix.ModuleManager;
//import Watermark.ui.tech.blinkfix.Watermark;
//import utils.tech.blinkfix.RenderUtils;
//import utils.tech.blinkfix.SmoothAnimationTimer;
//import utils.tech.blinkfix.StencilUtils;
//import com.heypixel.heypixelmod.utils.localization.ModuleLanguageManager;
//import renderer.utils.tech.blinkfix.Fonts;
//import text.renderer.utils.tech.blinkfix.CustomTextRenderer;
//import values.tech.blinkfix.ValueBuilder;
//import impl.values.tech.blinkfix.BooleanValue;
//import impl.values.tech.blinkfix.FloatValue;
//import impl.values.tech.blinkfix.ModeValue;
//
//import java.awt.Color;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//import dev.yalan.live.LiveUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.joml.Vector4f;
//
//@ModuleInfo(
//        name = "HUD",
//        description = "Displays information on your screen",
//        category = Category.RENDER
//)
//public class HUD extends Module {
//    public static final int headerColor = new Color(150, 45, 45, 255).getRGB();
//    public static final int bodyColor = new Color(0, 0, 0, 120).getRGB();
//    public static final int backgroundColor = new Color(0, 0, 0, 40).getRGB();
//    public BooleanValue waterMark = ValueBuilder.create(this, "Water Mark").setDefaultBooleanValue(true).build().getBooleanValue();
//
//    public ModeValue watermarkStyle = ValueBuilder.create(this, "Watermark Style")
//            .setVisibility(this.waterMark::getCurrentValue)
//            .setDefaultModeIndex(0)
//            .setModes("Rainbow", "Classic", "Capsule", "exhibition", "skeet")
//            .build()
//            .getModeValue();
//
//    public FloatValue watermarkSize = ValueBuilder.create(this, "Watermark Size")
//            .setVisibility(this.waterMark::getCurrentValue)
//            .setDefaultFloatValue(0.4F)
//            .setFloatStep(0.01F)
//            .setMinFloatValue(0.1F)
//            .setMaxFloatValue(1.0F)
//            .build()
//            .getFloatValue();
//    public FloatValue watermarkCornerRadius = ValueBuilder.create(this, "Watermark Corner Radius")
//            .setVisibility(this.waterMark::getCurrentValue)
//            .setDefaultFloatValue(5.0F)
//            .setFloatStep(0.5F)
//            .setMinFloatValue(0.0F)
//            .setMaxFloatValue(20.0F)
//            .build()
//            .getFloatValue();
//    public FloatValue watermarkVPadding = ValueBuilder.create(this, "Watermark V-Padding")
//            .setVisibility(this.waterMark::getCurrentValue)
//            .setDefaultFloatValue(4.0F)
//            .setFloatStep(0.5F)
//            .setMinFloatValue(0.0F)
//            .setMaxFloatValue(10.0F)
//            .build()
//            .getFloatValue();
//    public BooleanValue renderBlackBackground = ValueBuilder.create(this, "RenderBlackBackGround")
//            .setVisibility(() -> this.waterMark.getCurrentValue() && this.watermarkStyle.isCurrentMode("Capsule"))
//            .setDefaultBooleanValue(true)
//            .build()
//            .getBooleanValue();
//    public BooleanValue blackFont = ValueBuilder.create(this, "BlackFont")
//            .setVisibility(() -> this.waterMark.getCurrentValue() && this.watermarkStyle.isCurrentMode("Capsule"))
//            .setDefaultBooleanValue(false)
//            .build()
//            .getBooleanValue();
//    public BooleanValue moduleToggleSound = ValueBuilder.create(this, "Module Toggle Sound").setDefaultBooleanValue(true).build().getBooleanValue();
//    public BooleanValue notification = ValueBuilder.create(this, "Notification").setDefaultBooleanValue(true).build().getBooleanValue();
//    public BooleanValue arrayList = ValueBuilder.create(this, "Array List").setDefaultBooleanValue(true).build().getBooleanValue();
//    public ModeValue arrayListMode = ValueBuilder.create(this, "ArrayList Mode")
//            .setVisibility(this.arrayList::getCurrentValue)
//            .setDefaultModeIndex(0)
//            .setModes("Normal", "Exhibition")
//            .build()
//            .getModeValue();
//
//    public BooleanValue arrayListCapsule = ValueBuilder.create(this, "ArrayList Capsule")
//            .setVisibility(this.arrayList::getCurrentValue)
//            .setDefaultBooleanValue(true)
//            .build()
//            .getBooleanValue();
//    public BooleanValue prettyModuleName = ValueBuilder.create(this, "Pretty Module Name")
//            .setOnUpdate(value -> Module.update = true)
//            .setVisibility(this.arrayList::getCurrentValue)
//            .setDefaultBooleanValue(false)
//            .build()
//            .getBooleanValue();
//    public BooleanValue hideRenderModules = ValueBuilder.create(this, "Hide Render Modules")
//            .setOnUpdate(value -> Module.update = true)
//            .setVisibility(this.arrayList::getCurrentValue)
//            .setDefaultBooleanValue(false)
//            .build()
//            .getBooleanValue();
//    public BooleanValue rainbow = ValueBuilder.create(this, "Rainbow")
//            .setDefaultBooleanValue(true)
//            .setVisibility(this.arrayList::getCurrentValue)
//            .build()
//            .getBooleanValue();
//    public FloatValue rainbowSpeed = ValueBuilder.create(this, "Rainbow Speed")
//            .setVisibility(this.arrayList::getCurrentValue)
//            .setMinFloatValue(1.0F)
//            .setMaxFloatValue(20.0F)
//            .setDefaultFloatValue(10.0F)
//            .setFloatStep(0.1F)
//            .build()
//            .getFloatValue();
//    public FloatValue rainbowOffset = ValueBuilder.create(this, "Rainbow Offset")
//            .setVisibility(this.arrayList::getCurrentValue)
//            .setMinFloatValue(1.0F)
//            .setMaxFloatValue(20.0F)
//            .setDefaultFloatValue(10.0F)
//            .setFloatStep(0.1F)
//            .build()
//            .getFloatValue();
//    public ModeValue arrayListDirection = ValueBuilder.create(this, "ArrayList Direction")
//            .setVisibility(this.arrayList::getCurrentValue)
//            .setDefaultModeIndex(0)
//            .setModes("Right", "Left")
//            .build()
//            .getModeValue();
//    public FloatValue xOffset = ValueBuilder.create(this, "X Offset")
//            .setVisibility(this.arrayList::getCurrentValue)
//            .setMinFloatValue(-100.0F)
//            .setMaxFloatValue(100.0F)
//            .setDefaultFloatValue(1.0F)
//            .setFloatStep(1.0F)
//            .build()
//            .getFloatValue();
//    public FloatValue yOffset = ValueBuilder.create(this, "Y Offset")
//            .setVisibility(this.arrayList::getCurrentValue)
//            .setMinFloatValue(1.0F)
//            .setMaxFloatValue(100.0F)
//            .setDefaultFloatValue(1.0F)
//            .setFloatStep(1.0F)
//            .build()
//            .getFloatValue();
//    public FloatValue arrayListSize = ValueBuilder.create(this, "ArrayList Size")
//            .setVisibility(this.arrayList::getCurrentValue)
//            .setDefaultFloatValue(0.4F)
//            .setFloatStep(0.01F)
//            .setMinFloatValue(0.1F)
//            .setMaxFloatValue(1.0F)
//            .build()
//            .getFloatValue();
//    // 新增：ArrayList中模块之间的垂直间距
//    public FloatValue arrayListSpacing = ValueBuilder.create(this, "ArrayList Spacing")
//            .setVisibility(this.arrayList::getCurrentValue)
//            .setDefaultFloatValue(2.0F)
//            .setFloatStep(0.5F)
//            .setMinFloatValue(0.0F)
//            .setMaxFloatValue(10.0F)
//            .build()
//            .getFloatValue();
//    List<Module> renderModules;
//    float width;
//    float watermarkHeight;
//    List<Vector4f> blurMatrices = new ArrayList<>();
//
//    @EventTarget
//    public void notification(EventRender2D e) {
//        if (this.notification.getCurrentValue()) {
//            BlinkFix.getInstance().getNotificationManager().onRender(e);
//        }
//    }
//
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
//
//        if (this.waterMark.getCurrentValue()) {
//            Watermark.onShader(e, this.watermarkStyle.getCurrentMode(), this.watermarkCornerRadius.getCurrentValue(), this.watermarkSize.getCurrentValue(), this.watermarkVPadding.getCurrentValue(), this.renderBlackBackground.getCurrentValue(), this.blackFont.getCurrentValue());
//        }
//
//        // 仅在 BLUR 通道为ArrayList背景板写入模糊蒙版
//        if (this.arrayList.getCurrentValue() && e.getType() == EventType.BLUR) {
//            ArrayList.ui.tech.blinkfix.ArrayList.onShader(e);
//        }
//    }
//
//    @EventTarget
//    public void onRender(EventRender2D e) {
//        if (this.waterMark.getCurrentValue()) {
//            // 传递彩虹效果和新的padding相关参数到Watermark
//            Watermark.onRender(e, this.watermarkSize.getCurrentValue(), this.watermarkStyle.getCurrentMode(), this.rainbow.getCurrentValue(), this.rainbowSpeed.getCurrentValue(), this.rainbowOffset.getCurrentValue(), this.watermarkCornerRadius.getCurrentValue(), this.watermarkVPadding.getCurrentValue(), this.renderBlackBackground.getCurrentValue(), this.blackFont.getCurrentValue());
//        }
//
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
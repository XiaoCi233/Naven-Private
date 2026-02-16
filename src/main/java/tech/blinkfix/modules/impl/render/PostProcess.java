package tech.blinkfix.modules.impl.render;

import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.impl.EventRender2D;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.utils.ChatUtils;
import tech.blinkfix.utils.renderer.BlurUtils;
import tech.blinkfix.utils.renderer.ShadowUtils;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;
import tech.blinkfix.values.impl.FloatValue;
import org.lwjgl.opengl.GL11;

@ModuleInfo(
   name = "PostProcess",
   description = "Post process effects",
   category = Category.RENDER
)
public class PostProcess extends Module {
   private static PostProcess instance;
   private boolean disableBlur = false;
   private boolean disableBloom = false;
   private final BooleanValue blur = ValueBuilder.create(this, "Blur").setDefaultBooleanValue(true).build().getBooleanValue();
   private final FloatValue blurFPS = ValueBuilder.create(this, "Blur FPS")
           .setVisibility(this.blur::getCurrentValue)
           .setFloatStep(1.0F)
           .setDefaultFloatValue(90.0F)
           .setMinFloatValue(15.0F)
           .setMaxFloatValue(120.0F)
           .build()
           .getFloatValue();
   private final BooleanValue fastBlur = ValueBuilder.create(this, "FastBlur")
           .setDefaultBooleanValue(false)
           .build()
           .getBooleanValue();
   private final FloatValue strength = ValueBuilder.create(this, "Blur Strength")
           .setVisibility(this.blur::getCurrentValue)
           .setDefaultFloatValue(2.0F)
           .setMinFloatValue(0.0F)
           .setMaxFloatValue(19.0F)
           .setFloatStep(1.0F)
           .build()
           .getFloatValue();
   private final BooleanValue bloom = ValueBuilder.create(this, "Bloom").setDefaultBooleanValue(true).build().getBooleanValue();
   private final FloatValue bloomFPS = ValueBuilder.create(this, "Bloom FPS")
           .setVisibility(this.bloom::getCurrentValue)
           .setFloatStep(1.0F)
           .setDefaultFloatValue(90.0F)
           .setMinFloatValue(15.0F)
           .setMaxFloatValue(120.0F)
           .build()
           .getFloatValue();
   private final FloatValue bloomStrength = ValueBuilder.create(this, "Bloom Strength")
           .setVisibility(this.bloom::getCurrentValue)
           .setDefaultFloatValue(5.0F)
           .setMinFloatValue(1.0F)
           .setMaxFloatValue(10.0F)
           .setFloatStep(0.5F)
           .build()
           .getFloatValue();
   private final BooleanValue glow = ValueBuilder.create(this, "Glow")
           .setDefaultBooleanValue(true)
           .build()
           .getBooleanValue();

   @EventTarget(0)
   public void onRender(EventRender2D e) {
      if (this.blur.getCurrentValue() && !this.disableBlur) {
         GL11.glGetError();
         BlurUtils.onRenderAfterWorld(e, this.blurFPS.getCurrentValue(), (int) this.strength.getCurrentValue());
         if (GL11.glGetError() != 0) {
            ChatUtils.addChatMessage("由于错误而禁用blur.");
            this.disableBlur = true;
         }
      }

      if (this.bloom.getCurrentValue() && !this.disableBloom) {
         // 先检查 ShadowUtils 是否可用
         if (!ShadowUtils.isAvailable()) {
            // 尝试渲染
            GL11.glGetError();
            ShadowUtils.onRenderAfterWorld(e, this.bloomFPS.getCurrentValue(), this.bloomStrength.getCurrentValue());

            // 检查是否有错误或者仍然不可用
            if (GL11.glGetError() != 0 || !ShadowUtils.isAvailable()) {
               ChatUtils.addChatMessage("由于错误而禁用bloom (可能是核显不兼容).");
               this.disableBloom = true;
            }
         } else {
            // 正常渲染
            ShadowUtils.onRenderAfterWorld(e, this.bloomFPS.getCurrentValue(), this.bloomStrength.getCurrentValue());
         }
      }
   }
   public boolean getFastBlur() {
      return fastBlur.getCurrentValue();
   }
   public int getBlurFPS() {
      return (int) blurFPS.getCurrentValue();
   }
   public static boolean isGlowEnabled() {
      return instance != null && instance.glow.getCurrentValue();
   }
}


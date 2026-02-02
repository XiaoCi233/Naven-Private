package tech.blinkfix.events.impl;

import tech.blinkfix.events.api.events.Event;
import tech.blinkfix.events.api.types.EventType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import java.math.BigInteger;

public class EventShader implements Event {
   public static Object trash = new BigInteger("fffffffffffffffffffffffffffffffaaffffffffffffffafffaffff09ffcfff", 16);
   private final PoseStack stack;
   private final GuiGraphics graphics;
   private final EventType type;

   public PoseStack getStack() {
      return this.stack;
   }

   public GuiGraphics getGraphics() {
      return this.graphics;
   }

   public EventType getType() {
      return this.type;
   }

   public EventShader(PoseStack stack, GuiGraphics graphics, EventType type) {
      this.stack = stack;
      this.graphics = graphics;
      this.type = type;
   }
}

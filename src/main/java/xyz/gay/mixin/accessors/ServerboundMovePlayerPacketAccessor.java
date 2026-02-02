package xyz.gay.mixin.accessors;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ServerboundMovePlayerPacket.class})
public interface ServerboundMovePlayerPacketAccessor {
   @Accessor float getYRot();
   @Accessor void setYRot(float var1);
   @Accessor float getXRot();
   @Accessor void setXRot(float var1);
}

package tech.blinkfix.modules.impl.render;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.impl.EventMotion;
import tech.blinkfix.events.impl.EventMouseClick;
import tech.blinkfix.events.impl.EventRender;
import tech.blinkfix.events.impl.EventRender2D;
import tech.blinkfix.events.impl.EventShader;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.modules.impl.misc.Teams;
import tech.blinkfix.ui.notification.Notification;
import tech.blinkfix.ui.notification.NotificationLevel;
import tech.blinkfix.utils.BlinkingPlayer;
import tech.blinkfix.utils.EntityWatcher;
import tech.blinkfix.utils.FriendManager;
import tech.blinkfix.utils.InventoryUtils;
import tech.blinkfix.utils.MathUtils;
import tech.blinkfix.utils.ProjectionUtils;
import tech.blinkfix.utils.RenderUtils;
import tech.blinkfix.utils.SharedESPData;
import tech.blinkfix.utils.StencilUtils;
import tech.blinkfix.utils.Vector2f;
import tech.blinkfix.utils.renderer.Fonts;
import tech.blinkfix.utils.rotation.RotationUtils;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;
import tech.blinkfix.values.impl.FloatValue;
import tech.blinkfix.values.impl.ModeValue;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.joml.Vector4f;

@ModuleInfo(
   name = "NameTags",
   category = Category.RENDER,
   description = "Renders name tags"
)
public class NameTags extends Module {
   // 样式模式选择
   public ModeValue style = ValueBuilder.create(this, "Style")
      .setModes("Normal", "Capsule", "New", "South", "MCP")
      .setDefaultModeIndex(0)
      .build()
      .getModeValue();
   
   // New/South/MCP 模式设置
   public BooleanValue invis = ValueBuilder.create(this, "Show Invisible")
      .setDefaultBooleanValue(true)
      .setVisibility(() -> this.style.getCurrentMode().equals("New") || 
                          this.style.getCurrentMode().equals("South") || 
                          this.style.getCurrentMode().equals("MCP"))
      .build()
      .getBooleanValue();
   
   public BooleanValue armor = ValueBuilder.create(this, "Show Armor")
      .setDefaultBooleanValue(false)
      .setVisibility(() -> this.style.getCurrentMode().equals("New") || 
                          this.style.getCurrentMode().equals("South") || 
                          this.style.getCurrentMode().equals("MCP"))
      .build()
      .getBooleanValue();
   
   public BooleanValue glow = ValueBuilder.create(this, "Glow")
      .setDefaultBooleanValue(false)
      .setVisibility(() -> this.style.getCurrentMode().equals("New") || 
                          this.style.getCurrentMode().equals("South") || 
                          this.style.getCurrentMode().equals("MCP"))
      .build()
      .getBooleanValue();
   
   public FloatValue alphaValue = ValueBuilder.create(this, "Alpha")
      .setDefaultFloatValue(80.0F)
      .setMinFloatValue(10.0F)
      .setMaxFloatValue(255.0F)
      .setFloatStep(1.0F)
      .setVisibility(() -> this.style.getCurrentMode().equals("New") || 
                          this.style.getCurrentMode().equals("South") || 
                          this.style.getCurrentMode().equals("MCP"))
      .build()
      .getFloatValue();
   
   // Bloom 效果设置
   public BooleanValue bloom = ValueBuilder.create(this, "Bloom")
      .setDefaultBooleanValue(false)
      .build()
      .getBooleanValue();
   
   // 圆角半径
   public FloatValue cornerRadius = ValueBuilder.create(this, "Corner Radius")
      .setDefaultFloatValue(4.0F)
      .setFloatStep(0.1F)
      .setMinFloatValue(0.0F)
      .setMaxFloatValue(10.0F)
      .build()
      .getFloatValue();
   
   // 胶囊之间的间距
   public FloatValue capsuleSpacing = ValueBuilder.create(this, "Capsule Spacing")
      .setDefaultFloatValue(2.0F)
      .setFloatStep(0.1F)
      .setMinFloatValue(0.0F)
      .setMaxFloatValue(10.0F)
      .setVisibility(() -> this.style.getCurrentMode().equals("Capsule"))
      .build()
      .getFloatValue();
   
   // 不透明度
   public FloatValue opacity = ValueBuilder.create(this, "Opacity")
      .setDefaultFloatValue(0.4F)
      .setFloatStep(0.01F)
      .setMinFloatValue(0.0F)
      .setMaxFloatValue(1.0F)
      .build()
      .getFloatValue();
   
   public BooleanValue mcf = ValueBuilder.create(this, "Middle Click Friend").setDefaultBooleanValue(true).build().getBooleanValue();
   public BooleanValue showCompassPosition = ValueBuilder.create(this, "Compass Position").setDefaultBooleanValue(true).build().getBooleanValue();
   public BooleanValue compassOnly = ValueBuilder.create(this, "Compass Only")
      .setDefaultBooleanValue(true)
      .setVisibility(() -> this.showCompassPosition.getCurrentValue())
      .build()
      .getBooleanValue();
   public BooleanValue noPlayerOnly = ValueBuilder.create(this, "No Player Only")
      .setDefaultBooleanValue(true)
      .setVisibility(() -> this.showCompassPosition.getCurrentValue())
      .build()
      .getBooleanValue();
   public BooleanValue shared = ValueBuilder.create(this, "Shared ESP").setDefaultBooleanValue(true).build().getBooleanValue();
   public FloatValue scale = ValueBuilder.create(this, "Scale")
      .setDefaultFloatValue(0.3F)
      .setFloatStep(0.01F)
      .setMinFloatValue(0.1F)
      .setMaxFloatValue(0.5F)
      .build()
      .getFloatValue();
   private final Map<Entity, Vector2f> entityPositions = new ConcurrentHashMap<>();
   private final List<NameTags.NameTagData> sharedPositions = new CopyOnWriteArrayList<>();
   List<Vector4f> blurMatrices = new ArrayList<>();
   private BlockPos spawnPosition;
   private Vector2f compassPosition;
   private final Map<Player, Integer> aimTicks = new ConcurrentHashMap<>();
   private Player aimingPlayer;

   private boolean hasPlayer() {
      for (Entity entity : mc.level.entitiesForRendering()) {
         if (entity != mc.player && !(entity instanceof BlinkingPlayer) && entity instanceof Player) {
            return true;
         }
      }

      return false;
   }

   private BlockPos getSpawnPosition(ClientLevel p_117922_) {
      return p_117922_.dimensionType().natural() ? p_117922_.getSharedSpawnPos() : null;
   }

   @EventTarget
   public void onMotion(EventMotion e) {
      if (e.getType() == EventType.PRE) {
         if (!this.mcf.getCurrentValue()) {
            this.aimingPlayer = null;
         } else {
            for (Player player : mc.level.players()) {
               if (!(player instanceof BlinkingPlayer) && player != mc.player) {
                  if (isAiming(player, mc.player.getYRot(), mc.player.getXRot())) {
                     if (this.aimTicks.containsKey(player)) {
                        this.aimTicks.put(player, this.aimTicks.get(player) + 1);
                     } else {
                        this.aimTicks.put(player, 1);
                     }

                     if (this.aimTicks.get(player) >= 10) {
                        this.aimingPlayer = player;
                        break;
                     }
                  } else if (this.aimTicks.containsKey(player) && this.aimTicks.get(player) > 0) {
                     this.aimTicks.put(player, this.aimTicks.get(player) - 1);
                  } else {
                     this.aimTicks.put(player, 0);
                  }
               }
            }

            if (this.aimingPlayer != null && this.aimTicks.containsKey(this.aimingPlayer) && this.aimTicks.get(this.aimingPlayer) <= 0) {
               this.aimingPlayer = null;
            }
         }

         this.spawnPosition = null;
         if (!InventoryUtils.hasItem(Items.COMPASS) && this.compassOnly.getCurrentValue()) {
            return;
         }

         if (this.hasPlayer() && this.noPlayerOnly.getCurrentValue()) {
            return;
         }

         this.spawnPosition = this.getSpawnPosition(mc.level);
      }
   }

   public static boolean isAiming(Entity targetEntity, float yaw, float pitch) {
      Vec3 playerEye = new Vec3(mc.player.getX(), mc.player.getY() + (double)mc.player.getEyeHeight(), mc.player.getZ());
      HitResult intercept = RotationUtils.getIntercept(targetEntity.getBoundingBox(), new Vector2f(yaw, pitch), playerEye, 150.0);
      if (intercept == null) {
         return false;
      } else {
         return intercept.getType() != Type.ENTITY ? false : intercept.getLocation().distanceTo(playerEye) < 150.0;
      }
   }

   @EventTarget
   public void onShader(EventShader e) {
      if (!this.isEnabled()) return;
      
      if (e.getType() == EventType.BLUR) {
         // 使用 blurMatrices 渲染模糊效果
         for (Vector4f blurMatrix : this.blurMatrices) {
            float x = blurMatrix.x();
            float y = blurMatrix.y();
            float width = blurMatrix.z() - x;
            float height = blurMatrix.w() - y;
            RenderUtils.drawRoundedRect(e.getStack(), x, y, width, height, this.cornerRadius.getCurrentValue(), 1073741824);
         }
      } else if (e.getType() == EventType.SHADOW && this.bloom.getCurrentValue()) {
         // 使用 blurMatrices 渲染 bloom 效果
         for (Vector4f blurMatrix : this.blurMatrices) {
            float x = blurMatrix.x();
            float y = blurMatrix.y();
            float width = blurMatrix.z() - x;
            float height = blurMatrix.w() - y;
            RenderUtils.drawRoundedRect(e.getStack(), x, y, width, height, this.cornerRadius.getCurrentValue(), Integer.MIN_VALUE);
         }
      }
   }

   @EventTarget
   public void update(EventRender e) {
      try {
         this.updatePositions(e.getRenderPartialTicks());
         this.compassPosition = null;
         if (this.spawnPosition != null) {
            this.compassPosition = ProjectionUtils.project(
               (double)this.spawnPosition.getX() + 0.5,
               (double)this.spawnPosition.getY() + 1.75,
               (double)this.spawnPosition.getZ() + 0.5,
               e.getRenderPartialTicks()
            );
         }
      } catch (Exception var3) {
      }
   }

   @EventTarget
   public void onMouseKey(EventMouseClick e) {
      if (e.getKey() == 2 && !e.isState() && this.mcf.getCurrentValue() && this.aimingPlayer != null) {
         if (FriendManager.isFriend(this.aimingPlayer)) {
            Notification notification = new Notification(
               NotificationLevel.ERROR, "Removed " + this.aimingPlayer.getName().getString() + " from friends!", 3000L
            );
            BlinkFix.getInstance().getNotificationManager().addNotification(notification);
            FriendManager.removeFriend(this.aimingPlayer);
         } else {
            Notification notification = new Notification(NotificationLevel.SUCCESS, "Added " + this.aimingPlayer.getName().getString() + " as friends!", 3000L);
            BlinkFix.getInstance().getNotificationManager().addNotification(notification);
            FriendManager.addFriend(this.aimingPlayer);
         }
      }
   }

   @EventTarget
   public void onRender(EventRender2D e) {
      this.blurMatrices.clear();
      int color1 = new Color(0, 0, 0, (int) (this.opacity.getCurrentValue() * 100)).getRGB();
      int color2 = new Color(0, 0, 0, (int) (this.opacity.getCurrentValue() * 200)).getRGB();
      
      if (this.compassPosition != null) {
         Vector2f position = this.compassPosition;
         float scale = Math.max(
               80.0F
                  - Mth.sqrt(
                     (float)mc.player
                        .distanceToSqr(
                           (double)this.spawnPosition.getX() + 0.5, (double)this.spawnPosition.getY() + 1.75, (double)this.spawnPosition.getZ() + 0.5
                        )
                  ),
               0.0F
            )
            * this.scale.getCurrentValue()
            / 80.0F;
         String text = "Compass";
         float width = Fonts.harmony.getWidth(text, (double)scale);
         double height = Fonts.harmony.getHeight(true, (double)scale);
         this.blurMatrices
            .add(new Vector4f(position.x - width / 2.0F - 2.0F, position.y - 2.0F, position.x + width / 2.0F + 2.0F, (float)((double)position.y + height + 2.0F)));
         StencilUtils.write(false);
         RenderUtils.drawRoundedRect(
            e.getStack(), position.x - width / 2.0F - 2.0F, position.y - 2.0F, width + 4.0F, (float) (height + 2.0F), this.cornerRadius.getCurrentValue(), -1
         );
         StencilUtils.erase(true);
         RenderUtils.drawRoundedRect(
            e.getStack(), position.x - width / 2.0F - 2.0F, position.y - 2.0F, width + 4.0F, (float) (height + 2.0F), this.cornerRadius.getCurrentValue(), color1
         );
         StencilUtils.dispose();
         Fonts.harmony.setAlpha(0.8F);
         Fonts.harmony.render(e.getStack(), text, (double)(position.x - width / 2.0F), (double)(position.y - 1.0F), Color.WHITE, true, (double)scale);
      }

      for (Entry<Entity, Vector2f> entry : this.entityPositions.entrySet()) {
         if (entry.getKey() != mc.player && entry.getKey() instanceof Player) {
            Player living = (Player)entry.getKey();
            e.getStack().pushPose();
            float hp = living.getHealth();
            if (hp > 20.0F) {
               living.setHealth(20.0F);
            }

            Vector2f position = entry.getValue();
            float scale = this.scale.getCurrentValue();
            double height = Fonts.harmony.getHeight(true, (double)scale);

            // 根据样式模式渲染
            if (this.style.getCurrentMode().equals("Normal")) {
               // Normal 模式渲染逻辑
               String text = "";


               if (Teams.isSameTeam(living)) {
                  text = text + "§aTeam§f | ";
               }

               if (FriendManager.isFriend(living)) {
                  text = text + "§aFriend§f | ";
               }

               if (this.aimingPlayer == living) {
                  text = text + "§cAiming§f | ";
               }

               text = text + living.getName().getString();
               text = text + "§f | §c" + Math.round(hp) + (living.getAbsorptionAmount() > 0.0F ? "+" + Math.round(living.getAbsorptionAmount()) : "") + "HP";
               float width = Fonts.harmony.getWidth(text, (double)scale);
               float delta = 1.0F - living.getHealth() / living.getMaxHealth();
               this.blurMatrices
                  .add(new Vector4f(position.x - width / 2.0F - 2.0F, position.y - 2.0F, position.x + width / 2.0F + 2.0F, (float)((double)position.y + height + 2.0F)));
               RenderUtils.drawRoundedRect(
                  e.getStack(),
                  position.x - width / 2.0F - 2.0F,
                  position.y - 2.0F,
                  width + 4.0F,
                  (float) (height + 2.0F),
                  this.cornerRadius.getCurrentValue(),
                  color1
               );
               RenderUtils.drawRoundedRect(
                  e.getStack(),
                  position.x - width / 2.0F - 2.0F,
                  position.y - 2.0F,
                  (width + 4.0F) * (1.0F - delta),
                  (float) (height + 2.0F),
                  this.cornerRadius.getCurrentValue(),
                  color2
               );
               Fonts.harmony.setAlpha(0.8F);
               Fonts.harmony.render(e.getStack(), text, (double)(position.x - width / 2.0F), (double)(position.y - 1.0F), Color.WHITE, true, (double)scale);
               Fonts.harmony.setAlpha(1.0F);
            } else if (this.style.getCurrentMode().equals("Capsule")) {
               // Capsule 模式渲染逻辑
               float spacing = this.capsuleSpacing.getCurrentValue();
               
               // 准备所有胶囊数据
               List<CapsuleData> capsules = new ArrayList<>();

               
               // 2. Team状态（如果是队友）
               if (Teams.isSameTeam(living)) {
                  String teamText = "§aTeam";
                  float teamWidth = Fonts.harmony.getWidth(teamText, (double)scale);
                  capsules.add(new CapsuleData(teamText, teamWidth));
               }
               
               // 3. Friend状态（如果是好友）
               if (FriendManager.isFriend(living)) {
                  String friendText = "§aFriend";
                  float friendWidth = Fonts.harmony.getWidth(friendText, (double)scale);
                  capsules.add(new CapsuleData(friendText, friendWidth));
               }
               
               // 4. Aiming状态（如果正在瞄准）
               if (this.aimingPlayer == living) {
                  String aimingText = "§cAiming";
                  float aimingWidth = Fonts.harmony.getWidth(aimingText, (double)scale);
                  capsules.add(new CapsuleData(aimingText, aimingWidth));
               }
               
               // 5. 血量
               String healthText = "§c" + Math.round(hp) + (living.getAbsorptionAmount() > 0.0F ? "+" + Math.round(living.getAbsorptionAmount()) : "") + "HP";
               float healthWidth = Fonts.harmony.getWidth(healthText, (double)scale);
               capsules.add(new CapsuleData(healthText, healthWidth));
               
               // 6. 名字（带截断，固定最大宽度80）
               String originalName = living.getName().getString();
               String nameToRender = originalName;
               float maxNameWidth = 80.0F;
               if (Fonts.harmony.getWidth(nameToRender, (double)scale) > maxNameWidth) {
                  while (nameToRender.length() > 0 && Fonts.harmony.getWidth(nameToRender + "...", (double)scale) > maxNameWidth) {
                     nameToRender = nameToRender.substring(0, nameToRender.length() - 1);
                  }
                  nameToRender += "...";
               }
               String nameText = nameToRender;
               float nameMeasuredWidth = Fonts.harmony.getWidth(nameText, (double)scale);
               float nameWidthClamped = Math.min(nameMeasuredWidth, maxNameWidth);
               capsules.add(new CapsuleData(nameText, nameWidthClamped));
               
               // 7. 距离
               float distance = mc.player.distanceTo(living);
               String distanceText = "§7" + String.format("%.1f", distance) + "m";
               float distanceWidth = Fonts.harmony.getWidth(distanceText, (double)scale);
               capsules.add(new CapsuleData(distanceText, distanceWidth));
               
               // 计算总宽度
               float totalWidth = capsules.stream()
                     .map(c -> c.width + 4.0F)
                     .reduce(0.0F, Float::sum)
                     + spacing * (capsules.size() - 1);
               
               // 从左到右渲染所有胶囊
               float startX = position.x - totalWidth / 2.0F;
               float currentX = startX;
               
               Fonts.harmony.setAlpha(0.8F);
               for (CapsuleData capsule : capsules) {
                  float capsuleWidth = capsule.width + 4.0F;
                  float capsuleEndX = currentX + capsuleWidth;
                  
                  this.blurMatrices.add(new Vector4f(currentX, position.y - 2.0F, capsuleEndX, (float)(position.y + height)));
                  
                  // 使用模板裁剪，防止文本溢出到下一个胶囊
                  StencilUtils.write(false);
                  RenderUtils.drawRoundedRect(e.getStack(), currentX, position.y - 2.0F, capsuleWidth, (float)(height + 2.0F), this.cornerRadius.getCurrentValue(), -1);
                  StencilUtils.erase(true);
                  RenderUtils.drawRoundedRect(e.getStack(), currentX, position.y - 2.0F, capsuleWidth, (float)(height + 2.0F), this.cornerRadius.getCurrentValue(), color1);
                  Fonts.harmony.render(e.getStack(), capsule.text, (double)(currentX + 2.0F), (double)(position.y - 1.0F), Color.WHITE, true, (double)scale);
                  StencilUtils.dispose();
                  
                  currentX = capsuleEndX + spacing;
               }
               Fonts.harmony.setAlpha(1.0F);
            } else if (this.style.getCurrentMode().equals("New")) {
               // New 模式 (DistanceNameTag) - 显示距离的 NameTag
               renderNewStyle(e, living, position, scale, height, hp, color1, color2);
            } else if (this.style.getCurrentMode().equals("South")) {
               // South 模式 (TestNameTag) - 简洁测试样式
               renderSouthStyle(e, living, position, scale, height, hp, color1, color2);
            } else if (this.style.getCurrentMode().equals("MCP")) {
               // MCP 模式 (RoundNameTag) - 圆角样式
               renderMCPStyle(e, living, position, scale, height,hp, color1, color2);
            }
            
            e.getStack().popPose();
         }
      }

      if (this.shared.getCurrentValue()) {
         for (NameTags.NameTagData data : this.sharedPositions) {
            e.getStack().pushPose();
            Vector2f positionx = data.getRender();
            String textx = "§aShared§f | " + data.getDisplayName();
            float scale = this.scale.getCurrentValue();
            float width = Fonts.harmony.getWidth(textx, (double)scale);
            double delta = 1.0 - data.getHealth() / data.getMaxHealth();
            double height = Fonts.harmony.getHeight(true, (double)scale);
            this.blurMatrices
               .add(
                  new Vector4f(positionx.x - width / 2.0F - 2.0F, positionx.y - 2.0F, positionx.x + width / 2.0F + 2.0F, (float)((double)positionx.y + height + 2.0F))
               );
            RenderUtils.drawRoundedRect(
               e.getStack(),
               positionx.x - width / 2.0F - 2.0F,
               positionx.y - 2.0F,
               width + 4.0F,
               (float) (height + 2.0F),
               this.cornerRadius.getCurrentValue(),
               color1
            );
            RenderUtils.drawRoundedRect(
               e.getStack(),
               positionx.x - width / 2.0F - 2.0F,
               positionx.y - 2.0F,
               (float)((double)(width + 4.0F) * (1.0 - delta)),
               (float) (height + 2.0F),
               this.cornerRadius.getCurrentValue(),
               color2
            );
            Fonts.harmony.setAlpha(0.8F);
            Fonts.harmony.render(e.getStack(), textx, (double)(positionx.x - width / 2.0F), (double)(positionx.y - 1.0F), Color.WHITE, true, (double)scale);
            Fonts.harmony.setAlpha(1.0F);
            e.getStack().popPose();
         }
      }
   }
   
   // Capsule 模式辅助数据类
   private static class CapsuleData {
      private final String text;
      private final float width;

      public CapsuleData(String text, float width) {
         this.text = text;
         this.width = width;
      }
   }
   
   /**
    * 渲染 New 模式 (DistanceNameTag) - 显示距离的 NameTag
    */
   private void renderNewStyle(EventRender2D e, Player living, Vector2f position, float scale, double height, 
                                  float hp, int color1, int color2) {
      // 检查是否显示隐身玩家
      if (!this.invis.getCurrentValue() && living.isInvisible()) {
         return;
      }
      
      // 构建文本
      StringBuilder textBuilder = new StringBuilder();

      
      if (Teams.isSameTeam(living)) {
         textBuilder.append("§aTeam§f | ");
      }
      
      if (FriendManager.isFriend(living)) {
         textBuilder.append("§aFriend§f | ");
      }
      
      if (this.aimingPlayer == living) {
         textBuilder.append("§cAiming§f | ");
      }
      
      textBuilder.append(living.getName().getString());
      textBuilder.append("§f | §c").append(Math.round(hp));
      if (living.getAbsorptionAmount() > 0.0F) {
         textBuilder.append("+").append(Math.round(living.getAbsorptionAmount()));
      }
      textBuilder.append("HP");
      
      // 添加距离信息
      float distance = mc.player.distanceTo(living);
      textBuilder.append(" §7| ").append(String.format("%.1f", distance)).append("m");
      
      String text = textBuilder.toString();
      float width = Fonts.harmony.getWidth(text, (double)scale);
      float delta = 1.0F - living.getHealth() / living.getMaxHealth();
      
      // 计算 alpha
      int alpha = (int)this.alphaValue.getCurrentValue();
      int bgColor = new Color(0, 0, 0, alpha).getRGB();
      int healthColor = new Color(200, 45, 45, (int)(alpha * 1.5f)).getRGB();
      
      // 添加到 blur 矩阵
      this.blurMatrices.add(new Vector4f(
         position.x - width / 2.0F - 2.0F, 
         position.y - 2.0F, 
         position.x + width / 2.0F + 2.0F, 
         (float)(position.y + height + 2.0F)
      ));
      
      // 绘制背景
      RenderUtils.drawRoundedRect(
         e.getStack(),
         position.x - width / 2.0F - 2.0F,
         position.y - 2.0F,
         width + 4.0F,
         (float)(height + 2.0F),
         this.cornerRadius.getCurrentValue(),
         bgColor
      );
      
      // 绘制血量条
      RenderUtils.drawRoundedRect(
         e.getStack(),
         position.x - width / 2.0F - 2.0F,
         position.y - 2.0F,
         (width + 4.0F) * (1.0F - delta),
         (float)(height + 2.0F),
         this.cornerRadius.getCurrentValue(),
         healthColor
      );
      
      // 渲染文本
      Fonts.harmony.setAlpha(0.9F);
      Fonts.harmony.render(e.getStack(), text, (double)(position.x - width / 2.0F), (double)(position.y - 1.0F), Color.WHITE, true, (double)scale);
      
      // 渲染装备（如果启用）
      if (this.armor.getCurrentValue()) {
         renderArmor(e, living, position, scale, height);
      }
      
      Fonts.harmony.setAlpha(1.0F);
   }
   
   /**
    * 渲染 South 模式 (TestNameTag) - 简洁测试样式
    */
   private void renderSouthStyle(EventRender2D e, Player living, Vector2f position, float scale, double height,
                                float hp, int color1, int color2) {
      // 检查是否显示隐身玩家
      if (!this.invis.getCurrentValue() && living.isInvisible()) {
         return;
      }
      
      // 构建简洁文本
      String name = living.getName().getString();
      float distance = mc.player.distanceTo(living);
      String text = name + " §7(" + String.format("%.1f", distance) + "m)";
      
      float width = Fonts.harmony.getWidth(text, (double)scale);
      float delta = 1.0F - living.getHealth() / living.getMaxHealth();
      
      // 计算 alpha
      int alpha = (int)this.alphaValue.getCurrentValue();
      int bgColor = new Color(20, 20, 20, alpha).getRGB();
      int healthColor = new Color(150, 30, 30, (int)(alpha * 1.2f)).getRGB();
      
      // 添加到 blur 矩阵
      this.blurMatrices.add(new Vector4f(
         position.x - width / 2.0F - 2.0F,
         position.y - 2.0F,
         position.x + width / 2.0F + 2.0F,
         (float)(position.y + height + 2.0F)
      ));
      
      // 绘制背景（更简洁的样式）
      RenderUtils.drawRoundedRect(
         e.getStack(),
         position.x - width / 2.0F - 2.0F,
         position.y - 2.0F,
         width + 4.0F,
         (float)(height + 2.0F),
         3.0F, // 较小的圆角
         bgColor
      );
      
      // 绘制血量条（底部）
      float healthBarHeight = 2.0F;
      RenderUtils.drawRoundedRect(
         e.getStack(),
         position.x - width / 2.0F - 2.0F,
         (float)(position.y + height),
         (width + 4.0F) * (1.0F - delta),
         healthBarHeight,
         1.0F,
         healthColor
      );
      
      // 渲染文本
      Fonts.harmony.setAlpha(0.95F);
      Fonts.harmony.render(e.getStack(), text, (double)(position.x - width / 2.0F), (double)(position.y - 1.0F), Color.WHITE, true, (double)scale);
      
      // 渲染装备（如果启用）
      if (this.armor.getCurrentValue()) {
         renderArmor(e, living, position, scale, height);
      }
      
      Fonts.harmony.setAlpha(1.0F);
   }
   
   /**
    * 渲染 MCP 模式 (RoundNameTag) - 圆角样式
    */
   private void renderMCPStyle(EventRender2D e, Player living, Vector2f position, float scale, double height,
                                float hp, int color1, int color2) {
      // 检查是否显示隐身玩家
      if (!this.invis.getCurrentValue() && living.isInvisible()) {
         return;
      }
      
      // 构建文本
      StringBuilder textBuilder = new StringBuilder();
      

      
      if (Teams.isSameTeam(living)) {
         textBuilder.append("§aTeam§f | ");
      }
      
      if (FriendManager.isFriend(living)) {
         textBuilder.append("§aFriend§f | ");
      }
      
      if (this.aimingPlayer == living) {
         textBuilder.append("§cAiming§f | ");
      }
      
      textBuilder.append(living.getName().getString());
      textBuilder.append(" §c").append(Math.round(hp));
      if (living.getAbsorptionAmount() > 0.0F) {
         textBuilder.append("+").append(Math.round(living.getAbsorptionAmount()));
      }
      textBuilder.append("HP");
      
      String text = textBuilder.toString();
      float width = Fonts.harmony.getWidth(text, (double)scale);
      float delta = 1.0F - living.getHealth() / living.getMaxHealth();
      
      // 计算 alpha
      int alpha = (int)this.alphaValue.getCurrentValue();
      int bgColor = new Color(15, 15, 15, alpha).getRGB();
      int healthColor = new Color(220, 50, 50, (int)(alpha * 1.3f)).getRGB();
      
      // 添加到 blur 矩阵
      this.blurMatrices.add(new Vector4f(
         position.x - width / 2.0F - 2.0F,
         position.y - 2.0F,
         position.x + width / 2.0F + 2.0F,
         (float)(position.y + height + 2.0F)
      ));
      
      // 使用更大的圆角半径
      float roundRadius = 6.0F;
      
      // 绘制背景
      StencilUtils.write(false);
      RenderUtils.drawRoundedRect(
         e.getStack(),
         position.x - width / 2.0F - 2.0F,
         position.y - 2.0F,
         width + 4.0F,
         (float)(height + 2.0F),
         roundRadius,
         -1
      );
      StencilUtils.erase(true);
      RenderUtils.drawRoundedRect(
         e.getStack(),
         position.x - width / 2.0F - 2.0F,
         position.y - 2.0F,
         width + 4.0F,
         (float)(height + 2.0F),
         roundRadius,
         bgColor
      );
      
      // 绘制血量条（使用圆角）
      RenderUtils.drawRoundedRect(
         e.getStack(),
         position.x - width / 2.0F - 2.0F,
         position.y - 2.0F,
         (width + 4.0F) * (1.0F - delta),
         (float)(height + 2.0F),
         roundRadius,
         healthColor
      );
      
      StencilUtils.dispose();
      
      // 渲染文本
      Fonts.harmony.setAlpha(0.85F);
      Fonts.harmony.render(e.getStack(), text, (double)(position.x - width / 2.0F), (double)(position.y - 1.0F), Color.WHITE, true, (double)scale);
      
      // 渲染装备（如果启用）
      if (this.armor.getCurrentValue()) {
         renderArmor(e, living, position, scale, height);
      }
      
      Fonts.harmony.setAlpha(1.0F);
   }
   
   /**
    * 渲染玩家装备
    */
   private void renderArmor(EventRender2D e, Player living, Vector2f position, float scale, double height) {
      float itemSize = 12.0F;
      float startY = (float)(position.y + height + 4.0F);
      float currentX = position.x;
      
      // 渲染护甲（从下到上：靴子、护腿、胸甲、头盔）
      for (int i = 0; i < 4; i++) {
         var armorStack = living.getInventory().getArmor(i);
         if (!armorStack.isEmpty()) {
            e.getGuiGraphics().renderItem(armorStack, (int)currentX, (int)startY);
            e.getGuiGraphics().renderItemDecorations(mc.font, armorStack, (int)currentX, (int)startY);
            currentX += itemSize + 2.0F;
         }
      }
      
      // 渲染主手物品
      var mainHand = living.getMainHandItem();
      if (!mainHand.isEmpty()) {
         e.getGuiGraphics().renderItem(mainHand, (int)currentX, (int)startY);
         e.getGuiGraphics().renderItemDecorations(mc.font, mainHand, (int)currentX, (int)startY);
      }
   }

   private void updatePositions(float renderPartialTicks) {
      this.entityPositions.clear();
      this.sharedPositions.clear();

      for (Entity entity : mc.level.entitiesForRendering()) {
         if (entity instanceof Player && !entity.getName().getString().startsWith("CIT-")) {
            // 检查隐身玩家设置（仅对 New/South/MCP 模式）
            if ((this.style.getCurrentMode().equals("New") || 
                 this.style.getCurrentMode().equals("South") || 
                 this.style.getCurrentMode().equals("MCP")) &&
                !this.invis.getCurrentValue() && entity.isInvisible()) {
               continue;
            }
            
            double x = MathUtils.interpolate(renderPartialTicks, entity.xo, entity.getX());
            double y = MathUtils.interpolate(renderPartialTicks, entity.yo, entity.getY()) + (double)entity.getBbHeight() + 0.5;
            double z = MathUtils.interpolate(renderPartialTicks, entity.zo, entity.getZ());
            Vector2f vector = ProjectionUtils.project(x, y, z, renderPartialTicks);
            vector.setY(vector.getY() - 2.0F);
            this.entityPositions.put(entity, vector);
         }
      }

      if (this.shared.getCurrentValue()) {
         Map<String, SharedESPData> dataMap = EntityWatcher.getSharedESPData();

         for (SharedESPData value : dataMap.values()) {
            double x = value.getPosX();
            double y = value.getPosY() + (double)mc.player.getBbHeight() + 0.5;
            double z = value.getPosZ();
            Vector2f vector = ProjectionUtils.project(x, y, z, renderPartialTicks);
            vector.setY(vector.getY() - 2.0F);
            String displayName = value.getDisplayName();
            displayName = displayName
               + "§f | §c"
               + Math.round(value.getHealth())
               + (value.getAbsorption() > 0.0 ? "+" + Math.round(value.getAbsorption()) : "")
               + "HP";
            this.sharedPositions
               .add(new NameTags.NameTagData(displayName, value.getHealth(), value.getMaxHealth(), value.getAbsorption(), new Vec3(x, y, z), vector));
         }
      }
   }

   private static class NameTagData {
      private final String displayName;
      private final double health;
      private final double maxHealth;
      private final double absorption;
      private final Vec3 position;
      private final Vector2f render;

      public String getDisplayName() {
         return this.displayName;
      }

      public double getHealth() {
         return this.health;
      }

      public double getMaxHealth() {
         return this.maxHealth;
      }

      public double getAbsorption() {
         return this.absorption;
      }

      public Vec3 getPosition() {
         return this.position;
      }

      public Vector2f getRender() {
         return this.render;
      }

      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof NameTags.NameTagData other)) {
            return false;
         } else if (!other.canEqual(this)) {
            return false;
         } else if (Double.compare(this.getHealth(), other.getHealth()) != 0) {
            return false;
         } else if (Double.compare(this.getMaxHealth(), other.getMaxHealth()) != 0) {
            return false;
         } else if (Double.compare(this.getAbsorption(), other.getAbsorption()) != 0) {
            return false;
         } else {
            Object this$displayName = this.getDisplayName();
            Object other$displayName = other.getDisplayName();
            if (this$displayName == null ? other$displayName == null : this$displayName.equals(other$displayName)) {
               Object this$position = this.getPosition();
               Object other$position = other.getPosition();
               if (this$position == null ? other$position == null : this$position.equals(other$position)) {
                  Object this$render = this.getRender();
                  Object other$render = other.getRender();
                  return this$render == null ? other$render == null : this$render.equals(other$render);
               } else {
                  return false;
               }
            } else {
               return false;
            }
         }
      }

      protected boolean canEqual(Object other) {
         return other instanceof NameTags.NameTagData;
      }

      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         long $health = Double.doubleToLongBits(this.getHealth());
         result = result * 59 + (int)($health >>> 32 ^ $health);
         long $maxHealth = Double.doubleToLongBits(this.getMaxHealth());
         result = result * 59 + (int)($maxHealth >>> 32 ^ $maxHealth);
         long $absorption = Double.doubleToLongBits(this.getAbsorption());
         result = result * 59 + (int)($absorption >>> 32 ^ $absorption);
         Object $displayName = this.getDisplayName();
         result = result * 59 + ($displayName == null ? 43 : $displayName.hashCode());
         Object $position = this.getPosition();
         result = result * 59 + ($position == null ? 43 : $position.hashCode());
         Object $render = this.getRender();
         return result * 59 + ($render == null ? 43 : $render.hashCode());
      }

      @Override
      public String toString() {
         return "NameTags.NameTagData(displayName="
            + this.getDisplayName()
            + ", health="
            + this.getHealth()
            + ", maxHealth="
            + this.getMaxHealth()
            + ", absorption="
            + this.getAbsorption()
            + ", position="
            + this.getPosition()
            + ", render="
            + this.getRender()
            + ")";
      }

      public NameTagData(String displayName, double health, double maxHealth, double absorption, Vec3 position, Vector2f render) {
         this.displayName = displayName;
         this.health = health;
         this.maxHealth = maxHealth;
         this.absorption = absorption;
         this.position = position;
         this.render = render;
      }
   }
}

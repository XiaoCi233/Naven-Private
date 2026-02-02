package tech.blinkfix.modules.impl.render;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.impl.EventRender2D;
import tech.blinkfix.events.impl.EventShader;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.utils.RenderUtils;
import tech.blinkfix.utils.StencilUtils;
import tech.blinkfix.utils.renderer.Fonts;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;
import tech.blinkfix.values.impl.FloatValue;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.*;

import java.awt.*;
import java.util.Collection;
import java.util.List;

@ModuleInfo(
   name = "Scoreboard",
   description = "Modifies the scoreboard",
   category = Category.RENDER
)
public class Scoreboard extends Module {
   public static final int headerColor = new Color(150, 45, 45, 255).getRGB();
   public static final int bodyColor = new Color(0, 0, 0, 120).getRGB();
   
   public BooleanValue modernStyle = ValueBuilder.create(this, "Modern Style")
      .setDefaultBooleanValue(true)
      .build()
      .getBooleanValue();
   
   public BooleanValue hideScore = ValueBuilder.create(this, "Hide Red Score")
      .setDefaultBooleanValue(true)
      .build()
      .getBooleanValue();
   
   public FloatValue down = ValueBuilder.create(this, "Down")
      .setDefaultFloatValue(120.0F)
      .setFloatStep(1.0F)
      .setMinFloatValue(0.0F)
      .setMaxFloatValue(300.0F)
      .build()
      .getFloatValue();
   
   public FloatValue cornerRadius = ValueBuilder.create(this, "Corner Radius")
      .setVisibility(() -> this.modernStyle.getCurrentValue())
      .setDefaultFloatValue(5.0F)
      .setFloatStep(0.5F)
      .setMinFloatValue(0.0F)
      .setMaxFloatValue(10.0F)
      .build()
      .getFloatValue();
   
   public FloatValue fontSize = ValueBuilder.create(this, "Font Size")
      .setVisibility(() -> this.modernStyle.getCurrentValue())
      .setDefaultFloatValue(0.35F)
      .setFloatStep(0.01F)
      .setMinFloatValue(0.2F)
      .setMaxFloatValue(0.6F)
      .build()
      .getFloatValue();
   
   private float[] position = new float[4]; // 存储位置用于shader
   
   @EventTarget
   public void onShader(EventShader e) {
      if (!this.modernStyle.getCurrentValue()) return;
      if (!this.isEnabled()) return;
      
      // 渲染模糊背景
      if (position[0] != 0 && position[1] != 0 && position[2] != 0 && position[3] != 0) {
         RenderUtils.drawRoundedRect(
            e.getStack(),
            position[0],
            position[1],
            position[2] - position[0],
            position[3] - position[1],
            this.cornerRadius.getCurrentValue(),
            Integer.MIN_VALUE
         );
      }
   }
   
   @EventTarget
   public void onRender(EventRender2D e) {
      if (!this.modernStyle.getCurrentValue()) return;
      if (!this.isEnabled()) return;
      if (mc.level == null) return;
      
      renderModernScoreboard(e);
   }
   
   private void renderModernScoreboard(EventRender2D e) {
      Minecraft mc = Minecraft.getInstance();
      net.minecraft.world.scores.Scoreboard scoreboard = mc.level.getScoreboard();
      Objective objective = scoreboard.getDisplayObjective(1); // 1 = SIDEBAR
      
      if (objective == null) return;
      
      // 获取记分板数据（使用1.20.1 Forge API）
      Collection<Score> collection = scoreboard.getPlayerScores(objective);
      
      // 过滤
      List<Score> list = Lists.newArrayList(Iterables.filter(collection, new Predicate<Score>() {
         @Override
         public boolean apply(Score score) {
            return score.getOwner() != null && !score.getOwner().startsWith("#");
         }
      }));
      
      if (list.size() > 15) {
         list = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
      }
      
      // 计算宽度
      float scale = this.fontSize.getCurrentValue();
      int maxWidth = (int) Fonts.harmony.getWidth(objective.getDisplayName().getString(), (double)scale);
      
      for (Score score : list) {
         Team team = scoreboard.getPlayersTeam(score.getOwner());
         String playerName = PlayerTeam.formatNameForTeam(team, Component.literal(score.getOwner())).getString();
         String scoreText = ChatFormatting.RED + String.valueOf(score.getScore());
         String fullText = playerName + (this.hideScore.getCurrentValue() ? "" : ": " + scoreText);
         int width = (int) Fonts.harmony.getWidth(fullText, (double)scale);
         maxWidth = Math.max(maxWidth, width);
      }
      
      // 计算位置和尺寸
      int screenWidth = mc.getWindow().getGuiScaledWidth();
      int screenHeight = mc.getWindow().getGuiScaledHeight();
      float fontHeight = (float) Fonts.harmony.getHeight(true, (double)scale);
      
      float totalHeight = (list.size() + 1) * fontHeight + fontHeight + 6; // +1 for title, +6 for padding
      float startY = screenHeight / 2f - totalHeight / 2f + this.down.getCurrentValue();
      float startX = screenWidth - maxWidth - 10;
      float endX = screenWidth - 3;
      float endY = startY + totalHeight;
      
      // 保存位置用于shader
      position[0] = startX - 2;
      position[1] = startY - fontHeight - 3;
      position[2] = endX;
      position[3] = endY;
      
      // 使用 stencil 绘制圆角背景
      StencilUtils.write(false);
      RenderUtils.drawRoundedRect(
         e.getStack(),
         position[0],
         position[1],
         position[2] - position[0],
         position[3] - position[1],
         this.cornerRadius.getCurrentValue(),
         0xFFFFFFFF
      );
      StencilUtils.erase(true);
      
      // 绘制头部
      RenderUtils.fill(
         e.getStack(),
         position[0],
         position[1],
         position[2],
         position[1] + 3,
         headerColor
      );
      
      // 绘制标题
      float titleY = startY - fontHeight;
      RenderUtils.fill(
         e.getStack(),
         position[0],
         titleY - 1,
         position[2],
         titleY + fontHeight,
         bodyColor
      );
      
      // 使用原版格式化的标题（保留颜色）
      Component titleComponent = objective.getDisplayName();
      String title = titleComponent.getString();
      float titleWidth = (float) Fonts.harmony.getWidth(title, (double)scale);
      float titleX = startX + (maxWidth - titleWidth) / 2f;
      // 从Component获取颜色
      Color titleColor = Color.WHITE;
      if (titleComponent.getStyle() != null) {
         net.minecraft.network.chat.TextColor textColor = titleComponent.getStyle().getColor();
         if (textColor != null) {
            Integer colorValue = textColor.getValue();
            if (colorValue != null) {
               titleColor = new Color(colorValue);
            }
         }
      }
      Fonts.harmony.render(e.getStack(), title, (double)titleX, (double)titleY, titleColor, true, (double)scale);
      
      // 绘制每一行
      float currentY = startY;
      for (Score score : list) {
         // 绘制行背景
         RenderUtils.fill(
            e.getStack(),
            position[0],
            currentY,
            position[2],
            currentY + fontHeight,
            bodyColor
         );
         
         // 获取玩家名称（保留Team的颜色信息）
         Team team = scoreboard.getPlayersTeam(score.getOwner());
         Component playerNameComponent = PlayerTeam.formatNameForTeam(team, Component.literal(score.getOwner()));
         String playerName = playerNameComponent.getString();
         
         // 从Team或Component获取颜色
         Color playerNameColor = Color.WHITE;
         if (team != null) {
            ChatFormatting teamColor = team.getColor();
            if (teamColor != null && teamColor.isColor()) {
               // 优先使用Team的颜色
               Integer colorValue = teamColor.getColor();
               if (colorValue != null) {
                  playerNameColor = new Color(colorValue);
               }
            }
         }
         // 如果Team没有颜色，尝试从Component获取
         if (playerNameColor.equals(Color.WHITE) && playerNameComponent.getStyle() != null) {
            net.minecraft.network.chat.TextColor textColor = playerNameComponent.getStyle().getColor();
            if (textColor != null) {
               Integer colorValue = textColor.getValue();
               if (colorValue != null) {
                  playerNameColor = new Color(colorValue);
               }
            }
         }
         
         // 渲染玩家名称（Font会自动解析字符串中的颜色码，但我们也传入正确的初始颜色）
         Fonts.harmony.render(e.getStack(), playerName, (double)startX, (double)currentY, playerNameColor, true, (double)scale);
         
         // 渲染分数（如果不隐藏）
         if (!this.hideScore.getCurrentValue()) {
            String scoreText = ChatFormatting.RED + String.valueOf(score.getScore());
            float scoreWidth = (float) Fonts.harmony.getWidth(scoreText, (double)scale);
            Fonts.harmony.render(
               e.getStack(),
               scoreText,
               (double)(endX - scoreWidth - 5),
               (double)currentY,
               Color.WHITE,
               true,
               (double)scale
            );
         }
         
         currentY += fontHeight;
      }
      
      StencilUtils.dispose();
   }
}

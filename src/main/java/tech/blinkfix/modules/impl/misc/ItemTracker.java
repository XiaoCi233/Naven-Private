package tech.blinkfix.modules.impl.misc;

import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.impl.EventMotion;
import tech.blinkfix.events.impl.EventRender;
import tech.blinkfix.events.impl.EventRender2D;
import tech.blinkfix.events.impl.EventShader;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.modules.ModuleInfo;
import tech.blinkfix.modules.impl.render.HUD;
import tech.blinkfix.utils.*;
import tech.blinkfix.utils.renderer.Fonts;
import tech.blinkfix.values.ValueBuilder;
import tech.blinkfix.values.impl.BooleanValue;
import tech.blinkfix.values.impl.FloatValue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.Entity;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import com.mojang.blaze3d.vertex.PoseStack;

@ModuleInfo(
        name = "ItemTracker",
        description = "Show the player's effect tags.",
        category = Category.MISC
)
public class ItemTracker extends Module {
    private static final Minecraft mc = Minecraft.getInstance();
    private final BooleanValue debug = ValueBuilder.create(this, "Debug").setDefaultBooleanValue(false).build().getBooleanValue();
    private final BooleanValue shared = ValueBuilder.create(this, "Shared").setDefaultBooleanValue(true).build().getBooleanValue();
    private final BooleanValue chatOutput = ValueBuilder.create(this, "ChatOutput").setDefaultBooleanValue(false).build().getBooleanValue();
    private final BooleanValue playerlist = ValueBuilder.create(this, "PlayerList").setDefaultBooleanValue(false).build().getBooleanValue();

    // PlayerList 设置
    public FloatValue playerListX = ValueBuilder.create(this, "PlayerList X")
            .setDefaultFloatValue(185.0F)
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(1200.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();

    public FloatValue playerListY = ValueBuilder.create(this, "PlayerList Y")
            .setDefaultFloatValue(385.0F)
            .setMinFloatValue(0.0F)
            .setMaxFloatValue(1200.0F)
            .setFloatStep(1.0F)
            .build()
            .getFloatValue();

    public FloatValue playerListScale = ValueBuilder.create(this, "PlayerList Scale")
            .setDefaultFloatValue(0.3F)
            .setMinFloatValue(0.1F)
            .setMaxFloatValue(1.0F)
            .setFloatStep(0.05F)
            .build()
            .getFloatValue();

    private final List<ItemTracker.TargetInfo> entityPositions = new CopyOnWriteArrayList<>();
    private int lastTickCount = 0;

    // PlayerList 变量
    private final Map<String, PlayerInfo> playerMap = new LinkedHashMap<>();
    private boolean shouldRenderPlayerList = false;

    // HUD 动画变量
    private float currentWidth = 0;
    private float currentHeight = 0;
    private float finalWidth = 0;
    private float finalHeight = 0;
    private long lastUpdateTime = 0;
    
    // Bloom 效果变量
    private org.joml.Vector4f blurMatrix = null;

    @EventTarget
    public void onEnable() {
        super.onEnable();
        lastTickCount = 0;
        if (chatOutput.getCurrentValue()) {
            ChatUtils.addChatMessage("§aEffectTags Enable CheckPlayer");
        }
    }

    @EventTarget
    public void onDisable() {
        super.onDisable();
        if (chatOutput.getCurrentValue()) {
            ChatUtils.addChatMessage("§cEffectTags Disabled CheckPlayer");
        }
    }

    @EventTarget
    public void update(EventRender e) {
        try {
            this.updatePositions(e.getRenderPartialTicks());
            this.outputToChat();
        } catch (Exception var3) {
        }
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (e.getType() != EventType.PRE) return;

        if (playerlist.getCurrentValue()) {
            updatePlayerList();
        }
    }

    private void updatePlayerList() {
        playerMap.clear();

        if (mc.level != null && mc.player != null) {
            List<AbstractClientPlayer> players = new ArrayList<>(mc.level.players());
            players.removeIf(player -> player.getId() < 0);

            // 按距离排序
            players.sort((o1, o2) -> {
                double dist1 = mc.player.distanceTo(o1);
                double dist2 = mc.player.distanceTo(o2);
                return Double.compare(dist1, dist2);
            });

            // 显示所有附近玩家（不只是有特殊物品的）
            for (AbstractClientPlayer player : players) {
                if (!Teams.isSameTeam(player) && !FriendManager.isFriend(player) && player != mc.player) {
                    int distance = (int) mc.player.distanceTo(player);
                    List<String> tags = new ArrayList<>();

                    // 检查所有特殊物品/神器标签
                    Set<String> entityTags = EntityWatcher.getEntityTags(player);
                    if (entityTags.contains("God Axe")) {
                        tags.add("God Axe");
                    }
                    if (entityTags.contains("Enchanted Golden Apple")) {
                        tags.add("Enchanted GApple");
                    }
                    if (entityTags.contains("End Crystal")) {
                        tags.add("End Crystal");
                    }
                    if (entityTags.contains("KB Ball")) {
                        tags.add("KB Ball");
                    }
                    if (entityTags.contains("KB Stick")) {
                        tags.add("KB Stick");
                    }
                    if (entityTags.contains("Punch Bow")) {
                        tags.add("Punch Bow");
                    }
                    if (entityTags.contains("Power Bow")) {
                        tags.add("Power Bow");
                    }
                    if (entityTags.contains("Totem")) {
                        tags.add("Totem");
                    }

                    // 添加所有附近玩家，无论是否有神器
                    playerMap.put(player.getName().getString(), new PlayerInfo(tags, distance));
                }
            }
        }

        // 启用后持续显示，不依赖条件
        shouldRenderPlayerList = playerlist.getCurrentValue();

        // 更新 HUD 尺寸
        calculateHudDimensions();
    }

    private void calculateHudDimensions() {
        // 使用 PearlPrediction 的尺寸计算方式
        double currentScale = playerListScale.getCurrentValue();
        float headerHeight = 3.0f; // 与 PearlPrediction 一致

        // 构建文本行列表
        List<String> lines = new ArrayList<>();
        lines.add("Player List"); // 标题

        // 计算最大宽度
        float maxWidth = 0.0f;

        // 标题宽度
        float titleWidth = Fonts.harmony.getWidth("Player List", currentScale);
        maxWidth = Math.max(maxWidth, titleWidth);

        // 玩家列表行宽度
        for (Map.Entry<String, PlayerInfo> entry : playerMap.entrySet()) {
            String playerName = entry.getKey();
            PlayerInfo playerInfo = entry.getValue();
            String displayText = playerName + " (" + playerInfo.distance + "m)";
            double nameWidth = Fonts.harmony.getWidth(displayText, currentScale);

            // 计算所有神器标签的宽度
            double tagsWidth = 0;
            if (!playerInfo.tags.isEmpty()) {
                String tagsText = String.join(", ", playerInfo.tags);
                tagsWidth = Fonts.harmony.getWidth(tagsText, currentScale);
            }

            float totalLineWidth = (float)(nameWidth + tagsWidth + 8); // 8px spacing between name and tags
            maxWidth = Math.max(maxWidth, totalLineWidth);
            lines.add(displayText); // 添加玩家行
        }

        // 计算文本高度（使用 PearlPrediction 的方式）
        float textHeight = (float)Fonts.harmony.getHeight(true, currentScale);
        float totalTextHeight = (textHeight * 0.875f) * Math.max(0, lines.size() - 1) + textHeight;

        // 使用 PearlPrediction 的尺寸计算方式
        this.finalWidth = maxWidth + 8.0f;
        this.finalHeight = totalTextHeight + headerHeight + 4.0f;

        // 动画计算（与 PearlPrediction 一致）
        long currentTime = System.currentTimeMillis();
        if (this.lastUpdateTime == 0) this.lastUpdateTime = currentTime;
        float deltaTime = (currentTime - this.lastUpdateTime) / 1000.0F;
        float animationSpeed = 10.0F;

        this.currentWidth += (this.finalWidth - this.currentWidth) * animationSpeed * deltaTime;
        this.currentHeight += (this.finalHeight - this.currentHeight) * animationSpeed * deltaTime;
        this.lastUpdateTime = currentTime;

        if (Math.abs(this.finalWidth - this.currentWidth) < 0.01f) this.currentWidth = this.finalWidth;
        if (Math.abs(this.finalHeight - this.currentHeight) < 0.01f) this.currentHeight = this.finalHeight;
    }

    private void updatePositions(float renderPartialTicks) {
        this.entityPositions.clear();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity != mc.player && entity instanceof AbstractClientPlayer) {
                AbstractClientPlayer player = (AbstractClientPlayer) entity;
                double x = MathUtils.interpolate(renderPartialTicks, entity.xo, entity.getX());
                double y = MathUtils.interpolate(renderPartialTicks, entity.yo, entity.getY()) + (double)entity.getBbHeight();
                double z = MathUtils.interpolate(renderPartialTicks, entity.zo, entity.getZ());
                Vector2f vector = ProjectionUtils.project(x, y, z, renderPartialTicks);
                Set<String> tags = EntityWatcher.getEntityTags(player);

                this.entityPositions.add(new ItemTracker.TargetInfo(player, vector, tags));
            }
        }

        if (this.shared.getCurrentValue()) {
            Map<String, SharedESPData> dataMap = EntityWatcher.getSharedESPData();

            for (SharedESPData value : dataMap.values()) {
                double x = value.getPosX();
                double y = value.getPosY() + (double)mc.player.getBbHeight();
                double z = value.getPosZ();
                Vector2f vector = ProjectionUtils.project(x, y, z, renderPartialTicks);
                this.entityPositions.add(new ItemTracker.TargetInfo(null, vector, Set.of(value.getTags())));
            }
        }
    }

    private void outputToChat() {
        if (!chatOutput.getCurrentValue() || mc.player == null) return;

        // 每5秒输出一次（100 ticks）
        if (mc.player.tickCount - lastTickCount < 100) return;

        lastTickCount = mc.player.tickCount;

        StringBuilder chatMessage = new StringBuilder();
        chatMessage.append("§6===== Player effect detection =====");

        int playerCount = 0;
        int totalTags = 0;

        for (ItemTracker.TargetInfo info : this.entityPositions) {
            if (info.getPlayer() != null) {
                playerCount++;
                AbstractClientPlayer player = info.getPlayer();
                Set<String> tags = info.getDescription();
                totalTags += tags.size();

                chatMessage.append("\n§bPlayer: §f").append(player.getName().getString());
                chatMessage.append(" §8[§7X: ").append(String.format("%.1f", player.getX()));
                chatMessage.append(" Y: ").append(String.format("%.1f", player.getY()));
                chatMessage.append(" Z: ").append(String.format("%.1f", player.getZ())).append("§8]");

                if (!tags.isEmpty()) {
                    chatMessage.append("\n  §aEffect: §f");
                    int tagCount = 0;
                    for (String tag : tags) {
                        if (tagCount > 0) chatMessage.append(", ");
                        chatMessage.append(I18n.get(tag));
                        tagCount++;
                        if (tagCount >= 5) {
                            chatMessage.append("\n       ");
                            tagCount = 0;
                        }
                    }
                } else {
                    chatMessage.append("\n  §7NoEffect");
                }
                chatMessage.append("\n");
            }
        }

        chatMessage.append("\n§6===== Statistics =====");
        chatMessage.append("\n§eCheckPlayer: §f").append(playerCount).append(" 个");
        chatMessage.append("\n§eTotal Effect Label: §f").append(totalTags).append(" 个");
        chatMessage.append("\n§6===================");

        ChatUtils.addChatMessage(chatMessage.toString());
    }

    @EventTarget
    public void onRender(EventRender2D e) {
        // 渲染原有的效果标签
        for (ItemTracker.TargetInfo info : this.entityPositions) {
            e.getStack().pushPose();
            double y = 0.0;

            for (String entityTag : info.getDescription()) {
                Fonts.harmony
                        .render(
                                e.getStack(),
                                I18n.get(entityTag, new Object[0]),
                                (double)(info.getPosition().x + 10.0F),
                                (double)info.getPosition().y + y,
                                Color.RED,
                                true,
                                0.3F
                        );
                y += Fonts.harmony.getHeight(true, 0.3F);
            }

            if (this.debug.getCurrentValue() && info.getPlayer() != null) {
                AbstractClientPlayer player = info.getPlayer();
                OrderedHashSet<String> debugInfos = new OrderedHashSet();
                debugInfos.add("X: " + player.getX());
                debugInfos.add("Y: " + player.getY());
                debugInfos.add("Z: " + player.getZ());
                debugInfos.add("Ticks: " + player.tickCount);

                for (String debugInfo : debugInfos) {
                    Fonts.harmony.render(e.getStack(), debugInfo, (double)(info.getPosition().x + 10.0F), (double)info.getPosition().y + y, Color.RED, true, 0.35F);
                    y += Fonts.harmony.getHeight(true, 0.35F);
                }
            }

            e.getStack().popPose();
        }

        // 渲染新的 PlayerList
        if (playerlist.getCurrentValue() && shouldRenderPlayerList) {
            renderPlayerListHud(e.getStack());
        }
    }

    private void renderPlayerListHud(PoseStack matrix) {
        if (currentWidth > 0.1F && currentHeight > 0.1F) {
            float animX = playerListX.getCurrentValue() + (finalWidth - currentWidth) / 2.0f;
            float animY = playerListY.getCurrentValue() + (finalHeight - currentHeight) / 2.0f;

            // 更新 blurMatrix 用于 bloom 效果
            blurMatrix = new org.joml.Vector4f(
                playerListX.getCurrentValue(),
                playerListY.getCurrentValue(),
                playerListX.getCurrentValue() + finalWidth,
                playerListY.getCurrentValue() + finalHeight
            );

            // 使用 MidPearl 的渲染方式
            renderPlayerListMark(matrix, animX, animY, currentWidth, currentHeight);
        } else {
            blurMatrix = null;
        }
    }

    private void renderPlayerListMark(PoseStack matrix, float x, float y, float width, float height) {
        // 使用 PearlPrediction 的渲染风格
        matrix.pushPose();
        StencilUtils.write(false);
        RenderUtils.drawRoundedRect(matrix, x, y, width, height, 5.0F, -1);
        StencilUtils.erase(true);

        // 使用 PearlPrediction 的 headerHeight (3.0f)
        float headerHeight = 3.0f;
        float finalX = playerListX.getCurrentValue();
        float finalY = playerListY.getCurrentValue();

        // 使用 HUD 的颜色方案（与 PearlPrediction 一致）
        RenderUtils.fill(matrix, finalX, finalY, finalX + finalWidth, finalY + headerHeight, HUD.headerColor);
        RenderUtils.fill(matrix, finalX, finalY + headerHeight, finalX + finalWidth, finalY + finalHeight, HUD.bodyColor);

        // 渲染文本（使用 PearlPrediction 的文本渲染方式）
        float tY = finalY + headerHeight + 2.0f;
        float textRowHeight = (float)Fonts.harmony.getHeight(true, playerListScale.getCurrentValue());

        // 渲染标题（居中）
        String title = "Player List";
        float textXOffset = (finalWidth - Fonts.harmony.getWidth(title, playerListScale.getCurrentValue())) / 2;
        Fonts.harmony.render(matrix, title, finalX + textXOffset, tY, Color.WHITE, true, playerListScale.getCurrentValue());
        tY += textRowHeight * 0.875F;

        // 渲染玩家列表（使用 PearlPrediction 的渲染方式）
        for (Map.Entry<String, PlayerInfo> entry : playerMap.entrySet()) {
            String playerName = entry.getKey();
            PlayerInfo playerInfo = entry.getValue();
            String displayText = playerName + " (" + playerInfo.distance + "m)";

            // 绘制玩家名称和距离（左对齐）
            Fonts.harmony.render(matrix, displayText, finalX + 4, tY, Color.WHITE, true, playerListScale.getCurrentValue());

            // 绘制神器标签（如果有的话，右对齐）
            if (!playerInfo.tags.isEmpty()) {
                String tagsText = String.join(", ", playerInfo.tags);
                double tagsWidth = Fonts.harmony.getWidth(tagsText, playerListScale.getCurrentValue());
                float tagsX = finalX + finalWidth - 4 - (float) tagsWidth;

                // 如果有多个标签，用不同颜色渲染，否则用单个标签的颜色
                if (playerInfo.tags.size() == 1) {
                    Color tagColor = getTagColor(playerInfo.tags.get(0));
                    Fonts.harmony.render(matrix, tagsText, tagsX, tY, tagColor, true, playerListScale.getCurrentValue());
                } else {
                    // 多个标签时，用逗号分隔，每个标签用不同颜色
                    float currentX = tagsX;
                    for (int i = 0; i < playerInfo.tags.size(); i++) {
                        String tag = playerInfo.tags.get(i);
                        Color tagColor = getTagColor(tag);
                        Fonts.harmony.render(matrix, tag, currentX, tY, tagColor, true, playerListScale.getCurrentValue());
                        double tagWidth = Fonts.harmony.getWidth(tag, playerListScale.getCurrentValue());
                        currentX += (float) tagWidth;
                        if (i < playerInfo.tags.size() - 1) {
                            // 渲染逗号和空格
                            Fonts.harmony.render(matrix, ", ", currentX, tY, Color.WHITE, true, playerListScale.getCurrentValue());
                            currentX += Fonts.harmony.getWidth(", ", playerListScale.getCurrentValue());
                        }
                    }
                }
            }

            tY += textRowHeight * 0.875F;
        }

        StencilUtils.dispose();
        matrix.popPose();
    }

    @EventTarget
    public void onShader(EventShader e) {
        // PlayerList Bloom 渲染
        if (e.getType() == EventType.SHADOW && blurMatrix != null && playerlist.getCurrentValue() && shouldRenderPlayerList) {
            RenderUtils.drawRoundedRect(e.getStack(), blurMatrix.x(), blurMatrix.y(), 
                blurMatrix.z() - blurMatrix.x(), blurMatrix.w() - blurMatrix.y(), 5.0F, Integer.MIN_VALUE);
        }
    }

    private Color getTagColor(String tag) {
        return switch (tag) {
            case "God Axe" -> new Color(229, 17, 17);
            case "Enchanted GApple" -> new Color(255, 170, 0);
            case "End Crystal" -> new Color(147, 112, 219);
            case "KB Ball" -> new Color(23, 232, 62);
            case "KB Stick" -> new Color(23, 232, 62);
            case "Punch Bow" -> new Color(255, 69, 0);
            case "Power Bow" -> new Color(255, 215, 0);
            case "Totem" -> new Color(133, 132, 7);
            default -> Color.WHITE;
        };
    }

    // PlayerInfo 内部类
    private static class PlayerInfo {
        public final List<String> tags; // 改为支持多个神器标签
        public final int distance;

        public PlayerInfo(List<String> tags, int distance) {
            this.tags = tags != null ? tags : new ArrayList<>();
            this.distance = distance;
        }
    }

    // 原有的 TargetInfo 类保持不变
    private static class TargetInfo {
        AbstractClientPlayer player;
        Vector2f position;
        Set<String> description;

        public AbstractClientPlayer getPlayer() {
            return this.player;
        }

        public Vector2f getPosition() {
            return this.position;
        }

        public Set<String> getDescription() {
            return this.description;
        }

        public void setPlayer(AbstractClientPlayer player) {
            this.player = player;
        }

        public void setPosition(Vector2f position) {
            this.position = position;
        }

        public void setDescription(Set<String> description) {
            this.description = description;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof ItemTracker.TargetInfo other)) {
                return false;
            } else if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$player = this.getPlayer();
                Object other$player = other.getPlayer();
                if (this$player == null ? other$player == null : this$player.equals(other$player)) {
                    Object this$position = this.getPosition();
                    Object other$position = other.getPosition();
                    if (this$position == null ? other$position == null : this$position.equals(other$position)) {
                        Object this$description = this.getDescription();
                        Object other$description = other.getDescription();
                        return this$description == null ? other$description == null : this$description.equals(other$description);
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof ItemTracker.TargetInfo;
        }

        @Override
        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            Object $player = this.getPlayer();
            result = result * 59 + ($player == null ? 43 : $player.hashCode());
            Object $position = this.getPosition();
            result = result * 59 + ($position == null ? 43 : $position.hashCode());
            Object $description = this.getDescription();
            return result * 59 + ($description == null ? 43 : $description.hashCode());
        }

        @Override
        public String toString() {
            return "ItemTracker.TargetInfo(player=" + this.getPlayer() + ", position=" + this.getPosition() + ", description=" + this.getDescription() + ")";
        }

        public TargetInfo(AbstractClientPlayer player, Vector2f position, Set<String> description) {
            this.player = player;
            this.position = position;
            this.description = description;
        }
    }
}
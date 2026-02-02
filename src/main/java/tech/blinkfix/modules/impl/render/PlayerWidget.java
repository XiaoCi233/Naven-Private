package tech.blinkfix.modules.impl.render;

import tech.blinkfix.events.api.EventTarget;
import tech.blinkfix.events.api.types.EventType;
import tech.blinkfix.events.impl.EventMotion;
import tech.blinkfix.events.impl.EventRender2D;
import tech.blinkfix.modules.impl.misc.Teams;
import tech.blinkfix.utils.EntityWatcher;
import tech.blinkfix.utils.FriendManager;
import tech.blinkfix.utils.SmoothAnimationTimer;
import tech.blinkfix.utils.renderer.Fonts;
import tech.blinkfix.utils.renderer.text.CustomTextRenderer;
import tech.blinkfix.values.impl.BooleanValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.AbstractClientPlayer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlayerWidget {
    private static final Minecraft mc = Minecraft.getInstance();
    private final SmoothAnimationTimer height = new SmoothAnimationTimer(0);
    private final SmoothAnimationTimer width = new SmoothAnimationTimer(0);
    private final Map<String, String> playerMap = new LinkedHashMap<>();
    private boolean shouldRender = false;
    private final BooleanValue value;
    private final String name = "Players";
    
    // Position for the widget
    private float x = 10.0f;
    private float y = 10.0f;

    public PlayerWidget(BooleanValue value) {
        this.value = value;
    }

    @EventTarget
    public void onUpdate(EventMotion e) {
        if (e.getType() != EventType.PRE) return;
        
        playerMap.clear();

        if (mc.level != null && mc.player != null) {
            List<AbstractClientPlayer> players = new ArrayList<>(mc.level.players());
            players.removeIf(player -> player.getId() < 0);
            
            // Sort by distance to player
            players.sort((o1, o2) -> {
                double dist1 = mc.player.distanceTo(o1);
                double dist2 = mc.player.distanceTo(o2);
                return Double.compare(dist1, dist2);
            });

            for (AbstractClientPlayer player : players) {
                if (!Teams.isSameTeam(player) && !FriendManager.isFriend(player) && player != mc.player) {
                    int health = Math.round(player.getHealth() + player.getAbsorptionAmount());
                    double distance = mc.player.distanceTo(player);
                    
                    // Get player tags from EntityWatcher
                    Set<String> tags = EntityWatcher.getEntityTags(player);
                    String playerName = player.getName().getString();
                    
                    String displayName = "[" + Math.round(distance) + "m][" + health + "HP] " + playerName;
                    
                    // Check for special items/tags
                    if (tags.contains("God Axe")) {
                        playerMap.put(displayName, "God Axe");
                    } else if (tags.contains("Enchanted Golden Apple")) {
                        playerMap.put(displayName, "Enchanted GApple");
                    } else if (tags.contains("End Crystal")) {
                        playerMap.put(displayName, "End Crystal");
                    } else if (tags.contains("KB Ball")) {
                        playerMap.put(displayName, "KB Ball");
                    } else if (tags.contains("KB Stick")) {
                        playerMap.put(displayName, "KB Stick");
                    } else if (tags.contains("Punch Bow")) {
                        playerMap.put(displayName, "Punch Bow");
                    } else if (tags.contains("Power Bow")) {
                        playerMap.put(displayName, "Power Bow");
                    } else {
                        // Add player without special tags if they have any effects
                        if (!tags.isEmpty()) {
                            playerMap.put(displayName, "Suspicious");
                        }
                    }
                }
            }
        }

        shouldRender = (!playerMap.isEmpty() || mc.screen instanceof ChatScreen) && value.getCurrentValue();

        if (!shouldRender) {
            height.target = 0;
            width.target = 0;
        }
    }

    public void render(EventRender2D event) {
        CustomTextRenderer font = Fonts.harmony;
        
        // Draw title
        font.render(event.getStack(), name, x + 2, y + 2, Color.WHITE, true, 0.3);

        if (shouldRender) {
            height.target = 12;
            width.target = font.getWidth(name, 0.3);

            // Calculate maximum width needed
            for (Map.Entry<String, String> entry : playerMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                float textWidth = font.getWidth(key + " " + value, 0.3) + 10;
                width.target = Math.max(width.target, textWidth);
            }

            // Draw player entries
            float currentY = y + 2 + height.target;
            for (Map.Entry<String, String> entry : playerMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                // Draw player name
                font.render(event.getStack(), key, x + 2, currentY, Color.WHITE, true, 0.3);
                
                // Draw status on the right
                float statusX = x + 2 + width.target - font.getWidth(value, 0.3);
                font.render(event.getStack(), value, statusX, currentY, Color.WHITE, true, 0.3);

                currentY += 10;
                height.target += 10;
            }
        } else {
            height.target = 0;
            width.target = 0;
        }

        height.update(true);
        width.update(true);
    }

    public float getWidth() {
        return width.value + 6;
    }

    public float getHeight() {
        return height.value + 6;
    }

    public boolean shouldRender() {
        return shouldRender || width.value > 1 || height.value > 1;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}


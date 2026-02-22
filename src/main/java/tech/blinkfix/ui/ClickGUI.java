package tech.blinkfix.ui;

import tech.blinkfix.BlinkFix;
import tech.blinkfix.modules.Category;
import tech.blinkfix.modules.Module;
import tech.blinkfix.utils.SmoothAnimationTimer;
import tech.blinkfix.utils.skia.Skia;
import tech.blinkfix.utils.skia.context.SkiaContext;
import tech.blinkfix.utils.skia.font.Fonts;
import tech.blinkfix.utils.skia.font.Icon;
import tech.blinkfix.values.Value;
import tech.blinkfix.values.impl.BooleanValue;
import tech.blinkfix.values.impl.FloatValue;
import tech.blinkfix.values.impl.ModeValue;
import tech.blinkfix.values.impl.StringValue;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.Path;
import io.github.humbleui.skija.Shader;
import io.github.humbleui.types.Point;
import io.github.humbleui.types.Rect;

import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ClickGUI extends Screen {

    // ==================== Theme Colors ====================
    public static final class Theme {
        public Color background;
        public Color sidebar;
        public Color accent;
        public Color accentDim;
        public Color line;
        public Color textPrimary;
        public Color textSecondary;
        public Color moduleBackground;
        public Color moduleHover;
        public Color toggleOn;
        public Color toggleOff;
        public Color sliderTrack;
        public Color dropdown;
        public Color overlay;
        public Color topbar;
        public Color moduleOutline;
        public Color settingLine;

        public Theme(Color background, Color sidebar, Color accent, Color accentDim, Color line, Color textPrimary,
                Color textSecondary, Color moduleBackground, Color moduleHover, Color toggleOn, Color toggleOff,
                Color sliderTrack, Color dropdown, Color overlay, Color topbar, Color moduleOutline, Color settingLine) {
            this.background = background;
            this.sidebar = sidebar;
            this.accent = accent;
            this.accentDim = accentDim;
            this.line = line;
            this.textPrimary = textPrimary;
            this.textSecondary = textSecondary;
            this.moduleBackground = moduleBackground;
            this.moduleHover = moduleHover;
            this.toggleOn = toggleOn;
            this.toggleOff = toggleOff;
            this.sliderTrack = sliderTrack;
            this.dropdown = dropdown;
            this.overlay = overlay;
            this.topbar = topbar;
            this.moduleOutline = moduleOutline;
            this.settingLine = settingLine;
        }

        public static Theme defaultTheme() {
            return new Theme(
                    new Color(0, 12, 24),
                    new Color(8, 18, 34),
                    new Color(0, 187, 255),
                    new Color(0, 100, 160),
                    new Color(19, 28, 41),
                    new Color(255, 255, 255),
                    new Color(140, 150, 165),
                    new Color(14, 22, 36),
                    new Color(18, 28, 44),
                    new Color(0, 187, 255),
                    new Color(35, 45, 60),
                    new Color(30, 40, 55),
                    new Color(10, 18, 30),
                    new Color(0, 0, 0, 120),
                    new Color(11, 16, 23),
                    new Color(7, 27, 47),
                    new Color(4, 24, 51)
            );
        }
    }

    private static Theme theme = Theme.defaultTheme();
    private static Color BG_COLOR;
    private static Color SIDEBAR_COLOR;
    private static Color ACCENT;
    private static Color ACCENT_DIM;
    private static Color LINE_COLOR;
    private static Color TEXT_WHITE;
    private static Color TEXT_GRAY;
    private static Color MODULE_BG;
    private static Color MODULE_HOVER;
    private static Color TOGGLE_ON_BG;
    private static Color TOGGLE_OFF_BG;
    private static Color SLIDER_TRACK;
    private static Color DROPDOWN_BG;
    private static Color OVERLAY_BG;
    private static Color TOPBAR_COLOR;
    private static Color MODULE_OUTLINE;
    private static Color SETTING_LINE;

    static {
        applyTheme(theme);
    }

    // ==================== Layout Constants ====================
    private static final float BASE_PANEL_W = 560;
    private static final float BASE_PANEL_H = 420;
    private static final float MIN_PANEL_W = 460;
    private static final float MIN_PANEL_H = 320;
    private static final float SIDEBAR_W = 136;
    private static final float TOPBAR_H = 44;
    private static final float CAT_ITEM_H = 38;
    private static final float MODULE_H = 32;
    private static final float SETTING_H = 24;
    private static final float SLIDER_H = 30;
    private static final float PAD = 10;
    private static final float COLUMN_GAP = 10;
    private static final float RADIUS = 8;
    private static final float RESIZE_HANDLE = 10;

    // ==================== Category Icon Map ====================
    private static final Map<Category, String> CAT_ICONS = new LinkedHashMap<>() {
        {
            put(Category.COMBAT, Icons.COMBAT);
            put(Category.MOVEMENT, Icons.MOVEMENT);
            put(Category.RENDER, Icons.RENDER);
            put(Category.MISC, Icons.MISC);
        }
    };

    private static class Icons {
        static final String SEARCH = "\ue8b6";
        static final String SETTINGS = "\ue8b8";
        static final String VISIBILITY = "\ue8f4";
        static final String EXPAND_MORE = "\ue5cf";
        static final String EXPAND_LESS = "\ue5ce";
        static final String COMBAT = "\uea48"; // sports_mma or similar
        static final String MOVEMENT = "\ue566"; // directions_run
        static final String RENDER = "\ue8f4"; // visibility
        static final String MISC = "\ue8b8"; // settings
        static final String CATEGORY = "\ue5d2"; // menu
    }

    // ==================== State ====================
    public static float savedPanelX = -1, savedPanelY = -1;
    public static float savedPanelW = -1, savedPanelH = -1;
    private float panelX = 100, panelY = 50;
    private float panelW = BASE_PANEL_W, panelH = BASE_PANEL_H;
    private Category selectedCategory = Category.COMBAT;
    private Module bindingModule = null;
    private boolean dragging = false;
    private float dragOX, dragOY;
    private boolean resizing = false;
    private float resizeOX, resizeOY;
    private float resizeW, resizeH;
    private float scroll = 0, maxScroll = 0;
    private float scrollYVelocity = 0;
    private boolean mouseDown = false;
    private FloatValue draggingSlider = null;
    private ModeValue openedMode = null;
    private StringValue editingString = null;
    private final Set<Module> expanded = new HashSet<>();

    // Search
    private boolean searching = false;
    private String searchString = "";
    private final SmoothAnimationTimer searchAnim = new SmoothAnimationTimer(1.0F, 0.0F);
    private float categoryIndicatorY = -1;

    // ==================== Animations ====================
    private final SmoothAnimationTimer popupAnim = new SmoothAnimationTimer(100.0F, 0.0F);
    private final Map<Category, SmoothAnimationTimer> catAnims = new LinkedHashMap<>();
    private final Map<Module, SmoothAnimationTimer> toggleAnims = new HashMap<>(); // toggle switch pos
    private final Map<Module, SmoothAnimationTimer> expandAnims = new HashMap<>(); // height expansion
    private final Map<Module, SmoothAnimationTimer> alphaAnims = new HashMap<>(); // module alpha
    private final Map<Value, SmoothAnimationTimer> boolAnims = new HashMap<>();
    private final Map<FloatValue, SmoothAnimationTimer> sliderAnims = new HashMap<>(); // slider dot size

    // ==================== Module Cache ====================
    private final List<Module> visibleModules = new ArrayList<>();
    private String hoveredTooltip = null;

    // ==================== Constructor ====================
    public ClickGUI() {
        super(Component.nullToEmpty("BlinkFix"));
        for (Category cat : Category.values()) {
            catAnims.put(cat, new SmoothAnimationTimer(100.0F, 0.0F));
        }
        updateModuleLists();
    }

    public static void setTheme(Theme nextTheme) {
        if (nextTheme == null) {
            return;
        }
        theme = nextTheme;
        applyTheme(nextTheme);
    }

    public static Theme getTheme() {
        return theme;
    }

    // ==================== Lifecycle ====================
    @Override
    protected void init() {
        BlinkFix.getInstance().getEventManager().register(this);
        popupAnim.value = 0;
        if (savedPanelW > 0 && savedPanelH > 0) {
            panelW = savedPanelW;
            panelH = savedPanelH;
        } else {
            panelW = BASE_PANEL_W;
            panelH = BASE_PANEL_H;
        }
        if (savedPanelX < 0 || savedPanelY < 0) {
            panelX = (width - panelW) / 2;
            panelY = (height - panelH) / 2;
        } else {
            panelX = savedPanelX;
            panelY = savedPanelY;
        }
        searching = false;
        searchString = "";
        updateModuleLists();
    }

    @Override
    public void onClose() {
        savedPanelX = panelX;
        savedPanelY = panelY;
        savedPanelW = panelW;
        savedPanelH = panelH;
        BlinkFix.getInstance().getFileManager().save();
        BlinkFix.getInstance().getEventManager().unregister(this);
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void updateModuleLists() {
        visibleModules.clear();
        List<Module> all;

        if (searching && !searchString.isEmpty()) {
            String q = searchString.toLowerCase().replace(" ", "");
            all = BlinkFix.getInstance().getModuleManager().getModules().stream()
                    .filter(m -> m.getName().toLowerCase().replace(" ", "").contains(q))
                    .sorted((m1, m2) -> Integer.compare(m2.getValues().size(), m1.getValues().size()))
                    .collect(Collectors.toList());
        } else {
            all = BlinkFix.getInstance().getModuleManager().getModulesByCategory(selectedCategory);
            // Sort by settings count to balance columns somewhat
            all.sort((m1, m2) -> Integer.compare(m2.getValues().size(), m1.getValues().size()));
        }

        visibleModules.addAll(all);

        // Init animations for new modules
        for (Module m : all) {
            toggleAnims.putIfAbsent(m, new SmoothAnimationTimer(1.0F, m.isEnabled() ? 1.0F : 0.0F));
            expandAnims.putIfAbsent(m, new SmoothAnimationTimer(1.0F, expanded.contains(m) ? 1.0F : 0.0F));
            alphaAnims.putIfAbsent(m, new SmoothAnimationTimer(1.0F, 0.0F));
            for (Value v : m.getValues()) {
                if (v instanceof BooleanValue)
                    boolAnims.putIfAbsent(v,
                            new SmoothAnimationTimer(1.0F, ((BooleanValue) v).getCurrentValue() ? 1.0F : 0.0F));
                if (v instanceof FloatValue)
                    sliderAnims.putIfAbsent((FloatValue) v, new SmoothAnimationTimer(6.0F, 0.0F)); // 0 to 6
            }
        }
    }

    // ==================== Render ====================
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float pPartialTick) {
        tickAnimations();
        popupAnim.update(true);
        float scale = popupAnim.value / 100.0F;

        // Inertial scrolling
        if (Math.abs(scrollYVelocity) > 0.1) {
            scroll += scrollYVelocity;
            scrollYVelocity *= 0.85f;
            scroll = Math.min(0, Math.max(maxScroll, scroll));
        }

        hoveredTooltip = null;

        SkiaContext.draw(canvas -> {
            Skia.drawRect(0, 0, width, height, OVERLAY_BG);
            if (scale < 0.01F)
                return;

            float cx = panelX + panelW / 2;
            float cy = panelY + panelH / 2;
            Skia.save();
            Skia.scale(cx, cy, scale);

            // Panel Blur & Background
            Skia.drawRoundedBlur(panelX, panelY, panelW, panelH, RADIUS);
            Skia.drawRoundedRect(panelX, panelY, panelW, panelH, RADIUS, BG_COLOR);
            Skia.drawShadow(panelX, panelY, panelW, panelH, RADIUS);
            Skia.drawOutline(panelX, panelY, panelW, panelH, RADIUS, 1f, LINE_COLOR);

            drawSidebar(mouseX, mouseY);
            drawTopBar(mouseX, mouseY);
            drawContent(mouseX, mouseY);

            if (bindingModule != null)
                drawBindOverlay();

            // Tooltip
            if (hoveredTooltip != null) {
                drawTooltip(mouseX, mouseY, hoveredTooltip);
            }

            drawResizeHandle(mouseX, mouseY);

            Skia.restore();
        });

        if (dragging) {
            panelX = mouseX - dragOX;
            panelY = mouseY - dragOY;
        }
        if (resizing && mouseDown) {
            float maxW = Math.max(MIN_PANEL_W, width - 20);
            float maxH = Math.max(MIN_PANEL_H, height - 20);
            panelW = Math.max(MIN_PANEL_W, Math.min(maxW, resizeW + (mouseX - resizeOX)));
            panelH = Math.max(MIN_PANEL_H, Math.min(maxH, resizeH + (mouseY - resizeOY)));
        }
        if (draggingSlider != null && mouseDown) {
            // Logic moved to drawSliderSetting to ensure coordinate context
        }
    }

    private void tickAnimations() {
        for (Category c : Category.values()) {
            catAnims.get(c).update(c == selectedCategory);
        }
        searchAnim.update(searching);

        for (Module m : visibleModules) {
            SmoothAnimationTimer t = toggleAnims.get(m);
            if (t != null)
                t.update(m.isEnabled());

            SmoothAnimationTimer e = expandAnims.get(m);
            if (e != null)
                e.update(expanded.contains(m));

            SmoothAnimationTimer a = alphaAnims.get(m);
            if (a != null)
                a.update(true); // Fade in

            for (Value v : m.getValues()) {
                if (v instanceof BooleanValue) {
                    SmoothAnimationTimer ba = boolAnims.get(v);
                    if (ba != null)
                        ba.update(((BooleanValue) v).getCurrentValue());
                }
            }
        }
    }

    // ==================== Sidebar ====================
    private void drawSidebar(int mx, int my) {
        float sx = panelX, sy = panelY;
        Skia.save();
        Skia.clip(sx, sy, SIDEBAR_W, panelH, RADIUS, 0, 0, RADIUS);
        Skia.drawRect(sx, sy, SIDEBAR_W, panelH, SIDEBAR_COLOR);

        Font logo = Fonts.getMiSans(18);
        String logoText = BlinkFix.CLIENT_NAME.toUpperCase();
        Skia.drawText(logoText, sx + 12, sy + 12, ACCENT, logo);
        Skia.drawLine(sx + 12, sy + 42, sx + SIDEBAR_W - 12, sy + 42, 1, LINE_COLOR);

        float iy = sy + 52;
        int selectedIndex = 0;
        int index = 0;
        for (Category cat : Category.values()) {
            if (cat == selectedCategory) {
                selectedIndex = index;
                break;
            }
            index++;
        }
        float targetY = sy + 52 + selectedIndex * CAT_ITEM_H;
        if (categoryIndicatorY < 0) {
            categoryIndicatorY = targetY;
        }
        categoryIndicatorY += (targetY - categoryIndicatorY) * 0.25f;
        float selectAlpha = catAnims.getOrDefault(selectedCategory, new SmoothAnimationTimer(100.0F, 100.0F)).value / 100.0F;
        Color selectBg = new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), AC((int) (30 * selectAlpha)));
        Skia.drawRoundedRect(sx + 8, categoryIndicatorY, SIDEBAR_W - 16, CAT_ITEM_H, 6, selectBg);
        Skia.drawRoundedRect(sx + 4, categoryIndicatorY + 9, 3, CAT_ITEM_H - 18, 2, ACCENT);
        Font catFont = Fonts.getMiSans(12);
        Font iconFont = Fonts.getIconFill(18);

        for (Category cat : Category.values()) {
            boolean hov = hover(mx, my, sx + 8, iy, SIDEBAR_W - 16, CAT_ITEM_H);
            boolean sel = cat == selectedCategory;

            if (!sel && hov) {
                Skia.drawRoundedRect(sx + 8, iy, SIDEBAR_W - 16, CAT_ITEM_H, 6, new Color(255, 255, 255, 10));
            }

            String icon = CAT_ICONS.getOrDefault(cat, Icons.CATEGORY);
            Color ic = sel ? ACCENT : (hov ? TEXT_WHITE : TEXT_GRAY);
            Color tc = sel ? TEXT_WHITE : (hov ? TEXT_WHITE : TEXT_GRAY);

            Skia.drawText(icon, sx + 20, iy + (CAT_ITEM_H - 18) / 2, ic, iconFont);
            Skia.drawText(cat.getDisplayName(), sx + 46, iy + (CAT_ITEM_H - 12) / 2, tc, catFont);
            iy += CAT_ITEM_H;
        }

        // User info at bottom
        float uiy = sy + panelH - 42;
        Skia.drawLine(sx + 8, uiy, sx + SIDEBAR_W - 8, uiy, 1, LINE_COLOR);
        String userName = Minecraft.getInstance().getUser().getName();
        Font userFont = Fonts.getMiSans(11);
        Font subFont = Fonts.getMiSans(9);

        Skia.drawCircle(sx + 22, uiy + 20, 12, ACCENT_DIM);
        String initial = userName.substring(0, 1).toUpperCase();
        float iw = Skia.getStringWidth(initial, userFont);
        Skia.drawText(initial, sx + 22 - iw / 2, uiy + 15, TEXT_WHITE, userFont);
        Skia.drawText(userName, sx + 38, uiy + 12, TEXT_WHITE, userFont);
        Skia.drawText("Till: ", sx + 38, uiy + 24, TEXT_GRAY, subFont);
        float tillW = Skia.getStringWidth("Till: ", subFont);
        Skia.drawText("Lifetime", sx + 38 + tillW, uiy + 24, ACCENT, subFont);

        Skia.restore();
        Skia.drawLine(sx + SIDEBAR_W, sy, sx + SIDEBAR_W, sy + panelH, 1, LINE_COLOR);
    }

    // ==================== Top Bar ====================
    private void drawTopBar(int mx, int my) {
        float tx = panelX + SIDEBAR_W, ty = panelY;
        Skia.save();
        Skia.clip(tx, ty, panelW - SIDEBAR_W, TOPBAR_H, 0, RADIUS, 0, 0);
        Skia.drawRect(tx, ty, panelW - SIDEBAR_W, TOPBAR_H, TOPBAR_COLOR);
        Skia.restore();

        // Search Bar Area
        float searchW = 160;
        float searchX = panelX + panelW - searchW - 14;
        float searchY = ty + 10;
        float searchH = 24;

        boolean searchHov = hover(mx, my, searchX, searchY, searchW, searchH);

        Skia.drawRoundedRect(searchX, searchY, searchW, searchH, 4, new Color(20, 30, 45));
        Skia.drawOutline(searchX, searchY, searchW, searchH, 4, 1, searchHov || searching ? ACCENT : LINE_COLOR);

        Font f = Fonts.getMiSans(12);
        Font iconF = Fonts.getIconFill(14);

        if (searching || !searchString.isEmpty()) {
            String caret = searching && (System.currentTimeMillis() / 500 % 2 == 0) ? "|" : "";
            String display = Skia.getLimitText(searchString + caret, f, searchW - 24);
            Skia.save();
            Skia.clip(searchX + 4, searchY, searchW - 24, searchH, 0);
            Skia.drawText(display, searchX + 6, searchY + 8, TEXT_WHITE, f);
            Skia.restore();
        } else {
            Skia.drawText("Search", searchX + 6, searchY + 8, TEXT_GRAY, f);
        }

        // Search Icon
        Skia.drawText(Icons.SEARCH, searchX + searchW - 20, searchY + 5, searching ? ACCENT : TEXT_GRAY, iconF);

        // Category/Breadcrumb title
        if (!searching) {
            Skia.drawText(selectedCategory.getDisplayName(), tx + 16, ty + (TOPBAR_H - 14) / 2, TEXT_WHITE,
                    Fonts.getMiSans(14));
        } else {
            Skia.drawText("Searching Results", tx + 16, ty + (TOPBAR_H - 14) / 2, ACCENT, Fonts.getMiSans(14));
        }

        Skia.drawLine(tx, ty + TOPBAR_H, panelX + panelW, ty + TOPBAR_H, 1, LINE_COLOR);
    }

    // ==================== Content ====================
    private void drawContent(int mx, int my) {
        float cx = panelX + SIDEBAR_W, cy = panelY + TOPBAR_H;
        float cw = panelW - SIDEBAR_W, ch = panelH - TOPBAR_H;

        Skia.save();
        Skia.clip(cx, cy, cw, ch, 0, RADIUS, RADIUS, 0);

        float colW = (cw - PAD * 2 - COLUMN_GAP) / 2;
        float leftY = cy + PAD + scroll;
        float rightY = cy + PAD + scroll;

        ColumnLayout layout = buildColumns();
        leftY = drawColumn(layout.left, cx + PAD, leftY, colW, mx, my);
        rightY = drawColumn(layout.right, cx + PAD + colW + COLUMN_GAP, rightY, colW, mx, my);

        float maxY = Math.max(leftY, rightY);
        maxScroll = Math.min(0, -(maxY - scroll - cy - ch + PAD));
        Skia.restore();
    }

    private static final class ColumnLayout {
        private final List<Module> left;
        private final List<Module> right;

        private ColumnLayout(List<Module> left, List<Module> right) {
            this.left = left;
            this.right = right;
        }
    }

    private ColumnLayout buildColumns() {
        List<Module> left = new ArrayList<>();
        List<Module> right = new ArrayList<>();
        float leftH = 0;
        float rightH = 0;
        for (Module mod : visibleModules) {
            float h = moduleHeight(mod);
            if (leftH <= rightH) {
                left.add(mod);
                leftH += h;
            } else {
                right.add(mod);
                rightH += h;
            }
        }
        return new ColumnLayout(left, right);
    }

    private float moduleHeight(Module mod) {
        SmoothAnimationTimer ea = expandAnims.get(mod);
        float ep = ea != null ? ea.value : (expanded.contains(mod) ? 1f : 0f);
        float sh = (expanded.contains(mod) || ep > 0.01f) ? settingsH(mod) : 0;
        float eh = MODULE_H + sh * ep;
        return eh + 6;
    }

    private float drawColumn(List<Module> modules, float x, float startY, float w, int mx, int my) {
        float y = startY;
        Font nameF = Fonts.getMiSans(11);
        Font setF = Fonts.getMiSans(10);
        Font valF = Fonts.getMiSans(9);

        for (Module mod : modules) {
            SmoothAnimationTimer ea = expandAnims.get(mod);
            float ep = ea != null ? ea.value : 0;
            float sh = (expanded.contains(mod) || ep > 0.01f) ? settingsH(mod) : 0;
            float eh = MODULE_H + sh * ep;

            // Alpha fade in
            float alpha = alphaAnims.get(mod) != null ? alphaAnims.get(mod).value : 1f;
            if (alpha < 0.05f) {
                y += eh + 4;
                continue;
            } // Skip invisible

            // We can use Skia.setAlpha but simpler to just mod colors or not handle alpha
            // for layout speed
            // Properly implementing alpha stacking in skia requires saveLayer

            boolean hov = hover(mx, my, x, y, w, eh);

            // Background
            Skia.drawRoundedRect(x, y, w, eh, 6, hov ? MODULE_HOVER : MODULE_BG);
            Skia.drawOutline(x, y, w, eh, 6, 1, MODULE_OUTLINE);

            Skia.save();
            Skia.clip(x, y, w, eh, 6); // Clip content inside module card

            // Module Name & Toggle
            Skia.drawText(mod.getPrettyName(), x + 10, y + (MODULE_H - 11) / 2, TEXT_WHITE, nameF);

            // Toggle Switch
            drawToggle(mod, x + w - 38, y + (MODULE_H - 16) / 2, mx, my);

            // Settings Icon / Expand Arrow
            Font arrowF = Fonts.getIconFill(14);
            boolean isExp = expanded.contains(mod);
            // Skia.drawText(isExp ? Icon.EXPAND_LESS : Icon.EXPAND_MORE, x + w - 55, y +
            // (MODULE_H - 14) / 2, TEXT_GRAY, arrowF);
            // Let's make entire header clickable for expand except toggle

            // Keybind
            if (mod.getKey() != 0) {
                String keyName = GLFW.glfwGetKeyName(mod.getKey(), 0);
                if (keyName == null)
                    keyName = String.valueOf(mod.getKey());
                keyName = "[" + keyName.toUpperCase() + "]";
                float kw = Skia.getStringWidth(keyName, valF);
                Skia.drawText(keyName, x + w - 50 - kw, y + (MODULE_H - 9) / 2, TEXT_GRAY, valF);
            }

            // Description Tooltip logic
            if (hov && my < y + MODULE_H && hover(mx, my, x, y, w - 40, MODULE_H)) {
                if (mod.getDescription() != null && !mod.getDescription().isEmpty())
                    hoveredTooltip = mod.getDescription();
            }

            // Settings
            if (ep > 0.01f) {
                float sy = y + MODULE_H;
                float setAlpha = ep; // use opacity too

                // Divider
                Skia.drawLine(x + 10, sy, x + w - 10, sy, 1, new Color(LINE_COLOR.getRed(), LINE_COLOR.getGreen(),
                        LINE_COLOR.getBlue(), (int) (255 * 0.5 * setAlpha)));
                sy += 4;

                List<Value> vals = BlinkFix.getInstance().getValueManager().getValuesByHasValue(mod);
                for (Value v : vals) {
                    if (!v.isVisible())
                        continue;

                    // Helper for setting background hover
                    boolean sHov = hover(mx, my, x + 4, sy, w - 8,
                            v instanceof ModeValue && openedMode == v.getModeValue() ? 20
                                    : (v instanceof BooleanValue ? SETTING_H
                                            : (v instanceof FloatValue ? SLIDER_H : SETTING_H)));

                    switch (v.getValueType()) {
                        case BOOLEAN -> {
                            drawBoolSetting(v.getBooleanValue(), v, x, sy, w, setF, mx, my);
                            sy += SETTING_H;
                        }
                        case FLOAT -> {
                            drawSliderSetting(v.getFloatValue(), x, sy, w, setF, valF, mx, my);
                            sy += SLIDER_H;
                        }
                        case MODE -> {
                            sy += drawModeSetting(v.getModeValue(), x, sy, w, setF, mx, my);
                        }
                        case STRING -> {
                            drawStringSetting(v.getStringValue(), x, sy, w, setF, mx, my);
                            sy += SETTING_H;
                        }
                        default -> {
                        }
                    }
                }
            }

            Skia.restore();
            y += eh + 6; // Gap between modules
        }
        return y;
    }

    // ==================== Components ====================

    private void drawToggle(Module mod, float x, float y, int mx, int my) {
        float w = 30, h = 16, r = h / 2;
        SmoothAnimationTimer a = toggleAnims.get(mod);
        float p = a != null ? a.value : (mod.isEnabled() ? 1 : 0);

        Color bg = lerp(TOGGLE_OFF_BG, TOGGLE_ON_BG, p);
        Skia.drawRoundedRect(x, y, w, h, r, bg);

        float circleX = x + 3 + (w - 14) * p; // 3 padding
        Skia.drawCircle(circleX + 4, y + 8, 4, TEXT_WHITE);
    }

    private void drawBoolSetting(BooleanValue bv, Value v, float x, float y, float w, Font f, int mx, int my) {
        Skia.drawText(bv.getName(), x + 12, y + (SETTING_H - 10) / 2, TEXT_GRAY, f);
        float tw = 24, th = 12, tx = x + w - tw - 12, ty = y + (SETTING_H - th) / 2;
        SmoothAnimationTimer a = boolAnims.get(v);
        float p = a != null ? a.value : (bv.getCurrentValue() ? 1 : 0);

        Skia.drawRoundedRect(tx, ty, tw, th, th / 2, lerp(TOGGLE_OFF_BG, ACCENT, p));
        Skia.drawCircle(tx + 4 + (tw - 8) * p, ty + th / 2, 3, TEXT_WHITE);
    }

    private void drawSliderSetting(FloatValue fv, float x, float y, float w, Font f, Font vf, int mx, int my) {
        Skia.drawText(fv.getName(), x + 12, y + 4, TEXT_GRAY, f);

        String vs = String.format(fv.getStep() % 1 == 0 ? "%.0f" : "%.2f", fv.getCurrentValue());
        float vw = Skia.getStringWidth(vs, vf);
        Skia.drawText(vs, x + w - vw - 12, y + 4, TEXT_WHITE, vf);

        float tx = x + 12, ty = y + SLIDER_H - 12, tw = w - 24, th = 3;
        Skia.drawRoundedRect(tx, ty, tw, th, 1.5f, SLIDER_TRACK);

        // Input Handling
        if (draggingSlider == fv && mouseDown) {
            float diff = Math.min(tw, Math.max(0, mx - tx));
            float min = fv.getMinValue();
            float max = fv.getMaxValue();
            float val = min + (diff / tw) * (max - min);
            if (fv.getStep() > 0)
                val = Math.round(val / fv.getStep()) * fv.getStep();
            fv.setCurrentValue(val);
        }

        float range = fv.getMaxValue() - fv.getMinValue();
        float pct = range > 0 ? (fv.getCurrentValue() - fv.getMinValue()) / range : 0;

        if (pct > 0)
            Skia.drawRoundedRect(tx, ty, tw * pct, th, 1.5f, ACCENT);

        // Slider Dot Animation
        SmoothAnimationTimer sa = sliderAnims.get(fv);
        boolean hoverDot = hover(mx, my, tx, ty - 5, tw, 13) || draggingSlider == fv;
        if (sa != null)
            sa.update(hoverDot);
        float dotR = sa != null ? 3 + sa.value * 0.5f : 3; // 4 to 8

        Skia.drawCircle(tx + tw * pct, ty + th / 2, dotR, TEXT_WHITE);
        // Glow effect for dot
        if (hoverDot)
            Skia.drawCircle(tx + tw * pct, ty + th / 2, dotR + 3, new Color(255, 255, 255, 50));
    }

    private float drawModeSetting(ModeValue mv, float x, float y, float w, Font f, int mx, int my) {
        float h = SETTING_H;
        Skia.drawText(mv.getName(), x + 12, y + (h - 10) / 2, TEXT_GRAY, f);

        float bw = 70, bh = 16, bx = x + w - bw - 12, by = y + (h - bh) / 2;
        boolean open = openedMode == mv;

        Skia.drawRoundedRect(bx, by, bw, bh, 4, DROPDOWN_BG);
        Skia.drawOutline(bx, by, bw, bh, 4, 1, open ? ACCENT_DIM : LINE_COLOR);

        String cur = mv.getCurrentMode();
        // Clip text
        Skia.save();
        Skia.clip(bx + 4, by, bw - 16, bh, 0);
        Skia.drawText(cur, bx + 6, by + 4, TEXT_WHITE, f);
        Skia.restore();

        Font iconF = Fonts.getIconFill(10);
        Skia.drawText(open ? Icons.EXPAND_LESS : Icons.EXPAND_MORE, bx + bw - 12, by + 4, TEXT_GRAY, iconF);

        if (open) {
            String[] modes = mv.getValues();
            float dh = modes.length * 18 + 4;
            // Draw dropdown on top (needs high z-index concept, but we use painter
            // algorithm)
            // Ideally should be drawn last, but for simplicity here we assume it overlaps
            // OK or structure handles it.
            // Actually in `drawColumn` we clip modules. If dropdown extends, it gets
            // clipped!
            // FIX: Dropdowns can't work easily inside clipped modules without rendering
            // overlay separately.
            // For now, let's just grow the setting height so it pushes down?
            // "Grow push" style is easier in this architecture than "Overlay".

            float oy = by + bh + 2;
            Skia.drawRoundedRect(bx, oy, bw, dh, 4, DROPDOWN_BG);
            Skia.drawOutline(bx, oy, bw, dh, 4, 1, LINE_COLOR);

            for (String m : modes) {
                boolean oh = hover(mx, my, bx, oy, bw, 18);
                boolean sel = m.equals(mv.getCurrentMode());
                if (oh)
                    Skia.drawRoundedRect(bx + 1, oy, bw - 2, 18, 3, new Color(255, 255, 255, 10));
                Skia.drawText(m, bx + 6, oy + 5, sel ? ACCENT : (oh ? TEXT_WHITE : TEXT_GRAY), f);
                oy += 18;
            }
            return h + dh + 4;
        }

        return h;
    }

    private void drawStringSetting(StringValue sv, float x, float y, float w, Font f, int mx, int my) {
        Skia.drawText(sv.getName(), x + 12, y + (SETTING_H - 10) / 2, TEXT_GRAY, f);

        float tw = 80, th = 16, tx = x + w - tw - 12, ty = y + (SETTING_H - th) / 2;
        boolean editing = editingString == sv;

        Skia.drawRoundedRect(tx, ty, tw, th, 4, DROPDOWN_BG);
        Skia.drawOutline(tx, ty, tw, th, 4, 1, editing ? ACCENT : LINE_COLOR);

        String txt = sv.getCurrentValue();
        if (editing && (System.currentTimeMillis() / 500 % 2 == 0))
            txt += "_";

        Skia.save();
        Skia.clip(tx + 2, ty, tw - 4, th, 0);
        Skia.drawText(txt.isEmpty() && !editing ? "Type..." : txt, tx + 4, ty + 4,
                editing || !txt.isEmpty() ? TEXT_WHITE : Color.DARK_GRAY, f);
        Skia.restore();
    }

    private void drawBindOverlay() {
        Skia.drawRoundedBlur(panelX, panelY, panelW, panelH, RADIUS);
        Skia.drawRoundedRect(panelX, panelY, panelW, panelH, RADIUS, new Color(0, 0, 0, 200));

        Font f = Fonts.getMiSans(16);
        String t = "Binding " + bindingModule.getPrettyName();
        float tw = Skia.getStringWidth(t, f);
        Skia.drawText(t, panelX + (panelW - tw) / 2, panelY + panelH / 2 - 10, ACCENT, f);

        Font sf = Fonts.getMiSans(12);
        String s = "Press any key (ESC to cancel, DEL to unbind)";
        float sw = Skia.getStringWidth(s, sf);
        Skia.drawText(s, panelX + (panelW - sw) / 2, panelY + panelH / 2 + 10, TEXT_GRAY, sf);
    }

    private void drawTooltip(int mx, int my, String text) {
        if (text == null || text.isEmpty())
            return;
        Font f = Fonts.getMiSans(11);
        float tw = Skia.getStringWidth(text, f);
        float padding = 6;
        float w = tw + padding * 2;
        float h = 18;
        float x = mx + 8;
        float y = my + 8;

        Skia.drawRoundedBlur(x, y, w, h, 4);
        Skia.drawRoundedRect(x, y, w, h, 4, new Color(0, 0, 0, 200));
        Skia.drawOutline(x, y, w, h, 4, 1, LINE_COLOR);
        Skia.drawText(text, x + padding, y + 4, TEXT_WHITE, f);
    }

    private void drawResizeHandle(int mx, int my) {
        float x = panelX + panelW - RESIZE_HANDLE - 4;
        float y = panelY + panelH - RESIZE_HANDLE - 4;
        boolean hov = hover(mx, my, x, y, RESIZE_HANDLE, RESIZE_HANDLE);
        Color base = hov || resizing ? ACCENT : LINE_COLOR;
        Color glow = new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), hov || resizing ? 140 : 80);
        Skia.drawShadow(x - 2, y - 2, RESIZE_HANDLE + 4, RESIZE_HANDLE + 4, 4, glow);
        Skia.drawShadow(x - 1, y - 1, RESIZE_HANDLE + 2, RESIZE_HANDLE + 2, 3, new Color(0, 0, 0, 110));
        Skia.drawRoundedRect(x, y, RESIZE_HANDLE, RESIZE_HANDLE, 2, base);
        Skia.drawOutline(x, y, RESIZE_HANDLE, RESIZE_HANDLE, 2, 1, TEXT_WHITE);
        Skia.drawOutline(x + 1, y + 1, RESIZE_HANDLE - 2, RESIZE_HANDLE - 2, 1, 1, new Color(255, 255, 255, 90));

        int c1 = io.github.humbleui.skija.Color.makeARGB(200, ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue());
        int c2 = io.github.humbleui.skija.Color.makeARGB(80, LINE_COLOR.getRed(), LINE_COLOR.getGreen(), LINE_COLOR.getBlue());
        Path tri = new Path();
        tri.moveTo(x + RESIZE_HANDLE, y + RESIZE_HANDLE);
        tri.lineTo(x + RESIZE_HANDLE, y);
        tri.lineTo(x, y + RESIZE_HANDLE);
        tri.closePath();
        Paint paint = new Paint();
        paint.setShader(Shader.makeLinearGradient(new Point(x, y), new Point(x + RESIZE_HANDLE, y + RESIZE_HANDLE), new int[]{c1, c2}, new float[]{0f, 1f}));
        Skia.getCanvas().drawPath(tri, paint);

        Color line = hov || resizing ? TEXT_WHITE : TEXT_GRAY;
        for (int i = 0; i < 3; i++) {
            float o = 2 + i * 3;
            Skia.drawLine(x + RESIZE_HANDLE - o, y + RESIZE_HANDLE, x + RESIZE_HANDLE, y + RESIZE_HANDLE - o, 1, line);
        }
        Skia.drawLine(x + RESIZE_HANDLE - 2, y + RESIZE_HANDLE, x + RESIZE_HANDLE, y + RESIZE_HANDLE - 2, 1, new Color(255, 255, 255, 160));
    }

    // ==================== Inputs ====================
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX, my = (int) mouseY;
        if (button == 0)
            mouseDown = true;

        // Search Click
        float searchW = 160;
        float searchX = panelX + panelW - searchW - 14;
        float searchY = panelY + 10;
        if (hover(mx, my, searchX, searchY, searchW, 24)) {
            if (button == 0) {
                searching = !searching;
                if (searching) {
                    // Keep string
                } else {
                    searchString = "";
                    updateModuleLists();
                }
            } else if (button == 1) { // Right click clear
                searchString = "";
                updateModuleLists();
            }
            return true;
        }

        if (button == 0 && hover(mx, my, panelX + panelW - RESIZE_HANDLE - 4, panelY + panelH - RESIZE_HANDLE - 4, RESIZE_HANDLE, RESIZE_HANDLE)) {
            resizing = true;
            resizeOX = mx;
            resizeOY = my;
            resizeW = panelW;
            resizeH = panelH;
            return true;
        }

        if (bindingModule != null)
            return true;

        // Panel Drag
        if (button == 0 && !dragging && hover(mx, my, panelX, panelY, panelW, TOPBAR_H)) {
            dragging = true;
            dragOX = mx - panelX;
            dragOY = my - panelY;
            return true;
        }

        // Sidebar
        if (hover(mx, my, panelX, panelY + 52, SIDEBAR_W, panelH - 52)) {
            float catY = panelY + 52;
            for (Category cat : Category.values()) {
                if (hover(mx, my, panelX + 8, catY, SIDEBAR_W - 16, CAT_ITEM_H)) {
                    if (button == 0) {
                        selectedCategory = cat;
                        scroll = 0;
                        searching = false;
                        searchString = ""; // Exit search on cat change
                        updateModuleLists();
                    }
                    return true;
                }
                catY += CAT_ITEM_H;
            }
        }

        // Content Area
        float cx = panelX + SIDEBAR_W;
        float cy = panelY + TOPBAR_H;
        float cw = panelW - SIDEBAR_W;
        float ch = panelH - TOPBAR_H;

        if (hover(mx, my, cx, cy, cw, ch)) {
            float colW = (cw - PAD * 2 - COLUMN_GAP) / 2;
            ColumnLayout layout = buildColumns();

            // Handle clicks in columns
            if (handleColumnClick(layout.left, cx + PAD, cy + PAD + scroll, colW, mx, my, button))
                return true;
            if (handleColumnClick(layout.right, cx + PAD + colW + COLUMN_GAP, cy + PAD + scroll, colW, mx, my, button))
                return true;

            // Close opened things if clicked empty
            if (button == 0) {
                openedMode = null;
                editingString = null;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleColumnClick(List<Module> modules, float x, float startY, float w, int mx, int my,
            int button) {
        float y = startY;
        for (Module mod : modules) {
            SmoothAnimationTimer ea = expandAnims.get(mod);
            float ep = ea != null ? ea.value : 0;
            float sh = (expanded.contains(mod) || ep > 0.01f) ? settingsH(mod) : 0;
            float eh = MODULE_H + sh * ep;

            boolean visible = alphaAnims.get(mod) == null || alphaAnims.get(mod).value > 0.05f;
            if (!visible) {
                y += eh + 6;
                continue;
            }

            // Module Header
            if (hover(mx, my, x, y, w, MODULE_H)) {
                // Toggle Button
                if (button == 0 && hover(mx, my, x + w - 40, y, 40, MODULE_H)) {
                    mod.toggle();
                    return true;
                }
                // Expand
                if (button == 1 || (button == 0 && hover(mx, my, x, y, w - 40, MODULE_H))) {
                    if (expanded.contains(mod))
                        expanded.remove(mod);
                    else
                        expanded.add(mod);
                    return true;
                }
                // Bind
                if (button == 2) {
                    bindingModule = mod;
                    return true;
                }
            }

            // Settings
            if (expanded.contains(mod) && ep > 0.5f) {
                float sy = y + MODULE_H + 4;
                for (Value v : mod.getValues()) {
                    if (!v.isVisible())
                        continue;
                    float h = SETTING_H;
                    if (v instanceof FloatValue)
                        h = SLIDER_H;
                    if (v instanceof ModeValue && openedMode == v.getModeValue()) {
                        h += ((ModeValue) v).getValues().length * 18 + 4;
                    }

                    if (v instanceof BooleanValue) {
                        if (button == 0 && hover(mx, my, x + w - 40, sy, 30, h)) {
                            boolean current = ((BooleanValue) v).getCurrentValue();
                            ((BooleanValue) v).setCurrentValue(!current);
                            return true;
                        }
                    } else if (v instanceof FloatValue) {
                        if (button == 0 && hover(mx, my, x, sy, w, h)) {
                            draggingSlider = (FloatValue) v;
                            return true;
                        }
                    } else if (v instanceof ModeValue) {
                        ModeValue mv = (ModeValue) v;
                        if (button == 0 && hover(mx, my, x + w - 85, sy, 72, 16)) {
                            if (openedMode == mv)
                                openedMode = null;
                            else
                                openedMode = mv;
                            return true;
                        }
                        if (openedMode == mv && button == 0) {
                            // Check dropdown list
                            float dy = sy + (SETTING_H - 16) / 2 + 18;
                            for (int i = 0; i < mv.getValues().length; i++) {
                                if (hover(mx, my, x + w - 85, dy + i * 18, 70, 18)) {
                                    mv.setCurrentValue(i);
                                    openedMode = null;
                                    return true;
                                }
                            }
                        }
                    } else if (v instanceof StringValue) {
                        if (button == 0 && hover(mx, my, x + w - 95, sy, 82, 16)) {
                            editingString = (StringValue) v;
                            return true;
                        }
                    }

                    sy += h;
                }
            }

            y += eh + 6;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            mouseDown = false;
            dragging = false;
            draggingSlider = null;
            resizing = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (hover((int) mouseX, (int) mouseY, panelX, panelY, panelW, panelH)) {
            scrollYVelocity += delta * 15;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (bindingModule != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE)
                bindingModule = null;
            else if (keyCode == GLFW.GLFW_KEY_DELETE) {
                bindingModule.setKey(0);
                bindingModule = null;
            } else {
                bindingModule.setKey(keyCode);
                bindingModule = null;
            }
            return true;
        }

        if (searching) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searching = false;
                searchString = "";
                updateModuleLists();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchString.isEmpty()) {
                searchString = searchString.substring(0, searchString.length() - 1);
                updateModuleLists();
                return true;
            }
            if (Screen.isPaste(keyCode)) {
                String paste = GLFW.glfwGetClipboardString(Minecraft.getInstance().getWindow().getWindow());
                if (paste != null && !paste.isEmpty()) {
                    searchString += paste;
                    updateModuleLists();
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                searching = false;
                searchString = "";
                updateModuleLists();
                return true;
            }
            return true;
        }

        if (editingString != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
                editingString = null;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                String val = editingString.getCurrentValue();
                if (!val.isEmpty())
                    editingString.setCurrentValue(val.substring(0, val.length() - 1));
                return true;
            }
            // Ctrl+V handled in charTyped usually or here
            if (Screen.isPaste(keyCode)) {
                String val = editingString.getCurrentValue();
                editingString.setCurrentValue(
                        val + GLFW.glfwGetClipboardString(Minecraft.getInstance().getWindow().getWindow()));
                return true;
            }
            return true;
        }

        // Ctrl+F for Search
        if (keyCode == GLFW.GLFW_KEY_F && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            searching = !searching;
            if (!searching) {
                searchString = "";
                updateModuleLists();
            }
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searching) {
            searchString += codePoint;
            updateModuleLists();
            return true;
        }
        if (editingString != null) {
            String val = editingString.getCurrentValue();
            editingString.setCurrentValue(val + codePoint);
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    // ==================== Helpers ====================
    private static void applyTheme(Theme t) {
        BG_COLOR = t.background;
        SIDEBAR_COLOR = t.sidebar;
        ACCENT = t.accent;
        ACCENT_DIM = t.accentDim;
        LINE_COLOR = t.line;
        TEXT_WHITE = t.textPrimary;
        TEXT_GRAY = t.textSecondary;
        MODULE_BG = t.moduleBackground;
        MODULE_HOVER = t.moduleHover;
        TOGGLE_ON_BG = t.toggleOn;
        TOGGLE_OFF_BG = t.toggleOff;
        SLIDER_TRACK = t.sliderTrack;
        DROPDOWN_BG = t.dropdown;
        OVERLAY_BG = t.overlay;
        TOPBAR_COLOR = t.topbar;
        MODULE_OUTLINE = t.moduleOutline;
        SETTING_LINE = t.settingLine;
    }

    private float settingsH(Module mod) {
        float h = 4;
        List<Value> vals = BlinkFix.getInstance().getValueManager().getValuesByHasValue(mod);
        for (Value v : vals) {
            if (!v.isVisible())
                continue;
            switch (v.getValueType()) {
                case BOOLEAN -> h += SETTING_H;
                case FLOAT -> h += SLIDER_H;
                case STRING -> h += SETTING_H;
                case MODE -> {
                    h += SETTING_H;
                    if (openedMode == v.getModeValue())
                        h += v.getModeValue().getValues().length * 18 + 4;
                }
                default -> {
                }
            }
        }
        return h;
    }

    private static boolean hover(int mx, int my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private static Color lerp(Color a, Color b, float t) {
        t = Math.max(0, Math.min(1, t));
        return new Color(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t),
                (int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
    }

    private int AC(int alpha) { // alpha clamp
        return Math.max(0, Math.min(255, alpha));
    }
}

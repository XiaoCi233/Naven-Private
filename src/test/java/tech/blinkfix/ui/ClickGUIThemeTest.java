package tech.blinkfix.ui;

import org.junit.jupiter.api.Test;
import java.awt.Color;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ClickGUIThemeTest {
    @Test
    void setThemeUpdatesReference() {
        ClickGUI.Theme custom = new ClickGUI.Theme(
                new Color(1, 2, 3),
                new Color(4, 5, 6),
                new Color(7, 8, 9),
                new Color(10, 11, 12),
                new Color(13, 14, 15),
                new Color(16, 17, 18),
                new Color(19, 20, 21),
                new Color(22, 23, 24),
                new Color(25, 26, 27),
                new Color(28, 29, 30),
                new Color(31, 32, 33),
                new Color(34, 35, 36),
                new Color(37, 38, 39),
                new Color(40, 41, 42, 120),
                new Color(43, 44, 45),
                new Color(46, 47, 48),
                new Color(49, 50, 51)
        );
        try {
            ClickGUI.setTheme(custom);
            assertSame(custom, ClickGUI.getTheme());
        } finally {
            ClickGUI.setTheme(ClickGUI.Theme.defaultTheme());
        }
    }
}

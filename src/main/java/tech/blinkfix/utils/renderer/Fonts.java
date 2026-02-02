package tech.blinkfix.utils.renderer;

import tech.blinkfix.utils.renderer.text.CustomTextRenderer;
import java.awt.FontFormatException;
import java.io.IOException;

public class Fonts {
   public static CustomTextRenderer opensans;
   public static CustomTextRenderer harmony;
   public static CustomTextRenderer icons;
   public static CustomTextRenderer youyuan;
    public static CustomTextRenderer test;
    public static CustomTextRenderer tenacity;

    public static void loadFonts() throws IOException, FontFormatException {
      opensans = new CustomTextRenderer("opensans", 32, 0, 255, 512);
      harmony = new CustomTextRenderer("harmony", 32, 0, 65535, 16384);
      icons = new CustomTextRenderer("icon", 32, 59648, 59652, 512);
      youyuan = new CustomTextRenderer("youyuan", 32, 0, 255, 512);
      // test 字体：如果是英文字体用这个，如果是中文字体改为 (32, 0, 65535, 16384)
      test = new CustomTextRenderer("test", 32, 0, 255, 512);
        tenacity = new CustomTextRenderer("tenacity", 32, 0, 255, 512);
   }
}

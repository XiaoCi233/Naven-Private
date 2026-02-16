package tech.blinkfix.utils.renderer;

import tech.blinkfix.utils.shader.Shader;

public class Shaders {
   public static Shader TEXT;

   public static void init() {
      TEXT = new Shader("text.vert", "text.frag");
   }
}

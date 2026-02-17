package tech.blinkfix.files.impl;

import tech.blinkfix.files.ClientFile;
import tech.blinkfix.ui.ClickGUI;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class CGuiFile extends ClientFile {
   public CGuiFile() {
      super("clickgui.cfg");
   }

   @Override
   public void read(BufferedReader reader) throws IOException {
      try {
         ClickGUI.savedPanelX = (float) Integer.parseInt(reader.readLine());
         ClickGUI.savedPanelY = (float) Integer.parseInt(reader.readLine());
      } catch (Exception ignored) {
      }
   }

   @Override
   public void save(BufferedWriter writer) throws IOException {
      writer.write((int) ClickGUI.savedPanelX + "\n");
      writer.write((int) ClickGUI.savedPanelY + "\n");
   }
}

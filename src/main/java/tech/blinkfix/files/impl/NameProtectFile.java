package tech.blinkfix.files.impl;

import tech.blinkfix.files.ClientFile;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.blinkfix.modules.impl.render.NameProtect;

public class NameProtectFile extends ClientFile {
    private static final Logger log = LogManager.getLogger(NameProtectFile.class);
    public static String customName = "§d塞西莉亚宝宝§7"; // 默认名称与模块中一致

    public NameProtectFile() {
        super("nameprotect.cfg");
    }

    @Override
    public void read(BufferedReader reader) throws IOException {
        try {
            String name = reader.readLine();
            if (name != null && !name.trim().isEmpty()) {
                customName = name.trim();
                log.info("Loaded custom name: {}", customName);

                // 如果NameProtect模块已经实例化，同步设置名称
                if (NameProtect.instance != null) {
                    NameProtect.instance.setCustomName(customName);
                }
            }
        } catch (Exception var3) {
            log.error("Failed to read nameprotect file!", var3);
        }
    }

    @Override
    public void save(BufferedWriter writer) throws IOException {
        writer.write(customName);
    }
}
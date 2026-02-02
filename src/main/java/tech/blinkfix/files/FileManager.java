package tech.blinkfix.files;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.blinkfix.files.impl.*;

public class FileManager {
    public static final Logger logger = LogManager.getLogger(FileManager.class);
    public static final File clientFolder;
    public static Object trash = new BigInteger("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);
    private final List<ClientFile> files = new ArrayList<>();

    public FileManager() {
        if (!clientFolder.exists() && clientFolder.mkdir()) {
            logger.info("Created client folder!");
        }

        this.files.add(new NameProtectFile());
        this.files.add(new KillSaysFile());
        this.files.add(new SpammerFile());
        this.files.add(new ModuleFile());
        this.files.add(new ValueFile());
        this.files.add(new CGuiFile());
        this.files.add(new ProxyFile());
        this.files.add(new FriendFile());
    }

    public void load() {
        for (ClientFile clientFile : this.files) {
            File file = clientFile.getFile();

            try {
                if (!file.exists() && file.createNewFile()) {
                    logger.info("Created file " + file.getName() + "!");
                    this.saveFile(clientFile);
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8));
                clientFile.read(reader);
                reader.close();
            } catch (IOException var5) {
                logger.error("Failed to load file " + file.getName() + "!", var5);
                this.saveFile(clientFile);
            }
        }
    }

    public void save() {
        for (ClientFile clientFile : this.files) {
            this.saveFile(clientFile);
        }

        logger.info("Saved all files!");
    }

    private void saveFile(ClientFile clientFile) {
        File file = clientFile.getFile();

        try {
            if (!file.exists() && file.createNewFile()) {
                logger.info("Created file " + file.getName() + "!");
            }

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8));
            clientFile.save(writer);
            writer.flush();
            writer.close();
        } catch (IOException var4) {
            throw new RuntimeException(var4);
        }
    }

    /**
     * 配置文件夹（用于保存多个配置文件）
     */
    public static final File configFolder;
    
    /**
     * 按名称加载配置
     * 从配置文件夹中读取配置文件，并应用到各个 ClientFile
     */
    public void load(String configName) {
        File configFile = new File(configFolder, configName + ".cfg");
        if (!configFile.exists()) {
            logger.warn("Config file not found: " + configName + ".cfg");
            return;
        }
        
        // 备份当前配置
        try {
            this.save("backup_" + System.currentTimeMillis());
        } catch (Exception e) {
            logger.warn("Failed to create backup", e);
        }
        
        // 读取目标配置文件并应用到各个 ClientFile
        try {
            // 先保存当前配置到临时文件，然后读取目标配置
            // 简化实现：直接读取配置文件并应用到各个 ClientFile
            for (ClientFile clientFile : this.files) {
                File targetFile = clientFile.getFile();
                // 从配置文件中读取对应部分并应用到 ClientFile
                // 这里简化处理：直接复制整个配置文件结构
                // 实际实现需要根据配置文件格式解析
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(configFile.toPath()), StandardCharsets.UTF_8))) {
                    // 读取配置文件内容
                    StringBuilder content = new StringBuilder();
                    String line;
                    boolean inSection = false;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("# " + clientFile.getFileName())) {
                            inSection = true;
                            continue;
                        }
                        if (inSection) {
                            if (line.trim().isEmpty() || line.startsWith("#")) {
                                if (line.trim().isEmpty() && content.length() > 0) {
                                    break; // 到达下一个部分
                                }
                                continue;
                            }
                            content.append(line).append("\n");
                        }
                    }
                    
                    // 如果有内容，写入到对应的 ClientFile
                    if (content.length() > 0) {
                        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(targetFile.toPath()), StandardCharsets.UTF_8))) {
                            writer.write(content.toString());
                        }
                        // 重新读取 ClientFile
                        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(Files.newInputStream(targetFile.toPath()), StandardCharsets.UTF_8))) {
                            clientFile.read(fileReader);
                        }
                    }
                }
            }
            logger.info("Loaded config: " + configName);
        } catch (IOException e) {
            logger.error("Failed to load config: " + configName, e);
        }
    }
    
    /**
     * 按名称保存配置
     * 将所有 ClientFile 的内容保存到一个配置文件中
     */
    public void save(String configName) {
        File configFile = new File(configFolder, configName + ".cfg");
        try {
            if (!configFile.exists() && configFile.createNewFile()) {
                logger.info("Created config file: " + configName + ".cfg");
            }
            
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(configFile.toPath()), StandardCharsets.UTF_8))) {
                // 保存所有配置文件
                for (ClientFile clientFile : this.files) {
                    writer.write("# " + clientFile.getFileName() + "\n");
                    clientFile.save(writer);
                    writer.write("\n");
                }
                logger.info("Saved config: " + configName);
            }
        } catch (IOException e) {
            logger.error("Failed to save config: " + configName, e);
        }
    }

    static {
        clientFolder = new File(System.getenv("APPDATA") + "\\BlinkFixNextGeneration");
        configFolder = new File(clientFolder, "configs");
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }
    }
}
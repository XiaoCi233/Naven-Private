package com.surface.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.surface.Wrapper;
import com.surface.mod.Mod;
import com.surface.util.struct.HSBData;
import com.surface.value.Value;
import com.surface.value.impl.*;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    private final Gson gson = new Gson();
    private final File clientDirectory = new File(Minecraft.getMinecraft().mcDataDir, "surface/");
    private final Logger logger = LogManager.getLogger("Config Manager");

    public File getClientDirectory() {
        return clientDirectory;
    }

    public ConfigManager() {
        if (!clientDirectory.exists())
            clientDirectory.mkdir();
    }

    public boolean exists(String name) {
        File config = new File(clientDirectory, name + ".json");
        return config.exists();
    }

    public void read(String name) {
        File config = new File(clientDirectory, name + ".json");
        if (config.exists()) {
            for (Mod m : Wrapper.Instance.getModManager().getMods()) {
                if (m.getState())
                    m.setState(false);
            }
            try {
                loadJson(new JsonParser().parse(new InputStreamReader(Files.newInputStream(config.toPath()))));
            } catch (IOException e) {
                logger.catching(e);
            }
            if (Minecraft.getMinecraft().thePlayer != null)
                Wrapper.sendMessage("Config " + name + " was loaded.");
        } else {
            if (Minecraft.getMinecraft().thePlayer != null)
                Wrapper.sendMessage("Config " + name + " not existed.");
        }
    }

    public void save(String name) {
        File config = new File(clientDirectory, name + ".json");
        try {
            FileOutputStream fos = new FileOutputStream(config);
            fos.write(gson.toJson(toJson()).getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch (IOException e) {
            logger.catching(e);
        }
    }

    public void delete(String name) {
        File config = new File(clientDirectory, name + ".json");
        config.delete();
    }

    public JsonObject toJson() {
        final JsonObject moduleObject = new JsonObject();

        for (Mod m : Wrapper.Instance.getModManager().getMods()) {
            final JsonObject settingObject = new JsonObject();

            settingObject.addProperty("Enabled", m.isEnable());
            settingObject.addProperty("Key", m.getKeyCode());
            settingObject.addProperty("Hidden", m.isHide());

            final List<Value<?>> values = m.getValues();

            if (!values.isEmpty()) {
                for (Value<?> value : values) {
                    if (value instanceof FilterValue<?>) {
                        final FilterValue<?> opt = (FilterValue<?>) value;
                        final JsonObject property = new JsonObject();

                        for (BooleanValue bool : opt.getValue())
                            property.addProperty(bool.getValueName(), bool.getValue());

                        settingObject.add(opt.getValueName(), property);
                    } else if (value instanceof ColorValue) {
                        final ColorValue val = (ColorValue) value;
                        final JsonObject property = new JsonObject();

                        property.addProperty("Hue", val.getValue().getHue());
                        property.addProperty("Saturation", val.getValue().getSaturation());
                        property.addProperty("Brightness", val.getValue().getBrightness());
                        property.addProperty("Alpha", val.getValue().getAlpha());
                        property.addProperty("Rainbow", val.isEnabledRainbow());

                        settingObject.add(val.getValueName(), property);
                    } else settingObject.addProperty(value.getValueName(), value.getValue().toString());
                }
            }

            moduleObject.add(m.getName(), settingObject);
        }

        return moduleObject;
    }

    public void loadJson(JsonElement element) {
        if (!element.isJsonObject())
            return;

        final JsonObject moduleObject = element.getAsJsonObject();

        for (Map.Entry<String, JsonElement> moduleElement : moduleObject.entrySet()) {
            final Mod module = Wrapper.Instance.getModManager().getModFromName(moduleElement.getKey());

            if (module == null)
                continue;

            final JsonElement settingElement = moduleElement.getValue();

            if (!settingElement.isJsonObject())
                continue;

            final JsonObject settingObject = settingElement.getAsJsonObject();

            for (Map.Entry<String, JsonElement> settingSet : settingObject.entrySet()) {
                switch (settingSet.getKey()) {
                    case "Enabled": {
                        if (!module.getClass().isAnnotationPresent(DontLoadState.class))
                            module.setState(settingSet.getValue().getAsBoolean());
                        break;
                    }
                    case "Key": {
                        module.setKeyCode(settingSet.getValue().getAsInt());
                        break;
                    }
                    case "Hidden": {
                        module.setHide(settingSet.getValue().getAsBoolean());
                        break;
                    }
                    default: {
                        final Value<?> option = module.getValue(settingSet.getKey());

                        if (option == null)
                            continue;

                        try {
                            if (option instanceof BooleanValue) {
                                final BooleanValue opt = (BooleanValue) option;
                                opt.setValue(settingSet.getValue().getAsBoolean());
                            } else if (option instanceof NumberValue) {
                                final NumberValue opt = (NumberValue) option;
                                opt.setValue(settingSet.getValue().getAsDouble());
                            } else if (option instanceof ModeValue) {
                                final ModeValue opt = (ModeValue) option;
                                opt.setValue(settingSet.getValue().getAsString());
                            } else if (option instanceof TextValue) {
                                final TextValue opt = (TextValue) option;
                                opt.setValue(settingSet.getValue().getAsString());
                            } else if (option instanceof ColorValue) {
                                final ColorValue opt = (ColorValue) option;
                                final JsonObject settings = settingSet.getValue().getAsJsonObject();
                                opt.setRainbowEnabled(settings.get("Rainbow").getAsBoolean());
                                opt.setValue(new HSBData(settings.get("Hue").getAsFloat(), settings.get("Saturation").getAsFloat(), settings.get("Brightness").getAsFloat(), settings.get("Alpha").getAsFloat()));
                            } else if (option instanceof FilterValue<?>) {
                                final FilterValue<?> opt = (FilterValue<?>) option;
                                final JsonObject items = settingSet.getValue().getAsJsonObject();
                                for (BooleanValue bool : opt.getValue())
                                    bool.setValue(items.get(bool.getValueName()).getAsBoolean());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }
    }


}

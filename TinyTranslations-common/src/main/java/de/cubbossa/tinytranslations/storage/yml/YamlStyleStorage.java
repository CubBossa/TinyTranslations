package de.cubbossa.tinytranslations.storage.yml;

import de.cubbossa.tinytranslations.MessageStyle;
import de.cubbossa.tinytranslations.storage.StyleStorage;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class YamlStyleStorage implements StyleStorage {

    private final File file;
    private final Yaml yaml;

    public YamlStyleStorage(File file) {
        this(file, null);
    }

    public YamlStyleStorage(File file, @Nullable DumperOptions options) {
        if (!file.getName().endsWith(".yml")) {
            throw new IllegalArgumentException("YamlStyleStorage file must be of type yml. Instead: " + file.getName());
        }
        this.file = file;
        if (options == null) {
            options = new DumperOptions();
            options.setIndent(2);
            options.setPrettyFlow(true);
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        }
        this.yaml = new Yaml(options);
    }

    @Override
    public void writeStyles(Map<String, MessageStyle> styles) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Map<String, MessageStyle> present = loadStyles();
        Map<String, String> toWrite = new LinkedHashMap<>();
        styles.forEach((s, messageStyle) -> {
            if (!present.containsKey(s)) {
                toWrite.put(s, messageStyle.toString());
            }
        });
        try (FileWriter writer = new FileWriter(file)) {
            yaml.dump(YamlUtils.fromDotNotation(toWrite), writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, MessageStyle> loadStyles() {
        if (!file.exists()) {
            try {
                file.createNewFile();
                return new HashMap<>();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Map<String, Object> map = new LinkedHashMap<>();
        try (FileInputStream fis = new FileInputStream(file)) {
            Map<String, Object> content = yaml.load(fis);
            if (content != null) {
                map.putAll(YamlUtils.toDotNotation(content));
            }
        } catch (IOException t) {
            throw new RuntimeException(t);
        }
        Map<String, MessageStyle> result = new HashMap<>();
        map.forEach((s, o) -> {
            String des = "";
            if (o instanceof List<?> list) {
                des = list.stream().map(Object::toString).collect(Collectors.joining("\n"));
            } else {
                des = o.toString();
            }
            result.put(s, MessageStyle.messageStyle(s, des));
        });
        return result;
    }
}

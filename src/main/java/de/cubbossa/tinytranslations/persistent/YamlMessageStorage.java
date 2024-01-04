package de.cubbossa.tinytranslations.persistent;

import de.cubbossa.tinytranslations.Message;
import de.cubbossa.tinytranslations.MessageCore;
import de.cubbossa.tinytranslations.util.YamlUtils;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class YamlMessageStorage extends FileMessageStorage implements MessageStorage {

    private final Yaml yaml;

    public YamlMessageStorage(File directory) {
        this(directory, "", "", null);
    }

    public YamlMessageStorage(File directory, String filePrefix, String fileSuffix) {
        this(directory, filePrefix, fileSuffix, null);
    }

    public YamlMessageStorage(File directory, @Nullable DumperOptions dumperOptions) {
        this(directory, "", "", dumperOptions);
    }

    public YamlMessageStorage(File directory, String filePrefix, String fileSuffix, @Nullable DumperOptions dumperOptions) {
        super(directory, filePrefix, fileSuffix + ".yml");
        if (dumperOptions == null) {
            dumperOptions = new DumperOptions();
            dumperOptions.setIndent(2);
            dumperOptions.setPrettyFlow(true);
            dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        }
        this.yaml = new Yaml(dumperOptions);
    }

    @Override
    public Map<Message, String> readMessages(Locale locale) {
        Map<Message, String> result = new HashMap<>();
        File file = localeFileIfExists(locale);
        if (file == null) {
            return new HashMap<>();
        }
        Map<String, Object> map;
        try (FileInputStream fis = new FileInputStream(file)) {
            map = YamlUtils.toDotNotation(yaml.load(fis));
        } catch (IOException t) {
            throw new RuntimeException(t);
        }
        map.forEach((s, o) -> {
            if (o instanceof String val) {
                result.put(new MessageCore(s), val);
            } else if (o instanceof List<?> list) {
                result.put(new MessageCore(s), list.stream().map(Object::toString).collect(Collectors.joining("\n")));
            } else {
                result.put(new MessageCore(s), o.toString());
            }
        });
        return result;
    }

    @Override
    public Collection<Message> writeMessages(Collection<Message> messages, Locale locale) {
        Map<String, Object> result = new HashMap<>();
        Collection<Message> success = new HashSet<>();

        File file = localeFileIfExists(locale);
        if (file != null) {
            try (FileInputStream fis = new FileInputStream(file)) {
                result = YamlUtils.toDotNotation(yaml.load(fis));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        for (Message message : messages) {
            if (result.containsKey(message.getKey())) {
                continue;
            }
            result.put(message.getKey(), message.getDictionary().get(locale));
            success.add(message);
        }
        file = localeFile(locale);
        try (FileWriter writer = new FileWriter(file)) {
            yaml.dump(YamlUtils.fromDotNotation(result), writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return success;
    }
}

package de.cubbossa.tinytranslations.persistent;

import de.cubbossa.tinytranslations.Message;
import de.cubbossa.tinytranslations.impl.MessageCore;
import de.cubbossa.tinytranslations.util.YamlUtils;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
        try (FileReader fis = new FileReader(file, StandardCharsets.UTF_8)) {
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
            try (FileReader fis = new FileReader(file, StandardCharsets.UTF_8)) {
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
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            yaml.dump(YamlUtils.fromDotNotation(result), writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return success;
    }
}

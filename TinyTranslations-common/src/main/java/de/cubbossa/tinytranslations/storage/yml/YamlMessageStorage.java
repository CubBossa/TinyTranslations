package de.cubbossa.tinytranslations.storage.yml;

import de.cubbossa.tinytranslations.Message;
import de.cubbossa.tinytranslations.TranslationKey;
import de.cubbossa.tinytranslations.storage.FileMessageStorage;
import de.cubbossa.tinytranslations.storage.MessageStorage;
import de.cubbossa.tinytranslations.storage.StorageEntry;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
    public Map<TranslationKey, StorageEntry> readMessages(Locale locale) {
        Map<TranslationKey, StorageEntry> result = new HashMap<>();
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
            String v = null;
            if (o instanceof String val) {
                v = val;
            } else if (o instanceof List<?> list) {
                v = list.stream().map(Object::toString).collect(Collectors.joining("\n"));
            } else if (o != null) {
                v = o.toString();
            }
            result.put(TranslationKey.of(s), new StorageEntry(s, v, null));
        });
        return result;
    }

    @Override
    public Collection<Message> writeMessages(Collection<Message> messages, Locale locale, boolean override) {
        Map<String, Object> result = new HashMap<>();
        Collection<Message> success = new HashSet<>();

        File file = override ? null : localeFileIfExists(locale);
        if (file != null) {
            try (FileReader fis = new FileReader(file, StandardCharsets.UTF_8)) {
                result = YamlUtils.toDotNotation(yaml.load(fis));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        for (Message message : messages) {
            if (result.containsKey(message.getKey().key())) {
                continue;
            }
            result.put(message.getKey().key(), message.dictionary().get(locale));
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

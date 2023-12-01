package de.cubbossa.translations.persistent;

import de.cubbossa.translations.Message;
import de.cubbossa.translations.MessageCore;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class YamlMessageStorage extends FileStorage implements MessageStorage {

    private final Yaml yaml;

    public YamlMessageStorage(File directory) {
        super(directory, ".yml");
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.yaml = new Yaml(options);
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
            map = toDotNotation(yaml.load(fis));
        } catch (IOException t) {
            throw new RuntimeException(t);
        }
        map.forEach((s, o) -> {
            if (o instanceof String val) {
                result.put(new MessageCore(s), val);
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
                result = toDotNotation(yaml.load(fis));
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
            yaml.dump(fromDotNotation(result), writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return success;
    }

    Map<String, Object> toDotNotation(Map<String, Object> map) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (e.getValue() instanceof Map inner) {
                inner = toDotNotation(inner);
                inner.forEach((o, o2) -> {
                    result.put(e.getKey() + "." + o, o2);
                });
            } else {
                result.put(e.getKey(), e.getValue());
            }
        }
        return result;
    }

    Map<String, Object> fromDotNotation(Map<String, Object> map) {
        Map<String, Object> result = new HashMap<>();
        map.forEach((s, o) -> {
            String[] splits = s.split("\\.");
            LinkedList<String> keys = new LinkedList<>(List.of(Arrays.copyOfRange(splits, 0, splits.length - 1)));
            Map<String, Object> m = result;
            for (String key : keys) {
                Object obj = m.computeIfAbsent(key, s1 -> new HashMap<>());
                if (obj instanceof Map x) {
                    m = x;
                } else {
                    throw new IllegalStateException("Map contains a value at a tree node that is no leaf.");
                }
            }
            m.put(splits[splits.length - 1], o);
        });
        return result;
    }
}

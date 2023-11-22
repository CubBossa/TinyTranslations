package de.cubbossa.translations.persistent;

import de.cubbossa.translations.Message;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YamlStorage extends FileStorage implements MessageStorage {

    private final Yaml yaml;

    public YamlStorage(Logger logger, File directory) {
        super(logger, directory, ".yml");
        this.yaml = new Yaml();
    }

    @Override
    public Optional<String> readMessage(Message message, Locale locale) {
        return Optional.ofNullable(readMessages(Set.of(message), locale).get(message));
    }

    @Override
    public Map<Message, String> readMessages(Collection<Message> messages, Locale locale) {
        try {
            Map<Message, String> result = new HashMap<>();
            Map<String, Object> map = yaml.load(new FileInputStream(localeFile(locale)));
            map.forEach((s, o) -> {
                Optional<Message> message = messages.stream().filter(m -> m.getKey().equals(s)).findFirst();
                if (message.isEmpty()) {
                    return;
                }
                if (o instanceof String translation) {
                    result.put(message.get(), translation);
                }
            });
            return result;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean writeMessage(Message message, Locale locale, String translation) {
        return writeMessages(Map.of(message, translation), locale).contains(message);
    }

    @Override
    public Collection<Message> writeMessages(Map<Message, String> messages, Locale locale) {
        Map<String, Object> result = new HashMap<>();
        Collection<Message> success = new HashSet<>();

        messages.forEach((message, s) -> {
            String[] keys = message.getKey().split("\\.");
            Map<String, Object> current = result;

            for (int i = 0; i < keys.length - 1; i++) {
                Object present = current.get(keys[i]);
                if (present == null) {
                    Map<String, Object> m =new HashMap<>();
                    current.put(keys[i], m);
                    current = m;
                } else if(present instanceof Message msg) {
                    String x = "";
                    for (int j = 0; j < i; j++) {
                        x += keys[j];
                    }
                    logger.log(Level.WARNING, "Clashing message keys: '" + x + "'.");
                    return;
                } else if(present instanceof Map<?, ?> map) {
                    current = (Map<String, Object>) map;
                }
            }
            current.put(keys[keys.length - 1], s);
            success.add(message);
        });
        return success;
    }
}

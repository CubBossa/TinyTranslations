package de.cubbossa.translations.persistent;

import de.cubbossa.translations.Message;
import de.cubbossa.translations.MessageCore;
import de.cubbossa.translations.Translations;
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
        super(directory, ".yml");
        this.yaml = new Yaml();
    }

    @Override
    public Map<Message, String> readMessages(Locale locale) {
        try {
            Map<Message, String> result = new HashMap<>();
            Map<String, Object> map = yaml.load(new FileInputStream(localeFile(locale)));
            map.forEach((s, o) -> {
                if (o instanceof String val) {
                    result.put(new MessageCore(s), val);
                }
            });
            return result;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<Message> writeMessages(Collection<Message> messages, Locale locale) {
        Map<String, Object> result = new HashMap<>();
        Collection<Message> success = new HashSet<>();

        messages.forEach(message -> {
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
                    Logger.getLogger("Translations").log(Level.WARNING, "Clashing message keys: '" + x + "'.");
                    return;
                } else if(present instanceof Map<?, ?> map) {
                    current = (Map<String, Object>) map;
                }
            }
            current.put(keys[keys.length - 1], message.getDictionary().get(locale));
            success.add(message);
        });
        return success;
    }
}

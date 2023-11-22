package de.cubbossa.translations;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleMessageSet implements MessageSet {

    private final Map<String, Message> set = new HashMap<>();

    @Override
    public void addMessage(Message message) {
        set.put(message.getKey(), message);
    }

    @Override
    public void addMessages(Message... messages) {
        Arrays.stream(messages).forEach(this::addMessage);
    }

    @Override
    public void addMessagesClass(Class<?> messageClass) {
        Field[] messages = Arrays.stream(messageClass.getDeclaredFields())
                .filter(field -> field.getType().equals(Message.class))
                .toArray(Field[]::new);

        for (Field messageField : messages) {
            try {
                addMessage((Message) messageField.get(messageClass));
            } catch (Throwable t) {
                Logger.getLogger("Translations").log(Level.WARNING, "Could not extract message '" + messageField.getName() + "' from class " + messageClass.getSimpleName(), t);
            }
        }
    }

    @Override
    public boolean removeMessage(String key) {
        return set.remove(key) != null;
    }

    @Override
    public boolean removeMessage(Message message) {
        return set.remove(message.getKey()) != null;
    }

    @Override
    public void clear() {
        set.clear();
    }

    @Override
    public @Nullable Message getMessage(String key) {
        return set.get(key);
    }
}

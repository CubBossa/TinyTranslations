package de.cubbossa.translations;

import org.jetbrains.annotations.Nullable;

public interface MessageSet {

    void addMessage(Message message);

    void addMessages(Message...messages);

    void addMessagesClass(Class<?> fromClass);

    boolean removeMessage(String key);

    boolean removeMessage(Message message);

    void clear();

    @Nullable Message getMessage(String key);
}

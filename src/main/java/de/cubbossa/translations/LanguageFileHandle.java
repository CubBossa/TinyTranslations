package de.cubbossa.translations;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public interface LanguageFileHandle {

    Optional<String> readMessage(Message message, Locale locale, Collection<Message> scope);

    Map<Message, String> readMessages(Collection<Message> message, Locale locale, Collection<Message> scope);

    boolean writeMessage(Message message, Locale locale, String translation);

    Collection<Message> writeMessages(Map<Message, String> messages, Locale locale);
}

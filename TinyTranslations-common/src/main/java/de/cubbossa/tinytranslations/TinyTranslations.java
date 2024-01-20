package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.nanomessage.NanoMessage;
import de.cubbossa.tinytranslations.nanomessage.ObjectTagResolverMap;
import net.kyori.adventure.text.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TinyTranslations {

  public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

  public static final NanoMessage NM = NanoMessage.nanoMessage();
  static {
    applyDefaultObjectResolvers(NM.getObjectTypeResolverMap());
  }

  public static MessageTranslator application(String name) {
    return new MessageTranslatorImpl(null, name);
  }

  public static Message[] messageFieldsFromClass(Class<?> messageClass) {
    try {
      TinyTranslations.class.getClassLoader().loadClass(messageClass.getName());
    } catch (Throwable t) {
      t.printStackTrace();
    }
    Field[] fields = Arrays.stream(messageClass.getDeclaredFields())
            .filter(field -> Message.class.isAssignableFrom(field.getType()))
            .toArray(Field[]::new);

    Message[] messages = new Message[fields.length];
    int i = 0;
    for (Field messageField : fields) {
      try {
        messages[i++] = (Message) messageField.get(messageClass);
      } catch (Throwable t) {
        Logger.getLogger("Translations").log(Level.WARNING, "Could not extract message '" + messageField.getName() + "' from class " + messageClass.getSimpleName(), t);
      }
    }
    return messages;
  }

  private static void applyDefaultObjectResolvers(ObjectTagResolverMap map) {
    map.put(List.class, Collections.emptyMap(), list -> Component.text(
            (String) list.stream().map(Object::toString).collect(Collectors.joining(", "))
    ));
  }

  protected TinyTranslations() {}
}

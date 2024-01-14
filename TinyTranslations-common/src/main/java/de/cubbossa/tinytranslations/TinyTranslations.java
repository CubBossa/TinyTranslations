package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.impl.AppTranslator;
import de.cubbossa.tinytranslations.impl.GlobalTranslator;
import de.cubbossa.tinytranslations.nanomessage.NanoMessage;
import de.cubbossa.tinytranslations.nanomessage.ObjectTagResolverMap;
import net.kyori.adventure.text.Component;

import java.io.File;
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
  private static volatile Translator global;

  private static final Object mutex = new Object();
  public static Translator global() {
    Translator g = global;
    if (g == null) {
      throw new IllegalStateException("Accessing global without enabling TranslationsFramework.");
    }
    return g;
  }

  public static Translator standalone(String name) {
    return new AppTranslator(null, name);
  }

  public static Translator application(String name) {
    return global().fork(name);
  }

  public static boolean isEnabled() {
    Translator g = global;
    if (g == null) {
      synchronized (mutex) {
        return g != null;
      }
    }
    return false;
  }

  public static void enable(File pluginDirectory) {
    Translator g = global;
    if (g == null) {
      synchronized (mutex) {
        g = global;
        if (g == null) {
          global = new GlobalTranslator(pluginDirectory);
        }
      }
    }
  }

  public static void disable() {
    synchronized (mutex) {
      global = null;
    }
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

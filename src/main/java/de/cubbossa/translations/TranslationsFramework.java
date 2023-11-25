package de.cubbossa.translations;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TranslationsFramework {

  private TranslationsFramework() {}

  public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

  private static volatile Translations global;
  private static final Object mutex = new Object();

  public static Translations global() {
    Translations g = global;
    if (g == null) {
      throw new IllegalStateException("Accessing global without enabling TranslationsFramework.");
    }
    return g;
  }

  public static Translations application(String name) {
    return global().fork(name);
  }

  public static void enable(File pluginDirectory) {
    Translations g = global;
    if (g == null) {
      synchronized (mutex) {
        g = global;
        if (g == null) {
          global = new GlobalTranslations(pluginDirectory);
        }
      }
    }
  }

  public static void disable() {
  }

  public static Message[] messageFieldsFromClass(Class<?> messageClass) {
    try {
      TranslationsFramework.class.getClassLoader().loadClass(messageClass.getName());
    } catch (Throwable t) {
      t.printStackTrace();
    }
    Field[] fields = Arrays.stream(messageClass.getDeclaredFields())
            .filter(field -> field.getType().isInstance(Message.class))
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
}

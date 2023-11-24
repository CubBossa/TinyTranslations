package de.cubbossa.translations;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TranslationsFramework {

  private TranslationsFramework() {}

  private static Translations global;

  public static Message[] messageFieldsFromClass(Class<?> messageClass) {
    try {
      TranslationsFramework.class.getClassLoader().loadClass(messageClass.getName());
    } catch (Throwable t) {
      t.printStackTrace();
    }
    Field[] fields = Arrays.stream(messageClass.getDeclaredFields())
        .filter(field -> field.getType().equals(Message.class))
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

  public static Translations global() {
    return global;
  }

  public static Translations application(String name) {
    return global().fork(name);
  }

  public static void enable(File rootDir) {
    global = new GlobalTranslations(rootDir);
  }

  public static void disable() {
    global().shutdown();
    global = null;
  }
}

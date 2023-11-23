package de.cubbossa.translations;

import java.io.File;

public final class TranslationsFramework {

  private TranslationsFramework() {}

  private static Translations global;

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

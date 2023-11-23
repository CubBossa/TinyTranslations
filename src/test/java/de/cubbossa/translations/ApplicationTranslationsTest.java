package de.cubbossa.translations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationTranslationsTest {

  public static final Translations translations = Translations.application("test");

  public static final Message SIMPLE = translations.messageBuilder("simple")
      .withDefault("<red>Hello world")
      .withTranslation(Locale.GERMANY, "Hallo welt - Deutschland")
      .withTranslation(Locale.GERMAN, "Hallo welt - Deutsch")
      .withComment("abc")
      .build();

  public static final Message EMBED = translations.messageBuilder("embedded")
      .withDefault("Embedded: <msg:simple>a")
      .build();
  public static final Message EMBED_NO_BLEED = translations.messageBuilder("embedded_complex")
      .withDefault("Embedded: <msg:simple:true>a")
      .build();

  public static final Message NEW_LINE = translations.messageBuilder("new_line")
      .withDefault("Hello\nworld")
      .build();

  @Test
  void newLine() {

    PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();

    Component abc = Component.empty()
        .append(Component.text("a")
            .append(Component.text("b"))
            .append(Component.newline()))
        .append(Component.text("c"));

    assertEquals(
        List.of("ab", "c"),
        ComponentSplit.split(abc, "\n").stream().map(serializer::serialize).collect(Collectors.toList())
    );


    assertEquals(
        List.of("Hallo"),
        ComponentSplit.split(Component.text("Hallo"), "\n").stream()
            .map(serializer::serialize)
            .toList()
    );

    assertEquals(
        List.of("Hallo", "welt", "123"),
        ComponentSplit.split(Component.text("Hallo\nwelt\n123"), "\n").stream()
            .map(serializer::serialize)
            .toList()
    );

    Component cs = MiniMessage.miniMessage().deserialize("<gray>» <yellow>right-click air:</yellow> Open GUI</gray>"
        .replaceAll("\\\\", "\\"));

    assertEquals(
        List.of(
            "» right-click air: Open GUI"
        ),
        ComponentSplit.split(cs, "\n").stream().map(serializer::serialize).collect(Collectors.toList())
    );

    Component c = MiniMessage.miniMessage().deserialize("<gray>Assign and remove multiple\n<gray>groups at once.\n\n<gray>» <yellow>right-click air:</yellow> Open GUI</gray>\n<gray>» <yellow>right-click node:</yellow> Add groups</gray>\n<gray>» <yellow>right-click node:</yellow> Remove groups</gray>"
        .replaceAll("\\\\", "\\"));

    assertEquals(
        List.of(
            "Assign and remove multiple",
            "groups at once.",
            "",
            "» right-click air: Open GUI",
            "» right-click node: Add groups",
            "» right-click node: Remove groups"
        ),
        ComponentSplit.split(c, "\n").stream().map(serializer::serialize).collect(Collectors.toList())
    );

    assertEquals(
        2,
        ComponentSplit.split(translations.process(NEW_LINE), "\n").size()
    );
  }

  @Test
  void translate() {
    assertEquals(text("Hello world", NamedTextColor.RED), translations.process(SIMPLE));
    assertEquals(text("Hallo welt - Deutschland"), translations.process(SIMPLE, Locale.GERMANY));
    assertEquals(text("Hallo welt - Deutsch"), translations.process(SIMPLE, Locale.GERMAN));
    assertEquals(text("Hallo welt - Deutsch"), translations.process(SIMPLE, Locale.forLanguageTag("de-AT")));

    assertEquals(
        text("Embedded: ")
            .append(text("Hello worlda", NamedTextColor.RED)),
        translations.process(EMBED, Locale.US)
    );
    assertEquals(
        text("Embedded: ")
            .append(text("Hello world", NamedTextColor.RED))
            .append(text("a")),
        translations.process(EMBED_NO_BLEED, Locale.US)
    );
  }

  @Test
  void getStylesAsResolver() {
  }

  @Test
  void addStyle() {
  }
}
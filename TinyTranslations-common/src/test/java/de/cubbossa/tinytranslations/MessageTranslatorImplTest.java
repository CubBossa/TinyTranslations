package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.storage.MessageStorage;
import de.cubbossa.tinytranslations.storage.properties.PropertiesMessageStorage;
import de.cubbossa.tinytranslations.tinyobject.TinyObjectResolver;
import de.cubbossa.tinytranslations.util.ComponentSplit;
import de.cubbossa.tinytranslations.util.ListSection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.*;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.TextColor.color;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageTranslatorImplTest extends TestBase {

    public static final Message SIMPLE = new MessageBuilder("simple")
            .withDefault("<red>Hello world")
            .withTranslation(Locale.GERMANY, "Hallo welt - Deutschland")
            .withTranslation(Locale.GERMAN, "Hallo welt - Deutsch")
            .withComment("abc")
            .build();
    public static final Message EMBED = new MessageBuilder("embedded")
            .withDefault("Embedded: {msg:simple}a")
            .build();
    public static final Message NEW_LINE = new MessageBuilder("new_line")
            .withDefault("Hello\nworld")
            .build();
    public static final Message TEST_1 = new MessageBuilder("examples.test.first")
            .withComment("Lets test this")
            .withPlaceholder("red", "The color red")
            .withPlaceholders("green", "blue")
            .withDefault("<red>Hello \nworld!")
            .withTranslation(Locale.GERMAN, "<red>Hallo Welt!")
            .build();
    public static final Message TEST_2 = new MessageBuilder("examples.test.second")
            .withComment("Another test with\nline break\n\ncomments")
            .withPlaceholder("abc")
            .withDefault("<green:0>Luke - I am your father!")
            .build();
    public static final Message TEST_C = new MessageBuilder("sorted.c")
            .withDefault("abC")
            .withTranslation(Locale.ENGLISH, "<re:abc>ast</re>")
            .build();
    public static final Message TEST_B = new MessageBuilder("sorted.b")
            .withDefault("aBc").build();
    public static final Message TEST_A = new MessageBuilder("sorted.a")
            .withDefault("Abc").build();

    @Test
    void newLine() {
        translator.addMessages(NEW_LINE);
        assertEquals(
                2,
                ComponentSplit.split(translator.translate(NEW_LINE), "\n").size()
        );
    }

    @Test
    void translateHover() {

        translator.messageBuilder("a").withDefault("A").build();
        translator.messageBuilder("b").withDefault("B").build();

        assertEquals(
                text("A").hoverEvent(text("B")),
                translator.translate("<hover:show_text:'{msg:b}'>{msg:a}</hover>")
        );
    }

    @Test
    void translateHover2() {

        translator.messageBuilder("a").withDefault("A").build();
        translator.messageBuilder("b").withDefault("{tr:'spectatorMenu.next_page'}").build();

        assertEquals(
                text("A").hoverEvent(translatable("spectatorMenu.next_page")),
                translator.translate("<hover:show_text:'{msg:b}'>{msg:a}</hover>")
        );
        translator.messageBuilder("next_page").withDefault("<tr:spectatorMenu.next_page>").build();
        assertEquals(
                translatable("spectatorMenu.next_page").hoverEvent(translatable("spectatorMenu.next_page")),
                translator.translate("<hover:show_text:\"{msg:next_page}\">{msg:next_page}")
        );
    }


    @Test
    void translate() {
        translator.addMessages(TinyTranslations.messageFieldsFromClass(this.getClass()));

        translator.messageBuilder("c").withDefault("c").build();
        assertEquals(
                text("abc"),
                render(translator.messageBuilder("x").withDefault("ab{msg:testapp:c}").build()).compact()
        );
        assertEquals(
                text("abcd"),
                render(translator.messageBuilder("x").withDefault("ab{msg:testapp:c}d").build()).compact()
        );

        assertEquals(text("Hello world", NamedTextColor.RED), translator.translate(SIMPLE));
        assertEquals(text("Hallo welt - Deutschland"), translator.translate(SIMPLE, Locale.GERMANY));
        assertEquals(text("Hallo welt - Deutsch"), translator.translate(SIMPLE, Locale.GERMAN));
        assertEquals(text("Hallo welt - Deutsch"), translator.translate(SIMPLE, Locale.forLanguageTag("de-AT")));

        assertEquals(
                text("Embedded: ").append(text("Hello world", NamedTextColor.RED).append(text("a"))).compact(),
                render(EMBED, Locale.ENGLISH)
        );
    }

    @Test
    void resolver() {
        Message m = translator.messageBuilder("a").withDefault("a <b>").build();
        assertEquals(text("a 1"), render(m.insertNumber("b", 1)));
        assertEquals(text("a 1"), render(m.formatted(Formatter.number("b", 1))));
    }

    @Test
    void getMessage() {
        Assertions.assertNull(translator.getMessage("a"));
        Message m1 = translator.messageBuilder("a").withDefault("A").build();
        assertEquals(m1, translator.getMessage("a"));
    }

    @Test
    public void testFileCreation() {

        File inner = new File(dir, "/TestApp/");
        inner.mkdirs();

        translator.setMessageStorage(new PropertiesMessageStorage(inner));
        translator.messageBuilder("key").withDefault("abcde").build();

        translator.saveLocale(Locale.ENGLISH);

        File en = new File(inner, "en.properties");
        Assertions.assertTrue(inner.exists());
        Assertions.assertTrue(en.exists());

        replaceInFile(en, "a", "e");
        String before = fileContent(en);
        translator.saveLocale(Locale.ENGLISH);
        assertEquals(before, fileContent(en));
    }

    @Test
    public void testLoad() {

        translator.addMessages(TEST_1, TEST_2);

        assertEquals(text("Hello \nworld!", NamedTextColor.RED), translator.translate(TEST_1));
        assertEquals(text("Hallo Welt!", NamedTextColor.RED), render(TEST_1, Locale.GERMAN));

        translator.saveLocale(Locale.ENGLISH);
        translator.loadLocale(Locale.ENGLISH);
        assertEquals(text("Hello \nworld!", NamedTextColor.RED), translator.translate(TEST_1));

        assertEquals(text("Luke - I am your father!", NamedTextColor.GREEN), translator.translate(TEST_2));
    }

    @Test
    public void testLoad2() {

        Message abc = translator.messageBuilder("a").withDefault("Yo!").build();

        translator.setMessageStorage(new MessageStorage() {
            @Override
            public Collection<Locale> fetchLocales() {
                return List.of(Locale.ENGLISH);
            }

            @Override
            public Map<TranslationKey, String> readMessages(Locale locale) {
                return Map.of(TranslationKey.of(translator.getPath(), "a"), "Worked!");
            }

            @Override
            public Collection<Message> writeMessages(Collection<Message> messages, Locale locale) {
                return Collections.emptyList();
            }
        });
        assertEquals(translator.translate(abc), Component.text("Yo!"));
        translator.loadLocales();
        assertEquals(translator.translate(abc), Component.text("Worked!"));
    }

    @Test
    public void testLoad3() {

        Message abc = new MessageBuilder("a").withDefault("Yo!").build();

        translator.addMessages(abc);
        translator.setMessageStorage(new MessageStorage() {
            @Override
            public Collection<Locale> fetchLocales() {
                return List.of(Locale.ENGLISH);
            }

            @Override
            public Map<TranslationKey, String> readMessages(Locale locale) {
                return Map.of(TranslationKey.of(translator.getPath(), "a"), "Worked!");
            }

            @Override
            public Collection<Message> writeMessages(Collection<Message> messages, Locale locale) {
                return Collections.emptyList();
            }
        });
        assertEquals(Component.text("Yo!"), translator.translate(abc));
        translator.loadLocales();
        assertEquals(Component.text("Worked!"), translator.translate(abc));
    }

    @Test
    public void placeholderInTag() {

        assertEquals(
                text("<hi>"),
                translator.translate("<{ph}>", Placeholder.parsed("ph", "hi"))
        );
    }

    @Test
    public void testPrefix(@TempDir File d) {

        MessageTranslator global = TinyTranslations.globalTranslator(d);
        global.getStyleSet().put("prefix", "<primary>{msg:prefix}</primary> {slot}");
        MessageTranslator plugin = global.fork("plugin");
        plugin.getStyleSet().put("primary", "<#ff00ff>{slot}</#ff00ff>");
        plugin.messageBuilder("prefix").withDefault("Plugin").build();
        Message msg = plugin.messageBuilder("test").withDefault("<prefix>Message</prefix>").build();

        assertEquals(
                empty().append(text("Plugin", color(0xff00ff))).append(text(" Message")),
                plugin.translate(msg).compact()
        );
    }

    @Test
    public void testList() {

        List<Boolean> list = List.of(true, false, true);
        Message a = translator.messageBuilder("a.b.c").withDefault("Header\n<list:','>{el ? <green>true</green> : <red>false</red>}</list>\nFooter").build();

        a = a.insertList("list", list, ListSection.paged(0, 2));
        assertEquals(
                text("Header\n")
                        .append(text("true", NamedTextColor.GREEN))
                        .append(text(","))
                        .append(text("false", NamedTextColor.RED))
                        .append(text("\nFooter")),
                render(a).compact()
        );

        Message b = translator.messageBuilder("a.b.c").withDefault("Header\n<list:'\n'>{index}.) {el ? <green>true</green> : <red>false</red>}</list>\nFooter").build();

        b = b.insertList("list", list, ListSection.paged(0, 2));
        assertEquals(
                text("Header\n")
                        .append(text("1.) ").append(text("true", NamedTextColor.GREEN)))
                        .append(text("\n2.) ").append(text("false", NamedTextColor.RED)))
                        .append(text("\nFooter")).compact(),
                render(b).compact()
        );
    }

    @Test
    public void testMultiplePlaceholders() {
        Message a = translator.messageBuilder("a").withDefault("<a/><b/><c/>").build();
        assertEquals(
                Component.text("123"),
                render(a.insertNumber("a", 1).insertNumber("b", 2).insertNumber("c", 3))
        );
    }

    @Test
    public void testPlaceholder() {
        Message a = translator.messageBuilder("a").withDefault("{value}").build();
        assertEquals(
                Component.text("b"),
                render(a.insertString("value", "b"))
        );

        Message c = translator.messageBuilder("c").withDefault("<value/>").build();
        assertEquals(
                Component.text("d"),
                render(c.insertString("value", "d"))
        );
    }

    @Test
    public void testObject() {
        translator.add(TinyObjectResolver.builder(Player.class)
                .with("name", Player::name)
                .with("location", Player::location)
                .withFallback(player -> Component.text(player.name))
                .build());
        translator.add(TinyObjectResolver.builder(Location.class)
                .with("x", Location::x)
                .with("y", Location::y)
                .with("z", Location::z)
                .withFallback(l -> Component.text("<" + l.x + ";" + l.y + ";" + l.z + ">"))
                .build());

        Message msg = translator.messageBuilder("msg").withDefault("my test {player}").build();
        Assertions.assertEquals(
                Component.text("my test peter"),
                render(msg.insertObject("player", new Player("peter", new Location(1, 2, 3))))
        );
        msg = translator.messageBuilder("msg").withDefault("my test {player:name}").build();
        Assertions.assertEquals(
                Component.text("my test peter"),
                render(msg.insertObject("player", new Player("peter", new Location(1, 2, 3))))
        );
        msg = translator.messageBuilder("msg").withDefault("my test {player:location}").build();
        Assertions.assertEquals(
                Component.text("my test <1;2;3>"),
                render(msg.insertObject("player", new Player("peter", new Location(1, 2, 3))))
        );
    }

    @Test
    public void testAppResolvers() {
        translator.add(TinyObjectResolver.builder(Description.class)
                .with("name", Description::name)
                .withFallback(d -> Component.text(d.name))
                .build());
        translator.insertObject("desc", new Description("tim"));
        Message a = translator.messageBuilder("a").withDefault("{desc:name}").build();
        Assertions.assertEquals(
                Component.text("tim"),
                translator.translate(a)
        );
    }

    @Test
    public void testLoadMultipleLocales() {
        Message a = translator.messageBuilder("a")
                .withDefault("Hello world!")
                .withTranslation(Locale.GERMANY, "Hallo Welt!")
                .withTranslation(Locale.FRENCH, "Bonjour le monde!")
                .build();
        translator.saveLocale(Locale.ENGLISH);
        translator.saveLocale(Locale.GERMANY);
        translator.saveLocale(Locale.FRENCH);
        translator.loadLocales();
        translator.saveLocale(Locale.ENGLISH);
        translator.saveLocale(Locale.GERMANY);
        translator.saveLocale(Locale.FRENCH);
        translator.loadLocales();

        Assertions.assertEquals(
                Component.text("Hello world!"),
                render(a, Locale.ENGLISH)
        );
        Assertions.assertEquals(
                Component.text("Hello world!"),
                translator.translate(a, Locale.ITALIAN)
        );
        Assertions.assertEquals(
                Component.text("Hello world!"),
                translator.translate(a, Locale.GERMAN)
        );
        Assertions.assertEquals(
                Component.text("Hallo Welt!"),
                translator.translate(a, Locale.GERMANY)
        );
        Assertions.assertEquals(
                Component.text("Bonjour le monde!"),
                translator.translate(a, Locale.FRANCE)
        );
        Assertions.assertEquals(
                Component.text("Bonjour le monde!"),
                translator.translate(a, Locale.FRENCH)
        );
    }

    @Test
    void testDuplicateTextInsertion() {
        // Based on bug reported from a user of the framework

        translator.getStyleSet().put("a", "<red>{slot}</red>");
        PlainTextComponentSerializer p = PlainTextComponentSerializer.plainText();
        Assertions.assertEquals(
                p.serialize(translator.translate("<a>test</a><green>test</green>").compact()),
                p.serialize(translator.translate("<a>test<green>test</green>").compact())
        );
    }

    @Test
    void multipleChildren(@TempDir File d) {

        MessageTranslator server = TinyTranslations.globalTranslator(d);
        MessageTranslator plugin1 = server.fork("P1");
        MessageTranslator plugin2 = server.fork("P2");
        plugin1.messageBuilder("prefix").withDefault("[P1] ").build();
        plugin2.messageBuilder("prefix").withDefault("[P2] ").build();

        Message msg = Message.message("msg", "{msg:prefix}text");
        plugin2.addMessage(msg);

        assertEquals(
                Component.text("[P2] text"),
                render(msg).compact()
        );
        assertEquals(
                msg.getKey(),
                TranslationKey.of("msg")
        );
        assertEquals(
                Component.text("[P2] text"),
                render(Component.translatable("global.p2.msg")).compact()
        );
        plugin2.getMessageSet().remove(msg.getKey());
        plugin1.addMessage(msg);
        assertEquals(
                Component.text("[P1] text"),
                render(msg).compact()
        );
    }

    @Test
    void testMessageToString() {
        Message m = translator.messageBuilder("msg").withDefault("Hello").build();
        assertEquals(
                "Hello",
                m.toString(MessageEncoding.PLAIN)
        );

        Message p = translator.messageBuilder("parent").withDefault("{msg:msg} world!").build();
        assertEquals(
                "Hello world!",
                p.toString(MessageEncoding.PLAIN)
        );
    }

    private record Location(int x, int y, int z) {
    }

    private record Player(String name, Location location) {
    }

    private record Description(String name) {
    }
}
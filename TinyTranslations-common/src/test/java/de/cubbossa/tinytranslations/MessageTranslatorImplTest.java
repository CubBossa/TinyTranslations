package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.storage.MessageStorage;
import de.cubbossa.tinytranslations.storage.StorageEntry;
import de.cubbossa.tinytranslations.storage.properties.PropertiesMessageStorage;
import de.cubbossa.tinytranslations.tinyobject.TinyObjectResolver;
import de.cubbossa.tinytranslations.util.ComponentSplit;
import de.cubbossa.tinytranslations.util.ListSection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.Translator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.*;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.TextColor.color;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageTranslatorImplTest extends AbstractTest {

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

        assertRenderEquals(
                text("A").hoverEvent(translatable("spectatorMenu.next_page")),
                translator.translate("<hover:show_text:'{msg:b}'>{msg:a}</hover>")
        );
        translator.messageBuilder("next_page").withDefault("<tr:spectatorMenu.next_page>").build();
        assertRenderEquals(
                translatable("spectatorMenu.next_page").hoverEvent(translatable("spectatorMenu.next_page")),
                translator.translate("<hover:show_text:\"{msg:next_page}\">{msg:next_page}")
        );
    }


    @Test
    void translate() {
        translator.addMessages(TinyTranslations.messageFieldsFromClass(this.getClass()));

        translator.messageBuilder("c").withDefault("c").build();
        assertRenderEquals(
                text("abc"),
                render(translator.messageBuilder("x").withDefault("ab{msg:testapp:c}").build()).compact()
        );
        assertRenderEquals(
                text("abcd"),
                render(translator.messageBuilder("x").withDefault("ab{msg:testapp:c}d").build()).compact()
        );

        assertRenderEquals(text("Hello world", NamedTextColor.RED), translator.translate(SIMPLE));
        assertRenderEquals(text("Hallo welt - Deutschland"), translator.translate(SIMPLE, Locale.GERMANY));
        assertRenderEquals(text("Hallo welt - Deutsch"), translator.translate(SIMPLE, Locale.GERMAN));
        assertRenderEquals(text("Hallo welt - Deutsch"), translator.translate(SIMPLE, Locale.forLanguageTag("de-AT")));

        assertRenderEquals(
                text("Embedded: ").append(text("Hello world", NamedTextColor.RED).append(text("a"))).compact(),
                render(EMBED, Locale.ENGLISH)
        );
    }

    @Test
    void resolver() {
        Message m = translator.messageBuilder("a").withDefault("a <b>").build();
        assertRenderEquals(text("a 1"), render(m.insertNumber("b", 1)));
        assertRenderEquals(text("a 1"), render(m.formatted(Formatter.number("b", 1))));
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

        assertRenderEquals(text("Hello \nworld!", NamedTextColor.RED), translator.translate(TEST_1));
        assertRenderEquals(text("Hallo Welt!", NamedTextColor.RED), render(TEST_1, Locale.GERMAN));

        translator.saveLocale(Locale.ENGLISH);
        translator.loadLocale(Locale.ENGLISH);
        assertRenderEquals(text("Hello \nworld!", NamedTextColor.RED), translator.translate(TEST_1));

        assertRenderEquals(text("Luke - I am your father!", NamedTextColor.GREEN), translator.translate(TEST_2));
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
            public Map<TranslationKey, StorageEntry> readMessages(Locale locale) {
                return Map.of(TranslationKey.of(translator.getPath(), "a"),  new StorageEntry("a", "Worked!", null));
            }

            @Override
            public Collection<Message> overwriteMessages(Collection<Message> messages, Locale locale) {
                return Collections.emptyList();
            }

            @Override
            public Collection<Message> writeMessages(Collection<Message> messages, Locale locale) {
                return Collections.emptyList();
            }
        });
        assertRenderEquals(translator.translate(abc), text("Yo!"));
        translator.loadLocales();
        assertRenderEquals(translator.translate(abc), text("Worked!"));
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
            public Map<TranslationKey, StorageEntry> readMessages(Locale locale) {
                return Map.of(TranslationKey.of(translator.getPath(), "a"), new StorageEntry("a", "Worked!", null));
            }

            @Override
            public Collection<Message> overwriteMessages(Collection<Message> messages, Locale locale) {
                return Collections.emptyList();
            }

            @Override
            public Collection<Message> writeMessages(Collection<Message> messages, Locale locale) {
                return Collections.emptyList();
            }
        });
        assertRenderEquals(text("Yo!"), translator.translate(abc));
        translator.loadLocales();
        assertRenderEquals(text("Worked!"), translator.translate(abc));
    }

    @Test
    public void placeholderInTag() {

        assertRenderEquals(
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

        assertRenderEquals(
                empty().append(text("Plugin", color(0xff00ff))).append(text(" Message")),
                plugin.translate(msg)
        );
    }

    @Test
    public void testList() {

        List<Boolean> list = List.of(true, false, true);
        Message a = translator.messageBuilder("a.b.c").withDefault("Header\n<list:','>{el ? <green>true</green> : <red>false</red>}</list>\nFooter").build();

        a = a.insertList("list", list, ListSection.paged(0, 2));
        assertRenderEquals(
                text("Header\n")
                        .append(text("true", NamedTextColor.GREEN))
                        .append(text(","))
                        .append(text("false", NamedTextColor.RED))
                        .append(text("\nFooter")),
                a
        );

        Message b = translator.messageBuilder("a.b.c").withDefault("Header\n<list:'\n'>{index}.) {el ? <green>true</green> : <red>false</red>}</list>\nFooter").build();

        var other = b.insertList("list", list, ListSection.paged(0, 2));
        assertRenderEquals(
                text("Header\n")
                        .append(text("1.) ").append(text("true", NamedTextColor.GREEN)))
                        .append(text("\n2.) ").append(text("false", NamedTextColor.RED)))
                        .append(text("\nFooter")),
                other
        );

        other = b.insertList("list", l -> list.subList(l.getOffset(), l.getOffset() + l.getRange()), ListSection.paged(0, 2));
        assertRenderEquals(
                text("Header\n")
                        .append(text("1.) ").append(text("true", NamedTextColor.GREEN)))
                        .append(text("\n2.) ").append(text("false", NamedTextColor.RED)))
                        .append(text("\nFooter")),
                other
        );
    }

    @Test
    public void testListOnUnownedMessage() {
        Message msg = Message.message("testListOnUnownedMessage", "Header\n<list:'\n'>{index}.) {el ? <green>true</green> : <red>false</red>}</list>\nFooter");
        translator.addMessage(msg);

        assertRenderEquals(
                text("Header\n")
                        .append(text("1.) ").append(text("true", NamedTextColor.GREEN)))
                        .append(text("\n2.) ").append(text("false", NamedTextColor.RED)))
                        .append(text("\nFooter")),
                msg.insertList("list", List.of(true, false))
        );

        assertRenderEquals(
                text("Header\n")
                        .append(text("1.) ").append(text("true", NamedTextColor.GREEN)))
                        .append(text("\n2.) ").append(text("false", NamedTextColor.RED)))
                        .append(text("\nFooter")),
                msg.insertList("list", s -> List.of(true, false), ListSection.paged(0, 10))
        );
    }

    private record TestData(String xy) {
    }

    @Test
    public void testListWithObjects() {
        Message msg = Message.message("aööö", "<list>{el}</list>");
        translator.addMessage(msg);
        var r = TinyObjectResolver.builder(TestData.class)
                .with("xy", TestData::xy)
                .withFallback(TestData::xy)
                .build();
        translator.add(r);

        Message inserted = msg.insertList("list", List.of(
                new TestData("a"), new TestData("b")
        ));

        assertRenderEquals(
                text("a, b"),
                inserted
        );

        translator.remove(r);
    }

    @Test
    public void testMultiplePlaceholders() {
        Message a = translator.messageBuilder("a").withDefault("<a/><b/><c/>").build();
        assertRenderEquals(
                text("123"),
                a.insertNumber("a", 1).insertNumber("b", 2).insertNumber("c", 3)
        );
    }

    @Test
    public void testPlaceholder() {
        Message a = translator.messageBuilder("a").withDefault("{value}").build();
        assertRenderEquals(
                text("b"),
                a.insertString("value", "b")
        );

        Message c = translator.messageBuilder("c").withDefault("<value/>").build();
        assertRenderEquals(
                text("d"),
                c.insertString("value", "d")
        );
    }

    @Test
    public void testObject() {
        translator.add(TinyObjectResolver.builder(Player.class)
                .with("name", Player::name)
                .with("location", Player::location)
                .withFallback(player -> text(player.name))
                .build());
        translator.add(TinyObjectResolver.builder(Location.class)
                .with("x", Location::x)
                .with("y", Location::y)
                .with("z", Location::z)
                .withFallback(l -> text("<" + l.x + ";" + l.y + ";" + l.z + ">"))
                .build());

        Message msg = translator.messageBuilder("msg").withDefault("my test {player}").build();
        assertRenderEquals(
                text("my test peter"),
                msg.insertObject("player", new Player("peter", new Location(1, 2, 3)))
        );
        msg = translator.messageBuilder("msg").withDefault("my test {player:name}").build();
        assertRenderEquals(
                text("my test peter"),
                msg.insertObject("player", new Player("peter", new Location(1, 2, 3)))
        );
        msg = translator.messageBuilder("msg").withDefault("my test {player:location}").build();
        assertRenderEquals(
                text("my test <1;2;3>"),
                msg.insertObject("player", new Player("peter", new Location(1, 2, 3)))
        );
    }

    @Test
    public void testAppResolvers() {
        translator.add(TinyObjectResolver.builder(Description.class)
                .with("name", Description::name)
                .withFallback(d -> text(d.name))
                .build());
        translator.insertObject("desc", new Description("tim"));
        Message a = translator.messageBuilder("a").withDefault("{desc:name}").build();
        assertRenderEquals(
                text("tim"),
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

        assertRenderEquals(
                text("Hello world!"),
                render(a, Locale.ENGLISH)
        );
        assertRenderEquals(
                text("Hello world!"),
                translator.translate(a, Locale.ITALIAN)
        );
        assertRenderEquals(
                text("Hello world!"),
                translator.translate(a, Locale.GERMAN)
        );
        assertRenderEquals(
                text("Hallo Welt!"),
                translator.translate(a, Locale.GERMANY)
        );
        assertRenderEquals(
                text("Bonjour le monde!"),
                translator.translate(a, Locale.FRANCE)
        );
        assertRenderEquals(
                text("Bonjour le monde!"),
                translator.translate(a, Locale.FRENCH)
        );
    }

    @Test
    void testOverwriteAndBackup(@TempDir File dir) {
        translator.setMessageStorage(new PropertiesMessageStorage(dir));

        Message a = Message.builder("a").withDefault("Old value")
                .withComment("head").build();
        translator.addMessage(a);
        translator.saveLocale(translator.defaultLocale());
        translator.loadLocale(translator.defaultLocale());
        assertEquals(a.comment(), translator.getMessage("a").comment());

        a = a.dictionaryEntry(translator.defaultLocale(), "New Value");
        translator.saveMessagesAndBackupExistingValues(Set.of(a), translator.defaultLocale());
        assertEquals("head\nBacked up value: 'Old value'", translator.getMessage("a").comment());
    }

    @Test
    void testOverrideIfEquals(@TempDir File dir) {
        translator.setMessageStorage(new PropertiesMessageStorage(dir));
        Message a = translator.messageBuilder("a").withDefault("spelo").build();
        Message b = translator.messageBuilder("b").withDefault("no spello").build();

        translator.saveLocale(translator.defaultLocale());
        a = a.dictionaryEntry(translator.defaultLocale(), "no spello");
        b = b.dictionaryEntry(translator.defaultLocale(), "spelo");

        translator.saveMessagesIfOldValueEquals(Map.of(
                a, "spelo",
                b, "spelo"
        ), translator.defaultLocale());
        translator.loadLocales();

        assertEquals("no spello", translator.getMessage("a").dictionary().get(translator.defaultLocale()));
        assertEquals("no spello", translator.getMessage("b").dictionary().get(translator.defaultLocale()));
    }

    @Test
    void testDuplicateTextInsertion() {
        // Based on bug reported from a user of the framework

        translator.getStyleSet().put("a", "<red>{slot}</red>");
        PlainTextComponentSerializer p = PlainTextComponentSerializer.plainText();
        assertEquals(
                p.serialize(translator.translate("<a>test</a><green>test</green>").compact()),
                p.serialize(translator.translate("<a>test<green>test</green>").compact())
        );
    }

    @Test
    void testCommentTags(@TempDir File dir) {
        translator.setMessageStorage(new PropertiesMessageStorage(dir, "messages_", ""));

        Message message = translator.messageBuilder("a")
                .withComment("A comment")
                .withPlaceholder("player", "a player", Player.class)
                .withPlaceholder("players", "the online player list", List.class)
                .withPlaceholder("surprise")
                .withPlaceholder("d", "with description")
                .build();
        assertEquals(
                """
                        A comment
                        <player> (Player): a player
                        <players> (List): the online player list
                        <surprise>
                        <d>: with description""",
                message.comment()
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

        assertRenderEquals(
                text("[P2] text"),
                msg
        );
        assertEquals(
                msg.getKey(),
                TranslationKey.of("msg")
        );
        assertRenderEquals(
                text("[P2] text"),
                translatable("global.p2.msg")
        );
        plugin2.getMessageSet().remove(msg.getKey());
        plugin1.addMessage(msg);
        assertRenderEquals(
                text("[P1] text"),
                msg
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
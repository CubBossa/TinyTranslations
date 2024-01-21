package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.storage.properties.PropertiesMessageStorage;
import de.cubbossa.tinytranslations.util.ComponentSplit;
import de.cubbossa.tinytranslations.util.ListSection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static net.kyori.adventure.text.Component.text;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageTranslatorImplTest extends TestBase {

	public static final Message SIMPLE = new MessageBuilder("simple")
			.withDefault("<red>Hello world")
			.withTranslation(Locale.GERMANY, "Hallo welt - Deutschland")
			.withTranslation(Locale.GERMAN, "Hallo welt - Deutsch")
			.withComment("abc")
			.build();
	public static final Message EMBED = new MessageBuilder("embedded")
			.withDefault("Embedded: <msg:simple>a")
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
		assertEquals(
				2,
				ComponentSplit.split(translator.translate(NEW_LINE), "\n").size()
		);
	}


	@Test
	void translate() {
		translator.addMessages(TinyTranslations.messageFieldsFromClass(this.getClass()));

		assertEquals(text("Hello world", NamedTextColor.RED), translator.translate(SIMPLE));
		assertEquals(text("Hallo welt - Deutschland"), translator.translate(SIMPLE, Locale.GERMANY));
		assertEquals(text("Hallo welt - Deutsch"), translator.translate(SIMPLE, Locale.GERMAN));
		assertEquals(text("Hallo welt - Deutsch"), translator.translate(SIMPLE, Locale.forLanguageTag("de-AT")));

		assertEquals(
				text("Embedded: ").append(text("Hello worlda", NamedTextColor.RED)),
				translator.translate(EMBED, Locale.ENGLISH)
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
		assertEquals(text("Hallo Welt!", NamedTextColor.RED), translator.translate(TEST_1, Locale.GERMAN));

		translator.saveLocale(Locale.ENGLISH);
		translator.loadLocale(Locale.ENGLISH);
		assertEquals(text("Hello \nworld!", NamedTextColor.RED), translator.translate(TEST_1));

		assertEquals(text("Luke - I am your father!", NamedTextColor.GREEN), translator.translate(TEST_2));
	}

	@Test
	public void testList() {

		List<Boolean> list = List.of(true, false, true);
		Message a = translator.messageBuilder("a.B.c").withDefault("Header\n<list:','>\nFooter").build();
		Message b = translator.messageBuilder("b").withDefault("{val ? '<green>true</green>' : '<red>false</red>' }").build();

		a = a.insertList("list", list, ListSection.paged(0, 2), v -> b.insertBool("val", v));
		assertEquals(
				text("Header\n")
						.append(text("true", NamedTextColor.GREEN))
						.append(text(","))
						.append(text("false", NamedTextColor.RED))
						.append(text("\nFooter")),
				render(a).compact()
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

	private record Location(int x, int y, int z) {}
	private record Player(String name, Location location) {}

	@Test
	public void testObject() {
		TinyTranslations.NM.getObjectTypeResolverMap().put(Player.class, Map.of(
				"name", Player::name,
				"location", Player::location
		), player -> Component.text(player.name));
		TinyTranslations.NM.getObjectTypeResolverMap().put(Location.class, Map.of(
				"x", Location::x,
				"y", Location::y,
				"z", Location::z
		), l -> Component.text("<" + l.x + ";" + l.y + ";" + l.z + ">"));

		Message msg = translator.messageBuilder("msg").withDefault("my test {player}").build();
		Assertions.assertEquals(
				Component.text("my test peter"),
				translator.translate(msg.insertObject("player", new Player("peter", new Location(1,2,3))))
		);
		msg = translator.messageBuilder("msg").withDefault("my test {player:name}").build();
		Assertions.assertEquals(
				Component.text("my test peter"),
				translator.translate(msg.insertObject("player", new Player("peter", new Location(1,2,3))))
		);
		msg = translator.messageBuilder("msg").withDefault("my test {player:location}").build();
		Assertions.assertEquals(
				Component.text("my test <1;2;3>"),
				translator.translate(msg.insertObject("player", new Player("peter", new Location(1,2,3))))
		);
	}

	private record Description(String name) {}

	@Test
	public void testAppResolvers() {
		TinyTranslations.NM.getObjectTypeResolverMap().put(Description.class, Map.of(
				"name", Description::name
		), d -> Component.text(d.name));
		translator.insertObject("desc", new Description("tim"));
		Message a = translator.messageBuilder("a").withDefault("{desc:name}").build();
		Assertions.assertEquals(
				Component.text("tim"),
				translator.translate(a)
		);
	}

	@Test
	public void testInheritedResolvers() {
		TinyTranslations.NM.getObjectTypeResolverMap().put(Description.class, Map.of(
				"name", Description::name
		), d -> Component.text(d.name));

		Message a = translator.messageBuilder("a").withDefault("<b>123</b>").build();
		translator.getStyleSet().put("b", "{slot}{msg:c}");
		translator.messageBuilder("c").withDefault("4{desc:name}").build();

		Assertions.assertEquals(
				Component.text("12345"),
				translator.translate(a.insertObject("desc", new Description("5")))
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
				translator.translate(a, Locale.ENGLISH)
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
}
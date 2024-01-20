package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.storage.properties.PropertiesMessageStorage;
import de.cubbossa.tinytranslations.util.ComponentSplit;
import de.cubbossa.tinytranslations.util.ListSection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static net.kyori.adventure.text.Component.empty;
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
				ComponentSplit.split(messageTranslator.process(NEW_LINE), "\n").size()
		);
	}

	@Test
	void globalMsg() {
		MessageTranslator global = TinyTranslations.global();
		global.messageBuilder("brand").withDefault("<red>Prefix</red> ").build();

		Message b = messageTranslator.messageBuilder("b").withDefault("<msg:global:brand>b").build();
		assertEquals(empty().append(text("Prefix", NamedTextColor.RED)).append(text(" b")), messageTranslator.process(b).compact());

		Message a = messageTranslator.messageBuilder("a").withDefault("<msg:brand>a").build();
		assertEquals(empty().append(text("Prefix", NamedTextColor.RED)).append(text(" a")), messageTranslator.process(a).compact());
	}

	@Test
	void globalMsgOverride() {
		MessageTranslator global = TinyTranslations.global();
		global.messageBuilder("brand").withDefault("<red>Prefix</red> ").build();
		messageTranslator.messageBuilder("brand").withDefault("<green>Prefix</green> ").build();

		Message b = messageTranslator.messageBuilder("b").withDefault("<msg:global:brand>b").build();
		assertEquals(empty().append(text("Prefix", NamedTextColor.RED)).append(text(" b")), messageTranslator.process(b).compact());

		Message a = messageTranslator.messageBuilder("a").withDefault("<msg:brand>a").build();
		assertEquals(empty().append(text("Prefix", NamedTextColor.GREEN)).append(text(" a")), messageTranslator.process(a).compact());
	}

	@Test
	void globalStyle() {
		MessageTranslator global = TinyTranslations.global();
		global.getStyleSet().put("negative", Style.style(NamedTextColor.RED));
		Message m = messageTranslator.messageBuilder("a").withDefault("<negative>abc").build();
		assertEquals(text("abc", NamedTextColor.RED), messageTranslator.process(m));
	}

	@Test
	void globalStyleOverride() {
		MessageTranslator global = TinyTranslations.global();
		global.getStyleSet().put("negative", Style.style(NamedTextColor.RED));
		messageTranslator.getStyleSet().put("negative", Style.style(TextDecoration.UNDERLINED));
		Message m = messageTranslator.messageBuilder("a").withDefault("<negative>abc").build();
		assertEquals(text("abc").decorate(TextDecoration.UNDERLINED), messageTranslator.process(m));
	}

	@Test
	void globalOverride() {
		MessageTranslator global = TinyTranslations.global();
		messageTranslator.getStyleSet().put("negative", "<yellow>");
		Message m = global.messageBuilder("test").withDefault("<negative>test</negative>").build();
		assertEquals(text("test", NamedTextColor.RED), global.process(m));
		assertEquals(text("test", NamedTextColor.YELLOW), messageTranslator.process(m));
	}

	@Test
	void translate() {
		messageTranslator.addMessages(TinyTranslations.messageFieldsFromClass(this.getClass()));

		assertEquals(text("Hello world", NamedTextColor.RED), messageTranslator.process(SIMPLE));
		assertEquals(text("Hallo welt - Deutschland"), messageTranslator.process(SIMPLE, Locale.GERMANY));
		assertEquals(text("Hallo welt - Deutsch"), messageTranslator.process(SIMPLE, Locale.GERMAN));
		assertEquals(text("Hallo welt - Deutsch"), messageTranslator.process(SIMPLE, Locale.forLanguageTag("de-AT")));

		assertEquals(
				text("Embedded: ").append(text("Hello worlda", NamedTextColor.RED)),
				messageTranslator.process(EMBED, Locale.ENGLISH)
		);
	}

	@Test
	void resolver() {
		Message m = messageTranslator.messageBuilder("a").withDefault("a <b>").build();
		assertEquals(text("a 1"), m.formatted(Formatter.number("b", 1)).asComponent());
	}

	@Test
	void getMessage() {
		Assertions.assertNull(messageTranslator.getMessage("a"));
		Message m1 = messageTranslator.messageBuilder("a").withDefault("A").build();
		assertEquals(m1, messageTranslator.getMessage("a"));
	}

	@Test
	void getInParent() {
		Message m1 = messageTranslator.messageBuilder("a").withDefault("A").build();
		Message m2 = messageTranslator.getParent().messageBuilder("b").withDefault("B").build();
		assertEquals(m1, messageTranslator.getMessageInParentTree("a"));
		assertEquals(m2, messageTranslator.getMessageInParentTree("b"));
		Assertions.assertNull(messageTranslator.getMessageInParentTree("x"));
	}

	@Test
	void getByKey() {
		Message m1 = messageTranslator.getParent().message("a");
		assertEquals(m1, messageTranslator.getMessageByNamespace("global", "a"));
		Message m2 = messageTranslator.message("a");
		assertEquals(m1, messageTranslator.getMessageByNamespace("global", "a"));
	}

	@Test
	public void testFileCreation() {

		File inner = new File(dir, "/TestApp/");
		inner.mkdirs();

		messageTranslator.setMessageStorage(new PropertiesMessageStorage(inner));
		messageTranslator.messageBuilder("key").withDefault("abcde").build();

		messageTranslator.saveLocale(Locale.ENGLISH);

		File en = new File(inner, "en.properties");
		Assertions.assertTrue(inner.exists());
		Assertions.assertTrue(en.exists());

		replaceInFile(en, "a", "e");
		String before = fileContent(en);
		messageTranslator.saveLocale(Locale.ENGLISH);
		assertEquals(before, fileContent(en));
	}

	@Test
	public void testLoad() {

		messageTranslator.addMessages(TEST_1, TEST_2);

		assertEquals(text("Hello \nworld!", NamedTextColor.RED), messageTranslator.process(TEST_1));
		assertEquals(text("Hallo Welt!", NamedTextColor.RED), messageTranslator.process(TEST_1, Locale.GERMAN));

		messageTranslator.saveLocale(Locale.ENGLISH);
		messageTranslator.loadLocale(Locale.ENGLISH);
		assertEquals(text("Hello \nworld!", NamedTextColor.RED), messageTranslator.process(TEST_1));

		assertEquals(text("Luke - I am your father!", NamedTextColor.GREEN), messageTranslator.process(TEST_2));
	}

	@Test
	public void testList() {

		List<Boolean> list = List.of(true, false, true);
		Message a = messageTranslator.messageBuilder("a.B.c").withDefault("Header\n<list:','>\nFooter").build();
		Message b = messageTranslator.messageBuilder("b").withDefault("<val:'<green>true</green>':'<red>false</red>'>").build();

		a = a.insertList("list", list, ListSection.paged(0, 2), v -> b.insertBool("val", v));
		assertEquals(
				text("Header\n")
						.append(text("true", NamedTextColor.GREEN))
						.append(text(","))
						.append(text("false", NamedTextColor.RED))
						.append(text("\nFooter")),
				a.asComponent().compact()
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

		Message msg = messageTranslator.messageBuilder("msg").withDefault("my test {player}").build();
		Assertions.assertEquals(
				Component.text("my test peter"),
				messageTranslator.process(msg.insertObject("player", new Player("peter", new Location(1,2,3))))
		);
		msg = messageTranslator.messageBuilder("msg").withDefault("my test {player:name}").build();
		Assertions.assertEquals(
				Component.text("my test peter"),
				messageTranslator.process(msg.insertObject("player", new Player("peter", new Location(1,2,3))))
		);
		msg = messageTranslator.messageBuilder("msg").withDefault("my test {player:location}").build();
		Assertions.assertEquals(
				Component.text("my test <1;2;3>"),
				messageTranslator.process(msg.insertObject("player", new Player("peter", new Location(1,2,3))))
		);
	}

	private record Description(String name) {}

	@Test
	public void testAppResolvers() {
		TinyTranslations.NM.getObjectTypeResolverMap().put(Description.class, Map.of(
				"name", Description::name
		), d -> Component.text(d.name));
		messageTranslator.insertObject("desc", new Description("tim"));
		Message a = messageTranslator.messageBuilder("a").withDefault("{desc:name}").build();
		Assertions.assertEquals(
				Component.text("tim"),
				messageTranslator.process(a)
		);
	}

	@Test
	public void testInheritedResolvers() {
		TinyTranslations.NM.getObjectTypeResolverMap().put(Description.class, Map.of(
				"name", Description::name
		), d -> Component.text(d.name));

		Message a = messageTranslator.messageBuilder("a").withDefault("<b>123</b>").build();
		messageTranslator.getStyleSet().put("b", "{slot}{msg:c}");
		messageTranslator.messageBuilder("c").withDefault("4{desc:name}").build();

		Assertions.assertEquals(
				Component.text("12345"),
				messageTranslator.process(a.insertObject("desc", new Description("5")))
		);
	}

	@Test
	public void testLoadMultipleLocales() {
		Message a = messageTranslator.messageBuilder("a")
				.withDefault("Hello world!")
				.withTranslation(Locale.GERMANY, "Hallo Welt!")
				.withTranslation(Locale.FRENCH, "Bonjour le monde!")
				.build();
		messageTranslator.saveLocale(Locale.ENGLISH);
		messageTranslator.saveLocale(Locale.GERMANY);
		messageTranslator.saveLocale(Locale.FRENCH);
		messageTranslator.loadLocales();
		messageTranslator.saveLocale(Locale.ENGLISH);
		messageTranslator.saveLocale(Locale.GERMANY);
		messageTranslator.saveLocale(Locale.FRENCH);
		messageTranslator.loadLocales();

		Assertions.assertEquals(
				Component.text("Hello world!"),
				messageTranslator.process(a, Locale.ENGLISH)
		);
		Assertions.assertEquals(
				Component.text("Hello world!"),
				messageTranslator.process(a, Locale.ITALIAN)
		);
		Assertions.assertEquals(
				Component.text("Hello world!"),
				messageTranslator.process(a, Locale.GERMAN)
		);
		Assertions.assertEquals(
				Component.text("Hallo Welt!"),
				messageTranslator.process(a, Locale.GERMANY)
		);
		Assertions.assertEquals(
				Component.text("Bonjour le monde!"),
				messageTranslator.process(a, Locale.FRANCE)
		);
		Assertions.assertEquals(
				Component.text("Bonjour le monde!"),
				messageTranslator.process(a, Locale.FRENCH)
		);
	}

	@Test
	void testDuplicateTextInsertion() {
		// Based on bug reported from a user of the framework

		messageTranslator.getStyleSet().put("a", "<red>{slot}</red>");
		PlainTextComponentSerializer p = PlainTextComponentSerializer.plainText();
		Assertions.assertEquals(
				p.serialize(messageTranslator.process("<a>test</a><green>test</green>").compact()),
				p.serialize(messageTranslator.process("<a>test<green>test</green>").compact())
		);
	}
}
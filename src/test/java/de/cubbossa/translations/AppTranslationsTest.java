package de.cubbossa.translations;

import de.cubbossa.translations.persistent.PropertiesMessageStorage;
import de.cubbossa.translations.util.ComponentSplit;
import de.cubbossa.translations.util.ListSection;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Locale;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTranslationsTest extends TestBase {

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
				ComponentSplit.split(translations.process(NEW_LINE), "\n").size()
		);
	}

	@Test
	void globalMsg() {
		Translations global = TranslationsFramework.global();
		global.messageBuilder("brand").withDefault("<red>Prefix</red> ").build();

		Message b = translations.messageBuilder("b").withDefault("<msg:global:brand>b").build();
		assertEquals(empty().append(text("Prefix", NamedTextColor.RED)).append(text(" b")), translations.process(b).compact());

		Message a = translations.messageBuilder("a").withDefault("<msg:brand>a").build();
		assertEquals(empty().append(text("Prefix", NamedTextColor.RED)).append(text(" a")), translations.process(a).compact());
	}

	@Test
	void globalMsgOverride() {
		Translations global = TranslationsFramework.global();
		global.messageBuilder("brand").withDefault("<red>Prefix</red> ").build();
		translations.messageBuilder("brand").withDefault("<green>Prefix</green> ").build();

		Message b = translations.messageBuilder("b").withDefault("<msg:global:brand>b").build();
		assertEquals(empty().append(text("Prefix", NamedTextColor.RED)).append(text(" b")), translations.process(b).compact());

		Message a = translations.messageBuilder("a").withDefault("<msg:brand>a").build();
		assertEquals(empty().append(text("Prefix", NamedTextColor.GREEN)).append(text(" a")), translations.process(a).compact());
	}

	@Test
	void globalStyle() {
		Translations global = TranslationsFramework.global();
		global.getStyleSet().put("negative", Style.style(NamedTextColor.RED));
		Message m = translations.messageBuilder("a").withDefault("<negative>abc").build();
		assertEquals(text("abc", NamedTextColor.RED), translations.process(m));
	}

	@Test
	void globalStyleOverride() {
		Translations global = TranslationsFramework.global();
		global.getStyleSet().put("negative", Style.style(NamedTextColor.RED));
		translations.getStyleSet().put("negative", Style.style(TextDecoration.UNDERLINED));
		Message m = translations.messageBuilder("a").withDefault("<negative>abc").build();
		assertEquals(text("abc").decorate(TextDecoration.UNDERLINED), translations.process(m));
	}

	@Test
	void translate() {
		translations.addMessages(TranslationsFramework.messageFieldsFromClass(this.getClass()));

		assertEquals(text("Hello world", NamedTextColor.RED), translations.process(SIMPLE));
		assertEquals(text("Hallo welt - Deutschland"), translations.process(SIMPLE, Locale.GERMANY));
		assertEquals(text("Hallo welt - Deutsch"), translations.process(SIMPLE, Locale.GERMAN));
		assertEquals(text("Hallo welt - Deutsch"), translations.process(SIMPLE, Locale.forLanguageTag("de-AT")));

		assertEquals(
				text("Embedded: ").append(text("Hello worlda", NamedTextColor.RED)),
				translations.process(EMBED, Locale.ENGLISH)
		);
	}

	@Test
	void resolver() {
		Message m = translations.messageBuilder("a").withDefault("a <b>").build();
		assertEquals(text("a 1"), m.formatted(Formatter.number("b", 1)).asComponent());
	}

	@Test
	void getMessage() {
		Assertions.assertNull(translations.getMessage("a"));
		Message m1 = translations.messageBuilder("a").withDefault("A").build();
		assertEquals(m1, translations.getMessage("a"));
	}

	@Test
	void getInParent() {
		Message m1 = translations.messageBuilder("a").withDefault("A").build();
		Message m2 = translations.getParent().messageBuilder("b").withDefault("B").build();
		assertEquals(m1, translations.getMessageInParentTree("a"));
		assertEquals(m2, translations.getMessageInParentTree("b"));
		Assertions.assertNull(translations.getMessageInParentTree("x"));
	}

	@Test
	void getByKey() {
		Message m1 = translations.getParent().message("a");
		assertEquals(m1, translations.getMessageByNamespace("global", "a"));
		Message m2 = translations.message("a");
		assertEquals(m1, translations.getMessageByNamespace("global", "a"));
	}

	@Test
	public void testFileCreation() {

		File inner = new File(dir, "/TestApp/");
		inner.mkdirs();

		translations.setMessageStorage(new PropertiesMessageStorage(inner));
		translations.messageBuilder("key").withDefault("abcde").build();

		translations.saveLocale(Locale.ENGLISH);

		File en = new File(inner, "en.properties");
		Assertions.assertTrue(inner.exists());
		Assertions.assertTrue(en.exists());

		replaceInFile(en, "a", "e");
		String before = fileContent(en);
		translations.saveLocale(Locale.ENGLISH);
		assertEquals(before, fileContent(en));
	}

	@Test
	public void testLoad() {

		translations.addMessages(TEST_1, TEST_2);

		assertEquals(text("Hello \nworld!", NamedTextColor.RED), translations.process(TEST_1));
		assertEquals(text("Hallo Welt!", NamedTextColor.RED), translations.process(TEST_1, Locale.GERMAN));

		translations.saveLocale(Locale.ENGLISH);
		translations.loadLocale(Locale.ENGLISH);
		assertEquals(text("Hello \nworld!", NamedTextColor.RED), translations.process(TEST_1));

		assertEquals(text("Luke - I am your father!", NamedTextColor.GREEN), translations.process(TEST_2));
	}

	@Test
	public void testList() {

		List<Boolean> list = List.of(true, false, true);
		Message a = translations.messageBuilder("a.B.c").withDefault("Header\n<list:','>\nFooter").build();
		Message b = translations.messageBuilder("b").withDefault("<val:'<green>true</green>':'<red>false</red>'>").build();

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
}
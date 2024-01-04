package de.cubbossa.tinytranslations;

import de.cubbossa.tinytranslations.persistent.PropertiesMessageStorage;
import de.cubbossa.tinytranslations.util.ComponentSplit;
import de.cubbossa.tinytranslations.util.ListSection;
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

class AppTranslatorTest extends TestBase {

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
				ComponentSplit.split(translator.process(NEW_LINE), "\n").size()
		);
	}

	@Test
	void globalMsg() {
		Translator global = TinyTranslations.global();
		global.messageBuilder("brand").withDefault("<red>Prefix</red> ").build();

		Message b = translator.messageBuilder("b").withDefault("<msg:global:brand>b").build();
		assertEquals(empty().append(text("Prefix", NamedTextColor.RED)).append(text(" b")), translator.process(b).compact());

		Message a = translator.messageBuilder("a").withDefault("<msg:brand>a").build();
		assertEquals(empty().append(text("Prefix", NamedTextColor.RED)).append(text(" a")), translator.process(a).compact());
	}

	@Test
	void globalMsgOverride() {
		Translator global = TinyTranslations.global();
		global.messageBuilder("brand").withDefault("<red>Prefix</red> ").build();
		translator.messageBuilder("brand").withDefault("<green>Prefix</green> ").build();

		Message b = translator.messageBuilder("b").withDefault("<msg:global:brand>b").build();
		assertEquals(empty().append(text("Prefix", NamedTextColor.RED)).append(text(" b")), translator.process(b).compact());

		Message a = translator.messageBuilder("a").withDefault("<msg:brand>a").build();
		assertEquals(empty().append(text("Prefix", NamedTextColor.GREEN)).append(text(" a")), translator.process(a).compact());
	}

	@Test
	void globalStyle() {
		Translator global = TinyTranslations.global();
		global.getStyleSet().put("negative", Style.style(NamedTextColor.RED));
		Message m = translator.messageBuilder("a").withDefault("<negative>abc").build();
		assertEquals(text("abc", NamedTextColor.RED), translator.process(m));
	}

	@Test
	void globalStyleOverride() {
		Translator global = TinyTranslations.global();
		global.getStyleSet().put("negative", Style.style(NamedTextColor.RED));
		translator.getStyleSet().put("negative", Style.style(TextDecoration.UNDERLINED));
		Message m = translator.messageBuilder("a").withDefault("<negative>abc").build();
		assertEquals(text("abc").decorate(TextDecoration.UNDERLINED), translator.process(m));
	}

	@Test
	void translate() {
		translator.addMessages(TinyTranslations.messageFieldsFromClass(this.getClass()));

		assertEquals(text("Hello world", NamedTextColor.RED), translator.process(SIMPLE));
		assertEquals(text("Hallo welt - Deutschland"), translator.process(SIMPLE, Locale.GERMANY));
		assertEquals(text("Hallo welt - Deutsch"), translator.process(SIMPLE, Locale.GERMAN));
		assertEquals(text("Hallo welt - Deutsch"), translator.process(SIMPLE, Locale.forLanguageTag("de-AT")));

		assertEquals(
				text("Embedded: ").append(text("Hello worlda", NamedTextColor.RED)),
				translator.process(EMBED, Locale.ENGLISH)
		);
	}

	@Test
	void resolver() {
		Message m = translator.messageBuilder("a").withDefault("a <b>").build();
		assertEquals(text("a 1"), m.formatted(Formatter.number("b", 1)).asComponent());
	}

	@Test
	void getMessage() {
		Assertions.assertNull(translator.getMessage("a"));
		Message m1 = translator.messageBuilder("a").withDefault("A").build();
		assertEquals(m1, translator.getMessage("a"));
	}

	@Test
	void getInParent() {
		Message m1 = translator.messageBuilder("a").withDefault("A").build();
		Message m2 = translator.getParent().messageBuilder("b").withDefault("B").build();
		assertEquals(m1, translator.getMessageInParentTree("a"));
		assertEquals(m2, translator.getMessageInParentTree("b"));
		Assertions.assertNull(translator.getMessageInParentTree("x"));
	}

	@Test
	void getByKey() {
		Message m1 = translator.getParent().message("a");
		assertEquals(m1, translator.getMessageByNamespace("global", "a"));
		Message m2 = translator.message("a");
		assertEquals(m1, translator.getMessageByNamespace("global", "a"));
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

		assertEquals(text("Hello \nworld!", NamedTextColor.RED), translator.process(TEST_1));
		assertEquals(text("Hallo Welt!", NamedTextColor.RED), translator.process(TEST_1, Locale.GERMAN));

		translator.saveLocale(Locale.ENGLISH);
		translator.loadLocale(Locale.ENGLISH);
		assertEquals(text("Hello \nworld!", NamedTextColor.RED), translator.process(TEST_1));

		assertEquals(text("Luke - I am your father!", NamedTextColor.GREEN), translator.process(TEST_2));
	}

	@Test
	public void testList() {

		List<Boolean> list = List.of(true, false, true);
		Message a = translator.messageBuilder("a.B.c").withDefault("Header\n<list:','>\nFooter").build();
		Message b = translator.messageBuilder("b").withDefault("<val:'<green>true</green>':'<red>false</red>'>").build();

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
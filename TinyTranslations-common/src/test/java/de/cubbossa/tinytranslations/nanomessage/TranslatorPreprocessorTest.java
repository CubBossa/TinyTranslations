package de.cubbossa.tinytranslations.nanomessage;

import de.cubbossa.tinytranslations.TestBase;
import de.cubbossa.tinytranslations.nanomessage.compiler.NanoMessageCompiler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TranslatorPreprocessorTest extends TestBase {


	@Test
	void simple() {
		NanoMessageCompiler pp = new NanoMessageCompiler();

		Assertions.assertEquals(
				"<gradient:black:dark_gray:black>----------- <primary><page:'}{}}'/></primary>/<primary_d><pages/></primary_d> -----------</gradient>",
				pp.compile("<gradient:black:dark_gray:black>----------- <primary>{page:'}{}}'}</primary>/<primary_d>{pages}</primary_d> -----------</gradient>")
		);

		Assertions.assertEquals(
				"<red><nbt:'{\"some\":\"text\"}'/><nbt/><nbt:'test</red>'/>",
				pp.compile("<red><nbt>{\"some\":\"text\"}</nbt><nbt/><nbt>test</red>")
		);

		Assertions.assertEquals(
				"<red><legacy:'&':'&6Some Text'/><legacy/><legacy:'test</red>'/>",
				pp.compile("<red><legacy:'&'>&6Some Text</legacy><legacy/><legacy>test</red>")
		);

		Assertions.assertEquals(
				"<red><choice:'<time:minutes/>':'minute':'minutes'/></red>",
				pp.compile("<red>{time:minutes ? 'minute' : 'minutes'}</red>")
		);
	}

	@Test
	void embedded() {
		Assertions.assertEquals(
				Component.text("Red", NamedTextColor.RED).append(Component.text("Blue", NamedTextColor.DARK_BLUE)),
				translator.process("<red>Red<legacy:'&'>&1Blue</legacy></red>")
		);
		Assertions.assertEquals(
				Component.text("Red", NamedTextColor.RED).append(Component.text("<Blue>", NamedTextColor.DARK_BLUE)),
				translator.process("<red>Red<nbt>{\"text\":\"<Blue>\",\"color\":\"dark_blue\"}</nbt></red>")
		);
	}

	@Test
	void avoidWrongClose() {
		NanoMessageCompiler pp = new NanoMessageCompiler();

		Assertions.assertEquals(
				"<<nbt:'{\"text\":\"\\</nbt>\"}<nbt/>'/>",
				pp.compile("<<nbt>{\"text\":\"\\</nbt>\"}<nbt/></nbt>")
		);

		Assertions.assertEquals(
				"<nbt:'<nbt>x'/></nbt>",
				pp.compile("<nbt><nbt>x</nbt></nbt>")
		);
	}
}

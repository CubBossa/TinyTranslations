package de.cubbossa.tinytranslations.nanomessage;

import de.cubbossa.tinytranslations.Message;
import de.cubbossa.tinytranslations.TestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Locale;

class MessageLoopDetectorTest extends TestBase {

	@Test
	void detectLoops() {

		Message a = messageTranslator.messageBuilder("a").withDefault("{msg:a}").build();
		Assertions.assertFalse(new MessageLoopDetector().detectLoops(a).isEmpty());

		Message b = messageTranslator.messageBuilder("b").withDefault("{msg:c}").build();
		messageTranslator.messageBuilder("c").withDefault("{msg:b}").build();
		Assertions.assertFalse(new MessageLoopDetector().detectLoops(b).isEmpty());

		Message d = messageTranslator.messageBuilder("d").withDefault("<e>abc</e>").build();
		messageTranslator.getStyleSet().put("e", "{msg:f}{slot}");
		messageTranslator.messageBuilder("f").withDefault("{msg:d}").build();
		Assertions.assertFalse(new MessageLoopDetector().detectLoops(d).isEmpty());
	}

	@Test
	void detectLoopsOnLoad() {

		messageTranslator.messageBuilder("a")
				.withTranslation(Locale.ENGLISH, "{msg:a}").build();
		messageTranslator.loadLocale(Locale.ENGLISH);
		Assertions.assertNotEquals(
				"{msg:a}",
				messageTranslator.getMessage("a").getDictionary().get(Locale.ENGLISH)
		);
	}
}
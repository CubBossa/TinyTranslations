package de.cubbossa.tinytranslations.nanomessage;

import de.cubbossa.tinytranslations.Message;
import de.cubbossa.tinytranslations.MessageStyle;
import de.cubbossa.tinytranslations.TestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageLoopDetectorTest extends TestBase {

	@Test
	void detectLoops() {

		Message a = translator.messageBuilder("a").withDefault("{msg:a}").build();
		Assertions.assertFalse(new MessageLoopDetector().detectLoops(a).isEmpty());

		Message b = translator.messageBuilder("b").withDefault("{msg:c}").build();
		translator.messageBuilder("c").withDefault("{msg:b}").build();
		Assertions.assertFalse(new MessageLoopDetector().detectLoops(b).isEmpty());

		Message d = translator.messageBuilder("d").withDefault("<e>abc</e>").build();
		translator.getStyleSet().put("e", "{msg:f}{slot}");
		translator.messageBuilder("f").withDefault("{msg:d}").build();
		Assertions.assertFalse(new MessageLoopDetector().detectLoops(d).isEmpty());
	}
}
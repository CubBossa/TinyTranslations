package de.cubbossa.tinytranslations.nanomessage;

import de.cubbossa.tinytranslations.Message;
import de.cubbossa.tinytranslations.MessageReferenceLoopDetector;
import de.cubbossa.tinytranslations.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Locale;

class MessageReferenceLoopDetectorTest extends AbstractTest {

    @Test
    void detectLoops() {

        Message a = translator.messageBuilder("a").withDefault("{msg:a}").build();
        Assertions.assertFalse(new MessageReferenceLoopDetector().detectLoops(a).isEmpty());

        Message b = translator.messageBuilder("b").withDefault("{msg:c}").build();
        translator.messageBuilder("c").withDefault("{msg:b}").build();
        Assertions.assertFalse(new MessageReferenceLoopDetector().detectLoops(b).isEmpty());

        Message d = translator.messageBuilder("d").withDefault("<e>abc</e>").build();
        translator.getStyleSet().put("e", "{msg:f}{slot}");
        translator.messageBuilder("f").withDefault("{msg:d}").build();
        Assertions.assertFalse(new MessageReferenceLoopDetector().detectLoops(d).isEmpty());
    }

    @Test
    void detectLoopsOnLoad() {

        translator.messageBuilder("a")
                .withTranslation(Locale.ENGLISH, "{msg:a}").build();
        translator.loadLocale(Locale.ENGLISH);
        Assertions.assertNotEquals(
                "{msg:a}",
                translator.getMessage("a").dictionary().get(Locale.ENGLISH)
        );
    }
}
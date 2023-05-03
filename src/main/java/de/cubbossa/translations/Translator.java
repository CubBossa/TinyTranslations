package de.cubbossa.translations;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

import java.util.Locale;

public interface Translator {

    String translateRaw(Message message);

    String translateRaw(Message message, Audience audience);

    String translateRaw(Message message, Locale locale);

    Component translate(Message message);

    Component translate(Message message, Audience audience);

    Component translate(Message message, Locale locale);
}

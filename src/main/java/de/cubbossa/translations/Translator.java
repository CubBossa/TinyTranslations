package de.cubbossa.translations;

import net.kyori.adventure.audience.Audience;

public interface Translator {

    String translate(Message message);

    String translate(Message message, Audience audience);
}

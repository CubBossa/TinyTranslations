package de.cubbossa.translations;

import de.cubbossa.translations.persistent.MessageStorage;
import de.cubbossa.translations.persistent.StyleStorage;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Locale;

@Getter
@Setter
public class ApplTranslations implements Translations {

    private MiniMessage miniMessage;

    private MessageSet messageSet;
    private MessageStorage messageStorage;
    private StyleSet styleSet;
    private StyleStorage styleStorage;

    @Override
    public void loadLocale(Locale locale) {

    }

    @Override
    public void loadStyles() {

    }

    @Override
    public void saveStyles() {

    }

    @Override
    public void saveLocale(Locale locale) {

    }


    @Override
    public TagResolver getMessageResolver(Audience audience) {
        return null;
    }

    @Override
    public String translateRaw(Message message) {
        return null;
    }

    @Override
    public String translateRaw(Message message, Audience audience) {
        return null;
    }

    @Override
    public String translateRaw(Message message, Locale locale) {
        return null;
    }

    @Override
    public Component translate(Message message) {
        return null;
    }

    @Override
    public Component translate(Message message, Audience audience) {
        return null;
    }

    @Override
    public Component translate(Message message, Locale locale) {
        return null;
    }
}

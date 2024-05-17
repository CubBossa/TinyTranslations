package de.cubbossa.tinytranslations;

public class BungeeGlobalMessages {

    public static final Message FORMAT_PLAYER = new MessageBuilder("format.player")
            .withDefault("<hover:show_text:'{player:uuid}'>{player:name}</hover>")
            .withPlaceholder("player")
            .build();
}

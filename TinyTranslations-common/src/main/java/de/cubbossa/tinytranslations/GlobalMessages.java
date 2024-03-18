package de.cubbossa.tinytranslations;

import java.util.Locale;

public final class GlobalMessages {

    public static Locale[] LOCALES = {
            Locale.GERMAN,
            new Locale("de", "AU"),
            new Locale("de", "CH"),
            Locale.ENGLISH,
            new Locale("es"),
            new Locale("fr"),
            new Locale("ru"),
            new Locale("zh"),
    };

    public static final Message BRAND = Message.message("brand");
    public static final Message PREFIX = Message.message("prefix");

    public static final Message JOIN = Message.builder("multiplayer.join")
            .withPlaceholder("player")
            .build();
    public static final Message JOIN_RENAMED = Message.builder("multiplayer.join_renamed")
            .withPlaceholders("player", "old_name")
            .build();
    public static final Message QUIT = Message.builder("multiplayer.quit")
            .withPlaceholder("player")
            .build();

    public static final Message NEXT_PAGE = Message.message("next_page");
    public static final Message PREV_PAGE = Message.message("previous_page");

    public static final Message NO_PERM = Message.message("missing_permissions");
    public static final Message NO_PERM_SPECIFIED = new MessageBuilder("missing_permission")
            .withPlaceholder("permission", "The required node")
            .build();
    public static final Message NO_PERM_CMD = Message.message("command.missing_permissions");
    public static final Message PLAYER_ONLY = Message.message("player_required");
    public static final Message CMD_PLAYER_ONLY = Message.message("command.player_required");
    public static final Message CONSOLE_ONLY = Message.message("console_required");
    public static final Message CMD_CONSOLE_ONLY = Message.message("command.console_required");

    public static final Message ARROW_RIGHT = Message.message("symbols.arrow_right");
    public static final Message ARROW_LEFT = Message.message("symbols.arrow_left");

}

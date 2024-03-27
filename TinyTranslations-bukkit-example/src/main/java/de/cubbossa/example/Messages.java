package de.cubbossa.example;

import de.cubbossa.tinytranslations.*;

import java.util.List;

public class Messages {

    // Everything within "withDefault" method calls represents ENGLISH

    public static Message NO_PERM;
    public static Message PLAYER_LIST;
    public static Message INTEGER_REQUIRED;
    public static Message CMD_PLAYERS_SYNTAX;

    public static void init(MessageTranslator translator) {
        NO_PERM = translator.messageBuilder("error.no_permission")
                // Let's just use {msg:global:no_perm} so that the global preferences are being used
                .withDefault(GlobalMessages.NO_PERM)
                .withComment("Used to indicate missing permissions")
                .build();
        PLAYER_LIST = translator.messageBuilder("cmd.playerlist.list")
                .withDefault("""
                        ---- Players Online: {count}<repeat:30>-</repeat>
                        <players:'\n'><text_d>{index}.) </text_d><text>{player:name}</text></players>
                        ---- <click:run_command:"/players {prev_page}">{msg:symbols.arrow_left}</click> {page}/{pages} <click:run_command:"/players {next_page}">{msg:symbols.arrow_right}</click> <repeat:30></repeat>""")
                .withPlaceholders(Formattable.LIST_PLACEHOLDERS)
                .withPlaceholder("players", "list of online players", List.class)
                .withComment("Lists all online players in a paginated list.")
                .build();
        INTEGER_REQUIRED = translator.messageBuilder("error.integer_required")
                .withDefault("<prefix_negative>Number value expected, found '{input}'</prefix_negative>")
                .withPlaceholder("input", Number.class)
                .build();
        CMD_PLAYERS_SYNTAX = translator.messageBuilder("cmd.playerlist.syntax")
                .withDefault("<prefix_negative>Wrong command usage. Use <cmd_syntax>/players <arg_opt>page</arg_opt></cmd_syntax></prefix_negative>")
                .build();
    }
}

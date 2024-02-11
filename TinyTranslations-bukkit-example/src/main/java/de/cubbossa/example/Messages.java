package de.cubbossa.example;

import de.cubbossa.tinytranslations.GlobalMessages;
import de.cubbossa.tinytranslations.GlobalStyles;
import de.cubbossa.tinytranslations.Message;
import de.cubbossa.tinytranslations.MessageBuilder;

public class Messages {

    public static final Message NO_PERM = new MessageBuilder("error.no_permission")
            // Let's just use {msg:global:no_perm} so that the global preferences are being used
            .withDefault(GlobalMessages.NO_PERM)
            .withComment("Used to indicate missing permissions")
            .build();
    public static final Message PLAYER_LIST = new MessageBuilder("cmd.playerlist.list")
            .withDefault("""
            ---- Players Online: {count}<repeat:30>-</repeat>
            <players:'\n'><text_d>{index}.) </text_d><text>{player:name}</text></players>
            ---- <click:run_command:"/players {prev_page}">{msg:symbols.arrow_left}</click> {page}/{pages} <click:run_command:"/players {next_page}">{msg:symbols.arrow_right}</click> <repeat:30></repeat>""")
            .withPlaceholders("page", "pages", "players", "next_page", "prev_page", "...")
            .withComment("Lists all online players in a paginated list.")
            .build();
    public static final Message INTEGER_REQUIRED = new MessageBuilder("error.integer_required")
            .withDefault("<prefix_negative>Number value expected, found '{input}'</prefix_negative>")
            .withPlaceholder("input")
            .build();
    public static final Message CMD_PLAYERS_SYNTAX = new MessageBuilder("cmd.playerlist.syntax")
            .withDefault("<prefix_negative>Wrong command usage. Use <cmd_syntax>/players <arg_opt>page</arg_opt></cmd_syntax></prefix_negative>")
            .build();
}

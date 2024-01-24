package de.cubbossa.tinytranslations;

public final class GlobalMessages {

	public static final Message BRAND = new MessageBuilder("brand")
			.withDefault("<gradient:#ff2200:#ffff00:#ff2200>My Server Name</gradient>")
			.build();

	public static final Message PREFIX = new MessageBuilder("prefix")
			.withDefault("")
			.withComment("Empty placeholder to make global messages work without application to set prefix message")
			.build();

	public static final Message JOIN = Message.builder("multiplayer.join")
			.withDefault("<text>{tr:'multiplayer.player.joined':'{player}'}</text>")
			.withPlaceholder("player").build();
	public static final Message JOIN_RENAMED = Message.builder("multiplayer.join_renamed")
			.withDefault("<text>{tr:'multiplayer.player.joined.renamed':'{player}':'{old_name}'}</text>")
			.withPlaceholder("player", "old_name").build();
	public static final Message QUIT = Message.builder("multiplayer.quit")
			.withDefault("<text>{tr:'multiplayer.player.left':'{player}'}</text>")
			.withPlaceholder("player").build();

	public static final Message NEXT_PAGE = Message.message("next_page", "{tr:'spectatorMenu.next_page'}");
	public static final Message PREV_PAGE = Message.message("previous_page", "{tr:'spectatorMenu.previous_page'}");

	public static final Message NO_PERM = new MessageBuilder("missing_permissions")
			.withDefault("<prefix_negative>You don't have enough permission to perform this action!</prefix_negative>")
			.build();
	public static final Message NO_PERM_SPECIFIED = new MessageBuilder("missing_permission")
			.withDefault("<prefix_negative>You don't have the required permission!</prefix_negative> <text_d>(<text>{permission}</text>)</text_d>")
			.withPlaceholder("permission", "The required node")
			.build();
	public static final Message NO_PERM_CMD = new MessageBuilder("command.missing_permissions")
			.withDefault("<prefix_negative>You don't have enough permission to perform this command!</prefix_negative>")
			.build();
	public static final Message PLAYER_ONLY = new MessageBuilder("player_required")
			.withDefault("<prefix_negative>You must be a player to perform this action!</prefix_negative>")
			.build();
	public static final Message CMD_PLAYER_ONLY = new MessageBuilder("command.player_required")
			.withDefault("<prefix_negative>You must be a player to execute this command!</prefix_negative>")
			.build();
	public static final Message CONSOLE_ONLY = new MessageBuilder("console_required")
			.withDefault("<prefix_negative>This action can only be performed via console!</prefix_negative>")
			.build();
	public static final Message CMD_CONSOLE_ONLY = new MessageBuilder("command.console_required")
			.withDefault("<prefix_negative>This command can only be executed via console!</prefix_negative>")
			.build();

	public static final Message ARROW_RIGHT = new MessageBuilder("symbols.arrow_right")
			.withDefault("→")
			.build();
	public static final Message ARROW_LEFT = new MessageBuilder("symbols.arrow_left")
			.withDefault("←")
			.build();

}

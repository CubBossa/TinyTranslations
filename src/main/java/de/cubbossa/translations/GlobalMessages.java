package de.cubbossa.translations;

public class GlobalMessages {

	public static final Message BRAND = new MessageBuilder("brand")
			.withDefault("<gradient:#ff2200:#ffff00:#ff2200>My Server Name</gradient>")
			.build();

	public static final Message PREFIX = new MessageBuilder("prefix")
			.withDefault("")
			.withComment("Empty placeholder to make global messages work without application to set prefix message")
			.build();

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

	public static final Message ARROW_RIGHT = new MessageBuilder("symbols.arrow_right")
			.withDefault("🠖")
			.build();
	public static final Message ARROW_LEFT = new MessageBuilder("symbols.arrow_left")
			.withDefault("🠔")
			.build();

}

package de.cubbossa.tinytranslations;

import java.util.Locale;

public final class GlobalMessages {

	private static final Locale GERMAN = Locale.GERMAN;
	private static final Locale FRENCH = Locale.FRENCH;
	private static final Locale SPANISH = Locale.forLanguageTag("es");

	public static final Message BRAND = new MessageBuilder("brand")
			.withDefault("<gradient:#ff2200:#ffff00:#ff2200>My Server Name</gradient>")
			.build();

	public static final Message PREFIX = new MessageBuilder("prefix")
			.withDefault("")
			.withComment("Empty placeholder to make global messages work without application to set prefix message")
			.build();

	public static final Message NO_PERM = new MessageBuilder("missing_permissions")
			.withDefault("<prefix_negative>You don't have enough permission to perform this action!</prefix_negative>")
			.withTranslation(SPANISH, "<prefix_negative>No tienes permiso suficiente para realizar esta acción.</prefix_negative>")
			.withTranslation(GERMAN, "<prefix_negative>Du hast nicht genug Berechtigung, um diese Aktion durchzuführen!</prefix_negative>")
			.withTranslation(FRENCH, "<prefix_negative>Tu n'as pas assez d'autorisations pour effectuer cette action!</prefix_negative>")
			.build();
	public static final Message NO_PERM_SPECIFIED = new MessageBuilder("missing_permission")
			.withDefault("<prefix_negative>You don't have the required permission!</prefix_negative> <text_d>(<text>{permission}</text>)</text_d>")
			.withTranslation(SPANISH, "<prefix_negative>No tiene el permiso necesario.</prefix_negative> <text_d>(<text>{permission}</text>)</text_d>")
			.withTranslation(GERMAN, "<prefix_negative>Du hast nicht die erforderliche Berechtigung!</prefix_negative> <text_d>(<text>{permission}</text>)</text_d>")
			.withTranslation(FRENCH, "<prefix_negative>Tu n'as pas l'autorisation requise!</prefix_negative> <text_d>(<text>{permission}</text>)</text_d>")
			.withPlaceholder("permission", "The required node")
			.build();
	public static final Message NO_PERM_CMD = new MessageBuilder("command.missing_permissions")
			.withDefault("<prefix_negative>You don't have enough permission to perform this command!</prefix_negative>")
			.withTranslation(SPANISH, "<prefix_negative>No tienes permiso suficiente para ejecutar este comando.</prefix_negative>")
			.withTranslation(GERMAN, "<prefix_negative>Du hast nicht genug Berechtigung, um diesen Befehl auszuführen!</prefix_negative>")
			.withTranslation(FRENCH, "<prefix_negative>Tu n'as pas assez de droits pour exécuter cette commande!</prefix_negative>")
			.build();
	public static final Message PLAYER_ONLY = new MessageBuilder("player_required")
			.withDefault("<prefix_negative>You must be a player to perform this action!</prefix_negative>")
			.withTranslation(SPANISH, "<prefix_negative>Debes ser jugador para realizar esta acción.</prefix_negative>")
			.withTranslation(GERMAN, "<prefix_negative>Du musst ein Spieler sein, um diese Aktion durchzuführen!</prefix_negative>")
			.withTranslation(FRENCH, "<prefix_negative>Tu dois être un joueur pour effectuer cette action!</prefix_negative>")
			.build();
	public static final Message CMD_PLAYER_ONLY = new MessageBuilder("command.player_required")
			.withDefault("<prefix_negative>You must be a player to execute this command!</prefix_negative>")
			.withTranslation(SPANISH, "<prefix_negative>Debes ser un jugador para ejecutar este comando.</prefix_negative>")
			.withTranslation(GERMAN, "<prefix_negative>Du musst ein Spieler sein, um diesen Befehl auszuführen!</prefix_negative>")
			.withTranslation(FRENCH, "<prefix_negative>Tu dois être un joueur pour exécuter cet ordre!</prefix_negative>")
			.build();
	public static final Message CONSOLE_ONLY = new MessageBuilder("console_required")
			.withDefault("<prefix_negative>This action can only be performed via console!</prefix_negative>")
			.withTranslation(SPANISH, "<prefix_negative>Esta acción sólo puede realizarse a través de la consola.</prefix_negative>")
			.withTranslation(GERMAN, "<prefix_negative>Diese Aktion kann nur über die Konsole durchgeführt werden!</prefix_negative>")
			.withTranslation(FRENCH, "<prefix_negative>Cette action ne peut être effectuée que via la console!</prefix_negative>")
			.build();
	public static final Message CMD_CONSOLE_ONLY = new MessageBuilder("command.console_required")
			.withDefault("<prefix_negative>This command can only be executed via console!</prefix_negative>")
			.withTranslation(SPANISH, "<prefix_negative>Este comando sólo puede ejecutarse a través de la consola.</prefix_negative>")
			.withTranslation(GERMAN, "<prefix_negative>Dieser Befehl kann nur über die Konsole ausgeführt werden!</prefix_negative>")
			.withTranslation(FRENCH, "<prefix_negative>Cette commande ne peut être exécutée que via la console!</prefix_negative>")
			.build();

	public static final Message ARROW_RIGHT = new MessageBuilder("symbols.arrow_right")
			.withDefault("→")
			.build();
	public static final Message ARROW_LEFT = new MessageBuilder("symbols.arrow_left")
			.withDefault("←")
			.build();

}

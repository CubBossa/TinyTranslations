package de.cubbossa.tinytranslations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

import java.util.Locale;

public final class GlobalMessages {

	private static final Locale ENGLISH = Locale.ENGLISH;
	private static final Locale RUSSIAN = Locale.forLanguageTag("ru");
	private static final Locale CHINESE = Locale.SIMPLIFIED_CHINESE;
	private static final Locale FRENCH = Locale.FRENCH;
	private static final Locale SPANISH = Locale.forLanguageTag("es");

	/*
	  "permissions.requires.entity": "An entity is required to run this command here",
  "permissions.requires.player": "A player is required to run this command here",
  "argument.anchor.invalid": "Invalid entity anchor position %s",
  "argument.angle.incomplete": "Incomplete (expected 1 angle)",
  "argument.angle.invalid": "Invalid angle",
  "argument.block.id.invalid": "Unknown block type '%s'",
  "argument.block.property.duplicate": "Property '%s' can only be set once for block %s",
  "argument.block.property.invalid": "Block %s does not accept '%s' for %s property",
  "argument.block.property.novalue": "Expected value for property '%s' on block %s",
  "argument.block.property.unclosed": "Expected closing ] for block state properties",
  "argument.block.property.unknown": "Block %s does not have property '%s'",
  "argument.block.tag.disallowed": "Tags aren't allowed here, only actual blocks",
  "argument.color.invalid": "Unknown color '%s'",
  "argument.component.invalid": "Invalid chat component: %s",
  "argument.criteria.invalid": "Unknown criterion '%s'",
  "argument.dimension.invalid": "Unknown dimension '%s'",
  "argument.double.big": "Double must not be more than %s, found %s",
  "argument.double.low": "Double must not be less than %s, found %s",
  "argument.entity.invalid": "Invalid name or UUID",
  "argument.entity.notfound.entity": "No entity was found",
  "argument.entity.notfound.player": "No player was found",
  "argument.entity.options.advancements.description": "Players with advancements",
  "argument.entity.options.distance.description": "Distance to entity",
  "argument.entity.options.distance.negative": "Distance cannot be negative",
  "argument.entity.options.dx.description": "Entities between x and x + dx",
  "argument.entity.options.dy.description": "Entities between y and y + dy",
  "argument.entity.options.dz.description": "Entities between z and z + dz",
  "argument.entity.options.gamemode.description": "Players with game mode",
  "argument.entity.options.inapplicable": "Option '%s' isn't applicable here",
  "argument.entity.options.level.description": "Experience level",
  "argument.entity.options.level.negative": "Level shouldn't be negative",
  "argument.entity.options.limit.description": "Maximum number of entities to return",
  "argument.entity.options.limit.toosmall": "Limit must be at least 1",
  "argument.entity.options.mode.invalid": "Invalid or unknown game mode '%s'",
  "argument.entity.options.name.description": "Entity name",
  "argument.entity.options.nbt.description": "Entities with NBT",
  "argument.entity.options.predicate.description": "Custom predicate",
  "argument.entity.options.scores.description": "Entities with scores",
  "argument.entity.options.sort.description": "Sort the entities",
  "argument.entity.options.sort.irreversible": "Invalid or unknown sort type '%s'",
  "argument.entity.options.tag.description": "Entities with tag",
  "argument.entity.options.team.description": "Entities on team",
  "argument.entity.options.type.description": "Entities of type",
  "argument.entity.options.type.invalid": "Invalid or unknown entity type '%s'",
  "argument.entity.options.unknown": "Unknown option '%s'",
  "argument.entity.options.unterminated": "Expected end of options",
  "argument.entity.options.valueless": "Expected value for option '%s'",
  "argument.entity.options.x_rotation.description": "Entity's x rotation",
  "argument.entity.options.x.description": "x position",
  "argument.entity.options.y_rotation.description": "Entity's y rotation",
  "argument.entity.options.y.description": "y position",
  "argument.entity.options.z.description": "z position",
  "argument.entity.selector.allEntities": "All entities",
  "argument.entity.selector.allPlayers": "All players",
  "argument.entity.selector.missing": "Missing selector type",
  "argument.entity.selector.nearestPlayer": "Nearest player",
  "argument.entity.selector.not_allowed": "Selector not allowed",
  "argument.entity.selector.randomPlayer": "Random player",
  "argument.entity.selector.self": "Current entity",
  "argument.entity.selector.unknown": "Unknown selector type '%s'",
  "argument.entity.toomany": "Only one entity is allowed, but the provided selector allows more than one",
  "argument.enum.invalid": "Invalid value \"%s\"",
  "argument.float.big": "Float must not be more than %s, found %s",
  "argument.float.low": "Float must not be less than %s, found %s",
  "argument.gamemode.invalid": "Unknown game mode: %s",
  "argument.id.invalid": "Invalid ID",
  "argument.id.unknown": "Unknown ID: %s",
  "argument.integer.big": "Integer must not be more than %s, found %s",
  "argument.integer.low": "Integer must not be less than %s, found %s",
  "argument.item.id.invalid": "Unknown item '%s'",
  "argument.item.tag.disallowed": "Tags aren't allowed here, only actual items",
  "argument.literal.incorrect": "Expected literal %s",
  "argument.long.big": "Long must not be more than %s, found %s",
  "argument.long.low": "Long must not be less than %s, found %s",
  "argument.nbt.array.invalid": "Invalid array type '%s'",
  "argument.nbt.array.mixed": "Can't insert %s into %s",
  "argument.nbt.expected.key": "Expected key",
  "argument.nbt.expected.value": "Expected value",
  "argument.nbt.list.mixed": "Can't insert %s into list of %s",
  "argument.nbt.trailing": "Unexpected trailing data",
  "argument.player.entities": "Only players may be affected by this command, but the provided selector includes entities",
  "argument.player.toomany": "Only one player is allowed, but the provided selector allows more than one",
  "argument.player.unknown": "That player does not exist",
  "argument.pos.missing.double": "Expected a coordinate",
  "argument.pos.missing.int": "Expected a block position",
  "argument.pos.mixed": "Cannot mix world & local coordinates (everything must either use ^ or not)",
  "argument.pos.outofbounds": "That position is outside the allowed boundaries.",
  "argument.pos.outofworld": "That position is out of this world!",
  "argument.pos.unloaded": "That position is not loaded",
  "argument.pos2d.incomplete": "Incomplete (expected 2 coordinates)",
  "argument.pos3d.incomplete": "Incomplete (expected 3 coordinates)",
  "argument.range.empty": "Expected value or range of values",
  "argument.range.ints": "Only whole numbers allowed, not decimals",
  "argument.range.swapped": "Min cannot be bigger than max",
  "argument.resource_tag.invalid_type": "Tag '%s' has wrong type '%s' (expected '%s')",
  "argument.resource_tag.not_found": "Can't find tag '%s' of type '%s'",
  "argument.resource.invalid_type": "Element '%s' has wrong type '%s' (expected '%s')",
  "argument.resource.not_found": "Can't find element '%s' of type '%s'",
  "argument.rotation.incomplete": "Incomplete (expected 2 coordinates)",
  "argument.scoreboardDisplaySlot.invalid": "Unknown display slot '%s'",
  "argument.scoreHolder.empty": "No relevant score holders could be found",
  "argument.style.invalid": "Invalid style: %s",
  "argument.time.invalid_tick_count": "Tick count must be non-negative",
  "argument.time.invalid_unit": "Invalid unit",
  "argument.time.tick_count_too_low": "Tick count must not be less than %s, found %s",
  "argument.uuid.invalid": "Invalid UUID",
  "arguments.block.tag.unknown": "Unknown block tag '%s'",
  "arguments.function.tag.unknown": "Unknown function tag '%s'",
  "arguments.function.unknown": "Unknown function %s",
  "arguments.item.overstacked": "%s can only stack up to %s",
  "arguments.item.tag.unknown": "Unknown item tag '%s'",
  "arguments.nbtpath.node.invalid": "Invalid NBT path element",
  "arguments.nbtpath.nothing_found": "Found no elements matching %s",
  "arguments.nbtpath.too_deep": "Resulting NBT too deeply nested",
  "arguments.nbtpath.too_large": "Resulting NBT too large",
  "arguments.objective.notFound": "Unknown scoreboard objective '%s'",
  "arguments.objective.readonly": "Scoreboard objective '%s' is read-only",
  "arguments.operation.div0": "Cannot divide by zero",
  "arguments.operation.invalid": "Invalid operation",
  "arguments.swizzle.invalid": "Invalid swizzle, expected combination of 'x', 'y' and 'z'",

  "parsing.bool.expected": "Expected boolean",
  "parsing.bool.invalid": "Invalid boolean, expected 'true' or 'false' but found '%s'",
  "parsing.double.expected": "Expected double",
  "parsing.double.invalid": "Invalid double '%s'",
  "parsing.expected": "Expected '%s'",
  "parsing.float.expected": "Expected float",
  "parsing.float.invalid": "Invalid float '%s'",
  "parsing.int.expected": "Expected integer",
  "parsing.int.invalid": "Invalid integer '%s'",
  "parsing.long.expected": "Expected long",
  "parsing.long.invalid": "Invalid long '%s'",
  "parsing.quote.escape": "Invalid escape sequence '\\%s' in quoted string",
  "parsing.quote.expected.end": "Unclosed quoted string",
  "parsing.quote.expected.start": "Expected quote to start a string",

  "predicate.unknown": "Unknown predicate: %s",

    "sleep.not_possible": "No amount of rest can pass this night",
  "sleep.players_sleeping": "%s/%s players sleeping",
  "sleep.skipping_night": "Sleeping through this night",

    "spectatorMenu.teleport": "Teleport to Player",
	 */

	public static final Message BRAND = new MessageBuilder("brand")
			.withTranslation(ENGLISH, "<gradient:#ff2200:#ffff00:#ff2200>My Server Name</gradient>")
			.build();

	public static final Message PREFIX = new MessageBuilder("prefix")
			.withTranslation(ENGLISH, "")
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
			.withTranslation(ENGLISH, "<prefix_negative>You don't have enough permission to perform this action!</prefix_negative>")
			.withTranslation(SPANISH, "<prefix_negative>No tienes permiso suficiente para realizar esta acción.</prefix_negative>")
			.withTranslation(CHINESE, "<prefix_negative>您没有足够的授权来执行此操作！</prefix_negative>")
			.withTranslation(RUSSIAN, "<prefix_negative>У вас недостаточно полномочий для выполнения этого действия!</prefix_negative>")
			.withTranslation(FRENCH, "<prefix_negative>Tu n'as pas assez d'autorisations pour effectuer cette action!</prefix_negative>")
			.build();
	public static final Message NO_PERM_SPECIFIED = new MessageBuilder("missing_permission")
			.withTranslation(ENGLISH, "<prefix_negative>You don't have the required permission!</prefix_negative> <text_d>(<text>{permission}</text>)</text_d>")
			.withTranslation(SPANISH, "<prefix_negative>No tiene el permiso necesario.</prefix_negative> <text_d>(<text>{permission}</text>)</text_d>")
			.withTranslation(CHINESE, "<prefix_negative>您没有所需的授权！</prefix_negative> <text_d>(<text>{permission}</text>)</text_d>")
			.withTranslation(RUSSIAN, "<prefix_negative>У вас нет необходимых полномочий!</prefix_negative> <text_d>(<text>{permission}</text>)</text_d>")
			.withTranslation(FRENCH, "<prefix_negative>Tu n'as pas l'autorisation requise!</prefix_negative> <text_d>(<text>{permission}</text>)</text_d>")
			.withPlaceholder("permission", "The required node")
			.build();
	public static final Message NO_PERM_CMD = new MessageBuilder("command.missing_permissions")
			.withTranslation(ENGLISH, "<prefix_negative>You don't have enough permission to perform this command!</prefix_negative>")
			.withTranslation(SPANISH, "<prefix_negative>No tienes permiso suficiente para ejecutar este comando.</prefix_negative>")
			.withTranslation(CHINESE, "<prefix_negative>您没有足够的权限执行此命令！</prefix_negative>")
			.withTranslation(RUSSIAN, "<prefix_negative>У вас недостаточно полномочий для выполнения этой команды!</prefix_negative>")
			.withTranslation(FRENCH, "<prefix_negative>Tu n'as pas assez de droits pour exécuter cette commande!</prefix_negative>")
			.build();
	public static final Message PLAYER_ONLY = new MessageBuilder("player_required")
			.withTranslation(ENGLISH, "<prefix_negative>You must be a player to perform this action!</prefix_negative>")
			.withTranslation(SPANISH, "<prefix_negative>Debes ser jugador para realizar esta acción.</prefix_negative>")
			.withTranslation(CHINESE, "<prefix_negative>您必须是玩家才能执行此操作！</prefix_negative>")
			.withTranslation(RUSSIAN, "<prefix_negative>Вы должны быть игроком, чтобы выполнить это действие!</prefix_negative>")
			.withTranslation(FRENCH, "<prefix_negative>Tu dois être un joueur pour effectuer cette action!</prefix_negative>")
			.build();
	public static final Message CMD_PLAYER_ONLY = new MessageBuilder("command.player_required")
			.withTranslation(ENGLISH, "<prefix_negative>You must be a player to execute this command!</prefix_negative>")
			.withTranslation(SPANISH, "<prefix_negative>Debes ser un jugador para ejecutar este comando.</prefix_negative>")
			.withTranslation(CHINESE, "<prefix_negative>您必须是玩家才能执行此命令！</prefix_negative>")
			.withTranslation(RUSSIAN, "<prefix_negative>Вы должны быть игроком, чтобы выполнить эту команду!</prefix_negative>")
			.withTranslation(FRENCH, "<prefix_negative>Tu dois être un joueur pour exécuter cet ordre!</prefix_negative>")
			.build();
	public static final Message CONSOLE_ONLY = new MessageBuilder("console_required")
			.withTranslation(ENGLISH, "<prefix_negative>This action can only be performed via console!</prefix_negative>")
			.withTranslation(SPANISH, "<prefix_negative>Esta acción sólo puede realizarse a través de la consola.</prefix_negative>")
			.withTranslation(CHINESE, "<prefix_negative>该操作只能通过控制台执行！</prefix_negative>")
			.withTranslation(RUSSIAN, "<prefix_negative>Это действие может быть выполнено только через консоль!</prefix_negative>")
			.withTranslation(FRENCH, "<prefix_negative>Cette action ne peut être effectuée que via la console!</prefix_negative>")
			.build();
	public static final Message CMD_CONSOLE_ONLY = new MessageBuilder("command.console_required")
			.withTranslation(ENGLISH, "<prefix_negative>This command can only be executed via console!</prefix_negative>")
			.withTranslation(SPANISH, "<prefix_negative>Este comando sólo puede ejecutarse a través de la consola.</prefix_negative>")
			.withTranslation(CHINESE, "<prefix_negative>该命令只能通过控制台执行！</prefix_negative>")
			.withTranslation(RUSSIAN, "<prefix_negative>Эта команда может быть выполнена только через консоль!</prefix_negative>")
			.withTranslation(FRENCH, "<prefix_negative>Cette commande ne peut être exécutée que via la console!</prefix_negative>")
			.build();

	public static final Message ARROW_RIGHT = new MessageBuilder("symbols.arrow_right")
			.withTranslation(ENGLISH, "→")
			.build();
	public static final Message ARROW_LEFT = new MessageBuilder("symbols.arrow_left")
			.withTranslation(ENGLISH, "←")
			.build();

}

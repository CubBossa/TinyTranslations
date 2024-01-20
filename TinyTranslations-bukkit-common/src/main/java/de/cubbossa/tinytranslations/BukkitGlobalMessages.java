package de.cubbossa.tinytranslations;

public class BukkitGlobalMessages {


	public static final Message FORMAT_PLAYER = new MessageBuilder("format.player")
			.withDefault("<hover:show_text:'{player:uuid}'>{player:name}</hover>")
			.withPlaceholder("player")
			.build();
	public static final Message FORMAT_ENTITY = new MessageBuilder("format.entity")
			.withDefault("{selector:'@[uuid={entity:uuid}]'}")
			.withPlaceholder("entity")
			.build();
	public static final Message FORMAT_WORLD = new MessageBuilder("format.world")
			.withDefault("{world:name}")
			.withPlaceholder("world")
			.build();
	public static final Message FORMAT_BLOCK = new MessageBuilder("format.block")
			.withDefault("<hover:show_text:'X: {bloc:x}\nY: {bloc:y}\nZ: {bloc:z}\nWorld: {bloc:world:name}'>{block:type}<{bloc:x};{bloc:y};{bloc:z}></hover>")
			.withPlaceholder("block")
			.build();
	public static final Message FORMAT_LOCATION = new MessageBuilder("format.location")
			.withDefault("<hover:show_text:'X: {loc:x}\nY: {loc:y}\nZ: {loc:z}\nYaw: {loc:yaw}\nPitch: {loc:pitch}\nWorld: {loc:world:name}'><{loc:x};{loc:y};{loc:z}></hover>")
			.withPlaceholder("loc")
			.build();
	public static final Message FORMAT_VECTOR = new MessageBuilder("format.vector")
			.withDefault("<{vector:x};{vector:y};{vector:z}>")
			.withPlaceholder("vector")
			.build();
	public static final Message FORMAT_MATERIAL = new MessageBuilder("format.material")
			.withDefault("{material}")
			.withPlaceholders("material")
			.build();
	public static final Message FORMAT_ENTITY_TYPE = new MessageBuilder("format.entity_type")
			.withDefault("{type}")
			.withPlaceholders("type")
			.build();
}

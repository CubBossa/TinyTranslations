package de.cubbossa.tinytranslations;

public class BukkitGlobalMessages {


    public static final Message FORMAT_PLAYER = new MessageBuilder("format.player")
            .withDefault("<hover:show_text:'ID: {player.uuid}'>{player.name}</hover>")
            .withPlaceholder("player")
            .build();
    public static final Message FORMAT_ENTITY = new MessageBuilder("format.entity")
            .withDefault("{selector:'@[uuid={entity.uuid}]'}")
            .withPlaceholder("entity")
            .build();
    public static final Message FORMAT_WORLD = new MessageBuilder("format.world")
            .withDefault("{world.name}")
            .withPlaceholder("world")
            .build();
    public static final Message FORMAT_BLOCK = new MessageBuilder("format.block")
            .withDefault("<hover:show_text:'X: {block.loc.x}\nY: {block.loc.y}\nZ: {block.loc.z}\nWorld: {block.loc.world.name}'>{block.type}<{block.loc.x};{block.loc.y};{block.loc.z}></hover>")
            .withPlaceholder("block")
            .build();
    public static final Message FORMAT_LOCATION = new MessageBuilder("format.location")
            .withDefault("<hover:show_text:'X: {loc.x}\nY: {loc.y}\nZ: {loc.z}\nYaw: {loc.yaw}\nPitch: {loc.pitch}\nWorld: {loc.world.name}'><{loc.x};{loc.y};{loc.z}></hover>")
            .withPlaceholder("loc")
            .build();
    public static final Message FORMAT_VECTOR = new MessageBuilder("format.vector")
            .withDefault("<{vector.x};{vector.y};{vector.z}>")
            .withPlaceholder("vector")
            .build();
    public static final Message FORMAT_ITEM = new MessageBuilder("format.item")
            .withDefault("{item.amount}x{item.type}")
            .withPlaceholder("item")
            .build();
}

package com.gnottero.cassiopeia.content.block;

import com.gnottero.cassiopeia.Cassiopeia;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;




public class ModBlocks {
    private ModBlocks() {}

    public static final ResourceKey<Block> BASIC_CONTROLLER_KEY = ResourceKey.create(
        Registries.BLOCK,
        Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "basic_controller")
    );

    public static final Block BASIC_CONTROLLER = registerBlock(
        "basic_controller",
        new BasicControllerBlock(BlockBehaviour.Properties.of()
            .setId(BASIC_CONTROLLER_KEY)
            .mapColor(MapColor.METAL)
            .strength(5.0F, 6.0F)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
            .noOcclusion()
        )
    );

    private static Block registerBlock(final String name, final Block block) {
        registerBlockItem(name, block);
        return Registry.register(
            BuiltInRegistries.BLOCK,
            Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, name),
            block
        );
    }

    private static Item registerBlockItem(final String name, final Block block) {
        final ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, name));
        return Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, name),
            new BlockItem(block, new Item.Properties().setId(itemKey))
        );
    }

    public static void registerModBlocks() {
        Cassiopeia.LOGGER.info("Registering ModBlocks for " + Cassiopeia.MOD_ID);
    }
}

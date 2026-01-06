package com.gnottero.cassiopeia.content;

import com.gnottero.cassiopeia.Cassiopeia;
import com.gnottero.cassiopeia.content.block.Tier1ControllerBlock;
import com.gnottero.cassiopeia.content.block.entity.Tier1ControllerBlockEntity;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class ModRegistry {

        // --- BLOCKS ---
        public static final ResourceKey<Block> TIER_1_CONTROLLER_KEY = ResourceKey.create(Registries.BLOCK,
                        Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "tier_1_controller"));

        public static final Block TIER_1_CONTROLLER_BLOCK = registerBlock("tier_1_controller",
                        new Tier1ControllerBlock(
                                        BlockBehaviour.Properties.of().setId(TIER_1_CONTROLLER_KEY)
                                                        .mapColor(MapColor.METAL).strength(5.0F, 6.0F)
                                                        .sound(SoundType.METAL).requiresCorrectToolForDrops()
                                                        .noOcclusion()));

        // --- ITEMS ---
        public static final ResourceKey<Item> TIER_1_CONTROLLER_ITEM_KEY = ResourceKey.create(Registries.ITEM,
                        Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "tier_1_controller"));

        public static final BlockItem TIER_1_CONTROLLER_ITEM = registerItem("tier_1_controller",
                        new BlockItem(TIER_1_CONTROLLER_BLOCK,
                                        new Item.Properties().setId(TIER_1_CONTROLLER_ITEM_KEY)));

        // --- BLOCK ENTITIES ---
        public static final BlockEntityType<Tier1ControllerBlockEntity> TIER_1_CONTROLLER_BLOCK_ENTITY = registerBlockEntity(
                        "tier_1_controller",
                        FabricBlockEntityTypeBuilder.create(Tier1ControllerBlockEntity::new, TIER_1_CONTROLLER_BLOCK));

        // --- MENUS ---
        // (No custom menus used for now)

        // --- CREATIVE TABS ---
        public static final CreativeModeTab CASSIOPEIA_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                        Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "general"),
                        FabricItemGroup.builder()
                                        .title(Component.literal("Cassiopeia"))
                                        .icon(() -> new ItemStack(TIER_1_CONTROLLER_ITEM))
                                        .displayItems((params, output) -> {
                                                output.accept(TIER_1_CONTROLLER_ITEM);
                                        })
                                        .build());

        public static void register() {
                // Static init
        }

        // --- HELPER METHODS ---

        private static Block registerBlock(String name, Block block) {
                Identifier id = Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, name);
                // Ensure registry key is associated?
                // The block passed in should already have the correct properties setup with
                // resource key if needed by 1.21
                return Registry.register(BuiltInRegistries.BLOCK, id, block);
        }

        private static <T extends Item> T registerItem(String name, T item) {
                Identifier id = Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, name);
                return Registry.register(BuiltInRegistries.ITEM, id, item);
        }

        private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String name,
                        FabricBlockEntityTypeBuilder<T> builder) {
                Identifier id = Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, name);
                return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, builder.build());
        }
}

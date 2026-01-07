package com.gnottero.cassiopeia.content;

import com.gnottero.cassiopeia.Cassiopeia;
import com.gnottero.cassiopeia.content.block.BasicControllerBlock;
import com.gnottero.cassiopeia.content.block.entity.BasicControllerBlockEntity;
import com.gnottero.cassiopeia.content.menu.CrusherMenu;
import com.gnottero.cassiopeia.content.menu.UnpackagerMenu;
import com.gnottero.cassiopeia.content.recipe.CrusherRecipe;
import com.gnottero.cassiopeia.content.recipe.CrusherRecipeSerializer;
import com.gnottero.cassiopeia.content.recipe.UnpackagerRecipe;
import com.gnottero.cassiopeia.content.recipe.UnpackagerRecipeSerializer;
import com.gnottero.cassiopeia.content.machine.MachineHandlerRegistry;

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
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class ModRegistry {

        // --- BLOCKS ---
        public static final ResourceKey<Block> BASIC_CONTROLLER_KEY = ResourceKey.create(Registries.BLOCK,
                        Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "basic_controller"));

        public static final Block BASIC_CONTROLLER_BLOCK = registerBlock("basic_controller",
                        new BasicControllerBlock(
                                        BlockBehaviour.Properties.of().setId(BASIC_CONTROLLER_KEY)
                                                        .mapColor(MapColor.METAL).strength(5.0F, 6.0F)
                                                        .sound(SoundType.METAL).requiresCorrectToolForDrops()
                                                        .noOcclusion()));

        // --- ITEMS ---
        public static final ResourceKey<Item> BASIC_CONTROLLER_ITEM_KEY = ResourceKey.create(Registries.ITEM,
                        Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "basic_controller"));

        public static final BlockItem BASIC_CONTROLLER_ITEM = registerItem("basic_controller",
                        new BlockItem(BASIC_CONTROLLER_BLOCK,
                                        new Item.Properties().setId(BASIC_CONTROLLER_ITEM_KEY)));

        // --- BLOCK ENTITIES ---
        public static final BlockEntityType<BasicControllerBlockEntity> BASIC_CONTROLLER_BLOCK_ENTITY = registerBlockEntity(
                        "basic_controller",
                        FabricBlockEntityTypeBuilder.create(BasicControllerBlockEntity::new, BASIC_CONTROLLER_BLOCK));

        // --- MENUS ---
        public static final MenuType<CrusherMenu> CRUSHER_MENU = Registry.register(
                        BuiltInRegistries.MENU,
                        Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "crusher"),
                        new MenuType<>(CrusherMenu::new, FeatureFlags.VANILLA_SET));

        public static final MenuType<UnpackagerMenu> UNPACKAGER_MENU = Registry.register(
                        BuiltInRegistries.MENU,
                        Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "unpackager"),
                        new MenuType<>(UnpackagerMenu::new, FeatureFlags.VANILLA_SET));

        // --- RECIPE TYPES ---
        public static final RecipeType<CrusherRecipe> CRUSHER_RECIPE_TYPE = Registry.register(
                        BuiltInRegistries.RECIPE_TYPE,
                        Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "crushing"),
                        new RecipeType<CrusherRecipe>() {
                                @Override
                                public String toString() {
                                        return "crushing";
                                }
                        });

        public static final RecipeType<UnpackagerRecipe> UNPACKAGER_RECIPE_TYPE = Registry.register(
                        BuiltInRegistries.RECIPE_TYPE,
                        Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "unpackaging"),
                        new RecipeType<UnpackagerRecipe>() {
                                @Override
                                public String toString() {
                                        return "unpackaging";
                                }
                        });

        // --- RECIPE SERIALIZERS ---
        public static final RecipeSerializer<CrusherRecipe> CRUSHER_RECIPE_SERIALIZER = Registry.register(
                        BuiltInRegistries.RECIPE_SERIALIZER,
                        Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "crushing"),
                        CrusherRecipeSerializer.INSTANCE);

        public static final RecipeSerializer<UnpackagerRecipe> UNPACKAGER_RECIPE_SERIALIZER = Registry.register(
                        BuiltInRegistries.RECIPE_SERIALIZER,
                        Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "unpackaging"),
                        UnpackagerRecipeSerializer.INSTANCE);

        // --- CREATIVE TABS ---
        public static final CreativeModeTab CASSIOPEIA_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                        Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "general"),
                        FabricItemGroup.builder()
                                        .title(Component.literal("Cassiopeia"))
                                        .icon(() -> new ItemStack(BASIC_CONTROLLER_ITEM))
                                        .displayItems((params, output) -> {
                                                output.accept(BASIC_CONTROLLER_ITEM);
                                        })
                                        .build());

        public static void register() {
                // Initialize machine handlers
                MachineHandlerRegistry.init();
        }

        // --- HELPER METHODS ---

        private static Block registerBlock(String name, Block block) {
                Identifier id = Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, name);
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

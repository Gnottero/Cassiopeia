package com.gnottero.cassiopeia.content;

import com.gnottero.cassiopeia.Cassiopeia;
import com.gnottero.cassiopeia.content.block.BasicControllerBlock;
import com.gnottero.cassiopeia.content.block.entity.BasicControllerBlockEntity;
import com.gnottero.cassiopeia.content.machine.MachineHandlerRegistry;
import com.gnottero.cassiopeia.content.menu.CrusherMenu;
import com.gnottero.cassiopeia.content.recipe.CrusherRecipe;
import com.gnottero.cassiopeia.content.recipe.CrusherRecipeSerializer;
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

        public static void register() {
                Blocks.init();
                Items.init();
                BlockEntities.init();
                Menus.init();
                RecipeTypes.init();
                RecipeSerializers.init();
                CreativeTabs.init();
                MachineHandlerRegistry.init();
                BookCategories.init();
        }

        // --- RECIPE BOOK CATEGORIES ---
        public static final class BookCategories {
                private static final RegistryHelper<net.minecraft.world.item.crafting.RecipeBookCategory> REGISTRY = new RegistryHelper<>(
                                BuiltInRegistries.RECIPE_BOOK_CATEGORY);

                public static final net.minecraft.world.item.crafting.RecipeBookCategory CRUSHER_SEARCH = REGISTRY
                                .register("crusher_search", new net.minecraft.world.item.crafting.RecipeBookCategory());
                public static final net.minecraft.world.item.crafting.RecipeBookCategory CRUSHER_BLOCKS = REGISTRY
                                .register("crusher_blocks", new net.minecraft.world.item.crafting.RecipeBookCategory());
                public static final net.minecraft.world.item.crafting.RecipeBookCategory CRUSHER_ITEMS = REGISTRY
                                .register("crusher_items", new net.minecraft.world.item.crafting.RecipeBookCategory());
                public static final net.minecraft.world.item.crafting.RecipeBookCategory CRUSHER_MISC = REGISTRY
                                .register("crusher_misc", new net.minecraft.world.item.crafting.RecipeBookCategory());

                public static void init() {
                }
        }

        // --- BLOCKS ---
        public static final class Blocks {
                private static final RegistryHelper<Block> REGISTRY = new RegistryHelper<>(BuiltInRegistries.BLOCK);

                public static final ResourceKey<Block> BASIC_CONTROLLER_KEY = ResourceKey.create(Registries.BLOCK,
                                Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "basic_controller"));

                public static final Block BASIC_CONTROLLER = REGISTRY.register("basic_controller",
                                new BasicControllerBlock(
                                                BlockBehaviour.Properties.of().setId(BASIC_CONTROLLER_KEY)
                                                                .mapColor(MapColor.METAL).strength(5.0F, 6.0F)
                                                                .sound(SoundType.METAL).requiresCorrectToolForDrops()
                                                                .noOcclusion()));

                public static void init() {
                }
        }

        // --- ITEMS ---
        public static final class Items {
                private static final RegistryHelper<Item> REGISTRY = new RegistryHelper<>(BuiltInRegistries.ITEM);

                public static final ResourceKey<Item> BASIC_CONTROLLER_ITEM_KEY = ResourceKey.create(Registries.ITEM,
                                Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "basic_controller"));

                public static final BlockItem BASIC_CONTROLLER_ITEM = REGISTRY.register("basic_controller",
                                new BlockItem(Blocks.BASIC_CONTROLLER,
                                                new Item.Properties().setId(BASIC_CONTROLLER_ITEM_KEY)));

                public static void init() {
                }
        }

        // --- BLOCK ENTITIES ---
        public static final class BlockEntities {
                private static final RegistryHelper<BlockEntityType<?>> REGISTRY = new RegistryHelper<>(
                                BuiltInRegistries.BLOCK_ENTITY_TYPE);

                public static final BlockEntityType<BasicControllerBlockEntity> BASIC_CONTROLLER = REGISTRY.register(
                                "basic_controller",
                                FabricBlockEntityTypeBuilder
                                                .create(BasicControllerBlockEntity::new, Blocks.BASIC_CONTROLLER)
                                                .build());

                public static void init() {
                }
        }

        // --- MENUS ---
        public static final class Menus {
                private static final RegistryHelper<MenuType<?>> REGISTRY = new RegistryHelper<>(
                                BuiltInRegistries.MENU);

                public static final MenuType<CrusherMenu> CRUSHER = REGISTRY.register("crusher",
                                new MenuType<>(CrusherMenu::new, FeatureFlags.VANILLA_SET));

                public static void init() {
                }
        }

        // --- RECIPE TYPES ---
        public static final class RecipeTypes {
                private static final RegistryHelper<RecipeType<?>> REGISTRY = new RegistryHelper<>(
                                BuiltInRegistries.RECIPE_TYPE);

                public static final RecipeType<CrusherRecipe> CRUSHER = REGISTRY.register("crushing",
                                new RecipeType<CrusherRecipe>() {
                                        @Override
                                        public String toString() {
                                                return "crushing";
                                        }
                                });

                public static void init() {
                }
        }

        // --- RECIPE SERIALIZERS ---
        public static final class RecipeSerializers {
                private static final RegistryHelper<RecipeSerializer<?>> REGISTRY = new RegistryHelper<>(
                                BuiltInRegistries.RECIPE_SERIALIZER);

                public static final RecipeSerializer<CrusherRecipe> CRUSHER = REGISTRY.register("crushing",
                                CrusherRecipeSerializer.INSTANCE);

                public static void init() {
                }
        }

        // --- CREATIVE TABS ---
        public static final class CreativeTabs {
                private static final RegistryHelper<CreativeModeTab> REGISTRY = new RegistryHelper<>(
                                BuiltInRegistries.CREATIVE_MODE_TAB);

                public static final CreativeModeTab CASSIOPEIA = REGISTRY.register("general",
                                FabricItemGroup.builder()
                                                .title(Component.literal("Cassiopeia"))
                                                .icon(() -> new ItemStack(Items.BASIC_CONTROLLER_ITEM))
                                                .displayItems((params, output) -> {
                                                        output.accept(Items.BASIC_CONTROLLER_ITEM);
                                                })
                                                .build());

                public static void init() {
                }
        }

        // --- OOP HELPER ---
        public static class RegistryHelper<T> {
                private final Registry<T> registry;

                public RegistryHelper(Registry<T> registry) {
                        this.registry = registry;
                }

                public <V extends T> V register(String name, V value) {
                        Identifier id = Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, name);
                        return Registry.register(registry, id, value);
                }
        }
}

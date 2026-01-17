package com.gnottero.cassiopeia.content.recipe;

import com.gnottero.cassiopeia.Cassiopeia;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;




public class ModRecipes {
    private ModRecipes() {}




    public static class Categories {
        private Categories() {}
        public static final Registry<RecipeBookCategory> registry = BuiltInRegistries.RECIPE_BOOK_CATEGORY;

        public static final RecipeBookCategory CRUSHER_SEARCH    = Registry.register(registry, Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "crusher_search"), new RecipeBookCategory());
        public static final RecipeBookCategory CRUSHER_BLOCKS    = Registry.register(registry, Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "crusher_blocks"), new RecipeBookCategory());
        public static final RecipeBookCategory CRUSHER_ITEMS     = Registry.register(registry, Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "crusher_items"), new RecipeBookCategory());
        public static final RecipeBookCategory CRUSHER_MISC      = Registry.register(registry, Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "crusher_misc"), new RecipeBookCategory());

        // Alloy Kiln categories
        public static final RecipeBookCategory ALLOY_KILN_SEARCH = Registry.register(registry, Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "alloy_kiln_search"), new RecipeBookCategory());
        public static final RecipeBookCategory ALLOY_KILN_MISC   = Registry.register(registry, Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "alloy_kiln_misc"), new RecipeBookCategory());
    }


    // Crusher recipes
    public static final RecipeType<CrusherRecipe> CRUSHER_TYPE = Registry.register(
        BuiltInRegistries.RECIPE_TYPE,
        Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "crushing"),
        new RecipeType<CrusherRecipe>() {
            @Override public String toString() {
                return "crushing";
            }
        }
    );


    public static final RecipeSerializer<CrusherRecipe> CRUSHER_SERIALIZER = Registry.register(
        BuiltInRegistries.RECIPE_SERIALIZER,
        Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "crushing"),
        CrusherRecipeSerializer.INSTANCE
    );


    // Alloying recipes
    public static final RecipeType<AlloyingRecipe> ALLOYING_TYPE = Registry.register(
        BuiltInRegistries.RECIPE_TYPE,
        Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "alloying"),
        new RecipeType<AlloyingRecipe>() {
            @Override public String toString() {
                return "alloying";
            }
        }
    );


    public static final RecipeSerializer<AlloyingRecipe> ALLOYING_SERIALIZER = Registry.register(
        BuiltInRegistries.RECIPE_SERIALIZER,
        Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "alloying"),
        AlloyingRecipeSerializer.INSTANCE
    );


    public static void registerRecipes() {
        Cassiopeia.LOGGER.info("Registering Recipes for " + Cassiopeia.MOD_ID);
        Class<?> _ = Categories.class; // Force load inner class
    }
}

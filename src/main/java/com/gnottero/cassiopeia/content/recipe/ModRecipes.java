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

        // Crusher categories
        public static final RecipeBookCategory CRUSHER_SEARCH    = Registry.register(registry, Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "crusher_search"), new RecipeBookCategory());
        public static final RecipeBookCategory CRUSHER_MISC      = Registry.register(registry, Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "crusher_misc"), new RecipeBookCategory());

        // Alloy Kiln categories
        public static final RecipeBookCategory ALLOY_KILN_SEARCH = Registry.register(registry, Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "alloy_kiln_search"), new RecipeBookCategory());
        public static final RecipeBookCategory ALLOY_KILN_MISC   = Registry.register(registry, Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "alloy_kiln_misc"), new RecipeBookCategory());
    }


    // Crushing recipes
    public static final RecipeType<CrushingRecipe> CRUSHING_TYPE = Registry.register(
        BuiltInRegistries.RECIPE_TYPE,
        Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "crushing"),
        new RecipeType<CrushingRecipe>() {
            @Override public String toString() {
                return "crushing";
            }
        }
    );


    public static final RecipeSerializer<CrushingRecipe> CRUSHING_SERIALIZER = Registry.register(
        BuiltInRegistries.RECIPE_SERIALIZER,
        Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "crushing"),
        CrushingRecipeSerializer.INSTANCE
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

        // Force load inner class
        try {
            Class.forName("com.gnottero.cassiopeia.content.recipe.ModRecipes$Categories");
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

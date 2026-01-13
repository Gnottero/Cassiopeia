package com.gnottero.cassiopeia.content.recipe;

import com.gnottero.cassiopeia.content.block.ModBlocks;
import com.gnottero.cassiopeia.content.recipe.ModRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeBookCategory;

public class CrusherRecipe extends AbstractCrushingRecipe {
    public CrusherRecipe(String group, CrushingBookCategory category, Ingredient input, ItemStack output,
            float experience, int crushingTime) {
        super(ModRecipes.CRUSHER_TYPE, group, category, input, output, experience, crushingTime);
    }

    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.BASIC_CONTROLLER);
    }

    @Override
    public RecipeSerializer<CrusherRecipe> getSerializer() {
        return ModRecipes.CRUSHER_SERIALIZER;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return switch (this.category) {
            case BLOCKS -> ModRecipes.Categories.CRUSHER_BLOCKS;
            case ITEMS -> ModRecipes.Categories.CRUSHER_ITEMS;
            case MISC -> ModRecipes.Categories.CRUSHER_MISC;
        };
    }
}

package com.gnottero.cassiopeia.content.recipe;

import com.gnottero.cassiopeia.content.ModRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeBookCategory;

public class CrusherRecipe extends AbstractCrushingRecipe {
    public CrusherRecipe(String group, CrushingBookCategory category, Ingredient input, ItemStack output,
            float experience, int crushingTime) {
        super(ModRegistry.RecipeTypes.CRUSHER, group, category, input, output, experience, crushingTime);
    }

    public ItemStack getToastSymbol() {
        return new ItemStack(ModRegistry.Blocks.BASIC_CONTROLLER);
    }

    @Override
    public RecipeSerializer<CrusherRecipe> getSerializer() {
        return ModRegistry.RecipeSerializers.CRUSHER;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return switch (this.category) {
            case BLOCKS -> ModRegistry.BookCategories.CRUSHER_BLOCKS;
            case ITEMS -> ModRegistry.BookCategories.CRUSHER_ITEMS;
            case MISC -> ModRegistry.BookCategories.CRUSHER_MISC;
        };
    }
}

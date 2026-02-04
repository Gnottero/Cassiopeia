package com.gnottero.cassiopeia.content.recipe;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public class CrushingRecipe implements Recipe<SingleRecipeInput> {
    private final String group;
    private final IngredientWithComponents input;
    private final ItemStack result;
    private final float experience;
    private final int crushingTime;

    public CrushingRecipe(
        final String group,
        final IngredientWithComponents input,
        final ItemStack result,
        final float experience,
        final int crushingTime
    ) {
        this.group = group;
        this.input = input;
        this.result = result;
        this.experience = experience;
        this.crushingTime = crushingTime;
    }


    @Override
    public boolean matches(SingleRecipeInput recipeInput, Level level) {
        return input.test(recipeInput.getItem(0));
    }

    @Override
    public ItemStack assemble(SingleRecipeInput recipeInput, Provider provider) {
        return result.copy();
    }

    @Override
    public @NotNull RecipeSerializer<CrushingRecipe> getSerializer() {
        return ModRecipes.CRUSHING_SERIALIZER;
    }

    @Override
    public @NotNull RecipeType<CrushingRecipe> getType() {
        return ModRecipes.CRUSHING_TYPE;
    }

    @Override
    public @NotNull String group() {
        return group;
    }

    @Override
    public @NotNull RecipeBookCategory recipeBookCategory() {
        return ModRecipes.Categories.CRUSHER_MISC;
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    // Getters
    public IngredientWithComponents getInput() {
        return input;
    }

    public ItemStack getResult() {
        return result;
    }

    public float getExperience() {
        return experience;
    }

    public int getCrushingTime() {
        return crushingTime;
    }
    
}

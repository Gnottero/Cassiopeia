package com.gnottero.cassiopeia.content.recipe;

import com.gnottero.cassiopeia.content.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrusherRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient ingredient;
    private final ItemStack result;
    private final int crushingTime;

    public CrusherRecipe(Ingredient ingredient, ItemStack result, int crushingTime) {
        this.ingredient = ingredient;
        this.result = result;
        this.crushingTime = crushingTime;
    }

    @Override
    public boolean matches(@NotNull SingleRecipeInput input, @NotNull Level level) {
        return this.ingredient.test(input.item());
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SingleRecipeInput input, HolderLookup.@NotNull Provider registries) {
        return this.result.copy();
    }

    @Override
    public @NotNull RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return ModRegistry.CRUSHER_RECIPE_SERIALIZER;
    }

    @Override
    public @NotNull RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return ModRegistry.CRUSHER_RECIPE_TYPE;
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        // Create placement info from this recipe's ingredient
        return PlacementInfo.create(java.util.List.of(this.ingredient));
    }

    @Nullable
    @Override
    public RecipeBookCategory recipeBookCategory() {
        // Crusher recipes don't show in the recipe book
        return null;
    }

    public Ingredient getIngredient() {
        return this.ingredient;
    }

    public ItemStack getResult() {
        return this.result;
    }

    public int getCrushingTime() {
        return this.crushingTime;
    }
}

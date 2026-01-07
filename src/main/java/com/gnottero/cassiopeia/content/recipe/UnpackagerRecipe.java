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

import java.util.List;

/**
 * Recipe for the unpackager machine.
 * Single input, single output, time-based processing.
 */
public class UnpackagerRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient ingredient;
    private final ItemStack result;
    private final int processingTime;

    public UnpackagerRecipe(Ingredient ingredient, ItemStack result, int processingTime) {
        this.ingredient = ingredient;
        this.result = result;
        this.processingTime = processingTime;
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
        return ModRegistry.UNPACKAGER_RECIPE_SERIALIZER;
    }

    @Override
    public @NotNull RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return ModRegistry.UNPACKAGER_RECIPE_TYPE;
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.create(List.of(this.ingredient));
    }

    @Nullable
    @Override
    public RecipeBookCategory recipeBookCategory() {
        return null;
    }

    public Ingredient getIngredient() {
        return this.ingredient;
    }

    public ItemStack getResult() {
        return this.result;
    }

    public int getProcessingTime() {
        return this.processingTime;
    }
}

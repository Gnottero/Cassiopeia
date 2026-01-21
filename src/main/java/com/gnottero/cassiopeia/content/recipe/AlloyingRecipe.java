package com.gnottero.cassiopeia.content.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;




/**
 * Recipe for the Alloy Kiln: 2 inputs → 1 output.
 */
public class AlloyingRecipe implements Recipe<AlloyingRecipeInput> {
    private final String group;
    private final Ingredient inputA;
    private final Ingredient inputB;
    private final ItemStack result;
    private final float experience;
    private final int alloyingTime;


    public AlloyingRecipe(
        final String group,
        final Ingredient inputA,
        final Ingredient inputB,
        final ItemStack result,
        final float experience,
        final int alloyingTime
    ) {
        this.group = group;
        this.inputA = inputA;
        this.inputB = inputB;
        this.result = result;
        this.experience = experience;
        this.alloyingTime = alloyingTime;
    }




    @Override
    public boolean matches(final AlloyingRecipeInput input, final Level level) {

        // Check if inputs match in either order
        final boolean matchAB = inputA.test(input.getInputA()) && inputB.test(input.getInputB());
        final boolean matchBA = inputA.test(input.getInputB()) && inputB.test(input.getInputA());
        return matchAB || matchBA;
    }

    @Override
    public @NotNull ItemStack assemble(final AlloyingRecipeInput input, final HolderLookup.Provider provider) {
        return result.copy();
    }

    @Override
    public @NotNull RecipeSerializer<AlloyingRecipe> getSerializer() {
        return ModRecipes.ALLOYING_SERIALIZER;
    }

    @Override
    public @NotNull RecipeType<AlloyingRecipe> getType() {
        return ModRecipes.ALLOYING_TYPE;
    }

    @Override
    public @NotNull String group() {
        return group;
    }

    @Override
    public @NotNull RecipeBookCategory recipeBookCategory() {
        return ModRecipes.Categories.ALLOY_KILN_MISC;
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
    public Ingredient getInputA() {
        return inputA;
    }

    public Ingredient getInputB() {
        return inputB;
    }

    public ItemStack getResult() {
        return result;
    }

    public float getExperience() {
        return experience;
    }

    public int getAlloyingTime() {
        return alloyingTime;
    }

    public NonNullList<Ingredient> placementIngredients() {
        final NonNullList<Ingredient> list = NonNullList.create();
        list.add(inputA);
        list.add(inputB);
        return list;
    }
}

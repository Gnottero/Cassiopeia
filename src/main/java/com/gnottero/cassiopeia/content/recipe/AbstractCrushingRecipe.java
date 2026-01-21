package com.gnottero.cassiopeia.content.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.PlacementInfo;
import org.jetbrains.annotations.NotNull;




public abstract class AbstractCrushingRecipe implements Recipe<SingleRecipeInput> {
    protected final RecipeType<? extends AbstractCrushingRecipe> type;
    protected final CrushingBookCategory category;
    protected final String group;
    protected final Ingredient input;
    protected final ItemStack output;
    protected final float experience;
    protected final int crushingTime;


    protected AbstractCrushingRecipe(
        final RecipeType<? extends AbstractCrushingRecipe> type,
        final String group,
        final CrushingBookCategory category,
        final Ingredient input,
        final ItemStack output,
        final float experience,
        final int crushingTime
    ) {
        this.type = type;
        this.category = category;
        this.group = group;
        this.input = input;
        this.output = output;
        this.experience = experience;
        this.crushingTime = crushingTime;
    }




    @Override
    public boolean matches(final SingleRecipeInput input, final Level level) {
        return this.input.test(input.item());
    }

    @Override
    public @NotNull ItemStack assemble(final SingleRecipeInput input,
            final HolderLookup.Provider provider) {
        return this.output.copy();
    }

    @SuppressWarnings("java:S1172") // Unused parameters
    public boolean canCraftInDimensions(final int width, final int height) {
        return true;
    }

    @Override
    public String group() {
        return this.group;
    }

    public CrushingBookCategory category() {
        return this.category;
    }

    public Ingredient getIngredient() {
        return this.input;
    }

    public ItemStack getResult() {
        return this.output;
    }

    public float getExperience() {
        return this.experience;
    }

    public int getCrushingTime() {
        return this.crushingTime;
    }

    public NonNullList<Ingredient> getIngredients() {
        final NonNullList<Ingredient> nonNullList = NonNullList.create();
        nonNullList.add(this.input);
        return nonNullList;
    }

    @SuppressWarnings("java:S1172") // Unused parameters
    public ItemStack getResultItem(final HolderLookup.Provider provider) {
        return this.output;
    }

    @Override
    public RecipeType<? extends AbstractCrushingRecipe> getType() {
        return this.type;
    }

    public Ingredient getInput() {
        return this.input;
    }

    public ItemStack getOutput() {
        return this.output;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    // recipeBookCategory is left for subclasses to implement
}

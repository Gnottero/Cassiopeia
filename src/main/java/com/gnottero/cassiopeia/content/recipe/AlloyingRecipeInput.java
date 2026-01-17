package com.gnottero.cassiopeia.content.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;




/**
 * Recipe input for Alloy Kiln: holds 2 input ItemStacks.
 */
public record AlloyingRecipeInput(ItemStack inputA, ItemStack inputB) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> inputA;
            case 1 -> inputB;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public int size() {
        return 2;
    }

    public ItemStack getInputA() {
        return inputA;
    }

    public ItemStack getInputB() {
        return inputB;
    }
}

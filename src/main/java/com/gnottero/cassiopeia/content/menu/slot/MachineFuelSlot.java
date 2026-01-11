package com.gnottero.cassiopeia.content.menu.slot;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A slot for fuel items in machines.
 * Uses level's fuel values when available, with fallback for common fuels.
 */
public class MachineFuelSlot extends Slot {

    @Nullable
    private final Level level;

    public MachineFuelSlot(Container container, int slot, int x, int y, @Nullable Level level) {
        super(container, slot, x, y);
        this.level = level;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return isFuel(stack, level);
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        // Lava buckets can only stack to 1
        return stack.is(Items.LAVA_BUCKET) ? 1 : super.getMaxStackSize(stack);
    }

    /**
     * Check if an item is a valid fuel source.
     * Uses level's fuel values when available, otherwise uses fallback check.
     */
    public static boolean isFuel(ItemStack stack, @Nullable Level level) {
        if (stack.isEmpty())
            return false;

        // Try to use level's fuel values if available
        if (level != null) {
            try {
                return level.fuelValues().isFuel(stack);
            } catch (Exception e) {
                // Fallback if method doesn't exist or fails
            }
        }

        // Fallback to common fuels check
        return isFuelFallback(stack);
    }

    /**
     * Fallback fuel check for when level fuel values are not available.
     */
    private static boolean isFuelFallback(ItemStack stack) {
        if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL))
            return true;
        if (stack.is(Items.COAL_BLOCK))
            return true;
        if (stack.is(Items.BLAZE_ROD))
            return true;
        if (stack.is(Items.LAVA_BUCKET))
            return true;
        if (stack.is(Items.STICK))
            return true;
        if (stack.is(ItemTags.LOGS_THAT_BURN))
            return true;
        if (stack.is(ItemTags.PLANKS))
            return true;
        return false;
    }
}

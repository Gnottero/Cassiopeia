package com.gnottero.cassiopeia.content.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;




/**
 * A slot for fuel items in machines.
 * Uses level's fuel values when available.
 */
public class MachineFuelSlot extends Slot {
    private final Level level;

    public MachineFuelSlot(Level level, Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.level = level;
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        if(level != null) {
            return level.fuelValues().burnDuration(itemStack) > 0 || isBucket(itemStack);
        }
        return isBucket(itemStack);
    }

    @Override
    public int getMaxStackSize(ItemStack itemStack) {
        return isBucket(itemStack) ? 1 : super.getMaxStackSize(itemStack);
    }

    public static boolean isBucket(ItemStack itemStack) {
        return itemStack.is(Items.BUCKET);
    }
}

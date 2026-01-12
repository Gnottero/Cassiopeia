package com.gnottero.cassiopeia.content.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.gnottero.cassiopeia.content.menu.AbstractCrushingMenu;

/**
 * A slot for fuel items in machines.
 * Uses level's fuel values when available, with fallback for common fuels.
 */
public class MachineFuelSlot extends Slot {
    private final AbstractCrushingMenu menu;

    public MachineFuelSlot(AbstractCrushingMenu abstractMachineMenu, Container container, int i, int j, int k) {
        super(container, i, j, k);
        this.menu = abstractMachineMenu;
    }

    public boolean mayPlace(ItemStack itemStack) {
        return this.menu.isFuel(itemStack) || isBucket(itemStack);
    }

    public int getMaxStackSize(ItemStack itemStack) {
        return isBucket(itemStack) ? 1 : super.getMaxStackSize(itemStack);
    }

    public static boolean isBucket(ItemStack itemStack) {
        return itemStack.is(Items.BUCKET);
    }
}

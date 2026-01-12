package com.gnottero.cassiopeia.content.menu;

import com.gnottero.cassiopeia.content.ModRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Menu for the Crusher machine.
 * 1 input slot, 1 fuel slot, 1 output slot.
 */
public class CrusherMenu extends AbstractCrushingMenu {

    public CrusherMenu(int i, Inventory inventory) {
        super(ModRegistry.Menus.CRUSHER, ModRegistry.RecipeTypes.CRUSHER, RecipePropertySet.FURNACE_INPUT,
                RecipeBookType.FURNACE, i, inventory);
    }

    public CrusherMenu(int i, Inventory inventory, Container container, ContainerData containerData) {
        super(ModRegistry.Menus.CRUSHER, ModRegistry.RecipeTypes.CRUSHER, RecipePropertySet.FURNACE_INPUT,
                RecipeBookType.FURNACE, i, inventory, container, containerData);
    }
}

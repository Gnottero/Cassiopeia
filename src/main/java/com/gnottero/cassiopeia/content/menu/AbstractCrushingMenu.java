package com.gnottero.cassiopeia.content.menu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractCrushingMenu extends AbstractContainerMenu {
    protected final Level level;

    protected AbstractCrushingMenu(@Nullable MenuType<?> menuType, int containerId, Player player) {
        super(menuType, containerId);
        this.level = player.level();
    }

    public abstract boolean isFuel(ItemStack stack);
}

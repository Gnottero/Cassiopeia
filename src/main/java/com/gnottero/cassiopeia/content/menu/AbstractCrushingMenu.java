package com.gnottero.cassiopeia.content.menu;

import com.gnottero.cassiopeia.content.menu.slot.MachineFuelSlot;
import com.gnottero.cassiopeia.content.menu.slot.MachineResultSlot;
import com.gnottero.cassiopeia.content.recipe.AbstractCrushingRecipe;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Base class for all machine menus.
 * Provides configurable slot layout, player inventory handling, and item
 * transfer logic.
 */
public abstract class AbstractCrushingMenu extends RecipeBookMenu {
    public static final int INGREDIENT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    public static final int SLOT_COUNT = 3;
    public static final int DATA_COUNT = 4;

    final Container container;
    private final ContainerData data;
    protected final Level level;
    private final RecipeType<? extends AbstractCrushingRecipe> recipeType;
    private final RecipePropertySet acceptedInputs;
    private final RecipeBookType recipeBookType;

    protected AbstractCrushingMenu(MenuType<?> menuType, RecipeType<? extends AbstractCrushingRecipe> recipeType,
            ResourceKey<RecipePropertySet> resourceKey, RecipeBookType recipeBookType, int i, Inventory inventory) {
        this(menuType, recipeType, resourceKey, recipeBookType, i, inventory, new SimpleContainer(SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT));
    }

    protected AbstractCrushingMenu(MenuType<?> menuType, RecipeType<? extends AbstractCrushingRecipe> recipeType,
            ResourceKey<RecipePropertySet> resourceKey, RecipeBookType recipeBookType, int i, Inventory inventory,
            Container container, ContainerData containerData) {
        super(menuType, i);
        this.recipeType = recipeType;
        this.recipeBookType = recipeBookType;
        checkContainerSize(container, SLOT_COUNT);
        checkContainerDataCount(containerData, DATA_COUNT);
        this.container = container;
        this.data = containerData;
        this.level = inventory.player.level();
        this.acceptedInputs = this.level.recipeAccess().propertySet(resourceKey);
        this.addSlot(new Slot(container, 0, 56, 17));
        this.addSlot(new MachineFuelSlot(this, container, 1, 56, 53));
        this.addSlot(new MachineResultSlot(inventory.player, container, 2, 116, 35));
        this.addStandardInventorySlots(inventory, 8, 84);
        this.addDataSlots(containerData);
    }

    public boolean isFuel(ItemStack itemStack) {
        return this.level.fuelValues().isFuel(itemStack);
    }

    public void fillCraftSlotsStackedContents(StackedItemContents stackedItemContents) {
        if (this.container instanceof StackedContentsCompatible) {
            ((StackedContentsCompatible) this.container).fillStackedContents(stackedItemContents);
        }
    }

    public Slot getResultSlot() {
        return (Slot) this.slots.get(2);
    }

    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot) this.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (i == 2) {
                if (!this.moveItemStackTo(itemStack2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemStack2, itemStack);
            } else if (i != 1 && i != 0) {
                if (this.canCrush(itemStack2)) {
                    if (!this.moveItemStackTo(itemStack2, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (this.isFuel(itemStack2)) {
                    if (!this.moveItemStackTo(itemStack2, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (i >= 3 && i < 30) {
                    if (!this.moveItemStackTo(itemStack2, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (i >= 30 && i < 39 && !this.moveItemStackTo(itemStack2, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack2, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemStack2);
        }

        return itemStack;
    }

    public boolean canCrush(ItemStack itemStack) {
        return this.acceptedInputs.test(itemStack);
    }

    public float getBurnProgress() {
        int i = this.data.get(2);
        int j = this.data.get(3);
        return j != 0 && i != 0 ? Mth.clamp((float) i / (float) j, 0.0F, 1.0F) : 0.0F;
    }

    public float getLitProgress() {
        int i = this.data.get(1);
        if (i == 0) {
            i = 200;
        }

        return Mth.clamp((float) this.data.get(0) / (float) i, 0.0F, 1.0F);
    }

    public boolean isLit() {
        return this.data.get(0) > 0;
    }

    public RecipeBookType getRecipeBookType() {
        return this.recipeBookType;
    }

    public RecipeBookMenu.PostPlaceAction handlePlacement(boolean bl, boolean bl2, RecipeHolder<?> recipeHolder,
            ServerLevel serverLevel, Inventory inventory) {
        List<Slot> list = List.of(this.getSlot(0), this.getSlot(2));
        return ServerPlaceRecipe.placeRecipe(new ServerPlaceRecipe.CraftingMenuAccess<AbstractCrushingRecipe>() {
            @Override
            public void fillCraftSlotsStackedContents(StackedItemContents stackedItemContents) {
                AbstractCrushingMenu.this.fillCraftSlotsStackedContents(stackedItemContents);
            }

            @Override
            public void clearCraftingContent() {
                AbstractCrushingMenu.this.getSlot(0).set(ItemStack.EMPTY);
                AbstractCrushingMenu.this.getSlot(2).set(ItemStack.EMPTY);
            }

            @Override
            public boolean recipeMatches(RecipeHolder<AbstractCrushingRecipe> recipeHolder) {
                return recipeHolder.value().matches(
                        new SingleRecipeInput(AbstractCrushingMenu.this.container.getItem(0)),
                        serverLevel);
            }
        }, 1, 1, List.of(this.getSlot(0)), list, inventory, (RecipeHolder<AbstractCrushingRecipe>) recipeHolder, bl,
                bl2);
    }
}

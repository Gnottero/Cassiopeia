package com.gnottero.cassiopeia.content.menu;

import com.gnottero.cassiopeia.content.ModRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CrusherMenu extends AbstractContainerMenu {

    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int SLOT_COUNT = 3;

    public static final int DATA_BURN_TIME = 0;
    public static final int DATA_BURN_TIME_TOTAL = 1;
    public static final int DATA_CRUSH_PROGRESS = 2;
    public static final int DATA_CRUSH_TIME_TOTAL = 3;
    public static final int DATA_COUNT = 4;

    private final Container container;
    private final ContainerData data;

    // Client constructor
    public CrusherMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(DATA_COUNT));
    }

    // Server constructor
    public CrusherMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModRegistry.CRUSHER_MENU, containerId);
        checkContainerSize(container, SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);
        this.container = container;
        this.data = data;

        // Add crusher slots
        this.addSlot(new Slot(container, INPUT_SLOT, 56, 17)); // Input
        this.addSlot(new FuelSlot(container, FUEL_SLOT, 56, 53)); // Fuel
        this.addSlot(new OutputSlot(container, OUTPUT_SLOT, 116, 35)); // Output

        // Add player inventory (3 rows of 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Add player hotbar (1 row of 9)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        this.addDataSlots(data);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();

            if (index == OUTPUT_SLOT) {
                // Moving from output to player inventory
                if (!this.moveItemStackTo(slotStack, SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= SLOT_COUNT) {
                // Moving from player inventory to crusher
                if (isFuel(slotStack)) {
                    // Try fuel slot first
                    if (!this.moveItemStackTo(slotStack, FUEL_SLOT, FUEL_SLOT + 1, false)) {
                        // Try input slot
                        if (!this.moveItemStackTo(slotStack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                } else {
                    // Try input slot
                    if (!this.moveItemStackTo(slotStack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo(slotStack, SLOT_COUNT, this.slots.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.container.stillValid(player);
    }

    public float getBurnProgress() {
        int burnTime = this.data.get(DATA_BURN_TIME);
        int burnTimeTotal = this.data.get(DATA_BURN_TIME_TOTAL);
        return burnTimeTotal != 0 && burnTime != 0 ? (float) burnTime / (float) burnTimeTotal : 0.0F;
    }

    public float getCrushProgress() {
        int crushProgress = this.data.get(DATA_CRUSH_PROGRESS);
        int crushTimeTotal = this.data.get(DATA_CRUSH_TIME_TOTAL);
        return crushTimeTotal != 0 && crushProgress != 0 ? (float) crushProgress / (float) crushTimeTotal : 0.0F;
    }

    public boolean isBurning() {
        return this.data.get(DATA_BURN_TIME) > 0;
    }

    private static boolean isFuel(ItemStack stack) {
        // In 1.21+, check if item has burn time via stack property
        // A simple way is to check if the item is in the fuel tag
        return stack.is(net.minecraft.tags.ItemTags.COALS) ||
                stack.is(net.minecraft.world.item.Items.COAL) ||
                stack.is(net.minecraft.world.item.Items.CHARCOAL) ||
                stack.is(net.minecraft.world.item.Items.COAL_BLOCK) ||
                stack.is(net.minecraft.world.item.Items.BLAZE_ROD) ||
                stack.is(net.minecraft.world.item.Items.LAVA_BUCKET) ||
                stack.is(net.minecraft.tags.ItemTags.LOGS_THAT_BURN) ||
                stack.is(net.minecraft.tags.ItemTags.PLANKS);
    }

    // Custom slot for fuel that only accepts burnable items
    private static class FuelSlot extends Slot {
        public FuelSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return isFuel(stack);
        }
    }

    // Custom slot for output that doesn't accept items
    private static class OutputSlot extends Slot {
        public OutputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return false;
        }
    }
}

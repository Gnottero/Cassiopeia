package com.gnottero.cassiopeia.content.menu;

import com.gnottero.cassiopeia.content.ModRegistry;
import com.gnottero.cassiopeia.content.machine.UnpackagerMachineHandler;
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

/**
 * Menu for the unpackager machine.
 * 2 slots: input, output.
 */
public class UnpackagerMenu extends AbstractContainerMenu {

    public static final int DATA_PROGRESS = 0;
    public static final int DATA_TOTAL_TIME = 1;
    public static final int DATA_COUNT = 2;

    private final Container container;
    private final ContainerData data;

    // Client constructor
    public UnpackagerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(UnpackagerMachineHandler.SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT));
    }

    // Server constructor
    public UnpackagerMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModRegistry.UNPACKAGER_MENU, containerId);
        checkContainerSize(container, UnpackagerMachineHandler.SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);
        this.container = container;
        this.data = data;

        // Input slot
        this.addSlot(new Slot(container, UnpackagerMachineHandler.INPUT_SLOT, 56, 35));

        // Output slot
        this.addSlot(new OutputSlot(container, UnpackagerMachineHandler.OUTPUT_SLOT, 116, 35));

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        this.addDataSlots(data);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            if (index == UnpackagerMachineHandler.OUTPUT_SLOT) {
                // From output to inventory
                if (!this.moveItemStackTo(slotStack, UnpackagerMachineHandler.SLOT_COUNT,
                        UnpackagerMachineHandler.SLOT_COUNT + 36, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, result);
            } else if (index == UnpackagerMachineHandler.INPUT_SLOT) {
                // From input to inventory
                if (!this.moveItemStackTo(slotStack, UnpackagerMachineHandler.SLOT_COUNT,
                        UnpackagerMachineHandler.SLOT_COUNT + 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From inventory to input
                if (!this.moveItemStackTo(slotStack, UnpackagerMachineHandler.INPUT_SLOT,
                        UnpackagerMachineHandler.INPUT_SLOT + 1, false)) {
                    // Move between inventory and hotbar
                    if (index < UnpackagerMachineHandler.SLOT_COUNT + 27) {
                        if (!this.moveItemStackTo(slotStack, UnpackagerMachineHandler.SLOT_COUNT + 27,
                                UnpackagerMachineHandler.SLOT_COUNT + 36, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.moveItemStackTo(slotStack, UnpackagerMachineHandler.SLOT_COUNT,
                                UnpackagerMachineHandler.SLOT_COUNT + 27, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return result;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.container.stillValid(player);
    }

    public int getProgress() {
        return this.data.get(DATA_PROGRESS);
    }

    public int getTotalTime() {
        return this.data.get(DATA_TOTAL_TIME);
    }

    public int getProgressScaled(int pixels) {
        int progress = this.data.get(DATA_PROGRESS);
        int total = this.data.get(DATA_TOTAL_TIME);
        return total != 0 ? progress * pixels / total : 0;
    }

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

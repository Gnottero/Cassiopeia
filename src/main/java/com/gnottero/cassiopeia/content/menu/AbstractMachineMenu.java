package com.gnottero.cassiopeia.content.menu;

import com.gnottero.cassiopeia.content.menu.slot.MachineFuelSlot;
import com.gnottero.cassiopeia.content.menu.slot.MachineResultSlot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for all machine menus.
 * Provides configurable slot layout, player inventory handling, and item
 * transfer logic.
 */
public abstract class AbstractMachineMenu extends AbstractContainerMenu {

    public static final int PLAYER_INVENTORY_SIZE = 36; // 27 inventory + 9 hotbar

    protected final Container container;
    protected final ContainerData data;
    protected final MachineSlotConfig slotConfig;
    @Nullable
    protected final Level level;

    /**
     * Server-side constructor.
     */
    protected AbstractMachineMenu(
            @Nullable MenuType<?> menuType,
            int containerId,
            Inventory playerInventory,
            Container container,
            ContainerData data,
            MachineSlotConfig slotConfig,
            int dataCount) {
        super(menuType, containerId);

        checkContainerSize(container, slotConfig.totalSlotCount());
        checkContainerDataCount(data, dataCount);

        this.container = container;
        this.data = data;
        this.slotConfig = slotConfig;
        this.level = playerInventory.player.level();

        // Add machine slots based on configuration
        addMachineSlots(playerInventory.player);

        // Add player inventory and hotbar
        addPlayerInventorySlots(playerInventory);
        addPlayerHotbarSlots(playerInventory);

        // Register data slots for sync
        this.addDataSlots(data);
    }

    /**
     * Add machine slots based on the slot configuration.
     * Can be overridden for custom slot types.
     */
    protected void addMachineSlots(Player player) {
        // Add input slots
        for (int i = 0; i < slotConfig.inputSlotCount(); i++) {
            MachineSlotConfig.SlotPosition pos = slotConfig.inputPositions()[i];
            this.addSlot(createInputSlot(container, slotConfig.inputSlotStart() + i, pos.x(), pos.y()));
        }

        // Add fuel slots
        for (int i = 0; i < slotConfig.fuelSlotCount(); i++) {
            MachineSlotConfig.SlotPosition pos = slotConfig.fuelPositions()[i];
            this.addSlot(createFuelSlot(container, slotConfig.fuelSlotStart() + i, pos.x(), pos.y()));
        }

        // Add output slots
        for (int i = 0; i < slotConfig.outputSlotCount(); i++) {
            MachineSlotConfig.SlotPosition pos = slotConfig.outputPositions()[i];
            this.addSlot(createOutputSlot(player, container, slotConfig.outputSlotStart() + i, pos.x(), pos.y()));
        }
    }

    /**
     * Create an input slot. Override for custom input slot behavior.
     */
    protected Slot createInputSlot(Container container, int index, int x, int y) {
        return new Slot(container, index, x, y);
    }

    /**
     * Create a fuel slot. Override for custom fuel slot behavior.
     */
    protected Slot createFuelSlot(Container container, int index, int x, int y) {
        return new MachineFuelSlot(container, index, x, y, level);
    }

    /**
     * Create an output slot. Override for custom output slot behavior.
     */
    protected Slot createOutputSlot(Player player, Container container, int index, int x, int y) {
        return new MachineResultSlot(player, container, index, x, y);
    }

    /**
     * Get the starting slot index for player inventory.
     */
    protected int getPlayerInventoryStart() {
        return slotConfig.totalSlotCount();
    }

    /**
     * Get the ending slot index for player inventory (exclusive).
     */
    protected int getPlayerInventoryEnd() {
        return slotConfig.totalSlotCount() + 27;
    }

    /**
     * Get the starting slot index for player hotbar.
     */
    protected int getPlayerHotbarStart() {
        return slotConfig.totalSlotCount() + 27;
    }

    /**
     * Get the ending slot index for player hotbar (exclusive).
     */
    protected int getPlayerHotbarEnd() {
        return slotConfig.totalSlotCount() + PLAYER_INVENTORY_SIZE;
    }

    /**
     * Add player inventory slots (3 rows of 9).
     */
    protected void addPlayerInventorySlots(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9 + 9;
                int x = 8 + col * 18;
                int y = 84 + row * 18;
                this.addSlot(new Slot(playerInventory, slotIndex, x, y));
            }
        }
    }

    /**
     * Add player hotbar slots (1 row of 9).
     */
    protected void addPlayerHotbarSlots(Inventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            int x = 8 + col * 18;
            int y = 142;
            this.addSlot(new Slot(playerInventory, col, x, y));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            int machineSlotCount = slotConfig.totalSlotCount();

            if (index < machineSlotCount) {
                // Moving from machine to player inventory
                if (!this.moveItemStackTo(slotStack, getPlayerInventoryStart(), getPlayerHotbarEnd(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, result);
            } else {
                // Moving from player inventory to machine
                if (!moveToMachineSlots(slotStack)) {
                    // If can't go to machine, move between inventory and hotbar
                    if (index < getPlayerInventoryEnd()) {
                        if (!this.moveItemStackTo(slotStack, getPlayerHotbarStart(), getPlayerHotbarEnd(), false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.moveItemStackTo(slotStack, getPlayerInventoryStart(), getPlayerInventoryEnd(),
                                false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
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

    /**
     * Move item stack from player inventory to appropriate machine slots.
     * Default implementation: fuel slots first (if fuel), then input slots.
     */
    protected boolean moveToMachineSlots(ItemStack stack) {
        // Try fuel slots first for fuel items
        if (slotConfig.hasFuelSlots() && isFuel(stack)) {
            int fuelStart = slotConfig.fuelSlotStart();
            int fuelEnd = fuelStart + slotConfig.fuelSlotCount();
            if (this.moveItemStackTo(stack, fuelStart, fuelEnd, false)) {
                return true;
            }
        }

        // Try input slots
        int inputStart = slotConfig.inputSlotStart();
        int inputEnd = inputStart + slotConfig.inputSlotCount();
        return this.moveItemStackTo(stack, inputStart, inputEnd, false);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.container.stillValid(player);
    }

    /**
     * Check if a stack is valid fuel.
     */
    protected boolean isFuel(ItemStack stack) {
        return MachineFuelSlot.isFuel(stack, level);
    }

    /**
     * Get a data value by index.
     */
    protected int getData(int index) {
        return this.data.get(index);
    }

    /**
     * Get the container for this menu.
     */
    public Container getContainer() {
        return this.container;
    }

    /**
     * Get the slot configuration.
     */
    public MachineSlotConfig getSlotConfig() {
        return this.slotConfig;
    }
}

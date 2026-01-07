package com.gnottero.cassiopeia.content.machine;

import com.gnottero.cassiopeia.content.block.entity.BasicControllerBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for machine behavior handlers.
 */
public interface MachineHandler {

    /**
     * @return The structure_id this handler is for
     */
    String getStructureId();

    /**
     * @return Number of inventory slots for this machine
     */
    int getSlotCount();

    /**
     * @return Number of data slots for ContainerData synchronization
     */
    int getDataCount();

    /**
     * Get accessible slots for hopper/pipe interaction from a given face.
     */
    int[] getSlotsForFace(Direction side);

    /**
     * Check if an item can be placed in a slot.
     */
    boolean canPlaceItem(int slot, ItemStack stack);

    /**
     * Check if an item can be extracted from a slot.
     */
    boolean canTakeItem(int slot, ItemStack stack, Direction direction);

    /**
     * Server-side tick processing.
     */
    void tick(Level level, BlockPos pos, BlockState state, BasicControllerBlockEntity be);

    /**
     * Save machine-specific data.
     */
    void saveAdditional(ValueOutput output, BasicControllerBlockEntity be);

    /**
     * Load machine-specific data.
     */
    void loadAdditional(ValueInput input, BasicControllerBlockEntity be);

    /**
     * @return Display name for the container GUI
     */
    @NotNull
    Component getDisplayName();

    /**
     * Create the menu/screen handler for this machine.
     */
    @Nullable
    AbstractContainerMenu createMenu(int containerId, Inventory playerInventory,
            BasicControllerBlockEntity be, ContainerData data);

    /**
     * Get container data value at index.
     */
    int getDataValue(BasicControllerBlockEntity be, int index);

    /**
     * Set container data value at index.
     */
    void setDataValue(BasicControllerBlockEntity be, int index, int value);
}

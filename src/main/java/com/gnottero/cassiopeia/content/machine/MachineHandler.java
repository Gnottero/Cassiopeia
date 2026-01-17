package com.gnottero.cassiopeia.content.machine;

import com.gnottero.cassiopeia.content.block.entity.BasicControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;




/**
 * Interface for machine behavior handlers.
 * Follows the vanilla AbstractFurnaceBlockEntity pattern.
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
     * @return The slot index for the primary input slot
     */
    int getInputSlotIndex();

    /**
     * @return All input slot indices (for multi-input machines)
     */
    default int[] getInputSlotIndices() {
        return new int[] { getInputSlotIndex() };
    }

    /**
     * @return The data index for cooking/processing progress
     */
    int getProcessProgressIndex();

    /**
     * Get accessible slots for hopper/pipe interaction from a given face.
     */
    int[] getSlotsForFace(Direction side);

    /**
     * Check if an item can be placed in a slot.
     * @param be    The block entity (provides access to level for fuel registry)
     * @param slot  The slot index
     * @param stack The item to place
     * @return true if the item can be placed
     */
    boolean canPlaceItem(BasicControllerBlockEntity be, int slot, ItemStack stack);

    /**
     * Check if an item can be extracted from a slot.
     * @param slot  The slot index.
     * @param stack The item to test.
     * @param direction The face to extract from.
     */
    boolean canTakeItem(int slot, ItemStack stack, Direction direction);

    /**
     * Server-side tick processing. Called every tick on the server.
     * Follows vanilla AbstractFurnaceBlockEntity.serverTick pattern.
     */
    void serverTick(Level level, BlockPos pos, BlockState state, BasicControllerBlockEntity be);

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
    @NotNull Component getDisplayName();

    /**
     * Create the menu/screen handler for this machine.
     */
    @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, BasicControllerBlockEntity be, ContainerData data);

    /**
     * Get container data value at index.
     */
    int getDataValue(BasicControllerBlockEntity be, int index);

    /**
     * Set container data value at index.
     */
    void setDataValue(BasicControllerBlockEntity be, int index, int value);
}

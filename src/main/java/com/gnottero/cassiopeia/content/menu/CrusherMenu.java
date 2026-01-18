package com.gnottero.cassiopeia.content.menu;

import com.gnottero.cassiopeia.content.block.entity.BasicControllerBlockEntity;
import com.gnottero.cassiopeia.content.machine.CrusherMachineHandler;
import com.gnottero.cassiopeia.content.menu.slot.MachineFuelSlot;
import com.gnottero.cassiopeia.content.menu.slot.MachineResultSlot;
import com.gnottero.cassiopeia.content.screen.ModScreenHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;




public class CrusherMenu extends AbstractCrushingMenu {
    public final BasicControllerBlockEntity blockEntity;
    private final ContainerData data;




    // Client Constructor - receives BlockPos from ExtendedScreenHandlerType
    public CrusherMenu(int containerId, Inventory inv, BlockPos pos) {
        this(containerId, inv, inv.player.level().getBlockEntity(pos), new SimpleContainerData(CrusherMachineHandler.DATA_COUNT));
    }

    // Server Constructor / Internal
    public CrusherMenu(int containerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModScreenHandlers.CRUSHER, containerId, inv.player);
        this.blockEntity = (BasicControllerBlockEntity) entity;
        this.data = data;

        // Check container size on blockEntity, not player inventory
        checkContainerSize(this.blockEntity, CrusherMachineHandler.SLOT_COUNT);
        checkContainerDataCount(data, CrusherMachineHandler.DATA_COUNT);

        // Add Machine Slots FIRST
        this.addSlot(new              Slot(this.blockEntity,             CrusherMachineHandler.INPUT_SLOT,   56, 17));
        this.addSlot(new   MachineFuelSlot(this.level, this.blockEntity, CrusherMachineHandler.FUEL_SLOT,    56, 53));
        this.addSlot(new MachineResultSlot(inv.player, this.blockEntity, CrusherMachineHandler.OUTPUT_SLOT, 116, 35));

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        addDataSlots(data);
    }




    public boolean isFuel(ItemStack stack) {
        if (level != null) {
            return CrusherMachineHandler.getBurnTime(level.fuelValues(), stack) > 0;
        }
        return false;
    }

    public boolean isCrafting() {
        return data.get(2) > 0; // DATA_COOKING_PROGRESS
    }

    public boolean isBurning() {
        return data.get(0) > 0; // DATA_LIT_TIME
    }

    public boolean hasFuel() {
        return data.get(0) > 0; // DATA_LIT_TIME
    }

    // Returns burn progress as a float 0-1
    public float getBurnProgress() {
        int litTime     = data.get(0);
        int litDuration = data.get(1);
        return litDuration != 0 ? (float) litTime / (float) litDuration : 0;
    }

    // Returns crush progress as a float 0-1
    public float getCrushProgress() {
        int progress     = data.get(2);
        int maxProgress = data.get(3);
        return maxProgress != 0 ? (float) progress / (float) maxProgress : 0;
    }

    public int getScaledProgress() {
        int progress     = data.get(2);
        int maxProgress = data.get(3); // DATA_COOKING_TOTAL_TIME
        int progressArrowSize = 24; // Arrow width in pixels

        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
    }

    public int getScaledFuelProgress() {
        int litTime     = data.get(0);
        int litDuration = data.get(1); // DATA_LIT_DURATION
        int burnProgressSize = 14;

        return litDuration != 0 && litTime != 0 ? litTime * burnProgressSize / litDuration : 0;
    }




    // QuickMoveStack logic depends on indexes.
    // Order added: 3 Machine Slots (0-2), then 27 Player Inventory (3-29), then 9
    // Hotbar (30-38).

    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_SLOT_COUNT = 3;
    private static final int VANILLA_FIRST_SLOT_INDEX = TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_SLOT_COUNT = 36;

    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (pIndex < VANILLA_FIRST_SLOT_INDEX) {
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, true)) {
                return ItemStack.EMPTY;
            }
        }
        else if (pIndex >= VANILLA_FIRST_SLOT_INDEX && pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        }
        else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }




    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}

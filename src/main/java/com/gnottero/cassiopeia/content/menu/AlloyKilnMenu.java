package com.gnottero.cassiopeia.content.menu;

import com.gnottero.cassiopeia.content.block.entity.BasicControllerBlockEntity;
import com.gnottero.cassiopeia.content.machine.AlloyKilnMachineHandler;
import com.gnottero.cassiopeia.content.menu.slot.MachineFuelSlot;
import com.gnottero.cassiopeia.content.menu.slot.MachineResultSlot;
import com.gnottero.cassiopeia.content.screen.ModScreenHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;




/**
 * Menu for the Alloy Kiln: 2 inputs + fuel → 1 output.
 */
public class AlloyKilnMenu extends AbstractContainerMenu {
    public final BasicControllerBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;




    // Client Constructor
    public AlloyKilnMenu(int containerId, Inventory inv, BlockPos pos) {
        this(containerId, inv, inv.player.level().getBlockEntity(pos), new SimpleContainerData(AlloyKilnMachineHandler.DATA_COUNT));
    }

    // Server Constructor
    public AlloyKilnMenu(int containerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModScreenHandlers.ALLOY_KILN, containerId);
        this.blockEntity = (BasicControllerBlockEntity) entity;
        this.level = inv.player.level();
        this.data = data;

        checkContainerSize(this.blockEntity, AlloyKilnMachineHandler.SLOT_COUNT);
        checkContainerDataCount(data, AlloyKilnMachineHandler.DATA_COUNT);

        // Add machine slots - layout: 2 inputs side by side, fuel below, output to
        // right
        this.addSlot(new              Slot(this.blockEntity,             AlloyKilnMachineHandler.INPUT_SLOT_A, 24, 17));
        this.addSlot(new              Slot(this.blockEntity,             AlloyKilnMachineHandler.INPUT_SLOT_B, 52, 17));
        this.addSlot(new   MachineFuelSlot(this.level, this.blockEntity, AlloyKilnMachineHandler.FUEL_SLOT,    38, 53));
        this.addSlot(new MachineResultSlot(inv.player, this.blockEntity, AlloyKilnMachineHandler.OUTPUT_SLOT, 116, 35));

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        addDataSlots(data);
    }




    public boolean isFuel(ItemStack stack) {
        return level != null && level.fuelValues().burnDuration(stack) > 0;
    }

    public boolean isCrafting() {
        return data.get(AlloyKilnMachineHandler.DATA_ALLOYING_PROGRESS) > 0;
    }

    public boolean isBurning() {
        return data.get(AlloyKilnMachineHandler.DATA_LIT_TIME) > 0;
    }

    public float getBurnProgress() {
        int litTime     = data.get(AlloyKilnMachineHandler.DATA_LIT_TIME);
        int litDuration = data.get(AlloyKilnMachineHandler.DATA_LIT_DURATION);
        return litDuration != 0 ? (float) litTime / (float) litDuration : 0;
    }

    public float getAlloyProgress() {
        int progress    = data.get(AlloyKilnMachineHandler.DATA_ALLOYING_PROGRESS);
        int maxProgress = data.get(AlloyKilnMachineHandler.DATA_ALLOYING_TOTAL_TIME);
        return maxProgress != 0 ? (float) progress / (float) maxProgress : 0;
    }

    public int getScaledProgress() {
        int progress    = data.get(AlloyKilnMachineHandler.DATA_ALLOYING_PROGRESS);
        int maxProgress = data.get(AlloyKilnMachineHandler.DATA_ALLOYING_TOTAL_TIME);
        int progressArrowSize = 24;
        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
    }

    public int getScaledFuelProgress() {
        int litTime     = data.get(AlloyKilnMachineHandler.DATA_LIT_TIME);
        int litDuration = data.get(AlloyKilnMachineHandler.DATA_LIT_DURATION);
        int burnProgressSize = 14;
        return litDuration != 0 && litTime != 0 ? litTime * burnProgressSize / litDuration : 0;
    }




    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_SLOT_COUNT = 4;
    private static final int VANILLA_FIRST_SLOT_INDEX = TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_SLOT_COUNT = 36;

    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if(sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if(pIndex < VANILLA_FIRST_SLOT_INDEX) {
            if(!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, true)) {
                return ItemStack.EMPTY;
            }
        }
        else if(pIndex >= VANILLA_FIRST_SLOT_INDEX && pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            if(!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        }

        if(sourceStack.getCount() == 0) {
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
        for(int i = 0; i < 3; ++i) {
            for(int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for(int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}

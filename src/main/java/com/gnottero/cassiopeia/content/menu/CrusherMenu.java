package com.gnottero.cassiopeia.content.menu;

import com.gnottero.cassiopeia.content.ModRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

/**
 * Menu for the Crusher machine.
 * 1 input slot, 1 fuel slot, 1 output slot.
 */
public class CrusherMenu extends AbstractMachineMenu {

    public static final int DATA_BURN_TIME = 0;
    public static final int DATA_BURN_TIME_TOTAL = 1;
    public static final int DATA_CRUSH_PROGRESS = 2;
    public static final int DATA_CRUSH_TIME_TOTAL = 3;
    public static final int DATA_COUNT = 4;

    // Slot configuration: 1 input, 1 fuel, 1 output
    private static final MachineSlotConfig SLOT_CONFIG = MachineSlotConfig.builder()
            .inputs(new MachineSlotConfig.SlotPosition(56, 17))
            .fuels(new MachineSlotConfig.SlotPosition(56, 53))
            .outputs(new MachineSlotConfig.SlotPosition(116, 35))
            .build();

    /**
     * Client constructor.
     */
    public CrusherMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory,
                new SimpleContainer(SLOT_CONFIG.totalSlotCount()),
                new SimpleContainerData(DATA_COUNT));
    }

    /**
     * Server constructor.
     */
    public CrusherMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModRegistry.CRUSHER_MENU, containerId, playerInventory, container, data, SLOT_CONFIG, DATA_COUNT);
    }

    /**
     * Get the burn progress as a percentage (0.0 to 1.0).
     */
    public float getBurnProgress() {
        int burnTime = getData(DATA_BURN_TIME);
        int burnTimeTotal = getData(DATA_BURN_TIME_TOTAL);
        return burnTimeTotal != 0 && burnTime != 0 ? (float) burnTime / (float) burnTimeTotal : 0.0F;
    }

    /**
     * Get the crush progress as a percentage (0.0 to 1.0).
     */
    public float getCrushProgress() {
        int crushProgress = getData(DATA_CRUSH_PROGRESS);
        int crushTimeTotal = getData(DATA_CRUSH_TIME_TOTAL);
        return crushTimeTotal != 0 && crushProgress != 0 ? (float) crushProgress / (float) crushTimeTotal : 0.0F;
    }

    /**
     * Check if the crusher is currently burning fuel.
     */
    public boolean isBurning() {
        return getData(DATA_BURN_TIME) > 0;
    }

    /**
     * Get the slot configuration.
     */
    public static MachineSlotConfig getConfig() {
        return SLOT_CONFIG;
    }
}

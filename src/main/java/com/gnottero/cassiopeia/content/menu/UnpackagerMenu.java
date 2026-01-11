package com.gnottero.cassiopeia.content.menu;

import com.gnottero.cassiopeia.content.ModRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

/**
 * Menu for the Unpackager machine.
 * 1 input slot, no fuel, 1 output slot.
 */
public class UnpackagerMenu extends AbstractMachineMenu {

    public static final int DATA_PROGRESS = 0;
    public static final int DATA_TOTAL_TIME = 1;
    public static final int DATA_COUNT = 2;

    // Slot configuration: 1 input, 0 fuel, 1 output
    private static final MachineSlotConfig SLOT_CONFIG = MachineSlotConfig.builder()
            .inputs(new MachineSlotConfig.SlotPosition(56, 35))
            .outputs(new MachineSlotConfig.SlotPosition(116, 35))
            .build();

    /**
     * Client constructor.
     */
    public UnpackagerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory,
                new SimpleContainer(SLOT_CONFIG.totalSlotCount()),
                new SimpleContainerData(DATA_COUNT));
    }

    /**
     * Server constructor.
     */
    public UnpackagerMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModRegistry.UNPACKAGER_MENU, containerId, playerInventory, container, data, SLOT_CONFIG, DATA_COUNT);
    }

    /**
     * Get the current progress value.
     */
    public int getProgress() {
        return getData(DATA_PROGRESS);
    }

    /**
     * Get the total processing time.
     */
    public int getTotalTime() {
        return getData(DATA_TOTAL_TIME);
    }

    /**
     * Get the progress as a percentage (0.0 to 1.0).
     */
    public float getProgressPercent() {
        int progress = getData(DATA_PROGRESS);
        int total = getData(DATA_TOTAL_TIME);
        return total != 0 ? (float) progress / (float) total : 0.0F;
    }

    /**
     * Get the slot configuration.
     */
    public static MachineSlotConfig getConfig() {
        return SLOT_CONFIG;
    }
}

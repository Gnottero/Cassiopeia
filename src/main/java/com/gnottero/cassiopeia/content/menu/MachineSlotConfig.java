package com.gnottero.cassiopeia.content.menu;

/**
 * Configuration for machine slot layout.
 * Allows defining any number of input, fuel, and output slots with their
 * positions.
 */
public record MachineSlotConfig(
        int inputSlotCount,
        int fuelSlotCount,
        int outputSlotCount,
        SlotPosition[] inputPositions,
        SlotPosition[] fuelPositions,
        SlotPosition[] outputPositions) {
    /**
     * Get the total number of machine slots.
     */
    public int totalSlotCount() {
        return inputSlotCount + fuelSlotCount + outputSlotCount;
    }

    /**
     * Get the starting index for input slots (always 0).
     */
    public int inputSlotStart() {
        return 0;
    }

    /**
     * Get the starting index for fuel slots.
     */
    public int fuelSlotStart() {
        return inputSlotCount;
    }

    /**
     * Get the starting index for output slots.
     */
    public int outputSlotStart() {
        return inputSlotCount + fuelSlotCount;
    }

    /**
     * Check if this configuration has fuel slots.
     */
    public boolean hasFuelSlots() {
        return fuelSlotCount > 0;
    }

    /**
     * Simple slot position record.
     */
    public record SlotPosition(int x, int y) {
    }

    /**
     * Builder for creating MachineSlotConfig with fluent API.
     */
    public static class Builder {
        private int inputCount = 0;
        private int fuelCount = 0;
        private int outputCount = 0;
        private SlotPosition[] inputPositions = new SlotPosition[0];
        private SlotPosition[] fuelPositions = new SlotPosition[0];
        private SlotPosition[] outputPositions = new SlotPosition[0];

        public Builder inputs(SlotPosition... positions) {
            this.inputCount = positions.length;
            this.inputPositions = positions;
            return this;
        }

        public Builder fuels(SlotPosition... positions) {
            this.fuelCount = positions.length;
            this.fuelPositions = positions;
            return this;
        }

        public Builder outputs(SlotPosition... positions) {
            this.outputCount = positions.length;
            this.outputPositions = positions;
            return this;
        }

        public MachineSlotConfig build() {
            return new MachineSlotConfig(
                    inputCount, fuelCount, outputCount,
                    inputPositions, fuelPositions, outputPositions);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}

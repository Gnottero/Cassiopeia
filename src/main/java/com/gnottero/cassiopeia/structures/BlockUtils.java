package com.gnottero.cassiopeia.structures;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.HashMap;
import java.util.Map;

public class BlockUtils {

    /**
     * Extracts properties from a block state, normalizing the 'facing' property
     * relative to a controller facing.
     *
     * @param state            The block state to process.
     * @param controllerFacing The facing direction of the controller block.
     * @return A map of property names to their values.
     */
    public static Map<String, String> processBlockProperties(BlockState state, Direction controllerFacing) {
        Map<String, String> properties = new HashMap<>();

        for (Property<?> property : state.getProperties()) {
            String key = property.getName();
            Object rawValue = state.getValue(property);
            String value = rawValue.toString();

            if ((key.equals("facing") || key.equals("horizontal_facing")) && rawValue instanceof Direction) {
                // Normalize facing relative to controller
                Direction blockFacing = (Direction) rawValue;
                Direction normalizedFacing = normalizeFacing(blockFacing, controllerFacing);
                value = normalizedFacing.getName();
            }

            properties.put(key, value);
        }

        return properties;
    }

    /**
     * Normalizes a facing direction relative to the controller's facing.
     */
    public static Direction normalizeFacing(Direction blockFacing, Direction controllerFacing) {
        if (blockFacing.getAxis().isVertical() || controllerFacing.getAxis().isVertical()) {
            return blockFacing;
        }

        int controllerIndex = getHorizontalIndex(controllerFacing);
        int blockIndex = getHorizontalIndex(blockFacing);

        if (controllerIndex == -1 || blockIndex == -1) {
            return blockFacing;
        }

        int normalizedIndex = (blockIndex - controllerIndex + 4) % 4;
        return getHorizontalDirection(normalizedIndex);
    }

    public static Direction denormalizeFacing(Direction normalizedFacing, Direction controllerFacing) {
        if (normalizedFacing.getAxis().isVertical() || controllerFacing.getAxis().isVertical()) {
            return normalizedFacing;
        }

        int controllerIndex = getHorizontalIndex(controllerFacing);
        int normalizedIndex = getHorizontalIndex(normalizedFacing);

        if (controllerIndex == -1 || normalizedIndex == -1) {
            return normalizedFacing;
        }

        int actualIndex = (normalizedIndex + controllerIndex) % 4;
        return getHorizontalDirection(actualIndex);
    }

    private static int getHorizontalIndex(Direction direction) {
        switch (direction) {
            case NORTH:
                return 0;
            case EAST:
                return 1;
            case SOUTH:
                return 2;
            case WEST:
                return 3;
            default:
                return -1;
        }
    }

    private static Direction getHorizontalDirection(int index) {
        switch (index) {
            case 0:
                return Direction.NORTH;
            case 1:
                return Direction.EAST;
            case 2:
                return Direction.SOUTH;
            case 3:
                return Direction.WEST;
            default:
                return Direction.NORTH;
        }
    }
}

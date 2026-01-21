package com.gnottero.cassiopeia.utils;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;




public class Utils {
    private Utils() {}




    /**
     * Extracts properties from a block state, normalizing the 'facing' property
     * relative to a controller facing.
     * @param state            The block state to process.
     * @param controllerFacing The facing direction of the controller block.
     * @return A map of property names to their values.
     */
    public static Map<String, String> processBlockProperties(final BlockState state, final Direction controllerFacing) {
        final Map<String, String> properties = new HashMap<>();

        for(final Property<?> property : state.getProperties()) {
            final String key = property.getName();
            final Object rawValue = state.getValue(property);
            String value = rawValue.toString();

            if((key.equals("facing") || key.equals("horizontal_facing")) && rawValue instanceof final Direction blockFacing) {

                // Normalize facing relative to controller
                final Direction normalizedFacing = normalizeFacing(blockFacing, controllerFacing);
                value = normalizedFacing.getName();
            }

            properties.put(key, value);
        }

        return properties;
    }




    /**
     * Normalizes a facing direction relative to the controller's facing.
     */
    public static Direction normalizeFacing(final Direction blockFacing, final Direction controllerFacing) {
        if(blockFacing.getAxis().isVertical() || controllerFacing.getAxis().isVertical()) {
            return blockFacing;
        }

        final int controllerIndex = getHorizontalIndex(controllerFacing);
        final int blockIndex = getHorizontalIndex(blockFacing);

        if(controllerIndex == -1 || blockIndex == -1) {
            return blockFacing;
        }

        final int normalizedIndex = (blockIndex - controllerIndex + 4) % 4;
        return getHorizontalDirection(normalizedIndex);
    }


    public static Direction denormalizeFacing(final Direction normalizedFacing, final Direction controllerFacing) {
        if(normalizedFacing.getAxis().isVertical() || controllerFacing.getAxis().isVertical()) {
            return normalizedFacing;
        }

        final int controllerIndex = getHorizontalIndex(controllerFacing);
        final int normalizedIndex = getHorizontalIndex(normalizedFacing);

        if(controllerIndex == -1 || normalizedIndex == -1) {
            return normalizedFacing;
        }

        final int actualIndex = (normalizedIndex + controllerIndex) % 4;
        return getHorizontalDirection(actualIndex);
    }




    private static int getHorizontalIndex(final Direction direction) {
        return switch(direction) {
            case NORTH ->  0;
            case EAST  ->  1;
            case SOUTH ->  2;
            case WEST  ->  3;
            default    -> -1;
        };
    }


    private static Direction getHorizontalDirection(final int index) {
        return switch(index) {
            case 0  -> Direction.NORTH;
            case 1  -> Direction.EAST;
            case 2  -> Direction.SOUTH;
            case 3  -> Direction.WEST;
            default -> Direction.NORTH;
        };
    }



    public static @NotNull BlockPos localToGlobal(final @NotNull Vector3i localCoords, final @NotNull BlockPos origin, final @NotNull Direction facing) {

        // Apply rotation based on facing direction
        final Vector3i rotated = rotateCoords(localCoords, facing);

        // Translate to global position
        return origin.offset(rotated.x, rotated.y, rotated.z);
    }




    public static @NotNull Vector3i globalToLocal(final @NotNull BlockPos globalPos, final @NotNull BlockPos origin, final @NotNull Direction facing) {

        // Get offset from origin
        final Vector3i offset = new Vector3i(
            globalPos.getX() - origin.getX(),
            globalPos.getY() - origin.getY(),
            globalPos.getZ() - origin.getZ()
        );

        // Apply inverse rotation
        return inverseRotateCoords(offset, facing);
    }




    public static @NotNull Vector3i rotateCoords(final @NotNull Vector3i coords, final @NotNull Direction facing) {
        final int x = coords.x;
        final int y = coords.y;
        final int z = coords.z;

        return switch(facing) {
            case NORTH -> new Vector3i(+x, +y, -z);  // 0° - no rotation
            case EAST  -> new Vector3i(+z, +y, -x);  // 90° CW around Y (from north to east)
            case SOUTH -> new Vector3i(-x, +y, +z);  // 180° around Y
            case WEST  -> new Vector3i(-z, +y, +x);  // 270° CW around Y (or 90° CCW)
            case UP    -> new Vector3i(+x, -z, +y);  // 90° around X axis
            case DOWN  -> new Vector3i(+x, +z, -y);  // -90° around X axis
        };
    }




    public static @NotNull Vector3i inverseRotateCoords(final @NotNull Vector3i coords, final @NotNull Direction facing) {
        final int x = coords.x;
        final int y = coords.y;
        final int z = coords.z;

        return switch(facing) {
            case NORTH -> new Vector3i(+x, +y, -z);  // 0° - no rotation
            case EAST  -> new Vector3i(-z, +y, +x);  // 90° CCW around Y (inverse of 90° CW)
            case SOUTH -> new Vector3i(-x, +y, +z);  // 180° around Y (self-inverse)
            case WEST  -> new Vector3i(+z, +y, -x);  // 90° CW around Y (inverse of 270° CW)
            case UP    -> new Vector3i(+x, +z, -y);  // -90° around X axis (inverse)
            case DOWN  -> new Vector3i(+x, -z, +y);  // 90° around X axis (inverse)
        };
    }
}

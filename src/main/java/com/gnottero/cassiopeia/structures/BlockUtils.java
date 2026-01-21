package com.gnottero.cassiopeia.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3i;




public class BlockUtils {

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


    /**
     * Represents a coordinate system basis with front, up, and right vectors.
     */
    public record Basis(Vec3i front, Vec3i up, Vec3i right) {
        public static Basis from(final Direction facing) {
            final Vec3i front = new Vec3i(facing.getStepX(), facing.getStepY(), facing.getStepZ());
            final Vec3i up    = new Vec3i(0, 1, 0);
            final Vec3i right = front.cross(up);
            return new Basis(front, up, right);
        }
    }




    private static final Map<Direction, Basis> BASIS_CACHE = new EnumMap<>(Direction.class);

    static {
        for(final Direction dir : Direction.values()) {
            BASIS_CACHE.put(dir, Basis.from(dir));
        }
    }

    /**
     * Gets the cached coordinate system basis for the given facing direction.
     */
    public static Basis getBasis(final Direction facing) {
        return BASIS_CACHE.get(facing);
    }

    /**
     * Calculates the target position based on a controller position, an offset, and a basis.
     */
    public static BlockPos calculateTargetPos(final BlockPos controllerPos, final Vector3i offset, final Basis basis) {
        final int offFront = offset.get(0);
        final int offUp    = offset.get(1);
        final int offRight = offset.get(2);

        final int targetX = controllerPos.getX() + offFront * basis.front.getX() + offUp * basis.up.getX() + offRight * basis.right.getX();
        final int targetY = controllerPos.getY() + offFront * basis.front.getY() + offUp * basis.up.getY() + offRight * basis.right.getY();
        final int targetZ = controllerPos.getZ() + offFront * basis.front.getZ() + offUp * basis.up.getZ() + offRight * basis.right.getZ();

        return new BlockPos(targetX, targetY, targetZ);
    }
}

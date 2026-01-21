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
    public static Map<String, String> processBlockProperties(BlockState state, Direction controllerFacing) {
        Map<String, String> properties = new HashMap<>();

        for(Property<?> property : state.getProperties()) {
            String key = property.getName();
            Object rawValue = state.getValue(property);
            String value = rawValue.toString();

            if((key.equals("facing") || key.equals("horizontal_facing")) && rawValue instanceof Direction blockFacing) {

                // Normalize facing relative to controller
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
        if(blockFacing.getAxis().isVertical() || controllerFacing.getAxis().isVertical()) {
            return blockFacing;
        }

        int controllerIndex = getHorizontalIndex(controllerFacing);
        int blockIndex = getHorizontalIndex(blockFacing);

        if(controllerIndex == -1 || blockIndex == -1) {
            return blockFacing;
        }

        int normalizedIndex = (blockIndex - controllerIndex + 4) % 4;
        return getHorizontalDirection(normalizedIndex);
    }


    public static Direction denormalizeFacing(Direction normalizedFacing, Direction controllerFacing) {
        if(normalizedFacing.getAxis().isVertical() || controllerFacing.getAxis().isVertical()) {
            return normalizedFacing;
        }

        int controllerIndex = getHorizontalIndex(controllerFacing);
        int normalizedIndex = getHorizontalIndex(normalizedFacing);

        if(controllerIndex == -1 || normalizedIndex == -1) {
            return normalizedFacing;
        }

        int actualIndex = (normalizedIndex + controllerIndex) % 4;
        return getHorizontalDirection(actualIndex);
    }


    private static int getHorizontalIndex(Direction direction) {
        return switch(direction) {
            case NORTH -> 0;
            case EAST  -> 1;
            case SOUTH -> 2;
            case WEST  -> 3;
            default    -> -1;
        };
    }


    private static Direction getHorizontalDirection(int index) {
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
        public static Basis from(Direction facing) {
            Vec3i front = new Vec3i(facing.getStepX(), facing.getStepY(), facing.getStepZ());
            Vec3i up    = new Vec3i(0, 1, 0);
            Vec3i right = front.cross(up);
            return new Basis(front, up, right);
        }
    }




    private static final Map<Direction, Basis> BASIS_CACHE = new EnumMap<>(Direction.class);

    static {
        for(Direction dir : Direction.values()) {
            BASIS_CACHE.put(dir, Basis.from(dir));
        }
    }

    /**
     * Gets the cached coordinate system basis for the given facing direction.
     */
    public static Basis getBasis(Direction facing) {
        return BASIS_CACHE.get(facing);
    }

    /**
     * Calculates the target position based on a controller position, an offset, and a basis.
     */
    public static BlockPos calculateTargetPos(BlockPos controllerPos, Vector3i offset, Basis basis) {
        int offFront = offset.get(0);
        int offUp    = offset.get(1);
        int offRight = offset.get(2);

        int targetX = controllerPos.getX() + offFront * basis.front.getX() + offUp * basis.up.getX() + offRight * basis.right.getX();
        int targetY = controllerPos.getY() + offFront * basis.front.getY() + offUp * basis.up.getY() + offRight * basis.right.getY();
        int targetZ = controllerPos.getZ() + offFront * basis.front.getZ() + offUp * basis.up.getZ() + offRight * basis.right.getZ();

        return new BlockPos(targetX, targetY, targetZ);
    }
}

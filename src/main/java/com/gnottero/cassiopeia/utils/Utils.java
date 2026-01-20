package com.gnottero.cassiopeia.utils;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;




public class Utils {
    private Utils() {}



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

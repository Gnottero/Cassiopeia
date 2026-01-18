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
            case NORTH -> new Vector3i(+x, +y, +z);  // No rotation (default)
            case SOUTH -> new Vector3i(-x, +y, -z);  // 180° around Y
            case EAST  -> new Vector3i(-z, +y, +x);  // 90° CW around Y
            case WEST  -> new Vector3i(+z, +y, -x);  // 90° CCW around Y
            case UP    -> new Vector3i(+x, -z, +y);  // 90° around X
            case DOWN  -> new Vector3i(+x, +z, -y);  // -90° around X
        };
    }




    public static @NotNull Vector3i inverseRotateCoords(final @NotNull Vector3i coords, final @NotNull Direction facing) {
        final int x = coords.x;
        final int y = coords.y;
        final int z = coords.z;

        return switch(facing) {
            case NORTH -> new Vector3i(+x, +y, +z);  // No rotation (default)
            case SOUTH -> new Vector3i(-x, +y, -z);  // 180° around Y (same as forward)
            case EAST  -> new Vector3i(+z, +y, -x);  // 90° CCW around Y (inverse of CW)
            case WEST  -> new Vector3i(-z, +y, +x);  // 90° CW around Y (inverse of CCW)
            case UP    -> new Vector3i(+x, +z, -y);  // -90° around X (inverse)
            case DOWN  -> new Vector3i(+x, -y, +z);  // 90° around X (inverse)
        };
    }
}

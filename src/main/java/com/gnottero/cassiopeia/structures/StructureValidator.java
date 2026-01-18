package com.gnottero.cassiopeia.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.gnottero.cassiopeia.Cassiopeia;
import com.gnottero.cassiopeia.content.block.entity.AbstractControllerBlockEntity;
import com.gnottero.cassiopeia.utils.Pair;
import com.gnottero.cassiopeia.utils.Utils;







/**
 * A class used for event-driven structure validation.
 */
public class StructureValidator {
    private StructureValidator() {}




    /** A key that uniquely identifies a block in a level. */
    public static record BlockKey(ResourceKey<Level> dimension, BlockPos pos) {}

    /** A structure containing cached data of an existing controller block */
    @SuppressWarnings("java:S1104")
    public static class ControllerData {
        public List<Pair<Boolean, BlockPos>> cachedBlocks;
        public Vector3i min;
        public Vector3i max;
        public int valid;

        public ControllerData(
            final @NotNull List<Pair<Boolean, BlockPos>> cachedBlocks,
            final @NotNull Vector3i min,
            final @NotNull Vector3i max,
            final @NotNull Integer valid
        ) {
            this.cachedBlocks = cachedBlocks;
            this.min = min;
            this.max = max;
            this.valid = valid;
        }
    }




    // A map that associates each controller with the blocks that make up its multiblock structure.
    // The controller is identified by its level and coordinates.
    // The blocks are a static list whose elements are sorted by x, y, and z local coordinates relative to the controller to allow for O(1) access.
    // Each block also stores its state (valud/not valid) and its world coordinates.
    private static final Map<BlockKey, ControllerData> controllers = new HashMap<>();


    // A map that associates each block that could be part of one or more structures to their controllers.
    // The block and its controllers are both identified by their level and coordinates.
    private static final Map<BlockKey, List<BlockKey>> blocks = new HashMap<>();








    /**
     * Registers a controller. Registered controllers keep track of their blocks in the world.
     * <p>
     * This must be called each time a controller is loaded or added to the world.
     * <p>
     * Registering a controller that's already registered has no effect.
     * @param level The level the controller is in.
     * @param pos The position of the controller to register.
     */
    //TODO call this function when events are fired
    public static void registerController(final @NotNull Level level, final @NotNull BlockPos pos) {

        // If the controller is not registered
        final BlockKey key = new BlockKey(level.dimension(), pos);
        if(!controllers.containsKey(key)) {

            // Scan the blocks and cache the data
            scanAllBlocks(level, pos);
        }
    }




    /**
     * Unregisters a controller.
     * <p>
     * This must be called each time a controller is removed from the world.
     * <p>
     * Calling this when a controller is unloaded is not necessary.
     * <p>
     * Calling this on a controller that's not registered has no effect.
     * @param level The level the controller is in.
     * @param pos The position of the controller to unregister.
     */
    //TODO call this function when events are fired
    public static void unregisterController(final @NotNull Level level, final @NotNull BlockPos pos) {
        final BlockKey controllerKey = new BlockKey(level.dimension(), pos);

        // If the controller is registered
        final ControllerData controllerData = controllers.get(controllerKey);
        if(controllerData != null) {

            // Remove controller from the list of controllers of each block
            for(final var cachedBlock : controllerData.cachedBlocks) {
                final BlockKey blockKey = new BlockKey(level.dimension(), cachedBlock.getSecond());
                final var block = blocks.get(blockKey);
                if(block == null) Cassiopeia.LOGGER.error("Controller to unregister not present in one of its block's list of controllers. This should never happen");
                else block.remove(controllerKey);
            }

            // Remove controller from constrollers list
            controllers.remove(controllerKey);
        }
    }




    /**
     * Scans all the blocks that make up the specified controller's structure, fully replacing the cached block data and block-controller references.
     * <p>
     * This also adds the controller to each block's controllers, adding new blocks to the lookup map if needed.
     * <p>
     * This must be called when a controller is first registered (done automatically) and when its target structure is changed.
     * @param level The level the controller is in.
     * @param pos The position of the controller to scan the blocks of.
     */
    public static void scanAllBlocks(final @NotNull Level level, final @NotNull BlockPos pos) {

        // Find structure instance
        final Structure structure = getStructureFromController(level, pos);


        // Create or replace the list of associated blocks
        final BlockKey controllerKey = new BlockKey(level.dimension(), pos);
        final var controllerData = controllers.compute(controllerKey, (k, v) -> {

            // Remove old block-controllers references if present
            if(v != null) {
                for(final var cachedBlock : v.cachedBlocks) {
                    final BlockKey blockKey = new BlockKey(level.dimension(), cachedBlock.getSecond());
                    final var blockControllers = blocks.get(blockKey);
                    if(blockControllers != null) {
                        blockControllers.remove(k);
                    }
                    else Cassiopeia.LOGGER.error("Old affected blocks list references a block that doesn't exist in the block lookup map");
                }
            }

            // Create new ControllerData
            return new ControllerData(
                new ArrayList<>(structure.getBlocks().size()),
                structure.getMinCorner(),
                structure.getMaxCorner(),
                0 // Start valid blocks at 0. Increase the amount while checking each block
            );
        });


        // For each block in the controller's structure
        final Direction direction = Structure.getControllerFacing(level.getBlockState(pos));
        final Vector3i min = structure.getMinCorner();
        final Vector3i max = structure.getMaxCorner();
        for(int x = min.x; x <= max.x; ++x) {
            for(int y = min.y; y <= max.y; ++y) {
                for(int z = min.z; z <= max.z; ++z) {
                    final Vector3i offset = new Vector3i(x, y, z);
                    final BlockPos worldPos = Utils.localToGlobal(offset, pos, direction);

                    // Store the current state of the block in the controller's block list
                    final BlockState blockState = level.getBlockState(worldPos);
                    final boolean isValid = structure.validateBlock(offset, blockState, direction);
                    controllerData.cachedBlocks.add(Pair.from(isValid, worldPos));
                    if(isValid) ++controllerData.valid;

                    // Update the block lookup map
                    final BlockKey blockKey = new BlockKey(level.dimension(), worldPos);
                    final List<BlockKey> affectedControllers = blocks.computeIfAbsent(blockKey, k -> new ArrayList<>());
                    affectedControllers.add(controllerKey);
                }
            }
        }
    }




    /**
     * //TODO
     * @param level //TODO
     * @param pos //TODO
     * @return //TODO
     */
    //FIXME this might need to be cached in the controller data instead of gettting it every single time
    private static @NotNull Structure getStructureFromController(final @NotNull Level level, final @NotNull BlockPos pos) {

        // Find structure id
        if(level.getBlockEntity(pos) instanceof AbstractControllerBlockEntity be) {

            // Find Structure instance (loaded and handled by the structure manager)
            Optional<Structure> structureOpt = StructureManager.getStructure(be.getStructureId());
            if(structureOpt.isPresent()) {

                // Return structure value
                return structureOpt.get();
            }
            else throw new RuntimeException("Structure instance not present");
        }
        else throw new RuntimeException("getStructureFromController called on a non-controller block");
    }




    public enum BlockChangeAction {
        PLACE,
        BREAK,
        MODIFY
    }

    //TODO comment, implement and call
    //TODO specify that this event must be called for any block, including controllers. cntrollers are recognized by the method and handles accordingly
    public static void onBlockChange(final @NotNull Level level, final @NotNull BlockPos pos, final @NotNull BlockChangeAction action) {

        // If the modified block is a controller, register/unregister/scan it based on the action
        if(level.getBlockEntity(pos) instanceof AbstractControllerBlockEntity) {
            switch(action) {
                case PLACE:  { registerController  (level, pos); break; }
                case BREAK:  { unregisterController(level, pos); break; }
                case MODIFY: { scanAllBlocks       (level, pos); break; }
            }
        }

        // If the modified block is not a controller
        else {

            // If the block is part of a possible structure
            final BlockKey blockKey = new BlockKey(level.dimension(), pos);
            final var controllerKeys = blocks.get(blockKey);
            if(controllerKeys != null) {

                // For each controller of the structures the block is part of
                for(final var controllerKey : controllerKeys) {

                    // Get the controller's data
                    final var controllerData = controllers.get(controllerKey);
                    if(controllerData != null) {

                        // Find structure instance
                        final Structure structure = getStructureFromController(level, controllerKey.pos);

                        // Update block validation flag and valid blocks count
                        final Direction direction = Structure.getControllerFacing(level.getBlockState(controllerKey.pos)); //FIXME this might need to be cached in the controller data
                        final Vector3i offset = Utils.globalToLocal(pos, controllerKey.pos, direction);
                        final int index = structure.blockOffsetToIndex(offset);
                        final var blockData = controllerData.cachedBlocks.get(index);
                        if(blockData.getFirst().booleanValue()) --controllerData.valid;
                        final boolean isValid = structure.validateBlock(offset, level.getBlockState(pos), direction);
                        blockData.setFirst(isValid);
                        if(isValid) ++controllerData.valid;
                    }
                    else throw new RuntimeException("A controller referenced by a tracked block is not present in the controllers map");
                }
            }
        }
    }




    public static boolean validateStructure(final @NotNull Level level, final @NotNull BlockPos pos) {

        // Find structure instance
        final Structure structure = getStructureFromController(level, pos);

        // Get controller data and compare the number of valid blocks
        final BlockKey controllerKey = new BlockKey(level.dimension(), pos);
        final ControllerData controllerData = controllers.get(controllerKey);
        return controllerData != null && controllerData.valid >= structure.getBlocks().size();
    }
}

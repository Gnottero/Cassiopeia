package com.gnottero.cassiopeia.structures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import com.gnottero.cassiopeia.Cassiopeia;
import com.gnottero.cassiopeia.content.block.ModBlocks;
import com.gnottero.cassiopeia.content.block.entity.AbstractControllerBlockEntity;
import com.gnottero.cassiopeia.structures.Structure.BlockEntry;
import com.gnottero.cassiopeia.structures.Structure.StructureError;
import com.gnottero.cassiopeia.utils.Utils;


//FIXME RESET MAPS WHEN THE WORLD LOADS
//FIXME RESET MAPS WHEN THE WORLD LOADS
//FIXME RESET MAPS WHEN THE WORLD LOADS
//FIXME RESET MAPS WHEN THE WORLD LOADS




/**
 * A class for event-driven incremental structure validation.
 * <p>
 * This keeps track of all the blocks in a world that are part of a multiblock structure.
 * <p>
 * Every time any block is changed, it's tested against the structures it could be part of and the result is cached.
 * This allows for structure validation in O(1) time.
 */
public class IncrementalStructureValidator {
    private IncrementalStructureValidator() {}




    /** A key that uniquely identifies a block in a level. */
    public static record BlockKey(ResourceKey<Level> dimension, BlockPos pos) {}


    /** A structure containing cached data of an existing controller block */
    @SuppressWarnings("java:S1104")
    public static class ControllerData {
        public List<Boolean> cachedBlocks;
        public Direction direction;
        public Structure structure;
        public int valid;

        public ControllerData(
            final @NotNull List<Boolean> cachedBlocks,
            final @NotNull Direction direction,
            final @NotNull Structure structure,
            final @NotNull Integer validBlocks
        ) {
            this.cachedBlocks = cachedBlocks;
            this.direction = direction;
            this.structure = structure;
            this.valid = validBlocks;
        }
    }




    // A map that associates each controller with the blocks that make up its multiblock structure.
    // The controller is identified by its level and coordinates.
    // The blocks are a static list whose elements are sorted by x, y, and z local coordinates relative to the controller to allow for O(1) access.
    // Each block also stores its state (valud/not valid).
    private static final Map<BlockKey, ControllerData> controllers = new HashMap<>();


    // A map that associates each block that could be part of one or more structures to their controllers.
    // The block and its controllers are both identified by their level and coordinates.
    private static final Map<BlockKey, List<BlockKey>> blocks = new HashMap<>();







    /**
     * Registers a controller. Registered controllers keep track of their blocks in the world.
     * <p>
     * This method also adds the controller to each block's controllers, adding new blocks to the lookup map if needed.
     * <p>
     * This must be called each time a controller is loaded or added to the world or modified.
     * NOTICE: DO NOT CALL THIS WHILE THE CHUNK IS LOADING. That would cause Minecraft to hang. Use lazy registration instead.
     * <p>
     * Registering a controller that's already registered will replace the old data (and update the references).
     * @param level The level the controller is in.
     * @param pos The position of the controller to register.
     */
    public static void registerController(final @NotNull Level level, final @NotNull BlockPos pos) {
        if(level.isClientSide()) return;


        // Find structure instance. Return if it can't be found
        final BlockEntity be = level.getBlockEntity(pos);
        if(!(be instanceof AbstractControllerBlockEntity cbe)) return;
        final Optional<Structure> structureOpt = StructureManager.getStructure(cbe.getStructureId());
        if(structureOpt.isEmpty()) return;
        final Structure structure = structureOpt.get();
        if(structure == null) return;
        structure.ensureInitialized();


        // Create or replace the list of associated blocks
        final BlockKey controllerKey = new BlockKey(level.dimension(), pos);
        final var controllerData = controllers.compute(controllerKey, (k, v) -> {

            // Remove old block-controllers references if present
            if(v != null) {
                for(int i = 0; i < v.cachedBlocks.size(); ++i) {
                    final var blockCoords = Utils.localToGlobal(structure.blockIndexToOffset(i), pos, v.direction);
                    final BlockKey blockKey = new BlockKey(level.dimension(), blockCoords);
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
                Structure.getControllerFacing(level.getBlockState(pos)),
                structure,
                0
                // Start valid blocks at 0. Increase the amount while checking each block
            );
        });


        // For each block in the controller's structure
        final Vector3i min = structure.getMinCorner();
        final Vector3i max = structure.getMaxCorner();
        for(int x = min.x; x <= max.x; ++x) {
            for(int y = min.y; y <= max.y; ++y) {
                for(int z = min.z; z <= max.z; ++z) {
                    final Vector3i offset = new Vector3i(x, y, z);
                    final BlockPos worldPos = Utils.localToGlobal(offset, pos, controllerData.direction);

                    // Store the current state of the block in the controller's block list
                    final BlockState blockState = level.getBlockState(worldPos);
                    final boolean isValid = structure.validateBlock(offset, blockState, controllerData.direction);
                    controllerData.cachedBlocks.add(isValid);
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
    public static void unregisterController(final @NotNull Level level, final @NotNull BlockPos pos) {
        if(level.isClientSide()) return;
        final BlockKey controllerKey = new BlockKey(level.dimension(), pos);

        // If the controller is registered
        final ControllerData controllerData = controllers.get(controllerKey);
        if(controllerData != null) {

            // Remove controller from the list of controllers of each block
            for(int i = 0; i < controllerData.cachedBlocks.size(); ++i) {
                final var blockCoords = Utils.localToGlobal(controllerData.structure.blockIndexToOffset(i), pos, controllerData.direction);
                final BlockKey blockKey = new BlockKey(level.dimension(), blockCoords);
                final var block = blocks.get(blockKey);
                if(block == null) Cassiopeia.LOGGER.error("Controller to unregister not present in one of its block's list of controllers. This should never happen");
                else block.remove(controllerKey);
            }

            // Remove controller from constrollers list
            controllers.remove(controllerKey);
        }
    }








    public enum BlockChangeAction { PLACE, BREAK }
    /**
     * Callback method for block changes. It keeps track of matching blocks in active structures.
     * <p>
     * This should be called each time a block is changed anywhere in the server for any reason.
     * @param level The level the changed block is in.
     * @param pos The position of the changed block.
     * @param oldState The block state of the previous block.
     * @param newState The block state of the current block.
     * @param action The action. This should be PLACE when a non-air block replaces air, BREAK when air replaces non-air.
     *     Replacing a non-air block with another non-air block should be treated as a BREAK and then a PLACE.
     *     In this case, this method should be called twice in order to correctly track changes.
     */
    public static void onBlockChange(
        final @NotNull Level level, final @NotNull BlockPos pos,
        final @NotNull BlockState oldState, final @NotNull BlockState newState,
        final @NotNull BlockChangeAction action
    ) {
        if(level.isClientSide()) return;


        // If the modified block is a controller, register/unregister/scan it based on the action
        //TODO this might need to check a controller tag or something, if we add more controller types in the future. "basic controller" suggests non basic types will be a thing
        if     (action == BlockChangeAction.PLACE && newState.is(ModBlocks.BASIC_CONTROLLER)) registerController  (level, pos);
        else if(action == BlockChangeAction.BREAK && oldState.is(ModBlocks.BASIC_CONTROLLER)) unregisterController(level, pos);


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

                        // Find structure instance, then update the block validation flag and valid blocks count
                        final Vector3i offset = Utils.globalToLocal(pos, controllerKey.pos, controllerData.direction);
                        final int index = controllerData.structure.blockOffsetToIndex(offset);
                        final var blockData = controllerData.cachedBlocks;
                        if(blockData.get(index).booleanValue()) --controllerData.valid;
                        final boolean isValid = controllerData.structure.validateBlock(offset, level.getBlockState(pos), controllerData.direction);
                        blockData.set(index, isValid);
                        if(isValid) ++controllerData.valid;
                    }
                    else throw new RuntimeException("A controller referenced by a tracked block is not present in the controllers map");
                }
            }
        }
    }








    /**
     * Checks if a structure is valid.
     * <p>
     * This uses cached data for O(1) validation time.
     * <p>
     * The cache is updated when relevant events are detected.
     * @param level The level the controller is in.
     * @param pos The position of the controller.
     * @param blockEntity The block entity instance. This is found automatically if omitted.
     * @return True if the structure is valid, false otherwise
     */
    public static boolean validateStructure(final @NotNull Level level, final @NotNull BlockPos pos) {
        return validateStructure(level, pos, level.getBlockEntity(pos));
    }


    /**
     * Checks if a structure is valid.
     * <p>
     * This uses cached data for O(1) validation time.
     * <p>
     * The cache is updated when relevant events are detected.
     * @param level The level the controller is in.
     * @param pos The position of the controller.
     * @return True if the structure is valid, false otherwise
     */
    public static boolean validateStructure(final @NotNull Level level, final @NotNull BlockPos pos, final BlockEntity blockEntity) {
        if(level.isClientSide()) return false;

        // Lazy controller registration
        if(!(blockEntity instanceof AbstractControllerBlockEntity cbe)) return false; //TODO this might need to be an exception/error log
        cbe.ensureRegistered();

        // Get controller data
        final BlockKey controllerKey = new BlockKey(level.dimension(), pos);
        final ControllerData controllerData = controllers.get(controllerKey);

        // Compare the number of valid blocks
        return controllerData != null && controllerData.valid >= controllerData.structure.getBlocks().size();
    }








    /**
     * Identifies missing blocks in the structure relative to the controller's position.
     * <p>
     * Calling this on a single-block structure will always return an empty list.
     * @param level The level the controller is in.
     * @param pos The position of the controller.
     * @return A List of {@link StructureError} objects, one for each incorrect block.
     */
    public static List<StructureError> computeValidationErrors(final @NotNull Level level, final @NotNull BlockPos pos) {
        return computeValidationErrors(level, pos, level.getBlockEntity(pos));
    }


    /**
     * Identifies missing blocks in the structure relative to the controller's position.
     * <p>
     * Calling this on a single-block structure will always return an empty list.
     * @param level The level the controller is in.
     * @param pos The position of the controller.
     * @param blockEntity The block entity instance. This is found automatically if omitted.
     * @return A List of {@link StructureError} objects, one for each incorrect block.
     */
    public static List<StructureError> computeValidationErrors(final @NotNull Level level, final @NotNull BlockPos pos, final BlockEntity blockEntity) {
        final List<StructureError> errors = new ArrayList<>();
        if(level.isClientSide()) return errors;


        // Lazy controller registration
        if(!(blockEntity instanceof AbstractControllerBlockEntity cbe)) return errors; //TODO this might need to be an exception/error log
        cbe.ensureRegistered();


        // Get controller data
        final BlockKey controllerKey = new BlockKey(level.dimension(), pos);
        final ControllerData controllerData = controllers.get(controllerKey);
        final Direction direction = controllerData.direction;


        // For each block in the block entry list
        for(final BlockEntry entry : controllerData.structure.getBlocks()) {
            final BlockPos worldPos = Utils.localToGlobal(entry.getOffset(), pos, direction);
            final BlockState currentState = level.getBlockState(worldPos);

            // Check Block Type (incorrect state)
            if(!currentState.is(entry.getCachedBlock())) {
                BlockState expectedStateForRender = buildDesiredBlockState(entry, direction);
                errors.add(new StructureError(worldPos, StructureError.ErrorType.MISSING, entry.getBlock(), null, expectedStateForRender));
                continue;
            }

            // Check Properties (wrong block)
            Map<String, String> mismatchedProps = checkProperties(currentState, entry, direction);
            if(!mismatchedProps.isEmpty()) {
                BlockState expectedStateForRender = buildDesiredBlockState(entry, direction);
                errors.add(new StructureError(worldPos, StructureError.ErrorType.WRONG_STATE, entry.getBlock(), mismatchedProps, expectedStateForRender));
                //! continue
            }
        }
        return errors;
    }


    private static Map<String, String> checkProperties(BlockState currentState, BlockEntry entry, Direction controllerFacing) {
        if(entry.getCachedProperties() == null || entry.getCachedProperties().isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> mismatched = new HashMap<>();
        for(Map.Entry<Property<?>, Comparable<?>> propEntry : entry.getCachedProperties().entrySet()) {
            Property<?> property = propEntry.getKey();
            Comparable<?> desiredValue = propEntry.getValue();

            // Handle Facing rotation
            if(property.getName().equals("facing") || property.getName().equals("horizontal_facing")) {
                if(desiredValue instanceof Direction desiredDir) {

                    // The desired value stored in the structure is "normalized" (relative to NORTH).
                    // We need to denormalize it to the actual world direction to compare with the world state.
                    Direction desiredWorldDir = BlockUtils.denormalizeFacing(desiredDir, controllerFacing);
                    if(!currentState.getValue(property).equals(desiredWorldDir)) {
                        mismatched.put(property.getName(), desiredDir.getName()); // Return the normalized name as expected
                    }
                    continue;
                }
            }

            if(!currentState.getValue(property).equals(desiredValue)) {
                mismatched.put(property.getName(), desiredValue.toString());
            }
        }
        return mismatched;
    }


    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static BlockState buildDesiredBlockState(BlockEntry entry, Direction controllerFacing) {
        if(entry.getCachedBlock() == null) {
            return null;
        }

        BlockState state = entry.getCachedBlock().defaultBlockState();
        if(entry.getCachedProperties() == null) {
            return state;
        }

        for(Map.Entry<Property<?>, Comparable<?>> propEntry : entry.getCachedProperties().entrySet()) {
            Property property = propEntry.getKey();
            Comparable value = propEntry.getValue();

            if(property.getName().equals("facing") || property.getName().equals("horizontal_facing")) {
                if(value instanceof Direction dir) {
                    value = BlockUtils.denormalizeFacing(dir, controllerFacing);
                }
            }
            state = state.setValue(property, value);
        }
        return state;
    }








    /**
     * Unregisters all controllers whose structure ID matches the specified identifier.
     * <p>
     * This allows them to re-read the data from the Structure instance when needed.
     * <p>
     * Notice:
     *     This method should be called BEFORE a structure is changed.
     *     Doing so after it's changed will break the validation data.
     * @param identifier The identifier to check for.
     */
    public static void unregisterMatching(final @NotNull String identifier) {

        // For each registered controller
        //! Use a copy of the keys as invalidateStructureCache() calls unregisterController which removes controllers from the map
        for(final var key : new ArrayList<>(controllers.keySet())) {

            // Retrieve server, level and block entity
            final Level level = Cassiopeia.getServer().getLevel(key.dimension);
            if(level instanceof ServerLevel serverLevel) {
                final BlockEntity be = serverLevel.getBlockEntity(key.pos);

                // If the block entity is a controller and its ID matches the provided identifier
                if(be instanceof AbstractControllerBlockEntity cbe && cbe.getStructureId().equals(identifier)) {

                    // Invalidate the block entity's structure cache
                    cbe.invalidateStructureCache();
                    //! unregisterController is called by invalidateStructureCache()
                }
            }
        }
    }
}

package com.gnottero.cassiopeia.structures;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.Remove;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;




@SuppressWarnings("java:S2065") // Transient members
public class Structure {
    private List<BlockEntry> blocks;
    private String controller;

    // Transient data
    private transient boolean initialized = false;
    private transient Block cachedControllerBlock;

    // Cache corners for faster in-world operations. Updated whenever a new block is added
    private transient Vector3i minCorner = new Vector3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    private transient Vector3i maxCorner = new Vector3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

    /**
     * Returns the corner with the smallest coordinate values.
     * The vector represents the local coordinates relative to the controller and its direction.
     */
    public Vector3i getMinCorner() { return minCorner; }

    /**
     * Returns the corner with the largest  coordinate values.
     * The vector represents the local coordinates relative to the controller and its direction.
     */
    public Vector3i getMaxCorner() { return maxCorner; }




    public Structure() {
        this.blocks = new ArrayList<>();
    }

    public Structure(List<BlockEntry> blocks, String controller) {
        this.blocks = blocks;
        this.controller = controller;
    }

    public void setController(String controller) {
        this.controller = controller;
        this.initialized = false;
    }

    public String getController() {
        return controller;
    }

    public List<BlockEntry> getBlocks() {
        return blocks;
    }


    public void addBlock(BlockEntry entry) {
        this.blocks.add(entry);
        this.initialized = false;
    }


    public void ensureInitialized() {
        if (initialized) return;

        // Initialize controller
        if (controller != null && !controller.isEmpty()) {
            Identifier id = Identifier.tryParse(controller);
            if (id != null) {
                cachedControllerBlock = BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
            }
        }

        // Initialize all the blocks and calculate the corners
        for (BlockEntry entry : blocks) {
            entry.initialize();
            minCorner.min(entry.getOffset());
            maxCorner.max(entry.getOffset());
        }

        // Sort list of blocks to match the xyz order.
        // This is required in order to properly calculate their index from the offset in O(1) time
        blocks.sort(Comparator
            .comparingInt((BlockEntry entry) -> entry.getOffset().x)
            .thenComparingInt(        entry  -> entry.getOffset().y)
            .thenComparingInt(        entry  -> entry.getOffset().z)
        );

        initialized = true;
    }




    // /**
    //  * Core validation logic.
    //  * @param level            The level to check.
    //  * @param controllerPos    The position of the controller block.
    //  * @param stopOnFirstError If true, returns immediately upon finding the first error (used for verification).
    //  * @return A list of errors found. If stopOnFirstError is true, the list will contain at most one error.
    //  */
    // private List<StructureError> validate(Level level, BlockPos controllerPos, boolean stopOnFirstError) {
    //     if (level == null || controllerPos == null) {
    //         return Collections.singletonList(new StructureError(controllerPos, StructureError.ErrorType.MISSING, "Invalid arguments", null, null));
    //     }
    //     ensureInitialized();
    //     List<StructureError> errors = new ArrayList<>();


    //     // Check Controller
    //     BlockState controllerState = level.getBlockState(controllerPos);
    //     if (cachedControllerBlock != null && !controllerState.is(cachedControllerBlock)) {
    //         errors.add(new StructureError(controllerPos, StructureError.ErrorType.MISSING, controller, null, null));
    //         if (stopOnFirstError) return errors;
    //     }


    //     // Check blocks
    //     if(!blocks.isEmpty()) {
    //         Direction controllerFacing = getControllerFacing(controllerState);
    //         BlockUtils.Basis basis = BlockUtils.getBasis(controllerFacing);

    //         for (BlockEntry entry : blocks) {
    //             BlockPos targetPos = BlockUtils.calculateTargetPos(controllerPos, entry.getOffset(), basis);
    //             BlockState targetState = level.getBlockState(targetPos);

    //             // 1. Check Block Type
    //             if (!targetState.is(entry.cachedBlock)) {
    //                 BlockState expectedStateForRender = buildDesiredBlockState(entry, controllerFacing);
    //                 errors.add(new StructureError(targetPos, StructureError.ErrorType.MISSING, entry.getBlock(), null, expectedStateForRender));
    //                 if (stopOnFirstError) return errors;
    //                 else continue;
    //             }

    //             // 2. Check Properties
    //             Map<String, String> mismatchedProps = checkProperties(targetState, entry, controllerFacing);
    //             if (!mismatchedProps.isEmpty()) {
    //                 BlockState expectedStateForRender = buildDesiredBlockState(entry, controllerFacing);
    //                 errors.add(new StructureError(targetPos, StructureError.ErrorType.WRONG_STATE, entry.getBlock(), mismatchedProps, expectedStateForRender));
    //                 if (stopOnFirstError) return errors;
    //             }
    //         }
    //     }
    //     return errors;
    // }




    //TODO this should prob be a public method in a utility class
    public static Direction getControllerFacing(BlockState controllerState) {
        Property<?> facingProp = controllerState.getBlock().getStateDefinition().getProperty("facing");
        if (facingProp == null) {
            facingProp = controllerState.getBlock().getStateDefinition().getProperty("horizontal_facing");
        }

        if (facingProp != null) {
            Object val = controllerState.getValue(facingProp);
            if (val instanceof Direction direction) {
                return direction;
            }
        }
        return Direction.NORTH;
    }








    public int blockOffsetToIndex(final @NotNull Vector3i offset) {
        final int ySize = maxCorner.y - minCorner.y + 1;
        final int zSize = maxCorner.z - minCorner.z + 1;

        // Calculate offsets from min corner
        final int x_offset = offset.x - minCorner.x;
        final int y_offset = offset.y - minCorner.y;
        final int z_offset = offset.z - minCorner.z;

        // Calculate index from min corner offsets
        return x_offset * (ySize * zSize) + y_offset * zSize + z_offset;
    }




    public @NotNull Vector3i blockIndexToOffset(final int index) {
        final int ySize = maxCorner.y - minCorner.y + 1;
        final int zSize = maxCorner.z - minCorner.z + 1;

        // Extract offsets from the index
        final int x_offset =  index / (ySize * zSize);
        final int y_offset = (index % (ySize * zSize)) / zSize;
        final int z_offset =  index %          zSize;

        // Convert offsets back to local offset
        return new Vector3i(
            x_offset + minCorner.x,
            y_offset + minCorner.y,
            z_offset + minCorner.z
        );
    }




    //TODO add documentation
    public boolean validateBlock(final @NotNull Vector3i offset, final @NotNull BlockState currentState, final @NotNull Direction direction) {
        final int index = blockOffsetToIndex(offset);
        final BlockEntry entry = blocks.get(index);
        final Block desiredBlock = entry.cachedBlock;

        // Check Block Type
        if(!currentState.is(desiredBlock)) {
            return false;
        }

        // Check Properties
        Map<String, String> mismatchedProps = checkProperties(currentState, entry, direction);
        return mismatchedProps.isEmpty();
    }




    private Map<String, String> checkProperties(BlockState currentState, BlockEntry entry, Direction controllerFacing) {
        if (entry.cachedProperties == null || entry.cachedProperties.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> mismatched = new HashMap<>();
        for (Map.Entry<Property<?>, Comparable<?>> propEntry : entry.cachedProperties.entrySet()) {
            Property<?> property = propEntry.getKey();
            Comparable<?> desiredValue = propEntry.getValue();

            // Handle Facing rotation
            if (property.getName().equals("facing") || property.getName().equals("horizontal_facing")) {
                if (desiredValue instanceof Direction desiredDir) {

                    // The desired value stored in the structure is "normalized" (relative to NORTH).
                    // We need to denormalize it to the actual world direction to compare with the world state.
                    Direction desiredWorldDir = BlockUtils.denormalizeFacing(desiredDir, controllerFacing);
                    if (!currentState.getValue(property).equals(desiredWorldDir)) {
                        mismatched.put(property.getName(), desiredDir.getName()); // Return the normalized name as expected
                    }
                    continue;
                }
            }

            if (!currentState.getValue(property).equals(desiredValue)) {
                mismatched.put(property.getName(), desiredValue.toString());
            }
        }
        return mismatched;
    }




    /**
     * A class that describes the issues of an incorrect block in a multiblock structure.
     * <p>
     * It contains the block's position, its block ID, its expected data, and its expected block state.
     */
    public static class StructureError {
        public enum ErrorType {
            MISSING,
            WRONG_STATE
        }

        public final BlockPos pos;
        public final ErrorType type;
        public final String expectedBlockId;
        public final Map<String, String> expectedState;
        public final BlockState expectedBlockState;

        public StructureError(BlockPos pos, ErrorType type, String expectedBlockId, Map<String, String> expectedState, BlockState expectedBlockState) {
            this.pos = pos;
            this.type = type;
            this.expectedBlockId = expectedBlockId;
            this.expectedState = expectedState;
            this.expectedBlockState = expectedBlockState;
        }
    }




    public static class BlockEntry {
        private String block;
        private Vector3i offset;
        private Map<String, String> properties;

        private transient Block cachedBlock;
        private transient Map<Property<?>, Comparable<?>> cachedProperties;

        public BlockEntry(String block, Vector3i offset, Map<String, String> properties) {
            this.block = block;
            this.offset = offset;
            this.properties = properties;
        }

        /**
         * Initializes the block entry.
         */
        public void initialize() {
            Identifier id = Identifier.tryParse(block);
            if (id != null) {
                this.cachedBlock = BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
            }
            if (this.cachedBlock != null && properties != null) {
                this.cachedProperties = new HashMap<>();
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    Property<?> prop = cachedBlock.getStateDefinition().getProperty(entry.getKey());
                    if (prop != null) {
                        Optional<? extends Comparable<?>> val = prop.getValue(entry.getValue());
                        val.ifPresent(comparable -> cachedProperties.put(prop, comparable));
                    }
                }
            }
            else {
                this.cachedProperties = Collections.emptyMap();
            }
        }

        public String getBlock() {
            return block;
        }

        public Vector3i getOffset() {
            return offset;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        public Block getCachedBlock() {
            return cachedBlock;
        }

        public Map<Property<?>, Comparable<?>> getCachedProperties() {
            return cachedProperties;
        }
    }
}

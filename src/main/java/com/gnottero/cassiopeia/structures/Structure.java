package com.gnottero.cassiopeia.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
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

import com.gnottero.cassiopeia.utils.Utils;




@SuppressWarnings("java:S2065") // Transient members
public class Structure {
    private final List<BlockEntry> blocks;
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

    public Structure(final List<BlockEntry> blocks, final String controller) {
        this.blocks = blocks;
        this.controller = controller;
    }

    public void setController(final String controller) {
        this.controller = controller;
        this.initialized = false;
    }

    public String getController() {
        return controller;
    }

    public List<BlockEntry> getBlocks() {
        return blocks;
    }


    public void addBlock(final BlockEntry entry) {
        this.blocks.add(entry);
        this.initialized = false;
    }


    public void ensureInitialized() {
        if(initialized) return;

        // Initialize controller
        if(controller != null && !controller.isEmpty()) {
            final Identifier id = Identifier.tryParse(controller);
            if(id != null) {
                cachedControllerBlock = BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
            }
        }

        // Initialize all the blocks and calculate the corners
        for(final BlockEntry entry : blocks) {
            entry.initialize();
            minCorner.min(entry.getOffset());
            maxCorner.max(entry.getOffset());
        }

        // Sort list of blocks to match the xyz order.
        // This is required in order to properly calculate their index from the offset in O(1) time
        blocks.sort(Comparator
            .comparingInt((final BlockEntry entry) -> entry.getOffset().x)
            .thenComparingInt(        entry  -> entry.getOffset().y)
            .thenComparingInt(        entry  -> entry.getOffset().z)
        );

        initialized = true;
    }




    //TODO this should prob be a public method in a utility class or something
    public static Direction getControllerFacing(final BlockState controllerState) {
        Property<?> facingProp = controllerState.getBlock().getStateDefinition().getProperty("facing");
        if(facingProp == null) {
            facingProp = controllerState.getBlock().getStateDefinition().getProperty("horizontal_facing");
        }

        if(facingProp != null) {
            final Object val = controllerState.getValue(facingProp);
            if(val instanceof final Direction direction) {
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
        final Map<String, String> mismatchedProps = checkProperties(currentState, entry, direction);
        return mismatchedProps.isEmpty();
    }




    private Map<String, String> checkProperties(final BlockState currentState, final BlockEntry entry, final Direction controllerFacing) {
        if(entry.cachedProperties == null || entry.cachedProperties.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<String, String> mismatched = new HashMap<>();
        for(final Map.Entry<Property<?>, Comparable<?>> propEntry : entry.cachedProperties.entrySet()) {
            final Property<?> property = propEntry.getKey();
            final Comparable<?> desiredValue = propEntry.getValue();

            // Handle Facing rotation
            if(property.getName().equals("facing") || property.getName().equals("horizontal_facing")) {
                if(desiredValue instanceof final Direction desiredDir) {

                    // The desired value stored in the structure is "normalized" (relative to NORTH).
                    // We need to denormalize it to the actual world direction to compare with the world state.
                    final Direction desiredWorldDir = Utils.denormalizeFacing(desiredDir, controllerFacing);
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

        public StructureError(final BlockPos pos, final ErrorType type, final String expectedBlockId, final Map<String, String> expectedState, final BlockState expectedBlockState) {
            this.pos = pos;
            this.type = type;
            this.expectedBlockId = expectedBlockId;
            this.expectedState = expectedState;
            this.expectedBlockState = expectedBlockState;
        }
    }




    public static class BlockEntry {
        private final String block;
        private final Vector3i offset;
        private final Map<String, String> properties;

        private transient Block cachedBlock;
        private transient Map<Property<?>, Comparable<?>> cachedProperties;

        public BlockEntry(final String block, final Vector3i offset, final Map<String, String> properties) {
            this.block = block;
            this.offset = offset;
            this.properties = properties;
        }

        /**
         * Initializes the block entry.
         */
        public void initialize() {
            final Identifier id = Identifier.tryParse(block);
            if(id != null) {
                this.cachedBlock = BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
            }
            if(this.cachedBlock != null && properties != null) {
                this.cachedProperties = new HashMap<>();
                for(final Map.Entry<String, String> entry : properties.entrySet()) {
                    final Property<?> prop = cachedBlock.getStateDefinition().getProperty(entry.getKey());
                    if(prop != null) {
                        final Optional<? extends Comparable<?>> val = prop.getValue(entry.getValue());
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

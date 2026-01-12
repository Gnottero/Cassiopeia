package com.gnottero.cassiopeia.structures;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Structure {
    private List<BlockEntry> blocks;
    private String controller;

    private transient boolean initialized = false;
    private transient Block cachedControllerBlock;

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

    @SuppressWarnings("null")
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

    private void ensureInitialized() {
        if (initialized)
            return;

        if (controller != null && !controller.isEmpty()) {
            Identifier id = Identifier.tryParse(controller);
            if (id != null) {
                cachedControllerBlock = BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
            }
        }

        for (BlockEntry entry : blocks) {
            entry.initialize();
        }
        initialized = true;
    }

    /**
     * Verifies if the structure exists in the world at the given controller
     * position.
     *
     * @param level         The level to check.
     * @param controllerPos The position of the controller block.
     * @return true if the structure matches, false otherwise.
     */
    public boolean verify(Level level, BlockPos controllerPos) {
        return validate(level, controllerPos, true).isEmpty();
    }

    /**
     * Identifies missing blocks in the structure relative to the controller
     * position.
     *
     * @param level         The level to check.
     * @param controllerPos The position of the controller block.
     * @return A List of StructureError objects.
     */
    public List<StructureError> getValidationErrors(Level level, BlockPos controllerPos) {
        return validate(level, controllerPos, false);
    }

    /**
     * Core validation logic.
     *
     * @param level            The level to check.
     * @param controllerPos    The position of the controller block.
     * @param stopOnFirstError If true, returns immediately upon finding the first
     *                         error (used for verification).
     * @return A list of errors found. If stopOnFirstError is true, the list will
     *         contain at most one error.
     */
    private List<StructureError> validate(Level level, BlockPos controllerPos, boolean stopOnFirstError) {
        if (level == null || controllerPos == null) {
            return Collections.singletonList(new StructureError(controllerPos, StructureError.ErrorType.MISSING,
                    "Invalid arguments", null, null));
        }

        ensureInitialized();

        BlockState controllerState = level.getBlockState(controllerPos);

        // Check Controller
        if (cachedControllerBlock != null && !controllerState.is(cachedControllerBlock)) {
            if (stopOnFirstError)
                return Collections.singletonList(
                        new StructureError(controllerPos, StructureError.ErrorType.MISSING, controller, null, null));
            // If we're getting errors, we probably want to report the controller is wrong
            // first and foremost
            return Collections.singletonList(
                    new StructureError(controllerPos, StructureError.ErrorType.MISSING, controller, null, null));
        }

        Direction controllerFacing = getControllerFacing(controllerState);
        BlockUtils.Basis basis = BlockUtils.getBasis(controllerFacing);

        List<StructureError> errors = new ArrayList<>();

        for (BlockEntry entry : blocks) {
            BlockPos targetPos = BlockUtils.calculateTargetPos(controllerPos, entry.getOffset(), basis);
            BlockState targetState = level.getBlockState(targetPos);

            // 1. Check Block Type
            if (!targetState.is(entry.cachedBlock)) {
                BlockState expectedStateForRender = buildExpectedBlockState(entry, controllerFacing);
                errors.add(new StructureError(targetPos, StructureError.ErrorType.MISSING, entry.getBlock(), null,
                        expectedStateForRender));
                if (stopOnFirstError)
                    return errors;
                continue;
            }

            // 2. Check Properties
            Map<String, String> mismatchedProps = checkProperties(targetState, entry, controllerFacing);
            if (!mismatchedProps.isEmpty()) {
                BlockState expectedStateForRender = buildExpectedBlockState(entry, controllerFacing);
                errors.add(new StructureError(targetPos, StructureError.ErrorType.WRONG_STATE, entry.getBlock(),
                        mismatchedProps, expectedStateForRender));
                if (stopOnFirstError)
                    return errors;
            }
        }

        return errors;
    }

    private Direction getControllerFacing(BlockState controllerState) {
        Property<?> facingProp = controllerState.getBlock().getStateDefinition().getProperty("facing");
        if (facingProp == null) {
            facingProp = controllerState.getBlock().getStateDefinition().getProperty("horizontal_facing");
        }

        if (facingProp != null) {
            Object val = controllerState.getValue(facingProp);
            if (val instanceof Direction) {
                return (Direction) val;
            }
        }
        return Direction.NORTH;
    }

    private Map<String, String> checkProperties(BlockState targetState, BlockEntry entry, Direction controllerFacing) {
        if (entry.cachedProperties == null || entry.cachedProperties.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> mismatched = new HashMap<>();

        for (Map.Entry<Property<?>, Comparable<?>> propEntry : entry.cachedProperties.entrySet()) {
            Property<?> property = propEntry.getKey();
            Comparable<?> expectedValue = propEntry.getValue();

            // Handle Facing rotation
            if (property.getName().equals("facing") || property.getName().equals("horizontal_facing")) {
                if (expectedValue instanceof Direction expectedDir) {
                    // The expected value stored in the structure is "normalized" (relative to
                    // NORTH).
                    // We need to denormalize it to the actual world direction to compare with the
                    // world state.
                    Direction expectedWorldDir = BlockUtils.denormalizeFacing(expectedDir, controllerFacing);

                    if (!targetState.getValue(property).equals(expectedWorldDir)) {
                        mismatched.put(property.getName(), expectedDir.getName()); // Return the normalized name as
                                                                                   // expected
                    }
                    continue;
                }
            }

            if (!targetState.getValue(property).equals(expectedValue)) {
                mismatched.put(property.getName(), expectedValue.toString());
            }
        }
        return mismatched;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private BlockState buildExpectedBlockState(BlockEntry entry, Direction controllerFacing) {
        if (entry.cachedBlock == null)
            return null;

        BlockState state = entry.cachedBlock.defaultBlockState();
        if (entry.cachedProperties == null)
            return state;

        for (Map.Entry<Property<?>, Comparable<?>> propEntry : entry.cachedProperties.entrySet()) {
            Property property = propEntry.getKey();
            Comparable value = propEntry.getValue();

            if (property.getName().equals("facing") || property.getName().equals("horizontal_facing")) {
                if (value instanceof Direction dir) {
                    value = BlockUtils.denormalizeFacing(dir, controllerFacing);
                }
            }
            state = state.setValue(property, value);
        }
        return state;
    }

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

        public StructureError(BlockPos pos, ErrorType type, String expectedBlockId, Map<String, String> expectedState,
                BlockState expectedBlockState) {
            this.pos = pos;
            this.type = type;
            this.expectedBlockId = expectedBlockId;
            this.expectedState = expectedState;
            this.expectedBlockState = expectedBlockState;
        }
    }

    public static class BlockEntry {
        private String block;
        private List<Double> offset;
        private Map<String, String> properties;

        private transient Block cachedBlock;
        private transient Map<Property<?>, Comparable<?>> cachedProperties;

        public BlockEntry(String block, List<Double> offset, Map<String, String> properties) {
            this.block = block;
            this.offset = offset;
            this.properties = properties;
        }

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
            } else {
                this.cachedProperties = Collections.emptyMap();
            }
        }

        public String getBlock() {
            return block;
        }

        public List<Double> getOffset() {
            return offset;
        }

        public Map<String, String> getProperties() {
            return properties;
        }
    }
}

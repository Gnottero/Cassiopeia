package com.gnottero.cassiopeia.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Structure {
    private List<BlockEntry> blocks;
    private String controller;

    public Structure() {
        this.blocks = new ArrayList<>();
    }

    public Structure(List<BlockEntry> blocks, String controller) {
        this.blocks = blocks;
        this.controller = controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public String getController() {
        return controller;
    }

    public List<BlockEntry> getBlocks() {
        return blocks;
    }

    public void addBlock(BlockEntry entry) {
        this.blocks.add(entry);
    }

    /**
     * Verifies if the structure exists in the world at the given controller
     * position.
     *
     * @param level      The level to check.
     * @param controller The position of the controller block.
     * @return true if the structure matches, false otherwise.
     */
    public boolean verify(Level level, BlockPos controllerPos) {
        if (level == null || controllerPos == null) {
            return false;
        }
        BlockState controllerState = level.getBlockState(controllerPos);

        // Check if the controller block matches the required controller ID
        if (this.controller != null && !this.controller.isEmpty()) {
            String currentControllerId = BuiltInRegistries.BLOCK.getKey(controllerState.getBlock()).toString();
            if (!currentControllerId.equals(this.controller)) {
                return false;
            }
        }

        Property<?> facingProp = controllerState.getBlock().getStateDefinition().getProperty("facing");
        if (facingProp == null) {
            // Try "horizontal_facing" as well, just in case
            facingProp = controllerState.getBlock().getStateDefinition().getProperty("horizontal_facing");
        }

        if (facingProp == null) {
            return false;
        }

        Object facingVal = controllerState.getValue(facingProp);
        if (!(facingVal instanceof Direction)) {
            return false;
        }

        Direction controllerFacing = (Direction) facingVal;

        // Calculate coordinate system bases
        Vec3 front = new Vec3(controllerFacing.getStepX(), controllerFacing.getStepY(), controllerFacing.getStepZ());
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = front.cross(up);

        for (BlockEntry entry : blocks) {
            List<Double> offset = entry.getOffset();
            double offFront = offset.get(0);
            double offUp = offset.get(1);
            double offRight = offset.get(2);

            double targetX = controllerPos.getX() + offFront * front.x + offUp * up.x + offRight * right.x;
            double targetY = controllerPos.getY() + offFront * front.y + offUp * up.y + offRight * right.y;
            double targetZ = controllerPos.getZ() + offFront * front.z + offUp * up.z + offRight * right.z;

            BlockPos targetPos = new BlockPos((int) Math.round(targetX), (int) Math.round(targetY),
                    (int) Math.round(targetZ));
            BlockState targetState = level.getBlockState(targetPos);

            // 1. Check Block Type
            String expectedBlockId = entry.getBlock(); // e.g. "minecraft:stone"
            String targetId = BuiltInRegistries.BLOCK.getKey(targetState.getBlock()).toString();

            if (!targetId.equals(expectedBlockId)) {
                return false;
            }

            // 2. Check Properties
            Map<String, String> expectedProps = entry.getProperties();
            if (expectedProps != null && !expectedProps.isEmpty()) {
                for (Map.Entry<String, String> propEntry : expectedProps.entrySet()) {
                    String propName = propEntry.getKey();
                    String expectedValue = propEntry.getValue();

                    Property<?> property = targetState.getBlock().getStateDefinition().getProperty(propName);
                    if (property == null) {
                        return false;
                    }

                    Object actualObj = targetState.getValue(property);
                    String actualValue = actualObj.toString();

                    if (actualObj instanceof Direction) {
                        // Simplify facing checks if needed, but existing logic tried to normalize.
                        // For now, let's keep it simple or strictly match string if normalization logic
                        // is complex and dependent on BlockUtils which I didn't verify heavily.
                        // But wait, existing logic had:
                        /*
                         * if (propName.equals("facing") && actualObj instanceof Direction) {
                         * Direction actualFacing = (Direction) actualObj;
                         * Direction normalizedActual = BlockUtils.normalizeFacing(actualFacing,
                         * controllerFacing);
                         * actualValue = normalizedActual.getName();
                         * }
                         */
                        // I should preserve that logic if I can.
                        if ((propName.equals("facing") || propName.equals("horizontal_facing"))
                                && actualObj instanceof Direction) {
                            Direction actualFacing = (Direction) actualObj;
                            // Assuming BlockUtils exists and was working
                            Direction normalizedActual = BlockUtils.normalizeFacing(actualFacing, controllerFacing);
                            actualValue = normalizedActual.getName();
                        }
                    }

                    if (!Objects.equals(actualValue, expectedValue)) {
                        return false;
                    }
                }
            }
        }

        return true;
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
        List<StructureError> errors = new java.util.ArrayList<>();

        if (level == null || controllerPos == null) {
            return errors;
        }
        BlockState controllerState = level.getBlockState(controllerPos);

        Property<?> facingProp = controllerState.getBlock().getStateDefinition().getProperty("facing");
        if (facingProp == null) {
            facingProp = controllerState.getBlock().getStateDefinition().getProperty("horizontal_facing");
        }

        if (facingProp == null) {
            return errors;
        }

        Object facingVal = controllerState.getValue(facingProp);
        if (!(facingVal instanceof Direction)) {
            return errors;
        }

        Direction controllerFacing = (Direction) facingVal;
        Vec3 front = new Vec3(controllerFacing.getStepX(), controllerFacing.getStepY(), controllerFacing.getStepZ());
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = front.cross(up);

        for (BlockEntry entry : blocks) {
            List<Double> offset = entry.getOffset();
            double offFront = offset.get(0);
            double offUp = offset.get(1);
            double offRight = offset.get(2);

            double targetX = controllerPos.getX() + offFront * front.x + offUp * up.x + offRight * right.x;
            double targetY = controllerPos.getY() + offFront * front.y + offUp * up.y + offRight * right.y;
            double targetZ = controllerPos.getZ() + offFront * front.z + offUp * up.z + offRight * right.z;

            BlockPos targetPos = new BlockPos((int) Math.round(targetX), (int) Math.round(targetY),
                    (int) Math.round(targetZ));
            BlockState targetState = level.getBlockState(targetPos);

            String expectedBlockId = entry.getBlock();
            String targetId = BuiltInRegistries.BLOCK.getKey(targetState.getBlock()).toString();

            // Build expected BlockState with denormalized properties for rendering
            BlockState expectedBlockState = buildExpectedBlockState(expectedBlockId, entry.getProperties(),
                    controllerFacing);

            if (!targetId.equals(expectedBlockId)) {
                errors.add(new StructureError(targetPos, StructureError.ErrorType.MISSING, expectedBlockId, null,
                        expectedBlockState));
            } else {
                Map<String, String> expectedProps = entry.getProperties();
                Map<String, String> mismatchedProps = new java.util.HashMap<>(); // Only track mismatched properties
                if (expectedProps != null && !expectedProps.isEmpty()) {
                    for (Map.Entry<String, String> propEntry : expectedProps.entrySet()) {
                        String propName = propEntry.getKey();
                        String expectedValue = propEntry.getValue();

                        Property<?> property = targetState.getBlock().getStateDefinition().getProperty(propName);
                        if (property == null) {
                            // Property doesn't exist on block - treat as mismatch
                            mismatchedProps.put(propName, expectedValue);
                            continue;
                        }

                        Object actualObj = targetState.getValue(property);
                        String actualValue = actualObj.toString();
                        String normalizedExpected = expectedValue;

                        if ((propName.equals("facing") || propName.equals("horizontal_facing"))
                                && actualObj instanceof Direction) {
                            Direction actualFacing = (Direction) actualObj;
                            Direction normalizedActual = BlockUtils.normalizeFacing(actualFacing, controllerFacing);
                            actualValue = normalizedActual.getName();
                        }

                        if (!Objects.equals(actualValue, normalizedExpected)) {
                            // Only add mismatched property with denormalized value for display
                            String displayValue = expectedValue;
                            if (propName.equals("facing") || propName.equals("horizontal_facing")) {
                                Direction normalizedDir = Direction.byName(expectedValue);
                                if (normalizedDir != null) {
                                    displayValue = BlockUtils.denormalizeFacing(normalizedDir, controllerFacing)
                                            .getName();
                                }
                            }
                            mismatchedProps.put(propName, displayValue);
                        }
                    }
                }
                if (!mismatchedProps.isEmpty()) {
                    errors.add(new StructureError(targetPos, StructureError.ErrorType.WRONG_STATE, expectedBlockId,
                            mismatchedProps, expectedBlockState));
                }
            }
        }

        return errors;
    }

    /**
     * Builds the expected BlockState with denormalized facing properties.
     */
    private BlockState buildExpectedBlockState(String blockId, Map<String, String> properties,
            Direction controllerFacing) {
        Block block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(blockId));
        if (block == null) {
            return null;
        }
        BlockState state = block.defaultBlockState();

        if (properties != null) {
            for (Map.Entry<String, String> propEntry : properties.entrySet()) {
                String propName = propEntry.getKey();
                String propValue = propEntry.getValue();

                Property<?> property = state.getBlock().getStateDefinition().getProperty(propName);
                if (property != null) {
                    // Denormalize facing properties
                    if ((propName.equals("facing") || propName.equals("horizontal_facing"))) {
                        Direction normalizedDir = Direction.byName(propValue);
                        if (normalizedDir != null) {
                            Direction denormalized = BlockUtils.denormalizeFacing(normalizedDir, controllerFacing);
                            propValue = denormalized.getName();
                        }
                    }
                    state = setPropertyValue(state, property, propValue);
                }
            }
        }
        return state;
    }

    /**
     * Denormalizes facing properties in the map for display purposes.
     */
    private Map<String, String> denormalizeProperties(Map<String, String> properties, Direction controllerFacing) {
        if (properties == null)
            return null;
        Map<String, String> result = new java.util.HashMap<>(properties);
        for (String key : result.keySet()) {
            if (key.equals("facing") || key.equals("horizontal_facing")) {
                Direction normalizedDir = Direction.byName(result.get(key));
                if (normalizedDir != null) {
                    Direction denormalized = BlockUtils.denormalizeFacing(normalizedDir, controllerFacing);
                    result.put(key, denormalized.getName());
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> BlockState setPropertyValue(BlockState state, Property<T> property,
            String value) {
        return property.getValue(value).map(v -> state.setValue(property, v)).orElse(state);
    }

    // Kept for backward compatibility if needed that delegates to new method
    public Map<String, Integer> getMissingBlocks(Level level, BlockPos controllerPos) {
        Map<String, Integer> combined = new java.util.HashMap<>();
        for (StructureError error : getValidationErrors(level, controllerPos)) {
            combined.merge(error.expectedBlockId, 1, Integer::sum);
        }
        return combined;
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
        public final BlockState expectedBlockState; // Full BlockState for rendering

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

        public BlockEntry(String block, List<Double> offset, Map<String, String> properties) {
            this.block = block;
            this.offset = offset;
            this.properties = properties;
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

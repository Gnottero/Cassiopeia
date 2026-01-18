package com.gnottero.cassiopeia.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.gnottero.cassiopeia.Cassiopeia;
import com.gnottero.cassiopeia.content.block.entity.AbstractControllerBlockEntity;
import com.gnottero.cassiopeia.structures.Structure.BlockEntry;
import com.gnottero.cassiopeia.structures.Structure.StructureError;







/**
 * A class used for event-driven structure validation.
 */
public class StructureValidator {
    private StructureValidator() {}




    /**
     * A key that uniquely identifies a block in a level.
     */
    public record BlockKey(ResourceKey<Level> dimension, BlockPos pos) {}




    // A map that associates each controller with the blocks that make up its multiblock structure.
    // The controller is identified by its level and coordinates.
    // The blocks are a static list whose elements are sorted by x, y, and z local coordinates relative to the controller to allow for O(1) access.
    private static final Map<BlockKey, List<BlockState>> controllers = new HashMap<>();

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
    public static void registerController(final @NotNull Level level, final @NotNull BlockPos pos) {

        // If the controller is not registered
        final BlockKey key = new BlockKey(level.dimension(), pos);
        if(!controllers.containsKey(key)) {

            // Scan the blocks and cache block data
            scanAllBlocks(level, pos);
        }
    }


    /**
     * Unregisters a controller.
     * <p>
     * This must be called each time a controller is removed from the world.
     * <p>
     * Calling this when a controller is unloaded is not necessary.
     * @param level The level the controller is in.
     * @param pos The position of the controller to unregister.
     */
    public static void unregisterController(final @NotNull Level level, final @NotNull BlockPos pos) {

    }


    /**
     * Scans all the blocks that make up the specified controller's structure.
     * <p>
     * This fully replaces the cached block data.
     * <p>
     * This must be called when a controller is first registered (done automatically) and when its target structure is changed.
     * @param level The level the controller is in.
     * @param pos The position of the controller to scan the blocks of.
     */
    public static void scanAllBlocks(final @NotNull Level level, final @NotNull BlockPos pos) {

        // Create or replace the list of associated blocks
        final BlockKey key = new BlockKey(level.dimension(), pos);
        final List<BlockState> controllerBlocks = controllers.compute(key, (k, v) -> { return new ArrayList<>(); });

        // Store the current state of the blocks

    }



    public static Structure getStructureFromController(final @NotNull Level level, final @NotNull BlockPos pos) {

        // Find structure id
        final BlockEntity be = level.getBlockEntity(pos);
        if(be instanceof AbstractControllerBlockEntity cbe) {

            // Find Structure instance (loaded and handled by the structure manager)
            Optional<Structure> structureOpt = StructureManager.getStructure(cbe.getStructureId());
            if(structureOpt.isPresent()) {

                // Store all the blocks

                final BlockState controllerState = be.getBlockState();
                final Structure structure = structureOpt.get();
                //TODO
                if(!structure. blocks.isEmpty()) {
                    Direction controllerFacing = Structure.getControllerFacing(controllerState);
                    BlockUtils.Basis basis = BlockUtils.getBasis(controllerFacing);

                    for (BlockEntry entry : blocks) {
                        BlockPos targetPos = BlockUtils.calculateTargetPos(controllerPos, entry.getOffset(), basis);
                        BlockState targetState = level.getBlockState(targetPos);

                        // 1. Check Block Type
                        if (!targetState.is(entry.cachedBlock)) {
                            BlockState expectedStateForRender = buildExpectedBlockState(entry, controllerFacing);
                            errors.add(new StructureError(targetPos, StructureError.ErrorType.MISSING, entry.getBlock(), null, expectedStateForRender));
                            if (stopOnFirstError) return errors;
                            else continue;
                        }

                        // 2. Check Properties
                        Map<String, String> mismatchedProps = checkProperties(targetState, entry, controllerFacing);
                        if (!mismatchedProps.isEmpty()) {
                            BlockState expectedStateForRender = buildExpectedBlockState(entry, controllerFacing);
                            errors.add(new StructureError(targetPos, StructureError.ErrorType.WRONG_STATE, entry.getBlock(), mismatchedProps, expectedStateForRender));
                            if (stopOnFirstError) return errors;
                        }
                    }
                }
            }
        }

    //     // Make sure the ID is there
    //     final Optional<TagKey<Block>> structureId = level.getBlockState(pos)
    // .getTags()
    // .filter(tag -> tag.location().getPath().equals("structure_id"))
    // .findFirst();
    //     if (structureId.isEmpty()) {
    //         return null;
    //     }

        return null; //TODO remove
    }
}

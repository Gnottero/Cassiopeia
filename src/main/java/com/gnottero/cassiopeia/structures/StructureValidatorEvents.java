package com.gnottero.cassiopeia.structures;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.jfr.event.ChunkRegionWriteEvent;
import net.minecraft.world.level.Level;

import com.gnottero.cassiopeia.content.block.entity.AbstractControllerBlockEntity;
import com.gnottero.cassiopeia.structures.StructureValidator.BlockChangeAction;




/**
 * Event handlers for structure validation.
 * Listens to block changes and chunk events to maintain structure integrity.
 */
public class StructureValidatorEvents {
    private StructureValidatorEvents() {}


    /**
     * Registers all event listeners.
     * This must be called once during mod initialization.
     */
    public static void registerEvents() {

        // // Listen for block breaks
        // PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
        //     if (!level.isClientSide()) {
        //         StructureValidator.onBlockChange(level, pos, BlockChangeAction.BREAK);
        //     }
        //     return true;
        // });

        // Listen for chunk loads to register controllers
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk) -> {
            chunk.getBlockEntities().forEach((pos, blockEntity) -> {
                if (blockEntity instanceof AbstractControllerBlockEntity) {
                    StructureValidator.registerController(level, pos);
                }
            });
        });
    }




    // /**
    //  * Must be called when a block is placed.
    //  */
    // public static void onBlockPlaced(Level level, BlockPos pos) {
    //     if (!level.isClientSide()) {
    //         StructureValidator.onBlockChange(level, pos, BlockChangeAction.PLACE);
    //     }
    // }

    /**
     * Must be called when a controller's structure id is changed.
     */ //TODO check if this is covered by the mixin
    public static void onControllerModified(Level level, BlockPos pos) {
        if (!level.isClientSide()) {
            StructureValidator.onBlockChange(level, pos, BlockChangeAction.MODIFY);
        }
    }
}
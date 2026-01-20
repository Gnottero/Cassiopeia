package com.gnottero.cassiopeia.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import com.gnottero.cassiopeia.structures.StructureValidator;
import com.gnottero.cassiopeia.structures.StructureValidator.BlockChangeAction;




/**
 * A mixin that detects all block changes in a server and runs block removal / placement / change callbacks of the structure validator.
 */
@Mixin(Level.class)
// @Mixin(LevelChunk.class)
public class BlockUpdateDetectorMixin {




    // @Inject(
    //     method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Lnet/minecraft/world/level/block/state/BlockState;",
    //     at = @At(value = "RETURN")
    // )
    // private void onBlockStateSet(BlockPos pos, BlockState newState, int flags, CallbackInfoReturnable<BlockState> cir) {

    @Inject(
        method = "setBlocksDirty(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V",
        at = @At("HEAD")
    )
    private void onSetBlocksDirty(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        System.out.println("-------------------------------------------------");
        System.out.println("Block state changed for " + newState.getBlock().getDescriptionId());
        // final LevelChunk chunk = (LevelChunk)(Object)this;
        Level level = (Level)(Object)this;
        // final Level level = chunk.getLevel();
        System.out.println("Client: " + level.isClientSide());
        System.out.println("Server: " + !level.isClientSide());

        // If we are on the server, retrieve the old block state
        if(!level.isClientSide()) {
            // final BlockState oldState = cir.getReturnValue();


            // Determine the action based on old and new states
            final BlockChangeAction action;

            if(oldState == null || oldState.isAir()) {
                if(!newState.isAir()) {
                    action = BlockChangeAction.PLACE;
                }
                else {
                    action = null;
                }
            }
            else if(newState.isAir()) {
                action = BlockChangeAction.BREAK;
            }
            else if(oldState.is(newState.getBlock())) {
                action = BlockChangeAction.MODIFY;
            }
            else {
                //! Different block -> treat as BREAK then PLACE, but just call PLACE
                action = BlockChangeAction.PLACE;
            }


            // Call block change callback if the change is valid
            System.err.println("Action: " + (action == null ? "null" : action.name()));
            if(action != null) StructureValidator.onBlockChange(level, pos, action);
        }
        System.out.println("-------------------------------------------------");
    }
}
package com.gnottero.cassiopeia.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import com.gnottero.cassiopeia.structures.StructureValidator;
import com.gnottero.cassiopeia.structures.StructureValidator.BlockChangeAction;




/**
 * A mixin that detects all block changes in a server and runs block removal / placement / change callbacks of the structure validator.
 */
@Mixin(Level.class)
public class BlockUpdateDetectorMixin {




    @Inject(
        method = "setBlocksDirty(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V",
        at = @At("HEAD")
    )
    private void onSetBlocksDirty(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        Level level = (Level)(Object)this;


        if(!level.isClientSide()) {
            if(oldState.isAir()) {
                if(!newState.isAir()) {
                    StructureValidator.onBlockChange(level, pos, oldState, newState, BlockChangeAction.PLACE);
                }
                //! else return
            }
            else if(newState.isAir()) {
                StructureValidator.onBlockChange(level, pos, oldState, newState, BlockChangeAction.BREAK);
            }
            else {
                final BlockState airState = Blocks.AIR.defaultBlockState();
                StructureValidator.onBlockChange(level, pos, oldState, airState, BlockChangeAction.BREAK);
                StructureValidator.onBlockChange(level, pos, airState, newState, BlockChangeAction.PLACE);
            }
        }
    }
}
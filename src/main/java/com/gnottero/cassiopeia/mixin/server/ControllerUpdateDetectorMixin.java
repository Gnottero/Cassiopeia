package com.gnottero.cassiopeia.mixin.server;

import java.lang.ModuleLayer.Controller;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gnottero.cassiopeia.content.block.entity.AbstractControllerBlockEntity;
import com.gnottero.cassiopeia.structures.StructureValidator;
import com.gnottero.cassiopeia.structures.StructureValidator.BlockChangeAction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;




/**
 * A mixin that detects changes in the data of a controller block entity.
 * These include changes caused by /data commands.
 */
@Mixin(BlockEntity.class)
public class ControllerUpdateDetectorMixin {

    @Inject(
        method = "loadWithComponents",
        at = @At("RETURN")
    )
    private void onLoadWithComponents(ValueInput valueInput, CallbackInfo ci) {
        BlockEntity blockEntity = (BlockEntity)(Object)this;
        Level level = blockEntity.getLevel();

        System.out.println("BLOCK ENTITY CHANGED");
        if (level instanceof ServerLevel && blockEntity instanceof AbstractControllerBlockEntity c) {
            c.invalidateStructureCache();
            System.out.println("INVALIDATED");
        }
    }

    // @Inject(
    //     method = "loadStatic",
    //     at = @At("RETURN")
    // )
    // private static void onLoadStatic(BlockPos pos, BlockState state, CompoundTag tag, HolderLookup.Provider registries, CallbackInfoReturnable<BlockEntity> cir) {
    //     BlockEntity blockEntity = cir.getReturnValue();
    //     if (blockEntity != null) {
    //         Level level = blockEntity.getLevel();
    //         if (level instanceof ServerLevel) {
    //             // Static load (used by commands)
    //             StructureValidator.onBlockEntityModified(level, pos);
    //         }
    //     }
    // }
}
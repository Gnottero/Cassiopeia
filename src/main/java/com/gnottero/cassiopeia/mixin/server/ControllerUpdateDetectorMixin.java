package com.gnottero.cassiopeia.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gnottero.cassiopeia.content.block.entity.AbstractControllerBlockEntity;
import com.gnottero.cassiopeia.structures.IncrementalStructureValidator;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;




/**
 * A mixin that detects changes in the data of a controller block entity.
 * These include changes caused by /data commands.
 */
@Mixin(BlockEntity.class)
public class ControllerUpdateDetectorMixin {

    @Inject(
        method = "loadWithComponents",
        at = @At("HEAD")
    )
    private void onLoadWithComponents(final ValueInput valueInput, final CallbackInfo ci) {
        final BlockEntity blockEntity = (BlockEntity)(Object)this;
        final Level level = blockEntity.getLevel();

        if(level instanceof ServerLevel && blockEntity instanceof final AbstractControllerBlockEntity c) {
            IncrementalStructureValidator.unregisterController(level, c.getBlockPos());
        }
    }
}
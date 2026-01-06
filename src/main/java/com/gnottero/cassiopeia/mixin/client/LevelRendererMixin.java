package com.gnottero.cassiopeia.mixin.client;

import com.gnottero.cassiopeia.client.StructureHighlightRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.gizmos.GizmoCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "collectPerFrameGizmos", at = @At("TAIL"))
    private void cassiopeia$addStructureHighlightGizmos(CallbackInfoReturnable<GizmoCollector> cir) {
        // Emit gizmo outlines for block highlighting
        StructureHighlightRenderer.emitGizmos();
    }

    // Ghost block rendering is now handled by Fabric
    // WorldRenderEvents.BEFORE_DEBUG_RENDER
    // registered in StructureHighlightRenderer.init()
}

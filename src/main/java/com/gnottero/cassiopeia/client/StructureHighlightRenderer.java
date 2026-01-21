package com.gnottero.cassiopeia.client;

import com.gnottero.cassiopeia.mixin.client.CameraAccessor;
import com.gnottero.cassiopeia.network.StructureHighlightPayload;
import com.gnottero.cassiopeia.structures.Structure;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;




/**
 * Renders structure block highlights using Fabric Rendering API.
 * Uses WorldRenderEvents.BEFORE_DEBUG_RENDER for proper world-space ghost block
 * rendering.
 */
public class StructureHighlightRenderer {

    private static final List<Highlight> highlights = new ArrayList<>();
    private static final long HIGHLIGHT_DURATION_MS = 3000; // 5 seconds
    private static final float GHOST_BLOCK_ALPHA_PERCENTAGE = 0.75f; // Alpha value (0.0-1.0) for ghost blocks
    private static long highlightStartTime = 0;




    public static void init() {

        // Register packet receiver
        ClientPlayNetworking.registerGlobalReceiver(StructureHighlightPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                highlights.clear();
                for(final Structure.StructureError error : payload.errors()) {
                    if(error.expectedBlockState != null) {
                        highlights.add(new Highlight(error.pos, error.type, error.expectedBlockState));
                    }
                }
                highlightStartTime = System.currentTimeMillis();
            });
        });

        // Register Fabric world render event for ghost blocks
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(StructureHighlightRenderer::renderGhostBlocks);
    }


    public static boolean hasHighlights() {
        if(highlights.isEmpty())
            return false;
        if(System.currentTimeMillis() - highlightStartTime > HIGHLIGHT_DURATION_MS) {
            highlights.clear();
            return false;
        }
        return true;
    }


    /**
     * Render ghost blocks using Fabric WorldRenderEvents.
     * Called during BEFORE_DEBUG_RENDER with proper view matrix applied.
     */
    @SuppressWarnings("java:S7467")
    private static void renderGhostBlocks(final WorldRenderContext context) {
        if(!hasHighlights()) return;

        final Minecraft mc = Minecraft.getInstance();
        if(mc.level == null) return;

        final BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();

        // Use the PoseStack from context - Fabric says this has proper view matrix
        final PoseStack poseStack = context.matrices();

        // Use the context's consumer as recommended by Fabric API
        // "vertex coordinates sent to consumers should be relative to the camera"
        final MultiBufferSource consumers = context.consumers();

        // Get camera position using accessor mixin
        final Camera camera = mc.gameRenderer.getMainCamera();
        final Vec3 camPos = ((CameraAccessor) camera).cassiopeia$getPosition();

        // Wrap to force translucent rendering using context consumers
        final MultiBufferSource ghostSource = type -> new TranslucentVertexConsumer(
            consumers.getBuffer(RenderTypes.translucentMovingBlock()), (int) (GHOST_BLOCK_ALPHA_PERCENTAGE * 255)
        );

        for(final Highlight highlight : highlights) {
            final BlockPos pos = highlight.pos;
            final BlockState state = highlight.expectedState;

            if(state == null || state.isAir())
                continue;

            poseStack.pushPose();

            // Translate to block position relative to camera
            poseStack.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z );

            try {
                blockRenderer.renderSingleBlock(state, poseStack, ghostSource, 0xF000F0, OverlayTexture.NO_OVERLAY);
            } catch(final Exception e) {
                // Silently ignore render errors
            }

            poseStack.popPose();
        }
    }


    /**
     * Emit gizmo outlines for block highlighting.
     * Called from LevelRendererMixin for outline visualization.
     */
    public static void emitGizmos() {
        if(!hasHighlights())
            return;

        for(final Highlight highlight : highlights) {
            final int color = highlight.type == Structure.StructureError.ErrorType.MISSING
                    ? 0xFFFF3333 // Red outline
                    : 0xFFFFCC33; // Yellow/Orange outline

            emitBlockOutline(highlight.pos, color);
        }
    }


    private static void emitBlockOutline(final BlockPos pos, final int color) {
        final float width = 2.0f;
        final double x0 = pos.getX() - 0.002;
        final double y0 = pos.getY() - 0.002;
        final double z0 = pos.getZ() - 0.002;
        final double x1 = pos.getX() + 1.002;
        final double y1 = pos.getY() + 1.002;
        final double z1 = pos.getZ() + 1.002;

        final Vec3 v000 = new Vec3(x0, y0, z0);
        final Vec3 v100 = new Vec3(x1, y0, z0);
        final Vec3 v110 = new Vec3(x1, y0, z1);
        final Vec3 v010 = new Vec3(x0, y0, z1);
        final Vec3 v001 = new Vec3(x0, y1, z0);
        final Vec3 v101 = new Vec3(x1, y1, z0);
        final Vec3 v111 = new Vec3(x1, y1, z1);
        final Vec3 v011 = new Vec3(x0, y1, z1);

        // Bottom face edges
        Gizmos.line(v000, v100, color, width);
        Gizmos.line(v100, v110, color, width);
        Gizmos.line(v110, v010, color, width);
        Gizmos.line(v010, v000, color, width);

        // Top face edges
        Gizmos.line(v001, v101, color, width);
        Gizmos.line(v101, v111, color, width);
        Gizmos.line(v111, v011, color, width);
        Gizmos.line(v011, v001, color, width);

        // Vertical edges
        Gizmos.line(v000, v001, color, width);
        Gizmos.line(v100, v101, color, width);
        Gizmos.line(v110, v111, color, width);
        Gizmos.line(v010, v011, color, width);
    }


    public static void clearHighlights() {
        highlights.clear();
    }


    public static List<Highlight> getHighlights() {
        return highlights;
    }




    private static class TranslucentVertexConsumer implements com.mojang.blaze3d.vertex.VertexConsumer {
        private final com.mojang.blaze3d.vertex.VertexConsumer delegate;
        private final int alpha;


        public TranslucentVertexConsumer(final com.mojang.blaze3d.vertex.VertexConsumer delegate, final int alpha) {
            this.delegate = delegate;
            this.alpha = alpha;
        }

        @Override
        public com.mojang.blaze3d.vertex.VertexConsumer addVertex(final float x, final float y, final float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public com.mojang.blaze3d.vertex.VertexConsumer setColor(final int r, final int g, final int b, final int a) {
            delegate.setColor(r, g, b, (a * alpha) / 255);
            return this;
        }

        @Override
        public com.mojang.blaze3d.vertex.VertexConsumer setColor(final int color) {
            final int a = (color >> 24) & 0xFF;
            final int r = (color >> 16) & 0xFF;
            final int g = (color >>  8) & 0xFF;
            final int b =  color        & 0xFF;
            return setColor(r, g, b, a);
        }

        @Override
        public com.mojang.blaze3d.vertex.VertexConsumer setUv(final float u, final float v) {
            delegate.setUv(u, v);
            return this;
        }

        @Override
        public com.mojang.blaze3d.vertex.VertexConsumer setUv1(final int u, final int v) {
            delegate.setUv1(u, v);
            return this;
        }

        @Override
        public com.mojang.blaze3d.vertex.VertexConsumer setUv2(final int u, final int v) {
            delegate.setUv2(u, v);
            return this;
        }

        @Override
        public com.mojang.blaze3d.vertex.VertexConsumer setNormal(final float nx, final float ny, final float nz) {
            delegate.setNormal(nx, ny, nz);
            return this;
        }

        @Override
        public com.mojang.blaze3d.vertex.VertexConsumer setLineWidth(final float width) {
            delegate.setLineWidth(width);
            return this;
        }
    }

    public record Highlight(BlockPos pos, Structure.StructureError.ErrorType type, BlockState expectedState) { }
}

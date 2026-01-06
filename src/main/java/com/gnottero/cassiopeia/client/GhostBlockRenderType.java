package com.gnottero.cassiopeia.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

/**
 * Custom RenderType for ghost block rendering with transparency.
 */
public class GhostBlockRenderType {

    /**
     * Creates a translucent block RenderType for ghost block rendering.
     */
    public static final RenderType GHOST_BLOCK;

    static {
        // Define the pipeline with custom shader and standard block rendering settings
        RenderPipeline pipeline = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath("cassiopeia", "ghost_block"))
                .withVertexShader(Identifier.fromNamespaceAndPath("cassiopeia", "core/ghost_block"))
                .withFragmentShader(Identifier.fromNamespaceAndPath("cassiopeia", "core/ghost_block"))
                .withVertexFormat(DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS)
                .withBlend(BlendFunction.TRANSLUCENT)
                .withDepthWrite(false) // Standard for translucent
                .build();

        // Create the RenderSetup
        RenderSetup setup = RenderSetup.builder(pipeline)
                .withTexture("Sampler0", Identifier.fromNamespaceAndPath("minecraft", "textures/atlas/blocks.png"))
                .useLightmap()
                .bufferSize(1536)
                .sortOnUpload() // Important for transparency
                .createRenderSetup();

        // Create the RenderType
        GHOST_BLOCK = RenderType.create("cassiopeia_ghost_block", setup);
    }

    /**
     * Get the ghost block render type.
     */
    public static RenderType ghostBlock() {
        return GHOST_BLOCK;
    }
}

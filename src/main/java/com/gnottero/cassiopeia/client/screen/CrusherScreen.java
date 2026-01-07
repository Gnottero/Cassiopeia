package com.gnottero.cassiopeia.client.screen;

import com.gnottero.cassiopeia.content.menu.CrusherMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class CrusherScreen extends AbstractContainerScreen<CrusherMenu> {

    // Using vanilla furnace texture for simplicity
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/furnace.png");

    public CrusherScreen(CrusherMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        // Center title
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Draw background using blit with RenderPipeline
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256,
                256);

        // Draw burn indicator (flame)
        if (this.menu.isBurning()) {
            int burnHeight = (int) (14 * this.menu.getBurnProgress());
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 56, y + 36 + 14 - burnHeight, 176,
                    14 - burnHeight, 14, burnHeight, 256, 256);
        }

        // Draw progress arrow
        int progressWidth = (int) (24 * this.menu.getCrushProgress());
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 79, y + 34, 176, 14, progressWidth, 17, 256, 256);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}

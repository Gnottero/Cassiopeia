package com.gnottero.cassiopeia.client.screen;

import com.gnottero.cassiopeia.content.menu.AbstractMachineMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for all machine screens.
 * Provides common rendering logic with support for animated progress
 * indicators.
 */
@Environment(EnvType.CLIENT)
public abstract class AbstractMachineScreen<T extends AbstractMachineMenu> extends AbstractContainerScreen<T> {

    protected final Identifier backgroundTexture;

    // Standard texture dimensions
    protected static final int TEXTURE_WIDTH = 256;
    protected static final int TEXTURE_HEIGHT = 256;

    protected AbstractMachineScreen(T menu, Inventory playerInventory, Component title, Identifier backgroundTexture) {
        super(menu, playerInventory, title);
        this.backgroundTexture = backgroundTexture;
    }

    @Override
    protected void init() {
        super.init();
        // Center title
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // Draw background texture
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                this.backgroundTexture,
                x, y,
                0, 0,
                this.imageWidth, this.imageHeight,
                TEXTURE_WIDTH, TEXTURE_HEIGHT);

        // Render machine-specific indicators
        renderMachineIndicators(guiGraphics, x, y, partialTick);
    }

    /**
     * Render machine-specific progress indicators.
     * Subclasses should override to draw progress arrows, flame indicators, etc.
     */
    protected abstract void renderMachineIndicators(GuiGraphics guiGraphics, int x, int y, float partialTick);

    /**
     * Render a horizontal progress sprite (left to right fill).
     *
     * @param guiGraphics   The graphics context
     * @param spriteTexture The sprite texture to render
     * @param x             X position in screen coordinates
     * @param y             Y position in screen coordinates
     * @param spriteWidth   Full width of the sprite
     * @param spriteHeight  Height of the sprite
     * @param progress      Progress from 0.0 to 1.0
     */
    protected void renderHorizontalProgressSprite(
            GuiGraphics guiGraphics,
            Identifier spriteTexture,
            int x, int y,
            int spriteWidth, int spriteHeight,
            float progress) {
        int progressWidth = (int) (spriteWidth * progress);
        if (progressWidth > 0) {
            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    spriteTexture,
                    x, y,
                    0, 0,
                    progressWidth, spriteHeight,
                    spriteWidth, spriteHeight);
        }
    }

    /**
     * Render a vertical progress sprite (bottom to top fill).
     * Commonly used for fuel/burn indicators.
     *
     * @param guiGraphics   The graphics context
     * @param spriteTexture The sprite texture to render
     * @param x             X position in screen coordinates
     * @param y             Y position in screen coordinates
     * @param spriteWidth   Width of the sprite
     * @param spriteHeight  Full height of the sprite
     * @param progress      Progress from 0.0 to 1.0
     */
    protected void renderVerticalProgressSprite(
            GuiGraphics guiGraphics,
            Identifier spriteTexture,
            int x, int y,
            int spriteWidth, int spriteHeight,
            float progress) {
        int progressHeight = (int) (spriteHeight * progress);
        if (progressHeight > 0) {
            int yOffset = spriteHeight - progressHeight;
            guiGraphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    spriteTexture,
                    x, y + yOffset,
                    0, yOffset,
                    spriteWidth, progressHeight,
                    spriteWidth, spriteHeight);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Draw labels with custom color instead of vanilla dark gray
        int color = getLabelColor();
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, color);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, color);
    }

    /**
     * Get the color for GUI labels (title and inventory text).
     * Override in subclasses to customize per machine.
     * NOTE: MC 1.21+ requires ARGB format with alpha channel (0xAARRGGBB).
     *
     * @return ARGB color value (e.g., 0xFF404040 for opaque dark gray)
     */
    protected int getLabelColor() {
        return 0xFF404040; // Default: opaque dark gray (vanilla default)
    }
}

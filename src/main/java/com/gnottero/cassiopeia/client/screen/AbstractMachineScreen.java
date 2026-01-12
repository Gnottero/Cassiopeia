package com.gnottero.cassiopeia.client.screen;

import com.gnottero.cassiopeia.content.menu.AbstractCrushingMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Base screen class for machine GUIs.
 * Provides common rendering utilities for progress indicators.
 *
 * @param <T> The menu type for this screen
 */
@Environment(EnvType.CLIENT)
public abstract class AbstractMachineScreen<T extends AbstractCrushingMenu> extends AbstractContainerScreen<T> {

    protected final Identifier background;

    protected AbstractMachineScreen(T menu, Inventory playerInventory, Component title, Identifier background) {
        super(menu, playerInventory, title);
        this.background = background;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Draw main background
        guiGraphics.blit(this.background, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        // Draw machine-specific indicators (flame, progress arrow, etc.)
        renderMachineIndicators(guiGraphics, x, y, partialTick);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, getLabelColor(), false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY,
                getLabelColor(), false);
    }

    /**
     * Override to render machine-specific progress indicators.
     *
     * @param guiGraphics Graphics context
     * @param x           Left position of the GUI
     * @param y           Top position of the GUI
     * @param partialTick Partial tick time
     */
    protected abstract void renderMachineIndicators(GuiGraphics guiGraphics, int x, int y, float partialTick);

    /**
     * Override to customize label color.
     *
     * @return ARGB color for labels
     */
    protected int getLabelColor() {
        return 0xFF404040; // Default dark gray
    }

    /**
     * Renders a horizontal progress sprite that fills from left to right.
     *
     * @param guiGraphics Graphics context
     * @param texture     The texture identifier
     * @param x           X position
     * @param y           Y position
     * @param width       Full sprite width
     * @param height      Sprite height
     * @param progress    Progress from 0.0 to 1.0
     */
    protected void renderHorizontalProgressSprite(GuiGraphics guiGraphics, Identifier texture,
            int x, int y, int width, int height, float progress) {
        int scaledWidth = (int) (progress * width);
        if (scaledWidth > 0) {
            guiGraphics.blit(texture, x, y, 0, 0, scaledWidth, height, width, height);
        }
    }

    /**
     * Renders a vertical progress sprite that fills from bottom to top.
     *
     * @param guiGraphics Graphics context
     * @param texture     The texture identifier
     * @param x           X position
     * @param y           Y position (top of sprite area)
     * @param width       Sprite width
     * @param height      Full sprite height
     * @param progress    Progress from 0.0 to 1.0
     */
    protected void renderVerticalProgressSprite(GuiGraphics guiGraphics, Identifier texture,
            int x, int y, int width, int height, float progress) {
        int scaledHeight = (int) (progress * height);
        if (scaledHeight > 0) {
            int yOffset = height - scaledHeight;
            guiGraphics.blit(texture, x, y + yOffset, 0, yOffset, width, scaledHeight, width, height);
        }
    }
}

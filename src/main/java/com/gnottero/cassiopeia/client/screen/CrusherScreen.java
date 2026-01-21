package com.gnottero.cassiopeia.client.screen;

import com.gnottero.cassiopeia.Cassiopeia;
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

    private static final Identifier TEXTURE               = Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "textures/gui/container/crusher.png");
    private static final Identifier LIT_PROGRESS_SPRITE   = Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "textures/gui/sprites/container/lit_progress.png");
    private static final Identifier CRUSH_PROGRESS_SPRITE = Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "textures/gui/sprites/container/progress_arrow.png");

    // Customizable label colors (ARGB format)
    protected int titleLabelColor     = 0xFFD0D0D0;
    protected int inventoryLabelColor = 0xFFD0D0D0;




    public CrusherScreen(final CrusherMenu menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title);
    }


    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }


    @Override
    protected void renderLabels(@NotNull final GuiGraphics guiGraphics, final int mouseX, final int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, this.titleLabelColor, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, this.inventoryLabelColor, false);
    }


    @Override
    protected void renderBg(@NotNull final GuiGraphics guiGraphics, final float partialTick, final int mouseX, final int mouseY) {
        final int x = this.leftPos;
        final int y = this.topPos;

        // Draw main background
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
            x, y,                              // dest x, y
            0, 0,                              // source u, v (from bottom of sprite)
            this.imageWidth, this.imageHeight, // width, height to draw
            256, 256                           // total sprite dimensions
        );

        // Draw burn indicator (flame) - sprite is 14x14
        if(this.menu.isBurning()) {
            final float burnProgress = this.menu.getBurnProgress();
            final int burnHeight = (int) Math.ceil(14 * burnProgress);
            if(burnHeight > 0) {

                // Flame sprite position: x+56, y+36 (bottom of flame area)
                // Sprite draws from bottom up
                final int flameX = x + 56;
                final int flameY = y + 36 + 14 - burnHeight;

                // Use blit with sprite texture - draw portion of sprite from bottom
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, LIT_PROGRESS_SPRITE,
                    flameX, flameY,       // dest x, y
                    0f, 14f - burnHeight, // source u, v (from bottom of sprite)
                    14, burnHeight,       // width, height to draw
                    14, 14                // total sprite dimensions
                );
            }
        }

        // Draw progress arrow - sprite dimensions (get from image, appears to be
        // ~24x17)
        final float crushProgress = this.menu.getCrushProgress();
        final int progressWidth = (int) Math.ceil(24 * crushProgress);
        if(progressWidth > 0) {
            // Arrow position: x+79, y+34
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CRUSH_PROGRESS_SPRITE,
                x + 79, y + 34,    // dest x, y
                0f, 0f,            // source u, v (from bottom of sprite)
                progressWidth, 17, // width, height to draw
                24, 17             // total sprite dimensions
            );
        }
    }


    @Override
    public void render(@NotNull final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }


    // Setters for label colors
    public void setTitleLabelColor(final int color) {
        this.titleLabelColor = color;
    }


    public void setInventoryLabelColor(final int color) {
        this.inventoryLabelColor = color;
    }


    public void setLabelColors(final int titleColor, final int inventoryColor) {
        this.titleLabelColor = titleColor;
        this.inventoryLabelColor = inventoryColor;
    }
}

package com.gnottero.cassiopeia.client.screen;

import com.gnottero.cassiopeia.Cassiopeia;
import com.gnottero.cassiopeia.content.menu.CrusherMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class CrusherScreen extends AbstractContainerScreen<CrusherMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID,
            "textures/gui/container/crusher.png");
    private static final Identifier LIT_PROGRESS_SPRITE = Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID,
            "textures/gui/sprites/container/crusher/lit_progress.png");
    private static final Identifier CRUSH_PROGRESS_SPRITE = Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID,
            "textures/gui/sprites/container/crusher/progress_arrow.png");

    protected int titleLabelColor = 0xFFD0D0D0;
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
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY,
                this.inventoryLabelColor, false);
    }

    @Override
    protected void renderBg(@NotNull final GuiGraphics guiGraphics, final float partialTick, final int mouseX,
            final int mouseY) {
        final int x = this.leftPos;
        final int y = this.topPos;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                x, y,
                0, 0,
                this.imageWidth, this.imageHeight,
                256, 256);

        // Draw burn indicator
        if (this.menu.isBurning()) {
            final float burnProgress = this.menu.getBurnProgress();
            // The lit_progress sprite for the crusher is 13x20
            final int burnHeight = (int) Math.ceil(20 * burnProgress);
            if (burnHeight > 0) {
                final int flameX = x + 48;
                final int flameY = y + 33 + 20 - burnHeight;

                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, LIT_PROGRESS_SPRITE,
                        flameX, flameY,
                        0f, 20f - burnHeight,
                        13, burnHeight,
                        13, 20);
            }
        }

        // Draw progress arrow
        final float crushProgress = this.menu.getCrushProgress();
        // The progress_arrow sprite for the crusher is 19x17
        final int progressWidth = (int) Math.ceil(19 * crushProgress);
        if (progressWidth > 0) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CRUSH_PROGRESS_SPRITE,
                    x + 72, y + 34,
                    0f, 0f,
                    progressWidth, 17,
                    19, 17);
        }
    }

    @Override
    public void render(@NotNull final GuiGraphics guiGraphics, final int mouseX, final int mouseY,
            final float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

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

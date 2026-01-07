package com.gnottero.cassiopeia.client.screen;

import com.gnottero.cassiopeia.Cassiopeia;
import com.gnottero.cassiopeia.content.menu.UnpackagerMenu;
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
 * Screen for the unpackager machine.
 * Simple 2-slot layout with progress arrow.
 */
@Environment(EnvType.CLIENT)
public class UnpackagerScreen extends AbstractContainerScreen<UnpackagerMenu> {

    // Using vanilla furnace texture for now (could create custom later)
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/furnace.png");

    public UnpackagerScreen(UnpackagerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Draw main texture
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256,
                256);

        // Draw progress arrow
        int progressWidth = this.menu.getProgressScaled(24);
        if (progressWidth > 0) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 79, y + 34, 176, 14, progressWidth, 17, 256,
                    256);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}

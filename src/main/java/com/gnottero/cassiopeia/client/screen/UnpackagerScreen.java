package com.gnottero.cassiopeia.client.screen;

import com.gnottero.cassiopeia.Cassiopeia;
import com.gnottero.cassiopeia.content.menu.UnpackagerMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for the Unpackager machine.
 * Displays animated progress arrow.
 */
@Environment(EnvType.CLIENT)
public class UnpackagerScreen extends AbstractMachineScreen<UnpackagerMenu> {

    // Using crusher texture for now - TODO: create unpackager-specific texture
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(
            Cassiopeia.MOD_ID, "textures/gui/container/crusher.png");
    private static final Identifier PROGRESS = Identifier.fromNamespaceAndPath(
            Cassiopeia.MOD_ID, "textures/gui/sprites/container/crusher/crush_progress.png");

    // Sprite dimensions
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 17;

    // UI positions relative to GUI top-left
    private static final int ARROW_X = 79;
    private static final int ARROW_Y = 34;

    public UnpackagerScreen(UnpackagerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, BACKGROUND);
    }

    @Override
    protected void renderMachineIndicators(GuiGraphics guiGraphics, int x, int y, float partialTick) {
        // Draw progress arrow - fills from left to right
        float progress = this.menu.getProgressPercent();
        if (progress > 0) {
            renderHorizontalProgressSprite(
                    guiGraphics,
                    PROGRESS,
                    x + ARROW_X, y + ARROW_Y,
                    ARROW_WIDTH, ARROW_HEIGHT,
                    progress);
        }
    }

    @Override
    protected int getLabelColor() {
        return 0xFFE0E0E0; // Off-white/dirty white
    }
}

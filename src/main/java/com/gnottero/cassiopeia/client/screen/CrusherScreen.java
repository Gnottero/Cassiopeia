package com.gnottero.cassiopeia.client.screen;

import com.gnottero.cassiopeia.Cassiopeia;
import com.gnottero.cassiopeia.content.menu.CrusherMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen for the Crusher machine.
 * Displays animated flame indicator and progress arrow.
 */
@Environment(EnvType.CLIENT)
public class CrusherScreen extends AbstractMachineScreen<CrusherMenu> {

    // Mod textures
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(
            Cassiopeia.MOD_ID, "textures/gui/container/crusher.png");
    private static final Identifier CRUSH_PROGRESS = Identifier.fromNamespaceAndPath(
            Cassiopeia.MOD_ID, "textures/gui/sprites/container/crusher/crush_progress.png");
    private static final Identifier LIT_PROGRESS = Identifier.fromNamespaceAndPath(
            Cassiopeia.MOD_ID, "textures/gui/sprites/container/crusher/lit_progress.png");

    // Sprite dimensions (measured from actual textures)
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 17;
    private static final int FLAME_WIDTH = 14;
    private static final int FLAME_HEIGHT = 14;

    // UI positions relative to GUI top-left
    private static final int FLAME_X = 56;
    private static final int FLAME_Y = 36;
    private static final int ARROW_X = 79;
    private static final int ARROW_Y = 34;

    public CrusherScreen(CrusherMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, BACKGROUND);
    }

    @Override
    protected void renderMachineIndicators(GuiGraphics guiGraphics, int x, int y, float partialTick) {
        // Draw burn indicator (flame) - fills from bottom to top when burning
        if (this.menu.isBurning()) {
            renderVerticalProgressSprite(
                    guiGraphics,
                    LIT_PROGRESS,
                    x + FLAME_X, y + FLAME_Y,
                    FLAME_WIDTH, FLAME_HEIGHT,
                    this.menu.getBurnProgress());
        }

        // Draw progress arrow - fills from left to right
        float crushProgress = this.menu.getCrushProgress();
        if (crushProgress > 0) {
            renderHorizontalProgressSprite(
                    guiGraphics,
                    CRUSH_PROGRESS,
                    x + ARROW_X, y + ARROW_Y,
                    ARROW_WIDTH, ARROW_HEIGHT,
                    crushProgress);
        }
    }

    @Override
    protected int getLabelColor() {
        return 0xFFE0E0E0; // Off-white/dirty white
    }
}

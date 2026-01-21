package com.gnottero.cassiopeia.content.menu.slot;

import com.gnottero.cassiopeia.content.block.entity.BasicControllerBlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;




/**
 * A slot for machine output that doesn't accept items from players.
 * Similar to Minecraft's FurnaceResultSlot.
 */
public class MachineResultSlot extends Slot {
    private final Player player;
    private int removeCount;

    public MachineResultSlot(final Player player, final Container container, final int slot, final int x, final int y) {
        super(container, slot, x, y);
        this.player = player;
    }

    @Override
    public boolean mayPlace(final ItemStack itemStack) {
        return false;
    }

    @Override
    public ItemStack remove(final int amount) {
        if(this.hasItem()) {
            this.removeCount += Math.min(amount, this.getItem().getCount());
        }
        return super.remove(amount);
    }

    @Override
    public void onTake(final Player player, final ItemStack itemStack) {
        this.checkTakeAchievements(itemStack);
        super.onTake(player, itemStack);
    }

    @Override
    protected void onQuickCraft(final ItemStack itemStack, final int count) {
        this.removeCount += count;
        this.checkTakeAchievements(itemStack);
    }
    @Override
    protected void checkTakeAchievements(final ItemStack itemStack) {
        itemStack.onCraftedBy(this.player, this.removeCount);
        awardRecipesIfApplicable();
        this.removeCount = 0;
    }

    /**
     * Awards used recipes to the player if the container is a
     * BasicControllerBlockEntity.
     */
    private void awardRecipesIfApplicable() {
        if(this.player instanceof final ServerPlayer serverPlayer
                && this.container instanceof final BasicControllerBlockEntity controllerEntity) {
            controllerEntity.awardUsedRecipes(serverPlayer, java.util.Collections.emptyList());
        }
    }
}

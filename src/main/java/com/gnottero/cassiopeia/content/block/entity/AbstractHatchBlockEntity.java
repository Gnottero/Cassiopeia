package com.gnottero.cassiopeia.content.block.entity;

import com.gnottero.cassiopeia.structures.IncrementalStructureValidator;
import com.gnottero.cassiopeia.structures.IncrementalStructureValidator.BlockKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public abstract class AbstractHatchBlockEntity extends BlockEntity implements WorldlyContainer {
    protected AbstractHatchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected Optional<BasicControllerBlockEntity> getController() {
        if (level == null || level.isClientSide()) {
            return Optional.empty();
        }

        List<BlockKey> controllerKeys = IncrementalStructureValidator.getControllersFor(level, getBlockPos());
        for (BlockKey key : controllerKeys) {
            if (IncrementalStructureValidator.isStructureValid(key)) {
                BlockEntity be = level.getBlockEntity(key.pos());
                if (be instanceof BasicControllerBlockEntity controller) {
                    return Optional.of(controller);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public int getContainerSize() {
        return getController().map(BasicControllerBlockEntity::getContainerSize).orElse(0);
    }

    @Override
    public boolean isEmpty() {
        return getController().map(BasicControllerBlockEntity::isEmpty).orElse(true);
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return getController().map(c -> c.getItem(slot)).orElse(ItemStack.EMPTY);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        return getController().map(c -> c.removeItem(slot, amount)).orElse(ItemStack.EMPTY);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        return getController().map(c -> c.removeItemNoUpdate(slot)).orElse(ItemStack.EMPTY);
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        getController().ifPresent(c -> c.setItem(slot, stack));
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        // Use the controller's stillValid logic but check distance to this hatch
        return getController().map(c -> c.stillValid(player)).orElse(false);
    }

    @Override
    public void clearContent() {
        getController().ifPresent(BasicControllerBlockEntity::clearContent);
    }

    @Override
    public abstract int @NotNull [] getSlotsForFace(@NotNull Direction side);

    @Override
    public abstract boolean canPlaceItemThroughFace(int index, @NotNull ItemStack stack, @Nullable Direction direction);

    @Override
    public abstract boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction);
}

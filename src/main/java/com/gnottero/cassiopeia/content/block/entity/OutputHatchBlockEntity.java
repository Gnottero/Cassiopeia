package com.gnottero.cassiopeia.content.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OutputHatchBlockEntity extends AbstractHatchBlockEntity {
    public OutputHatchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OUTPUT_HATCH, pos, state);
    }

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        return getController().map(c -> c.getSlotsForFace(Direction.DOWN)).orElse(new int[0]);
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack stack, @Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
        return getController().map(c -> c.canTakeItemThroughFace(index, stack, direction)).orElse(false);
    }
}

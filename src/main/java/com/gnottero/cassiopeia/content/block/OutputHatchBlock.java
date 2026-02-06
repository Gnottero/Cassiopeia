package com.gnottero.cassiopeia.content.block;

import com.gnottero.cassiopeia.content.block.entity.OutputHatchBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OutputHatchBlock extends HatchBlock {
    public static final MapCodec<OutputHatchBlock> CODEC = simpleCodec(OutputHatchBlock::new);

    public OutputHatchBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends OutputHatchBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new OutputHatchBlockEntity(pos, state);
    }
}

package com.gnottero.cassiopeia.content.block;

import com.gnottero.cassiopeia.content.block.entity.InputHatchBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InputHatchBlock extends HatchBlock {
    public static final MapCodec<InputHatchBlock> CODEC = simpleCodec(InputHatchBlock::new);

    public InputHatchBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends InputHatchBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new InputHatchBlockEntity(pos, state);
    }
}

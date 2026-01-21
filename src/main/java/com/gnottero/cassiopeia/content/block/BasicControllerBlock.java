package com.gnottero.cassiopeia.content.block;

import com.gnottero.cassiopeia.content.block.entity.AbstractControllerBlockEntity;
import com.gnottero.cassiopeia.content.block.entity.BasicControllerBlockEntity;
import com.gnottero.cassiopeia.content.machine.MachineHandlerRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;




public class BasicControllerBlock extends AbstractControllerBlock {
    public static final MapCodec<BasicControllerBlock> CODEC = simpleCodec(BasicControllerBlock::new);


    public BasicControllerBlock(final Properties properties) {
        super(properties);
    }


    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }


    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull final BlockPos pos, @NotNull final BlockState state) {
        return new BasicControllerBlockEntity(pos, state);
    }


    @Override
    @SuppressWarnings("java:S2638")
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull final Level level, @NotNull final BlockState state, @NotNull final BlockEntityType<T> type) {
        if(level.isClientSide()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if(be instanceof final BasicControllerBlockEntity controllerBE) {
                BasicControllerBlockEntity.serverTick(lvl, pos, st, controllerBE);
            }
        };
    }


    @Override
    protected void openGui(final Player player, final AbstractControllerBlockEntity be) {
        final String structureId = be.getStructureId();

        // Check if this is a machine with a registered handler
        if(structureId != null && be instanceof final BasicControllerBlockEntity machineBE) {
            if(MachineHandlerRegistry.hasHandler(structureId)) {
                player.openMenu(machineBE);
            }
        }
    }
}

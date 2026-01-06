package com.gnottero.cassiopeia.content.block.entity;

import com.gnottero.cassiopeia.content.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class Tier1ControllerBlockEntity extends AbstractControllerBlockEntity {

    public Tier1ControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.TIER_1_CONTROLLER_BLOCK_ENTITY, pos, state);
    }
}

package com.gnottero.cassiopeia.content.block;

import com.gnottero.cassiopeia.content.block.entity.AbstractControllerBlockEntity;
import com.gnottero.cassiopeia.content.block.entity.Tier1ControllerBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Tier1ControllerBlock extends AbstractControllerBlock {
    public static final MapCodec<Tier1ControllerBlock> CODEC = simpleCodec(Tier1ControllerBlock::new);

    public Tier1ControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new Tier1ControllerBlockEntity(pos, state);
    }

    @Override
    protected void openGui(Player player, AbstractControllerBlockEntity be) {
        String structureId = be.getStructureId();
        String title = "Inventory";
        if (structureId != null && !structureId.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            boolean capitalizeNext = true;
            for (char c : structureId.toCharArray()) {
                if (c == '_') {
                    sb.append(' ');
                    capitalizeNext = true;
                } else {
                    if (capitalizeNext) {
                        sb.append(Character.toUpperCase(c));
                        capitalizeNext = false;
                    } else {
                        sb.append(c);
                    }
                }
            }
            title = sb.toString();
        }

        player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                (syncId, inventory, p) -> net.minecraft.world.inventory.ChestMenu.threeRows(syncId, inventory,
                        new net.minecraft.world.SimpleContainer(27)),
                net.minecraft.network.chat.Component.literal(title)));
    }
}

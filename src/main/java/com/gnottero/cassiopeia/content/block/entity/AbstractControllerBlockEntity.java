package com.gnottero.cassiopeia.content.block.entity;

import com.gnottero.cassiopeia.structures.Structure;
import com.gnottero.cassiopeia.structures.StructureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class AbstractControllerBlockEntity extends BlockEntity {

    private static final String STRUCTURE_ID_KEY = "structure_id";
    private String structureId = "ticker_structure";

    public AbstractControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void setStructureId(String id) {
        this.structureId = id;
        this.setChanged();
    }

    public String getStructureId() {
        return structureId;
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        if (structureId != null && !structureId.isEmpty()) {
            output.putString(STRUCTURE_ID_KEY, structureId);
        }
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        this.structureId = input.getStringOr(STRUCTURE_ID_KEY, "ticker_structure");
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    public boolean verifyStructure(Level level, BlockPos pos) {
        if (structureId == null || structureId.isEmpty()) {
            return false;
        }

        Optional<Structure> structureOpt = StructureManager.getStructure(structureId);
        if (structureOpt.isPresent()) {
            return structureOpt.get().verify(level, pos);
        }
        return false;
    }
}

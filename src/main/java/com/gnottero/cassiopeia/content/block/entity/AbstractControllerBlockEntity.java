package com.gnottero.cassiopeia.content.block.entity;

import com.gnottero.cassiopeia.structures.IncrementalStructureValidator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;




public abstract class AbstractControllerBlockEntity extends BlockEntity {

    private static final String STRUCTURE_ID_KEY = "structure_id";
    private String structureId = Strings.EMPTY;
    // private boolean registered = false; //TODO remove




    protected AbstractControllerBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }


    public void setStructureId(final String id) {
        this.structureId = id;
        this.setChanged();
    }


    public String getStructureId() {
        return structureId;
    }


    @Override
    protected void saveAdditional(@NotNull final ValueOutput output) {
        super.saveAdditional(output);
        if(!structureId.isEmpty()) {
            output.putString(STRUCTURE_ID_KEY, structureId);
        }
    }


    @Override
    protected void loadAdditional(@NotNull final ValueInput input) {
        super.loadAdditional(input);
        this.structureId = input.getStringOr(STRUCTURE_ID_KEY, Strings.EMPTY);
    }


    @Override
    @SuppressWarnings("java:S2638")
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    @Override
    public @NotNull CompoundTag getUpdateTag(final HolderLookup.@NotNull Provider registries) {
        return this.saveWithoutMetadata(registries);
    }




    public boolean verifyStructure() {
        //! ensureRegistered is called by validateStructure
        return IncrementalStructureValidator.validateStructure(level, getBlockPos(), this);
    }

    public void ensureRegistered() {
        if(!IncrementalStructureValidator.isRegistered(level, getBlockPos()) && level != null && !level.isClientSide()) {
            IncrementalStructureValidator.registerController(level, getBlockPos());
        }
    }

    public void invalidateStructureCache() {
        if(IncrementalStructureValidator.isRegistered(level, getBlockPos())) {
            IncrementalStructureValidator.unregisterController(level, getBlockPos());
        }
    }
}

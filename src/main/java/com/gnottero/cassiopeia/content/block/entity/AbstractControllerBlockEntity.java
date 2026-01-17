package com.gnottero.cassiopeia.content.block.entity;

import com.gnottero.cassiopeia.structures.Structure;
import com.gnottero.cassiopeia.structures.StructureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
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

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.List;
import java.util.Optional;




public abstract class AbstractControllerBlockEntity extends BlockEntity {

    private static final String STRUCTURE_ID_KEY = "structure_id";
    private String structureId = Strings.EMPTY;




    protected AbstractControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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
        if (!structureId.isEmpty()) {
            output.putString(STRUCTURE_ID_KEY, structureId);
        }
    }


    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        this.structureId = input.getStringOr(STRUCTURE_ID_KEY, Strings.EMPTY);
    }


    @Override
    @SuppressWarnings("java:S2638")
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return this.saveWithoutMetadata(registries);
    }


    public boolean verifyStructure(Level level, BlockPos pos) {

        // Make sure the ID is there
        if (structureId.isEmpty()) {
            return false;
        }

        // Make sure the structure is loaded
        Optional<Structure> structureOpt = StructureManager.getStructure(structureId);
        if (structureOpt.isEmpty()) {
            return false;
        }

        // Check in-world controller and blocks
        Structure structure = structureOpt.get();
        return structure.verify(level, pos);
    }
}

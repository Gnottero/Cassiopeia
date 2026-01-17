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

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.List;
import java.util.Optional;

public abstract class AbstractControllerBlockEntity extends BlockEntity {

    private static final String STRUCTURE_ID_KEY = "structure_id";
    private String structureId = Strings.EMPTY;

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
        if (!structureId.isEmpty()) {
            output.putString(STRUCTURE_ID_KEY, structureId);
        }
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        this.structureId = input.getStringOr(STRUCTURE_ID_KEY, Strings.EMPTY);
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
        if (structureId.isEmpty()) {
            return false;
        }

        Optional<Structure> structureOpt = StructureManager.getStructure(structureId);
        if (structureOpt.isEmpty()) {
            return false;
        }

        Structure structure = structureOpt.get();

        // For single-block structures (just the controller), skip complex verification
        // Check if structure only contains the controller block at offset [0,0,0]
        List<Structure.BlockEntry> blocks = structure.getBlocks();
        if (blocks.size() == 1) {
            Structure.BlockEntry entry = blocks.get(0);
            Vector3d offset = entry.getOffset();
            // If the only block is at offset 0,0,0 - this is a single-block machine
            if (offset.get(0) == 0.0 && offset.get(1) == 0.0 && offset.get(2) == 0.0) {
                // Just verify the controller block matches
                String expectedBlockId = entry.getBlock();
                String actualBlockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                        .getKey(level.getBlockState(pos).getBlock()).toString();
                return actualBlockId.equals(expectedBlockId);
            }
        }

        // For multi-block structures, use full verification
        return structure.verify(level, pos);
    }
}

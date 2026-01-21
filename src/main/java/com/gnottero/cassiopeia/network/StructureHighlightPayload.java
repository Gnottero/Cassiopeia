package com.gnottero.cassiopeia.network;

import com.gnottero.cassiopeia.Cassiopeia;
import com.gnottero.cassiopeia.structures.Structure;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;




public record StructureHighlightPayload(List<Structure.StructureError> errors) implements CustomPacketPayload {

    @SuppressWarnings("java:S1845") // Confusing name "TYPE"
    public static final CustomPacketPayload.Type<StructureHighlightPayload> TYPE = new CustomPacketPayload.Type<>(
        Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "structure_highlight")
    );


    @SuppressWarnings("java:S7467") // Unused exception e
    public static final StreamCodec<RegistryFriendlyByteBuf, StructureHighlightPayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeInt(payload.errors.size());
            for(final Structure.StructureError error : payload.errors) {
                buf.writeBlockPos(error.pos);
                buf.writeEnum(error.type);

                // Serialize BlockState as string
                final String stateStr = error.expectedBlockState != null ? BlockStateParser.serialize(error.expectedBlockState) : "";
                buf.writeUtf(stateStr);
            }
        },
        buf -> {
            final int size = buf.readInt();
            final List<Structure.StructureError> errors = new ArrayList<>(size);
            final HolderLookup<net.minecraft.world.level.block.Block> blockLookup = BuiltInRegistries.BLOCK;
            for(int i = 0; i < size; i++) {
                final BlockPos pos = buf.readBlockPos();
                final Structure.StructureError.ErrorType type = buf.readEnum(Structure.StructureError.ErrorType.class);
                final String stateStr = buf.readUtf();
                BlockState state = null;
                if(!stateStr.isEmpty()) {
                    try {
                        state = BlockStateParser.parseForBlock(blockLookup, stateStr, false).blockState();
                    } catch(final Exception e) {
                        // Fallback: ignore parse errors
                    }
                }
                errors.add(new Structure.StructureError(pos, type, "", null, state));
            }
            return new StructureHighlightPayload(errors);
        }
    );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

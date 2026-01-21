package com.gnottero.cassiopeia.content.block;

import com.gnottero.cassiopeia.content.block.entity.AbstractControllerBlockEntity;
import com.gnottero.cassiopeia.network.StructureHighlightPayload;
import com.gnottero.cassiopeia.structures.Structure;
import com.gnottero.cassiopeia.structures.Structure.StructureError;
import com.gnottero.cassiopeia.structures.StructureManager;
import com.gnottero.cassiopeia.structures.IncrementalStructureValidator;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.jetbrains.annotations.NotNull;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;








public abstract class AbstractControllerBlock extends BaseEntityBlock {

    protected AbstractControllerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }


    @Override
    protected abstract MapCodec<? extends BaseEntityBlock> codec();


    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }


    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }




    @Override
    protected @NotNull InteractionResult useItemOn(
        @NotNull ItemStack stack,
        @NotNull BlockState state,
        @NotNull Level level,
        @NotNull BlockPos pos,
        @NotNull Player player,
        @NotNull InteractionHand hand,
        @NotNull BlockHitResult hitResult
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof AbstractControllerBlockEntity controllerBE) {
            return handleStructureInteraction(level, pos, player, controllerBE);
        }
        return InteractionResult.PASS;
    }




    private InteractionResult handleStructureInteraction(Level level, BlockPos pos, Player player, AbstractControllerBlockEntity controllerBE) {
        String structureId = controllerBE.getStructureId();

        // Make sure the structure ID is set
        if (structureId == null || structureId.isEmpty()) {
            sendErrorMessage(player, "No structure ID set for this controller.");
            return InteractionResult.SUCCESS;
        }

        // Make sure the referenced structure exists
        Optional<Structure> structureOpt = StructureManager.getStructure(structureId);
        if (structureOpt.isEmpty()) {
            sendErrorMessage(player, "Structure definition not found: " + structureId);
            return InteractionResult.SUCCESS;
        }

        // Check if the structure is intact, proceed accordingly
        List<StructureError> errors = IncrementalStructureValidator.computeValidationErrors(level, pos, controllerBE);
        if (errors.isEmpty()) {
            handleValidationSuccess(player, controllerBE);
        } else {
            handleValidationFailure(player, structureId, errors);
        }

        return InteractionResult.SUCCESS;
    }




    /**
     * Opens the controller's GUI.
     * This should be called after the player interacts with a valid structure.
     * @param player The player.
     * @param controllerBE The controller block entity.
     */
    private void handleValidationSuccess(Player player, AbstractControllerBlockEntity controllerBE) {
        openGui(player, controllerBE);
    }




    /**
     * Shows a list of missing and mismatched blocks to the player and displays the expected structure as a hologram.
     * <p>
     * This should be called afte rthe player interacts with a valid but incomplete structure.
     * @param player The player.
     * @param structureId The ID of the structure.
     * @param errors A list of errors, one for each incorrect block.
     */
    private void handleValidationFailure(Player player, String structureId, List<StructureError> errors) {
        sendHighlightPacket(player, errors);
        MutableComponent msg = buildFailureMessage(structureId, errors);
        player.displayClientMessage(msg, false);
    }




    /**
     * Shows the player the incorrect blocks as holograms.
     * @param player The player.
     * @param errors A list of errors, one for each incorrect block.
     */
    private void sendHighlightPacket(Player player, List<StructureError> errors) {
        if (player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new StructureHighlightPayload(errors));
        }
    }




    /**
     * Shows an error message to the player.
     * @param player The player.
     * @param message The message to show.
     */
    private void sendErrorMessage(Player player, String message) {
        player.displayClientMessage(Component.literal(message).withStyle(ChatFormatting.RED), false);
    }




    /**
     * Creates the error message for an incomplete structure based on the provided errors.
     * @param structureId The ID of the structure.
     * @param errors A list of errors, one for each incorrect block.
     * @return The generated error message.
     */
    private MutableComponent buildFailureMessage(String structureId, List<StructureError> errors) {
        String titleCaseName = formatStructureName(structureId);

        MutableComponent msg = Component
            .literal("Structure ").withStyle(ChatFormatting.RED)
            .append(Component.literal(titleCaseName).withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" incomplete.").withStyle(ChatFormatting.RED))
        ;

        appendMissingBlocks(msg, errors);
        appendMismatchedStates(msg, errors);

        return msg;
    }




    private String formatStructureName(String structureId) {
        StringBuilder nameBuilder = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : structureId.toCharArray()) {
            if (c == '_') {
                nameBuilder.append(' ');
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    nameBuilder.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    nameBuilder.append(c);
                }
            }
        }
        return nameBuilder.toString();
    }




    private void appendMissingBlocks(MutableComponent msg, List<StructureError> errors) {
        Map<String, Integer> missingCounts = new HashMap<>();
        for (StructureError error : errors) {
            if (error.type == StructureError.ErrorType.MISSING) {
                missingCounts.merge(error.expectedBlockId, 1, Integer::sum);
            }
        }

        if (!missingCounts.isEmpty()) {
            msg.append(Component.literal("\n\nMissing Blocks:").withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE));

            for (Map.Entry<String, Integer> entry : missingCounts.entrySet()) {
                addErrorLine(msg, entry.getValue(), getBlockComponent(entry.getKey()));
            }
        }
    }




    private void appendMismatchedStates(MutableComponent msg, List<StructureError> errors) {
        Map<String, Map<String, Integer>> wrongStateCounts = new HashMap<>();

        for (StructureError error : errors) {
            if (error.type != StructureError.ErrorType.MISSING) {
                String stateDesc = formatStateDescription(error.expectedState);
                wrongStateCounts.computeIfAbsent(error.expectedBlockId, k -> new HashMap<>()).merge(stateDesc, 1, Integer::sum);
            }
        }

        if (!wrongStateCounts.isEmpty()) {
            msg.append(Component.literal("\n\nIncorrect States:").withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE));

            for (Map.Entry<String, Map<String, Integer>> blockEntry : wrongStateCounts.entrySet()) {
                MutableComponent blockComponent = getBlockComponent(blockEntry.getKey());
                for (Map.Entry<String, Integer> stateEntry : blockEntry.getValue().entrySet()) {
                    addErrorLine(msg, stateEntry.getValue(), blockComponent.copy());
                    if (!stateEntry.getKey().isEmpty()) {
                        msg.append(Component.literal(" (Expected: " + stateEntry.getKey() + ")").withStyle(ChatFormatting.DARK_GRAY,ChatFormatting.ITALIC));
                    }
                }
            }
        }
    }




    private String formatStateDescription(Map<String, String> expectedState) {
        if (expectedState == null || expectedState.isEmpty()) {
            return "";
        }
        List<String> props = new ArrayList<>(expectedState.keySet());
        Collections.sort(props);
        StringBuilder sb = new StringBuilder();
        for (String key : props) {
            if (sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(key).append("=").append(expectedState.get(key));
        }
        return sb.toString();
    }




    private void addErrorLine(MutableComponent msg, int count, MutableComponent content) {
        msg.append(Component.literal("\n • ").withStyle(ChatFormatting.RED));
        msg.append(Component.literal(count + "x ").withStyle(ChatFormatting.GOLD));
        msg.append(content.withStyle(ChatFormatting.GRAY));
    }




    @SuppressWarnings("java:S7467")
    private MutableComponent getBlockComponent(String blockName) {
        try {
            Identifier id = Identifier.parse(blockName);
            Optional<Holder.Reference<Block>> blockHolderOpt = BuiltInRegistries.BLOCK.get(id);

            if (blockHolderOpt.isPresent()) {
                return Component.translatable(blockHolderOpt.get().value().getDescriptionId());
            }
        }
        catch (Exception e) {
            // ignore
        }
        return Component.literal(blockName);
    }

    protected abstract void openGui(Player player, AbstractControllerBlockEntity be);
}

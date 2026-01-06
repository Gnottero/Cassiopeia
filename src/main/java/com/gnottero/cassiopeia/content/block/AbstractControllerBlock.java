package com.gnottero.cassiopeia.content.block;

import com.gnottero.cassiopeia.content.block.entity.AbstractControllerBlockEntity;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Map;

public abstract class AbstractControllerBlock extends BaseEntityBlock {

    protected AbstractControllerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected abstract MapCodec<? extends BaseEntityBlock> codec();

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,
                context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    protected @NotNull InteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state,
            @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand,
            @NotNull BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof AbstractControllerBlockEntity controllerBE) {
            String structureId = controllerBE.getStructureId();
            if (structureId != null && !structureId.isEmpty()) {
                java.util.Optional<com.gnottero.cassiopeia.structures.Structure> structureOpt = com.gnottero.cassiopeia.structures.StructureManager
                        .getStructure(structureId);
                if (structureOpt.isPresent()) {
                    com.gnottero.cassiopeia.structures.Structure structure = structureOpt.get();

                    List<com.gnottero.cassiopeia.structures.Structure.StructureError> errors = structure
                            .getValidationErrors(level, pos);

                    if (errors.isEmpty()) {
                        // Structure is valid
                        openGui(player, controllerBE);
                        return InteractionResult.SUCCESS;
                    } else {
                        // Structure incomplete

                        // Send highlight packet
                        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(serverPlayer,
                                    new com.gnottero.cassiopeia.network.StructureHighlightPayload(errors));
                        }

                        // Title Case Name
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
                        String titleCaseName = nameBuilder.toString();

                        net.minecraft.network.chat.MutableComponent msg = net.minecraft.network.chat.Component
                                .literal("Structure ").withStyle(net.minecraft.ChatFormatting.RED)
                                .append(net.minecraft.network.chat.Component.literal(titleCaseName)
                                        .withStyle(net.minecraft.ChatFormatting.GOLD))
                                .append(net.minecraft.network.chat.Component.literal(" incomplete.")
                                        .withStyle(net.minecraft.ChatFormatting.RED));

                        // 1. Process Errors for Chat
                        Map<String, Integer> missingCounts = new java.util.HashMap<>();
                        // For wrong state: group by Block ID -> Map of (Description -> Count)
                        // Description could be "facing=north, powered=true"
                        Map<String, Map<String, Integer>> wrongStateCounts = new java.util.HashMap<>();

                        for (com.gnottero.cassiopeia.structures.Structure.StructureError error : errors) {
                            if (error.type == com.gnottero.cassiopeia.structures.Structure.StructureError.ErrorType.MISSING) {
                                missingCounts.put(error.expectedBlockId,
                                        missingCounts.getOrDefault(error.expectedBlockId, 0) + 1);
                            } else {
                                String stateDesc = "";
                                if (error.expectedState != null && !error.expectedState.isEmpty()) {
                                    List<String> props = new java.util.ArrayList<>(error.expectedState.keySet());
                                    // Sort for consistency
                                    java.util.Collections.sort(props);
                                    StringBuilder sb = new StringBuilder();
                                    for (String key : props) {
                                        if (sb.length() > 0)
                                            sb.append(", ");
                                        sb.append(key).append("=").append(error.expectedState.get(key));
                                    }
                                    stateDesc = sb.toString();
                                }

                                wrongStateCounts.computeIfAbsent(error.expectedBlockId, k -> new java.util.HashMap<>())
                                        .merge(stateDesc, 1, Integer::sum);
                            }
                        }

                        // 2. Report Missing Blocks
                        if (!missingCounts.isEmpty()) {
                            msg.append(net.minecraft.network.chat.Component.literal("\n\nMissing Blocks:")
                                    .withStyle(net.minecraft.ChatFormatting.RED,
                                            net.minecraft.ChatFormatting.UNDERLINE));

                            for (java.util.Map.Entry<String, Integer> entry : missingCounts.entrySet()) {
                                String blockName = entry.getKey();
                                net.minecraft.network.chat.MutableComponent blockComponent = getBlockComponent(
                                        blockName);

                                msg.append(net.minecraft.network.chat.Component.literal("\n • ")
                                        .withStyle(net.minecraft.ChatFormatting.RED));
                                msg.append(net.minecraft.network.chat.Component.literal(entry.getValue() + "x ")
                                        .withStyle(net.minecraft.ChatFormatting.GOLD));
                                msg.append(blockComponent.withStyle(net.minecraft.ChatFormatting.GRAY));
                            }
                        }

                        // 3. Report Mismatched States
                        if (!wrongStateCounts.isEmpty()) {
                            msg.append(net.minecraft.network.chat.Component.literal("\n\nIncorrect States:")
                                    .withStyle(net.minecraft.ChatFormatting.RED,
                                            net.minecraft.ChatFormatting.UNDERLINE));

                            for (java.util.Map.Entry<String, Map<String, Integer>> blockEntry : wrongStateCounts
                                    .entrySet()) {
                                String blockName = blockEntry.getKey();
                                net.minecraft.network.chat.MutableComponent blockComponent = getBlockComponent(
                                        blockName);

                                for (java.util.Map.Entry<String, Integer> stateEntry : blockEntry.getValue()
                                        .entrySet()) {
                                    msg.append(net.minecraft.network.chat.Component.literal("\n • ")
                                            .withStyle(net.minecraft.ChatFormatting.RED));
                                    msg.append(
                                            net.minecraft.network.chat.Component.literal(stateEntry.getValue() + "x ")
                                                    .withStyle(net.minecraft.ChatFormatting.GOLD));
                                    msg.append(blockComponent.copy().withStyle(net.minecraft.ChatFormatting.GRAY));

                                    if (!stateEntry.getKey().isEmpty()) {
                                        msg.append(net.minecraft.network.chat.Component
                                                .literal(" (Expected: " + stateEntry.getKey() + ")")
                                                .withStyle(net.minecraft.ChatFormatting.DARK_GRAY,
                                                        net.minecraft.ChatFormatting.ITALIC));
                                    }
                                }
                            }
                        }

                        player.displayClientMessage(msg, false);
                        return InteractionResult.SUCCESS;
                    }

                } else {
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component
                                    .literal("Structure definition not found: " + structureId)
                                    .withStyle(net.minecraft.ChatFormatting.RED),
                            false);
                    return InteractionResult.SUCCESS;
                }
            } else {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("No structure ID set for this controller.")
                                .withStyle(net.minecraft.ChatFormatting.RED),
                        false);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private net.minecraft.network.chat.MutableComponent getBlockComponent(String blockName) {
        try {
            java.util.Optional<net.minecraft.core.Holder.Reference<Block>> blockHolder = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                    .get(net.minecraft.resources.Identifier.parse(blockName));
            if (blockHolder.isPresent()) {
                return net.minecraft.network.chat.Component
                        .translatable(blockHolder.get().value().getDescriptionId());
            }
        } catch (Exception e) {
            // ignore
        }
        return net.minecraft.network.chat.Component.literal(blockName);
    }

    protected abstract void openGui(Player player, AbstractControllerBlockEntity be);
}

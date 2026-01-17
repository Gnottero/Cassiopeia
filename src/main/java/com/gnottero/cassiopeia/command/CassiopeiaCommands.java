package com.gnottero.cassiopeia.command;

import com.gnottero.cassiopeia.structures.InvalidStructureException;
import com.gnottero.cassiopeia.structures.Structure;
import com.gnottero.cassiopeia.structures.StructureManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;




public class CassiopeiaCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
        CommandBuildContext registryAccess,
        Commands.CommandSelection selection
    ) {
        dispatcher.register(Commands.literal("cassiopeia")
            .then(Commands.literal("save")
                .then(Commands.argument("from", BlockPosArgument.blockPos())
                    .then(Commands.argument("to", BlockPosArgument.blockPos())
                        .then(Commands.argument("identifier", StringArgumentType.word())
                            .executes(ctx -> executeSave(ctx, false))
                            .then(Commands.argument("keep_air", BoolArgumentType.bool())
                                .executes(ctx -> executeSave(ctx, BoolArgumentType.getBool(ctx, "keep_air")))
                            )
                        )
                    )
                )
            )
            .then(Commands.literal("verify")
                .then(Commands.argument("controller", BlockPosArgument.blockPos())
                    .then(Commands.argument("identifier", StringArgumentType.word())
                        .suggests(SUGGEST_STRUCTURES)
                        .executes(CassiopeiaCommands::executeVerify)
                    )
                )
            )
        );
    }




    private static final com.mojang.brigadier.suggestion.SuggestionProvider<CommandSourceStack> SUGGEST_STRUCTURES = (ctx, builder) -> {
        for (String s : StructureManager.getAvailableStructures()) {
            builder.suggest(s);
        }
        return builder.buildFuture();
    };




    private static int executeSave(CommandContext<CommandSourceStack> ctx, boolean keepAir) throws CommandSyntaxException {
        BlockPos from = BlockPosArgument.getLoadedBlockPos(ctx, "from");
        BlockPos to = BlockPosArgument.getLoadedBlockPos(ctx, "to");
        String identifier = StringArgumentType.getString(ctx, "identifier");

        try {
            StructureManager.saveStructure(ctx.getSource().getLevel(), from, to, identifier, keepAir);
            ctx.getSource().sendSuccess(
                () -> Component.translatable("command.cassiopeia.structure.saved", identifier)
                .withStyle(net.minecraft.ChatFormatting.GREEN),
            false);
            return 1;
        }
        catch (InvalidStructureException e) {
            ctx.getSource().sendFailure(Component.translatable("command.cassiopeia.structure.save.failed", e.getMessage()));
            return 0;
        }
        catch (Exception e) {
            ctx.getSource().sendFailure(Component.translatable("command.cassiopeia.structure.save.failed", e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }




    private static int executeVerify(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        BlockPos controller = BlockPosArgument.getLoadedBlockPos(ctx, "controller");
        String identifier = StringArgumentType.getString(ctx, "identifier");

        java.util.Optional<Structure> structureOpt = StructureManager.getStructure(identifier);

        if (structureOpt.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("command.cassiopeia.structure.not_found", identifier));
            return 0;
        }

        boolean matches = structureOpt.get().verify(ctx.getSource().getLevel(), controller);

        if (matches) {
            ctx.getSource().sendSuccess(
                () -> Component.translatable("command.cassiopeia.structure.verified")
                .withStyle(net.minecraft.ChatFormatting.GREEN),
            false);
            return 1;
        } else {
            ctx.getSource().sendSuccess(
                () -> Component.translatable("command.cassiopeia.structure.mismatch")
                .withStyle(net.minecraft.ChatFormatting.RED),
            false);
            return 0;
        }
    }
}

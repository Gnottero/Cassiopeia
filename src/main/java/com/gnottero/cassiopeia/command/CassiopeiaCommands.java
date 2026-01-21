package com.gnottero.cassiopeia.command;

import java.util.Optional;

import com.gnottero.cassiopeia.structures.InvalidStructureException;
import com.gnottero.cassiopeia.structures.Structure;
import com.gnottero.cassiopeia.structures.StructureManager;
import com.gnottero.cassiopeia.structures.IncrementalStructureValidator;
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
    private CassiopeiaCommands() {}




    @SuppressWarnings("java:S1172") // Unused parameters
    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher,
        final CommandBuildContext registryAccess,
        final Commands.CommandSelection selection
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
        for(final String s : StructureManager.getAvailableStructures()) {
            builder.suggest(s);
        }
        return builder.buildFuture();
    };




    /**
     * Executes the save command.
     * @param ctx The command context.
     * @param keepAir Whether to keep air blocks in the structure or ignore them.
     * @return 1 if the command succeeded, 0 otherwise.
     */
    private static int executeSave(final CommandContext<CommandSourceStack> ctx, final boolean keepAir) throws CommandSyntaxException {
        final BlockPos from = BlockPosArgument.getLoadedBlockPos(ctx, "from");
        final BlockPos to = BlockPosArgument.getLoadedBlockPos(ctx, "to");
        final String identifier = StringArgumentType.getString(ctx, "identifier");

        try {
            StructureManager.saveStructure(ctx.getSource().getLevel(), from, to, identifier, keepAir);
            ctx.getSource().sendSuccess(
                () -> Component.translatable("command.cassiopeia.structure.saved", identifier)
                .withStyle(net.minecraft.ChatFormatting.GREEN),
            false);
            return 1;
        }
        catch(final InvalidStructureException e) {
            ctx.getSource().sendFailure(Component.translatable("command.cassiopeia.structure.save.failed", e.getMessage()));
            return 0;
        }
        catch(final Exception e) {
            ctx.getSource().sendFailure(Component.translatable("command.cassiopeia.structure.save.failed", e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }




    /**
     * Executes the verify command.
     * @param ctx The command context.
     * @return 1 if the command succeeded, 0 otherwise.
     */
    private static int executeVerify(final CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final BlockPos controller = BlockPosArgument.getLoadedBlockPos(ctx, "controller");
        final String identifier = StringArgumentType.getString(ctx, "identifier");


        // Check if the structure exists
        final Optional<Structure> structureOpt = StructureManager.getStructure(identifier);
        if(structureOpt.isPresent()) {
            ctx.getSource().sendFailure(Component.translatable("command.cassiopeia.structure.not_found", identifier));
            return 0;
        }


        // If the structure is valid, send the player the success message
        final boolean matches = IncrementalStructureValidator.validateStructure(ctx.getSource().getLevel(), controller);
        if(matches) {
            ctx.getSource().sendSuccess(
                () -> Component.translatable("command.cassiopeia.structure.verified")
                .withStyle(net.minecraft.ChatFormatting.GREEN),
            false);
            return 1;
        }


        // If the structure is not valid, send the player an error
        else {
            ctx.getSource().sendSuccess(
                () -> Component.translatable("command.cassiopeia.structure.mismatch")
                .withStyle(net.minecraft.ChatFormatting.RED),
            false);
            return 0;
        }
    }
}

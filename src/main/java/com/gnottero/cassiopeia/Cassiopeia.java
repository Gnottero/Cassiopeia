package com.gnottero.cassiopeia;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import com.gnottero.cassiopeia.command.CassiopeiaCommands;
import com.gnottero.cassiopeia.content.block.ModBlocks;
import com.gnottero.cassiopeia.content.block.entity.ModBlockEntities;
import com.gnottero.cassiopeia.content.item.ModItemGroups;
import com.gnottero.cassiopeia.content.item.ModItems;
import com.gnottero.cassiopeia.content.machine.MachineHandlerRegistry;
import com.gnottero.cassiopeia.content.recipe.ModRecipes;
import com.gnottero.cassiopeia.content.screen.ModScreenHandlers;
import com.gnottero.cassiopeia.network.StructureHighlightPayload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class Cassiopeia implements ModInitializer {
    public static final String MOD_ID = "cassiopeia";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // ModRegistry.registerCreativeTabs();

        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(
            StructureHighlightPayload.TYPE,
            StructureHighlightPayload.STREAM_CODEC
        );

        LOGGER.info("Cassiopeia initialized!");
        CommandRegistrationCallback.EVENT.register(CassiopeiaCommands::register);
        ModItemGroups.registerItemGroups();
        ModItems.registerModItems();
        ModBlocks.registerModBlocks();
        ModBlockEntities.registerModBlockEntities();
        ModScreenHandlers.registerScreenHandlers();
        ModRecipes.registerRecipes();
        MachineHandlerRegistry.init();
    }
}
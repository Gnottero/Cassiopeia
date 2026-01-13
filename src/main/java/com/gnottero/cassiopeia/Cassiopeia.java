package com.gnottero.cassiopeia;

import net.fabricmc.api.ModInitializer;

import com.gnottero.cassiopeia.command.CassiopeiaCommands;
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
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// ModRegistry.registerCreativeTabs();

		net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(
				com.gnottero.cassiopeia.network.StructureHighlightPayload.TYPE,
				com.gnottero.cassiopeia.network.StructureHighlightPayload.STREAM_CODEC);

		LOGGER.info("Cassiopeia initialized!");
		net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register(CassiopeiaCommands::register);
		com.gnottero.cassiopeia.content.item.ModItemGroups.registerItemGroups();
		com.gnottero.cassiopeia.content.item.ModItems.registerModItems();
		com.gnottero.cassiopeia.content.block.ModBlocks.registerModBlocks();
		com.gnottero.cassiopeia.content.block.entity.ModBlockEntities.registerModBlockEntities();
		com.gnottero.cassiopeia.content.screen.ModScreenHandlers.registerScreenHandlers();
		com.gnottero.cassiopeia.content.recipe.ModRecipes.registerRecipes();
		com.gnottero.cassiopeia.content.machine.MachineHandlerRegistry.init();
	}
}
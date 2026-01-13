package com.gnottero.cassiopeia.content.item;

import com.gnottero.cassiopeia.Cassiopeia;
import com.gnottero.cassiopeia.content.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModItemGroups {
    public static final CreativeModeTab CASSIOPEIA_GROUP = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "general"),
            FabricItemGroup.builder()
                    .title(Component.literal("Cassiopeia"))
                    .icon(() -> new ItemStack(ModBlocks.BASIC_CONTROLLER))
                    .displayItems((displayContext, entries) -> {
                        entries.accept(ModBlocks.BASIC_CONTROLLER);
                    })
                    .build());

    public static void registerItemGroups() {
        Cassiopeia.LOGGER.info("Registering Item Groups for " + Cassiopeia.MOD_ID);
    }
}

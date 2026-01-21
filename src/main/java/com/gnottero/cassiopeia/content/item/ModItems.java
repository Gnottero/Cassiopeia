package com.gnottero.cassiopeia.content.item;

import com.gnottero.cassiopeia.Cassiopeia;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;




public class ModItems {
    private ModItems() {}

    private static Item registerItem(final String name, final Item item) {
        return Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, name),
            item
        );
    }

    public static void registerModItems() {
        Cassiopeia.LOGGER.info("Registering ModItems for " + Cassiopeia.MOD_ID);
    }
}

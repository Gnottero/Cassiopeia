package com.gnottero.cassiopeia.content.screen;

import com.gnottero.cassiopeia.Cassiopeia;
import com.gnottero.cassiopeia.content.menu.AlloyKilnMenu;
import com.gnottero.cassiopeia.content.menu.CrusherMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;

public class ModScreenHandlers {
    private static final StreamCodec<FriendlyByteBuf, BlockPos> BLOCK_POS_CODEC = StreamCodec.of(
            (buf, pos) -> buf.writeBlockPos(pos),
            buf -> buf.readBlockPos());

    public static final MenuType<CrusherMenu> CRUSHER = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "crusher"),
            new ExtendedScreenHandlerType<>(CrusherMenu::new, BLOCK_POS_CODEC));

    public static final MenuType<AlloyKilnMenu> ALLOY_KILN = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "alloy_kiln"),
            new ExtendedScreenHandlerType<>(AlloyKilnMenu::new, BLOCK_POS_CODEC));

    public static void registerScreenHandlers() {
        Cassiopeia.LOGGER.info("Registering Screen Handlers for " + Cassiopeia.MOD_ID);
    }
}

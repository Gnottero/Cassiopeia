package com.gnottero.cassiopeia.client;

import com.gnottero.cassiopeia.client.screen.CrusherScreen;
import com.gnottero.cassiopeia.client.screen.UnpackagerScreen;
import com.gnottero.cassiopeia.content.ModRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.MenuScreens;

@Environment(EnvType.CLIENT)
public class CassiopeiaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        StructureHighlightRenderer.init();

        // Register screens
        MenuScreens.register(ModRegistry.CRUSHER_MENU, CrusherScreen::new);
        MenuScreens.register(ModRegistry.UNPACKAGER_MENU, UnpackagerScreen::new);
    }
}

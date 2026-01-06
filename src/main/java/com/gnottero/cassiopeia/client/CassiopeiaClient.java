package com.gnottero.cassiopeia.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class CassiopeiaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        StructureHighlightRenderer.init();
    }
}

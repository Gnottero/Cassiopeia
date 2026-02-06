package com.gnottero.cassiopeia.content.block.entity;

import com.gnottero.cassiopeia.Cassiopeia;
import com.gnottero.cassiopeia.content.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    private ModBlockEntities() {
    }

    public static final BlockEntityType<BasicControllerBlockEntity> BASIC_CONTROLLER = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "basic_controller"),
            FabricBlockEntityTypeBuilder.create(BasicControllerBlockEntity::new, ModBlocks.BASIC_CONTROLLER).build());

    public static final BlockEntityType<InputHatchBlockEntity> INPUT_HATCH = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "input_hatch"),
            FabricBlockEntityTypeBuilder.create(InputHatchBlockEntity::new, ModBlocks.INPUT_HATCH).build());

    public static final BlockEntityType<OutputHatchBlockEntity> OUTPUT_HATCH = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Cassiopeia.MOD_ID, "output_hatch"),
            FabricBlockEntityTypeBuilder.create(OutputHatchBlockEntity::new, ModBlocks.OUTPUT_HATCH).build());

    public static void registerModBlockEntities() {
        Cassiopeia.LOGGER.info("Registering Block Entities for " + Cassiopeia.MOD_ID);
    }
}

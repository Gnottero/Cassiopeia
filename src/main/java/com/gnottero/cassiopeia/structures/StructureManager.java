package com.gnottero.cassiopeia.structures;

import com.gnottero.cassiopeia.content.block.ModBlocks;
import com.gnottero.cassiopeia.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;




public class StructureManager {
    private StructureManager() {}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, Structure> CACHE = new HashMap<>();
    private static final File STRUCTURE_DIR = FabricLoader.getInstance().getConfigDir().resolve("cassiopeia/structures").toFile();

    static {
        if(!STRUCTURE_DIR.exists()) {
            STRUCTURE_DIR.mkdirs();
        }
    }




    /**
     * Scans the specified area and saves it as a structure.
     * @param level //TODO
     * @param from //TODO
     * @param to //TODO
     * @param identifier //TODO
     * @param keepAir //TODO
     * @throws InvalidStructureException if the structure doesn't contain a controller or it contains more than one.
     */
    @SuppressWarnings("java:S1119")
    public static void saveStructure(final Level level, final BlockPos from, final BlockPos to, final String identifier, final boolean keepAir) throws InvalidStructureException {

        // Find bounding box
        final int minX = Math.min(from.getX(), to.getX());
        final int minY = Math.min(from.getY(), to.getY());
        final int minZ = Math.min(from.getZ(), to.getZ());
        final int maxX = Math.max(from.getX(), to.getX());
        final int maxY = Math.max(from.getY(), to.getY());
        final int maxZ = Math.max(from.getZ(), to.getZ());

        // Find controller block
        BlockPos controllerPos = null;
        BlockState controllerState = null;
        controllerSearch:
        for(int x = minX; x <= maxX; x++) {
            for(int y = minY; y <= maxY; y++) {
                for(int z = minZ; z <= maxZ; z++) {
                    final BlockPos pos = new BlockPos(x, y, z);
                    final var state = level.getBlockState(pos);
                    if(state.getBlock() == ModBlocks.BASIC_CONTROLLER) {
                        controllerPos = pos;
                        controllerState = state;
                        break controllerSearch;
                    }
                }
            }
        }
        if(controllerState == null) {
            throw new InvalidStructureException("The structure doesn't contain any controller");
        }


        final Property<?> facingProp = controllerState.getBlock().getStateDefinition().getProperty("facing");
        final Object facingVal = (facingProp != null) ? controllerState.getValue(facingProp) : null;

        if(!(facingVal instanceof Direction)) {
            throw new RuntimeException("Controller block must have a facing property");
        }

        // For each block
        final Direction controllerFacing = (Direction) facingVal;
        final Structure structure = new Structure();
        final String controllerId = BuiltInRegistries.BLOCK.getKey(controllerState.getBlock()).toString();
        structure.setController(controllerId);
        for(int x = minX; x <= maxX; x++) {
            for(int y = minY; y <= maxY; y++) {
                for(int z = minZ; z <= maxZ; z++) {

                    // Skip controller
                    final BlockPos pos = new BlockPos(x, y, z);
                    final BlockState state = level.getBlockState(pos);
                    if(state.getBlock() == ModBlocks.BASIC_CONTROLLER && !pos.equals(controllerPos)) {
                        throw new InvalidStructureException("The structure contains more than one controller");
                    }

                    // Skip air if required
                    final Vector3i offset = Utils.globalToLocal(pos, controllerPos, controllerFacing);
                    if(state.isAir() && !keepAir) {
                        structure.addBlock(new Structure.BlockEntry(offset));
                    }

                    // Add the block to the structure otherwise
                    else {
                        final String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
                        final Map<String, String> properties = Utils.processBlockProperties(state, controllerFacing);
                        structure.addBlock(new Structure.BlockEntry(blockId, offset, properties));
                    }
                }
            }
        }

        // Save to file
        final File file = new File(STRUCTURE_DIR, identifier + ".json");
        writeStructureToFile(structure, file);

        // Update cache and refresh validation structures of all active controllers
        IncrementalStructureValidator.unregisterMatching(identifier);
        CACHE.put(identifier, structure);
    }








    public static @NotNull Optional<Structure> getStructure(@NotNull final String identifier) {
        if(CACHE.containsKey(identifier)) {
            return Optional.of(CACHE.get(identifier));
        }

        final File file = new File(STRUCTURE_DIR, identifier + ".json");
        if(!file.exists()) {
            return Optional.empty();
        }

        try(Reader reader = new FileReader(file)) {
            final Structure structure = GSON.fromJson(reader, Structure.class);
            if(structure != null) {
                CACHE.put(identifier, structure);
                return Optional.of(structure);
            }
        } catch(final IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }




    private static void writeStructureToFile(final Structure structure, final File file) {
        try(Writer writer = new FileWriter(file)) {
            GSON.toJson(structure, writer);
        } catch(final IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save structure file", e);
        }
    }




    @NotNull
    public static Set<String> getAvailableStructures() {
        if(!STRUCTURE_DIR.exists()) {
            return Collections.emptySet();
        }

        final File[] files = STRUCTURE_DIR.listFiles((dir, name) -> name.endsWith(".json"));
        if(files == null) {
            return Collections.emptySet();
        }

        final Set<String> structures = new HashSet<>();
        for(final File file : files) {
            final String name = file.getName();
            structures.add(name.substring(0, name.length() - 5));
        }
        return structures;
    }

    public static void clearCache() {
        CACHE.clear();
    }
}

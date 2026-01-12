package com.gnottero.cassiopeia.structures;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class StructureManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, Structure> CACHE = new HashMap<>();
    private static final File STRUCTURE_DIR = FabricLoader.getInstance().getConfigDir().resolve("cassiopeia/structures")
            .toFile();

    static {
        if (!STRUCTURE_DIR.exists()) {
            STRUCTURE_DIR.mkdirs();
        }
    }

    public static void saveStructure(Level level, BlockPos from, BlockPos to, BlockPos controller, String identifier,
            boolean keepAir) {
        BlockState controllerState = level.getBlockState(Objects.requireNonNull(controller));

        Property<?> facingProp = controllerState.getBlock().getStateDefinition().getProperty("facing");
        Object facingVal = (facingProp != null) ? controllerState.getValue(facingProp) : null;

        if (!(facingVal instanceof Direction)) {
            throw new IllegalArgumentException("Controller block must have a facing property");
        }

        Direction controllerFacing = (Direction) facingVal;

        // Coordinate system basis
        BlockUtils.Basis basis = BlockUtils.getBasis(controllerFacing);
        Vec3 front = basis.front();
        Vec3 up = basis.up();
        Vec3 right = basis.right();

        // Bounding box iteration
        int minX = Math.min(from.getX(), to.getX());
        int minY = Math.min(from.getY(), to.getY());
        int minZ = Math.min(from.getZ(), to.getZ());
        int maxX = Math.max(from.getX(), to.getX());
        int maxY = Math.max(from.getY(), to.getY());
        int maxZ = Math.max(from.getZ(), to.getZ());

        Structure structure = new Structure();
        String controllerId = BuiltInRegistries.BLOCK.getKey(controllerState.getBlock()).toString();
        structure.setController(controllerId);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);

                    if (state.isAir() && !keepAir) {
                        continue;
                    }

                    // Calculate relative offset
                    Vec3 delta = new Vec3(x - controller.getX(), y - controller.getY(), z - controller.getZ());

                    double offFront = delta.dot(front);
                    double offUp = delta.dot(up);
                    double offRight = delta.dot(right);

                    List<Double> offset = List.of(offFront, offUp, offRight);

                    String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
                    Map<String, String> properties = BlockUtils.processBlockProperties(state, controllerFacing);

                    structure.addBlock(new Structure.BlockEntry(blockId, offset, properties));
                }
            }
        }

        // Save to file
        File file = new File(STRUCTURE_DIR, identifier + ".json");
        writeStructureToFile(structure, file);
        CACHE.remove(identifier); // Validate cache
    }

    @NotNull
    public static Optional<Structure> getStructure(@NotNull String identifier) {
        if (CACHE.containsKey(identifier)) {
            return Optional.ofNullable(CACHE.get(identifier));
        }

        File file = new File(STRUCTURE_DIR, identifier + ".json");
        if (!file.exists()) {
            createDefaultIfKnown(identifier, file);
        }

        if (!file.exists()) {
            return Optional.empty();
        }

        try (Reader reader = new FileReader(file)) {
            Structure structure = GSON.fromJson(reader, Structure.class);
            if (structure != null) {
                CACHE.put(identifier, structure);
                return Optional.of(structure);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private static void createDefaultIfKnown(String identifier, File file) {
        if (!identifier.equals("crusher")) {
            return;
        }

        String controllerId = "cassiopeia:basic_controller";
        Structure structure = new Structure();
        structure.setController(controllerId);

        List<Double> offset = List.of(0.0, 0.0, 0.0);

        // Add controller itself as the single block
        // Note: properties map can be empty to match any properties
        structure.addBlock(new Structure.BlockEntry(controllerId, offset, new java.util.HashMap<>()));

        writeStructureToFile(structure, file);
    }

    private static void writeStructureToFile(Structure structure, File file) {
        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(structure, writer);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save structure file", e);
        }
    }

    @NotNull
    public static Set<String> getAvailableStructures() {
        if (!STRUCTURE_DIR.exists()) {
            return Collections.emptySet();
        }

        File[] files = STRUCTURE_DIR.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) {
            return Collections.emptySet();
        }

        Set<String> structures = new HashSet<>();
        for (File file : files) {
            String name = file.getName();
            structures.add(name.substring(0, name.length() - 5));
        }
        return structures;
    }

    public static void clearCache() {
        CACHE.clear();
    }
}

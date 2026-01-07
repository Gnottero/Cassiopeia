package com.gnottero.cassiopeia.content.machine;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for machine handlers.
 * Maps structure_id to MachineHandler instance.
 */
public class MachineHandlerRegistry {

    private static final Map<String, MachineHandler> HANDLERS = new HashMap<>();

    /**
     * Register a machine handler.
     */
    public static void register(MachineHandler handler) {
        HANDLERS.put(handler.getStructureId(), handler);
    }

    /**
     * Get a handler for the given structure_id.
     */
    public static Optional<MachineHandler> getHandler(String structureId) {
        return Optional.ofNullable(HANDLERS.get(structureId));
    }

    /**
     * Check if a handler exists for the given structure_id.
     */
    public static boolean hasHandler(String structureId) {
        return HANDLERS.containsKey(structureId);
    }

    /**
     * Initialize all machine handlers.
     * Called during mod initialization.
     */
    public static void init() {
        register(new CrusherMachineHandler());
        register(new UnpackagerMachineHandler());
    }
}

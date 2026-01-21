package com.gnottero.cassiopeia.content.machine;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;




/**
 * Registry for machine handlers.
 * Maps structure_id to MachineHandler instance.
 */
public class MachineHandlerRegistry {
    private MachineHandlerRegistry() {}
    private static final Map<String, MachineHandler> HANDLERS = new HashMap<>();

    /**
     * Register a machine handler.
     * @param handler The handler to register.
     */
    public static void register(final MachineHandler handler) {
        HANDLERS.put(handler.getStructureId(), handler);
    }

    /**
     * Retrieve a handler for the given structureId.
     * @param structureId The ID of the structure.
     * @return The handler, or an empty Optional if a handler with the provided ID doesn't exist.
     */
    public static Optional<MachineHandler> getHandler(final String structureId) {
        return Optional.ofNullable(HANDLERS.get(structureId));
    }

    /**
     * Check if a handler exists for the given structureId.
     * @param structureId The ID of the structure.
     * @return True if the handler exists, false otherwise.
     */
    public static boolean hasHandler(final String structureId) {
        return HANDLERS.containsKey(structureId);
    }

    /**
     * Initialize all machine handlers.
     * Called during mod initialization.
     */
    public static void init() {
        register(new CrusherMachineHandler());
        register(new AlloyKilnMachineHandler());
    }
}

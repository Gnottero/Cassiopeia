__config() -> {
    'scope' -> 'global',
    'commands' -> {
        '' -> _() -> print('Root command - use /add_structure for functionality'),
        'add_structure <from> <to> <controller> <identifier>' -> ['__store_structure_schema', 0],
        'add_structure <from> <to> <controller> <identifier> <keep_air>' -> '__store_structure_schema',
        'clear_cache' -> '__oberon_clear_cache_cmd',
        'give_blueprint <player> <identifier>' -> '__give_blueprint'
    },
    'arguments' -> {
        'from' -> {'type' -> 'pos', 'loaded' -> true},
        'to' -> {'type' -> 'pos', 'loaded' -> true},
        'controller' -> {'type' -> 'pos', 'loaded' -> true},
        'player' -> {'type' -> 'players', 'suggester' -> _(args) -> player('all')},
        'identifier' -> {'type' -> 'term', 'suggester' -> _(args) -> __get_structure_files()},
        'keep_air' -> {'type' -> 'bool'}
    }
};

global_facing_order = ['north', 'east', 'south', 'west'];

global_directions_to_vectors = {
    'north' -> [0, 0, -1],
    'south' -> [0, 0, 1],
    'east' -> [1, 0, 0],
    'west' -> [-1, 0, 0],
    'up' -> [0, 1, 0],
    'down' -> [0, -1, 0]
};

global_vectors_to_directions = {
    [0, 0, -1] -> 'north',
    [0, 0, 1] -> 'south',
    [1, 0, 0] -> 'east',
    [-1, 0, 0] -> 'west',
    [0, 1, 0] -> 'up',
    [0, -1, 0] -> 'down'
};

global_positions = [];
global_structure_cache = {};
global_oberon_events = ['oberon_verify_structure', 'oberon_clear_cache', 'oberon_replace_structure_block'];


__on_start() -> (
    __oberon_init_handlers();
);

oberon_verify_structure(list) -> (
    [key, controller, identifier] = list;
    system_variable_get('oberon_queries'):key = __oberon_verify_structure(controller, identifier);
);

oberon_replace_structure_block(list) -> (
    [key, controller, identifier, offset, block] = list;
    system_variable_get('oberon_queries'):key = __oberon_replace_structure_block(controller, identifier, offset, block);
);

oberon_clear_cache(list) -> (
    [key] = list;
    system_variable_get('oberon_queries'):key = __oberon_clear_cache();
);

__oberon_init_handlers() -> (
    system_variable_set('oberon_queries', {});
    for(global_oberon_events,
        handle_event(_, _);
    );
);


// Stores a structure schema to a JSON file with relative positioning
//
// * `from`: Starting corner position
// * `to`: Ending corner position
// * `controller`: Reference block position (must have 'facing' property)
// * `identifier`: Name for the saved structure file
// * `keep_air`: Whether to include air blocks
__store_structure_schema(from, to, controller, identifier, keep_air) -> (

    // Validate controller block has facing property
    if (block_state(block(controller), 'facing') == null,
        print(format(str('r [Oberon]#> Error: Controller block at %s must have a facing property', [controller])));
        return()
    );

    // Establish coordinate system based on controller orientation
    facing_direction = block_state(controller, 'facing');
    front = pos_offset(controller, facing_direction, 1) - controller;
    up = [0, 1, 0];
    right = __vec_cross(front, up);

    // Initialize structure data
    blocks = {'blocks' -> []};

    // Process each block in the volume
    volume(from, to,
        __process_block(_, controller, front, up, right, keep_air, blocks)
    );

    // Save or report error
    if (blocks:'blocks' != [],
        (
            write_file(identifier, 'json', blocks);
            print(format(str('g [Oberon]#> Structure "%s" saved successfully with %d blocks', 
                identifier, length(blocks:'blocks'))));
            
            // Clear cache entry for this identifier
            delete(global_structure_cache, identifier);
        ),
        print(format('r [Oberon]#> Error: Structure is empty or cannot be saved'));
    );
);


__oberon_replace_structure_block(controller, identifier, offset, block) -> (
    controller_facing = block_state(controller, 'facing');
    if (controller_facing == null, return(false));

    blocks_list = global_structure_cache:identifier;
    if (blocks_list == null,
        structure_data = read_file(identifier, 'json');
        if (structure_data == null, return(false));
        blocks_list = structure_data:'blocks';
        if (blocks_list == null || length(blocks_list) == 0, return(false));
        // Cache for future checks
        global_structure_cache:identifier = blocks_list;
    );

    front = pos_offset(controller, controller_facing, 1) - controller;
    up = [0, 1, 0];
    right = [
        front:1 * up:2 - front:2 * up:1,
        front:2 * up:0 - front:0 * up:2,
        front:0 * up:1 - front:1 * up:0
    ];

    is_inside = reduce(blocks_list, if (_:'offset' == offset, break(true)), false);

    if (is_inside,
        (
            world_pos = controller + (offset:0 * front) + (offset:1 * up) + (offset:2 * right);
            set(world_pos, block);
        ),
        print(format('r [Oberon]#> Cannot change block'));
    );

);


// Fast verification (returns true/false only)
//
// * `controller`: Reference block position (must have 'facing' property)
// * `identifier`: Name of the saved structure file to verify against
__oberon_verify_structure(controller, identifier) -> (
    
    // Validate controller block has facing property
    controller_facing = block_state(controller, 'facing');
    if (controller_facing == null, return(false));

    // Load from cache or file
    blocks_list = global_structure_cache:identifier;
    if (blocks_list == null,
        structure_data = read_file(identifier, 'json');
        if (structure_data == null, return(false));
        blocks_list = structure_data:'blocks';
        if (blocks_list == null || length(blocks_list) == 0, return(false));
        // Cache for future checks
        global_structure_cache:identifier = blocks_list;
    );

    // Pre-calculate coordinate system (outside loop)
    front = pos_offset(controller, controller_facing, 1) - controller;
    up = [0, 1, 0];
    right = [
        front:1 * up:2 - front:2 * up:1,
        front:2 * up:0 - front:0 * up:2,
        front:0 * up:1 - front:1 * up:0
    ];
    
    // Get controller index once for property checks
    controller_index = global_facing_order~controller_facing;

    // Fast verification loop - exit immediately on first mismatch
    for (blocks_list,
        block_data = _;
        offset = block_data:'offset';
        
        // Inline position calculation
        world_pos = controller + (offset:0 * front) + (offset:1 * up) + (offset:2 * right);
        
        // Quick block type check
        if (block(world_pos) != block_data:'block', return(false));
        
        // Check properties if they exist
        expected_properties = block_data:'properties';
        if (expected_properties != null,
            actual_states = block_state(world_pos);
            if (actual_states == null, return(false));
            
            // Inline property check loop
            for (expected_properties,
                key = _;
                expected_val = expected_properties:key;
                actual_val = actual_states:key;
                
                // Quick property check with inline facing transformation
                if (key == 'facing',
                    expected_val = global_facing_order:((expected_val + controller_index) % 4)
                );
                
                if (actual_val == null || str(actual_val) != str(expected_val),
                    return(false)
                )
            )
        )
    );
    
    return(true)
);


// Clear structure cache
__oberon_clear_cache() -> (
    global_structure_cache = {};
    return(true);
);


// Command wrapper for cache clearing
__oberon_clear_cache_cmd() -> (
    __oberon_clear_cache();
    print(format('g [Oberon]#> Structure cache cleared'));
);


__give_blueprint(players, identifier) -> (
    // Check if structure file exists
    if (!__structure_file_exists(identifier),
        print(format(str('r [Oberon]#> Error: Structure file "%s" does not exist', identifier)));
        return()
    );
    
    // Load structure data to get block count
    structure_data = read_file(identifier, 'json');
    block_count = if(structure_data != null && structure_data:'blocks' != null, 
        length(structure_data:'blocks'), 
        0
    );
    
    // Create custom paper item with NBT data
    paper_components = {
        'minecraft:custom_data' -> {
            'oberon_blueprint' -> identifier
        },
        'minecraft:item_name' -> '[' + __identifier_to_title(identifier) + '] Blueprint',
        'minecraft:unbreakable' -> {},
        'minecraft:enchantment_glint_override' -> true
    };
    
    for(players,
        empty_slot = inventory_find(_, null);

        if (empty_slot != null && (empty_slot <= 35 || empty_slot == 40),
            inventory_set(_, empty_slot, 1, 'paper', encode_nbt({'components' -> paper_components, 'id' -> str('minecraft:%s', 'paper')})),
            spawn('item', pos(_), encode_nbt({
                'Item' -> {
                    'id' -> str('minecraft:%s', 'paper'),
                    'count' -> 1,
                    'components' -> axe_components
                }
            }))
        );
        
        print(_, format(str('g [Oberon]#> Received blueprint for structure "%s"', identifier)));
    );
);


// Internal function to process individual blocks in the structure
//
// * `pos`: Block position in the volume
//
// * `controller`: Block position of the controller
//
// * `front`: Front facing vector
//
// * `up`: Upward facing vector
//
// * `right`: Right facing vector
//
// * `keep_air`: Whether to include air blocks
//
// * `blocks`: Map of all the blocks to process
__process_block(pos, controller, front, up, right, keep_air, blocks) -> (
    delta = pos(pos) - controller;
    
    // Calculate relative offset in the local coordinate system
    offset = [
        __vec_dot(delta, front),
        __vec_dot(delta, up),
        __vec_dot(delta, right)
    ];
    
    value = {
        'block' -> block(pos),
        'offset' -> offset
    };
    
    // Skip air blocks unless keep_air is enabled
    if (value:'block' ~ 'air' != null && !keep_air,
        return()
    );
    
    block_states = block_state(pos);
    if (block_states,
        value:'properties' = __process_block_properties(block_states, controller)
    );
    
    blocks:'blocks' += value;
);


// Process block properties with special handling for facing directions
//
// * `block_states`: List of all the block states for a specific block
//
// * `controller`: Block position of the controller
__process_block_properties(block_states, controller) -> (
    properties = {};
    controller_facing = block_state(block(controller), 'facing');
    
    for (block_states,
        key = _;
        val = block_states:_;
        
        // Normalize facing property relative to controller
        if (key == 'facing',
            controller_index = global_facing_order~controller_facing;
            block_facing_index = global_facing_order~val;
            val = (block_facing_index - controller_index + 4) % 4
        );
        
        properties:key = val
    );
    
    return(properties)
);


// Calculate dot product of two vectors
//
// * `a`: First vector
// * `b`: Second vector
__vec_dot(a, b) -> reduce(a * b, _a + _, 0);


// Calculate cross product of two vectors
//
// * `a`: First vector
// * `b`: Second vector
__vec_cross(a, b) -> [
    a:1 * b:2 - a:2 * b:1,
    a:2 * b:0 - a:0 * b:2,
    a:0 * b:1 - a:1 * b:0
];


__get_structure_files() -> (
    files = list_files('.', 'json');
    return(files);
);


__structure_file_exists(identifier) -> (
    files = list_files('.', 'json');
    return(files ~ identifier != null);
);


__identifier_to_title(identifier) -> (
    words = split('_', identifier);
    title_words = map(words,
        word = _;
        if (length(word) > 0,
            first_letter = upper(slice(word, 0, 1));
            rest = slice(word, 1);
            first_letter + rest
        ,
            word
        )
    );
    return(join(' ', title_words));
);
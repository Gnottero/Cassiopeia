# To do

- Fixes
  - Remove controller from save data
  - Fix incremental structure validation not working with structures that don't include air
  - check tick events in machine implementations
    - cache data where possible
    - move shared logic to the abstract machine, call super.tick() from the implementations

- Features
  - add /cassiopeia reload
    - this reads all of the structures from file (and invalidates every controller's data)
      - add an invalidateAll method to StructureValidator that first clears all of the maps' data, then invalidates every controller block entity
    - it also reloads any config options
  - add nbt matching to recipe inputs
    - this should allow counts in recipes
  - add a public structure registration system
    - allow others to create plugins without the need to code new machine handlers

# To do

- Fixes
  - Remove controller from save data
  - Fix incremental structure validation not working with structures that don't include air
  - check tick events in machine implementations
    - cache data where possible
    - move shared logic to the abstract machine, call super.tick() from the implementations

- Features
  - add nbt matching to recipe inputs
    - this should allow counts in recipes
  - add a public structure registration system
    - allow others to create plugins without the need to code new machine handlers

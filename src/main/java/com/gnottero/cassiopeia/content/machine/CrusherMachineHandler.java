package com.gnottero.cassiopeia.content.machine;

import com.gnottero.cassiopeia.content.block.entity.BasicControllerBlockEntity;
import com.gnottero.cassiopeia.content.menu.CrusherMenu;
import com.gnottero.cassiopeia.content.recipe.CrusherRecipe;
import com.gnottero.cassiopeia.content.recipe.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;




/**
 * Machine handler for the Crusher.
 * Follows vanilla AbstractFurnaceBlockEntity pattern with serverTick.
 */
public class CrusherMachineHandler implements MachineHandler {

    public static final String STRUCTURE_ID = "crusher";

    // Slot indices
    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int SLOT_COUNT = 3;

    // Data indices (matches vanilla furnace pattern)
    public static final int DATA_LIT_TIME = 0;
    public static final int DATA_LIT_DURATION = 1;
    public static final int DATA_CRUSHING_PROGRESS = 2;
    public static final int DATA_CRUSHING_TOTAL_TIME = 3;
    public static final int DATA_COUNT = 4;

    // NBT keys
    private static final String KEY_LIT_TIME = "lit_time_remaining";
    private static final String KEY_LIT_DURATION = "lit_total_time";
    private static final String KEY_CRUSHING_PROGRESS = "crushing_time_spent";
    private static final String KEY_CRUSHING_TOTAL_TIME = "crushing_total_time";

    // Hopper slot mappings
    private static final int[] SLOTS_UP = { INPUT_SLOT };
    private static final int[] SLOTS_DOWN = { OUTPUT_SLOT, FUEL_SLOT };
    private static final int[] SLOTS_SIDES = { FUEL_SLOT };




    @Override
    public String getStructureId() {
        return STRUCTURE_ID;
    }

    @Override
    public int getSlotCount() {
        return SLOT_COUNT;
    }

    @Override
    public int getDataCount() {
        return DATA_COUNT;
    }

    @Override
    public int getInputSlotIndex() {
        return INPUT_SLOT;
    }

    @Override
    public int getProcessProgressIndex() {
        return DATA_CRUSHING_PROGRESS;
    }

    @Override
    public int[] getSlotsForFace(final Direction side) {
        return switch(side) {
            case UP -> SLOTS_UP;
            case DOWN -> SLOTS_DOWN;
            default -> SLOTS_SIDES;
        };
    }

    @Override
    public boolean canPlaceItem(final BasicControllerBlockEntity be, final int slot, final ItemStack stack) {
        if(slot == OUTPUT_SLOT) {
            return false;
        }
        else if(slot == FUEL_SLOT) {
            final Level level = be.getLevel();
            return level != null && getBurnTime(level.fuelValues(), stack) > 0;
        }
        return true;
    }

    @Override
    public boolean canTakeItem(final int slot, final ItemStack stack, final Direction direction) {
        return slot == OUTPUT_SLOT;
    }




    /**
     * Server-side tick processing following vanilla furnace pattern.
     * - Fuel continues burning even without input (like vanilla)
     * - Crushing progress only advances when lit and has valid recipe
     * - Crushing progress resets when input removed or no valid recipe
     */
    @Override
    public void serverTick(final Level level, final BlockPos pos, final BlockState state, final BasicControllerBlockEntity be) {
        if(!be.verifyStructure()) {
            return;
        }

        final NonNullList<ItemStack> items = be.getMachineItems();
        final FuelValues fuelValues = level.fuelValues();

        // Read current state
        int litTime           = be.getMachineData(DATA_LIT_TIME);
        int litDuration       = be.getMachineData(DATA_LIT_DURATION);
        int crushingProgress  = be.getMachineData(DATA_CRUSHING_PROGRESS);
        int crushingTotalTime = be.getMachineData(DATA_CRUSHING_TOTAL_TIME);

        final boolean wasLit = litTime > 0; //TODO this is never used. Remove if not needed
        boolean changed = false;

        // Decrement fuel (fuel burns regardless of input - vanilla behavior)
        if(litTime > 0) {
            litTime--;
            changed = true;
        }

        final ItemStack inputStack = items.get(INPUT_SLOT);
        final ItemStack fuelStack = items.get(FUEL_SLOT);

        // Try to get recipe for current input
        final Optional<RecipeHolder<CrusherRecipe>> recipeOpt = getRecipe(level, inputStack);

        if(recipeOpt.isPresent()) {
            final RecipeHolder<CrusherRecipe> holder = recipeOpt.get();
            final CrusherRecipe recipe = holder.value();
            final int recipeCookTime = recipe.getCrushingTime();

            // Update total time if changed
            if(crushingTotalTime != recipeCookTime) {
                crushingTotalTime = recipeCookTime;
                changed = true;
            }

            // Try to consume fuel if not lit but have fuel and valid recipe
            if(litTime <= 0 && !fuelStack.isEmpty() && canProcess(items, recipe)) {
                final int burnTime = getBurnTime(fuelValues, fuelStack);
                if(burnTime > 0) {
                    litTime = burnTime;
                    litDuration = burnTime;
                    consumeFuel(items);
                    changed = true;
                }
            }

            // Process if lit and can process
            if(litTime > 0 && canProcess(items, recipe)) {
                crushingProgress++;
                if(crushingProgress >= crushingTotalTime) {
                    crushingProgress = 0;
                    process(items, recipe);
                    recordRecipeUsed(be, holder);
                }
                changed = true;
            }

            // No fuel - decay progress (vanilla behavior)
            else if(litTime <= 0 && crushingProgress > 0) {
                crushingProgress = Math.max(0, crushingProgress - 2);
                changed = true;
            }
        }

        // No valid recipe - reset crushing progress but keep fuel burning
        else if(crushingProgress > 0) {
            crushingProgress = 0;
            changed = true;
        }

        // Write state back
        be.setMachineData(DATA_LIT_TIME,            litTime);
        be.setMachineData(DATA_LIT_DURATION,        litDuration);
        be.setMachineData(DATA_CRUSHING_PROGRESS,   crushingProgress);
        be.setMachineData(DATA_CRUSHING_TOTAL_TIME, crushingTotalTime);

        if(changed) {
            be.setChanged();
        }
    }




    private Optional<RecipeHolder<CrusherRecipe>> getRecipe(final Level level, final ItemStack input) {
        if(level == null || input.isEmpty() || !(level instanceof final ServerLevel serverLevel)) {
            return Optional.empty();
        }
        final SingleRecipeInput recipeInput = new SingleRecipeInput(input);
        return serverLevel.recipeAccess().getRecipeFor(ModRecipes.CRUSHER_TYPE, recipeInput, serverLevel);
    }




    private boolean canProcess(final NonNullList<ItemStack> items, final CrusherRecipe recipe) {
        final ItemStack result = recipe.getResult();
        final ItemStack outputSlot = items.get(OUTPUT_SLOT);

        if(outputSlot.isEmpty()) {
            return true;
        }
        if(!ItemStack.isSameItemSameComponents(outputSlot, result)) {
            return false;
        }
        return outputSlot.getCount() + result.getCount() <= outputSlot.getMaxStackSize();
    }




    private void process(final NonNullList<ItemStack> items, final CrusherRecipe recipe) {
        final ItemStack inputStack = items.get(INPUT_SLOT);
        final ItemStack result = recipe.getResult().copy();
        final ItemStack outputSlot = items.get(OUTPUT_SLOT);

        if(outputSlot.isEmpty()) {
            items.set(OUTPUT_SLOT, result);
        }
        else if(ItemStack.isSameItemSameComponents(outputSlot, result)) {
            outputSlot.grow(result.getCount());
        }

        inputStack.shrink(1);
    }




    private void consumeFuel(final NonNullList<ItemStack> items) {
        final ItemStack fuelStack = items.get(FUEL_SLOT);
        final Item fuelItem = fuelStack.getItem();
        final ItemStack remainder = fuelItem.getCraftingRemainder();
        fuelStack.shrink(1);

        if(fuelStack.isEmpty()) {
            items.set(FUEL_SLOT, remainder.isEmpty() ? ItemStack.EMPTY : remainder);
        }
    }




    private void recordRecipeUsed(final BasicControllerBlockEntity be, final RecipeHolder<CrusherRecipe> holder) {
        String idStr = holder.id().toString();
        final int idx = idStr.lastIndexOf(" / ");
        if(idx != -1) {
            idStr = idStr.substring(idx + 3, idStr.length() - 1);
        }
        final Identifier id = Identifier.tryParse(idStr);
        if(id != null) {
            be.recipeUsed(id);
        }
    }




    public static int getBurnTime(final FuelValues fuelValues, final ItemStack fuel) {
        return fuelValues.burnDuration(fuel);
    }




    @Override
    public void saveAdditional(final ValueOutput output, final BasicControllerBlockEntity be) {
        output.putShort(KEY_LIT_TIME,            (short) be.getMachineData(DATA_LIT_TIME));
        output.putShort(KEY_LIT_DURATION,        (short) be.getMachineData(DATA_LIT_DURATION));
        output.putShort(KEY_CRUSHING_PROGRESS,   (short) be.getMachineData(DATA_CRUSHING_PROGRESS));
        output.putShort(KEY_CRUSHING_TOTAL_TIME, (short) be.getMachineData(DATA_CRUSHING_TOTAL_TIME));
    }

    @Override
    public void loadAdditional(final ValueInput input, final BasicControllerBlockEntity be) {
        be.setMachineData(DATA_LIT_TIME,            input.getShortOr(KEY_LIT_TIME,            (short) 0));
        be.setMachineData(DATA_LIT_DURATION,        input.getShortOr(KEY_LIT_DURATION,        (short) 0));
        be.setMachineData(DATA_CRUSHING_PROGRESS,   input.getShortOr(KEY_CRUSHING_PROGRESS,   (short) 0));
        be.setMachineData(DATA_CRUSHING_TOTAL_TIME, input.getShortOr(KEY_CRUSHING_TOTAL_TIME, (short) 0));
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.cassiopeia.crusher");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory,
            final BasicControllerBlockEntity be, final ContainerData data) {
        return new CrusherMenu(containerId, playerInventory, be, data);
    }

    @Override
    public int getDataValue(final BasicControllerBlockEntity be, final int index) {
        return be.getMachineData(index);
    }

    @Override
    public void setDataValue(final BasicControllerBlockEntity be, final int index, final int value) {
        be.setMachineData(index, value);
    }
}

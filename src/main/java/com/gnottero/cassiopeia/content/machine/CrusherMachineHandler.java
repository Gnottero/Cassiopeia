package com.gnottero.cassiopeia.content.machine;

import com.gnottero.cassiopeia.content.ModRegistry;
import com.gnottero.cassiopeia.content.block.entity.BasicControllerBlockEntity;
import com.gnottero.cassiopeia.content.menu.CrusherMenu;
import com.gnottero.cassiopeia.content.recipe.CrusherRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Machine handler for the crusher.
 * Stateless implementation: stores data in
 * BasicControllerBlockEntity.machineData
 */
public class CrusherMachineHandler implements MachineHandler {

    public static final String STRUCTURE_ID = "crusher";

    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int SLOT_COUNT = 3;

    // Data indices
    public static final int DATA_LIT_TIME = 0;
    public static final int DATA_LIT_DURATION = 1;
    public static final int DATA_COOKING_PROGRESS = 2;
    public static final int DATA_COOKING_TOTAL_TIME = 3;
    public static final int DATA_COUNT = 4;

    // NBT Keys strings
    private static final String KEY_LIT_TIME = "lit_time_remaining";
    private static final String KEY_LIT_DURATION = "lit_total_time";
    private static final String KEY_COOKING_PROGRESS = "crushing_time_spent";
    private static final String KEY_COOKING_TOTAL_TIME = "crushing_total_time";

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
    public int[] getSlotsForFace(Direction side) {
        return switch (side) {
            case UP -> SLOTS_UP;
            case DOWN -> SLOTS_DOWN;
            default -> SLOTS_SIDES;
        };
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == OUTPUT_SLOT) {
            return false;
        } else if (slot == FUEL_SLOT) {
            return getBurnTime(stack) > 0;
        }
        return true;
    }

    @Override
    public boolean canTakeItem(int slot, ItemStack stack, Direction direction) {
        return slot == OUTPUT_SLOT;
    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState state, BasicControllerBlockEntity be) {
        if (!be.verifyStructure(level, pos)) {
            return;
        }

        NonNullList<ItemStack> items = be.getMachineItems();
        boolean changed = false;

        // Fetch state from BE
        int litTime = be.getMachineData(DATA_LIT_TIME);
        int litDuration = be.getMachineData(DATA_LIT_DURATION);
        int crushingProgress = be.getMachineData(DATA_COOKING_PROGRESS);
        int crushingTotalTime = be.getMachineData(DATA_COOKING_TOTAL_TIME);

        if (litTime > 0) {
            litTime--;
            changed = true;
        }

        ItemStack inputStack = items.get(INPUT_SLOT);
        ItemStack fuelStack = items.get(FUEL_SLOT);

        boolean hasInput = !inputStack.isEmpty();
        boolean hasFuel = !fuelStack.isEmpty();

        if (litTime > 0 || (hasFuel && hasInput)) {
            Optional<RecipeHolder<CrusherRecipe>> recipeHolderOpt = getRecipe(level, inputStack);

            if (recipeHolderOpt.isPresent()) {
                RecipeHolder<CrusherRecipe> holder = recipeHolderOpt.get();
                CrusherRecipe recipe = holder.value();
                crushingTotalTime = recipe.getCrushingTime();
                be.setMachineData(DATA_COOKING_TOTAL_TIME, crushingTotalTime);

                if (litTime <= 0 && hasFuel) {
                    litDuration = getBurnTime(fuelStack);
                    litTime = litDuration;

                    if (litTime > 0) {
                        changed = true;
                        Item fuelItem = fuelStack.getItem();
                        ItemStack remainder = fuelItem.getCraftingRemainder();
                        fuelStack.shrink(1);
                        if (fuelStack.isEmpty() && !remainder.isEmpty()) {
                            items.set(FUEL_SLOT, remainder);
                        } else if (fuelStack.isEmpty()) {
                            items.set(FUEL_SLOT, ItemStack.EMPTY);
                        }
                    }
                }

                if (litTime > 0 && canProcess(items, recipe)) {
                    crushingProgress++;
                    if (crushingProgress >= crushingTotalTime) {
                        crushingProgress = 0;
                        process(items, recipe);
                        String idStr = holder.id().toString();
                        int idx = idStr.lastIndexOf(" / ");
                        if (idx != -1)
                            idStr = idStr.substring(idx + 3, idStr.length() - 1);
                        be.recipeUsed(Identifier.tryParse(idStr));
                        changed = true;
                    } else {
                        changed = true;
                    }
                } else if (litTime <= 0) {
                    if (crushingProgress > 0) {
                        crushingProgress = 0;
                        changed = true;
                    }
                }
            } else {
                crushingProgress = 0;
                changed = true;
            }
        } else if (crushingProgress > 0) {
            crushingProgress = Math.max(0, crushingProgress - 2);
            changed = true;
        }

        // Commit state back to BE
        be.setMachineData(DATA_LIT_TIME, litTime);
        be.setMachineData(DATA_LIT_DURATION, litDuration);
        be.setMachineData(DATA_COOKING_PROGRESS, crushingProgress);
        be.setMachineData(DATA_COOKING_TOTAL_TIME, crushingTotalTime);

        if (changed) {
            be.setChanged();
        }
    }

    private Optional<RecipeHolder<CrusherRecipe>> getRecipe(Level level, ItemStack input) {
        if (level == null || input.isEmpty() || !(level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }
        SingleRecipeInput recipeInput = new SingleRecipeInput(input);
        return serverLevel.recipeAccess()
                .getRecipeFor(ModRegistry.CRUSHER_RECIPE_TYPE, recipeInput, serverLevel);
    }

    private boolean canProcess(NonNullList<ItemStack> items, CrusherRecipe recipe) {
        ItemStack result = recipe.getResult();
        ItemStack outputSlot = items.get(OUTPUT_SLOT);
        if (outputSlot.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(outputSlot, result)) {
            return false;
        }
        return outputSlot.getCount() + result.getCount() <= outputSlot.getMaxStackSize();
    }

    private void process(NonNullList<ItemStack> items, CrusherRecipe recipe) {
        ItemStack inputStack = items.get(INPUT_SLOT);
        ItemStack result = recipe.getResult().copy();
        ItemStack outputSlot = items.get(OUTPUT_SLOT);

        if (outputSlot.isEmpty()) {
            items.set(OUTPUT_SLOT, result);
        } else if (ItemStack.isSameItemSameComponents(outputSlot, result)) {
            outputSlot.grow(result.getCount());
        }

        inputStack.shrink(1);
    }

    private static int getBurnTime(ItemStack fuel) {
        if (fuel.is(Items.COAL) || fuel.is(Items.CHARCOAL))
            return 1600;
        if (fuel.is(Items.COAL_BLOCK))
            return 16000;
        if (fuel.is(Items.BLAZE_ROD))
            return 2400;
        if (fuel.is(Items.LAVA_BUCKET))
            return 20000;
        if (fuel.is(ItemTags.LOGS_THAT_BURN))
            return 300;
        if (fuel.is(ItemTags.PLANKS))
            return 300;
        if (fuel.is(Items.STICK))
            return 100;
        return 0;
    }

    @Override
    public void saveAdditional(ValueOutput output, BasicControllerBlockEntity be) {
        output.putShort(KEY_LIT_TIME, (short) be.getMachineData(DATA_LIT_TIME));
        output.putShort(KEY_LIT_DURATION, (short) be.getMachineData(DATA_LIT_DURATION));
        output.putShort(KEY_COOKING_PROGRESS, (short) be.getMachineData(DATA_COOKING_PROGRESS));
        output.putShort(KEY_COOKING_TOTAL_TIME, (short) be.getMachineData(DATA_COOKING_TOTAL_TIME));
    }

    @Override
    public void loadAdditional(ValueInput input, BasicControllerBlockEntity be) {
        be.setMachineData(DATA_LIT_TIME, input.getShortOr(KEY_LIT_TIME, (short) 0));
        be.setMachineData(DATA_LIT_DURATION, input.getShortOr(KEY_LIT_DURATION, (short) 0));
        be.setMachineData(DATA_COOKING_PROGRESS, input.getShortOr(KEY_COOKING_PROGRESS, (short) 0));
        be.setMachineData(DATA_COOKING_TOTAL_TIME, input.getShortOr(KEY_COOKING_TOTAL_TIME, (short) 0));
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.cassiopeia.crusher");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory,
            BasicControllerBlockEntity be, ContainerData data) {
        return new CrusherMenu(containerId, playerInventory, be, data);
    }

    @Override
    public int getDataValue(BasicControllerBlockEntity be, int index) {
        return be.getMachineData(index);
    }

    @Override
    public void setDataValue(BasicControllerBlockEntity be, int index, int value) {
        be.setMachineData(index, value);
    }
}

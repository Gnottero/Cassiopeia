package com.gnottero.cassiopeia.content.machine;

import com.gnottero.cassiopeia.content.ModRegistry;
import com.gnottero.cassiopeia.content.block.entity.BasicControllerBlockEntity;
import com.gnottero.cassiopeia.content.menu.UnpackagerMenu;
import com.gnottero.cassiopeia.content.recipe.UnpackagerRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
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
 * Machine handler for the unpackager.
 * Stateless implementation: stores data in
 * BasicControllerBlockEntity.machineData
 */
public class UnpackagerMachineHandler implements MachineHandler {

    public static final String STRUCTURE_ID = "unpackager";

    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int SLOT_COUNT = 2;

    public static final int DATA_PROGRESS = 0;
    public static final int DATA_TOTAL_TIME = 1;
    public static final int DATA_COUNT = 2;

    private static final String KEY_COOK_TIME = "unpacking_time_spent";
    private static final String KEY_COOK_TIME_TOTAL = "unpacking_time_needed";

    private static final int[] SLOTS_UP = { INPUT_SLOT };
    private static final int[] SLOTS_DOWN = { OUTPUT_SLOT };
    private static final int[] SLOTS_SIDES = { INPUT_SLOT };

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
        return slot == INPUT_SLOT;
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
        ItemStack inputStack = items.get(INPUT_SLOT);

        int unpackingTime = be.getMachineData(DATA_PROGRESS);
        int unpackingTotalTime = be.getMachineData(DATA_TOTAL_TIME);
        boolean changed = false;

        if (!inputStack.isEmpty()) {
            Optional<RecipeHolder<UnpackagerRecipe>> recipeHolderOpt = getRecipe(level, inputStack);

            if (recipeHolderOpt.isPresent()) {
                RecipeHolder<UnpackagerRecipe> holder = recipeHolderOpt.get();
                UnpackagerRecipe recipe = holder.value();
                unpackingTotalTime = recipe.getProcessingTime();
                be.setMachineData(DATA_TOTAL_TIME, unpackingTotalTime);

                if (canProcess(items, recipe)) {
                    unpackingTime++;

                    if (unpackingTime >= unpackingTotalTime) {
                        process(items, recipe);
                        String idStr = holder.id().toString();
                        int idx = idStr.lastIndexOf(" / ");
                        if (idx != -1)
                            idStr = idStr.substring(idx + 3, idStr.length() - 1);
                        be.recipeUsed(Identifier.tryParse(idStr));
                        unpackingTime = 0;
                        changed = true;
                    }
                } else {
                    unpackingTime = 0;
                }
            } else {
                unpackingTime = 0;
                unpackingTotalTime = 0;
            }
        } else {
            if (unpackingTime > 0) {
                unpackingTime = 0;
            }
        }

        // Commit derived values back to BE
        be.setMachineData(DATA_PROGRESS, unpackingTime);
        be.setMachineData(DATA_TOTAL_TIME, unpackingTotalTime);

        if (changed) {
            be.setChanged();
        }
    }

    private Optional<RecipeHolder<UnpackagerRecipe>> getRecipe(Level level, ItemStack input) {
        if (level == null || input.isEmpty() || !(level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }
        SingleRecipeInput recipeInput = new SingleRecipeInput(input);
        return serverLevel.recipeAccess()
                .getRecipeFor(ModRegistry.UNPACKAGER_RECIPE_TYPE, recipeInput, serverLevel);
    }

    private boolean canProcess(NonNullList<ItemStack> items, UnpackagerRecipe recipe) {
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

    private void process(NonNullList<ItemStack> items, UnpackagerRecipe recipe) {
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

    @Override
    public void saveAdditional(ValueOutput output, BasicControllerBlockEntity be) {
        output.putShort(KEY_COOK_TIME, (short) be.getMachineData(DATA_PROGRESS));
        output.putShort(KEY_COOK_TIME_TOTAL, (short) be.getMachineData(DATA_TOTAL_TIME));
    }

    @Override
    public void loadAdditional(ValueInput input, BasicControllerBlockEntity be) {
        be.setMachineData(DATA_PROGRESS, input.getShortOr(KEY_COOK_TIME, (short) 0));
        be.setMachineData(DATA_TOTAL_TIME, input.getShortOr(KEY_COOK_TIME_TOTAL, (short) 0));
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.cassiopeia.unpackager");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory,
            BasicControllerBlockEntity be, ContainerData data) {
        return new UnpackagerMenu(containerId, playerInventory, be, data);
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

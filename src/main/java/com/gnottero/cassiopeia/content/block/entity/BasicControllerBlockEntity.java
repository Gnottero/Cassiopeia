package com.gnottero.cassiopeia.content.block.entity;

import com.gnottero.cassiopeia.content.machine.MachineHandler;
import com.gnottero.cassiopeia.content.machine.MachineHandlerRegistry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;








/**
 * Basic Controller Block Entity.
 * Delegates machine behavior to MachineHandler implementations.
 */
public class BasicControllerBlockEntity extends AbstractControllerBlockEntity implements ExtendedScreenHandlerFactory<BlockPos>, WorldlyContainer {

    private static final int MAX_SLOT_COUNT = 4;
    private static final int MAX_DATA_COUNT = 8;
    private static final int[] NO_SLOTS = {};

    private static final String TAG_ITEMS = "Items";
    private static final String TAG_RECIPES_USED = "RecipesUsed";

    private final NonNullList<ItemStack> machineItems = NonNullList.withSize(MAX_SLOT_COUNT, ItemStack.EMPTY);
    private final int[] machineData = new int[MAX_DATA_COUNT];
    private final Object2IntOpenHashMap<Identifier> recipesUsed = new Object2IntOpenHashMap<>();




    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            if(index < 0 || index >= MAX_DATA_COUNT) return 0;
            else return machineData[index];
        }

        @Override
        public void set(int index, int value) {
            if(index >= 0 && index < MAX_DATA_COUNT) {
                machineData[index] = value;
            }
        }

        @Override
        public int getCount() {
            return getHandler().map(MachineHandler::getDataCount).orElse(0);
        }
    };


    public BasicControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BASIC_CONTROLLER, pos, state);
    }


    private Optional<MachineHandler> getHandler() {
        String structureId = getStructureId();
        if(structureId == null) return Optional.empty();
        else return MachineHandlerRegistry.getHandler(structureId);
    }




    // ==================== Server Tick ====================

    public static void serverTick(Level level, BlockPos pos, BlockState state, BasicControllerBlockEntity be) {
        be.getHandler().ifPresent(handler -> handler.serverTick(level, pos, state, be));
    }




    // ==================== Machine Data Access ====================

    public NonNullList<ItemStack> getMachineItems() {
        return machineItems;
    }

    public int getMachineData(int index) {
        if(index < 0 || index >= MAX_DATA_COUNT) return 0;
        else return machineData[index];
    }

    public void setMachineData(int index, int value) {
        if(index >= 0 && index < MAX_DATA_COUNT) {
            machineData[index] = value;
        }
    }

    public ContainerData getContainerData() {
        return containerData;
    }




    // ==================== WorldlyContainer Implementation ====================

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        return getHandler().map(h -> h.getSlotsForFace(side)).orElse(NO_SLOTS);
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack stack, @Nullable Direction direction) {
        return getHandler().map(h -> h.canPlaceItem(this, index, stack)).orElse(false);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
        return getHandler().map(h -> h.canTakeItem(index, stack, direction)).orElse(false);
    }




    // ==================== Container Implementation ====================

    @Override
    public int getContainerSize() {
        return getHandler().map(MachineHandler::getSlotCount).orElse(0);
    }

    @Override
    public boolean isEmpty() {
        int size = getContainerSize();
        for(int i = 0; i < size; i++) {
            if(!machineItems.get(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        int size = getContainerSize();
        if(slot < 0 || slot >= size) return ItemStack.EMPTY;
        else return machineItems.get(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        if(getContainerSize() == 0) return ItemStack.EMPTY;
        else return ContainerHelper.removeItem(machineItems, slot, amount);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        if(getContainerSize() == 0) return ItemStack.EMPTY;
        else return ContainerHelper.takeItem(machineItems, slot);
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        int size = getContainerSize();
        if(slot < 0 || slot >= size) return;

        ItemStack existing = machineItems.get(slot);
        boolean sameItem = !stack.isEmpty() && ItemStack.isSameItemSameComponents(existing, stack);
        machineItems.set(slot, stack);

        if(stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }

        // Reset cooking progress when input changes (not fuel time!)
        getHandler().ifPresent(handler -> {
            if(slot == handler.getInputSlotIndex() && !sameItem) {
                setMachineData(handler.getProcessProgressIndex(), 0);
                setChanged();
            }
        });
    }

    @Override
    public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
        return getHandler().map(h -> h.canPlaceItem(this, index, stack)).orElse(false);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        int size = getContainerSize();
        for(int i = 0; i < size; i++) {
            machineItems.set(i, ItemStack.EMPTY);
        }
    }




    // ==================== NBT Serialization ====================

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);

        ContainerHelper.saveAllItems(output, machineItems, true);

        getHandler().ifPresent(handler -> handler.saveAdditional(output, this));

        // Save recipes used
        CompoundTag recipesTag = new CompoundTag();
        recipesUsed.forEach((id, count) -> recipesTag.putInt(id.toString(), count));
        output.store(TAG_RECIPES_USED, CompoundTag.CODEC, recipesTag);
    }


    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);

        ContainerHelper.loadAllItems(input, machineItems);

        getHandler().ifPresent(handler -> handler.loadAdditional(input, this));

        // Load recipes used
        CompoundTag recipesTag = input.read(TAG_RECIPES_USED, CompoundTag.CODEC).orElse(new CompoundTag());
        recipesUsed.clear();
        for(String key : recipesTag.keySet()) {
            Identifier id = Identifier.tryParse(key);
            if(id != null) {
                recipesUsed.put(id, recipesTag.getIntOr(key, 0));
            }
        }
    }




    // ==================== Menu Factory ====================

    @Override
    public @NotNull Component getDisplayName() {
        return getHandler().map(MachineHandler::getDisplayName)
        .orElse(Component.translatable("container.cassiopeia.controller"));
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return getHandler().map(h -> h.createMenu(containerId, playerInventory, this, containerData))
        .orElse(null);
    }

    @Override
    public BlockPos getScreenOpeningData(@NotNull ServerPlayer player) {
        return this.worldPosition;
    }




    // ==================== Recipe Tracking ====================

    public void recipeUsed(Identifier recipeId) {
        if(recipeId != null) {
            recipesUsed.addTo(recipeId, 1);
        }
    }

    public void awardUsedRecipes(Player player, List<ItemStack> items) {
        if(level == null || level.isClientSide())
            return;

        List<RecipeHolder<?>> recipesToAward = new ArrayList<>();

        if(level instanceof ServerLevel serverLevel) {
            for(var entry : recipesUsed.object2IntEntrySet()) {
                ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, entry.getKey());
                serverLevel.getServer().getRecipeManager().byKey(key).ifPresent(recipesToAward::add);
            }
        }

        player.awardRecipes(recipesToAward);
        recipesUsed.clear();
    }

    public Object2IntOpenHashMap<Identifier> getRecipesUsed() {
        return recipesUsed;
    }
}

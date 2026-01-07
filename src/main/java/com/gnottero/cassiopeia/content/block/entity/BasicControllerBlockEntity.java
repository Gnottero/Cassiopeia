package com.gnottero.cassiopeia.content.block.entity;

import com.gnottero.cassiopeia.content.ModRegistry;
import com.gnottero.cassiopeia.content.machine.MachineHandler;
import com.gnottero.cassiopeia.content.machine.MachineHandlerRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.nbt.CompoundTag;

/**
 * Basic Controller Block Entity.
 */
public class BasicControllerBlockEntity extends AbstractControllerBlockEntity
        implements MenuProvider, WorldlyContainer {

    private static final int MAX_SLOT_COUNT = 4;
    private static final int MAX_DATA_COUNT = 8;
    private static final int[] NO_SLOTS = {};

    public static final String TAG_ITEMS = "Items";
    public static final String TAG_RECIPES_USED = "RecipesUsed";

    private NonNullList<ItemStack> machineItems = NonNullList.withSize(MAX_SLOT_COUNT, ItemStack.EMPTY);
    private final int[] machineData = new int[MAX_DATA_COUNT];
    private final Object2IntOpenHashMap<Identifier> recipesUsed = new Object2IntOpenHashMap<>();

    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            if (index < 0 || index >= MAX_DATA_COUNT)
                return 0;
            return BasicControllerBlockEntity.this.machineData[index];
        }

        @Override
        public void set(int index, int value) {
            if (index >= 0 && index < MAX_DATA_COUNT) {
                BasicControllerBlockEntity.this.machineData[index] = value;
            }
        }

        @Override
        public int getCount() {
            return getHandler().map(MachineHandler::getDataCount).orElse(0);
        }
    };

    public BasicControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.BASIC_CONTROLLER_BLOCK_ENTITY, pos, state);
    }

    private Optional<MachineHandler> getHandler() {
        String structureId = getStructureId();
        if (structureId == null)
            return Optional.empty();
        return MachineHandlerRegistry.getHandler(structureId);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BasicControllerBlockEntity be) {
        be.getHandler().ifPresent(handler -> handler.tick(level, pos, state, be));
    }

    public NonNullList<ItemStack> getMachineItems() {
        return machineItems;
    }

    public int getMachineData(int index) {
        if (index < 0 || index >= MAX_DATA_COUNT)
            return 0;
        return machineData[index];
    }

    public void setMachineData(int index, int value) {
        if (index >= 0 && index < MAX_DATA_COUNT) {
            machineData[index] = value;
        }
    }

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        return getHandler().map(h -> h.getSlotsForFace(side)).orElse(NO_SLOTS);
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack stack, @Nullable Direction direction) {
        return getHandler().map(h -> h.canPlaceItem(index, stack)).orElse(false);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
        return getHandler().map(h -> h.canTakeItem(index, stack, direction)).orElse(false);
    }

    @Override
    public int getContainerSize() {
        return getHandler().map(MachineHandler::getSlotCount).orElse(0);
    }

    @Override
    public boolean isEmpty() {
        int size = getContainerSize();
        for (int i = 0; i < size; i++) {
            if (!machineItems.get(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        int size = getContainerSize();
        if (slot < 0 || slot >= size) {
            return ItemStack.EMPTY;
        }
        return machineItems.get(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        if (getContainerSize() == 0)
            return ItemStack.EMPTY;
        return ContainerHelper.removeItem(machineItems, slot, amount);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        if (getContainerSize() == 0)
            return ItemStack.EMPTY;
        return ContainerHelper.takeItem(machineItems, slot);
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        int size = getContainerSize();
        if (slot < 0 || slot >= size)
            return;

        ItemStack existing = machineItems.get(slot);
        boolean sameItem = !stack.isEmpty() && ItemStack.isSameItemSameComponents(existing, stack);
        machineItems.set(slot, stack);

        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }

        if (slot == 0 && !sameItem) {
            setMachineData(0, 0);
            setChanged();
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        int size = getContainerSize();
        for (int i = 0; i < size; i++) {
            machineItems.set(i, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
        return getHandler().map(h -> h.canPlaceItem(index, stack)).orElse(false);
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);

        // Save items (Always save, even if empty, when handler is present)
        ContainerHelper.saveAllItems(output, machineItems, true);

        Optional<MachineHandler> handlerOpt = getHandler();
        if (handlerOpt.isEmpty()) {
            return;
        }

        MachineHandler handler = handlerOpt.get();
        // Save machine specific data
        handler.saveAdditional(output, this);

        // Save recipes used
        CompoundTag recipesTag = new CompoundTag();
        this.recipesUsed.forEach((id, count) -> recipesTag.putInt(id.toString(), count));
        output.store(TAG_RECIPES_USED, CompoundTag.CODEC, recipesTag);
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);

        // Load items (ContainerHelper takes 2 args in this environment)
        ContainerHelper.loadAllItems(input, machineItems);

        Optional<MachineHandler> handlerOpt = getHandler();
        if (handlerOpt.isEmpty()) {
            return;
        }

        MachineHandler handler = handlerOpt.get();
        // Load machine specific data
        handler.loadAdditional(input, this);

        // Load recipes used
        CompoundTag recipesTag = input.read(TAG_RECIPES_USED, CompoundTag.CODEC).orElse(new CompoundTag());

        this.recipesUsed.clear();
        for (String key : recipesTag.keySet()) {
            Identifier id = Identifier.tryParse(key);
            if (id != null) {
                this.recipesUsed.put(id, recipesTag.getInt(key).orElse(0));
            }
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return getHandler().map(MachineHandler::getDisplayName)
                .orElse(Component.translatable("container.cassiopeia.controller"));
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory,
            @NotNull Player player) {
        return getHandler()
                .map(h -> h.createMenu(containerId, playerInventory, this, containerData))
                .orElse(null);
    }

    public ContainerData getContainerData() {
        return containerData;
    }

    public void recipeUsed(Identifier recipeId) {
        this.recipesUsed.addTo(recipeId, 1);
    }

    public void awardUsedRecipes(Player player, List<ItemStack> items) {
        // Simple implementation - in a full mod we would use RicepBook support
        // For now we just clear it to prevent infinite accumulation if not used
        // Or keep it if we want persistent stats.
        // Vanilla AbstractFurnaceBlockEntity clears it after awarding XP.
        // Since we don't have XP handling yet, we'll just expose the method for future
        // use.
    }

    public Object2IntOpenHashMap<Identifier> getRecipesUsed() {
        return recipesUsed;
    }
}

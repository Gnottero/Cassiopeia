package com.gnottero.cassiopeia.content.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Objects;

public record IngredientWithComponents(Ingredient ingredient, int count, DataComponentPatch components) {
    public static final Codec<IngredientWithComponents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(IngredientWithComponents::ingredient),
            Codec.INT.optionalFieldOf("count", 1).forGetter(IngredientWithComponents::count),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                    .forGetter(IngredientWithComponents::components))
            .apply(instance, IngredientWithComponents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, IngredientWithComponents> STREAM_CODEC = StreamCodec
            .composite(
                    Ingredient.CONTENTS_STREAM_CODEC, IngredientWithComponents::ingredient,
                    ByteBufCodecs.VAR_INT, IngredientWithComponents::count,
                    DataComponentPatch.STREAM_CODEC, IngredientWithComponents::components,
                    IngredientWithComponents::new);

    public boolean test(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        if (!ingredient.test(stack)) {
            return false;
        }

        if (stack.getCount() < count) {
            return false;
        }

        if (components.isEmpty()) {
            return true;
        }

        // Check if the stack has all the components from the patch
        // For a simple implementation, we check if applying the patch to the stack's
        // components
        // would result in the same components for the keys specified in the patch.

        // This is a common way to check for "NBT" matching in 1.21
        for (var entry : components.entrySet()) {
            var type = entry.getKey();
            var patchValue = entry.getValue();

            if (patchValue.isPresent()) {
                Object stackValue = stack.getComponents().get(type);
                if (!Objects.equals(patchValue.get(), stackValue)) {
                    return false;
                }
            } else {
                // If it's present but empty in the patch, it means it should be REMOVED
                // (absent)
                if (stack.getComponents().has(type)) {
                    return false;
                }
            }
        }

        return true;
    }
}

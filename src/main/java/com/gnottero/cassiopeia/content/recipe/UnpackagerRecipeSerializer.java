package com.gnottero.cassiopeia.content.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * Serializer for UnpackagerRecipe.
 * Uses ItemStack.STRICT_CODEC to preserve NBT/component data.
 */
public class UnpackagerRecipeSerializer implements RecipeSerializer<UnpackagerRecipe> {

    public static final UnpackagerRecipeSerializer INSTANCE = new UnpackagerRecipeSerializer();

    public static final MapCodec<UnpackagerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(UnpackagerRecipe::getIngredient),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(UnpackagerRecipe::getResult),
            Codec.INT.optionalFieldOf("time", 200).forGetter(UnpackagerRecipe::getProcessingTime))
            .apply(instance, UnpackagerRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, UnpackagerRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, UnpackagerRecipe::getIngredient,
            ItemStack.STREAM_CODEC, UnpackagerRecipe::getResult,
            ByteBufCodecs.INT, UnpackagerRecipe::getProcessingTime,
            UnpackagerRecipe::new);

    private UnpackagerRecipeSerializer() {
    }

    @Override
    public @NotNull MapCodec<UnpackagerRecipe> codec() {
        return CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, UnpackagerRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}

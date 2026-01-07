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

public class CrusherRecipeSerializer implements RecipeSerializer<CrusherRecipe> {

    public static final CrusherRecipeSerializer INSTANCE = new CrusherRecipeSerializer();

    public static final MapCodec<CrusherRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(CrusherRecipe::getIngredient),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(CrusherRecipe::getResult),
            Codec.INT.optionalFieldOf("time", 200).forGetter(CrusherRecipe::getCrushingTime))
            .apply(instance, CrusherRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrusherRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, CrusherRecipe::getIngredient,
            ItemStack.STREAM_CODEC, CrusherRecipe::getResult,
            ByteBufCodecs.INT, CrusherRecipe::getCrushingTime,
            CrusherRecipe::new);

    private CrusherRecipeSerializer() {
    }

    @Override
    public @NotNull MapCodec<CrusherRecipe> codec() {
        return CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, CrusherRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}

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

/**
 * Serializer for AlloyingRecipe.
 */
public class AlloyingRecipeSerializer implements RecipeSerializer<AlloyingRecipe> {
    public static final AlloyingRecipeSerializer INSTANCE = new AlloyingRecipeSerializer();

    private static final MapCodec<AlloyingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(AlloyingRecipe::group),
            Ingredient.CODEC.fieldOf("input_a").forGetter(AlloyingRecipe::getInputA),
            Ingredient.CODEC.fieldOf("input_b").forGetter(AlloyingRecipe::getInputB),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(AlloyingRecipe::getResult),
            Codec.FLOAT.optionalFieldOf("experience", 0.0f).forGetter(AlloyingRecipe::getExperience),
            Codec.INT.optionalFieldOf("alloying_time", 200).forGetter(AlloyingRecipe::getAlloyingTime))
            .apply(instance, AlloyingRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, AlloyingRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, AlloyingRecipe::group,
            Ingredient.CONTENTS_STREAM_CODEC, AlloyingRecipe::getInputA,
            Ingredient.CONTENTS_STREAM_CODEC, AlloyingRecipe::getInputB,
            ItemStack.STREAM_CODEC, AlloyingRecipe::getResult,
            ByteBufCodecs.FLOAT, AlloyingRecipe::getExperience,
            ByteBufCodecs.INT, AlloyingRecipe::getAlloyingTime,
            AlloyingRecipe::new);

    private AlloyingRecipeSerializer() {
    }

    @Override
    public MapCodec<AlloyingRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AlloyingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}

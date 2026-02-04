package com.gnottero.cassiopeia.content.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CrushingRecipeSerializer implements RecipeSerializer<CrushingRecipe> {
    public static final CrushingRecipeSerializer INSTANCE = new CrushingRecipeSerializer();

    private static final MapCodec<CrushingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.optionalFieldOf("group", "").forGetter(CrushingRecipe::group),
        IngredientWithComponents.CODEC.fieldOf("crushed").forGetter(CrushingRecipe::getInput),
        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(CrushingRecipe::getResult),
        Codec.FLOAT.optionalFieldOf("experience", 0.0f).forGetter(CrushingRecipe::getExperience),
        Codec.INT.optionalFieldOf("crushing_time", 200).forGetter(CrushingRecipe::getCrushingTime))
        .apply(instance, CrushingRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, CrushingRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CrushingRecipe::group,
            IngredientWithComponents.STREAM_CODEC, CrushingRecipe::getInput,
            ItemStack.STREAM_CODEC, CrushingRecipe::getResult,
            ByteBufCodecs.FLOAT, CrushingRecipe::getExperience,
            ByteBufCodecs.INT, CrushingRecipe::getCrushingTime,
            CrushingRecipe::new);

    @Override
    public MapCodec<CrushingRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, CrushingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
    
}

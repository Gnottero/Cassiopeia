package com.gnottero.cassiopeia.content.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CrusherRecipeSerializer implements RecipeSerializer<CrusherRecipe> {
    public static final CrusherRecipeSerializer INSTANCE = new CrusherRecipeSerializer();

    public static final MapCodec<CrusherRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(CrusherRecipe::group),
            CrushingBookCategory.CODEC.fieldOf("category").orElse(CrushingBookCategory.MISC)
                    .forGetter(CrusherRecipe::category),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(CrusherRecipe::getIngredient),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(CrusherRecipe::getResult),
            Codec.FLOAT.fieldOf("experience").orElse(0.0F).forGetter(CrusherRecipe::getExperience),
            Codec.INT.fieldOf("crushingTime").orElse(200).forGetter(CrusherRecipe::getCrushingTime))
            .apply(instance, CrusherRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, CrusherRecipe> STREAM_CODEC = StreamCodec.of(
            (buf, recipe) -> {
                buf.writeUtf(recipe.group());
                buf.writeEnum(recipe.category());
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input);
                ItemStack.STREAM_CODEC.encode(buf, recipe.output);
                buf.writeFloat(recipe.getExperience());
                buf.writeInt(recipe.getCrushingTime());
            },
            (buf) -> {
                String group = buf.readUtf();
                CrushingBookCategory category = buf.readEnum(CrushingBookCategory.class);
                Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
                float experience = buf.readFloat();
                int crushingTime = buf.readInt();
                return new CrusherRecipe(group, category, input, output, experience, crushingTime);
            });

    @Override
    public MapCodec<CrusherRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, CrusherRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}

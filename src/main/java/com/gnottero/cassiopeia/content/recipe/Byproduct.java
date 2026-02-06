package com.gnottero.cassiopeia.content.recipe;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record Byproduct(ItemStack result, float chance) {
        public static final Codec<Byproduct> CODEC = new Codec<Byproduct>() {
                @Override
                public <T> DataResult<Pair<Byproduct, T>> decode(DynamicOps<T> ops, T input) {
                        return ItemStack.STRICT_CODEC.decode(ops, input).flatMap(pair -> {
                                ItemStack stack = pair.getFirst();
                                float chance = ops.get(input, "chance")
                                                .flatMap(ops::getNumberValue)
                                                .map(Number::floatValue)
                                                .result()
                                                .orElse(1.0f);
                                return DataResult.success(Pair.of(new Byproduct(stack, chance), pair.getSecond()));
                        });
                }

                @Override
                public <T> DataResult<T> encode(Byproduct input, DynamicOps<T> ops, T prefix) {
                        DataResult<T> encoded = ItemStack.STRICT_CODEC.encodeStart(ops, input.result());
                        return encoded.flatMap(t -> {
                                DataResult<T> result = ops.mergeToMap(t, ops.createString("chance"),
                                                ops.createFloat(input.chance()));

                                if (result.isError()) {
                                        return ops.mergeToMap(ops.emptyMap(), ops.createString("id"), t)
                                                        .flatMap(m -> ops.mergeToMap(m, ops.createString("chance"),
                                                                        ops.createFloat(input.chance())));
                                }
                                return result;
                        });
                }
        };

        public static final StreamCodec<RegistryFriendlyByteBuf, Byproduct> STREAM_CODEC = StreamCodec.composite(
                        ItemStack.STREAM_CODEC, Byproduct::result,
                        ByteBufCodecs.FLOAT, Byproduct::chance,
                        Byproduct::new);
}

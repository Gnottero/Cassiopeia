package com.gnottero.cassiopeia.content.recipe;

import net.minecraft.util.StringRepresentable;

public enum CrushingBookCategory implements StringRepresentable {
    MISC("misc"),
    BLOCKS("blocks"),
    ITEMS("items");

    public static final StringRepresentable.EnumCodec<CrushingBookCategory> CODEC = StringRepresentable
            .fromEnum(CrushingBookCategory::values);
    private final String name;

    private CrushingBookCategory(String name) {
        this.name = name;
    }

    public String getSerializedName() {
        return this.name;
    }
}

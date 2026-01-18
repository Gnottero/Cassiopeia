package com.gnottero.cassiopeia.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;








/**
 * A simple container for three objects of different types.
 * @param <F> The type of the first value.
 * @param <S> The type of the second value.
 * @param <T> The type of the third value.
 * @author Ilaria (https://github.com/UwUSnek)
 */
public class Triplet<F, S, T> {

    // Values
    private @Nullable F first;
    private @Nullable S second;
    private @Nullable T third;

    // Getters
    public @Nullable F getFirst () { return first;  }
    public @Nullable S getSecond() { return second; }
    public @Nullable T getThird () { return third;  }

    // Setters
    public void setFirst (final @Nullable F first ) { this.first  = first;  }
    public void setSecond(final @Nullable S second) { this.second = second; }
    public void setThird (final @Nullable T third ) { this.third  = third;  }




    /**
     * Creates a new Triplet, setting all elements to {@code null}.
     */
    public Triplet() {
        first  = null;
        second = null;
        third  = null;
    }


    /**
     * Creates a new Triplet using the specified values.
     * @param first The first value.
     * @param second The second value.
     * @param third The third value.
     */
    public Triplet(final @Nullable F first, final @Nullable S second, final @Nullable T third) {
        this.first  = first;
        this.second = second;
        this.third  = third;
    }


    /**
     * Creates a new Triplet using the specified values.
     * @param <F> The type of the first value.
     * @param <S> The type of the second value.
     * @param <T> The type of the third value.
     * @param first The first value.
     * @param second The second value.
     * @param third The third value.
     * @return The newly created Triplet.
     */
    public static <F, S, T> @NotNull Triplet<@Nullable F, @Nullable S, @Nullable T> from(final @Nullable F first, final @Nullable S second, final @Nullable T third) {
        return new Triplet<>(first, second, third);
    }
}

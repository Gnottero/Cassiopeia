package com.gnottero.cassiopeia.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;








/**
 * A simple container for two objects of different types.
 * @param <F> The type of the first value.
 * @param <S> The type of the second value.
 * @author Ilaria (https://github.com/UwUSnek)
 */
public class Pair<F, S> {

    // Values
    private @Nullable F first;
    private @Nullable S second;

    // Getters
    public @Nullable F getFirst () { return first;  }
    public @Nullable S getSecond() { return second; }

    // Setters
    public void setFirst (final @Nullable F first ) { this.first  = first;  }
    public void setSecond(final @Nullable S second) { this.second = second; }




    /**
     * Creates a new Pair, setting both elements to {@code null}.
     */
    public Pair() {
        first  = null;
        second = null;
    }


    /**
     * Creates a new Pair using the specified values.
     * @param first The first value.
     * @param second The second value.
     */
    public Pair(final @Nullable F first, final @Nullable S second) {
        this.first  = first;
        this.second = second;
    }


    /**
     * Creates a new Pair using the specified values.
     * @param <F> The type of the first value.
     * @param <S> The type of the second value.
     * @param first The first value.
     * @param second The second value.
     * @return The newly created Pair.
     */
    public static <F, S> @NotNull Pair<@Nullable F, @Nullable S> from(final @Nullable F first, final @Nullable S second) {
        return new Pair<>(first, second);
    }
}

package com.gnottero.cassiopeia.utils;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;








/**
 * A Rust-like Optional that provides null-safe value handling.
 * <p>
 * Options can be created using {@link Option#Some} and {@link Option#None}.
 * {@code None} cannot contain any value, while {@code Some} must contain one.
 * <p>
 * Options can be converted to {@link Result}s.
 * @param <T> The type of value contained in the {@link Option}.
 */
public class Option<T> {

    private final @NotNull Optional<T> innerOptional;
    private Option(final T object) { innerOptional = Optional.of(object); }
    private Option(              ) { innerOptional = Optional.empty();    }


    /**
     * Creates an Option containing the specified value.
     * @param object The value.
     * @return The Option object.
     */
    public static <T> @NotNull Option<T> Some(final T object) {
        return new Option<>(object);
    }

    /**
     * Creates an Option containing no value.
     * @return The Option object.
     */
    public static <T> @NotNull Option<T> None() {
        return new Option<>();
    }








    /**
     * Checks if this Option contains a value.
     * @return True if it contains a value, false otherwise.
     */
    public boolean isSome() {
        return innerOptional.isPresent();
    }

    /**
     * Checks if this Option is empty.
     * @return True if it's empty, false otherwise.
     */
    public boolean isNone() {
        return  innerOptional.isEmpty();
    }

    /**
    * Checks if this Option contains a value that satisfies the predicate.
    * @param f The predicate to test the value against.
    * @return True if the value is present and satisfies the predicate, false otherwise.
    */
    public boolean isSomeAnd(final @NotNull Predicate<T> f) {
        if(this.isSome()) {
            return f.test(this.unwrap());
        }
        return false;
    }

    /**
     * Checks if this option is empty or it contains a value that satisfies the predicate.
    * @param f The predicate to test the value against.
     * @return True if empty or the value satisfies the predicate, false otherwise.
     */
    public boolean isNoneOr(final @NotNull Predicate<T> f) {
        if(this.isSome()) {
            return f.test(this.unwrap());
        }
        return true;
    }








    /**
     * Returns the contained value or throws an exception with the provided message.
     * @param message The message to use for the exception.
     * @return The value contained in this Option.
     * @throws NoSuchElementException if this Option is None.
     */
    public T expect(final @NotNull String message) {
        return innerOptional.orElseThrow(() -> new NoSuchElementException(message));
    }

    /**
     * Returns the contained value or throws an exception.
     * @return The value contained in this Option.
     * @throws NoSuchElementException if this Option is None.
     */
    public T unwrap() {
        return innerOptional.orElseThrow();
    }

    /**
     * Returns the contained value or a default value if empty.
     * @param default_value The value to return if the Option is empty.
     * @return The contained value or the default value.
     */
    public T unwrapOr(final T default_value) {
        return innerOptional.orElse(default_value);
    }

    /**
     * Returns the contained value or computes a default value using the supplier.
     * @param supplier The supplier to compute the default value.
     * @return The contained value or the computed default value.
     */
    public T unwrapOrElse(final @NotNull Supplier<? extends T> supplier) {
        return innerOptional.orElseGet(supplier);
    }

    /**
     * Converts this Option to a Result, mapping Some to Ok and None to Err.
     * @param err The error value to use if the Option is empty.
     * @return A Result containing the value or the error.
     */
    public <E> Result<T, E> okOr(final E err) {
        if(this.isSome()) {
            return Result.Ok(this.unwrap());
        }
        return Result.Err(err);
    }

    /**
     * Converts this Option to a Result, computing the error value with the supplier if needed.
     * @param f The supplier to compute the error value if the Option is empty.
     * @return A Result containing the value or the computed error.
     */
    public <E> Result<T, E> okOrElse(final @NotNull Supplier<E> f) {
        if(this.isSome()) {
            return Result.Ok(this.unwrap());
        }
        return Result.Err(f.get());
    }

    /**
     * Returns this Option if it contains a value that satisfies the predicate, otherwise None.
     * @param filter The predicate to test the value against.
     * @return This Option if the predicate is satisfied, None otherwise.
     */
    public Option<T> filter(final @NotNull Predicate<T> filter) {
        if(this.isSomeAnd(filter)) {
            return this;
        }
        return Option.None();
    }

    /**
     * Calls the consumer with the contained value if present, then returns this Option.
     * @param c The consumer to call.
     * @return This Option unchanged.
     */
    public Option<T> inspect(final @NotNull Consumer<T> c) {
        if(this.isSome()) {
            c.accept(this.unwrap());
        }
        return this;
    }

    /**
     * Maps the contained value to a new value using the given function.
     * @param f The mapping function.
     * @return An Option containing the mapped value, or None if this Option is empty.
     */
    public <U> Option<U> map(final @NotNull Function<T, U> f) {
        if(this.isNone()) {
            return Option.None();
        }
        return Option.Some(f.apply(this.unwrap()));
    }

    /**
     * Maps the contained value to a new value using the given function or returns a default value if this Option is Empty.
     * @param f The mapping function.
     * @param defaultValue The default value to return if the Option is empty.
     * @return An Option containing the mapped value or the default value.
     */
    public <U> U mapOr(final @NotNull Function<T, U> f, final U defaultValue) {
        return this.map(f).unwrapOr(defaultValue);
    }

    /**
     * Maps the contained value to a new value using the given function or computes a default value with the provided supplier if this Option is Empty.
     * @param f The mapping function.
     * @param supplier The supplied to compute the default value.
     * @return An Option containing the mapped value or the computed default value.
     */
    public <U> U mapOrElse(final @NotNull Function<T, U> f, final @NotNull Supplier<U> supplier) {
        return this.map(f).unwrapOrElse(supplier);
    }








    /**
     * Performs an AND binary operation with this Option and the provided one, treating Some as {@code true} and None as {@code false}.
     * @param input The other Option.
     * @return The input Option if both are Some, None otherwise.
     */
    public <U> @NotNull Option<U> and(final @NotNull Option<U> input) {
        if(input.isSome() && this.isSome()) {
            return input;
        }
        return Option.None();
    }

    /**
     * Performs an OR binary operation with this Option and the provided one, treating Some as {@code true} and None as {@code false}.
     * @param input The other Option.
     * @return This Option if Some, the input Option otherwise.
     */
    public @NotNull Option<T> or(final @NotNull Option<T> input) {
        if( this.isSome()) { return this;  }
        if(input.isSome()) { return input; }
        return Option.None();
    }

    /**
     * Performs an XOR binary operation with this Option and the provided one, treating Some as {@code true} and None as {@code false}.
     * @param input The other Option.
     * @return The Some Option if exactly one is Some, None otherwise.
     */
    public @NotNull Option<T> xor(final @NotNull Option<T> input) {
        if(this.isSome() && input.isNone()) { return this;  }
        if(this.isNone() && input.isSome()) { return input; }
        return Option.None();
    }

    /**
     * Applies the provided function to the contained value.
     * @param function The function to apply.
     * @return The result of the function, or None if this Option is empty.
     */
    public <U> @NotNull Option<U> andThen(final @NotNull Function<T,Option<U>> function) {
        if(this.isSome()) {
            return function.apply(this.unwrap());
        }
        return Option.None();
    }

    /**
     * Returns this Option if Some, otherwise computes an alternative Option using the provided supplier.
     * @param function The supplier to compute the alternative Option.
     * @return This Option if Some, the computed Option otherwise.
     */
    public @NotNull Option<T> orElse(final @NotNull Supplier<Option<T>> function) {
        if(this.isSome()) {
            return this;
        }
        return function.get();
    }
}
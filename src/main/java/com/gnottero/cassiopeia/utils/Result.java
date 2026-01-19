package com.gnottero.cassiopeia.utils;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;








/**
 * A Rust-like Result representing either success (Ok) or failure (Err).
 * @param <T> The type of the success value.
 * @param <E> The type of the error value.
 */
public class Result<T, E> {

    private final Option<T> innerOption;
    private final Option<E> err;
    //! innerOption and err can never both be None or both be Some.
    //! If they are, the object is invalid, and someone is a spoon.

    /**
     *
     * @param object
     */
    private Result(final T object) {
        this.innerOption = Option.Some(object);
        this.err = Option.None();
    }

    //! Java doesn't allow identical constructors with different definitions.
    //! The unused parameter is here to differentiate between the 2 constructors.
    @SuppressWarnings("java:S1172")
    private Result(final E err, final Void ignored) {
        this.innerOption = Option.None();
        this.err = Option.Some(err);
    }

    /**
     * Creates a successful Result.
     * @param obj The success value.
     * @return An {@code OK} Result containing the provided value.
     */
    public static <T, E> @NotNull Result<T, E> Ok(final T obj) {
        return new Result<>(obj);
    }

    /**
     * Creates a failed Result.
     * @param obj The error value.
     * @return An {@code Err} Result containing the provided value.
     */
    public static <T, E> @NotNull Result<T, E> Err(final E err) {
        return new Result<>(err, null);
        //! ^ null is ignored. It's there because java is mildly annoying.
    }








    /**
     * Checks if this Result is Ok.
     * @return True if this result is Ok, false otherwise.
     */
    public boolean isOk() {
        return innerOption.isSome();
    }

    /**
     * Checks if this Result is Ok and the contained value satisfies the predicate.
     * @param function The predicate to test the success value against.
     * @return True if this Result is Ok and the value satisfies the predicate, false otherwise.
     */
    public boolean isOkAnd(final @NotNull Predicate<T> function) {
        return innerOption.isSomeAnd(function);
    }

    /**
     * Checks if this Result is Err.
     * @return True if this result is Err, false otherwise.
     */
    public boolean isErr() {
        return err.isSome();
    }

    /**
     * Checks if this Result is Err and the contained value satisfies the predicate.
     * @param function The predicate to test the error value against.
     * @return True if this Result is Err and the value satisfies the predicate, false otherwise.
     */
    public boolean isErrAnd(final @NotNull Predicate<E> function) {
        return err.isSomeAnd(function);
    }

    /**
     * Returns the success value as an {@link Option}. This is Some only when this Result is Ok.
     * @return The success value as an {@link Option}.
     */
    @SuppressWarnings("java:S1845") //! Ambiguous method name
    public @NotNull Option<T> ok() {
        return innerOption;
    }

    /**
     * Returns the error value as an {@link Option}. This is Some only when this Result is Err.
     * @return The error value as an {@link Option}.
     */
    @SuppressWarnings("java:S1845") //! Ambiguous method name
    public @NotNull Option<E> err() {
        return err;
    }

    /**
     * Returns the contained success value or throws an exception.
     * @return The success value contained in this Result.
     * @throws NoSuchElementException if this Result is Err.
     */
    public T unwrap() {
        return this.innerOption.unwrap();
    }

    /**
     * Returns the contianed success value or throws an exception with the provided message.
     * @param msg The message to use for the exception.
     * @return The success value contained in this Result.
     * @throws NoSuchElementException if this Result is Err.
     */
    public T expect(final @NotNull String msg) {
        return this.innerOption.expect(msg);
    }

    /**
     * Returns the success value or the default value if Err.
     * @param defaultValue The value to return if this Result is Err.
     * @return The contained success value or the default value.
     */
    public T unwrapOr(final T defaultValue) {
        return this.innerOption.unwrapOr(defaultValue);
    }

    /**
     * Returns the success value if Ok, otherwise computes a success value from the provided function.
     * @param function The function used to compute the success value.
     * @return The contained success value if this Result is Ok, the computed success value otherwise.
     */
    public T unwrapOrElse(final @NotNull Function<E, T> function) {
        if(this.isOk()) {
            return this.unwrap();
        }
        return function.apply(this.err.unwrap());
    }

    /**
     * Returns the contained error value or throws an exception.
     * @return The error value contained in this Result.
     * @throws NoSuchElementException if this Result is Ok.
     */
    public E unwrapErr() {
        return this.err.unwrap();
    }

    /**
     * Returns the contianed error value or throws an exception with the provided message.
     * @param msg The message to use for the exception.
     * @return The error value contained in this Result.
     * @throws NoSuchElementException if this Result is Ok.
     */
    public E expectErr(final @NotNull String msg) {
        return this.err.expect(msg);
    }








    /**
     * Maps the contained success value to a new value using the given function. Leaves Err values unchanged.
     * @param function The mapping function.
     * @return A new Result with the transformed success value, or the original error if Err.
     */
    public <U> @NotNull Result<U, E> map(final @NotNull Function<T, U> function) {
        if(this.isErr()) {
            return Result.Err(err.unwrap());
        }
        return Result.Ok(this.ok().map(function).unwrap());
    }

    /**
     * Maps the contained success value to a new value using the given function or returns a default value if this Result is Err.
     * @param function The mapping function.
     * @param defaultValue The default value to return if this Result is Err.
     * @return The mapped value or the default value.
     */
    public <U> U mapOr(final @NotNull Function<T, U> function, final U defaultValue) {
        return this.ok().mapOr(function, defaultValue);
    }

    /**
     * Maps the contained success value to a new value using the given function or returns a default value computed with the provided function if this Result is Err.
     * @param function The mapping function.
     * @param defaultValue The function used to compute the default value to return if this Result is Err.
     * @return The mapped value or the computed default value.
     */
    public <U> U mapOrElse(final Function<T, U> function, final @NotNull Function<E, U> defaultFunction) {
        if(this.isOk()) {
            return this.ok().map(function).unwrap();
        }
        return this.err().map(defaultFunction).unwrap();
    }

    /**
     * Maps the contained error value to a new value using the given function. Leaves Ok values unchanged.
     * Leaves Ok values unchanged.
     * @param function The mapping function.
     * @return A new Result with the transformed error value, or the original success if Ok.
     */
    public <F> @NotNull Result<T, F> mapErr(final @NotNull Function<E, F> function) {
        if(this.isOk()) {
            return Result.Ok(this.unwrap());
        }
        return Result.Err(this.err().map(function).unwrap());
    }

    /**
     * Calls the provided consumer with the success value if Ok, does nothing if Err.
     * @param consumer The consumer to call with the success value.
     * @return This Result unchanged.
     */
    public @NotNull Result<T, E> inspect(final @NotNull Consumer<T> consumer) {
        this.ok().inspect(consumer);
        return this;
    }

    /**
     * Calls the provided consumer with the error value if Err, does nothing if Ok.
     * @param consumer The consumer to call with the error value.
     * @return This Result unchanged.
     */
    public @NotNull Result<T, E> inspectErr(final @NotNull Consumer<E> consumer) {
        this.err().inspect(consumer);
        return this;
    }
}

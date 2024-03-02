package net.lecousin.commons.function;

import java.util.Objects;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.function.FailableBiConsumer;
import org.apache.commons.lang3.function.TriConsumer;

/**
 * A functional interface like {@link TriConsumer} that declares a {@link Throwable}.
 *
 * @param <T> Consumed type 1.
 * @param <U> Consumed type 2.
 * @param <V> Consumed type 2.
 * @param <E> The kind of thrown exception or error.
 */
@FunctionalInterface
public interface FailableTriConsumer<T, U, V, E extends Throwable> {

    /** NOP singleton */
    @SuppressWarnings("rawtypes")
    FailableTriConsumer NOP = (t, u, v) -> {
    	/* NOP */
    };

    /**
     * Returns The NOP singleton.
     *
     * @param <T> Consumed type 1.
     * @param <U> Consumed type 2.
     * @param <V> Consumed type 2.
     * @param <E> The kind of thrown exception or error.
     * @return The NOP singleton.
     */
    @SuppressWarnings({"unchecked", "java:S1845"})
    static <T, U, V, E extends Throwable> FailableTriConsumer<T, U, V, E> nop() {
        return NOP;
    }

    /**
     * Accepts the given arguments.
     *
     * @param t the first parameter for the consumable to accept
     * @param u the second parameter for the consumable to accept
     * @param v the third parameter for the consumable to accept
     * @throws E Thrown when the consumer fails.
     */
    void accept(T t, U u, V v) throws E;

    /**
     * Returns a composed {@link FailableBiConsumer} like {@link BiConsumer#andThen(BiConsumer)}.
     *
     * @param after the operation to perform after this one.
     * @return a composed {@link FailableBiConsumer} like {@link BiConsumer#andThen(BiConsumer)}.
     * @throws NullPointerException when {@code after} is null.
     */
    default FailableTriConsumer<T, U, V, E> andThen(final FailableTriConsumer<? super T, ? super U, ? super V, E> after) {
        Objects.requireNonNull(after);
        return (t, u, v) -> {
            accept(t, u, v);
            after.accept(t, u, v);
        };
    }
}

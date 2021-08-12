package net.celestialdata.plexbot.discord.commandhandler.util.lazy;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * A lazy reference that initializes its value on first query in a thread-safe way from the given supplier.
 * {@code null} is not valid as value and means the value was not computed yet.
 *
 * @param <T> the class of the value object
 */
public class LazyReferenceBySupplier<T> extends LazyReference<T> {
    /**
     * The supplier for the value that is called on first query to compute the value.
     */
    private final Supplier<T> valueSupplier;

    /**
     * Constructs a new lazy reference that gets its value from the given supplier.
     *
     * @param valueSupplier the supplier for the value that is called on first query to compute the value
     */
    public LazyReferenceBySupplier(Supplier<T> valueSupplier) {
        this.valueSupplier = requireNonNull(valueSupplier, "value supplier must not be null");
    }

    /**
     * Returns the value of this reference. If the value was not computed yet, it gets
     * initialized using the value supplier given in the constructor in a thread-safe way and then returned.
     *
     * @return the value of this reference
     */
    public T get() {
        return get(valueSupplier);
    }
}
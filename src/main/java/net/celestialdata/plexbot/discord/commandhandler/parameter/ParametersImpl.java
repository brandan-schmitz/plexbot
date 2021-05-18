package net.celestialdata.plexbot.discord.commandhandler.parameter;

import net.celestialdata.plexbot.discord.commandhandler.api.parameter.Parameters;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static java.text.MessageFormat.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;

/**
 * The default implementation of the {@link Parameters} interface.
 *
 * @param <V> the class of the values in this parameters instance
 */
public class ParametersImpl<V> implements Parameters<V> {
    /**
     * The map that backs this parameters instance and holds the actual mappings.
     */
    private final Map<String, V> parameters = new HashMap<>();

    /**
     * An unmodifiable view on the backing map.
     */
    private final Map<String, V> unmodifiableParameters = unmodifiableMap(parameters);

    /**
     * A counter that keeps track on how many for-each iterations are currently in progress to be able to
     * throw a {@link ConcurrentModificationException} from {@link #fixup(String, String)} properly.
     */
    private final AtomicInteger iterationsInProgress = new AtomicInteger();

    /**
     * Constructs a new parameters implementation instance from the given parameter mappings.
     *
     * @param parameters the parameter mappings to hold in this instance
     */
    @SuppressWarnings({"unchecked", "unused", "CdiInjectionPointsInspection"})
    public ParametersImpl(Map<String, Object> parameters) {
        parameters.forEach((parameterName, parameterValues) -> {
            if (parameterValues == null) {
                String nullValuedParameters = parameters
                        .entrySet()
                        .stream()
                        .filter(entry -> isNull(entry.getValue()))
                        .map(Entry::getKey)
                        .sorted()
                        .collect(joining(", "));
                throw new IllegalArgumentException(String.format(
                        "parameters must not have null values: %s",
                        nullValuedParameters));
            }

            this.parameters.put(parameterName, (V) parameterValues);
        });
    }

    @Override
    public <R extends V> Optional<R> get(String parameterName) {
        return Optional.ofNullable(get(parameterName, (R) null));
    }

    @Override
    public <R extends V> R get(String parameterName, R defaultValue) {
        return this
                .<R>getAsMap()
                .getOrDefault(parameterName, defaultValue);
    }

    @SuppressWarnings("unused")
    @Override
    public <R extends V> R get(String parameterName, Supplier<R> defaultValueSupplier) {
        return this
                .<R>get(parameterName)
                .orElseGet(defaultValueSupplier);
    }

    @SuppressWarnings("unused")
    @Override
    public int size() {
        return parameters.size();
    }

    @SuppressWarnings("unused")
    @Override
    public boolean isEmpty() {
        return parameters.isEmpty();
    }

    @SuppressWarnings("unused")
    @Override
    public boolean containsParameter(String parameterName) {
        return parameters.containsKey(parameterName);
    }

    @SuppressWarnings("unused")
    @Override
    public boolean containsValue(V value) {
        return parameters.containsValue(value);
    }

    @SuppressWarnings("unused")
    @Override
    public Set<String> getParameterNames() {
        return unmodifiableParameters.keySet();
    }

    @SuppressWarnings("unused")
    @Override
    public <R extends V> Collection<R> getValues() {
        return this.<R>getAsMap().values();
    }

    @Override
    public <R extends V> Set<Entry<String, R>> getEntries() {
        return this.<R>getAsMap().entrySet();
    }

    @SuppressWarnings("unused")
    @Override
    public <R extends V> void forEach(BiConsumer<? super String, ? super R> action) {
        iterationsInProgress.incrementAndGet();
        try {
            this.<R>getEntries().forEach(entry -> action.accept(entry.getKey(), entry.getValue()));
        } finally {
            iterationsInProgress.decrementAndGet();
        }
    }

    @SuppressWarnings("unused")
    @Override
    public void fixup(String placeholderName, String literalName) {
        int iterationsInProgress = this.iterationsInProgress.get();
        if (iterationsInProgress != 0) {
            throw new ConcurrentModificationException(format(
                    "There {0, choice, 1#is| 2#are} {0, number, integer} iteration{0, choice, 1#| 2#s} in progress",
                    iterationsInProgress));
        }
        V placeholderValue = parameters.get(placeholderName);
        if ((literalName != null) && !parameters.containsKey(literalName) && literalName.equals(placeholderValue)) {
            parameters.put(literalName, placeholderValue);
            parameters.remove(placeholderName);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends V> ParametersImpl<R> getParameters() {
        return (ParametersImpl<R>) this;
    }

    @Override
    public <R extends V> Map<String, R> getAsMap() {
        return this.<R>getParameters().unmodifiableParameters;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        ParametersImpl<?> that = (ParametersImpl<?>) obj;
        return parameters.equals(that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameters);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ParametersImpl.class.getSimpleName() + "[", "]")
                .add("parameters=" + parameters)
                .toString();
    }
}

package net.celestialdata.plexbot.discord.commandhandler.parameter.converter;

import net.celestialdata.plexbot.discord.commandhandler.Internal;
import net.celestialdata.plexbot.discord.commandhandler.api.Command;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.InvalidParameterFormatException;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.ParameterConverter;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.ParameterType;

import javax.enterprise.context.ApplicationScoped;
import java.math.BigInteger;

import static java.lang.String.format;

/**
 * A parameter converter that reacts to the types {@code number} and {@code integer}
 * and converts the parameter to a {@link BigInteger}.
 */
@SuppressWarnings("unused")
@ApplicationScoped
@Internal
@ParameterType("number")
@ParameterType("integer")
class NumberConverter implements ParameterConverter<Object, BigInteger> {
    /**
     * Constructs a new number converter.
     */
    private NumberConverter() {
    }

    @Override
    public BigInteger convert(String parameter, String type, Command<?> command, Object message,
                              String prefix, String usedAlias, String parameterString) {
        try {
            return new BigInteger(parameter);
        } catch (NumberFormatException nfe) {
            throw new InvalidParameterFormatException(format("'%s' is not a valid number", parameter), nfe);
        }
    }
}

package net.celestialdata.plexbot.discord.commandhandler.parameter.converter;

import net.celestialdata.plexbot.discord.commandhandler.Internal;
import net.celestialdata.plexbot.discord.commandhandler.api.Command;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.InvalidParameterFormatException;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.ParameterConverter;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.ParameterType;

import javax.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;

import static java.lang.String.format;

/**
 * A parameter converter that reacts to the type {@code decimal}
 * and converts the parameter to a {@link BigDecimal}.
 */
@SuppressWarnings("unused")
@ApplicationScoped
@Internal
@ParameterType("decimal")
class DecimalConverter implements ParameterConverter<Object, BigDecimal> {

    @Override
    public BigDecimal convert(String parameter, String type, Command<?> command, Object message,
                              String prefix, String usedAlias, String parameterString) {
        try {
            return new BigDecimal(parameter);
        } catch (NumberFormatException nfe) {
            throw new InvalidParameterFormatException(format("'%s' is not a valid decimal", parameter), nfe);
        }
    }
}

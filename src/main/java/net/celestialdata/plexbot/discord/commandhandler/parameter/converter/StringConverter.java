package net.celestialdata.plexbot.discord.commandhandler.parameter.converter;

import net.celestialdata.plexbot.discord.commandhandler.Internal;
import net.celestialdata.plexbot.discord.commandhandler.api.Command;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.ParameterConverter;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.ParameterType;

import javax.enterprise.context.ApplicationScoped;

/**
 * A parameter converter that reacts to the types {@code string} and {@code text} and just returns the parameter as-is.
 * This can be helpful to specify a type for every parameter explicitly and it is mandatory if a colon in a parameter
 * name is necessary.
 */
@SuppressWarnings("unused")
@ApplicationScoped
@Internal
@ParameterType("string")
@ParameterType("text")
class StringConverter implements ParameterConverter<Object, String> {

    @Override
    public String convert(String parameter, String type, Command<?> command, Object message,
                          String prefix, String usedAlias, String parameterString) {
        return parameter;
    }
}

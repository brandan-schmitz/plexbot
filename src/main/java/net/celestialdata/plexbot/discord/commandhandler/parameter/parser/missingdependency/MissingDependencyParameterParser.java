package net.celestialdata.plexbot.discord.commandhandler.parameter.parser.missingdependency;

import net.celestialdata.plexbot.discord.commandhandler.api.Command;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.ParameterParser;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.ParameterParser.Typed;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.Parameters;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

/**
 * A parameter parser that is present if the ANTLR dependency is missing and throws an
 * {@link UnsupportedOperationException} if used.
 */
@SuppressWarnings("unused")
@ApplicationScoped
@Default
@Typed
public class MissingDependencyParameterParser implements ParameterParser {
    /**
     * Throws an {@link UnsupportedOperationException} as the ANTLR dependency is missing.
     */
    private MissingDependencyParameterParser() {
        throw new UnsupportedOperationException("ANTLR runtime is missing");
    }

    @Override
    public <V> Parameters<V> parse(Command<?> command, Object message, String prefix, String usedAlias, String parameterString) {
        throw new AssertionError();
    }
}

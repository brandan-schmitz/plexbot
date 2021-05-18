package net.celestialdata.plexbot.discord.commandhandler.parameter.parser;

import net.celestialdata.plexbot.discord.commandhandler.api.Command;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.Parameters;
import net.celestialdata.plexbot.discord.commandhandler.parameter.ParametersImpl;

import javax.enterprise.context.ApplicationScoped;
import java.util.*;

/**
 * The non-typed parameter parser that just returns {@code String} parameter values for single-valued parameters and
 * {@code List<String>} for multi-valued parameters.
 */
@SuppressWarnings("unused")
@ApplicationScoped
public class UntypedParameterParser extends BaseParameterParser {
    /**
     * Constructs a new untyped parameter parser.
     */
    private UntypedParameterParser() {
    }

    @Override
    public <V> Parameters<V> parse(Command<?> command, Object message, String prefix, String usedAlias, String parameterString) {
        return parse(command, prefix, usedAlias, parameterString, (parameterMatcher, groupNamesByTokenName) -> {
            Collection<String> firstTokenValues = new ArrayList<>();
            Map<String, Object> parameters = new HashMap<>();
            groupNamesByTokenName.forEach((tokenName, groupNames) -> groupNames
                    .stream()
                    .map(parameterMatcher::group)
                    .filter(Objects::nonNull)
                    .forEach(tokenValue -> addParameterValue(parameters, tokenName, tokenValue, firstTokenValues)));
            return new ParametersImpl<>(parameters);
        });
    }
}

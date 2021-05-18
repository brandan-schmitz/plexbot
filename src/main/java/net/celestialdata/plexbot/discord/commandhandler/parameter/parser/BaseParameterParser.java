package net.celestialdata.plexbot.discord.commandhandler.parameter.parser;

import net.celestialdata.plexbot.discord.commandhandler.api.Command;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.ParameterParseException;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.ParameterParser;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.Parameters;
import net.celestialdata.plexbot.discord.commandhandler.parameter.ParametersImpl;
import net.celestialdata.plexbot.discord.commandhandler.usage.UsageLexer;
import net.celestialdata.plexbot.discord.commandhandler.usage.UsageParser;
import net.celestialdata.plexbot.discord.commandhandler.usage.UsageParser.UsageContext;
import net.celestialdata.plexbot.discord.commandhandler.usage.UsagePatternBuilder;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;

/**
 * A base class for the parameter parsers that does the common logic of parsing the usage string into an AST,
 * transforming the usage AST to a regular expression pattern and testing the parameter string against the pattern,
 * then applying the parser-specific parsing logic.
 * It also provides a helper method for wrapping multi-valued parameters in lists.
 */
public abstract class BaseParameterParser implements ParameterParser {
    /**
     * A cache for usage trees built from usage specifications so that the usage parser does not need to be invoked
     * multiple times for the same usage pattern.
     */
    private final Map<String, UsageContext> usageTreeCache = new ConcurrentHashMap<>();
    /**
     * A usage pattern builder to transform usage ASTs to regular expression patterns which also holds
     * the token name to group names mapping for transformed usage patterns.
     */
    @Inject
    UsagePatternBuilder usagePatternBuilder;

    /**
     * A helper method for wrapping multi-valued parameters in lists automatically.
     *
     * <p>As the usual case is, that a parameter is single-valued this method only creates new lists if necessary.
     * To determine this, the {@code firstParameterValues} parameter is necessary which is used as a memory which
     * parameters have their first value currently and need to be wrapped in a list if another value is added.
     * This is necessary to differentiate between lists that were added by this method and lists that were created
     * by a parameter converter.
     *
     * @param parameters           the parameters map to which the given parameter name and value should be added
     * @param parameterName        the name of the parameter to be added
     * @param parameterValue       the value of the parameter to be added
     * @param firstParameterValues a collection used for storing which parameter currently holds its first value
     */
    @SuppressWarnings("unchecked")
    protected static void addParameterValue(Map<String, Object> parameters, String parameterName,
                                            Object parameterValue, Collection<String> firstParameterValues) {
        parameters.compute(parameterName, (key, parameterValues) -> {
            if (parameterValues == null) {
                firstParameterValues.add(parameterName);
                return parameterValue;
            }
            if (firstParameterValues.contains(parameterName)) {
                firstParameterValues.remove(parameterName);
                ArrayList<Object> result = new ArrayList<>();
                result.add(parameterValues);
                result.add(parameterValue);
                return result;
            }
            ((List<? super Object>) parameterValues).add(parameterValue);
            return parameterValues;
        });
    }

    /**
     * Returns the parsed parameters for the usage of the given command that was triggered using the given prefix, alias
     * and parameter string with an optional implicit downcast for the values. This method does the common logic, that
     * is it parses the usage string into an AST, transforms the usage AST into a regular expression pattern and and
     * checks whether the parameter string matches the pattern. It then uses the given parse logic to which it supplies
     * the regular expression matcher and the mapping of token names to group names in the regular expression. The
     * parse logic then is responsible for transforming these arguments to a {@code Parameters<V>} instance that will
     * then be returned.
     *
     * @param command         the command of which the usage should be used to parse the parameters
     * @param prefix          the command prefix that was used to invoke the command
     * @param usedAlias       the alias that was used to invoke the command
     * @param parameterString the parameter string to parse
     * @param parseLogic      the parser specific logic that actually extracts and maybe converts the values
     * @param <V>             the class to which the values are implicitly downcasted
     * @return the parsed and maybe converted parameters
     * @throws ParameterParseException if the parameter string does not adhere to the usage pattern of the given
     *                                 command, which includes that there are arguments given when none were
     *                                 expected; the message is suitable to be directly forwarded to end users
     */
    protected <V> Parameters<V> parse(Command<?> command, String prefix, String usedAlias, String parameterString,
                                      BiFunction<Matcher, Map<String, List<String>>, Parameters<V>> parseLogic) {
        Optional<String> optionalUsage = command.getUsage();
        if (optionalUsage.isPresent()) {
            String usage = optionalUsage.get();

            UsageContext usageTree = usageTreeCache.computeIfAbsent(usage, key -> {
                UsageLexer usageLexer = new UsageLexer(CharStreams.fromString(usage));
                UsageParser usageParser = new UsageParser(new CommonTokenStream(usageLexer));
                return usageParser.usage();
            });
            Pattern usagePattern = usagePatternBuilder.getPattern(usageTree);

            Matcher parameterMatcher = usagePattern.matcher(parameterString.trim());
            if (parameterMatcher.matches()) {
                return parseLogic.apply(parameterMatcher, usagePatternBuilder.getGroupNamesByTokenName(usageTree));
            } else {
                throw new ParameterParseException(format(
                        "Wrong arguments for command `%s%s`\nUsage: `%1$s%2$s %s`",
                        prefix, usedAlias, usage));
            }
        } else if (parameterString.chars().allMatch(Character::isWhitespace)) {
            return new ParametersImpl<>(emptyMap());
        } else {
            throw new ParameterParseException(format("Command `%s%s` does not expect arguments", prefix, usedAlias));
        }
    }

    @Override
    public String toString() {
        Class<? extends BaseParameterParser> clazz = getClass();
        String className = clazz.getSimpleName();
        if (className.isEmpty()) {
            className = clazz.getTypeName().substring(clazz.getPackage().getName().length() + 1);
        }
        return new StringJoiner(", ", className + "[", "]")
                .add("usagePatternBuilder=" + usagePatternBuilder)
                .add("usageTreeCache=" + usageTreeCache)
                .toString();
    }
}

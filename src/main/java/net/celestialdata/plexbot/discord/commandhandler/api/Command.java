package net.celestialdata.plexbot.discord.commandhandler.api;

import net.celestialdata.plexbot.discord.commandhandler.InvalidAnnotationCombinationException;
import net.celestialdata.plexbot.discord.commandhandler.api.annotation.Alias;
import net.celestialdata.plexbot.discord.commandhandler.api.annotation.Description;
import net.celestialdata.plexbot.discord.commandhandler.api.annotation.RestrictedTo;
import net.celestialdata.plexbot.discord.commandhandler.api.annotation.RestrictionPolicy;
import net.celestialdata.plexbot.discord.commandhandler.api.restriction.*;
import net.celestialdata.plexbot.discord.commandhandler.api.restriction.javacord.ChannelJavacord;
import net.celestialdata.plexbot.discord.commandhandler.api.restriction.javacord.RoleJavacord;
import net.celestialdata.plexbot.discord.commandhandler.api.restriction.javacord.ServerJavacord;
import net.celestialdata.plexbot.discord.commandhandler.api.restriction.javacord.UserJavacord;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.lang.Character.toLowerCase;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static net.celestialdata.plexbot.discord.commandhandler.api.annotation.RestrictionPolicy.Policy.NONE_OF;

/**
 * A command that can be triggered by messages of the given type.
 *
 * @param <M> the class of the messages for which this command can be triggered
 */
public interface Command<M> {
    /**
     * The regex pattern string for one parameter separator character. It matches one whitespace character.
     */
    String PARAMETER_SEPARATOR_CHARACTER = "\\s";

    /**
     * The pattern that is used to split parameters. It matches an arbitrary amount of whitespaces.
     */
    Pattern PARAMETER_SEPARATOR_PATTERN = Pattern.compile(PARAMETER_SEPARATOR_CHARACTER + "++");

    /**
     * Returns an array of parameters from the given parameter string. The parameter string is split at any sequence of
     * whitespace characters. If you expect three parameters, you should set {@code maxParameters} to four,
     * so you can easily test the length of the returned array whether too many parameters were given to the command.
     *
     * @param parameterString the parameter string to split into single parameters
     * @param maxParameters   the maximum amount of parameters to return, the last will hold all remaining text
     * @return an array of parameters from the given parameter string
     */
    static String[] getParameters(String parameterString, int maxParameters) {
        if (parameterString.chars().allMatch(Character::isWhitespace)) {
            return new String[0];
        }
        return PARAMETER_SEPARATOR_PATTERN.split(parameterString, maxParameters);
    }

    /**
     * Returns the aliases for this command.
     *
     * <p>The default implementation of this method returns the aliases configured using the {@link Alias @Alias}
     * annotation. If no alias is configured by annotation, the class name, stripped by {@code Command} or {@code Cmd}
     * suffix and / or prefix if present and the first letter (in lowercase) is used as default.
     *
     * <p>If this method is overwritten and there are annotations, the method overwrite takes precedence.
     *
     * @return the aliases for this command
     * @see Alias @Alias
     */
    default List<String> getAliases() {
        @SuppressWarnings("rawtypes")
        Class<? extends Command> clazz = getClass();

        List<String> annotatedAliases = Arrays
                .stream(clazz.getAnnotationsByType(Alias.class))
                .map(Alias::value)
                .collect(toList());

        if (!annotatedAliases.isEmpty()) {
            return annotatedAliases;
        }

        String className = clazz.getSimpleName();
        if (className.isEmpty()) {
            className = clazz.getTypeName().substring(clazz.getPackage().getName().length() + 1);
        }
        String defaultAlias = className.replaceAll("(?i)^(?:Command|Cmd)|(?:Command|Cmd)|_Subclass$", "");
        defaultAlias = defaultAlias.replaceFirst("^.", Character.toString(toLowerCase(defaultAlias.charAt(0))));
        return singletonList(defaultAlias);
    }

    /**
     * Returns the description of this command.
     * Currently, this description is used nowhere, but can for example be displayed in an own help command.
     *
     * <p>The default implementation of this method returns the description configured using the
     * {@link Description @Description} annotation. If no description is configured by annotation,
     * an empty {@code Optional} is used as default.
     *
     * <p>If this method is overwritten and the annotation is present, the method overwrite takes precedence.
     *
     * @return the description of this command
     * @see Description @Description
     */
    @SuppressWarnings("unused")
    default Optional<String> getDescription() {
        return Optional
                .ofNullable(getClass().getAnnotation(Description.class))
                .map(Description::value);
    }

    /**
     * Returns the restriction rules chain for this command.
     *
     * <p>Complex boolean logic can either be formulated using the methods of {@code RestrictionChainElement} or by
     * providing an own {@link Restriction} implementation. For the latter also helpers like {@link ChannelJavacord},
     * {@link RoleJavacord}, {@link ServerJavacord}, {@link UserJavacord}, {@link AllOf}, {@link AnyOf}, or
     * {@link NoneOf} can be used as super classes.
     *
     * <p>The default implementation of this method returns the restrictions configured using the
     * {@link RestrictedTo @RestrictedTo} annotation combined according to the
     * {@link RestrictionPolicy @RestrictionPolicy} annotation. If no {@code @RestrictedTo} annotation is present,
     * this method behaves as if the {@link Everyone} restriction was applied.
     *
     * <p>If this method is overwritten and the annotation is present, the method overwrite takes precedence.
     *
     * @return the restriction rules for this command
     * @see RestrictedTo @RestrictedTo
     * @see RestrictionPolicy @RestrictionPolicy
     */
    default RestrictionChainElement getRestrictionChain() {
        List<Class<? extends Restriction<?>>> restrictions = Arrays.stream(getClass().getAnnotationsByType(RestrictedTo.class))
                .map(RestrictedTo::value)
                .collect(toList());
        int restrictionsAmount = restrictions.size();

        // no restrictions, everyone can use it
        if (restrictionsAmount == 0) {
            return new RestrictionChainElement(Everyone.class);
        }

        RestrictionPolicy restrictionPolicy = getClass().getAnnotation(RestrictionPolicy.class);

        // one restriction
        if (restrictionsAmount == 1) {
            return restrictions.stream()
                    .map(RestrictionChainElement::new)
                    .map(restrictionChainElement ->
                            (restrictionPolicy != null) && (restrictionPolicy.value() == NONE_OF)
                                    ? restrictionChainElement.negate()
                                    : restrictionChainElement)
                    .findAny()
                    .orElseThrow(AssertionError::new);
        }

        // multiple restrictions, but no policy
        if (restrictionPolicy == null) {
            throw new InvalidAnnotationCombinationException(format("@RestrictionPolicy is mandatory if multiple @RestrictedTo annotations are given (%s)", getClass()));
        }

        switch (restrictionPolicy.value()) {
            case ALL_OF:
                return restrictions.stream()
                        .map(RestrictionChainElement::new)
                        .reduce(RestrictionChainElement::and)
                        .orElseThrow(AssertionError::new);

            case ANY_OF:
                return restrictions.stream()
                        .map(RestrictionChainElement::new)
                        .reduce(RestrictionChainElement::or)
                        .orElseThrow(AssertionError::new);

            case NONE_OF:
                return restrictions.stream()
                        .map(RestrictionChainElement::new)
                        .reduce(RestrictionChainElement::or)
                        .orElseThrow(AssertionError::new)
                        .negate();

            default:
                throw new AssertionError(format("Unhandled switch case for policy '%s'", restrictionPolicy.value()));
        }
    }

    /**
     * Returns whether this command should be executed asynchronously.
     *
     * <p>How exactly this is implemented is up to the command handler that evaluates this command. Usually the command
     * will be executed in some thread pool. But it would also be valid for a command handler to execute each
     * asynchronous command execution in a new thread, so using this can add significant overhead if overused. As long
     * as a command is not doing long-running or blocking operations it might be a good idea to not execute the command
     * asynchronously. But if long-running or blocking operations are done in the command code directly, depending on
     * the underlying message framework it might be a good idea to execute the command asynchronously to not block
     * message dispatching which could introduce serious lag to the command execution.
     *
     * <p>As the command executions are potentially done on different threads, special care must be taken
     * if the command holds state, to make sure this state is accessed in a thread-safe manner. This can of course also
     * happen without the command being configured asynchronously if the underlying message framework dispatches message
     * events on different threads.
     *
     * @return whether this command should be executed asynchronously
     */
    @SuppressWarnings("SameReturnValue")
    default boolean isAsynchronous() {
        return true;
    }

    void handleFailure(Throwable error);

    /**
     * Executes this command.
     *
     * @param incomingMessage the incoming message that caused this command to be executed
     * @param prefix          the command prefix that was used to trigger this command
     * @param usedAlias       the alias that was used to trigger this command
     * @param parameterString the remaining text after the command prefix and alias
     */
    @SuppressWarnings("unused")
    void execute(M incomingMessage, String prefix, String usedAlias, String parameterString);
}

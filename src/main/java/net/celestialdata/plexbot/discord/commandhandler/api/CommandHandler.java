package net.celestialdata.plexbot.discord.commandhandler.api;

import io.quarkus.arc.log.LoggerName;
import io.quarkus.runtime.StartupEvent;
import net.celestialdata.plexbot.discord.commandhandler.Internal;
import net.celestialdata.plexbot.discord.commandhandler.api.event.javacord.CommandNotAllowedEventJavacord;
import net.celestialdata.plexbot.discord.commandhandler.api.event.javacord.CommandNotFoundEventJavacord;
import net.celestialdata.plexbot.discord.commandhandler.api.prefix.PrefixProvider;
import net.celestialdata.plexbot.discord.commandhandler.api.restriction.Restriction;
import net.celestialdata.plexbot.discord.commandhandler.restriction.RestrictionLookup;
import net.celestialdata.plexbot.discord.commandhandler.util.lazy.LazyReferenceBySupplier;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.stream.Collectors.*;
import static net.celestialdata.plexbot.discord.commandhandler.api.Command.PARAMETER_SEPARATOR_CHARACTER;
import static net.celestialdata.plexbot.discord.commandhandler.api.Command.getParameters;

/**
 * A base class for command handlers that does the common logic.
 *
 * <p>Each method of this class starting with {@code do}, should usually be called by a subclass. Typically per each
 * such method a subclass will have an according method that gets the needed arguments injected by the CDI framework.
 * CDI cannot inject beans into methods that use wildcards (like {@code Restriction<? super M>}) but only into methods
 * that define concrete type arguments (like {@code Restriction<? super Message>}). Due to this fact, this class cannot
 * get the beans injected themselves, but has to rely on the subclass to get the beans injected and forward them to the
 * superclass.
 *
 * <p>If a subclass needs to do additional actions like registering message listeners on injected beans, this could for
 * example be done in a method annotated with {@link PostConstruct @PostConstruct}.
 *
 * @param <M> the class of the messages this command handler processes
 */
public abstract class CommandHandler<M> {
    /**
     * The logger for this command handler.
     */
    @LoggerName("net.celestialdata.plexbot.discord.commandhandler.CommandHandlerDiscord")
    Logger logger;

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    ManagedExecutor executor;

    /**
     * The default prefix provider that is used if no custom prefix provider was provided.
     */
    @Inject
    @Internal
    Instance<PrefixProvider<? super M>> defaultPrefixProvider;
    /**
     * The actual command by possible aliases for lookup.
     */
    private LazyReferenceBySupplier<Map<String, Command<? super M>>> commandByAlias =
            new LazyReferenceBySupplier<>(() -> {
                logger.info("Got no commands injected");
                return Collections.emptyMap();
            });
    /**
     * The pattern to match all possible commands.
     */
    private LazyReferenceBySupplier<Pattern> commandPattern =
            new LazyReferenceBySupplier<>(() -> Pattern.compile("[^\\w\\W]"));
    /**
     * The custom prefix provider that was provided.
     */
    private Instance<PrefixProvider<? super M>> customPrefixProvider;
    /**
     * The actual prefix provider that is used.
     */
    private final LazyReferenceBySupplier<PrefixProvider<? super M>> prefixProvider =
            new LazyReferenceBySupplier<>(() ->
                    ((customPrefixProvider == null) || customPrefixProvider.isUnsatisfied()
                            ? defaultPrefixProvider
                            : customPrefixProvider)
                            .get()
            );
    /**
     * The alias and parameter string transformer that was provided.
     */
    private Instance<AliasAndParameterStringTransformer<? super M>> injectedAliasAndParameterStringTransformer;
    /**
     * The alias and parameter string transformer that is used.
     */
    private LazyReferenceBySupplier<AliasAndParameterStringTransformer<? super M>> aliasAndParameterStringTransformer;
    /**
     * The available restrictions for this command handler.
     */
    private LazyReferenceBySupplier<RestrictionLookup<M>> availableRestrictions =
            new LazyReferenceBySupplier<>(() -> {
                logger.info("Got no restrictions injected");
                return new RestrictionLookup<>();
            });

    /**
     * Ensures the implementing command handlers are initialized on startup.
     *
     * @param event the event that was fired due to the application scope being initialized
     */
    @SuppressWarnings("EmptyMethod")
    void ensureInitializationAtStartup(@Observes StartupEvent event) {
        // just ensure initialization at startup
    }

    /**
     * Sets the available restrictions for this command handler.
     *
     * <p>A subclass will typically have a method where it gets these injected, specific to the handled message type,
     * and forwards its parameter as argument to this method like
     *
     * <pre>{@code
     * }&#64;{@code Inject
     * private void setAvailableRestrictions(Instance<Restriction<? super Message>> availableRestrictions) {
     *     doSetAvailableRestrictions(availableRestrictions);
     * }
     * }</pre>
     *
     * @param availableRestrictions the available restrictions for this command handler
     */
    protected void doSetAvailableRestrictions(Instance<Restriction<? super M>> availableRestrictions) {
        this.availableRestrictions = new LazyReferenceBySupplier<>(() -> {
            RestrictionLookup<M> result = new RestrictionLookup<>();
            Collection<Restriction<? super M>> restrictions = availableRestrictions.stream().peek(restriction ->
                    logger.debug("Restriction " + restriction.getClass().getName() + " injected")
            ).collect(toList());
            result.addAllRestrictions(restrictions);
            logger.info("Injected " + restrictions.size() + " restrictions");
            return result;
        });
    }

    /**
     * Sets the commands for this command handler.
     *
     * <p>A subclass will typically have a method where it gets these injected, specific to the handled message type,
     * and forwards its parameter as argument to this method like
     *
     * <pre>{@code
     * }&#64;{@code Inject
     * private void setCommands(Instance<Command<? super Message>> commands) {
     *     doSetCommands(commands);
     * }
     * }</pre>
     *
     * @param commands the available commands for this command handler
     */
    protected void doSetCommands(Instance<Command<? super M>> commands) {
        commandByAlias = new LazyReferenceBySupplier<>(() -> {
            Map<String, Command<? super M>> result = new ConcurrentHashMap<>();
            Collection<Command<? super M>> actualCommands = commands.stream().peek(command ->
                    logger.debug("Injected " + command.getClass().getName() + " command")
            ).collect(toList());
            logger.info("Injected " + actualCommands.size() + " commands");

            // verify the restriction annotations combination
            actualCommands.forEach(Command::getRestrictionChain);

            // build the alias to command map
            result.putAll(actualCommands.stream()
                    .flatMap(command -> command.getAliases().stream()
                            .map(alias -> new SimpleImmutableEntry<>(alias, command)))
                    .collect(toMap(
                            Entry::getKey,
                            Entry::getValue,
                            (cmd1, cmd2) -> {
                                throw new IllegalStateException(format(
                                        "The same alias was defined for the two commands '%s' and '%s'",
                                        cmd1,
                                        cmd2));
                            })));

            return result;
        });

        // build the command matching pattern
        commandPattern = new LazyReferenceBySupplier<>(() -> Pattern.compile(
                commandByAlias.get().keySet().stream()
                        .map(Pattern::quote)
                        .collect(joining("|", "(?s)^(?<alias>", ")(?=\\s|$)"
                                + PARAMETER_SEPARATOR_CHARACTER + "*+"
                                + "(?<parameterString>.*+)$"))));
    }

    /**
     * Sets the custom prefix provider for this command handler.
     *
     * <p>A subclass will typically have a method where it gets this injected, specific to the handled message type,
     * and forwards its parameter as argument to this method like
     *
     * <pre>{@code
     * }&#64;{@code Inject
     * private void setCustomPrefixProvider(Instance<PrefixProvider<? super Message>> customPrefixProvider) {
     *     doSetCustomPrefixProvider(customPrefixProvider);
     * }
     * }</pre>
     *
     * @param customPrefixProvider the custom prefix provider for this command handler
     */
    protected void doSetCustomPrefixProvider(Instance<PrefixProvider<? super M>> customPrefixProvider) {
        this.customPrefixProvider = customPrefixProvider;
    }

    /**
     * Sets the custom alias and parameter string transformer for this command handler.
     *
     * <p>A subclass will typically have a method where it gets this injected, specific to the handled message type,
     * and forwards its parameter as argument to this method like
     *
     * <pre>{@code
     * }&#64;{@code Inject
     * private void setAliasAndParameterStringTransformer(
     *         Instance<AliasAndParameterStringTransformer<? super Message>> aliasAndParameterStringTransformer) {
     *     doSetAliasAndParameterStringTransformer(aliasAndParameterStringTransformer);
     * }
     * }</pre>
     *
     * <p><b>Important:</b> This method should be called directly in the injectable method as shown above, not in some
     * {@link PostConstruct @PostConstruct} annotated method, as the {@code @PostConstruct} stage is used to decide
     * whether an alias and parameter string transformer should be used, so it has to already be set at that point.
     *
     * @param aliasAndParameterStringTransformer the alias and parameter string transformer for this command handler
     */
    protected void doSetAliasAndParameterStringTransformer(
            Instance<AliasAndParameterStringTransformer<? super M>> aliasAndParameterStringTransformer) {
        this.injectedAliasAndParameterStringTransformer = aliasAndParameterStringTransformer;
    }

    /**
     * Determines whether an alias and parameter string transformer is provided.
     */
    @PostConstruct
    void determineAliasAndParameterStringTransformer() {
        if ((injectedAliasAndParameterStringTransformer != null)
                && (!injectedAliasAndParameterStringTransformer.isUnsatisfied())) {
            aliasAndParameterStringTransformer =
                    new LazyReferenceBySupplier<>(() -> injectedAliasAndParameterStringTransformer.get());
        }
    }

    /**
     * Handles the given message with the given textual content. The textual content needs to be given separately as
     * this generic method does not know now to get the content from the message.
     *
     * <p>This method checks the message content for a command invocation, checks the configured restrictions for the
     * command and if all passed, invokes the command synchronously or asynchronously as configured. If the command was
     * denied by any restriction, a command not allowed CDI event is fired asynchronously. (See for example
     * {@link CommandNotAllowedEventJavacord}) If the message started with the command prefix, but no matching command
     * was found, a command not found CDI event is fired asynchronously. (See for example
     * {@link CommandNotFoundEventJavacord})
     *
     * @param message        the message that potentially contains a command invocation
     * @param messageContent the textual content of the given message
     */
    protected void doHandleMessage(M message, String messageContent) {
        String prefix = prefixProvider.get().getCommandPrefix(message);
        int prefixLength = prefix.length();
        boolean emptyPrefix = prefixLength == 0;
        if (emptyPrefix) {
            logger.warn("The command prefix is empty, this means that every message will be checked against a " +
                    "regular expression and that for every non-matching message an event will be sent. It is better " +
                    "for the performance if you set a command prefix instead of including it in the aliases directly. " +
                    "If you do not care, just configure your logging framework to ignore this warning, as it also " +
                    "costs additional performance and might hide other important log messages. ;-)");
        }
        if (messageContent.startsWith(prefix)) {
            String messageContentWithoutPrefix = messageContent.substring(prefix.length()).trim();
            AliasAndParameterString aliasAndParameterString =
                    determineAliasAndParameterString(message, messageContentWithoutPrefix);

            // no alias determined => command not found
            if (aliasAndParameterString == null) {
                logger.debug("No matching command found");
                String[] parameters = getParameters(messageContentWithoutPrefix, 2);
                fireCommandNotFoundEvent(message, prefix, parameters.length == 0 ? "" : parameters[0]);
                return;
            }

            String usedAlias = aliasAndParameterString.getAlias();
            Command<? super M> command = commandByAlias.get().get(usedAlias);
            // alias did not match any command => command not found
            if (command == null) {
                logger.debug("No matching command found");
                fireCommandNotFoundEvent(message, prefix, usedAlias);
                return;
            }

            if (isCommandAllowed(message, command)) {
                String parameterString = aliasAndParameterString.getParameterString();
                Runnable commandExecutor = () -> command.execute(message, prefix, usedAlias, parameterString);
                if (command.isAsynchronous()) {
                    executeAsync(message, commandExecutor, command);
                } else {
                    commandExecutor.run();
                }
            } else {
                logger.debug("Command " + command + " was not allowed by restrictions");
                fireCommandNotAllowedEvent(message, prefix, usedAlias);
            }
        }
    }

    /**
     * Determines the alias and parameter string from the given message and content without prefix.
     *
     * @param message                     the message from which to determine the details
     * @param messageContentWithoutPrefix the message content without prefix
     * @return the alias and parameter string from the given message and content
     */
    AliasAndParameterString determineAliasAndParameterString(M message, String messageContentWithoutPrefix) {
        Matcher commandMatcher = commandPattern.get().matcher(messageContentWithoutPrefix);

        AliasAndParameterString aliasAndParameterString = null;

        // get the alias and parameter string from the defined aliases
        if (commandMatcher.find()) {
            String usedAlias = commandMatcher.group("alias");
            String parameterString = commandMatcher.group("parameterString");
            aliasAndParameterString = new AliasAndParameterString(usedAlias, parameterString);
        }

        // use the alias and parameter string transformer if provided
        if (aliasAndParameterStringTransformer == null) {
            return aliasAndParameterString;
        }
        return aliasAndParameterStringTransformer
                .get()
                .transformAliasAndParameterString(message, aliasAndParameterString);
    }

    /**
     * Returns whether the given command that is caused by the given message should be allowed according to the
     * configured restrictions.
     *
     * @param message the message that caused the given command
     * @param command the command that is caused by the given message
     * @return whether the given command that is caused by the given message should be allowed
     */
    private boolean isCommandAllowed(M message, Command<? super M> command) {
        return command.getRestrictionChain().isCommandAllowed(message, availableRestrictions.get());
    }

    /**
     * Fires a command not allowed CDI event asynchronously using {@link Event#fireAsync(Object)} that can be handled
     * using {@link ObservesAsync @ObservesAsync}.
     *
     * @param message   the message that contains the command but was not allowed
     * @param prefix    the command prefix that was used to trigger the command
     * @param usedAlias the alias that was used to trigger the command
     * @see ObservesAsync @ObservesAsync
     */
    protected abstract void fireCommandNotAllowedEvent(M message, String prefix, String usedAlias);

    /**
     * Fires a command not found CDI event asynchronously using {@link Event#fireAsync(Object)} that can be handled
     * using {@link ObservesAsync @ObservesAsync}.
     *
     * @param message   the message that contains the command that was not found
     * @param prefix    the command prefix that was used to trigger the command
     * @param usedAlias the alias that was used to trigger the command
     * @see ObservesAsync @ObservesAsync
     */
    protected abstract void fireCommandNotFoundEvent(M message, String prefix, String usedAlias);

    /**
     * Executes the given command executor that is caused by the given message asynchronously.
     *
     * <p>The default implementation executes the command in a thread pool and logs any throwables on error level.
     * A subclass that has some means to execute tasks asynchronously anyways like the thread pool provided by Javacord,
     * can overwrite this message and replace the asynchronous execution implementation.
     *
     * @param message         the message that caused the given command executor
     * @param commandExecutor the executor that runs the actual command implementation
     */
    protected void executeAsync(M message, Runnable commandExecutor, Command<? super M> command) {
        executor.runAsync(commandExecutor).whenComplete((nothing, throwable) -> {
            if (throwable != null) {
                logger.error("Exception while executing command asynchronously", throwable);
                command.handleFailure(throwable);
            }
        });
    }
}
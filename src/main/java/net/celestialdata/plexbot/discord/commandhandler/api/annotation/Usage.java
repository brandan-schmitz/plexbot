package net.celestialdata.plexbot.discord.commandhandler.api.annotation;

import net.celestialdata.plexbot.discord.commandhandler.api.Command;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.ParameterParser;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation with which the usage of the command can be configured.
 * This usage can for example be displayed in an own help command.
 *
 * <p>When using the {@link ParameterParser}, the usage string has to follow
 * a pre-defined format that is described there.
 *
 * <p>Alternatively to using this annotation the {@link Command#getUsage()} method can be overwritten.
 * If that method is overwritten and this annotation is used, the method overwrite takes precedence.
 * That method is also what should be used to retrieve the configured usage.
 *
 * @see Command#getUsage()
 * @see ParameterParser
 */
@Retention(RUNTIME)
@Target(TYPE)
@Documented
public @interface Usage {
    /**
     * Returns the usage of the annotated command.
     *
     * @return the usage of the annotated command
     */
    String value();
}

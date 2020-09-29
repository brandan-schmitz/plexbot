package net.celestialdata.plexbot.commandhandler;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A command annotation.
 *
 * @author Celestialdeath99
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    /**
     * Gets whether the executor should listen to private messages or not.
     *
     * @return Whether the executor should listen to private messages or not.
     */
    boolean privateMessages() default true;

    /**
     * Gets whether the executor should listen to channel messages or not.
     *
     * @return Whether the executor should listen to channel messages or not.
     */
    boolean channelMessages() default true;

    /**
     * Gets the commands the executor should listen to. The first element is the main command.
     *
     * @return The commands the executor should listen to.
     */
    String[] aliases();

    /**
     * Gets the description of the command.
     *
     * @return The description of the command.
     */
    String description() default "none";

    /**
     * Gets the usage of the command.
     * If no usage was provided it will use the first alias.
     *
     * @return The usage of the command.
     */
    String usage() default "";

    /**
     * Gets the permissions required for a user to run the command.
     *
     * @return The permissions required for a user to run the command.
     */
    String requiredPermissions() default "none";

    /**
     * Gets whether the command should be shown in the help page or not.
     *
     * @return Whether the command should be shown if the help page or not.
     */
    boolean showInHelpPage() default true;

    /**
     * Gets whether the command should be executed async or not. If not the thread of the message listener is used.
     *
     * @return Whether the command should be executed async or not.
     */
    boolean async() default false;

    /**
     * Gets whether the bot has to be mentioned to react to a command.
     * This would look like <code>@botname alias</code>
     *
     * @return Whether the bot has to be mentioned to react to a command.
     */
    boolean requiresMention() default false;

    /**
     * Gets the category of the command.
     *
     * @return The category of the command.
     */
    String category() default "";
}

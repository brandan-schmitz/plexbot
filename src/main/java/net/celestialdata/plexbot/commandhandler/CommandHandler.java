package net.celestialdata.plexbot.commandhandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The basic command handler.
 */
public abstract class CommandHandler {

    // From Javacord's DiscordRegexPattern
    static final Pattern USER_MENTION =
            Pattern.compile("(?x)          # enable comment mode \n"
                    + "(?<!                # negative lookbehind \n"
                    + "                    # (do not have uneven amount of backslashes before) \n"
                    + "    (?<!\\\\)       # negative lookbehind (do not have one backslash before) \n"
                    + "    (?:\\\\{2}+)    # exactly two backslashes \n"
                    + "    {0,1000000000}+ # 0 to 1_000_000_000 times \n"
                    + "                    # (basically *, but a lookbehind has to have a maximum length) \n"
                    + "    \\\\            # the one escaping backslash \n"
                    + ")                   # \n"
                    + "<@!?+               # '<@' or '<@!' \n"
                    + "(?<id>[0-9]++)      # the user id as named group \n"
                    + ">                   # '>'");
    protected final HashMap<String, SimpleCommand> commands = new HashMap<>();
    private final List<SimpleCommand> commandList = new ArrayList<>();
    private final HashMap<String, List<String>> permissions = new HashMap<>();

    /**
     * Registers an executor.
     *
     * @param executor The executor to register.
     */
    public void registerCommand(CommandExecutor executor) {
        for (Method method : executor.getClass().getMethods()) {
            Command annotation = method.getAnnotation(Command.class);
            if (annotation == null) {
                continue;
            }
            if (annotation.aliases().length == 0) {
                throw new IllegalArgumentException("Aliases array cannot be empty!");
            }
            CommandHandler.SimpleCommand command = new SimpleCommand(annotation, method, executor);
            for (String alias : annotation.aliases()) {
                // add command to map. It's faster to access it from the map than iterating to the whole list
                commands.put(alias.toLowerCase().replace(" ", ""), command);
            }
            // we need a list, too, because a HashMap is not ordered.
            commandList.add(command);
        }
    }

    /**
     * Gets a map which contains all set permissions.
     * The map's key is the user id, the value is a list with all permissions of this user.
     *
     * @return A map which contains all set permissions.
     */
    public HashMap<String, List<String>> getPermissions() {
        return permissions;
    }

    /**
     * Adds a permission for the user with the given id.
     *
     * @param userId     The id of the user.
     * @param permission The permission to add.
     */
    public void addPermission(String userId, String permission) {
        List<String> permissions = this.permissions.computeIfAbsent(userId, k -> new ArrayList<>());
        permissions.add(permission);
    }

    /**
     * Checks if the user with the given id has the required permission.
     *
     * @param userId     The id of the user.
     * @param permission The permission to check.
     * @return If the user has the given permission.
     */
    public boolean hasPermission(String userId, String permission) {
        if (permission.equals("none") || permission.equals("")) {
            return true;
        }
        List<String> permissions = this.permissions.get(userId);
        if (permissions == null) {
            return false;
        }
        for (String perm : permissions) {
            // user has the permission
            if (checkPermission(perm, permission)) {
                return true;
            }
        }
        // user hasn't enough permissions
        return false;
    }

    /**
     * Gets a list with all commands in the order they were registered.
     * This is useful for automatic help commands.
     *
     * @return A list with all commands the the order they were registered.
     */
    public List<SimpleCommand> getCommands() {
        return Collections.unmodifiableList(commandList);
    }

    /**
     * Checks if you are allowed to do something with the given permission.
     *
     * @param has      The permission the user has.
     * @param required The permission which is required.
     * @return If you can use the command with the given permission.
     */
    private boolean checkPermission(String has, String required) {
        String[] splitHas = has.split("\\.");
        String[] splitRequired = required.split("\\.");
        int lower = Math.min(splitHas.length, splitRequired.length);
        for (int i = 0; i < lower; i++) {
            if (!splitHas[i].equalsIgnoreCase(splitRequired[i])) {
                return splitHas[i].equals("*");
            }
        }
        return splitRequired.length == splitHas.length;
    }

    /**
     * A simple representation of a command.
     */
    public static class SimpleCommand {

        private final Command annotation;
        private final Method method;
        private final CommandExecutor executor;

        /**
         * Class constructor.
         *
         * @param annotation The annotation of the executor's method.
         * @param method     The method which listens to the commands.
         * @param executor   The executor of the method.
         */
        SimpleCommand(Command annotation, Method method, CommandExecutor executor) {
            this.annotation = annotation;
            this.method = method;
            this.executor = executor;
        }

        /**
         * The command annotation of the method.
         *
         * @return The command annotation of the method.
         */
        public Command getCommandAnnotation() {
            return annotation;
        }

        /**
         * Gets the method which listens to the commands.
         *
         * @return The method which listens to the commands.
         */
        public Method getMethod() {
            return method;
        }

        /**
         * Gets the executor of the method.
         *
         * @return The executor of the method.
         */
        CommandExecutor getExecutor() {
            return executor;
        }
    }
}
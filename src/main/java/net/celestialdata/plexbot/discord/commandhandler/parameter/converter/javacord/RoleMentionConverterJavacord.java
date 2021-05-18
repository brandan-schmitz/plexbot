package net.celestialdata.plexbot.discord.commandhandler.parameter.converter.javacord;

import net.celestialdata.plexbot.discord.commandhandler.Internal;
import net.celestialdata.plexbot.discord.commandhandler.api.Command;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.InvalidParameterFormatException;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.InvalidParameterValueException;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.ParameterConverter;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.ParameterType;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.Role;

import javax.enterprise.context.ApplicationScoped;
import java.util.regex.Matcher;

import static java.lang.Long.parseUnsignedLong;
import static java.lang.String.format;
import static org.javacord.api.util.DiscordRegexPattern.ROLE_MENTION;

/**
 * A parameter converter that reacts to the types {@code role_mention} and {@code roleMention}
 * and converts the parameter to a Javacord {@link Role}.
 */
@SuppressWarnings("unused")
@ApplicationScoped
@Internal
@ParameterType("role_mention")
@ParameterType("roleMention")
class RoleMentionConverterJavacord implements ParameterConverter<Message, Role> {

    @Override
    public Role convert(String parameter, String type, Command<?> command, Message message,
                        String prefix, String usedAlias, String parameterString) {
        Matcher roleMatcher = ROLE_MENTION.matcher(parameter);
        if (!roleMatcher.matches()) {
            throw new InvalidParameterFormatException(format("'%s' is not a valid role mention", parameter));
        }

        String roleIdString = roleMatcher.group("id");
        long roleId;
        try {
            roleId = parseUnsignedLong(roleIdString);
        } catch (NumberFormatException nfe) {
            throw new InvalidParameterFormatException(format("'%s' is not a valid role mention", parameter), nfe);
        }

        return message
                .getApi()
                .getRoleById(roleId)
                .orElseThrow(() -> new InvalidParameterValueException(format("role for id '%s' was not found", roleIdString)));
    }
}

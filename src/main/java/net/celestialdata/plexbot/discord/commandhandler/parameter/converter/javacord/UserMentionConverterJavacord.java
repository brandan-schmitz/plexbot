package net.celestialdata.plexbot.discord.commandhandler.parameter.converter.javacord;

import net.celestialdata.plexbot.discord.commandhandler.Internal;
import net.celestialdata.plexbot.discord.commandhandler.api.Command;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.InvalidParameterFormatException;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.InvalidParameterValueException;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.ParameterConverter;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.ParameterType;
import net.celestialdata.plexbot.discord.commandhandler.util.ExceptionUtil;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.exception.NotFoundException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.CompletionException;
import java.util.regex.Matcher;

import static java.lang.Long.parseUnsignedLong;
import static java.lang.String.format;
import static org.javacord.api.util.DiscordRegexPattern.USER_MENTION;

/**
 * A parameter converter that reacts to the types {@code user_mention} and {@code userMention}
 * and converts the parameter to a Javacord {@link User}.
 */
@SuppressWarnings("unused")
@ApplicationScoped
@Internal
@ParameterType("user_mention")
@ParameterType("userMention")
class UserMentionConverterJavacord implements ParameterConverter<Message, User> {
    /**
     * An exception utility to sneakily throw checked exceptions
     * and unwrap completion and execution exceptions.
     */
    @Inject
    ExceptionUtil exceptionUtil;

    @Override
    public User convert(String parameter, String type, Command<?> command, Message message,
                        String prefix, String usedAlias, String parameterString) throws Exception {
        Matcher userMatcher = USER_MENTION.matcher(parameter);
        if (!userMatcher.matches()) {
            throw new InvalidParameterFormatException(format("'%s' is not a valid user mention", parameter));
        }

        String userIdString = userMatcher.group("id");
        long userId;
        try {
            userId = parseUnsignedLong(userIdString);
        } catch (NumberFormatException nfe) {
            throw new InvalidParameterFormatException(format("'%s' is not a valid user mention", parameter), nfe);
        }

        try {
            return message
                    .getApi()
                    .getUserById(userId)
                    .handle((user, throwable) -> {
                        if (throwable == null) {
                            return user;
                        }

                        if (throwable instanceof NotFoundException) {
                            throw new InvalidParameterValueException(format("user for id '%s' was not found", userIdString), throwable);
                        }
                        return exceptionUtil.sneakyThrow(throwable);
                    })
                    .join();
        } catch (CompletionException ce) {
            return exceptionUtil.<User, Exception>sneakyThrow(exceptionUtil.unwrapThrowable(ce));
        }
    }
}

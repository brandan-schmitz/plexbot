package net.celestialdata.plexbot.discord.commandhandler.parameter.converter.javacord;

import net.celestialdata.plexbot.discord.commandhandler.Internal;
import net.celestialdata.plexbot.discord.commandhandler.api.Command;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.InvalidParameterFormatException;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.InvalidParameterValueException;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.ParameterConverter;
import net.celestialdata.plexbot.discord.commandhandler.api.parameter.ParameterType;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.message.Message;

import javax.enterprise.context.ApplicationScoped;
import java.util.regex.Matcher;

import static java.lang.Long.parseUnsignedLong;
import static java.lang.String.format;
import static org.javacord.api.util.DiscordRegexPattern.CHANNEL_MENTION;

/**
 * A parameter converter that reacts to the types {@code channel_mention} and {@code channelMention}
 * and converts the parameter to a Javacord {@link Channel}.
 */
@SuppressWarnings("unused")
@ApplicationScoped
@Internal
@ParameterType("channel_mention")
@ParameterType("channelMention")
class ChannelMentionConverterJavacord implements ParameterConverter<Message, Channel> {
    /**
     * Constructs a new channel mention converter for Javacord.
     */
    private ChannelMentionConverterJavacord() {
    }

    @Override
    public Channel convert(String parameter, String type, Command<?> command, Message message,
                           String prefix, String usedAlias, String parameterString) {
        Matcher channelMatcher = CHANNEL_MENTION.matcher(parameter);
        if (!channelMatcher.matches()) {
            throw new InvalidParameterFormatException(format("'%s' is not a valid channel mention", parameter));
        }

        String channelIdString = channelMatcher.group("id");
        long channelId;
        try {
            channelId = parseUnsignedLong(channelIdString);
        } catch (NumberFormatException nfe) {
            throw new InvalidParameterFormatException(format("'%s' is not a valid channel mention", parameter), nfe);
        }

        return message
                .getApi()
                .getChannelById(channelId)
                .orElseThrow(() -> new InvalidParameterValueException(format("channel for id '%s' was not found", channelIdString)));
    }
}

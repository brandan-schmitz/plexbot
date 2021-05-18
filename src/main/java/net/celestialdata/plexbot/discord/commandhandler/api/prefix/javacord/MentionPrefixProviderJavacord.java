package net.celestialdata.plexbot.discord.commandhandler.api.prefix.javacord;

import net.celestialdata.plexbot.discord.commandhandler.api.prefix.PrefixProvider;
import net.celestialdata.plexbot.discord.commandhandler.util.lazy.LazyReferenceByFunction;
import org.javacord.api.entity.message.Message;

import javax.enterprise.context.ApplicationScoped;
import java.util.StringJoiner;

import static java.lang.String.format;

/**
 * A base class for having a mention of the Javacord-based bot as command prefix.
 * To use it, create a trivial subclass of this class and make it a discoverable CDI bean,
 * for example by annotating it with {@link ApplicationScoped @ApplicationScoped}.
 */
@SuppressWarnings("unused")
public abstract class MentionPrefixProviderJavacord implements PrefixProvider<Message> {
    /**
     * The mention string that is used as prefix.
     */
    private final LazyReferenceByFunction<Message, String> prefix =
            new LazyReferenceByFunction<>(message -> format("%s ", message.getApi().getYourself().getMentionTag()));

    @SuppressWarnings("unused")
    @Override
    public String getCommandPrefix(Message message) {
        return prefix.get(message);
    }

    @Override
    public String toString() {
        Class<? extends MentionPrefixProviderJavacord> clazz = getClass();
        String className = clazz.getSimpleName();
        if (className.isEmpty()) {
            className = clazz.getTypeName().substring(clazz.getPackage().getName().length() + 1);
        }
        return new StringJoiner(", ", className + "[", "]")
                .add("prefix=" + prefix)
                .toString();
    }
}

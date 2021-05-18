package net.celestialdata.plexbot.discord;

import net.celestialdata.plexbot.discord.commandhandler.api.prefix.PrefixProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javacord.api.entity.message.Message;

import javax.enterprise.context.ApplicationScoped;

@SuppressWarnings("unused")
@ApplicationScoped
public class PrefixCustomizer implements PrefixProvider<Message> {

    @ConfigProperty(name = "BotSettings.prefix")
    String botPrefix;

    @Override
    public String getCommandPrefix(Message message) {
        return botPrefix;
    }
}
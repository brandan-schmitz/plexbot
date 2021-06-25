package net.celestialdata.plexbot.discord;

import net.celestialdata.plexbot.discord.commandhandler.api.event.javacord.CommandNotFoundEventJavacord;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import java.awt.*;

@ApplicationScoped
public class EventObserver {

    void commandNotFound(@ObservesAsync CommandNotFoundEventJavacord event) {
        event.getMessage().reply(new EmbedBuilder()
                .setTitle("Unknown Command")
                .setDescription("The command you used is not recognized. Please check the usage " +
                        "of your intended command and try again.")
                .addField("Command you used:", "```" + event.getMessage().getContent() + "```")
                .setColor(Color.RED)
        );
    }
}
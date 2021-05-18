package net.celestialdata.plexbot.discord.commands;

import io.quarkus.runtime.Quarkus;
import me.koply.kcommando.integration.impl.javacord.JRunnable;
import me.koply.kcommando.integration.impl.javacord.JavacordCommand;
import me.koply.kcommando.internal.annotations.Commando;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import javax.enterprise.context.ApplicationScoped;
import java.awt.*;

@ApplicationScoped
@Commando(name = "Shutdown", aliases = {"stop", "shutdown"}, ownerOnly = true, privateOnly = true)
public class StopCommand extends JavacordCommand {

    public StopCommand() {
        getInfo().setOwnerOnlyCallback((JRunnable) e -> e.getMessage().reply(new EmbedBuilder()
                .setTitle("Unauthorized")
                .setDescription("This command is only able to be used by a bot owner.")
                .setColor(Color.RED)
        ));

        getInfo().setPrivateOnlyCallback((JRunnable) e -> e.getMessage().reply(new EmbedBuilder()
                .setTitle("Unauthorized")
                .setDescription("This command can only be used in a private chat by the bot owner.")
                .setColor(Color.RED)
        ));
    }

    @Override
    public boolean handle(MessageCreateEvent e) {
        Quarkus.blockingExit();
        return true;
    }
}
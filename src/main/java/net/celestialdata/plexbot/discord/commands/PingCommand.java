package net.celestialdata.plexbot.discord.commands;

import me.koply.kcommando.integration.impl.javacord.JavacordCommand;
import me.koply.kcommando.internal.annotations.Commando;
import org.javacord.api.event.message.MessageCreateEvent;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Commando(name = "ping", aliases = {"ping"})
public class PingCommand extends JavacordCommand {

    @Override
    public boolean handle(MessageCreateEvent e) {
        e.getChannel().sendMessage( "Pong!" );
        return true;
    }
}
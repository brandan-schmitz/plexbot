package net.celestialdata.plexbot.entities;

import net.celestialdata.plexbot.clients.models.omdb.OmdbResult;
import net.celestialdata.plexbot.discord.MessageFormatter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class EntityUtilities {

    @ConfigProperty(name = "ChannelSettings.waitlistChannelId")
    String waitlistChannel;

    @Inject
    MessageFormatter messageFormatter;

    @Inject
    DiscordApi discordApi;

    @Transactional
    public boolean movieExists(String id) {
        return Movie.count("id", id) == 1;
    }

    @Transactional
    public void addWaitlistMovie(OmdbResult movie, Long requestedBy) {
        WaitlistMovie waitlistMovie = new WaitlistMovie();
        var waitlistMessage = new MessageBuilder()
                .setEmbed(messageFormatter.waitlistNotification(movie))
                .send(discordApi.getTextChannelById(waitlistChannel).orElseThrow()).exceptionally(ExceptionLogger.get()
                ).exceptionally(ExceptionLogger.get()).join();

        waitlistMovie.id = movie.imdbID;
        waitlistMovie.title = movie.title;
        waitlistMovie.year = movie.year;
        waitlistMovie.requestedBy = requestedBy;
        waitlistMovie.messageId = waitlistMessage.getId();

        waitlistMovie.persist();
    }
}
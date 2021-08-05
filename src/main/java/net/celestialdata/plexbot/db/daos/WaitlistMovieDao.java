package net.celestialdata.plexbot.db.daos;

import net.celestialdata.plexbot.clients.models.tmdb.TmdbMovie;
import net.celestialdata.plexbot.db.entities.Movie;
import net.celestialdata.plexbot.db.entities.WaitlistMovie;
import net.celestialdata.plexbot.discord.MessageFormatter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@SuppressWarnings({"unused"})
@ApplicationScoped
public class WaitlistMovieDao {

    @ConfigProperty(name = "ChannelSettings.movieWaitlistChannel")
    String movieWaitlistChannel;

    @Inject
    DiscordApi discordApi;

    @Inject
    MessageFormatter messageFormatter;

    @Transactional
    public List<WaitlistMovie> listALl() {
        return WaitlistMovie.listAll();
    }

    @Transactional
    public WaitlistMovie get(int id) {
        return WaitlistMovie.findById(id);
    }

    @Transactional
    public WaitlistMovie getByTmdbId(long tmdbId) {
        return WaitlistMovie.find("tmdbId", tmdbId).firstResult();
    }

    @Transactional
    public WaitlistMovie getByImdbId(String imdbId) {
        return WaitlistMovie.find("imdbId", imdbId).firstResult();
    }

    @Transactional
    public Movie getByMessageId(Long messageId) {
        return WaitlistMovie.find("messageId", messageId).firstResult();
    }

    @Transactional
    public boolean exists(int id) {
        return WaitlistMovie.count("id", id) == 1;
    }

    @Transactional
    public boolean existsByTmdbId(long tmdbId) {
        return WaitlistMovie.count("tmdbId", tmdbId) == 1;
    }

    @Transactional
    public boolean existsByImdbId(String imdbId) {
        return WaitlistMovie.count("imdbId", imdbId) == 1;
    }

    @Transactional
    public boolean existsByMessageId(long messageId) {
        return WaitlistMovie.count("messageId", messageId) == 1;
    }

    @SuppressWarnings("DuplicatedCode")
    @Transactional
    public WaitlistMovie create(TmdbMovie movie, long requestedBy) {
        if (existsByTmdbId(movie.tmdbId)) {
            return getByTmdbId(movie.tmdbId);
        } else {
            var waitlistMessage = new MessageBuilder()
                    .setEmbed(messageFormatter.waitlistNotification(movie))
                    .send(discordApi.getTextChannelById(movieWaitlistChannel).orElseThrow())
                    .exceptionally(ExceptionLogger.get()).join();

            WaitlistMovie entity = new WaitlistMovie();
            entity.tmdbId = movie.tmdbId;
            entity.imdbId = movie.getImdbId();
            entity.title = movie.title;
            entity.year = movie.getYear();
            entity.requestedBy = requestedBy;
            entity.messageId = waitlistMessage.getId();

            entity.persist();
            return entity;
        }
    }

    @Transactional
    public void delete(int id) {
        WaitlistMovie entity = WaitlistMovie.findById(id);
        entity.delete();
    }

    @Transactional
    public void deleteByTmdbId(long tmdbId) {
        WaitlistMovie entity = WaitlistMovie.find("tmdbId", tmdbId).firstResult();
        entity.delete();
    }

    @Transactional
    public void deleteByImdbId(String imdbId) {
        WaitlistMovie entity = WaitlistMovie.find("imdbId", imdbId).firstResult();
        entity.delete();
    }

    @Transactional
    public void deleteByMessageId(long messageId) {
        WaitlistMovie entity = WaitlistMovie.find("messageId", messageId).firstResult();

        discordApi.getTextChannelById(movieWaitlistChannel).ifPresent(textChannel ->
                textChannel.getMessageById(entity.messageId).join().delete());

        entity.delete();
    }
}
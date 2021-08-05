package net.celestialdata.plexbot.db.daos;

import net.celestialdata.plexbot.clients.services.TmdbService;
import net.celestialdata.plexbot.db.entities.Movie;
import net.celestialdata.plexbot.db.entities.UpgradableMovie;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.awt.*;
import java.util.List;

@SuppressWarnings({"unused"})
@ApplicationScoped
public class UpgradableMovieDao {

    @ConfigProperty(name = "ChannelSettings.upgradeApprovalChannel")
    String upgradeApprovalChannel;

    @Inject
    DiscordApi discordApi;

    @Inject
    @RestClient
    TmdbService tmdbService;

    @Transactional
    public List<UpgradableMovie> listALl() {
        return UpgradableMovie.listAll();
    }

    @Transactional
    public UpgradableMovie get(int id) {
        return UpgradableMovie.findById(id);
    }

    @Transactional
    public UpgradableMovie getByMessageId(long messageId) {
        return UpgradableMovie.find("messageId", messageId).firstResult();
    }

    @Transactional
    public UpgradableMovie getByMovie(Movie movie) {
        return UpgradableMovie.find("movie", movie).firstResult();
    }

    @Transactional
    public boolean exists(int id) {
        return UpgradableMovie.count("id", id) == 1;
    }

    @Transactional
    public boolean existsByMovie(Movie movie) {
        return UpgradableMovie.count("movie", movie) == 1;
    }

    @Transactional
    public UpgradableMovie create(Movie upgradeMovie, int newResolution) {
        if (existsByMovie(upgradeMovie)) {
            return getByMovie(upgradeMovie);
        } else {
            UpgradableMovie entity = new UpgradableMovie();
            var movieData = tmdbService.getMovie(upgradeMovie.tmdbId);
            var upgradeMessage = new MessageBuilder()
                    .setEmbed(new EmbedBuilder()
                            .setTitle("Approval Required")
                            .setDescription("The bot has located an improved quality of the following movie and needs your approval " +
                                    "in order to download and upgrade the movie.")
                            .addInlineField("Title:", "```" + movieData.title + "```")
                            .addInlineField("Release Date:", "```" + movieData.releaseDate + "```")
                            .addInlineField("TMDB ID:", "```" + movieData.tmdbId + "```")
                            .addField("Overview:", "```" + movieData.getOverview() + "```")
                            .addInlineField("Old Resolution:", "```" + upgradeMovie.resolution + "```")
                            .addInlineField("New Resolution:", "```" + newResolution + "```")
                            .setImage(movieData.getPoster())
                            .setColor(Color.GREEN))
                    .addComponents(ActionRow.of(
                            Button.success("approve-upgrade", "Upgrade"),
                            Button.danger("ignore-upgrade", "Ignore")
                    ))
                    .send(discordApi.getTextChannelById(upgradeApprovalChannel).orElseThrow())
                    .exceptionally(ExceptionLogger.get()).join();

            entity.movie = upgradeMovie;
            entity.newResolution = newResolution;
            entity.messageId = upgradeMessage.getId();
            entity.persist();

            return entity;
        }
    }

    @Transactional
    public void delete(int id) {
        UpgradableMovie entity = UpgradableMovie.findById(id);

        discordApi.getTextChannelById(upgradeApprovalChannel).ifPresent(textChannel ->
                textChannel.getMessageById(entity.messageId).join().delete());

        entity.delete();
    }
}
package net.celestialdata.plexbot.discord.commands;

import io.quarkus.arc.log.LoggerName;
import net.celestialdata.plexbot.discord.commandhandler.api.Command;
import net.celestialdata.plexbot.entities.*;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.awt.*;
import java.io.File;

@SuppressWarnings("unused")
@ApplicationScoped
public class StatsCommand implements Command<Message> {

    @LoggerName("net.celestialdata.plexbot.discord.commands.StatsCommand")
    Logger logger;

    @ConfigProperty(name = "FolderSettings.movieFolder")
    String movieFolder;

    @ConfigProperty(name = "FolderSettings.tvFolder")
    String tvFolder;

    @Inject
    EntityUtilities entityUtilities;

    @Override
    public void handleFailure(Throwable error) {
        logger.error(error);
    }

    @Override
    public void execute(Message incomingMessage, String prefix, String usedAlias, String parameterString) {
        var replyMessage = incomingMessage.reply(new EmbedBuilder()
                .setTitle("Media Stats")
                .setDescription("You requested information about the media stored on the server. The bot is currently " +
                        "processing your request. This may take several minutes to complete.")
                .setColor(Color.BLUE)
        ).join();


        // Fetch lists of all the media on the server
        var movies = entityUtilities.getAllMovies();
        var movieSubtitles = entityUtilities.getAllMovieSubtitles();
        var episodes = entityUtilities.getAllEpisodes();
        var episodeSubtitles = entityUtilities.getAllEpisodeSubtitles();
        var shows = entityUtilities.getAllShows();
        var seasons = entityUtilities.getAllSeasons();

        // Create counters for some of the stats
        long totalDuration = 0;

        // Calculate the duration of all movies in minutes
        for (Movie movie : movies) {
            totalDuration = totalDuration + movie.duration;
        }

        // Calculate the duration of all episodes in minutes
        for (Episode episode : episodes) {
            totalDuration = totalDuration + episode.duration;
        }

        // Get the total filesize of the movies and tv directory
        var movieSize = FileUtils.sizeOfDirectory(new File(movieFolder));
        var tvSize = FileUtils.sizeOfDirectory(new File(tvFolder));

        // Calculate the total filesize of everything
        long totalSize = movieSize + tvSize;

        // Calculate the number of years
        var years = totalDuration / 525600;

        // Calculate the number of months
        totalDuration = totalDuration - (years * 525600);
        var months = totalDuration / 43800;

        // Calculate the number of days
        totalDuration = totalDuration - (months * 43800);
        var days = totalDuration / 1440;

        // Calculate the number of hours
        totalDuration = totalDuration - (days * 1440);
        var hours = totalDuration / 60;

        // Calculate the number of minutes
        totalDuration = totalDuration - (hours * 60);
        var minutes = totalDuration;

        // Send the complete stats message
        replyMessage.edit(new EmbedBuilder()
                .setTitle("Media Stats")
                .setDescription("You requested information about the media stored on the server. Below " +
                        "you will find several different statistics about all the media.")
                .addInlineField("Movies:", "```" + movies.size() + "```")
                .addInlineField("Shows:", "```" + shows.size() + "```")
                .addInlineField("Seasons:", "```" + seasons.size() + "```")
                .addInlineField("Episodes:", "```" + episodes.size() + "```")
                .addInlineField("Subtitles:", "```" + (movieSubtitles.size() + episodeSubtitles.size()) + "```")
                .addInlineField("File Size:", "```" + FileUtils.byteCountToDisplaySize(totalSize) + "```")
                .addField("Total Playback Duration:", "```" +
                        years + " years, " + months + " months, " + days + " days, " + hours + " hours, and " + minutes + " minutes```")
                .setColor(Color.GREEN)
        );
    }
}
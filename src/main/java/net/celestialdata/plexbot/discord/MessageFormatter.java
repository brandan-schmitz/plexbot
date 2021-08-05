package net.celestialdata.plexbot.discord;

import net.celestialdata.plexbot.clients.models.tmdb.TmdbMovie;
import net.celestialdata.plexbot.clients.models.tvdb.objects.TvdbEpisode;
import net.celestialdata.plexbot.clients.models.tvdb.objects.TvdbSeries;
import net.celestialdata.plexbot.dataobjects.BotEmojis;
import net.celestialdata.plexbot.enumerators.MovieDownloadSteps;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import javax.enterprise.context.ApplicationScoped;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@ApplicationScoped
public class MessageFormatter {
    private String escapeString(String string) {
        int firstCBIndex = string.indexOf("```");
        if (firstCBIndex != string.lastIndexOf("```")) {
            while (firstCBIndex != -1) {
                string = string.substring(0, firstCBIndex) + string.substring(firstCBIndex + 3);
                firstCBIndex = string.indexOf("```");
            }
        }
        return string.replaceAll("[*|_>~]", "\\\\$0");
    }

    public EmbedBuilder errorMessage(String errorMessage) {
        return new EmbedBuilder()
                .addField("An error has occurred while processing your command:", "```" + errorMessage + "```")
                .setColor(Color.RED);
    }

    public EmbedBuilder errorMessage(String errorMessage, String errorCode) {
        return new EmbedBuilder()
                .addField("An error has occurred while processing your command:", "```" + errorMessage + "```")
                .setFooter("Error code:  " + errorCode)
                .setColor(Color.RED);
    }

    public EmbedBuilder warningMessage(String warningMessage) {
        return new EmbedBuilder()
                .addField("An warning has occurred:", "```" + warningMessage + "```")
                .setColor(Color.YELLOW);
    }

    public EmbedBuilder warningMessage(String warningMessage, String warningCode) {
        return new EmbedBuilder()
                .addField("An warning has occurred:", "```" + warningMessage + "```")
                .setFooter("Warning code: " + escapeString(warningCode))
                .setColor(Color.YELLOW);
    }

    private EmbedBuilder baseDownloadProgressMessage(TmdbMovie movie) {
        return new EmbedBuilder()
                .setTitle("Download Status")
                .setDescription("The movie **" + escapeString(movie.title) + "** is being added:")
                .setFooter("Progress updated: " +
                        escapeString(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(ZonedDateTime.now()) + " CST"))
                .setColor(Color.BLUE);
    }

    private String downloadProgressStringBuilder(MovieDownloadSteps currentStep, double percentage) {
        StringBuilder stringBuilder = new StringBuilder();
        DecimalFormat decimalFormatter = new DecimalFormat("#0.00");

        if (currentStep == MovieDownloadSteps.SELECT_MOVIE) {
            stringBuilder.append(BotEmojis.TODO_STEP).append("  **User selects movie**\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  Locate movie file\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  Mask download file\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  Download movie\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  Import movie");
        } else if (currentStep == MovieDownloadSteps.LOCATE_MOVIE) {
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  User selects movie\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  **Locate movie file**\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  Mask download file\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  Download movie\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  Import movie");
        } else if (currentStep == MovieDownloadSteps.MASK_DOWNLOAD_INIT) {
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  User selects movie\n");
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  Locate movie file\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  **Mask download file**\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  Download movie\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  Import movie");
        } else if (currentStep == MovieDownloadSteps.MASK_DOWNLOAD_DOWNLOADING) {
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  User selects movie\n");
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  Locate movie file\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  **Mask download file:** ")
                    .append((int) percentage).append("%\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  Download movie\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  Import movie");
        } else if (currentStep == MovieDownloadSteps.MASK_DOWNLOAD_PROCESSING) {
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  User selects movie\n");
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  Locate movie file\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  **Mask download file:** Processing...\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  Download movie\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  Import movie");
        } else if (currentStep == MovieDownloadSteps.DOWNLOAD_MOVIE) {
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  User selects movie\n");
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  Locate movie file\n");
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  Mask download file\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  **Download movie:** ")
                    .append(decimalFormatter.format(percentage)).append("%\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  Import movie");
        } else if (currentStep == MovieDownloadSteps.IMPORT_MOVIE) {
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  User selects movie\n");
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  Locate movie file\n");
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  Mask download file\n");
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  Download movie\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  **Import movie**");
        }

        return stringBuilder.toString();
    }

    public EmbedBuilder downloadProgressMessage(TmdbMovie movie, MovieDownloadSteps currentStep) {
        return baseDownloadProgressMessage(movie).addField("Progress:", downloadProgressStringBuilder(currentStep, 0.00));
    }

    public EmbedBuilder downloadProgressMessage(TmdbMovie movie, MovieDownloadSteps currentStep, double percentage) {
        return baseDownloadProgressMessage(movie).addField("Progress:", downloadProgressStringBuilder(currentStep, percentage));
    }

    public EmbedBuilder downloadFinishedMessage(TmdbMovie movie) {
        return new EmbedBuilder()
                .setTitle("Movie Added")
                .addField("Title:", "```" + movie.title + "```")
                .addInlineField("Release Date:", "```" + movie.releaseDate + "```")
                .addInlineField("TMDB ID:", "```" + movie.tmdbId + "```")
                .addInlineField("IMDB ID:", "```" + movie.getImdbId() + "```")
                .addField("Overview:", "```" + movie.getOverview() + "```")
                .setImage(movie.getPoster())
                .setFooter("Added on: " +
                        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(ZonedDateTime.now()) + " CST")
                .setColor(Color.GREEN);
    }

    public EmbedBuilder newMovieNotification(TmdbMovie movie) {
        return new EmbedBuilder()
                .setTitle("Movie Added")
                .addField("Title:", "```" + movie.title + "```")
                .addInlineField("Release Date:", "```" + movie.releaseDate + "```")
                .addInlineField("TMDB ID:", "```" + movie.tmdbId + "```")
                .addInlineField("IMDB ID:", "```" + movie.getImdbId() + "```")
                .addField("Overview:", "```" + movie.getOverview() + "```")
                .setImage(movie.getPoster())
                .setColor(Color.GREEN);
    }

    public EmbedBuilder newEpisodeNotification(TvdbEpisode episode, TvdbSeries show, String episodeOverview) {
        // Create the base embedded message
        var embed = new EmbedBuilder()
                .setTitle("Episode Added")
                .addField("Show Name:", "```" + show.name + "```")
                .addField("Episode Name:", "```" + episode.name + "```")
                .addInlineField("Season #:", "```" + episode.seasonNumber + "```")
                .addInlineField("Episode #:", "```" + episode.number + "```")
                .addInlineField("TVDB ID:", "```" + episode.id + "```")
                .setImage(episode.getImage())
                .setColor(Color.GREEN);

        // Add an episode overview if one is provided
        if (!StringUtils.isBlank(episodeOverview)) {
            embed.addField("Episode Overview:", "```" + episodeOverview + "```");
        }

        // Return the embed message
        return embed;
    }

    public EmbedBuilder newMovieUserNotification(TmdbMovie movie) {
        return new EmbedBuilder()
                .setTitle("Movie Added")
                .setDescription("You requested the following movie be added to Celestial Movies Plex Server. This message " +
                        "is to notify you that the movie is now available on Plex.")
                .addField("Title:", "```" + movie.title + "```")
                .addInlineField("Release Date:", "```" + movie.releaseDate + "```")
                .addInlineField("TMDB ID:", "```" + movie.tmdbId + "```")
                .addInlineField("IMDB ID:", "```" + movie.getImdbId() + "```")
                .addField("Overview:", "```" + movie.getOverview() + "```")
                .setImage(movie.getPoster())
                .setColor(Color.GREEN);
    }

    public EmbedBuilder waitlistNotification(TmdbMovie movie) {
        return new EmbedBuilder()
                .setTitle("Movie Requested")
                .addField("Title:", "```" + movie.title + "```")
                .addInlineField("Release Date:", "```" + movie.releaseDate + "```")
                .addInlineField("TMDB ID:", "```" + movie.tmdbId + "```")
                .addInlineField("IMDB ID:", "```" + movie.getImdbId() + "```")
                .addField("Overview:", "```" + movie.getOverview() + "```")
                .setImage(movie.getPoster())
                .setFooter("Last Checked: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                        .format(ZonedDateTime.now()) + " CST")
                .setColor(Color.BLUE);
    }

    public EmbedBuilder importProgressMessage(String progressMessage) {
        return new EmbedBuilder()
                .setTitle("Import Processor")
                .setDescription("You have requested the bot import media contained within the import folder. Please stand-by while " +
                        "this action is performed as it may take a while.")
                .addField("Progress:", "```" + progressMessage + "```")
                .setFooter("Updated: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                        .format(ZonedDateTime.now()) + " CST")
                .setColor(Color.BLUE);
    }
}
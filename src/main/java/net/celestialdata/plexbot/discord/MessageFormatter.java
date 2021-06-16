package net.celestialdata.plexbot.discord;

import net.celestialdata.plexbot.clients.models.omdb.OmdbResult;
import net.celestialdata.plexbot.utilities.BotEmojis;
import net.celestialdata.plexbot.utilities.MovieDownloadSteps;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import javax.enterprise.context.ApplicationScoped;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@ApplicationScoped
public class MessageFormatter {
    public EmbedBuilder formatErrorMessage(String errorMessage) {
        return new EmbedBuilder()
                .addField("An error has occurred while processing your command:", "```" + errorMessage + "```")
                .setColor(Color.RED);
    }

    public EmbedBuilder formatErrorMessage(String errorMessage, String errorCode) {
        return new EmbedBuilder()
                .addField("An error has occurred while processing your command:", "```" + errorMessage + "```")
                .setFooter("Error code:  " + errorCode)
                .setColor(Color.RED);
    }

    public EmbedBuilder formatWarningMessage(String warningMessage) {
        return new EmbedBuilder()
                .addField("An warning has occurred:", "```" + warningMessage + "```")
                .setColor(Color.YELLOW);
    }

    private EmbedBuilder baseDownloadProgressMessage(OmdbResult movie) {
        return new EmbedBuilder()
                .setTitle("Download Status")
                .setDescription("The movie **" + movie.title + "** is being added:")
                .setFooter("Progress updated: " +
                        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(ZonedDateTime.now()) + " CST")
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
        } else if (currentStep == MovieDownloadSteps.FINISHED) {
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  User selects movie\n");
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  Locate movie file\n");
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  Mask download file\n");
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  Download movie\n");
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  Import movie");
        }

        return stringBuilder.toString();
    }

    public EmbedBuilder formatDownloadProgressMessage(OmdbResult movie, MovieDownloadSteps currentStep) {
        return baseDownloadProgressMessage(movie).addField("Progress:", downloadProgressStringBuilder(currentStep, 0.00));
    }

    public EmbedBuilder formatDownloadProgressMessage(OmdbResult movie, MovieDownloadSteps currentStep, double percentage) {
        return baseDownloadProgressMessage(movie).addField("Progress:", downloadProgressStringBuilder(currentStep, percentage));
    }

    public EmbedBuilder formatDownloadFinishedMessage(OmdbResult movie) {
        return new EmbedBuilder()
                .setTitle("Download Status")
                .setDescription("The movie **" + movie.title + "** has been added:")
                .addField("Progress:", downloadProgressStringBuilder(MovieDownloadSteps.FINISHED, 0))
                .addField(movie.title,
                        "**Year:** " + movie.year + "\n" +
                                "**Director(s):** " + movie.director + "\n" +
                                "**Plot:** " + movie.plot)
                .setImage(movie.poster)
                .setFooter("Added on: " +
                        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(ZonedDateTime.now()) + " CST")
                .setColor(Color.GREEN);
    }

    public EmbedBuilder formatNewMovieNotification(OmdbResult movie) {
        return new EmbedBuilder()
                .setTitle(movie.title)
                .setDescription("**Year:** " + movie.year + "\n" +
                        "**Director(s):** " + movie.director + "\n" +
                        "**Plot:** " + movie.plot)
                .setImage(movie.poster)
                .setColor(Color.GREEN);
    }

    public EmbedBuilder formatMovieAddedDirectMessage(OmdbResult movie) {
        return new EmbedBuilder()
                .setTitle("Movie Added")
                .setDescription("You requested the following movie be added to Celestial Movies Plex Server. This message is to notify you that the movie is now available on Plex.\n\n" +
                        "**Title:** " + movie.title + "\n" +
                        "**Year:** " + movie.year + "\n" +
                        "**Director(s):** " + movie.director + "\n" +
                        "**Plot:** " + movie.plot)
                .setImage(movie.poster)
                .setColor(Color.GREEN)
                .setFooter("This message was sent by the Plexbot and no reply will be received to messages sent here.");
    }
}
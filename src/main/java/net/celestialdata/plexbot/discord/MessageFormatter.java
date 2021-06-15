package net.celestialdata.plexbot.discord;

import net.celestialdata.plexbot.clients.models.omdb.OmdbResult;
import net.celestialdata.plexbot.utilities.BotEmojis;
import net.celestialdata.plexbot.utilities.MovieDownloadSteps;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import javax.enterprise.context.ApplicationScoped;
import java.awt.*;
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
                .setDescription("The movie **" + movie.title + "** is being added,")
                .setFooter("Progress updated: " +
                        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(ZonedDateTime.now()) + " CST")
                .setColor(Color.BLUE);
    }

    private String downloadProgressStringBuilder(MovieDownloadSteps currentStep, double percentage) {
        StringBuilder stringBuilder = new StringBuilder();

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
        } else if (currentStep == MovieDownloadSteps.MASK_DOWNLOAD) {
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  User selects movie\n");
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  Locate movie file\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  **Mask download file:** ").append(percentage).append("%\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  Download movie\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  Import movie");
        } else if (currentStep == MovieDownloadSteps.DOWNLOAD_MOVIE) {
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  User selects movie\n");
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  Locate movie file\n");
            stringBuilder.append(BotEmojis.FINISHED_STEP).append("  Mask download file\n");
            stringBuilder.append(BotEmojis.TODO_STEP).append("  **Download movie:** ").append(percentage).append("%\n");
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
}
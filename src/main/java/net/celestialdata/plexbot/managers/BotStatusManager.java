package net.celestialdata.plexbot.managers;

import net.celestialdata.plexbot.BotWorkPool;
import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.configuration.BotConfig;
import net.celestialdata.plexbot.utils.BotColors;
import net.celestialdata.plexbot.utils.StringBuilderPlus;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;

final public class BotStatusManager implements Runnable {
    private static volatile BotStatusManager single_instance = null;
    final Object lock = new Object();
    private final Message sentMessage;
    private final ArrayList<String> currentProcesses;
    private String resolutionManagerStatus;
    private String waitlistManagerStatus;
    private String importManagerStatus;
    private Color statusColor = BotColors.SUCCESS;

    private BotStatusManager() {
        resolutionManagerStatus = "Idle";
        waitlistManagerStatus = "Idle";
        importManagerStatus = "Idle";
        currentProcesses = new ArrayList<>();

        Main.getBotApi().getTextChannelById(BotConfig.getInstance().botStatusChannelId()).ifPresent(channel ->
                channel.getMessages(100).thenAccept(messages -> messages.deleteAll().join()));

        sentMessage = new MessageBuilder()
                .setEmbed(buildMessage())
                .send(Main.getBotApi().getTextChannelById(BotConfig.getInstance().botStatusChannelId()).orElseThrow())
                .join();

        BotWorkPool.getInstance().executor.submit(this);
    }

    public static BotStatusManager getInstance() {
        if (single_instance == null)
            single_instance = new BotStatusManager();

        return single_instance;
    }

    @Override
    public void run() {
        try {
            synchronized (lock) {
                //noinspection InfiniteLoopStatement
                while (true) {
                    sentMessage.edit(buildMessage());
                    lock.wait(3000);
                }
            }
        } catch (InterruptedException e) {
            new MessageBuilder()
                    .append("An error has occurred while performing the following task:", MessageDecoration.BOLD)
                    .appendCode("", "Bot Status Manager")
                    .appendCode("java", ExceptionUtils.getMessage(e))
                    .appendCode("java", ExceptionUtils.getStackTrace(e))
                    .send(Main.getBotApi().getUserById(BotConfig.getInstance().adminUserId()).join());
        }
    }

    private EmbedBuilder buildMessage() {
        return new EmbedBuilder()
                .setTitle("Bot Status")
                .setColor(statusColor)
                .setDescription("Below you can find the current status of the bot including all running processes. " +
                        "Only the tasks listed in the *Running Tasks* section are being actively executed. " +
                        "Items in the queued section will process once there is room.\n\n" +
                        "**Waitlist Manager:**\n```" + waitlistManagerStatus + "```\n" +
                        "**Resolution Manager:**\n```" + resolutionManagerStatus + "```\n" +
                        "**Import Manager:**\n```" + importManagerStatus + "```\n" +
                        "**Running Tasks:**\n```" + buildProcessList() + "```")
                .setFooter("Plexbot v" + Main.getVersion() + " - " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .format(ZonedDateTime.now()) + " CST");
    }

    private String buildProcessList() {
        StringBuilderPlus stringBuilderPlus = new StringBuilderPlus();
        statusColor = BotColors.SUCCESS;
        int processNum = 1;

        if (currentProcesses.isEmpty()) {
            return "Idle";
        } else if (currentProcesses.size() <= BotConfig.getInstance().concurrentTasks()) {
            for (String process : currentProcesses) {
                stringBuilderPlus.appendLine(processNum + ") " + process);
                processNum++;
            }
        } else {
            statusColor = BotColors.WARNING;

            for (String process : currentProcesses) {
                if (processNum == (BotConfig.getInstance().concurrentTasks() + 1)) {
                    stringBuilderPlus.appendLine("```");
                    stringBuilderPlus.appendLine("**Queued Tasks:**");
                    stringBuilderPlus.appendLine("```");
                }

                if (processNum >= (BotConfig.getInstance().concurrentTasks() + 1)) {
                    stringBuilderPlus.appendLine(processNum - BotConfig.getInstance().concurrentTasks() + ") " + process);
                } else {
                    stringBuilderPlus.appendLine(processNum + ") " + process);
                }
                processNum++;
            }
        }

        return stringBuilderPlus.toString();
    }

    public void setResolutionManagerStatus(int currentNumber, int totalMovies) {
        this.resolutionManagerStatus = "Checking " + currentNumber + " of " + totalMovies;
    }

    public void clearResolutionManagerStatus() {
        this.resolutionManagerStatus = "Idle";
    }

    public void setWaitlistManagerStatus(int currentNumber, int totalMovies) {
        this.waitlistManagerStatus = "Checking movie " + currentNumber + " of " + totalMovies;
    }

    public void clearWaitlistManagerStatus() {
        this.waitlistManagerStatus = "Idle";
    }

    public void setImportManagerStatus(int currentNumber, int total) {
        this.importManagerStatus = "Importing file " + currentNumber + " of " + total;
    }

    public void setImportManagerStatus(String status) {
        this.importManagerStatus = status;
    }

    public void clearImportManagerStatus() {
        this.importManagerStatus = "Idle";
    }

    public void addProcess(String processName) {
        currentProcesses.add(processName);
    }

    public void removeProcess(String processName) {
        currentProcesses.removeIf(process -> process.contentEquals(processName));
    }

    public boolean containsProcess(String processName) {
        return currentProcesses.contains(processName);
    }
}
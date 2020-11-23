package net.celestialdata.plexbot.managers;

import net.celestialdata.plexbot.BotWorkPool;
import net.celestialdata.plexbot.Main;
import net.celestialdata.plexbot.config.ConfigProvider;
import net.celestialdata.plexbot.utils.BotColors;
import net.celestialdata.plexbot.utils.StringBuilderPlus;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
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
    private Color statusColor = BotColors.SUCCESS;

    private BotStatusManager() {
        resolutionManagerStatus = "Idle";
        waitlistManagerStatus = "Idle";
        currentProcesses = new ArrayList<>();

        Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.botStatusChannelId()).ifPresent(channel ->
                channel.getMessages(100).thenAccept(messages -> messages.deleteAll().join()));

        sentMessage = new MessageBuilder()
                .setEmbed(buildMessage())
                .send(Main.getBotApi().getTextChannelById(ConfigProvider.BOT_SETTINGS.botStatusChannelId()).orElseThrow())
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
            e.printStackTrace();
        }
    }

    private EmbedBuilder buildMessage() {
        return new EmbedBuilder()
                .setTitle("Bot Status")
                .setColor(statusColor)
                .setDescription("Below you can find the current status of the bot including all running processes. " +
                        "Please note that only the first four tasks in the queue are actively being processed, anything " +
                        "after that will be processed once a previous task is completed.\n\n" +
                        "**Waitlist Manager:**\n```" + waitlistManagerStatus + "```\n" +
                        "**Resolution Manager:**\n```" + resolutionManagerStatus + "```\n" +
                        "**Task Queue:**\n```" + buildProcessList() + "```")
                .setFooter("Plexbot v" + getClass().getPackage().getImplementationVersion() + " - " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .format(ZonedDateTime.now()) + " CST");
    }

    private String buildProcessList() {
        StringBuilderPlus stringBuilderPlus = new StringBuilderPlus();

        if (currentProcesses.isEmpty()) {
            statusColor = BotColors.SUCCESS;
            return "Idle";
        } else {
            int orderNum = 1;
            for (String process : currentProcesses) {
                stringBuilderPlus.appendLine(orderNum + ") " + process);
                orderNum++;
            }

            if (orderNum > 4) {
                statusColor = BotColors.WARNING;
            } else statusColor = BotColors.SUCCESS;
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

    public void addProcess(String processName) {
        currentProcesses.add(processName);
    }

    public void removeProcess(String processName) {
        currentProcesses.removeIf(process -> process.contentEquals(processName));
    }
}
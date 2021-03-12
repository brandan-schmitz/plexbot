package net.celestialdata.plexbot;

import net.celestialdata.plexbot.managers.BotStatusManager;
import net.celestialdata.plexbot.utils.CustomRunnable;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BotWorkPool {
    private static BotWorkPool single_instance = null;
    public final ThreadPoolExecutor executor;

    private BotWorkPool() {
        // Configure the executor
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

        // If the queue is full, wait 1 second and try again
        executor.setRejectedExecutionHandler((r, executor) -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                new MessageBuilder()
                        .append("An error has occurred while performing the following task:", MessageDecoration.BOLD)
                        .appendCode("", "Bot Work Pool")
                        .appendCode("java", ExceptionUtils.getMessage(e))
                        .appendCode("java", ExceptionUtils.getStackTrace(e))
                        .send(Main.getBotApi().getUserById(BotConfig.getInstance().adminUserId()).join());
            }
            executor.execute(r);
        });

        executor.setKeepAliveTime(5, TimeUnit.SECONDS);

        // Start all the threads so they are ready to accept work right away
        executor.prestartAllCoreThreads();
    }

    public static BotWorkPool getInstance() {
        if (single_instance == null)
            single_instance = new BotWorkPool();

        return single_instance;
    }

    public boolean isPoolFull() {
        return executor.getActiveCount() == executor.getPoolSize();
    }

    public int getNumTasksInQueue() {
        return executor.getQueue().size() + (executor.getActiveCount() - 1);
    }

    public void submitProcess(CustomRunnable task) {
        if (isPoolFull() && task.cancelOnFull()) {
            return;
        }

        if (BotStatusManager.getInstance().containsProcess(task.taskName()) && task.cancelOnDuplicate()) {
            return;
        }

        executor.execute(task);
        BotStatusManager.getInstance().addProcess(task.taskName());
    }
}
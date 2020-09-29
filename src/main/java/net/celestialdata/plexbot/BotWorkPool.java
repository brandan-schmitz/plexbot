package net.celestialdata.plexbot;

import net.celestialdata.plexbot.utils.BotStatusManager;

import java.util.concurrent.*;

public class BotWorkPool {
    private static BotWorkPool single_instance = null;
    public ThreadPoolExecutor executor;

    private BotWorkPool() {
        // Configure the executor
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

        // If the queue is full, wait 1 second and try again
        executor.setRejectedExecutionHandler((r, executor) -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
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

    public void submitProcess(String processName, Runnable task) {
        executor.submit(task);
        BotStatusManager.getInstance().addProcess(processName);
    }
}
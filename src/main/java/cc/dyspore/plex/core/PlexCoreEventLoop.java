package cc.dyspore.plex.core;

import net.minecraft.client.Minecraft;
import cc.dyspore.plex.Plex;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class PlexCoreEventLoop {
    public static Map<String, PlexCoreEventLoop> instances = new HashMap<>();

    public String name;
    private final List<Runnable> loopTasks = Collections.synchronizedList(new ArrayList<>());
    private final Map<Runnable, Long> tasksClock = new ConcurrentHashMap<>();
    private Thread taskThread;

    private AtomicBoolean runTasks = new AtomicBoolean();
    private AtomicBoolean runTasksCallback = new AtomicBoolean();
    private AtomicBoolean shutdownTrigger = new AtomicBoolean();
    private AtomicLong clockMs = new AtomicLong(20L);

    private List<Runnable> threadGlobalTasksList;
    private Map<Runnable, Long> threadClockedTasksList;
    private long threadNextGlobalRun;
    private Map<Runnable, Long> threadClockedTasksClock;

    private List<Runnable> threadPreviousErrored;

    public static PlexCoreEventLoop create(String name) {
        if (instances.containsKey(name)) {
            throw new IllegalArgumentException("Event loop with \"" + name + "\" already exists");
        }
        PlexCoreEventLoop loop = new PlexCoreEventLoop(name);
        instances.put(name, loop);
        return loop;
    }

    public static PlexCoreEventLoop get(String name) {
        return instances.get(name);
    }

    private PlexCoreEventLoop(String name) {
        this.name = name;
        this.runTasks.set(false);
        this.runTasksCallback.set(false);
        this.shutdownTrigger.set(false);
        this.taskThread = new Thread(this::threadTask);
    }

    private void threadTask() {
        this.threadGlobalTasksList = new ArrayList<>();
        this.threadClockedTasksList = new HashMap<>();
        this.threadNextGlobalRun = 0;
        this.threadClockedTasksClock = new HashMap<>();
        this.threadPreviousErrored = new ArrayList<>();

        long sleep = 0L;
        while (!this.shutdownTrigger.get()) {
            try {
                Thread.sleep(sleep);
            }
            catch (InterruptedException e) {
                break;
            }
            synchronized (this.loopTasks) {
                this.updateTaskLists();
            }
            sleep = this.threadDoLoop();
        }
    }

    private void updateTaskLists() {
        this.threadClockedTasksClock.clear();
        this.threadClockedTasksClock.putAll(this.tasksClock);
        this.threadGlobalTasksList.clear();
        for (Runnable task1 : this.threadClockedTasksList.keySet()) {
            if (!this.loopTasks.contains(task1)) {
                this.threadClockedTasksList.remove(task1);
            }
        }

        for (Runnable task : this.loopTasks) {
            if (this.tasksClock.containsKey(task)) {
                if (!this.threadClockedTasksList.containsKey(task)) {
                    this.threadClockedTasksList.put(task, 0L);
                }
                continue;
            }
            this.threadGlobalTasksList.add(task);
        }
    }

    private void executeTask(Runnable runnable) {
        try {
            runnable.run();
            while (this.threadPreviousErrored.contains(runnable)) {
                this.threadPreviousErrored.remove(runnable);
            }
        }
        catch (Throwable throwable) {
            if (!this.threadPreviousErrored.contains(runnable)) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                Plex.logger.error("[PEL_" + this.name + "] Absorbing exception on mod loop for " + runnable + "\n" + sw.toString());
            }
            this.threadPreviousErrored.add(runnable);
        }
    }

    private long threadDoLoop() {
        boolean doTasks = this.runTasks.get();
        long clock = this.clockMs.get();
        this.runTasksCallback.set(doTasks);

        if (!doTasks) {
            return clock;
        }

        if (Minecraft.getSystemTime() >= this.threadNextGlobalRun) {
            for (Runnable runnable : this.threadGlobalTasksList) {
                this.executeTask(runnable);
            }
            this.threadNextGlobalRun = Minecraft.getSystemTime() + clock;
        }

        long next = this.threadNextGlobalRun - Minecraft.getSystemTime();

        for (Map.Entry<Runnable, Long> task : this.threadClockedTasksList.entrySet()) {
            if (Minecraft.getSystemTime() >= task.getValue()) {
                this.executeTask(task.getKey());
                task.setValue(Minecraft.getSystemTime() + this.threadClockedTasksClock.getOrDefault(task.getKey(), clock));
            }
            next = Math.min(next, task.getValue() - Minecraft.getSystemTime());
        }
        return Math.max(next, 0);
    }

    public void addTask(Runnable task) {
        if (!this.loopTasks.contains(task)) {
            this.loopTasks.add(task);
        }
    }

    public void addTask(Runnable task, long clockInterval) {
        this.addTask(task);
        this.setTaskClock(task, clockInterval);
    }

    public void setTaskClock(Runnable task, long clockInterval) {
        if (clockInterval == -1L) {
            this.tasksClock.remove(task);
            return;
        }
        this.tasksClock.put(task, clockInterval);
    }

    public void clearTaskClock(Runnable task) {
        this.setTaskClock(task, -1L);
    }

    public void removeTask(Runnable task) {
        while (this.loopTasks.contains(task)) {
            this.loopTasks.remove(task);
        }
    }

    public boolean isRunning() {
        return this.taskThread.isAlive() && this.runTasks.get() && this.runTasksCallback.get();
    }

    public boolean isStopped() {
        return !this.runTasks.get() && !this.runTasksCallback.get();
    }

    public boolean threadRunning() {
        return this.taskThread.isAlive();
    }

    public void start() {
        if (this.isRunning()) {
            return;
        }
        this.runTasks.set(true);
        if (!this.taskThread.isAlive()) {
            this.taskThread.start();
        }
    }

    public void stop() {
        this.runTasks.set(false);
    }

    public void shutdown() {
        this.shutdownTrigger.set(true);
    }

    public PlexCoreEventLoop setClock(long ms) {
        this.clockMs.set(ms);
        return this;
    }
}

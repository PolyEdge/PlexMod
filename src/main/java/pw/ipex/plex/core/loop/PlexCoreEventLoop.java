package pw.ipex.plex.core.loop;

import net.minecraft.client.Minecraft;
import pw.ipex.plex.Plex;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class PlexCoreEventLoop {
    private final List<Runnable> loopTasks = Collections.synchronizedList(new ArrayList<>());
    private final Map<Runnable, Long> customTaskClock = new ConcurrentHashMap<>();
    private Thread globalThread;
    public String name;
    private AtomicBoolean runLoop = new AtomicBoolean();
    private AtomicBoolean updatedRunLoop = new AtomicBoolean();
    private AtomicBoolean killThread = new AtomicBoolean();
    private AtomicLong clockMs = new AtomicLong(0L);
    private AtomicLong sleepMs = new AtomicLong(20L);

    public PlexCoreEventLoop(String name) {
        this.name = name;
        this.runLoop.set(false);
        this.updatedRunLoop.set(false);
        this.killThread.set(false);
        this.globalThread = new Thread(this::threadTask);
    }

    private void threadTask() {
        long lastRun = Minecraft.getSystemTime();
        List<Runnable> previousError = new ArrayList<>();
        Map<Runnable, Long> taskClockTimes = new HashMap<>();
        while (!this.killThread.get()) {
            try {
                Thread.sleep(this.sleepMs.get());
            }
            catch (InterruptedException e) {
                break;
            }
            this.updatedRunLoop.set(this.runLoop.get());
            synchronized (this.loopTasks) {
                for (Runnable task : this.loopTasks) {
                    if (!(Minecraft.getSystemTime() >= lastRun + clockMs.get()) || !this.runLoop.get() && !customTaskClock.containsKey(task)) {
                        continue;
                    }
                    if (!customTaskClock.containsKey(task)) {
                        lastRun = Minecraft.getSystemTime();
                    }
                    else if (taskClockTimes.containsKey(task) && !(Minecraft.getSystemTime() > taskClockTimes.get(task) + this.customTaskClock.get(task))) {
                        continue;
                    }
                    taskClockTimes.put(task, Minecraft.getSystemTime());
                    try {
                        task.run();
                        while (previousError.contains(task)) {
                            previousError.remove(task);
                        }
                    }
                    catch (Throwable e) {
                        if (!previousError.contains(task)) {
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            e.printStackTrace(pw);
                            Plex.logger.error("[PEL_" + this.name + "] Absorbing exception on mod loop for " + task + "\n" + sw.toString());
                        }
                        previousError.add(task);
                    }

                }
            }
        }
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
            this.customTaskClock.remove(task);
            return;
        }
        this.customTaskClock.put(task, clockInterval);
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
        return this.globalThread.isAlive() && this.runLoop.get() && this.updatedRunLoop.get();
    }

    public boolean isStopped() {
        return !this.runLoop.get() && !this.updatedRunLoop.get();
    }

    public boolean threadRunning() {
        return this.globalThread.isAlive();
    }

    public void start() {
        if (this.isRunning()) {
            return;
        }
        this.runLoop.set(true);
        if (!this.globalThread.isAlive()) {
            this.globalThread.start();
        }
    }

    public void stop() {
        this.runLoop.set(false);
    }

    public void exitThread() {
        this.killThread.set(true);
    }

    public PlexCoreEventLoop setClock(long ms) {
        this.clockMs.set(ms);
        return this;
    }

    public PlexCoreEventLoop setSleep(long ms) {
        this.sleepMs.set(ms);
        return this;
    }
}

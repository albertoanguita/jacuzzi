package org.aanguita.jacuzzi.concurrency;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * todo check and add stop
 * The periodic task reminder raises one thread for invoking heterogeneous periodic tasks. Clients can register their periodic tasks for being
 * invoked at specific intervals (the intervals can grow with time, or shrink upon request). Each task can be configured to be invoked synchronously
 * or asynchronously. Care must be taken not to choke the periodic task reminder with heavy load tasks that are repeated very frequently.
 *
 * Each registered task is identified by a string provided by the client. Ids must be unique.
 *
 * Created by Alberto on 01/03/2017.
 */
public class PeriodicTaskReminder {

    private static class TaskElement implements Comparable<TaskElement> {

        private final String name;

        private final Runnable task;

        private final boolean inBackground;

        private final long period;

        private long nextRun;

        private TaskElement(String name, Runnable task, boolean inBackground, long period, boolean runNow) {
            this.name = name;
            if (task == null) {
                throw new IllegalArgumentException("Received null task");
            }
            if (period <= 0) {
                throw new IllegalArgumentException("Received 0 or negative period: " + period);
            }
            this.task = task;
            this.inBackground = inBackground;
            this.period = period;
            long now = System.currentTimeMillis();
            if (runNow) {
                nextRun = now;
            } else {
                nextRun = now + period;
            }
        }

        private void calculateNextRun() {
            nextRun += period;
        }

        private void run() {
            if (inBackground) {
//                ManagerGeneral.executorService.submit(new Thread(name) {
//                    @Override
//                    public void run() {
//                        try {
//                            task.run();
//                        } catch (Throwable e) {
//                            if (LOGGER.isWarnEnabled()) {
//                                LOGGER.warn("Periodic task " + name + " failed to execute normally", e);
//                            }
//                        }
//                    }
//                });
                new Thread(name) {
                    @Override
                    public void run() {
                        try {
                            task.run();
                        } catch (Throwable e) {
                            if (LOGGER.isWarnEnabled()) {
                                LOGGER.warn("Periodic task " + name + " failed to execute normally", e);
                            }
                        }
                    }
                }.start();
            } else {
                try {
                    task.run();
                } catch (Throwable e) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("Periodic task " + name + " failed to execute normally", e);
                    }
                }
            }
        }

        @Override
        public int compareTo(TaskElement o) {
            return (int) (nextRun - o.nextRun);
        }
    }

    private static class ReminderThread extends Thread {

        private final PeriodicTaskReminder periodicTaskReminder;

        public ReminderThread(PeriodicTaskReminder periodicTaskReminder, String name) {
            super("PeriodicTaskReminder." + name);
            setDaemon(true);
            this.periodicTaskReminder = periodicTaskReminder;
        }

        @Override
        public void run() {
            while (true) {
                periodicTaskReminder.runFirstTask();
            }
        }
    }

    private static final Log LOGGER = LogFactory.getLog(PeriodicTaskReminder.class);

    private static ConcurrentHashMap<String, PeriodicTaskReminder> instances = new ConcurrentHashMap<>();

    private final String name;

    private final Queue<TaskElement> taskElementQueue;

    private final ReminderThread reminderThread;

    public static PeriodicTaskReminder getInstance(String name) {
        instances.putIfAbsent(name, new PeriodicTaskReminder(name));
        return instances.get(name);
    }

    public PeriodicTaskReminder(String name) {
        this.name = name;
        taskElementQueue = new PriorityQueue<>();
        reminderThread = new ReminderThread(this, name);
    }

    public synchronized void addPeriodicTask(String taskName, Runnable task, boolean inBackground, long period, boolean runNow) {
        LOGGER.info(name + " adding new task: " + taskName);
        taskElementQueue.add(new TaskElement(taskName, task, inBackground, period, runNow));
        if (taskElementQueue.size() == 1) {
            LOGGER.info(name + " starting reminder thread");
            startReminderThread();
        } else {
            reminderThread.interrupt();
        }
    }

    public synchronized void removePeriodicTask(String taskName) {
        Iterator<TaskElement> it = taskElementQueue.iterator();
        while (it.hasNext()) {
            TaskElement taskElement = it.next();
            if (taskElement.name.equals(taskName)) {
                it.remove();
                break;
            }
        }
    }

    private void startReminderThread() {
        reminderThread.start();
    }

    private void runFirstTask() {
        TaskElement taskElement;
        synchronized (this) {
            taskElement = taskElementQueue.peek();
        }
        try {
            long wait = Math.max(taskElement.nextRun - System.currentTimeMillis(), 0L);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(name + " waiting for next task: " + taskElement.name + ". We will wait " + wait + " ms");
            }
            Thread.sleep(wait);
            synchronized (this) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(name + " running task: " + taskElement.name);
                }
                taskElementQueue.remove();
                taskElement.run();
                taskElement.calculateNextRun();
                taskElementQueue.add(taskElement);
            }
        } catch (InterruptedException e) {
            // a new task was inserted. Return immediately and let the reminder thread come here again
        }
    }
}

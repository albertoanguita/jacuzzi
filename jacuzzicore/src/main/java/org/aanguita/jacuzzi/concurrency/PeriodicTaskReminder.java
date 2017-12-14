package org.aanguita.jacuzzi.concurrency;

import org.aanguita.jacuzzi.objects.ObjectMapPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * The periodic task reminder raises one thread for invoking heterogeneous periodic tasks. Clients can register their periodic tasks for being
 * invoked at specific intervals (the intervals can grow with time, or shrink upon request). Each task can be configured to be invoked synchronously
 * or asynchronously. Care must be taken not to choke the periodic task reminder with heavy load tasks that are repeated very frequently.
 *
 * Each registered task is identified by a string provided by the client. Ids must be unique.
 *
 * Created by Alberto on 01/03/2017.
 */
public class PeriodicTaskReminder {

    private static class TaskElement {

        private final String name;

        private final Runnable task;

        private final boolean inBackground;

        private final long period;

        private Integer numberOfRuns;

        private TaskElement(String name, Runnable task, boolean inBackground, long period, Integer numberOfRuns) {
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
            this.numberOfRuns = numberOfRuns;
        }

        private void run() {
            if (inBackground) {
                ThreadExecutor.submitUnregistered(task, name);
            } else {
                try {
                    task.run();
                } catch (Throwable e) {
                    if (LOGGER.isWarnEnabled()) {
                        LOGGER.warn("Periodic task " + name + " failed to execute normally", e);
                    }
                }
            }
            if (numberOfRuns != null) {
                numberOfRuns--;
            }
        }

        private boolean mustRunMore() {
            return numberOfRuns == null || numberOfRuns > 0;
        }
    }

    /**
     * Prefix for the timer alert id
     */
    private static final String TIMED_ALERT_ID = "periodic_task_reminder.";

    private static final Log LOGGER = LogFactory.getLog(PeriodicTaskReminder.class);

    private static ObjectMapPool<String, PeriodicTaskReminder> instances = new ObjectMapPool<>(PeriodicTaskReminder::new);

    private final String name;

    private final Map<String, TaskElement> taskElements;

    public static PeriodicTaskReminder getInstance(String name) {
        return instances.getObject(name);
    }

    private PeriodicTaskReminder(String name) {
        this.name = name;
        taskElements = new HashMap<>();
    }

    // TODO: 21/09/2017 add possibility of setting number of times that the task is executed
    public synchronized void addPeriodicTask(String taskName, Runnable task, boolean inBackground, long period, boolean runNow) {
        LOGGER.info(name + " adding new task: " + taskName);
        taskElements.put(taskName, new TaskElement("PeriodicTaskReminder(" + name + "):" + taskName, task, inBackground, period, null));
        TimeAlert.getInstance(getTimeAlertId()).addAlert(taskName, runNow ? 0 : period, (alertName) -> runTask(taskName));
    }

    private void runTask(String taskName) {
        TaskElement taskElement = null;
        synchronized (this) {
            if (taskElements.containsKey(taskName)) {
                taskElement = taskElements.get(taskName);
                TimeAlert.getInstance(getTimeAlertId()).addAlert(taskName, taskElement.period, (alertName) -> runTask(taskName));
            }
        }
        if (taskElement != null) {
            taskElement.run();
        }
        synchronized (this) {
            if (taskElement != null) {
                TimeAlert.getInstance(getTimeAlertId()).addAlert(taskName, taskElement.period, (alertName) -> runTask(taskName));
            }
        }
    }

    public synchronized void removePeriodicTask(String taskName) {
        taskElements.remove(taskName);
        TimeAlert.getInstance(getTimeAlertId()).removeAlert(taskName);
    }

    private String getTimeAlertId() {
        return TIMED_ALERT_ID + name;
    }

    public synchronized void stop() {
        for (String taskName : taskElements.keySet()) {
            removePeriodicTask(taskName);
        }
    }
}

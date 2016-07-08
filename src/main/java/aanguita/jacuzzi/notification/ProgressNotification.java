package aanguita.jacuzzi.notification;

/**
 * This interface contains methods for submitting progress notifications of a process.
 */
public interface ProgressNotification<E> {

    /**
     * Reports that the task being monitored has began
     */
    void beginTask();

    /**
     * Adds a new progress notification
     *
     * @param message content of the notification
     */
    void addNotification(E message);

    /**
     * This method can be invoked to indicate that the task has been finalized
     */
    void completeTask();
}

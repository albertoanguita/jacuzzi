package jacz.util.notification;

/**
 * This interface contains methods for submitting progress notifications of a process.
 */
public interface ProgressNotification<E> {

    /**
     * Adds a new progress notification
     *
     * @param message content of the notification
     */
    public void addNotification(E message);

    /**
     * This method can be invoked to indicate that the task has been finalized
     */
    public void completeTask();
}

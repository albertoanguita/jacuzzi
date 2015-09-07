package jacz.util.notification;

/**
 * This interfaces inherits from the ProgressNotification interface, adding a method for notifying an error in a
 * process, with a message describing the error or what caused it.
 * <p/>
 * Usually, the process will invoke this new method when an error has been raised during the process being
 * carried out. The invocation of this method should imply that no more processing is going to be carried out. Refer
 * to the specific documentation of the actual process to know the details.
 * <p/>
 * A method for reporting timeouts is also included. The invocation of this method should mean that the related process
 * has died due to reaching a time limitation
 */
public interface ProgressNotificationWithError<E, Y> extends ProgressNotification<E> {

    public void error(Y error);

    public void timeout();
}

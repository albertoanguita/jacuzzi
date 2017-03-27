package org.aanguita.jacuzzi.service.ondemand;

import org.aanguita.jacuzzi.concurrency.PeriodicTaskReminder;
import org.aanguita.jacuzzi.id.AlphaNumFactory;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by Alberto on 25/03/2017.
 */
public class ActiveOnDemandService<T> extends AbstractOnDemandService<T> implements OnDemandService<T>, Runnable {

    private static final String TASK_NAME = "event";

    private final long period;

    /**
     * We use our own periodic task reminder for generating events
     */
    private final PeriodicTaskReminder periodicTaskReminder;

    public ActiveOnDemandService(Supplier<T> eventSupplier, long period) {
        super(eventSupplier);
        this.period = period;
        periodicTaskReminder = new PeriodicTaskReminder(getClass().toString() + "-" + getId());
    }

    @Override
    public synchronized void register(Function<T, Boolean> eventCallback) {
        register(AlphaNumFactory.getStaticId(), eventCallback);
    }

    @Override
    void startService() {
        periodicTaskReminder.addPeriodicTask(TASK_NAME, this, false, period, true);
    }

    @Override
    void stopService() {
        periodicTaskReminder.removePeriodicTask(TASK_NAME);
    }

    @Override
    public synchronized void run() {
        event(eventSupplier.get());
    }
}

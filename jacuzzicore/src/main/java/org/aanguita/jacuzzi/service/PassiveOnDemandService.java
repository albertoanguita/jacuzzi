package org.aanguita.jacuzzi.service;

import org.aanguita.jacuzzi.id.AlphaNumFactory;
import org.aanguita.jacuzzi.queues.processor.FinishReadingMessagesException;
import org.aanguita.jacuzzi.queues.processor.MessageHandler;
import org.aanguita.jacuzzi.queues.processor.MessageProcessor;
import org.aanguita.jacuzzi.queues.processor.MessageReader;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by Alberto on 26/03/2017.
 */
public class PassiveOnDemandService<T> extends AbstractOnDemandService<T> implements OnDemandService<T> {

    private static class Reader<T> implements MessageReader<T> {

        private final Supplier<T> supplier;

        private final AtomicBoolean alive;

        public Reader(Supplier<T> supplier) {
            this.supplier = supplier;
            alive = new AtomicBoolean(true);
        }

        @Override
        public T readMessage() throws FinishReadingMessagesException {
            if (alive.get()) {
                return supplier.get();
            } else {
                throw new FinishReadingMessagesException();
            }
        }

        @Override
        public void stop() {
            alive.set(false);
        }
    }

    private static class Handler<T> implements MessageHandler<T> {

        private final PassiveOnDemandService<T> onDemandService;

        private Handler(PassiveOnDemandService<T> onDemandService) {
            this.onDemandService = onDemandService;
        }

        @Override
        public void handleMessage(T message) {

        }

        @Override
        public void close() {}
    }

    private MessageProcessor<T> messageProcessor;

    public PassiveOnDemandService(Supplier<T> eventSupplier) {
        super(eventSupplier);
    }

    @Override
    public synchronized void register(Function<T, Boolean> eventCallback) {
        register(AlphaNumFactory.getStaticId(), eventCallback);
    }

    @Override
    void startService() {
        messageProcessor = new MessageProcessor<T>(new Reader<T>(eventSupplier), new Handler<T>(this), false);
        messageProcessor.start();
    }

    @Override
    void stopService() {
        messageProcessor.stop();
    }
}

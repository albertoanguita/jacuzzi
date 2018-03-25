package org.aanguita.jacuzzi.queues.processor;

import java.util.function.Consumer;

public class MessageProcessorAbstractThread extends Thread {

    private final Consumer<Exception> exceptionConsumer;

    public MessageProcessorAbstractThread(String threadName, Consumer<Exception> exceptionConsumer) {
        super(threadName);
        this.exceptionConsumer = exceptionConsumer;
    }

    protected void consumeException(Exception e) {
        if (exceptionConsumer != null) {
            exceptionConsumer.accept(e);
        }
    }
}

package org.aanguita.jacuzzi.queues.event_processing;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by Alberto on 02/10/2016.
 */
public class MessageProcessorTest {

    private static class MessageReaderImpl implements MessageReader<String> {

        private final ArrayBlockingQueue<String> inputQueue;

        public MessageReaderImpl(ArrayBlockingQueue<String> inputQueue) {
            this.inputQueue = inputQueue;
        }

        @Override
        public String readMessage() throws FinishReadingMessagesException {
            try {
                return inputQueue.take();
            } catch (InterruptedException e) {
                throw new FinishReadingMessagesException();
            }
        }

        @Override
        public void stop() {
            System.out.println("Reader stop");
        }
    }

    private static class MessageHandlerImpl implements MessageHandler<String> {

        private final Output output;

        public MessageHandlerImpl(Output output) {
            this.output = output;
        }

        @Override
        public void handleMessage(String message) {
            output.handleValue(message);
        }

        @Override
        public void close() {
            System.out.println("Handler close");
        }
    }

    private interface Output {

        void handleValue(String value);
    }


    private ArrayBlockingQueue<String> inputQueue;

    private MessageReaderImpl messageReader;

    private Output output;

    private MessageHandlerImpl messageHandler;

    private MessageProcessor<String> messageProcessor;

    @Before
    public void setUp() throws Exception {
        inputQueue = new ArrayBlockingQueue<>(100);
        messageReader = new MessageReaderImpl(inputQueue);
        output = mock(Output.class);
        messageHandler = new MessageHandlerImpl(output);
    }

    @Test
    public void testSingleThread() throws InterruptedException {
        messageProcessor = new MessageProcessor<>(messageReader, messageHandler, false);
        test();
    }

    @Test
    public void testSeparateThreads() throws InterruptedException {
        messageProcessor = new MessageProcessor<>(messageReader, messageHandler, true);
        test();
    }

    private void test() throws InterruptedException {
        messageProcessor.start();
        addMessages();

        ThreadUtil.safeSleep(1000L);

        verify(output).handleValue("one");
        verify(output).handleValue("two");
        verify(output).handleValue("three");

        messageProcessor.stop();
    }

    private void addMessages() throws InterruptedException {
        inputQueue.put("one");
        inputQueue.put("two");
        inputQueue.put("three");
    }
}
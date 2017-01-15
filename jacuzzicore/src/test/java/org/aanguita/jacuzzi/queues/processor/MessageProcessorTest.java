package org.aanguita.jacuzzi.queues.processor;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;

import static org.mockito.Mockito.*;

/**
 * Created by Alberto on 02/10/2016.
 */
public class MessageProcessorTest {

    private static class MessageReaderImpl implements MessageReader<String> {

        private final ArrayBlockingQueue<String> inputQueue;

        MessageReaderImpl(ArrayBlockingQueue<String> inputQueue) {
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
        }
    }

    private static class MessageHandlerImpl implements MessageHandler<String> {

        private final Output output;

        MessageHandlerImpl(Output output) {
            this.output = output;
        }

        @Override
        public void handleMessage(String message) {
            output.handleValue(message);
        }

        @Override
        public void close() {
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
        messageReader = spy(new MessageReaderImpl(inputQueue));
//        messageReader = new MessageReaderImpl(inputQueue);
        output = mock(Output.class);
        messageHandler = spy(new MessageHandlerImpl(output));
//        messageHandler = new MessageHandlerImpl(output);
    }

    @Test
    public void testSingleThread() throws InterruptedException {
        messageProcessor = new MessageProcessor<>(messageReader, messageHandler, false);
        test(false);
    }

    @Test
    public void testSeparateThreads() throws InterruptedException {
        messageProcessor = new MessageProcessor<>(messageReader, messageHandler, true);
        test(true);
    }

    private void test(boolean separateThreads) throws InterruptedException {
        messageProcessor.start();
        addMessages();

        ThreadUtil.safeSleep(100L);

        verify(output).handleValue("one");
        verify(output).handleValue("two");
        verify(output).handleValue("three");

        addPausedMessages();


        verify(output).handleValue("four");
        verify(output, never()).handleValue("five");
        verify(output, never()).handleValue("six");
        Assert.assertEquals(0, messageProcessor.queueSize());
        messageProcessor.resume();
        ThreadUtil.safeSleep(100L);
        verify(output).handleValue("five");
        verify(output).handleValue("six");
        Assert.assertEquals(0, messageProcessor.queueSize());

        messageProcessor.stop();
        ThreadUtil.safeSleep(100L);

        verify(messageReader).stop();
        verify(messageHandler).close();
    }

    private void addMessages() throws InterruptedException {
        inputQueue.put("one");
        inputQueue.put("two");
        inputQueue.put("three");
    }

    private void addPausedMessages() throws InterruptedException {
        inputQueue.put("four");
        ThreadUtil.safeSleep(100L);
        messageProcessor.pause();
        inputQueue.put("five");
        inputQueue.put("six");
    }
}
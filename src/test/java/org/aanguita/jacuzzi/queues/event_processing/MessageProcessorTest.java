package org.aanguita.jacuzzi.queues.event_processing;

import org.junit.Test;

/**
 * Created by Alberto on 02/10/2016.
 */
public class MessageProcessorTest {

    private static class MessageReaderImpl implements MessageReader<String> {

        @Override
        public String readMessage() throws FinishReadingMessagesException {
            return null;
        }

        @Override
        public void stop() {

        }
    }

    @Test
    public void test() {
        // todo
    }
}
package org.aanguita.jacuzzi.service.ondemand;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.junit.Test;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * Created by Alberto on 28/03/2017.
 */
public class ActiveOnDemandServiceTest {

    private static class Client {

        private final int threshold;

        public Client(int threshold) {
            this.threshold = threshold;
        }

        public Function<Integer, Boolean> event() {
            return integer -> {
                System.out.println("Client with threshold" + threshold + " received " + integer + ". Finish = " + (integer >= threshold));
                return integer >= threshold;
            };
        }
    }

    @Test
    public  void test() {
        OnDemandService<Integer> onDemandService = new ActiveOnDemandService<>(new Supplier<Integer>() {

            int i = 0;
            @Override
            public Integer get() {
                System.out.println("Next event: " + i);
                return i++;
            }
        }, 1000L);

        onDemandService.register(new Client(4).event());
        onDemandService.register(new Client(7).event());

        ThreadUtil.safeSleep(10000);
    }

}
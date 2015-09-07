package jacz.util.numeric.test;

import jacz.util.concurrency.task_executor.ParallelTask;
import jacz.util.concurrency.task_executor.ParallelTaskExecutor;
import jacz.util.identifier.UniqueIdentifier;
import jacz.util.identifier.UniqueIdentifierFactory;
import jacz.util.numeric.LongRange;
import jacz.util.numeric.RangeQueue;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 19-mar-2011<br>
 * Last Modified: 19-mar-2011
 */
public class TestRangeQueue {


    public static class Retrieve implements ParallelTask {

        private RangeQueue<LongRange, Long> rq;

        UniqueIdentifier id = UniqueIdentifierFactory.getOneStaticIdentifier();

        public Retrieve(RangeQueue<LongRange, Long> rq) {
            this.rq = rq;
        }

        @Override
        public void performTask() {
            System.out.println(id + " trying to remove data...");
            LongRange lr = rq.remove(11L);
            System.out.println(id + " removed: " + lr);
        }
    }


    public static void main(String args[]) {

        RangeQueue<LongRange, Long> rq = new RangeQueue<LongRange, Long>();

        ParallelTaskExecutor.executeTask(new Retrieve(rq));
        ParallelTaskExecutor.executeTask(new Retrieve(rq));
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        rq.add(new LongRange(10L, 20L));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        rq.add(new LongRange(11L, 30L));
        rq.add(new LongRange(5L, 9L));
        System.out.println(rq.removeNonBlocking(new LongRange(5L, 18L)));


        System.out.println(rq);
        //System.out.println(lr);
    }
}

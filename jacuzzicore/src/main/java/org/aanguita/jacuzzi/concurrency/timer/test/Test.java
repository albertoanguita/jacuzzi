package org.aanguita.jacuzzi.concurrency.timer.test;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.aanguita.jacuzzi.concurrency.timer.TimerAction;
import org.aanguita.jacuzzi.concurrency.timer.Timer;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 28-abr-2010<br>
 * Last Modified: 28-abr-2010
 */
public class Test implements TimerAction {

    public void func() {
        Timer timer = new Timer(3000, this, "HELLO");

        ThreadUtil.safeSleep(500);

        //timer.goOff();
        //timer.goOff();

        //timer.stop();
        //timer.reset(0.1);
        System.out.println(timer.remainingTime());

//        timer.kill();
        //ResettableTimer__REMOVE timer = new ResettableTimer__REMOVE(1000, this);
//        timer.stop();
    }

    public static void main(String args[]) {

        Test test = new Test();
        test.func();
    }

    @Override
    public Long wakeUp(Timer timer) {
        System.out.println("WAKE UP!!!");
        System.out.println(Thread.currentThread().getName());
        return 500L;
    }

}

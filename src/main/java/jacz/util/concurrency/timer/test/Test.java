package jacz.util.concurrency.timer.test;

import jacz.util.concurrency.timer.TimerAction;
import jacz.util.concurrency.timer.Timer;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 28-abr-2010<br>
 * Last Modified: 28-abr-2010
 */
public class Test implements TimerAction {

    public void func() {
        Timer timer = new Timer(3000, this);

        //timer.goOff();
        //timer.goOff();

        //timer.stop();
        //timer.reset(0.1);
        System.out.println(timer.remainingTime());
        //ResettableTimer__REMOVE timer = new ResettableTimer__REMOVE(1000, this);
        //timer.stop();
    }

    public static void main(String args[]) {

        Test test = new Test();
        test.func();
    }

    @Override
    public Long wakeUp(Timer timer) {
        System.out.println("WAKE UP!!!");
        //return 500L;
        return null;
    }

}

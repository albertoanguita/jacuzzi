package org.aanguita.jacuzzi.concurrency.timer;

/**
 * Task that waits the specified millis and then invokes the timer wake up method
 */
class WakeUpTask implements Runnable {

    private final TimerOld timer;

    private boolean finished;

    public WakeUpTask(TimerOld timer) {
        this.timer = timer;
    }

    @Override
    public void run() {
        while (!finished) {
            try {
                Thread.sleep(timer.getMillisForThisRun());
            } catch (InterruptedException e) {
                // the timer interrupted this wake up task because it was stopped -> break
                break;
            }
            finished = !timer.wakeUp(this);
        }
    }
}

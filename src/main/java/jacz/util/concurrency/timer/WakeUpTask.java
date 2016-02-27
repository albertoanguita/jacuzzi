package jacz.util.concurrency.timer;

import jacz.util.concurrency.task_executor.Task;

/**
 * Task that waits the specified millis and then invokes the timer wake up method
 */
class WakeUpTask implements Task {

    private Timer timer;

    private boolean finished;

    public WakeUpTask(Timer timer) {
        this.timer = timer;
    }

    @Override
    public void performTask() {
        while (!finished) {
            try {
                Thread.sleep(timer.getMillis());
            } catch (InterruptedException e) {
                // the timer interrupted this wake up task because it was stopped -> break
                break;
            }
            finished = !timer.wakeUp(this);
        }
    }
}

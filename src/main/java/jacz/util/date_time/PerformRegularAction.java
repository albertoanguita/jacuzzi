package jacz.util.date_time;

/**
 * A class that passively reminds us of performing some action (every regular time, or after certain number of tries)
 */
public class PerformRegularAction {

    private enum ReminderType {
        TRY_COUNT,
        TIME_ELAPSE
    }

    private final int maxTriesCount;

    private final long maxElapsedTime;

    private final ReminderType reminderType;

    private int tryCount;

    private long timeForLastAction;

    public static PerformRegularAction tryCountPerformRegularAction(int maxTriesCount) {
        return new PerformRegularAction(maxTriesCount, 0L, ReminderType.TRY_COUNT);
    }

    public static PerformRegularAction timeElapsePerformRegularAction(long maxElapsedTime) {
        return new PerformRegularAction(0, maxElapsedTime, ReminderType.TIME_ELAPSE);
    }

    private PerformRegularAction(int maxTriesCount, long maxElapsedTime, ReminderType reminderType) {
        this.maxTriesCount = maxTriesCount;
        this.maxElapsedTime = maxElapsedTime;
        this.reminderType = reminderType;
        resetTryCount();
        resetTimeForLastAction(System.currentTimeMillis());
    }

    private void resetTryCount() {
        tryCount = 0;
    }

    private void resetTimeForLastAction(long time) {
        timeForLastAction = time;
    }

    public boolean mustPerformAction() {
        switch (reminderType) {

            case TRY_COUNT:
                if (tryCount >= maxTriesCount) {
                    resetTryCount();
                    return true;
                } else {
                    tryCount++;
                    return false;
                }

            case TIME_ELAPSE:
                long currentTime = System.currentTimeMillis();
                if (currentTime > timeForLastAction + maxElapsedTime) {
                    resetTimeForLastAction(currentTime);
                    return true;
                } else {
                    return false;
                }

            default:
                return false;
        }
    }
}

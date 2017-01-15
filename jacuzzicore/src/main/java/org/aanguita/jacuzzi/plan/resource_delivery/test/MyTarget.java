package org.aanguita.jacuzzi.plan.resource_delivery.test;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 09-jun-2010<br>
 * Last Modified: 09-jun-2010
 */
public class MyTarget {

    private int target;

    public MyTarget(int target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return Integer.toString(target);
    }

    @Override
    public int hashCode() {
        return target;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof MyTarget) && target == ((MyTarget) obj).target;
    }
}

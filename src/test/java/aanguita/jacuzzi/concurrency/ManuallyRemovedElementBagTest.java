package aanguita.jacuzzi.concurrency;

import org.junit.Test;

/**
 * Created by Alberto on 03/05/2016.
 */
public class ManuallyRemovedElementBagTest {

    @Test
    public void test() {

        ManuallyRemovedElementBag manuallyRemovedElementBag = ManuallyRemovedElementBag.getInstance("bag");

        manuallyRemovedElementBag.createElement("hello");
        manuallyRemovedElementBag.destroyElement("hello");
    }
}
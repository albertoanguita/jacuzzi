package org.aanguita.jacuzzi.plan.resource_delivery.test;

import org.aanguita.jacuzzi.time.TimeElapsed;
import org.aanguita.jacuzzi.plan.resource_delivery.ResourceDeliverer;

import java.util.ArrayList;
import java.util.List;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 09-jun-2010<br>
 * Last Modified: 09-jun-2010
 */
public class Test {

    public static void main(String args[]) {

        TimeElapsed timeElapsed = new TimeElapsed();
        ResourceDeliverer<MyTarget, ResourceImpl> resourceDeliverer = new ResourceDeliverer<>(new MessageHandlerImpl(timeElapsed), 20, 1.0d, 1000);

        MyTarget t1 = new MyTarget(1);
        MyTarget t2 = new MyTarget(2);
        MyTarget t3 = new MyTarget(3);

        resourceDeliverer.setDestination(t1, 5);
        resourceDeliverer.setDestination(t2, 10);
        resourceDeliverer.setDestination(t3, 20);


        resourceDeliverer.send(t1, genList("ho", "l", "a"));
        resourceDeliverer.send(t2, genList("j", "od", "er"));
        resourceDeliverer.send(t3, genList("mi", "er", "da"));
        //resourceDeliverer.send(t1, genList("ho", "l", "a"));
        //resourceDeliverer.send(t2, genList("j", "od", "er"));
        //resourceDeliverer.send(t3, genList("mi", "er", "da"));
        resourceDeliverer.stop();

        long time = timeElapsed.measureTime();

        System.out.println("time" + time);


    }

    private static List<ResourceImpl> genList(String... strList) {
        List<ResourceImpl> result = new ArrayList<>(strList.length);
        for (String str : strList) {
            result.add(new ResourceImpl(str));
        }
        return result;
    }
}

package org.aanguita.jacuzzi.plan.resource_delivery.test;

import org.aanguita.jacuzzi.plan.resource_delivery.Resource;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 09-jun-2010<br>
 * Last Modified: 09-jun-2010
 */
public class ResourceImpl implements Resource {

    private String str;

    public ResourceImpl(String str) {
        this.str = str;
    }

    @Override
    public int size() {
        return str.length();
    }

    @Override
    public String toString() {
        return "\"" + str + "\"";
    }
}

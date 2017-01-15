package org.aanguita.jacuzzi.plan.resource_delivery;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 10-jun-2010
 * Time: 12:22:42
 * To change this template use File | Settings | File Templates.
 */
public class TargetResourceFinalizationMessage<T, Y extends Resource> extends TargetResource<T, Y> {

    public TargetResourceFinalizationMessage() {
        super(null, 0, 0);
    }
}

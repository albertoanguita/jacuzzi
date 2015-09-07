package jacz.util.plan.resource_delivery;

/**
 *
 */
public class TargetAndResource<T, Y extends Resource> {

    private T target;

    private Y resource;

    public TargetAndResource(T target, Y resource) {
        this.target = target;
        this.resource = resource;
    }

    public T getTarget() {
        return target;
    }

    public Y getResource() {
        return resource;
    }
}

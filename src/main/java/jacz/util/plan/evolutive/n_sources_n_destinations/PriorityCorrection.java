package jacz.util.plan.evolutive.n_sources_n_destinations;

import java.util.Map;

/**
 *
 */
public interface PriorityCorrection<T, Y> {

    public void correctPriorities(Y source, Map<T, Double> relativePriorities);
}

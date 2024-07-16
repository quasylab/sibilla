package it.unicam.quasylab.sibilla.core.runtime.synthesis;

import java.util.Map;

public class SynthesisTaskFactory {

    public static SynthesisTask createTask(TaskName taskName, Map<String,Object> taskMap, SynthesisStrategy strategy) {
        switch (taskName) {
            case OPTIMAL_FEASIBILITY:
                return new OptimalFeasibilityTask(taskMap,strategy);
            case COMPARATIVE:
                return new ComparisonTask(taskMap,strategy);
            default:
                throw new IllegalArgumentException("Unknown task name");
        }
    }


}

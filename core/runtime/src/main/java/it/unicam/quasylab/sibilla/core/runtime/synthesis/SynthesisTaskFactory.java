package it.unicam.quasylab.sibilla.core.runtime.synthesis;

import java.util.Map;

public class SynthesisTaskFactory {

    public static SynthesisTask createTask(TaskName taskName, Map<String,Object> taskMap, SynthesisStrategy strategy){
        return switch (taskName) {
            case OPTIMAL_FEASIBILITY -> new OptimalFeasibilityTask(taskMap, strategy);
            case COMPARATIVE -> new ComparisonTask(taskMap, strategy);
        };
    }


}

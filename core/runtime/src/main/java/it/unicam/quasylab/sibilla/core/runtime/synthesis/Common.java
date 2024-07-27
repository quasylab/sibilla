package it.unicam.quasylab.sibilla.core.runtime.synthesis;

import java.util.List;
import java.util.Map;

public class Common {


    public static final String DEFAULT_SAMPLING = "lhs";
    public static final String DEFAULT_SURROGATE = "rf";
    public static final String DEFAULT_OPTIMIZATION = "pso";
    public static final int DEFAULT_DATASET_SIZE = 1000;
    public static final double DEFAULT_TRAINING_PORTION = 0.95;


    public static final double DEFAULT_DEADLINE = 100;
    public static final int DEFAULT_REPLICA = 100;
    public static final double DEFAULT_DT = 1.0;

    public static final double DEFAULT_CONVERGENCE_THRESHOLD = 0.01;
    public static final int DEFAULT_MAX_INFILL_ITERATIONS = 20;
    @SuppressWarnings("unchecked")
    public static Map<String,Object> getAsMap(Object obj) {
        if(!(obj instanceof Map)) {
            throw new ClassCastException("not a map");
        }
        return (Map<String, Object>) obj;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getAList(Object obj) {
        if(!(obj instanceof List)) {
            throw new ClassCastException("not a List");
        }
        return (List<Object>) obj;
    }
}

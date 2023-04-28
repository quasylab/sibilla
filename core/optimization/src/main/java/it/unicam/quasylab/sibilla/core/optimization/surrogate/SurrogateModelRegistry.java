package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationAlgorithm;

import java.util.HashMap;
import java.util.Map;

public class SurrogateModelRegistry {

    // SURROGATES IDs
    private static final String GTB_SURROGATE_ID = "gtb";
    private static final String RF_SURROGATE_ID = "rf";
    private static final String RBF_SURROGATE_ID = "rbf";

    // SURROGATES
    private static final SurrogateFactory GTB_SURROGATE = new GradientTreeBoost();
    private static final SurrogateFactory RF_SURROGATE = new RandomForest();
    private static final SurrogateFactory RBF_SURROGATE = new RBFNetwork();
    private final Map<String, SurrogateFactory> registry;
    private final static SurrogateModelRegistry instance = new SurrogateModelRegistry();

    Map<String, Map<String, String>> surrogateProperties = Map.of(
            GTB_SURROGATE_ID,Map.of(
                    "trees","Integer",
                    "loss","String : \"LeastSquares\",\"LeastAbsoluteDeviation\",\"Quantile(REAL_NUMBER)\" or \"Huber(REAL_NUMBER)\"",
                    "max_depth","Integer",
                    "max_nodes","Integer",
                    "node_size","Integer",
                    "shrinkage","Real number",
                    "sample_rate","Real number"
            ),
            RF_SURROGATE_ID,Map.of(
                    "trees","Integer",
                    "mtry","Integer",
                    "max_depth","Integer",
                    "max_nodes","Integer",
                    "node_size","Integer",
                    "rate","Real number"
                    ),
            RBF_SURROGATE_ID,Map.of(
                    "neurons","Integer",
                    "normalized","Boolean"
            )

    );

    public static SurrogateModelRegistry getInstance() {
        return instance;
    }


    private  SurrogateModelRegistry() {
        registry = new HashMap<>();
        initDefaultValues();
    }

    private void initDefaultValues() {
        register(GTB_SURROGATE_ID, GTB_SURROGATE);
        register(RF_SURROGATE_ID, RF_SURROGATE);
        register(RBF_SURROGATE_ID, RBF_SURROGATE);
    }


    public void register(String surrogateId, SurrogateFactory surrogate) {
        this.registry.put(surrogateId, surrogate);
    }

    public SurrogateFactory get(String surrogateID) {
        return this.registry.get(surrogateID);
    }

    public String[] getSurrogates() {
        return this.registry.keySet().toArray(String[]::new);
    }

    public Map<String, String> getSurrogateProperties(String surrogateName){
        return this.surrogateProperties.get(surrogateName);
    }

}

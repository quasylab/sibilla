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

}

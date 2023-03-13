package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.pso.PSOAlgorithm;

import java.util.HashMap;
import java.util.Map;

public class OptimizationAlgorithmRegistry {

    private static final String PSO_ALGORITHM_ID = "pso";
    private static final OptimizationAlgorithm PSO_ALGORITHM = new PSOAlgorithm();
    private final Map<String, OptimizationAlgorithm> registry;

    private final static OptimizationAlgorithmRegistry instance = new OptimizationAlgorithmRegistry();

    public static OptimizationAlgorithmRegistry getInstance() {
        return instance;
    }


    private  OptimizationAlgorithmRegistry() {
        registry = new HashMap<>();
        initDefaultValues();
    }

    private void initDefaultValues() {
        register(PSO_ALGORITHM_ID, PSO_ALGORITHM);
    }

    public void register(String algorithmId, OptimizationAlgorithm algorithm) {
        this.registry.put(algorithmId, algorithm);
    }

    public OptimizationAlgorithm get(String algorithmId) {
        return this.registry.get(algorithmId);
    }

    public String[] getAlgorithms() {
        return this.registry.keySet().toArray(String[]::new);
    }

}

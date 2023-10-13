package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.LTMADSAlgorithm;
import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.pso.PSOAlgorithm;

import java.util.HashMap;
import java.util.Map;

public class OptimizationAlgorithmRegistry {

    private static final String PSO_ALGORITHM_ID = "pso";
    private static final String LTMADS_ALGORITHM_ID = "ltmads";
    private static final OptimizationAlgorithm PSO_ALGORITHM = new PSOAlgorithm();
    private static final OptimizationAlgorithm LTMADS_ALGORITHM = new LTMADSAlgorithm();
    private final Map<String, OptimizationAlgorithm> registry;
    private final static OptimizationAlgorithmRegistry instance = new OptimizationAlgorithmRegistry();
    private final Map<String, Map<String, String>> algorithmsProperties = Map.of(
            PSO_ALGORITHM_ID,Map.of(
                    "inertia          ","Real number",
                    "self_confidence  ", "Real number",
                    "swarm_confidence ","Real number",
                    "particles_number ", "Integer",
                    "iteration        ", "Integer"
            ),
            LTMADS_ALGORITHM_ID,Map.of(
                    "delta_mesh       ","Real number",
                    "delta_poll       ", "Real number",
                    "tau              ","Real number",
                    "epsilon          ", "Real number",
                    "iteration        ", "Integer",
                    "search_points    ", "Integer",
                    "opportunistic    ", "Boolean",
                    "minimal_basis    ", "Boolean"
            )
    );

    public static OptimizationAlgorithmRegistry getInstance() {
        return instance;
    }

    private  OptimizationAlgorithmRegistry() {
        registry = new HashMap<>();
        initDefaultValues();
    }

    private void initDefaultValues() {
        register(PSO_ALGORITHM_ID, PSO_ALGORITHM);
        register(LTMADS_ALGORITHM_ID,LTMADS_ALGORITHM);
    }

    private void register(String algorithmId, OptimizationAlgorithm algorithm) {
        this.registry.put(algorithmId, algorithm);
    }

    public OptimizationAlgorithm get(String algorithmId) {
        return this.registry.get(algorithmId);
    }

    public String[] getAlgorithms() {
        return this.registry.keySet().toArray(String[]::new);
    }

    public Map<String, String> getAlgorithmProperties(String algorithmName){
        return this.algorithmsProperties.get(algorithmName);
    }

}

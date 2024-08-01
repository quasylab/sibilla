package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm;

import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads.LTMADSAlgorithm;
import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads.OrthoMADSAlgorithm;
import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.pso.PSOAlgorithm;
import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.sa.SimulatedAnnealingAlgorithm;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OptimizationAlgorithmRegistry {

    private static final String PSO_ALGORITHM_ID = "pso";
    private static final String LTMADS_ALGORITHM_ID = "ltmads";
    private static final String ORTHOMADS_ALGORITHM_ID = "orthomads";
    private static final String SA_ALGORITHM_ID = "sa";

    private static final OptimizationAlgorithm PSO_ALGORITHM = new PSOAlgorithm();
    private static final OptimizationAlgorithm LTMADS_ALGORITHM = new LTMADSAlgorithm();
    private static final OptimizationAlgorithm ORTHOMADS_ALGORITHM = new OrthoMADSAlgorithm();
    private static final OptimizationAlgorithm SA_ALGORITHM = new SimulatedAnnealingAlgorithm();

    private final Map<String, OptimizationAlgorithm> registry;
    private final static OptimizationAlgorithmRegistry instance = new OptimizationAlgorithmRegistry();
    private final Map<String, Map<String, String>> algorithmsProperties = Map.of(
            PSO_ALGORITHM_ID, Map.of(
                    "inertia          ", "Real number",
                    "self_confidence  ", "Real number",
                    "swarm_confidence ", "Real number",
                    "particles_number ", "Integer",
                    "iteration        ", "Integer"
            ),
            LTMADS_ALGORITHM_ID, Map.of(
                    "delta_mesh       ", "Real number",
                    "delta_poll       ", "Real number",
                    "tau              ", "Real number",
                    "epsilon          ", "Real number",
                    "iteration        ", "Integer",
                    "search_points    ", "Integer",
                    "opportunistic    ", "Boolean",
                    "minimal_basis    ", "Boolean"
            ),
            ORTHOMADS_ALGORITHM_ID, Map.of(
                    "delta_mesh       ", "Real number",
                    "delta_poll       ", "Real number",
                    "tau              ", "Real number",
                    "epsilon          ", "Real number",
                    "iteration        ", "Integer",
                    "search_points    ", "Integer",
                    "opportunistic    ", "Boolean"
            ),
            SA_ALGORITHM_ID, Map.of(
                    "max_iterations       ", "Integer",
                    "initial_temperature  ", "Real number",
                    "cooling_rate         ", "Real number",
                    "cooling_schedule     ", "String (linear, exponential, logarithmic)"
            )
    );

    public static OptimizationAlgorithmRegistry getInstance() {
        return instance;
    }

    private OptimizationAlgorithmRegistry() {
        registry = new HashMap<>();
        initDefaultValues();
    }

    private void initDefaultValues() {
        register(PSO_ALGORITHM_ID, PSO_ALGORITHM);
        register(LTMADS_ALGORITHM_ID, LTMADS_ALGORITHM);
        register(ORTHOMADS_ALGORITHM_ID, ORTHOMADS_ALGORITHM);
        register(SA_ALGORITHM_ID, SA_ALGORITHM);
    }

    private void register(String algorithmId, OptimizationAlgorithm algorithm) {
        this.registry.put(algorithmId, algorithm);
    }

    public OptimizationAlgorithm get(String algorithmId) {
        return this.registry.get(algorithmId);
    }

    public String[] getAlgorithms() {
        return this.getAlgorithmName().toArray(String[]::new);
    }

    public Set<String> getAlgorithmName() {
        return this.registry.keySet();
    }

    public Map<String, String> getAlgorithmProperties(String algorithmName) {
        return this.algorithmsProperties.get(algorithmName);
    }
}

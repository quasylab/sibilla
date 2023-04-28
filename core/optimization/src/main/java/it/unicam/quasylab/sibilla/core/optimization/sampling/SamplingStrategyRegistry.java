package it.unicam.quasylab.sibilla.core.optimization.sampling;

import java.util.HashMap;
import java.util.Map;

public class SamplingStrategyRegistry {


    // SURROGATES IDs
    private static final String LH_SAMPLING_ID = "lhs";
    private static final String FF_SAMPLING_ID = "ffs";
    private static final String R_SAMPLING_ID = "rs";

    // SURROGATES
    private static final SamplingFactory LH_SAMPLING_F= new LatinHyperCubeSamplingFactory();
    private static final SamplingFactory FF_SAMPLING_F = new FullFactorialSamplingFactory();
    private static final SamplingFactory R_SURROGATE_F = new RandomSamplingFactory();
    private final Map<String, SamplingFactory> registry;
    private final static SamplingStrategyRegistry instance = new SamplingStrategyRegistry();

    public static SamplingStrategyRegistry getInstance() {
        return instance;
    }


    private  SamplingStrategyRegistry() {
        registry = new HashMap<>();
        initDefaultValues();
    }

    private void initDefaultValues() {
        register(LH_SAMPLING_ID, LH_SAMPLING_F);
        register(FF_SAMPLING_ID, FF_SAMPLING_F);
        register(R_SAMPLING_ID, R_SURROGATE_F);
    }


    public void register(String surrogateId, SamplingFactory surrogate) {
        this.registry.put(surrogateId, surrogate);
    }

    public SamplingFactory get(String samplingID) {
        return this.registry.get(samplingID);
    }

    public String[] getSamplingStrategies() {
        return this.registry.keySet().toArray(String[]::new);
    }
}

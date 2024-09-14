package it.unicam.quasylab.sibilla.core.runtime.synthesis;

import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.OptimizationAlgorithmRegistry;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.SamplingStrategyRegistry;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.*;
import it.unicam.quasylab.sibilla.tools.synthesis.surrogate.SurrogateModelRegistry;

import java.util.*;

import static it.unicam.quasylab.sibilla.core.runtime.synthesis.Common.*;

public class SynthesisStrategy {

    private final OptimizationAlgorithmRegistry optimizationAlgorithmRegistry = OptimizationAlgorithmRegistry.getInstance();
    private final SamplingStrategyRegistry samplingStrategyRegistry = SamplingStrategyRegistry.getInstance();
    private final SurrogateModelRegistry surrogateModelRegistry = SurrogateModelRegistry.getInstance();

    // ERROR MESSAGES
    private final String UNKNOWN_OPTIMIZATION_MESSAGE = "%s is unknown! Available optimization algorithms are: " +
            Arrays.stream(optimizationAlgorithmRegistry.getAlgorithms()).reduce("\n",(a, b)->a +"\n"+b);
    private final String UNKNOWN_SAMPLING_MESSAGE = "%s is unknown! Available sampling strategy are: " +
            Arrays.stream(samplingStrategyRegistry.getSamplingStrategies()).reduce("\n",(a, b)->a +"\n"+b);
    private final String UNKNOWN_SURROGATE_MESSAGE = "%s is unknown! Available surrogates are: " +
            Arrays.stream(surrogateModelRegistry.getSurrogates()).reduce("\n",(a,b)->a +"\n"+b);

    private HyperRectangle searchSpace;
    private int dataSetSize;
    private double trainingDatasetPortion;


    private String surrogateName;
    private String optimizationName;
    private String samplingName;


    private String[] constraints;
    private final Properties properties;


    private final boolean performInfill;
    private double convergenceThreshold;
    private int maxInfillIterations;


    public SynthesisStrategy(Map<String,Object> strategyMap) {

        this.properties = new Properties();

        if(!strategyMap.containsKey("searchSpace"))
            throw new IllegalArgumentException("searchSpace not found!");
        this.setSearchSpace(strategyMap);

        if(strategyMap.containsKey("sampling"))
            setSamplingStrategy(strategyMap);
        else
            setDefaultSamplingStrategy();

        if(strategyMap.containsKey("surrogate"))
            setSurrogateStrategy(strategyMap);
        else
            setDefaultSurrogateStrategy();

        if(strategyMap.containsKey("optimization"))
            setOptimizationAlgorithm(strategyMap);
        else
            setDefaultOptimizationAlgorithm();

        if(strategyMap.containsKey("infill")){
            this.performInfill = true;
            setInfillParameters(strategyMap);
        }else
            this.performInfill = false;

    }

    /**
     * Sets the search space for the optimization process based on the provided configuration map.
     * The search space is defined as a list of intervals, each specifying a parameter and its range or set of values.
     *
     * @param configurationMap A map containing the search space configuration.
     *                         The map should have a "searchSpace" key with a list of interval specifications.
     *
     * @throws IllegalArgumentException if an unknown interval type is encountered or if required fields are missing.
     *
     * @example
     * Here's an example of how to use this method with different types of intervals:
     * <pre>
     * Map<String, Object> configMap = new HashMap<>();
     * List<Map<String, Object>> searchSpace = new ArrayList<>();
     *
     * // Continuous interval (default type)
     * searchSpace.add(Map.of(
     *     "parameterName", "d",
     *     "lowerBound", 0.5,
     *     "upperBound", 50.0
     * ));
     *
     * // Explicit continuous interval
     * searchSpace.add(Map.of(
     *     "parameterName", "a",
     *     "type", "continuous",
     *     "lowerBound", 0.5,
     *     "upperBound", 50.0
     * ));
     *
     * // Discrete interval
     * searchSpace.add(Map.of(
     *     "parameterName", "b",
     *     "type", "discrete",
     *     "lowerBound", 0.5,
     *     "upperBound", 5.0,
     *     "stepSize", 0.5
     * ));
     *
     * // Set interval
     * searchSpace.add(Map.of(
     *     "parameterName", "c",
     *     "type", "set",
     *     "set", List.of(0.5, 6.0, 3.2, 1.0)
     * ));
     *
     * configMap.put("searchSpace", searchSpace);
     * setSearchSpace(configMap);
     * </pre>
     */
    public void setSearchSpace(Map<String, Object> configurationMap) {
        List<Object> intervalList = getAsList(configurationMap.get("searchSpace"));
        List<Interval> intervals = new ArrayList<>(intervalList.size());

        for (Object intervalObj : intervalList) {
            Map<String, Object> intervalSpec = getAsMap(intervalObj);
            String type = (String) intervalSpec.getOrDefault("type", "continuous");
            String parameterName = (String) intervalSpec.get("parameterName");

            switch (type) {
                case "continuous":
                    intervals.add(createContinuousInterval(parameterName, intervalSpec));
                    break;
                case "discrete":
                    intervals.add(createDiscreteStepInterval(parameterName, intervalSpec));
                    break;
                case "set":
                    intervals.add(createDiscreteSetInterval(parameterName, intervalSpec));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown interval type: " + type);
            }
        }

        this.searchSpace = new HyperRectangle(intervals.toArray(new Interval[0]));
    }

    private ContinuousInterval createContinuousInterval(String parameterName, Map<String, Object> spec) {
        return new ContinuousInterval(
                parameterName,
                getDoubleValue(spec, "lowerBound"),
                getDoubleValue(spec, "upperBound")
        );
    }

    private DiscreteStepInterval createDiscreteStepInterval(String parameterName, Map<String, Object> spec) {
        double lowerBound = getDoubleValue(spec, "lowerBound");
        double upperBound = getDoubleValue(spec, "upperBound");
        double step = spec.containsKey("step")
                ? getDoubleValue(spec, "step")
                : Math.abs(upperBound - lowerBound) / 10;

        return new DiscreteStepInterval(parameterName, lowerBound, upperBound, step);
    }

    private DiscreteSetInterval createDiscreteSetInterval(String parameterName, Map<String, Object> spec) {
        List<Object> set = getAsList(spec.get("set"));
        double[] samples = set.stream()
                .mapToDouble(obj -> ((Number) obj).doubleValue())
                .toArray();

        return new DiscreteSetInterval(parameterName, samples);
    }

    private double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        throw new IllegalArgumentException("Expected a number for key: " + key);
    }
    public void setSamplingStrategy(Map<String,Object> configurationMap) {
        Map<String,Object> samplingSpec = getAsMap(configurationMap.get("sampling"));
        setSamplingStrategy((String) samplingSpec.get("name"));
        if(samplingSpec.containsKey("datasetSize"))
            this.dataSetSize = (int) samplingSpec.get("datasetSize");
        else
            this.dataSetSize = DEFAULT_DATASET_SIZE;
    }

    public void setOptimizationAlgorithm(Map<String,Object> configurationMap) {
        Map<String,Object> optimizationSpec = getAsMap(configurationMap.get("optimization"));
        setOptimizationStrategy((String) optimizationSpec.get("name"));
        if(optimizationSpec.containsKey("constraints")) {
            List<Object> constraintListSpec = getAsList(optimizationSpec.get("constraints"));
            this.constraints = new String[constraintListSpec.size()];
            for (int i = 0; i < constraintListSpec.size(); i++) {
                Map<String,Object> constraintSpec = getAsMap(constraintListSpec.get(i));
                this.constraints[i] = (String) constraintSpec.get("constraint");
            }
        }
        if(optimizationSpec.containsKey("properties"))
            setProperties(getAsList(optimizationSpec.get("properties")));
    }

    public void setSurrogateStrategy(Map<String,Object> configurationMap) {
        Map<String,Object> surrogateSpec = getAsMap(configurationMap.get("surrogate"));
        setSurrogateStrategy((String) surrogateSpec.get("name"));
        if(surrogateSpec.containsKey("trainPortion"))
            this.trainingDatasetPortion = (double) surrogateSpec.get("trainPortion");
        else
            this.trainingDatasetPortion = DEFAULT_TRAINING_PORTION;
        if(surrogateSpec.containsKey("properties"))
            setProperties(getAsList(surrogateSpec.get("properties")));
    }

    private void setInfillParameters(Map<String, Object> strategyMap) {
        Map<String,Object> infillSpec = getAsMap(strategyMap.get("infill"));
        this.convergenceThreshold = infillSpec.containsKey("threshold") ? (double) infillSpec.get("threshold") : DEFAULT_CONVERGENCE_THRESHOLD;
        this.maxInfillIterations = infillSpec.containsKey("maxIteration") ? (int) infillSpec.get("maxIteration") : DEFAULT_MAX_INFILL_ITERATIONS;
    }

    public void setProperties( List<Object> propertiesList){
        for (Object property : propertiesList) {
            Map<String,Object> propertySpec = getAsMap(property);
            String propertyName = (String) propertySpec.get("name");
            String propertyValue = (String) propertySpec.get("value");
            this.properties.setProperty(this.surrogateName+"."+propertyName,propertyValue);
        }
    }

    public void setDefaultSurrogateStrategy(){
        this.surrogateName = DEFAULT_SURROGATE;
        this.trainingDatasetPortion = DEFAULT_TRAINING_PORTION;
    }

    public void setDefaultSamplingStrategy(){
        this.samplingName = DEFAULT_SAMPLING;
        this.dataSetSize = DEFAULT_DATASET_SIZE;
    }

    public void setDefaultOptimizationAlgorithm(){
        this.optimizationName = DEFAULT_OPTIMIZATION;
    }

    public void setSurrogateStrategy(String surrogateName) {
        if(isContained(surrogateModelRegistry.getSurrogates(),surrogateName))
            this.surrogateName =surrogateName;
        else
            throw new IllegalArgumentException(String.format(UNKNOWN_SURROGATE_MESSAGE,surrogateName));
    }

    public void setOptimizationStrategy(String optimizationName) {
        if(isContained(optimizationAlgorithmRegistry.getAlgorithms(),optimizationName))
            this.optimizationName =optimizationName;
        else
            throw new IllegalArgumentException(String.format(UNKNOWN_OPTIMIZATION_MESSAGE,optimizationName));
    }

    public void setSamplingStrategy(String samplingName) {
        if(isContained(samplingStrategyRegistry.getSamplingStrategies(), samplingName))
            this.samplingName =samplingName;
        else
            throw new IllegalArgumentException(
                    String.format(UNKNOWN_SAMPLING_MESSAGE,samplingName));
    }

    private boolean isContained(String[] arrayOfString, String string){
        for (String s : arrayOfString) {
            if (s.equals(string))
                return true;
        }
        return false;
    }

    public HyperRectangle getSearchSpace() {
        return searchSpace;
    }

    public int getDataSetSize() {
        return dataSetSize;
    }

    public double getTrainingDatasetPortion() {
        return trainingDatasetPortion;
    }


    public String getSurrogateName() {
        return surrogateName;
    }

    public String getOptimizationName() {
        return optimizationName;
    }

    public String getSamplingName() {
        return samplingName;
    }

    public Properties getProperties() {
        return properties;
    }

    public String[] getConstraints() {
        return constraints;
    }

    @Override
    public String toString() {

        return "SynthesisStrategy{"+
                "\n dataSetSize=" + getDataSetSize() +
                "\n trainingDatasetPortion=" + getTrainingDatasetPortion() +
                "\n surrogateName='" + getSurrogateName() + '\'' +
                "\n optimizationName='" + getOptimizationName() + '\'' +
                "\n samplingName='" + getSamplingName() + '\'' +
                "\n properties=" + getProperties() +
                "\n searchSpace=" + getSearchSpace() +
                "\n constraints=" + Arrays.toString(getConstraints()) +
                (performInfill ? "\n with infill : tolerance = " + getConvergenceThreshold() + ", max iterations = " + getMaxInfillIterations() : "\n") + "\n" +
                '}';
    }

    private int getMaxInfillIterations() {
        return this.maxInfillIterations;
    }

    private double getConvergenceThreshold() {
        return this.convergenceThreshold;
    }


    public boolean getPerformInfill() {
        return this.performInfill;
    }
}

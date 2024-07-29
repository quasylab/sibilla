package it.unicam.quasylab.sibilla.core.runtime.synthesis;

import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.OptimizationAlgorithmRegistry;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.SamplingStrategyRegistry;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.tools.synthesis.surrogate.SurrogateModelRegistry;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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



    public void setSearchSpace(Map<String,Object> configurationMap){
        List<Object> intervalList = getAList(configurationMap.get("searchSpace"));
        ContinuousInterval[] intervals = new ContinuousInterval[intervalList.size()];
        for (int i = 0; i < intervalList.size(); i++) {
            Map<String,Object> intervalSpec = getAsMap(intervalList.get(i));
            intervals[i] = new ContinuousInterval(
                    (String) intervalSpec.get("parameterName"),
                    (double) intervalSpec.get("lowerBound"),
                    (double) intervalSpec.get("upperBound"));
        }
        this.searchSpace = new HyperRectangle(intervals);
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
            List<Object> constraintListSpec = getAList(optimizationSpec.get("constraints"));
            this.constraints = new String[constraintListSpec.size()];
            for (int i = 0; i < constraintListSpec.size(); i++) {
                Map<String,Object> constraintSpec = getAsMap(constraintListSpec.get(i));
                this.constraints[i] = (String) constraintSpec.get("constraint");
            }
        }
        if(optimizationSpec.containsKey("properties"))
            setProperties(getAList(optimizationSpec.get("properties")));
    }

    public void setSurrogateStrategy(Map<String,Object> configurationMap) {
        Map<String,Object> surrogateSpec = getAsMap(configurationMap.get("surrogate"));
        setSurrogateStrategy((String) surrogateSpec.get("name"));
        if(surrogateSpec.containsKey("trainPortion"))
            this.trainingDatasetPortion = (double) surrogateSpec.get("trainPortion");
        else
            this.trainingDatasetPortion = DEFAULT_TRAINING_PORTION;
        if(surrogateSpec.containsKey("properties"))
            setProperties(getAList(surrogateSpec.get("properties")));
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

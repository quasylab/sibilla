package it.unicam.quasylab.sibilla.core.runtime;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationAlgorithmRegistry;
import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.SamplingStrategyRegistry;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.Interval;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.SurrogateMetrics;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.SurrogateModel;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.SurrogateModelRegistry;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.DataSet;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

public class OptimizationModule {

    // REGISTRIES
    private final OptimizationAlgorithmRegistry optimizationAlgorithmRegistry = OptimizationAlgorithmRegistry.getInstance();
    private final SamplingStrategyRegistry samplingStrategyRegistry = SamplingStrategyRegistry.getInstance();
    private final SurrogateModelRegistry surrogateModelRegistry = SurrogateModelRegistry.getInstance();


    // ERROR MESSAGES
    private final String UNKNOWN_OPTIMIZATION_MESSAGE = "%s is unknown! Available optimization algorithms are: " +
            Arrays.stream(optimizationAlgorithmRegistry.getAlgorithms()).reduce("\n",(a,b)->a +"\n"+b);
    private final String UNKNOWN_SAMPLING_MESSAGE = "%s is unknown! Available sampling strategy are: " +
            Arrays.stream(samplingStrategyRegistry.getSamplingStrategies()).reduce("\n",(a, b)->a +"\n"+b);
    private final String UNKNOWN_SURROGATE_MESSAGE = "%s is unknown! Available surrogates are: " +
            Arrays.stream(surrogateModelRegistry.getSurrogates()).reduce("\n",(a,b)->a +"\n"+b);

    // VARIABLES
    private boolean usingASurrogate;
    private boolean isMinimization;

    private ToDoubleFunction<Map<String, Double>> objectiveFunction;
    private ToDoubleFunction<Map<String, Double>> surrogateFunction;

    private SurrogateModel surrogateModel;
    private SurrogateMetrics surrogateMetrics;

    private String surrogateName;
    private String optimizationName;
    private String samplingName;
    private int dataSetSize;

    private double trainingDatasetPortion;
    private List<Predicate<Map<String, Double>>> constraints= new ArrayList<>();
    private List<Interval> intervals= new ArrayList<>();
    private HyperRectangle searchSpace;
    private Properties properties;

    private Map<String,Double> optimalCoordinates;

    private DataSet dataSet;
    OptimizationTask optimizationTask;


    public OptimizationModule(){
        this.initialise();
    }

    public void setDefaultParameter(){
        this.samplingName = "lhs";
        this.surrogateName = "rf";
        this.optimizationName = "pso";
        this.dataSetSize = 50;
        this.trainingDatasetPortion = 0.9;
        resetProperties();
        resetConstraints();
        resetInterval();
    }



    ///////////////////////////////////////////////////////////////////////////////////////////////////

    // RESET METHODS

    ///////////////////////////////////////////////////////////////////////////////////////////////////


    public void initialise(){
        setDefaultParameter();
    }

    public void resetInterval(){
        this.intervals = new ArrayList<>();
    }

    public void resetProperties(){
        this.properties = new Properties();
    }

    public void resetConstraints(){
        this.constraints = new ArrayList<>();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////

    // OPTIMIZATION STRATEGIES METHODS

    ///////////////////////////////////////////////////////////////////////////////////////////////////


    public void setSurrogateStrategy(String surrogateName) {
        if(isContained(surrogateModelRegistry.getSurrogates(),surrogateName))
            this.surrogateName =surrogateName;
        else
            throw new IllegalArgumentException(UNKNOWN_SURROGATE_MESSAGE);
    }

    public void setOptimizationStrategy(String optimizationName) {
        if(isContained(optimizationAlgorithmRegistry.getAlgorithms(),optimizationName))
            this.optimizationName =optimizationName;
        else
            throw new IllegalArgumentException(UNKNOWN_OPTIMIZATION_MESSAGE);
    }

    public void setSamplingStrategy(String samplingName) {
        if(isContained(samplingStrategyRegistry.getSamplingStrategies(), samplingName))
            this.samplingName =samplingName;
        else
            throw new IllegalArgumentException(UNKNOWN_SAMPLING_MESSAGE);
    }



    ///////////////////////////////////////////////////////////////////////////////////////////////////

    // OBJECTIVE FUNCTION AND CONSTRAINTS METHODS

    ///////////////////////////////////////////////////////////////////////////////////////////////////



    public void setObjectiveFunction(ToDoubleFunction<Map<String,Double>> objectiveFunction){
        this.objectiveFunction = objectiveFunction;
    }

    public void addConstraint(Predicate<Map<String,Double>> constraint){
        this.constraints.add(constraint);
    }



    ///////////////////////////////////////////////////////////////////////////////////////////////////

    // SURROGATE METHODS

    ///////////////////////////////////////////////////////////////////////////////////////////////////



    public void useASurrogateFunction(boolean usingASurrogate){
        this.usingASurrogate = usingASurrogate;
    }

    public void generateSurrogateModel(){
        //TODO CHANGE GRAMMAR FOR ADD TRAINING PORTION
        if(this.dataSet == null)
            generateTrainingSet();
        this.surrogateModel = SurrogateModelRegistry
                .getInstance()
                .get(surrogateName)
                .getSurrogateModel(this.dataSet,this.trainingDatasetPortion,properties);
        //this.surrogateMetrics = this.surrogateModel.getTrainingSetMetrics();
    }

    public void generateSurrogateFunction(){
        generateSurrogateModel();
        this.surrogateFunction = this.surrogateModel.getSurrogateFunction(true);
        this.surrogateMetrics = this.surrogateModel.getTrainingSetMetrics();
    }


    public void generateTrainingSet(){
        generateSearchSpace();
        this.dataSet = new DataSet(
                this.searchSpace,
                SamplingStrategyRegistry.getInstance().get(samplingName).getSamplingTask(),
                this.dataSetSize,
                this.objectiveFunction);
    }


    private void generateSearchSpace(){
        if(this.intervals.size()==0)
            throw new RuntimeException("no interval in the search space has been added");
        this.searchSpace = new HyperRectangle(this.intervals);
    }

    public void setDataSetSize(int dataSetSize) {
        this.dataSetSize = dataSetSize;
    }



    public void setTrainingDatasetPortion(double trainingDatasetPortion) {
        this.trainingDatasetPortion = trainingDatasetPortion;
    }



    public void addInterval(String intervalName, double lowerBound, double upperBound){
        OptionalInt indexOpt = IntStream.range(0, intervals.size())
                .filter(i -> intervals.get(i).getId().equals(intervalName))
                .findFirst();
        if(indexOpt.isPresent())
            intervals.set(indexOpt.getAsInt(),new ContinuousInterval(intervalName,lowerBound,upperBound));
        else
            this.intervals.add(new ContinuousInterval(intervalName,lowerBound,upperBound));
    }



    ///////////////////////////////////////////////////////////////////////////////////////////////////

    // OPTIMIZATION AND SURROGATE PROPERTIES METHODS

    ///////////////////////////////////////////////////////////////////////////////////////////////////




    public void setSurrogateProperty(String propertyName, String property){
        if(surrogateModelRegistry.getSurrogateProperties(this.surrogateName).containsKey(propertyName))
            this.properties.setProperty(this.surrogateName+"."+propertyName,property);
        else
            throw new IllegalArgumentException(this.getUnknownSurrogatePropertyMessage(this.surrogateName));
    }

    public void setOptimizationProperty(String propertyName, String property){
        if(optimizationAlgorithmRegistry.getAlgorithmProperties(this.optimizationName).containsKey(propertyName))
            this.properties.setProperty(this.optimizationName+"."+propertyName,property);
        else
            throw new IllegalArgumentException(this.getUnknownOptimizationPropertyMessage(this.optimizationName));
    }

    public double getSurrogatePrediction(Map<String,Double> parameters){
        if(surrogateFunction != null)
            return this.surrogateFunction.applyAsDouble(parameters);
        else
            throw new NullPointerException("No Surrogate was generated");
    }



    ///////////////////////////////////////////////////////////////////////////////////////////////////

    // MINIMIZATION AND MAXIMIZATION METHODS

    ///////////////////////////////////////////////////////////////////////////////////////////////////

    public void isAMinimizationProblem(boolean isMinimization){
        this.isMinimization = isMinimization;
    }


    public void generateOptimizationTask(){
        generateSearchSpace();
        this.optimizationTask = OptimizationAlgorithmRegistry
                .getInstance()
                .get(optimizationName)
                .getOptimizationTask();
        this.optimizationTask.setProperties(properties);
    }


    public void performOptimization(){
        if(usingASurrogate) {
            if(isMinimization)
                minimizeUsingSurrogate();
            else
                maximizeUsingSurrogate();
        }else{
            if (isMinimization)
                minimize();
            else
                maximize();
        }
    }


    public void minimize(){
        generateOptimizationTask();
        this.optimalCoordinates = this.optimizationTask.minimize( this.objectiveFunction, this.searchSpace, this.constraints, this.properties);
    }


    public void maximize(){
        generateOptimizationTask();
        this.optimalCoordinates = this.optimizationTask.maximize(this.objectiveFunction, this.searchSpace, this.constraints, this.properties);
    }



    public void minimizeUsingSurrogate(){
        generateSurrogateFunction();
        generateOptimizationTask();
        this.optimalCoordinates = this.optimizationTask.minimize( this.surrogateFunction, this.searchSpace, this.constraints, this.properties);
    }


    public void maximizeUsingSurrogate(){
        generateSurrogateFunction();
        generateOptimizationTask();
        this.optimalCoordinates = this.optimizationTask.maximize(this.surrogateFunction, this.searchSpace, this.constraints, this.properties);
    }



    ///////////////////////////////////////////////////////////////////////////////////////////////////

    // GETTERS METHOD

    ///////////////////////////////////////////////////////////////////////////////////////////////////

    public Map<String, Double> getOptimalCoordinates() {
        return optimalCoordinates;
    }


    public boolean isUsingASurrogate(){
        return this.usingASurrogate;
    }

    public DataSet getTrainingSet() {
        return dataSet;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////

    // EVALUATION METHODS

    ///////////////////////////////////////////////////////////////////////////////////////////////////

    public double evaluateObjectiveFunction(Map<String, Double> values) {
        return this.objectiveFunction.applyAsDouble(values);
    }

    public double evaluateSurrogateFunction(Map<String, Double> values) {
        return this.surrogateFunction.applyAsDouble(values);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    // INFO

    ///////////////////////////////////////////////////////////////////////////////////////////////////

    public String info(){
        StringBuilder info = new StringBuilder();
        String emptySpace = "    ";

        info.append("Optimization Strategy: ").append(this.optimizationName).append("\n \n");

        if(isUsingASurrogate()){
            info.append("- - - - - - - - - - - - - - - - - - - - - - - - - - -").append("\n");
            info.append("Surrogate used : ").append(this.surrogateName).append("\n");
            info.append(surrogateMetrics.toString()).append("\n");
            info.append("- - - - - - - - - - - - - - - - - - - - - - - - - - -").append("\n \n");
        }

        info.append(isMinimization ? "Minimizing" :"Maximizing").append(" parameters :").append("\n");
        for (String key:optimalCoordinates.keySet()) {
            info.append(emptySpace).append(key).append(" = ").append(optimalCoordinates.get(key)).append("\n");
        }
        info.append("\n").append(isMinimization ? "Minimum" :"Maximum").append(" found").append("\n");
        info.append(emptySpace).append(this.objectiveFunction.applyAsDouble(this.optimalCoordinates)).append("\n \n");

        if(usingASurrogate){
            info.append("\n").append(isMinimization ? "Minimum" :"Maximum").append(" found in the surrogate").append("\n");
            info.append(emptySpace).append(this.surrogateFunction.applyAsDouble(this.optimalCoordinates)).append("\n \n");
        }

        return info.toString();
    }


    public String infoTrainingSet(){
        StringBuilder info = new StringBuilder();
        String emptySpace = "    ";
        info.append("Size: ").append(this.dataSet.rowCount()).append("\n");
        info.append("Result info: ").append(this.dataSet.rowCount()).append("\n");
        info.append(emptySpace).append("Mean               : ").append(this.dataSet.getResultMean()).append("\n");
        info.append(emptySpace).append("Standard Deviation : ").append(this.dataSet.getResultSD()).append("\n");
        info.append(emptySpace).append("Mode               : ").append(this.dataSet.getResultMode()).append("\n");
        return info.toString();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////

    // UTILS METHODS

    ///////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean isContained(String[] arrayOfString, String string){
        for (String s : arrayOfString) {
            if (s.equals(string))
                return true;
        }
        return false;
    }

    private String getUnknownOptimizationPropertyMessage(String optimizationName){
        StringBuilder message = new StringBuilder("\"%s\" is unknown! properties that can be set are:");
        Map<String,String> propertyMap = optimizationAlgorithmRegistry.getAlgorithmProperties(optimizationName);
        for (String key: propertyMap.keySet()) {
            message.append("\n   - ").append(key).append(" ->  ").append(propertyMap.get(key));
        }
        return message.toString();
    }

    private String getUnknownSurrogatePropertyMessage(String surrogateName){
        StringBuilder message = new StringBuilder("\"%s\" is unknown! properties that can be set are:");
        Map<String,String> propertyMap = surrogateModelRegistry.getSurrogateProperties(surrogateName);
        for (String key: propertyMap.keySet()) {
            message.append("\n   - ").append(key).append(" ->  ").append(propertyMap.get(key));
        }
        return message.toString();
    }





}


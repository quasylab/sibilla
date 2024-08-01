package it.unicam.quasylab.sibilla.tools.synthesis;

import it.unicam.quasylab.sibilla.tools.synthesis.expression.ExpressionInterpreter;
import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.OptimizationAlgorithmRegistry;
import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.OptimizationTask;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.SamplingStrategyRegistry;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.SamplingTask;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.tools.synthesis.surrogate.DataSet;
import it.unicam.quasylab.sibilla.tools.synthesis.surrogate.SurrogateMetrics;
import it.unicam.quasylab.sibilla.tools.synthesis.surrogate.SurrogateModel;
import it.unicam.quasylab.sibilla.tools.synthesis.surrogate.SurrogateModelRegistry;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * The Synthesizer class is responsible for orchestrating the synthesis process,
 * including sampling, surrogate modeling, and optimization.
 */
@SuppressWarnings("unused")
public class Synthesizer {

    private long seed;

    private String chosenOptimizationAlgorithm;
    private String chosenSurrogateModel;
    private String chosenSamplingStrategy;

    private ToDoubleFunction<Map<String,Double>> objectiveFunction;
    private ToDoubleFunction<Map<String,Double>> surrogateFunction;

    private DataSet realFunDataset;

    private boolean useSurrogate;
    private boolean isMinimizationProblem;

    private SamplingTask samplingTask;
    private SurrogateModel surrogateModel;
    private OptimizationTask optimizationTask;
    private HyperRectangle searchSpace;
    private final Properties properties;
    private int numberOfSamples;
    private double trainingPortion;
    private List<Predicate<Map<String, Double>>> constraints;

    private final SamplingStrategyRegistry samplingRegistry = SamplingStrategyRegistry.getInstance();
    private final SurrogateModelRegistry surrogateRegistry = SurrogateModelRegistry.getInstance();
    private final OptimizationAlgorithmRegistry optimizationRegistry = OptimizationAlgorithmRegistry.getInstance();


    private Map<String,Double> optimalCoordinates;
    private double optimalValueObjectiveFunction;
    private double optimalValueSurrogateFunction;

    SurrogateMetrics inSampleMetrics;
    SurrogateMetrics outOfSampleMetrics;

    private final List<SynthesisRecord> synthesisRecords = new ArrayList<>();

    private boolean performInfill;
    private int infillIterations = 0;
    private int infillMaxIterations = 10;
    Consumer<Synthesizer> infillFunction;
    Predicate<Synthesizer> infillStopCondition;
    double convergenceThreshold = 0.01;


    /**
     * Constructs a Synthesizer with specified parameters.
     *
     * @param chosenOptimizationAlgorithm the chosen optimization algorithm
     * @param chosenSurrogateModel        the chosen surrogate model
     * @param chosenSamplingStrategy      the chosen sampling strategy
     * @param isMinimizationProblem       true if the problem is a minimization problem
     * @param objectiveFunction           the objective function to optimize
     * @param searchSpace                 the search space defined as a HyperRectangle
     * @param numberOfSamples             the number of samples to generate
     * @param trainingPortion             the portion of data used for training
     * @param constraints                 the list of constraints for the optimization
     * @param properties                  additional properties for the synthesis process
     * @param useSurrogate                true if a surrogate model should be used
     * @param performInfill               true if infill iterations should be performed
     */
    public Synthesizer(String chosenOptimizationAlgorithm,
                       String chosenSurrogateModel,
                       String chosenSamplingStrategy,
                       boolean isMinimizationProblem,
                       ToDoubleFunction<Map<String,Double>> objectiveFunction,
                       HyperRectangle searchSpace,
                       int numberOfSamples,
                       double trainingPortion,
                       List<Predicate<Map<String,Double>>> constraints,
                       Properties properties,
                       boolean useSurrogate,
                       boolean performInfill) {
        this.chosenOptimizationAlgorithm = chosenOptimizationAlgorithm;
        this.chosenSurrogateModel = chosenSurrogateModel;
        this.chosenSamplingStrategy = chosenSamplingStrategy;
        this.objectiveFunction = objectiveFunction;
        this.searchSpace = searchSpace;
        this.numberOfSamples = numberOfSamples;
        this.trainingPortion = trainingPortion;
        this.constraints = constraints;
        this.properties = properties;
        this.useSurrogate = useSurrogate;
        this.performInfill = performInfill;
        this.isMinimizationProblem = isMinimizationProblem;
        this.setDefaultInfillFunction();
        this.setDefaultInfillStopCondition();
    }
    /**
     * Constructs a Synthesizer with specified parameters, parsing the objective function from a string.
     *
     * @param chosenOptimizationAlgorithm the chosen optimization algorithm
     * @param chosenSurrogateModel        the chosen surrogate model
     * @param chosenSamplingStrategy      the chosen sampling strategy
     * @param isMinimizationProblem       true if the problem is a minimization problem
     * @param objectiveFunction           the objective function as a string expression
     * @param searchSpace                 the search space defined as a HyperRectangle
     * @param numberOfSamples             the number of samples to generate
     * @param trainingPortion             the portion of data used for training
     * @param constraints                 the list of constraints for the optimization
     * @param properties                  additional properties for the synthesis process
     * @param useSurrogate                true if a surrogate model should be used
     * @param performInfill               true if infill iterations should be performed
     */
    public Synthesizer(String chosenOptimizationAlgorithm,
                       String chosenSurrogateModel,
                       String chosenSamplingStrategy,
                       boolean isMinimizationProblem,
                       String objectiveFunction,
                       HyperRectangle searchSpace,
                       int numberOfSamples,
                       double trainingPortion,
                       List<Predicate<Map<String,Double>>> constraints,
                       Properties properties,
                       boolean useSurrogate,
                       boolean performInfill) {
        this(chosenOptimizationAlgorithm,
                chosenSurrogateModel,
                chosenSamplingStrategy,
                isMinimizationProblem,
                ExpressionInterpreter.getArithmeticExpression(objectiveFunction),
                searchSpace,numberOfSamples,trainingPortion,constraints,properties,useSurrogate,performInfill);
    }

    /**
     * Constructs a Synthesizer with specified parameters, parsing the constraints from an array of strings.
     *
     * @param chosenOptimizationAlgorithm the chosen optimization algorithm
     * @param chosenSurrogateModel        the chosen surrogate model
     * @param chosenSamplingStrategy      the chosen sampling strategy
     * @param isMinimizationProblem       true if the problem is a minimization problem
     * @param objectiveFunction           the objective function to optimize
     * @param searchSpace                 the search space defined as a HyperRectangle
     * @param numberOfSamples             the number of samples to generate
     * @param trainingPortion             the portion of data used for training
     * @param constraints                 the array of constraints as string expressions
     * @param properties                  additional properties for the synthesis process
     * @param useSurrogate                true if a surrogate model should be used
     * @param performInfill               true if infill iterations should be performed
     */
    public Synthesizer(String chosenOptimizationAlgorithm,
                       String chosenSurrogateModel,
                       String chosenSamplingStrategy,
                       boolean isMinimizationProblem,
                       ToDoubleFunction<Map<String,Double>> objectiveFunction,
                       HyperRectangle searchSpace,
                       int numberOfSamples,
                       double trainingPortion,
                       String[] constraints,
                       Properties properties,
                       boolean useSurrogate,
                       boolean performInfill) {

        this(chosenOptimizationAlgorithm,chosenSurrogateModel,chosenSamplingStrategy,isMinimizationProblem,objectiveFunction,searchSpace,numberOfSamples,
                trainingPortion,ExpressionInterpreter.getPredicatesFromExpression(constraints),properties,useSurrogate,performInfill);
    }

    /**
     * Constructs a Synthesizer with specified parameters, parsing both the objective function and constraints from strings.
     *
     * @param chosenOptimizationAlgorithm the chosen optimization algorithm
     * @param chosenSurrogateModel        the chosen surrogate model
     * @param chosenSamplingStrategy      the chosen sampling strategy
     * @param isMinimizationProblem       true if the problem is a minimization problem
     * @param objectiveFunction           the objective function as a string expression
     * @param searchSpace                 the search space defined as a HyperRectangle
     * @param numberOfSamples             the number of samples to generate
     * @param trainingPortion             the portion of data used for training
     * @param constraints                 the array of constraints as string expressions
     * @param properties                  additional properties for the synthesis process
     * @param useSurrogate                true if a surrogate model should be used
     * @param performInfill               true if infill iterations should be performed
     */
    public Synthesizer(String chosenOptimizationAlgorithm,
                       String chosenSurrogateModel,
                       String chosenSamplingStrategy,
                       boolean isMinimizationProblem,
                       String objectiveFunction,
                       HyperRectangle searchSpace,
                       int numberOfSamples,
                       double trainingPortion,
                       String[] constraints,
                       Properties properties,
                       boolean useSurrogate,
                       boolean performInfill) {
        this(chosenOptimizationAlgorithm,
                chosenSurrogateModel,
                chosenSamplingStrategy,
                isMinimizationProblem,
                ExpressionInterpreter.getArithmeticExpression(objectiveFunction),
                searchSpace,numberOfSamples,trainingPortion,constraints,properties,useSurrogate,performInfill);
    }

    /**
     * Sets the chosen optimization algorithm.
     *
     * @param chosenOptimizationAlgorithm the chosen optimization algorithm
     */
    public void setChosenOptimizationAlgorithm(String chosenOptimizationAlgorithm) {
        if(!optimizationRegistry.getAlgorithmName().contains(chosenOptimizationAlgorithm))
            throw new IllegalArgumentException("Optimization algorithm not found");
        this.chosenOptimizationAlgorithm = chosenOptimizationAlgorithm;
    }
    /**
     * Sets the chosen surrogate model.
     *
     * @param chosenSurrogateModel the chosen surrogate model
     */
    public void setChosenSurrogateModel(String chosenSurrogateModel) {
        if(!surrogateRegistry.getSurrogateName().contains(chosenSurrogateModel))
            throw new IllegalArgumentException("Surrogate model not found");
        this.chosenSurrogateModel = chosenSurrogateModel;
    }

    /**
     * Sets the chosen sampling strategy.
     *
     * @param chosenSamplingStrategy the chosen sampling strategy
     */
    public void setChosenSamplingStrategy(String chosenSamplingStrategy) {
        if(!samplingRegistry.getSamplingStrategiesName().contains(chosenSamplingStrategy))
            throw new IllegalArgumentException("Sampling strategy not found");
        this.chosenSamplingStrategy = chosenSamplingStrategy;
    }
    /**
     * Sets whether to use a surrogate model.
     *
     * @param useSurrogate true if a surrogate model should be used
     */
    public void setUseSurrogate(boolean useSurrogate) {
        this.useSurrogate = useSurrogate;
    }
    /**
     * Sets the search space.
     *
     * @param searchSpace the search space defined as a HyperRectangle
     */
    public void setSearchSpace(HyperRectangle searchSpace) {
        this.searchSpace = searchSpace;
    }

    /**
     * Sets the training portion.
     *
     * @param trainingPortion the portion of data used for training
     */
    public void setTrainingPortion(double trainingPortion) {
        this.trainingPortion = trainingPortion;
    }
    /**
     * Sets the number of samples.
     *
     * @param numberOfSamples the number of samples to generate
     */
    public void setNumberOfSamples(int numberOfSamples) {
        this.numberOfSamples = numberOfSamples;
    }
    /**
     * Sets the constraints for the optimization.
     *
     * @param constraints the list of constraints
     */
    public void setConstraints(List<Predicate<Map<String, Double>>> constraints) {
        this.constraints = constraints;
    }

    /**
     * Sets a property for the synthesis process.
     *
     * @param propertyName the name of the property
     * @param property     the value of the property
     */
    public void setProperty(String propertyName, String property){
        this.properties.setProperty(propertyName,property);
    }
    /**
     * Sets the seed for random number generation.
     *
     * @param seed the seed
     */
    public void setSeed(long seed) {
        this.seed = seed;
    }
    /**
     * Generates a sampling task.
     */
    private void generateSampleTask() {
        this.samplingTask = samplingRegistry.get(this.chosenSamplingStrategy).getSamplingTask();
    }
    /**
     * Generates the dataset for the real function.
     */
    private void generateRealFunDataset() {
        this.generateSampleTask();
        this.realFunDataset = new DataSet(this.searchSpace, this.samplingTask, this.numberOfSamples, this.objectiveFunction, this.seed);
    }
    /**
     * Generates the surrogate model.
     */
    private void generateSurrogateModel(){
        this.generateRealFunDataset();
        this.surrogateModel = surrogateRegistry.get(this.chosenSurrogateModel).getSurrogateModel(this.realFunDataset, this.trainingPortion,this.properties);
        this.surrogateModel.setSeed(seed);
        this.surrogateModel.fit();
        this.surrogateFunction = surrogateModel.getSurrogateFunction();
        this.inSampleMetrics = surrogateModel.getInSampleMetrics();
        this.outOfSampleMetrics = surrogateModel.getOutOfSampleMetrics();
    }

    /**
     * Gets the surrogate function.
     *
     * @return the surrogate function
     */
    public ToDoubleFunction<Map<String, Double>> getSurrogateFunction() {
         return surrogateFunction;
    }
    /**
     * Generates the optimization task.
     */
    private void generateOptimizationTask(){
        this.optimizationTask = optimizationRegistry.get(this.chosenOptimizationAlgorithm).getOptimizationTask();
        this.optimizationTask.setProperties(this.properties);
    }
    /**
     * Performs the minimization process.
     */
    private void minimize(){
        generateOptimizationTask();
        if(this.useSurrogate){
            this.generateSurrogateModel();
            this.optimalCoordinates = this.optimizationTask.minimize(this.surrogateFunction, this.searchSpace, this.constraints, this.properties,this.seed);
        }
        else
            this.optimalCoordinates = this.optimizationTask.minimize(this.objectiveFunction, this.searchSpace, this.constraints, this.properties,this.seed);
    }

    /**
     * Performs the maximization process.
     */
    private void maximize(){
        generateOptimizationTask();
        if(this.useSurrogate){
            this.generateSurrogateModel();
            this.optimalCoordinates = this.optimizationTask.maximize(this.surrogateFunction, this.searchSpace, this.constraints, this.properties,this.seed);
        }
        else
            this.optimalCoordinates = this.optimizationTask.maximize(this.objectiveFunction, this.searchSpace, this.constraints, this.properties,this.seed);

    }


    /**
     * Searches for the optimal solutions.
     */
    public void searchOptimalSolution() {
        if(this.isMinimizationProblem)
            this.minimize();
        else
            this.maximize();

        evaluateOptimaValues();
        if (performInfill) {
            while (infillIterations < infillMaxIterations && !infillStopCondition.test(this)) {
                infillFunction.accept(this);

                if(this.isMinimizationProblem)
                    this.minimize();
                else
                    this.maximize();

                evaluateOptimaValues();
                recordSynthesis();
                infillIterations++;
            }
        }
        recordSynthesis();
    }
    /**
     * Evaluates the optimal values of the objective and surrogate functions.
     */
    private void evaluateOptimaValues(){
        this.optimalValueObjectiveFunction = this.objectiveFunction.applyAsDouble(this.optimalCoordinates);
        if(useSurrogate){
            this.optimalValueSurrogateFunction = this.surrogateFunction.applyAsDouble(this.optimalCoordinates);
        }
    }

    public DataSet generateFunDataset() {
        this.generateRealFunDataset();
        return realFunDataset;
    }

    public DataSet getFunDataset() {
        return realFunDataset;
    }

    public Map<String,double[]> getDataSate(){
        return getFunDataset().getColumnDataAsMap();
    }



    public void reset() {
        this.optimalCoordinates = null;
        this.optimalValueObjectiveFunction = Double.NaN;
        this.optimalValueSurrogateFunction = Double.NaN;

        this.realFunDataset = null;
        this.surrogateModel = null;
        this.surrogateFunction = null;

        this.samplingTask = null;
        this.optimizationTask = null;

        generateSampleTask();

        if (this.useSurrogate) {
            generateRealFunDataset();
            generateSurrogateModel();
        }
    }


    public SamplingTask getSamplingTask() {
        return samplingTask;
    }

    public SurrogateModel getSurrogateModel() {
        return surrogateModel;
    }

    public OptimizationTask getOptimizationTask() {
        return optimizationTask;
    }

    public Map<String, Double> getOptimalCoordinates() {
        return optimalCoordinates;
    }

    public List<SynthesisRecord> getSynthesisRecords() {
        return synthesisRecords;
    }

    public SynthesisRecord getLastSynthesisRecord() {
        return synthesisRecords.get(synthesisRecords.size()-1);
    }

    public long getSeed() {
        return seed;
    }

    public String getChosenOptimizationAlgorithm() {
        return chosenOptimizationAlgorithm;
    }

    public String getChosenSurrogateModel() {
        return chosenSurrogateModel;
    }

    public String getChosenSamplingStrategy() {
        return chosenSamplingStrategy;
    }

    public ToDoubleFunction<Map<String, Double>> getObjectiveFunction() {
        return objectiveFunction;
    }

    public void setObjectiveFunction(ToDoubleFunction<Map<String, Double>> objectiveFunction) {
        this.objectiveFunction = objectiveFunction;
    }

    public boolean isUseSurrogate() {
        return useSurrogate;
    }

    public boolean isMinimizationProblem() {
        return isMinimizationProblem;
    }

    public void setMinimizationProblem(boolean minimizationProblem) {
        isMinimizationProblem = minimizationProblem;
    }

    public HyperRectangle getSearchSpace() {
        return searchSpace;
    }

    public Properties getProperties() {
        return properties;
    }

    public int getNumberOfSamples() {
        return numberOfSamples;
    }

    public double getTrainingPortion() {
        return trainingPortion;
    }

    public List<Predicate<Map<String, Double>>> getConstraints() {
        return constraints;
    }

    public SurrogateMetrics getInSampleMetrics() {
        return inSampleMetrics;
    }

    public void setInSampleMetrics(SurrogateMetrics inSampleMetrics) {
        this.inSampleMetrics = inSampleMetrics;
    }

    public SurrogateMetrics getOutOfSampleMetrics() {
        return outOfSampleMetrics;
    }

    public void setOutOfSampleMetrics(SurrogateMetrics outOfSampleMetrics) {
        this.outOfSampleMetrics = outOfSampleMetrics;
    }

    public boolean isPerformInfill() {
        return performInfill;
    }

    public void setInfillMaxIterations(int infillMaxIterations) {
        this.infillMaxIterations = infillMaxIterations;
    }

    public void setPerformInfill(boolean performInfill) {
        this.performInfill = performInfill;
    }

    public void setDefaultInfillFunction(){
        this.infillFunction = s -> {
            s.getSearchSpace().changeCenter(s.optimalCoordinates);
            s.setSearchSpace(s.searchSpace.getScaledCopy(0.5));
            s.setNumberOfSamples((int) Math.ceil(s.getNumberOfSamples() * 1.5));
        };
    }

    public void setDefaultInfillStopCondition(){
        this.infillStopCondition = s -> Math.abs(s.optimalValueObjectiveFunction - s.optimalValueSurrogateFunction) <=  convergenceThreshold;
    }

    public Consumer<Synthesizer> getInfillFunction() {
        return infillFunction;
    }

    public void setInfillFunction(Consumer<Synthesizer> infillFunction) {
        this.infillFunction = infillFunction;
    }

    public Predicate<Synthesizer> getInfillStopCondition() {
        return infillStopCondition;
    }

    public void setInfillStopCondition(Predicate<Synthesizer> infillStopCondition) {
        this.infillStopCondition = infillStopCondition;
    }

    public void recordSynthesis() {
        SynthesisRecord record = new SynthesisRecord(
                chosenOptimizationAlgorithm,
                chosenSurrogateModel,
                chosenSamplingStrategy,
                objectiveFunction,
                surrogateFunction,
                realFunDataset,
                useSurrogate,
                isMinimizationProblem,
                searchSpace,
                properties,
                numberOfSamples,
                trainingPortion,
                constraints,
                optimalCoordinates,
                optimalValueObjectiveFunction,
                optimalValueSurrogateFunction,
                inSampleMetrics,
                outOfSampleMetrics
        );
        synthesisRecords.add(record);
    }

    public double getOptimalValueObjectiveFunction() {
        return optimalValueObjectiveFunction;
    }

    public double getOptimalValueSurrogateFunction() {
        return optimalValueSurrogateFunction;
    }
}

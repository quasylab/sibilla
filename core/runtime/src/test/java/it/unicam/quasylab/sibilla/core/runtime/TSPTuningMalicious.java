package it.unicam.quasylab.sibilla.core.runtime;

import it.unicam.quasylab.sibilla.core.models.pm.PopulationModel;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationModelDefinition;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationTask;
import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.pso.PSOTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.LatinHyperCubeSamplingTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.RandomForest;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.SurrogateFactory;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.SurrogateModel;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.tools.stl.QuantitativeMonitor;
import it.unicam.quasylab.sibilla.core.util.Signal;
import it.unicam.quasylab.sibilla.langs.pm.ModelGenerationException;
import it.unicam.quasylab.sibilla.langs.pm.PopulationModelGenerator;
import it.unicam.quasylab.sibilla.langs.stl.StlLoader;
import it.unicam.quasylab.sibilla.langs.stl.StlModelGenerationException;
import it.unicam.quasylab.sibilla.langs.stl.StlMonitorFactory;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class TSPTuningMalicious {

    private int numberOfTrajectories;
    private SimulationEnvironment se;
    private RandomGenerator rg;


    private String[] getFormulae(double time){
        String stability = String.format(Locale.US, """
            measure %%RED
            measure %%BLUE
            formula formula_stability [] : \\E[0,%f][ %%RED >= 1.0 ]   endformula
            """, time);

        String coherence = String.format(Locale.US, """
            measure %%RED
            measure %%BLUE
            formula formula_coherence [] : ([%%RED> 3 * %%BLUE]&&(\\E[0,%f][%%BLUE<=0.0])) endformula
            """, time);

        String redPresence = String.format(Locale.US, """
            measure %%RED
            measure %%BLUE
            formula formula_red [] : (\\G[0,%f][%%RED >= 0.5]) endformula
            """, time);


//       String maliciousPresence = String.format(Locale.US, """
//            measure %%MAL
//            formula formula_mal [] : ((\\E[0, 1/4 * %f][%%MAL >= 0.01])&&(\\G[3/4 * %f,%f]([%%MAL >= 0.25] && [%%MAL <= 0.5]))) endformula
//            """, time, time, time);

        String maliciousPresence = String.format(Locale.US, """
            measure %%MAL
            formula formula_mal [] : ((\\E[0, 1/4 * %f]([%%MAL >= 0.01]))<->(\\G[3/4 * %f,%f]([%%MAL >= 0.25] && [%%MAL <= 0.75]))) endformula
            """, time, time, time);

        return new String[]{stability, coherence, redPresence, maliciousPresence};
    }


    public String getFormulaName(String formula) {
        String regex = "formula\\s+(\\w+)\\s+\\[\\]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(formula);
        return matcher.find() ? matcher.group(1) : "";
    }
    @Disabled
    @Test
    public void testString(){
        for (String formula : getFormulae(25)){
            System.out.println("\n \n"+getFormulaName(formula)+"\n"+formula);
        }
    }

    /**
     * TSP base model
     */
    private String getTSPBaseString(){
        return """
                  species RED;
                  species BLUE;
                  species UNC;
                  species MAL;
                
                  const persuasion_rate = 1.0;
                   param becoming_malevolent = 0.1;
                
                  rule UNC_to_BLUE {
                    UNC-[ (%MAL>=0.5? 0:1) * #UNC * persuasion_rate * %BLUE ]-> BLUE
                  }
                                
                  rule UNC_to_RED {
                    UNC -[ (%MAL>=0.5? 0:1) * #UNC * persuasion_rate * %RED ]-> RED
                  }
                                
                                
                  rule UNC_to_MAL  {
                    UNC-[#UNC * persuasion_rate * %MAL ]-> MAL
                  }
                                
                  rule BLUE_to_UNC {
                    BLUE -[ (%MAL>=0.5? 0:1) * #BLUE * persuasion_rate * %RED ]-> UNC
                  }
                                
                  rule RED_to_UNC {
                    RED -[ (%MAL>=0.5? 0:1) * #RED * persuasion_rate * %BLUE ]-> UNC
                  }
                               
                """;
    }
    private String getTspRingString(){
        return """
                  const K = 5;       /* length of the ring */
                  
                    species RED of [0,K];
                    species BLUE of [0,K];
                    species UNC of [0,K];
                    species MAL of [0,K];
                  
                    param persuasion_rate = 3.0; /* persuasion rate */
                    param becoming_malevolent = 0.1;
                  
                  
                    rule UNC_to_BLUE for i in [0,K] {
                      UNC[i]-[ (%MAL[i]>=0.5? 0:1) * #UNC[i] * persuasion_rate * (%BLUE[i] + %BLUE[(i+1)%K] + %BLUE[(i-1+K)%K])/3 ]-> BLUE[i]
                    }
                  
                    rule UNC_to_RED for i in [0,K] {
                      UNC[i]-[(%MAL[i]>=0.5? 0:1) * #UNC[i] * persuasion_rate * (%RED[i] + %RED[(i+1)%K] + %RED[(i-1+K)%K])/3 ]-> RED[i]
                    }
                  
                  
                    rule UNC_to_MAL for i in [0,K] {
                      UNC[i]-[#UNC[i] * persuasion_rate * %MAL[i] ]-> MAL[i]
                    }
                  
                    rule BLUE_to_UNC for i in [0,K] {
                      BLUE[i]-[ (%MAL[i]>=0.5? 0:1) * #BLUE[i] * persuasion_rate * (%RED[i] + %RED[(i+1)%K] + %RED[(i-1+K)%K])/3 ]-> UNC[i]
                    }
                  
                    rule RED_to_UNC for i in [0,K] {
                      RED[i]-[ (%MAL[i]>=0.5? 0:1) * #RED[i] * persuasion_rate * (%BLUE[i] + %BLUE[(i+1)%K] + %BLUE[(i-1+K)%K])/3 ]-> UNC[i]
                    }
                  
                          
                """;
    }
    private Map<String, ToDoubleFunction<PopulationState>> getMeasureMapTSPBase(){
        Map<String, ToDoubleFunction<PopulationState>> measuresMappingTSP = new HashMap<>();
        measuresMappingTSP.put("%RED", s -> s.getFraction(0));
        measuresMappingTSP.put("%BLUE", s -> s.getFraction(1));
        measuresMappingTSP.put("%MAL", s -> s.getFraction(3));
        return measuresMappingTSP;
    }
    private Map<String, ToDoubleFunction<PopulationState>> getMeasureMapTSPRing(){
        Map<String, ToDoubleFunction<PopulationState>> measuresMappingTSP = new HashMap<>();
        measuresMappingTSP.put("%RED", s -> s.getFraction(0,1,2,3,4));
        measuresMappingTSP.put("%BLUE", s -> s.getFraction(5,6,7,8,9));
        measuresMappingTSP.put("%MAL", s -> s.getFraction(15,16,17,18,19));
        return measuresMappingTSP;
    }
    private PopulationModelDefinition getPopulationModelDefinition(String modelSpecification) {
        PopulationModelGenerator pmg =new PopulationModelGenerator(modelSpecification);
        try {
            return pmg.getPopulationModelDefinition();
        } catch (ModelGenerationException e) {
            throw new RuntimeException(e);
        }
    }
    private PopulationModel getPopulationModel(String modelSpecification){
        return getPopulationModelDefinition(modelSpecification).createModel();
    }
    private QuantitativeMonitor<PopulationState> getQuantitativeMonitor(String formulaDefinition, String formulaName, double[] formulaParameter, Map<String, ToDoubleFunction<PopulationState>> measureMapFunction) throws StlModelGenerationException {
        StlLoader loader = new StlLoader(formulaDefinition);
        StlMonitorFactory<PopulationState> monitorFactory = loader.getModelFactory(measureMapFunction);
        return monitorFactory.getQuantitativeMonitor(formulaName, formulaParameter);
    }
    private BiFunction<double[],double[],Double> euclideanDistance(){
        return (vec1,vec2) ->{
            if (vec1.length != vec2.length) throw new IllegalArgumentException("Vectors must have the same length");
            double sumOfSquares = 0;
            for (int i = 0; i < vec1.length; i++) {
                double difference = vec1[i] - vec2[i];
                sumOfSquares += difference * difference;
            }
            return Math.sqrt(sumOfSquares);
        };
    }


    // Chebyshev Distance
    private BiFunction<double[], double[], Double> chebyshevDistance() {
        return (vec1, vec2) -> {
            if (vec1.length != vec2.length) throw new IllegalArgumentException("Vectors must have the same length");
            return IntStream.range(0, vec1.length)
                    .mapToDouble(i -> Math.abs(vec1[i] - vec2[i]))
                    .max()
                    .orElseThrow(() -> new IllegalArgumentException("Vectors must not be empty"));
        };
    }
    private BiFunction<double[],double[],Double> euclideanDistanceInMaximization(){
        return (vec1,vec2) ->{
            if (vec1.length != vec2.length) throw new IllegalArgumentException("Vectors must have the same length");
            double sumOfSquares = 0;
            for (int i = 0; i < vec1.length; i++) {
                double difference = Math.max(0,vec1[i] - vec2[i]); // x_i - y_i
                sumOfSquares += difference * difference;
            }
            return Math.sqrt(sumOfSquares);
        };
    }
    private Trajectory<PopulationState> sampleATrajectory(PopulationModel currentModel, PopulationState currentInitialState, double timeHorizon){
        Trajectory<PopulationState> trajectory= se.sampleTrajectory(
                rg,
                currentModel,
                currentInitialState,
                timeHorizon);
        trajectory.setEnd(timeHorizon);
        return trajectory;
    }
    private double robustness(PopulationModel currentModel, PopulationState currentInitialState, QuantitativeMonitor<PopulationState> currentMonitor){
        double robustness = 0.0;
        for (int i = 0; i < this.numberOfTrajectories; i++) {
            Trajectory<PopulationState> trj = this.sampleATrajectory(currentModel,
                    currentInitialState,
                    currentMonitor.getTimeHorizon());
            Signal robustnessSignal = currentMonitor.monitor(trj);
            double currentRobustness = robustnessSignal.valueAt(0.0);
            robustness += currentRobustness;

        }
        return robustness / this.numberOfTrajectories;

    }

    private PopulationState getBaseInitialState(){
        return new PopulationState(new int[]{
                10,  // RED
                10,   // BLUE
                75,  // UNC
                5    // MAL
        });
    }

    private PopulationState getInitialRingState(Map<String,Double> param){
        int numberOfRedAndBlue = 20;
        int numberOfUncAndMal = 80;
        int k = 5;
        ToIntFunction<Double> redFromFrac= fraction -> Math.toIntExact(Math.round(fraction * numberOfRedAndBlue / k));
        ToIntFunction<Double> blueFromFrac = fraction -> Math.toIntExact(Math.round((1-fraction) * numberOfRedAndBlue / k));
        double malInFrac = param.getOrDefault("malInFrac", 0.0); // Get the fraction of MAL
        int quantityOfMalPerSpace = Math.toIntExact(Math.round(malInFrac * numberOfUncAndMal / k));
        int quantityOfUncPerSpace = Math.toIntExact(Math.round((1 - malInFrac) * numberOfUncAndMal / k)); // Corrected
        return new PopulationState(new int[]{
                redFromFrac.applyAsInt(param.get("redFracIn0")),   //  0 - RED[0]
                redFromFrac.applyAsInt(param.get("redFracIn1")),   //  1 - RED[1]
                redFromFrac.applyAsInt(param.get("redFracIn2")),   //  2 - RED[2]
                redFromFrac.applyAsInt(param.get("redFracIn3")),   //  3 - RED[3]
                redFromFrac.applyAsInt(param.get("redFracIn4")),   //  4 - RED[4]
                blueFromFrac.applyAsInt(param.get("redFracIn0")),   //  5 - BLUE[0]
                blueFromFrac.applyAsInt(param.get("redFracIn1")),   //  6 - BLUE[1]
                blueFromFrac.applyAsInt(param.get("redFracIn2")),   //  7 - BLUE[2]
                blueFromFrac.applyAsInt(param.get("redFracIn3")),   //  8 - BLUE[3]
                blueFromFrac.applyAsInt(param.get("redFracIn4")),   //  9 - BLUE[4]
                quantityOfUncPerSpace,  // 10 - UNC[0]
                quantityOfUncPerSpace,  // 11 - UNC[1]
                quantityOfUncPerSpace,  // 12 - UNC[2]
                quantityOfUncPerSpace,  // 13 - UNC[3]
                quantityOfUncPerSpace,  // 14 - UNC[4]
                quantityOfMalPerSpace,   // 15 - MAL[0]
                quantityOfMalPerSpace,   // 16 - MAL[1]
                quantityOfMalPerSpace,   // 17 - MAL[2]
                quantityOfMalPerSpace,   // 18 - MAL[3]
                quantityOfMalPerSpace    // 19 - MAL[4]
        });
    }
    private double robustnessInitialStateParametrized(PopulationModelDefinition currentModelDefinition, QuantitativeMonitor<PopulationState> currentMonitor, Map<String,Double> parameterization){
        return  robustness(currentModelDefinition.createModel(), getInitialRingState(parameterization),currentMonitor);
    }
    private double[] robustnessVector(PopulationModel currentModel, PopulationState currentInitialState, QuantitativeMonitor<PopulationState>[] currentMonitors){
        double[] robustnessVector = new double[currentMonitors.length];
        for (int i = 0; i < currentMonitors.length; i++) {
            robustnessVector[i] = robustness(currentModel,currentInitialState,currentMonitors[i]);
        }
        return robustnessVector;
    }
    private double[] robustnessVectorInitialStateParametrized(PopulationModelDefinition currentModelDefinition, QuantitativeMonitor<PopulationState>[] currentMonitors,Map<String,Double> parameterization){
        double[] robustnessVector = new double[currentMonitors.length];
        for (int i = 0; i < currentMonitors.length; i++) {
            robustnessVector[i] = robustnessInitialStateParametrized(currentModelDefinition,currentMonitors[i],parameterization);
        }
        return robustnessVector;
    }
    private HyperRectangle getSearchSpaceTspRing(){
        return new HyperRectangle(
                new ContinuousInterval("redFracIn0",0.1,1.0),
                new ContinuousInterval("redFracIn1",0.1,1.0),
                new ContinuousInterval("redFracIn2",0.1,1.0),
                new ContinuousInterval("redFracIn3",0.1,1.0),
                new ContinuousInterval("redFracIn4",0.1,1.0),
                new ContinuousInterval("malInFrac",0.0,1.0)
        );
    }
    private ToDoubleFunction<Map<String,Double>> getSurrogateFunction(ToDoubleFunction<Map<String,Double>> realFunction, HyperRectangle hr){
        SurrogateFactory surrogateFactory = new RandomForest();
        SurrogateModel randomForestModel = surrogateFactory.getSurrogateModel(
                realFunction,
                new LatinHyperCubeSamplingTask(),
                hr,1000,0.95,
                new Properties());
        return randomForestModel.getSurrogateFunction(true);
    }

    private List<Predicate<Map<String, Double>>> getConstraintsList(){
        List<Predicate<Map<String, Double>>> constraints = new ArrayList<>();
        Predicate<Map<String,Double>> constraint = map ->
                map.get("redFracIn0") + map.get("redFracIn1")
                + map.get("redFracIn2") + map.get("redFracIn3") + map.get("redFracIn4") >= 1.5
                &&
                map.get("redFracIn0") + map.get("redFracIn1")
                + map.get("redFracIn2") + map.get("redFracIn3") + map.get("redFracIn4") <= 3.5;
        constraints.add(constraint);
        return constraints;
    }

    private QuantitativeMonitor<PopulationState>[] getTSPMonitors(Map<String, ToDoubleFunction<PopulationState>> map) throws StlModelGenerationException {
        String[] formulae = getFormulae(25);
        QuantitativeMonitor<PopulationState>[] monitors = new QuantitativeMonitor[formulae.length];
        for (int i = 0; i < formulae.length; i++) {
            monitors[i] = getQuantitativeMonitor(formulae[i],getFormulaName(formulae[i]),new double[]{},map);
        }
        return monitors;
    }

    private QuantitativeMonitor<PopulationState>[] getTSPBaseMonitors() throws StlModelGenerationException {
        return getTSPMonitors(getMeasureMapTSPBase());
    }
    private QuantitativeMonitor<PopulationState>[] getTSPRingMonitors() throws StlModelGenerationException {
        return getTSPMonitors(getMeasureMapTSPRing());
    }

    private void printMinimizationResult(double[] baseRobustVector, Map<String,Double> solution,QuantitativeMonitor<PopulationState>[] mutatedMonitors,ToDoubleFunction<Map<String,Double>> surrogateRobustnessDistance  ){
        System.out.println();
        System.out.println("MINIMIZING");
        System.out.println();
        double[] variantRobustVector = robustnessVector(getPopulationModel(getTspRingString()),getInitialRingState(solution),mutatedMonitors);
        System.out.println("Robustness vector (base model): ");
        System.out.println("    > "+Arrays.toString(baseRobustVector));
        System.out.println("Robustness vector for optimal parameter (variant model): ");

        System.out.println("    > "+Arrays.toString(variantRobustVector));
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("Distances : ");
        System.out.println("    > Real          = "+this.euclideanDistance().apply(baseRobustVector,variantRobustVector));
        System.out.println("    > Surrogate     = "+surrogateRobustnessDistance.applyAsDouble(solution));
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("Solution founded:");
        printSolutionAsMap(solution);
double sumOfRedPercentage = solution.values().stream().mapToDouble(Double::doubleValue).sum() - solution.get("malInFrac");
        System.out.println("Sum of RED percentage : "+sumOfRedPercentage);    }



    private void printResultsMaxBetter(double[] baseRobustVector, Map<String,Double> solution,QuantitativeMonitor<PopulationState>[] mutatedMonitors,ToDoubleFunction<Map<String,Double>> surrogateRobustnessDistance  ){
        System.out.println();
        System.out.println("MAXIMIZING (Better)");
        System.out.println();
        double[] variantRobustVector = robustnessVector(getPopulationModel(getTspRingString()),getInitialRingState(solution),mutatedMonitors);
        System.out.println("Robustness vector (base model): ");
        System.out.println("    > "+Arrays.toString(baseRobustVector));
        System.out.println("Robustness vector for optimal parameter (variant model): ");
        System.out.println("    > "+Arrays.toString(variantRobustVector));
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("Distances : ");
        System.out.println("    > Real          = "+this.euclideanDistanceInMaximization().apply(variantRobustVector,baseRobustVector));
        System.out.println("    > Surrogate     = "+surrogateRobustnessDistance.applyAsDouble(solution));
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("Solution founded:");
        printSolutionAsMap(solution);
        double sumOfRedPercentage = solution.values().stream().mapToDouble(Double::doubleValue).sum() - solution.get("malInFrac");
        System.out.println("Sum of RED percentage : "+sumOfRedPercentage);    }

    private void printResultsMaxWorse(double[] baseRobustVector, Map<String,Double> solution,QuantitativeMonitor<PopulationState>[] mutatedMonitors,ToDoubleFunction<Map<String,Double>> surrogateRobustnessDistance  ){
        System.out.println();
        System.out.println("MAXIMIZING (Worse)");
        System.out.println();
        double[] variantRobustVector = robustnessVector(getPopulationModel(getTspRingString()),getInitialRingState(solution),mutatedMonitors);
        System.out.println("Robustness vector (base model): ");
        System.out.println("    > "+Arrays.toString(baseRobustVector));
        System.out.println("Robustness vector for optimal parameter (variant model): ");
        System.out.println("    > "+Arrays.toString(variantRobustVector));
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("Distances : ");
        System.out.println("    > Real          = "+this.euclideanDistanceInMaximization().apply(baseRobustVector,variantRobustVector));
        System.out.println("    > Surrogate     = "+surrogateRobustnessDistance.applyAsDouble(solution));
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("Solution founded:");
        printSolutionAsMap(solution);
        double sumOfRedPercentage = solution.values().stream().mapToDouble(Double::doubleValue).sum() - solution.get("malInFrac");
        System.out.println("Sum of RED percentage : "+sumOfRedPercentage);
    }
    private Map<String,Double> minimizePreserving() throws StlModelGenerationException {

        QuantitativeMonitor<PopulationState>[] baseMonitors =  getTSPBaseMonitors();
        QuantitativeMonitor<PopulationState>[] mutatedMonitors =  getTSPRingMonitors();

        double[] baseRobustVector = robustnessVector(getPopulationModel(getTSPBaseString()), getBaseInitialState(),baseMonitors);

        ToDoubleFunction<Map<String,Double>> robustnessDistance = param ->{
            double[] mutatedRobustVector = robustnessVectorInitialStateParametrized(
                    getPopulationModelDefinition(getTspRingString()),
                    mutatedMonitors,
                    param);
            return this.euclideanDistance().apply(baseRobustVector,mutatedRobustVector);

        };


        HyperRectangle hr = getSearchSpaceTspRing();
        ToDoubleFunction<Map<String,Double>> surrogateRobustnessDistance = getSurrogateFunction(robustnessDistance,hr);
        Properties psoProp = new Properties();
        psoProp.put("pso.particles_number",1000);
        OptimizationTask optTask = new PSOTask(psoProp);
        Map<String,Double> solution = optTask.minimize(surrogateRobustnessDistance,hr,getConstraintsList());
        printMinimizationResult(baseRobustVector,solution,mutatedMonitors,surrogateRobustnessDistance);
        return solution;
    }

    private Map<String,Double> maximizeWorse() throws StlModelGenerationException{
        QuantitativeMonitor<PopulationState>[] baseMonitors =  getTSPBaseMonitors();
        QuantitativeMonitor<PopulationState>[] mutatedMonitors =  getTSPRingMonitors();
        double[] baseRobustVector= robustnessVector(getPopulationModel(getTSPBaseString()), getBaseInitialState(),baseMonitors);

        ToDoubleFunction<Map<String,Double>> robustnessDistance = param ->{
            double[] mutatedRobustVector = robustnessVectorInitialStateParametrized(
                    getPopulationModelDefinition(getTspRingString()),
                    mutatedMonitors,
                    param);
            return this.euclideanDistanceInMaximization().apply(baseRobustVector,mutatedRobustVector);

        };

        HyperRectangle hr = getSearchSpaceTspRing();
        ToDoubleFunction<Map<String,Double>> surrogateRobustnessDistance = getSurrogateFunction(robustnessDistance,hr);
        Properties psoProp = new Properties();
        psoProp.put("pso.particles_number",1000);
        OptimizationTask optTask = new PSOTask(psoProp);
        Map<String,Double> solution = optTask.maximize(surrogateRobustnessDistance,hr,getConstraintsList());
        printResultsMaxWorse(baseRobustVector,solution,mutatedMonitors,surrogateRobustnessDistance);
        return solution;
    }

    private Map<String,Double> maximizeBetter() throws StlModelGenerationException{

        QuantitativeMonitor<PopulationState>[] baseMonitors =  getTSPBaseMonitors();
        QuantitativeMonitor<PopulationState>[] mutatedMonitors =  getTSPRingMonitors();
        double[] baseRobustVector= robustnessVector(getPopulationModel(getTSPBaseString()), getBaseInitialState(),baseMonitors);

        ToDoubleFunction<Map<String,Double>> robustnessDistance = param ->{
            double[] mutatedRobustVector = robustnessVectorInitialStateParametrized(
                    getPopulationModelDefinition(getTspRingString()),
                    mutatedMonitors,
                    param);
            return this.euclideanDistanceInMaximization().apply(mutatedRobustVector,baseRobustVector);

        };
        HyperRectangle hr = getSearchSpaceTspRing();
        ToDoubleFunction<Map<String,Double>> surrogateRobustnessDistance = getSurrogateFunction(robustnessDistance,hr);
        Properties psoProp = new Properties();
        psoProp.put("pso.particles_number",1000);
        OptimizationTask optTask = new PSOTask(psoProp);
        Map<String,Double> solution = optTask.maximize(surrogateRobustnessDistance,hr,getConstraintsList());
        printResultsMaxBetter(baseRobustVector,solution,mutatedMonitors,surrogateRobustnessDistance);
        return solution;
    }
    @Disabled
    @Test
    public void testMinimizing() throws StlModelGenerationException {
        numberOfTrajectories = 50;
        se = new SimulationEnvironment();
        rg = new DefaultRandomGenerator();
        minimizePreserving();
    }
    @Disabled
    @Test
    public void testMaximizingBetter() throws StlModelGenerationException {
        numberOfTrajectories = 50;
        se = new SimulationEnvironment();
        rg = new DefaultRandomGenerator();
        maximizeBetter();
    }
    @Disabled
    @Test
    public void testMaximizingWorse() throws StlModelGenerationException {
        numberOfTrajectories = 50;
        se = new SimulationEnvironment();
        rg = new DefaultRandomGenerator();
        maximizeWorse();
    }
    @Disabled
    @Test
    public void testWhole() throws StlModelGenerationException {
        System.out.println("\n \n \n");
        testMinimizing();
        System.out.println("\n \n \n");
        testMaximizingWorse();
        System.out.println("\n \n \n");
        testMaximizingBetter();
    }


    @Disabled
    @Test
    public void testModel(){
        PopulationModel pm = getPopulationModel(getTspRingString());

        System.out.println(Arrays.toString(pm.measures()));

        Map<String,Double> parameters = new HashMap<>();

        parameters.put("redFracIn0",0.5);
        parameters.put("redFracIn1",0.5);
        parameters.put("redFracIn2",0.5);
        parameters.put("redFracIn3",0.5);
        parameters.put("redFracIn4",0.5);
        this.se  = new SimulationEnvironment();
        this.rg = new DefaultRandomGenerator();
        Trajectory<PopulationState> t = sampleATrajectory(pm,getInitialRingState(parameters),20);
        System.out.println(t);
    }
    @Disabled
    @Test
    public void testPrintResult(){
        Map<String,Double> parameters = new HashMap<>();

        parameters.put("redFracIn0",0.264);
        parameters.put("redFracIn1",0.011);
        parameters.put("redFracIn2",0.32112);
        parameters.put("redFracIn3",0.4888);
        parameters.put("redFracIn4",0.8923);
        parameters.put("malInFrac",0.15);

        printSolutionAsMap(parameters);
    }
    @Disabled
    @Test
    public void testConstraint(){
        List<Predicate<Map<String, Double>>> constraintsList = getConstraintsList();
        Predicate<Map<String, Double>> constraint1 = constraintsList.get(0);

    }


    public void printSolutionAsMap(Map<String, Double> parameters) {

        int numberOfRedAndBlue = 20;
        int numberOfUncAndMal = 80;
        int k = 5;

        // Extract malInFrac directly since it doesn't depend on the red fractions
        double malInFrac = parameters.getOrDefault("malInFrac", 0.0);
        int quantityOfUncPerSpace = Math.toIntExact(Math.round((1 - malInFrac) * numberOfUncAndMal / k));
        int quantityOfMalPerSpace = Math.toIntExact(Math.round(malInFrac * numberOfUncAndMal / k));
        int totalUnc = quantityOfUncPerSpace * k;
        int totalMal = quantityOfMalPerSpace * k;

        // For RED and BLUE
        ToIntFunction<Double> redFromFrac = fraction -> Math.toIntExact(Math.round(fraction * numberOfRedAndBlue / k));
        ToIntFunction<Double> blueFromFrac = fraction -> Math.toIntExact(Math.round((1 - fraction) * numberOfRedAndBlue / k));

        // TreeMap to sort the parameters (if you need sorting)
        TreeMap<String, Double> sortedParameters = new TreeMap<>(parameters);

        int totalRed = 0;
        int totalBlue = 0;

        System.out.println("Quantities of RED, BLUE, UNC, and MAL");
        System.out.println("__________________________________________");

        // Process RED and BLUE quantities
        for (Map.Entry<String, Double> entry : sortedParameters.entrySet()) {
            if (entry.getKey().startsWith("redFracIn")) {
                String index = entry.getKey().substring(entry.getKey().length() - 1);
                double fraction = entry.getValue();
                int red = redFromFrac.applyAsInt(fraction);
                int blue = blueFromFrac.applyAsInt(fraction);
                totalRed += red;
                totalBlue += blue;

                System.out.printf("#RED[%s] = %d             #BLUE[%s] = %d\n", index, red, index, blue);
            }
        }

        // Print the total quantities for RED and BLUE
        System.out.println("__________________________________________");
        System.out.printf("#TOTAL_RED = %d \n#TOTAL_BLUE = %d\n", totalRed, totalBlue);
        System.out.println("--------------------------------------------------");

        // Assuming the structure is uniform across segments, print UNC and MAL for each segment
        for (int i = 0; i < k; i++) {
            System.out.printf("#UNC[%d] = %d             #MAL[%d] = %d\n", i, quantityOfUncPerSpace, i, quantityOfMalPerSpace);
        }

        // Print the total quantities for UNC and MAL
        System.out.println("--------------------------------------------------");
        System.out.printf("#TOTAL_UNC = %d \n#TOTAL_MAL = %d\n", totalUnc, totalMal);
    }

    public  void printSolutionAsMapOLD(Map<String, Double> parameters) {
        int n = 20;
        int k = 5;
        ToIntFunction<Double> redFromFrac = fraction -> Math.toIntExact(Math.round(fraction * n / k));
        ToIntFunction<Double> blueFromFrac = fraction -> Math.toIntExact(Math.round((1 - fraction) * n / k));

        // Use TreeMap to automatically sort the keys
        TreeMap<String, Double> sortedParameters = new TreeMap<>(parameters);

        System.out.println("percentage");
        int totalRed = 0;
        int totalBlue = 0;

        for (Map.Entry<String, Double> entry : sortedParameters.entrySet()) {
            String key = entry.getKey();
            double fraction = entry.getValue();
            int red = redFromFrac.applyAsInt(fraction);
            int blue = blueFromFrac.applyAsInt(fraction);
            totalRed += red;
            totalBlue += blue;

            System.out.printf("%%RED_%s = %.2f             %%BLUE_%s = %.2f\n", key.substring(key.length() - 1), fraction, key.substring(key.length() - 1), 1 - fraction);
        }

        System.out.println("__________________________________________");
        System.out.println("quantity");

        for (Map.Entry<String, Double> entry : sortedParameters.entrySet()) {
            String key = entry.getKey();
            double fraction = entry.getValue();
            int red = redFromFrac.applyAsInt(fraction);
            int blue = blueFromFrac.applyAsInt(fraction);

            System.out.printf("#RED_%s = %d             #BLUE_%s = %d\n", key.substring(key.length() - 1), red, key.substring(key.length() - 1), blue);
        }

        System.out.println("--------------------------------------------------");
        System.out.printf("#RED = %d \n#BLUE = %d\n", totalRed, totalBlue);
    }




}

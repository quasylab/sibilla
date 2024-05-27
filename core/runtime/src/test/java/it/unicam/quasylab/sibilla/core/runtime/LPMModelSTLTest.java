package it.unicam.quasylab.sibilla.core.runtime;

//import it.unicam.quasylab.sibilla.core.models.pm.PopulationModel;
//import it.unicam.quasylab.sibilla.core.models.pm.PopulationModelDefinition;
//import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
//import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.pso.PSOTask;
//import it.unicam.quasylab.sibilla.core.optimization.sampling.FullFactorialSamplingTask;
//import it.unicam.quasylab.sibilla.core.optimization.sampling.LatinHyperCubeSamplingTask;
//import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.ContinuousInterval;
//import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
//import it.unicam.quasylab.sibilla.core.optimization.surrogate.DataSet;
//import it.unicam.quasylab.sibilla.core.optimization.surrogate.RandomForest;
//import it.unicam.quasylab.sibilla.core.optimization.surrogate.SurrogateFactory;
//import it.unicam.quasylab.sibilla.core.optimization.surrogate.SurrogateModel;
//import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
//import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
//import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
//import it.unicam.quasylab.sibilla.core.tools.stl.QualitativeMonitor;
//import it.unicam.quasylab.sibilla.core.tools.stl.QuantitativeMonitor;
//import it.unicam.quasylab.sibilla.core.util.Signal;
//import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
//import it.unicam.quasylab.sibilla.langs.pm.ModelGenerationException;
//import it.unicam.quasylab.sibilla.langs.pm.PopulationModelGenerator;
//import it.unicam.quasylab.sibilla.langs.stl.StlLoader;
//import it.unicam.quasylab.sibilla.langs.stl.StlModelGenerationException;
//import it.unicam.quasylab.sibilla.langs.stl.StlMonitorFactory;
//import org.apache.commons.math3.random.RandomGenerator;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.util.*;
//import java.util.function.BiFunction;
//import java.util.function.Function;
//import java.util.function.ToDoubleFunction;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;

public class LPMModelSTLTest {



//    private class LPMScreening{
//        public LPMScreening(String modelDefinition) throws ModelGenerationException {
//            PopulationModelGenerator pmg = new PopulationModelGenerator(modelDefinition);
//            PopulationModelDefinition pmd = pmg.getPopulationModelDefinition();
//            PopulationModel pm = pmd.createModel();
//            System.out.println(Arrays.toString(pm.measures()));
//        }
//    }
//
//    @SuppressWarnings("All")
//    private void storeCSV(DataSet dataSet, String outputFolder, String fileName) throws IOException {
//        CSVWriter writer = new CSVWriter(outputFolder, "", "");
//        writer.write(fileName,dataSet);
//    }
//
//
//
//    /**
//     * The LPMModel class represents a simulation model for a specific population system.
//     * It provides methods for setting parameters, generating trajectories, and computing various
//     * metrics and measures based on the trajectories.
//     */
//    private static class LPMModelOld {
//
//        final private SimulationEnvironment se;
//        final private RandomGenerator rg;
//        final private PopulationModelDefinition pmd;
//        PopulationState initialState;
//        private final QuantitativeMonitor<PopulationState> quantitativeMonitor;
//        private final QualitativeMonitor<PopulationState> qualitativeMonitor;
//        private PopulationModel pm;
//        private int numberOfTrajectories;
//
//        public LPMModelOld(String modelDefinition,
//                           String formulaDefinition,
//                           PopulationState initialState,
//                           Map<String, ToDoubleFunction<PopulationState>> measureMapFunction,
//                           String formulaName,
//                           double[] formulaParameter,
//                           int numberOfTrajectories) throws ModelGenerationException, StlModelGenerationException {
//            this.numberOfTrajectories = numberOfTrajectories;
//            this.initialState = initialState;
//            this.se = new SimulationEnvironment();
//            this.rg = new DefaultRandomGenerator();
//            PopulationModelGenerator pmg = new PopulationModelGenerator(modelDefinition);
//            this.pmd = pmg.getPopulationModelDefinition();
//            this.pm = pmd.createModel();
//            StlLoader loader = new StlLoader(formulaDefinition);
//            StlMonitorFactory<PopulationState> monitorFactory = loader.getModelFactory(measureMapFunction);
//            this.quantitativeMonitor = monitorFactory.getQuantitativeMonitor(formulaName, formulaParameter);
//            this.qualitativeMonitor = monitorFactory.getQualitativeMonitor(formulaName, formulaParameter);
//        }
//        @SuppressWarnings("unused")
//        public void setNumberOfTrajectories(int numberOfTrajectories) {
//            this.numberOfTrajectories = numberOfTrajectories;
//        }
//        @SuppressWarnings("unused")
//        public void setSeed(long seed){
//            this.rg.setSeed(seed);
//        }
//
//        public LPMModelOld(String modelDefinition,
//                           String formulaDefinition,
//                           PopulationState initialState,
//                           Map<String, ToDoubleFunction<PopulationState>> measureMapFunction,
//                           String formulaName) throws StlModelGenerationException, ModelGenerationException {
//            this(modelDefinition, formulaDefinition,initialState, measureMapFunction, formulaName, new double[]{}, 100);
//        }
//
//        private void setParameters(Map<String,Double> parameters){
//            for (String key : parameters.keySet()){
//                this.pmd.setParameter(key,new SibillaDouble(parameters.get(key)));
//            }
//            this.pm = this.pmd.createModel();
//        }
//
//        private double getTimiHorizon(){
//            return this.quantitativeMonitor.getTimeHorizon();
//        }
//
//        private Trajectory<PopulationState> sampleATrajectory(){
//            Trajectory<PopulationState> trajectory= this.se.sampleTrajectory(
//                    this.rg,
//                    this.pm,
//                    this.initialState,
//                    this.getTimiHorizon());
//            trajectory.setEnd(getTimiHorizon());
//            return trajectory;
//        }
//
//        public double averageRobustness(){
//            double r = 0;
//            for (int i = 0; i < this.numberOfTrajectories; i++) {
//                Trajectory<PopulationState> trj  = sampleATrajectory();
//                Signal monitoredSignal = quantitativeMonitor.monitor(trj);
//                double currentRobustness = monitoredSignal.valueAt(0);
//                r += currentRobustness;
//            }
//            return  r/this.numberOfTrajectories;
//        }
//
//        @SuppressWarnings("unused")
//        public ToDoubleFunction<Map<String,Double>> getAverageRobustnessFunction(){
//            return m -> {
//                this.setParameters(m);
//                return this.averageRobustness();
//            };
//        }
//
//        public double probReachFormulaAtTime0(){
//            double[] probabilities = QualitativeMonitor
//                    .computeProbability(
//                            this.qualitativeMonitor,
//                            this::sampleATrajectory,
//                            this.numberOfTrajectories,
//                            new double[]{0.0}
//                    );
//            return probabilities[0];
//        }
//
//
//        public ToDoubleFunction<Map<String,Double>> getProbReachFormulaAtTime0Function(){
//            return m -> {
//                this.setParameters(m);
//                return this.probReachFormulaAtTime0();
//            };
//        }
//
//
//
//
//    }
//
//
//
//
//    private static class LPMModelTuner{
//
//        private SimulationEnvironment se;
//        private RandomGenerator rg;
//
//        // Population Models definition
//        private PopulationModelDefinition pmdBase;
//        private PopulationModelDefinition pmdMutated;
//
//        // Population Models initial state
//        PopulationState initialStateBase;
//        PopulationState initialStateMutated;
//
//        // Population Models
//        private PopulationModel pmBase;
//        private PopulationModel pmMutated;
//
//        private Map<String, ToDoubleFunction<PopulationState>> measureMapFunction;
//
//        private QuantitativeMonitor[] quantitativeMonitors;
//        private int numberOfTrajectories;
//
//        private double[] robustnessAt0;
//
//        public LPMModelTuner(){
//            this.numberOfTrajectories = 100;
//            this.se = new SimulationEnvironment();
//            this.rg = new DefaultRandomGenerator();
//        }
//
//        private BiFunction<double[],double[],Double> euclideanDistance(){
//            return (vec1,vec2) ->{
//                if (vec1.length != vec2.length) throw new IllegalArgumentException("Vectors must have the same length");
//                double sumOfSquares = 0;
//                for (int i = 0; i < vec1.length; i++) {
//                    double difference = vec1[i] - vec2[i];
//                    sumOfSquares += difference * difference;
//                }
//                return Math.sqrt(sumOfSquares);
//            };
//        }
//
//
//
//        public void minimizeDistance(){
//            double[] baseRobustness = robustness(this.pmBase,this.initialStateBase,quantitativeMonitors);
//            Function<Map<String,Double>,Double> preservation = parameter -> {
//                double[] mutatedRobustness = robustness(this.pmdMutated,this.initialStateMutated,quantitativeMonitors,parameter);
//                return this.euclideanDistance().apply(baseRobustness,mutatedRobustness);
//            };
//        }
//
//        public void setNumberOfTrajectories(int numberOfTrajectories){
//            this.numberOfTrajectories = numberOfTrajectories;
//        }
//
//        public void setSeed(long seed){
//            this.rg.setSeed(seed);
//        }
//
//        public void setInitialStates(PopulationState initialStateBase, PopulationState initialStateMutated){
//            this.initialStateBase = initialStateBase;
//            this.initialStateMutated = initialStateMutated;
//        }
//
//        public void setMeasureMap(Map<String, ToDoubleFunction<PopulationState>> measureMapFunction){
//            this.measureMapFunction = measureMapFunction;
//        }
//
//        public void setPopulationModels(String modelDefinition, String mutatedModelDefinition) throws ModelGenerationException {
//            PopulationModelGenerator pmg =new PopulationModelGenerator(modelDefinition);
//            PopulationModelGenerator pmgMutated = new PopulationModelGenerator(mutatedModelDefinition);
//            this.pmdBase = pmg.getPopulationModelDefinition();
//            this.pmdMutated = pmgMutated.getPopulationModelDefinition();
//            this.pmBase = pmdBase.createModel();
//            this.pmMutated = pmdMutated.createModel();
//        }
//
//
//        public void setQuantitativeMonitors(String[] formulaeDefinition, String[] formulaeNames,double[][] formulaParameters ) throws StlModelGenerationException {
//            this.quantitativeMonitors = new QuantitativeMonitor[formulaeDefinition.length];
//            this.robustnessAt0 = new double[formulaeDefinition.length];
//            for (int i = 0; i < formulaeDefinition.length; i++) {
//                StlLoader loader = new StlLoader(formulaeDefinition[i]);
//                if (this.measureMapFunction == null) throw new NullPointerException("Measure map is null");
//                StlMonitorFactory<PopulationState> monitorFactory = loader.getModelFactory(this.measureMapFunction);
//                this.quantitativeMonitors[i] = monitorFactory.getQuantitativeMonitor(formulaeNames[i], formulaParameters[i]);
//            }
//        }
//
//        private QuantitativeMonitor[] getQuantitativeMonitors(String[] formulaeDefinition,
//                                                              String[] formulaeNames,
//                                                              double[][] formulaParameters,
//                                                              Map<String, ToDoubleFunction<PopulationState>> measureMapFunction) throws StlModelGenerationException {
//            QuantitativeMonitor<PopulationState>[] quantitativeMonitors = new QuantitativeMonitor[formulaeDefinition.length];
//            for (int i = 0; i < formulaeNames.length; i++) {
//                quantitativeMonitors[i] = getQuantitativeMonitor(formulaeDefinition[i],formulaeNames[i],formulaParameters[i],measureMapFunction);
//            }
//            return quantitativeMonitors;
//        }
//        private QuantitativeMonitor getQuantitativeMonitor(String formulaDefinition,
//                                                           String formulaName,
//                                                           double[] formulaParameter,
//                                                           Map<String, ToDoubleFunction<PopulationState>> measureMapFunction) throws StlModelGenerationException {
//            StlLoader loader = new StlLoader(formulaDefinition);
//            StlMonitorFactory<PopulationState> monitorFactory = loader.getModelFactory(measureMapFunction);
//            return monitorFactory.getQuantitativeMonitor(formulaName, formulaParameter);
//        }
//
//        private Trajectory<PopulationState> sampleATrajectory(PopulationModel currentModel,
//                                                              PopulationState currentInitialState,
//                                                              double timeHorizon){
//            Trajectory<PopulationState> trajectory= this.se.sampleTrajectory(
//                    this.rg,
//                    currentModel,
//                    currentInitialState,
//                    timeHorizon);
//            trajectory.setEnd(timeHorizon);
//            return trajectory;
//        }
//
//
//        private double robustness(PopulationModel currentModel,
//                                  PopulationState currentInitialState,
//                                  QuantitativeMonitor<PopulationState> currentMonitor){
//            double robustness = 0.;
//            for (int i = 0; i < this.numberOfTrajectories; i++) {
//                Trajectory<PopulationState> trj = this.sampleATrajectory(currentModel,
//                        currentInitialState,
//                        currentMonitor.getTimeHorizon());
//                Signal robustnessSignal = currentMonitor.monitor(trj);
//                double currentRobustness = robustnessSignal.valueAt(0.0);
//                robustness += currentRobustness;
//
//            }
//            return robustness / this.numberOfTrajectories;
//
//        }
//
//        private double robustness(PopulationModelDefinition currentModelDefinition,
//                                  PopulationState currentInitialState,
//                                  QuantitativeMonitor<PopulationState> currentMonitor,
//                                  Map<String,Double> parameterization){
//            for (String key : parameterization.keySet()){
//                currentModelDefinition.setParameter(key,new SibillaDouble(parameterization.get(key)));
//            }
//            return  robustness(currentModelDefinition.createModel(),currentInitialState,currentMonitor);
//        }
//
//        private double[] robustness(PopulationModel currentModel,
//                                    PopulationState currentInitialState,
//                                    QuantitativeMonitor<PopulationState>[] currentMonitors){
//            double[] robustnessVector = new double[currentMonitors.length];
//            for (int i = 0; i < currentMonitors.length; i++) {
//                robustnessVector[i] = robustness(currentModel,currentInitialState,currentMonitors[i]);
//            }
//            return robustnessVector;
//        }
//
//        private double[] robustness(PopulationModelDefinition currentModelDefinition,
//                                  PopulationState currentInitialState,
//                                  QuantitativeMonitor<PopulationState>[] currentMonitors,
//                                  Map<String,Double> parameterization){
//            double[] robustnessVector = new double[currentMonitors.length];
//            for (int i = 0; i < currentMonitors.length; i++) {
//                robustnessVector[i] = robustness(currentModelDefinition,currentInitialState,currentMonitors[i],parameterization);
//            }
//            return robustnessVector;
//        }
//
//
//
//
//
//
//    }
//
//
//    private static class Tuning{
//
//        final private SimulationEnvironment se;
//        final private RandomGenerator rg;
//        final private PopulationModelDefinition pmd;
//        PopulationState initialState;
//        private final QualitativeMonitor<PopulationState> qualitativeMonitor[];
//        private PopulationModel pm;
//        private int numberOfTrajectories;
//
//        public Tuning(String modelDefinition,
//                      String[] formulaDefinitions,
//                      PopulationState initialState,
//                      Map<String, ToDoubleFunction<PopulationState>> measureMapFunction,
//                      String[] formulaNames,
//                      double[] formulaParameter,
//                      int numberOfTrajectories) throws ModelGenerationException, StlModelGenerationException {
//            this.numberOfTrajectories = numberOfTrajectories;
//            this.initialState = initialState;
//            this.se = new SimulationEnvironment();
//            this.rg = new DefaultRandomGenerator();
//            PopulationModelGenerator pmg = new PopulationModelGenerator(modelDefinition);
//            this.pmd = pmg.getPopulationModelDefinition();
//            this.pm = pmd.createModel();
//            this.qualitativeMonitor = new QualitativeMonitor[formulaDefinitions.length];
//            for (int i = 0; i < formulaDefinitions.length; i++) {
//                StlLoader loader = new StlLoader(formulaDefinitions[i]);
//                StlMonitorFactory<PopulationState> monitorFactory = loader.getModelFactory(measureMapFunction);
//                this.qualitativeMonitor[i] = monitorFactory.getQualitativeMonitor(formulaNames[i], formulaParameter);
//            }
//        }
//    }
//
//    /**
//     * This test case verify the effectiveness of optimization over a SIR epidemiological model.
//     * The case model and condition referred in this test, can be found
//     * at <a href="https://arxiv.org/pdf/1402.1450.pdf">the case</a>.
//     * The parameter ranges considered are:
//     * k_i (rate of infection) - between 0.005 and 0.3
//     * k_r (rate of recovery) - between 0.005 and 0.2
//     * The test aims to find the maximum or optimal values for these parameters. In this case,
//     * the optimal values found are:
//     * max -> 0.35
//     * k_i -> 0.25
//     * k_r -> 0.05
//     * The test employs the Sibilla probabilistic model checker, which generates a surrogate model using random forest.
//     * It then uses the Particle Swarm Optimization (PSO) technique to find the global maximum
//     * within the parameter space (HyperRectangle).
//     * The test validates the resulting maximum values against the expected optimal parameters,
//     * asserting that the difference is within a specified tolerance.
//     *
//     * @throws StlModelGenerationException if there are issues in generating the STL model
//     * @throws ModelGenerationException if there are issues in creating the SIR model or the surrogate model
//     */
//    @Test
//    public void testSirCase() throws StlModelGenerationException, ModelGenerationException{
//        String formula =  """
//                measure #I
//                formula formula_id [] : ( \\E[100,120][ #I == 0] )&& (\\G[0,100][ #I > 0 ]) endformula
//                """;
//
//        String sirModel = """
//                param k_i = 0.05;
//                param k_r = 0.05;
//
//                species S;
//                species I;
//                species R;
//
//                rule infection {
//                    S|I -[ #S * %I * k_i ]-> I|I
//                }
//
//                rule recovered {
//                    I -[ #I * k_r ]-> R
//                }
//                """;
//
//        PopulationState initialState = new PopulationState(new int[]{10, 90, 0});
//
//        Map<String, ToDoubleFunction<PopulationState>> measuresMapping = new HashMap<>();
//        measuresMapping.put("#S", s -> s.getOccupancy(0));
//        measuresMapping.put("#I", s -> s.getOccupancy(1));
//        measuresMapping.put("#R", s -> s.getOccupancy(2));
//
//        LPMModelOld sirModelTester = new LPMModelOld(sirModel,
//                formula, initialState, measuresMapping, "formula_id");
//        HyperRectangle hr = new HyperRectangle(
//                new ContinuousInterval("k_i",0.005,0.3),
//                new ContinuousInterval("k_r",0.005,0.2));
//
//
//        ToDoubleFunction<Map<String,Double>> function = sirModelTester.getProbReachFormulaAtTime0Function();
//
//        SurrogateFactory surrogateFactory = new RandomForest();
//        SurrogateModel randomForestModel = surrogateFactory.getSurrogateModel(
//                function,
//                new LatinHyperCubeSamplingTask(),
//                hr,1000,0.95,
//                new Properties());
//
//        ToDoubleFunction<Map<String,Double>> surrogateFunction = randomForestModel.getSurrogateFunction(true);
//
//        Map<String,Double> maximizingValues = new PSOTask().maximize(surrogateFunction,hr);
//        System.out.println(maximizingValues);
//
//        assertEquals(maximizingValues.get("k_i"),0.25,0.1);
//        assertEquals(maximizingValues.get("k_r"),0.05,0.1);
//    }
//
//
//    @Test
//    public void testVanilla() throws StlModelGenerationException,ModelGenerationException{
//
//        // STABILITY : majority or consensus are constants over time
//        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//        // majority  : %RED >= 1.0 || %BLUE >= 1.0
//        String formulaStability = """
//                measure %RED
//                measure %BLUE
//                formula formula_stability [] : \\E[0,100][ %RED >= 1.0 ]  || \\E[0,100][ %BLUE >= 1.0 ]   endformula
//                """;
//
//        // formula formula_stability [] :  || ( %RED >= 0.5 && %BLUE >= 0.5 ) ] endformula
//
//        // COHERENCE : if one faction is bigger than the other, it wins
//        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//        // bigger red  : ((%RED>%BLUE)->(\E[0,100][%RED>=1.0]))
//        // AND
//        // bigger blue : ((%BLUE>%RED)->(\E[0,100][%BLUE>=1.0]))
//
//        String formulaCoherence = """
//                measure %RED
//                measure %BLUE
//                formula formula_coherence [] : ([%RED>%BLUE]->(\\E[0,100][%RED>=1.0])) && ([%BLUE>%RED]->(\\E[0,100][%BLUE>=1.0])) endformula
//                """;
//
//        String tspModel = """
//                species RED;
//                species BLUE;
//                species UNC;
//
//                const meetRate = 1.0;
//
//                rule UNC_to_BLUE {
//                    UNC|BLUE -[ #UNC * meetRate * %BLUE ]-> BLUE|BLUE
//                }
//
//                rule UNC_to_RED {
//                    UNC|RED -[ #UNC * meetRate * %RED ]-> RED|RED
//                }
//
//                rule BLUE_to_UNC {
//                    BLUE|RED -[ #BLUE * meetRate * %RED ]-> UNC|RED
//                }
//
//                rule RED_to_UNC {
//                    RED|BLUE -[ #RED * meetRate * %BLUE ]-> UNC|BLUE
//                }
//
//                measure redPercentage = %RED;
//                measure bluePercentage = %BLUE;
//                measure uncPercentage = %UNC;
//
//                measure redQuantity = #RED;
//                measure blueQuantity  = #BLUE;
//                measure uncQuantity  = #UNC;
//
//
//                param scale = 100.0;
//
//                system configuration_common = RED<100>|BLUE<100>|UNC<800>;
//
//                system configuration_1 = RED<1*scale>|BLUE<1*scale>|UNC<8*scale>;
//                system configuration_2 = RED<100>|BLUE<100>|UNC<100>;
//                """;
//
//        String tspCross = """
//                  const N = 3;       /* Width of the Grid */
//                  const M = 3;       /* Length of the Grid */
//
//                  species RED of [0,N]*[0,M];
//                  species BLUE of [0,N]*[0,M];
//                  species UNC of [0,N]*[0,M];
//
//                  param on_site_persuasion = 1.0;
//                  param adjacent_persuasion = 0.5 ;
//
//                  param red_00 = 1.0;
//                  param red_01 = 1.0;
//                  param red_02 = 1.0;
//                  param red_10 = 1.0;
//                  param red_11 = 1.0;
//                  param red_12 = 1.0;
//                  param red_20 = 1.0;
//                  param red_21 = 1.0;
//                  param red_22 = 1.0;
//                  param blue_00 = 1.0;
//                  param blue_01 = 1.0;
//                  param blue_02 = 1.0;
//                  param blue_10 = 1.0;
//                  param blue_11 = 1.0;
//                  param blue_12 = 1.0;
//                  param blue_20 = 1.0;
//                  param blue_21 = 1.0;
//                  param blue_22 = 1.0;
//
//
//
//                  rule on_site_UNC_to_BLUE for i in [0,N] and j in [0,M] {
//                    UNC[i,j]|BLUE[i,j] -[ #UNC[i,j] * on_site_persuasion * %BLUE[i,j] ]-> BLUE[i,j]|BLUE[i,j]
//                  }
//
//                  rule on_site_UNC_to_RED for i in [0,N] and j in [0,M] {
//                    UNC[i,j]|RED[i,j] -[ #UNC[i,j] * on_site_persuasion * %RED[i,j] ]-> RED[i,j]|RED[i,j]
//                  }
//
//
//
//                  rule up_site_UNC_to_BLUE for i in [0,N] and j in [0,M] when j<M-1 {
//                    UNC[i,j+1]|BLUE[i,j] -[ #UNC[i,j+1] * adjacent_persuasion * %BLUE[i,j] ]-> BLUE[i,j+1]|BLUE[i,j]
//                  }
//
//                  rule down_site_UNC_to_BLUE for i in [0,N] and j in [0,M] when j>0 {
//                    UNC[i,j-11]|BLUE[i,j] -[ #UNC[i,j-1] * adjacent_persuasion * %BLUE[i,j] ]-> BLUE[i,j-1]|BLUE[i,j]
//                  }
//
//                  rule right_site_UNC_to_BLUE for i in [0,N] and j in [0,M] when i<N-1 {
//                    UNC[i+1,j]|BLUE[i,j] -[ #UNC[i+1,j] * adjacent_persuasion * %BLUE[i,j] ]-> BLUE[i+1,j]|BLUE[i,j]
//                  }
//
//                  rule left_site_UNC_to_BLUE for i in [0,N] and j in [0,M] when i>0 {
//                    UNC[i-1,j]|BLUE[i,j] -[ #UNC[i-1,j] * adjacent_persuasion * %BLUE[i,j] ]-> BLUE[i-1,j]|BLUE[i,j]
//                  }
//
//
//
//                  rule up_site_UNC_to_RED for i in [0,N] and j in [0,M] when j<M-1 {
//                    UNC[i,j+1]|RED[i,j] -[ #UNC[i,j+1] * adjacent_persuasion * %RED[i,j] ]-> RED[i,j+1]|RED[i,j]
//                  }
//
//                  rule down_site_UNC_to_RED for i in [0,N] and j in [0,M] when j>0 {
//                    UNC[i,j-11]|RED[i,j] -[ #UNC[i,j-1] * adjacent_persuasion * %RED[i,j] ]-> RED[i,j-1]|RED[i,j]
//                  }
//
//                  rule right_site_UNC_to_RED for i in [0,N] and j in [0,M] when i<N-1 {
//                    UNC[i+1,j]|RED[i,j] -[ #UNC[i+1,j] * adjacent_persuasion * %RED[i,j] ]-> RED[i+1,j]|RED[i,j]
//                  }
//
//                  rule left_site_UNC_to_RED for i in [0,N] and j in [0,M] when i>0 {
//                    UNC[i-1,j]|RED[i,j] -[ #UNC[i-1,j] * adjacent_persuasion * %RED[i,j] ]-> RED[i-1,j]|RED[i,j]
//                  }
//
//
//
//                  rule on_site_BLUE_to_UNC for i in [0,N] and j in [0,M] {
//                    BLUE[i,j]|RED[i,j] -[ #BLUE[i,j] * on_site_persuasion * %RED[i,j] ]-> UNC[i,j]|RED[i,j]
//                  }
//
//                  rule on_site_RED_to_UNC for i in [0,N] and j in [0,M] {
//                    RED[i,j]|BLUE[i,j] -[ #RED[i,j] * on_site_persuasion * %BLUE[i,j] ]-> UNC[i,j]|BLUE[i,j]
//                  }
//
//
//
//
//
//                  rule up_site_BLUE_to_UNC for i in [0,N] and j in [0,M] when j<M-1{
//                    BLUE[i,j]|RED[i,j+1] -[ #BLUE[i,j] * adjacent_persuasion * %RED[i,j+1] ]-> UNC[i,j]|RED[i,j+1]
//                  }
//
//                  rule down_site_BLUE_to_UNC for i in [0,N] and j in [0,M] when j>0{
//                    BLUE[i,j]|RED[i,j-1] -[ #BLUE[i,j] * adjacent_persuasion * %RED[i,j+1] ]-> UNC[i,j]|RED[i,j+1]
//                  }
//
//                  rule right_site_BLUE_to_UNC for i in [0,N] and j in [0,M] when i<N-1{
//                    BLUE[i,j]|RED[i+1,j] -[ #BLUE[i,j] * adjacent_persuasion * %RED[i+1,j] ]-> UNC[i,j]|RED[i+1,j]
//                  }
//
//                  rule left_site_BLUE_to_UNC for i in [0,N] and j in [0,M] when i>0{
//                    BLUE[i,j]|RED[i-1,j] -[ #BLUE[i,j] * adjacent_persuasion * %RED[i-1,j] ]-> UNC[i,j]|RED[i-1,j]
//                  }
//
//
//
//                  rule up_site_RED_to_UNC for i in [0,N] and j in [0,M] when j<M-1{
//                    RED[i,j]|BLUE[i,j+1] -[ #RED[i,j] * adjacent_persuasion * %BLUE[i,j+1] ]-> UNC[i,j]|BLUE[i,j+1]
//                  }
//
//                  rule down_site_RED_to_UNC for i in [0,N] and j in [0,M] when j>0{
//                    RED[i,j]|BLUE[i,j-1] -[ #RED[i,j] * adjacent_persuasion * %BLUE[i,j+1] ]-> UNC[i,j]|BLUE[i,j+1]
//                  }
//
//                  rule right_site_RED_to_UNC for i in [0,N] and j in [0,M] when i<N-1{
//                    RED[i,j]|BLUE[i+1,j] -[ #RED[i,j] * adjacent_persuasion * %BLUE[i+1,j] ]-> UNC[i,j]|BLUE[i+1,j]
//                  }
//
//                  rule left_site_RED_to_UNC for i in [0,N] and j in [0,M] when i>0{
//                    RED[i,j]|BLUE[i-1,j] -[ #RED[i,j] * adjacent_persuasion * %BLUE[i-1,j] ]-> UNC[i,j]|BLUE[i-1,j]
//                  }
//
//
//                  measure redPercentage = %RED[0,0] + %RED[0,1] + %RED[0,2] + %RED[1,0] + %RED[1,1] + %RED[1,2] + %RED[2,0] + %RED[2,1] + %RED[2,2];
//                  measure bluePercentage = %BLUE[0,0] + %BLUE[0,1] + %BLUE[0,2] + %BLUE[1,0] + %BLUE[1,1] + %BLUE[1,2] + %BLUE[2,0] + %BLUE[2,1] + %BLUE[2,2];
//                  measure uncPercentage = %UNC[0,0] + %UNC[0,1] + %UNC[0,2] + %UNC[1,0] + %UNC[1,1] + %UNC[1,2] + %UNC[2,0] + %UNC[2,1] + %UNC[2,2];
//
//                  measure redQuantity = #RED[0,0] + #RED[0,1] + #RED[0,2] + #RED[1,0] + #RED[1,1] + #RED[1,2] + #RED[2,0] + #RED[2,1] + #RED[2,2];
//                  measure blueQuantity  = #BLUE[0,0] + #BLUE[0,1] + #BLUE[0,2] + #BLUE[1,0] + #BLUE[1,1] + #BLUE[1,2] + #BLUE[2,0] + #BLUE[2,1] + #BLUE[2,2];
//                  measure uncQuantity  = #UNC[0,0] + #UNC[0,1] + #UNC[0,2] + #UNC[1,0] + #UNC[1,1] + #UNC[1,2] + #UNC[2,0] + #UNC[2,1] + #UNC[2,2];
//
//                  system initial = RED[0,0]<1*red_00>|RED[0,1]<1*red_01>|RED[0,2]<1*red_02>|RED[1,0]<1*red_10>|RED[1,1]<1*red_11>|RED[1,2]<1*red_12>|RED[2,0]<1*red_20>|RED[2,1]<1*red_21>|RED[2,2]<1*red_22>|BLUE[0,0]<1*blue_00>|BLUE[0,1]<1*blue_01>|BLUE[0,2]<1*blue_02>|BLUE[1,0]<1*blue_10>|BLUE[1,1]<1*blue_11>|BLUE[1,2]<1*blue_12>|BLUE[2,0]<1*blue_20>|BLUE[2,1]<1*blue_21>|BLUE[2,2]<1*blue_22>;
//
//                  system initial_cross = RED[0,1]<1*red_01>|RED[1,0]<1*red_10>|RED[1,1]<1*red_11>|RED[1,2]<1*red_12>|RED[2,1]<1*red_21>|BLUE[0,1]<1*blue_01>|BLUE[1,0]<1*blue_10>|BLUE[1,1]<1*blue_11>|BLUE[1,2]<1*blue_12>|BLUE[2,1]<1*blue_21>;
//
//                """;
//
//        LPMScreening screening = new LPMScreening(tspCross);
//
//        PopulationState tspState = new PopulationState(new int[]{10, 10, 80});
//        PopulationState tspCrossState = new PopulationState(new int[]{
//                0,   // RED[0,0]
//                1,   // RED[1,0]
//                0,   // RED[2,0]
//                3,   // RED[0,1]
//                0,   // RED[1,1]
//                5,   // RED[2,1]
//                0,   // RED[0,2]
//                1,   // RED[1,2]
//                0,   // RED[2,2]
//                0,   // BLUE[0,0]
//                2,   // BLUE[1,0]
//                0,   // BLUE[2,0]
//                0,   // BLUE[0,1]
//                0,   // BLUE[1,1]
//                5,   // BLUE[2,1]
//                0,   // BLUE[0,2]
//                3,   // BLUE[1,2]
//                0,   // BLUE[2,2]
//                0,   // UNC[0,0]
//                16,  // UNC[1,0]
//                0,   // UNC[2,0]
//                16,  // UNC[0,1]
//                16,  // UNC[1,1]
//                16,  // UNC[2,1]
//                0,   // UNC[0,2]
//                16,  // UNC[1,2]
//                0    // UNC[2,2]
//        });
//
//
//        String[] formulae = new String[]{formulaStability,formulaCoherence};
//        String[] formulaeNames = new String[]{"formula_stability","formula_coherence"};
//
//        Map<String, ToDoubleFunction<PopulationState>> measuresMappingTSP = new HashMap<>();
//        measuresMappingTSP.put("%RED", s -> s.getFraction(0));
//        measuresMappingTSP.put("%BLUE", s -> s.getFraction(1));
//        measuresMappingTSP.put("%UNC", s -> s.getFraction(2));
//
//        Map<String, ToDoubleFunction<PopulationState>> measuresMappingTSPCross = new HashMap<>();
//        measuresMappingTSP.put("%RED", s -> s.getFraction(3,1,4,7,5));
//        measuresMappingTSP.put("%BLUE", s -> s.getFraction(12,10,13,16,14));
//
//        Tuning tuning = new Tuning(tspModel,formulae,tspState,measuresMappingTSP,formulaeNames,new double[]{},100);
//
//    }
//
//
//    @Test
//    public void testRumorSpreadingCase() throws StlModelGenerationException, ModelGenerationException, IOException {
//
//        String formula= """
//                measure #S
//                measure #I
//                formula formula_id [] : ( \\G[3,5]( [ #I > 0 ] ) )  && ( \\E[0,1] ( \\G[0,0.02] ( [#S >  50 ] ) ) )  endformula
//                """;
//
//
////        String rumorSpreadModel = """
////                param k_s = 0.05;
////                param k_r = 0.05;
////
////                species S;
////                species I;
////                species B;
////
////                rule spreading {
////                	S|I -[ k_s * #S * #I ]-> S|S
////                }
////
////                rule stop_spreading1 {
////                	S -[ k_r * (#S-1)* #S ]->  B
////                }
////
////                rule stop_spreading2 {
////                	S -[ k_r * #S * #B ]->  B
////                }
////                """;
//
//
//        String rumorSpreadModel = """
//
//                param scale = 15;
//
//                param k_s = 0.05;
//                param k_r = 0.05;
//
//                species S;
//                species I;
//                species B;
//
//
//                rule spreading {
//                    S|I -[ #S * #I * k_s / scale]-> S|S
//                }
//
//                rule stop_spreading_1 {
//                    S|S -[ #S * (#S - 1) * k_r /scale ]-> S|B
//                }
//
//                rule stop_spreading_2 {
//                    S|B -[ #B * #S * k_r /scale ]-> B|B
//                }
//                """;
//
//
//        PopulationState initialState = new PopulationState(new int[]{10, 90, 0});
//
//        Map<String, ToDoubleFunction<PopulationState>> measuresMapping = new HashMap<>();
//        measuresMapping.put("#S", s -> s.getOccupancy(0));
//        measuresMapping.put("#I", s -> s.getOccupancy(1));
//        measuresMapping.put("#B", s -> s.getOccupancy(2));
//
//
//
//        LPMModelOld rsModelTester = new LPMModelOld(rumorSpreadModel,
//                formula, initialState, measuresMapping, "formula_id");
//
//        HyperRectangle hr = new HyperRectangle(
//                new ContinuousInterval("k_s",0.0001,2.0),
//                new ContinuousInterval("k_r",0.0001,0.5));
//
//        DataSet dataSetProbReach = new DataSet(hr, new FullFactorialSamplingTask(),50, rsModelTester.getProbReachFormulaAtTime0Function());
//
//        //storeCSV(dataSetProbReach,"/Users/lorenzomatteucci/Documents/SibillaTestFolder", "testRS_prob_functioning");
//    }
//
//
//    @Test
//    public void testSIRCaseDataset() throws StlModelGenerationException, ModelGenerationException, IOException {
//
//        String formula= """
//                measure #I
//                formula formula_id [] : ( \\E[100,120][ #I == 0] )&& (\\G[0,100][ #I > 0 ]) endformula
//                """;
//
//
//        String sirModel = """
//                param k_i = 0.05;
//                param k_r = 0.05;
//
//                species S;
//                species I;
//                species R;
//
//                rule infection {
//                    S|I -[ #S * %I * k_i ]-> I|I
//                }
//
//                rule recovered {
//                    I -[ #I * k_r ]-> R
//                }
//                """;
//
//
//        PopulationState initialState = new PopulationState(new int[]{90, 10, 0});
//
//        Map<String, ToDoubleFunction<PopulationState>> measuresMapping = new HashMap<>();
//        measuresMapping.put("#S", s -> s.getOccupancy(0));
//        measuresMapping.put("#I", s -> s.getOccupancy(1));
//        measuresMapping.put("#R", s -> s.getOccupancy(2));
//
//
//        LPMModelOld sirModelTester = new LPMModelOld(sirModel,
//                formula, initialState, measuresMapping, "formula_id");
//        HyperRectangle hr = new HyperRectangle(
//                new ContinuousInterval("k_i",0.005,0.3),
//                new ContinuousInterval("k_r",0.005,0.2));
//
//        DataSet dataSetProbReach = new DataSet(hr, new FullFactorialSamplingTask(),20, sirModelTester.getProbReachFormulaAtTime0Function());
//
//        storeCSV(dataSetProbReach,"/Users/lorenzomatteucci/phd/datasetForPlotting", "extensive_SIR_prob_2");
//    }
//
//
//    @Test
//    public void testTSP() throws ModelGenerationException {
//
//
//        String tspModel = """
//                    const N = 3;       /* Width of the Grid */
//                    const M = 3;       /* Length of the Grid */
//
//                    species RED of [0,N]*[0,M];
//                    species BLUE of [0,N]*[0,M];
//                    species UNC of [0,N]*[0,M];
//
//                    param on_site_persuasion = 1.0;
//                    param adjacent_persuasion = 0.5 ;
//
//                    param red_00 = 1.0;
//                    param red_01 = 1.0;
//                    param red_02 = 1.0;
//                    param red_10 = 1.0;
//                    param red_11 = 1.0;
//                    param red_12 = 1.0;
//                    param red_20 = 1.0;
//                    param red_21 = 1.0;
//                    param red_22 = 1.0;
//                    param blue_00 = 1.0;
//                    param blue_01 = 1.0;
//                    param blue_02 = 1.0;
//                    param blue_10 = 1.0;
//                    param blue_11 = 1.0;
//                    param blue_12 = 1.0;
//                    param blue_20 = 1.0;
//                    param blue_21 = 1.0;
//                    param blue_22 = 1.0;
//
//                    rule on_site_UNC_to_BLUE for i in [0,N] and j in [0,M] {
//                      UNC[i,j]|BLUE[i,j] -[ #UNC[i,j] * on_site_persuasion * %BLUE[i,j] ]-> BLUE[i,j]|BLUE[i,j]
//                    }
//
//                    rule on_site_UNC_to_RED for i in [0,N] and j in [0,M] {
//                      UNC[i,j]|RED[i,j] -[ #UNC[i,j] * on_site_persuasion * %RED[i,j] ]-> RED[i,j]|RED[i,j]
//                    }
//
//
//
//                    rule up_site_UNC_to_BLUE for i in [0,N] and j in [0,M] when j<M-1 {
//                      UNC[i,j+1]|BLUE[i,j] -[ #UNC[i,j+1] * adjacent_persuasion * %BLUE[i,j] ]-> BLUE[i,j+1]|BLUE[i,j]
//                    }
//
//                    rule down_site_UNC_to_BLUE for i in [0,N] and j in [0,M] when j>0 {
//                      UNC[i,j-11]|BLUE[i,j] -[ #UNC[i,j-1] * adjacent_persuasion * %BLUE[i,j] ]-> BLUE[i,j-1]|BLUE[i,j]
//                    }
//
//                    rule right_site_UNC_to_BLUE for i in [0,N] and j in [0,M] when i<N-1 {
//                      UNC[i+1,j]|BLUE[i,j] -[ #UNC[i+1,j] * adjacent_persuasion * %BLUE[i,j] ]-> BLUE[i+1,j]|BLUE[i,j]
//                    }
//
//                    rule left_site_UNC_to_BLUE for i in [0,N] and j in [0,M] when i>0 {
//                      UNC[i-1,j]|BLUE[i,j] -[ #UNC[i-1,j] * adjacent_persuasion * %BLUE[i,j] ]-> BLUE[i-1,j]|BLUE[i,j]
//                    }
//
//
//
//                    rule up_site_UNC_to_RED for i in [0,N] and j in [0,M] when j<M-1 {
//                      UNC[i,j+1]|RED[i,j] -[ #UNC[i,j+1] * adjacent_persuasion * %RED[i,j] ]-> RED[i,j+1]|RED[i,j]
//                    }
//
//                    rule down_site_UNC_to_RED for i in [0,N] and j in [0,M] when j>0 {
//                      UNC[i,j-11]|RED[i,j] -[ #UNC[i,j-1] * adjacent_persuasion * %RED[i,j] ]-> RED[i,j-1]|RED[i,j]
//                    }
//
//                    rule right_site_UNC_to_RED for i in [0,N] and j in [0,M] when i<N-1 {
//                      UNC[i+1,j]|RED[i,j] -[ #UNC[i+1,j] * adjacent_persuasion * %RED[i,j] ]-> RED[i+1,j]|RED[i,j]
//                    }
//
//                    rule left_site_UNC_to_RED for i in [0,N] and j in [0,M] when i>0 {
//                      UNC[i-1,j]|RED[i,j] -[ #UNC[i-1,j] * adjacent_persuasion * %RED[i,j] ]-> RED[i-1,j]|RED[i,j]
//                    }
//
//
//
//                    rule on_site_BLUE_to_UNC for i in [0,N] and j in [0,M] {
//                      BLUE[i,j]|RED[i,j] -[ #BLUE[i,j] * on_site_persuasion * %RED[i,j] ]-> UNC[i,j]|RED[i,j]
//                    }
//
//                    rule on_site_RED_to_UNC for i in [0,N] and j in [0,M] {
//                      RED[i,j]|BLUE[i,j] -[ #RED[i,j] * on_site_persuasion * %BLUE[i,j] ]-> UNC[i,j]|BLUE[i,j]
//                    }
//
//
//
//
//
//                    rule up_site_BLUE_to_UNC for i in [0,N] and j in [0,M] when j<M-1{
//                      BLUE[i,j]|RED[i,j+1] -[ #BLUE[i,j] * adjacent_persuasion * %RED[i,j+1] ]-> UNC[i,j]|RED[i,j+1]
//                    }
//
//                    rule down_site_BLUE_to_UNC for i in [0,N] and j in [0,M] when j>0{
//                      BLUE[i,j]|RED[i,j-1] -[ #BLUE[i,j] * adjacent_persuasion * %RED[i,j+1] ]-> UNC[i,j]|RED[i,j+1]
//                    }
//
//                    rule right_site_BLUE_to_UNC for i in [0,N] and j in [0,M] when i<N-1{
//                      BLUE[i,j]|RED[i+1,j] -[ #BLUE[i,j] * adjacent_persuasion * %RED[i+1,j] ]-> UNC[i,j]|RED[i+1,j]
//                    }
//
//                    rule left_site_BLUE_to_UNC for i in [0,N] and j in [0,M] when i>0{
//                      BLUE[i,j]|RED[i-1,j] -[ #BLUE[i,j] * adjacent_persuasion * %RED[i-1,j] ]-> UNC[i,j]|RED[i-1,j]
//                    }
//
//
//
//                    rule up_site_RED_to_UNC for i in [0,N] and j in [0,M] when j<M-1{
//                      RED[i,j]|BLUE[i,j+1] -[ #RED[i,j] * adjacent_persuasion * %BLUE[i,j+1] ]-> UNC[i,j]|BLUE[i,j+1]
//                    }
//
//                    rule down_site_RED_to_UNC for i in [0,N] and j in [0,M] when j>0{
//                      RED[i,j]|BLUE[i,j-1] -[ #RED[i,j] * adjacent_persuasion * %BLUE[i,j+1] ]-> UNC[i,j]|BLUE[i,j+1]
//                    }
//
//                    rule right_site_RED_to_UNC for i in [0,N] and j in [0,M] when i<N-1{
//                      RED[i,j]|BLUE[i+1,j] -[ #RED[i,j] * adjacent_persuasion * %BLUE[i+1,j] ]-> UNC[i,j]|BLUE[i+1,j]
//                    }
//
//                    rule left_site_RED_to_UNC for i in [0,N] and j in [0,M] when i>0{
//                      RED[i,j]|BLUE[i-1,j] -[ #RED[i,j] * adjacent_persuasion * %BLUE[i-1,j] ]-> UNC[i,j]|BLUE[i-1,j]
//                    }
//
//
//                    measure redPercentage = %RED[0,0] + %RED[0,1] + %RED[0,2] + %RED[1,0] + %RED[1,1] + %RED[1,2] + %RED[2,0] + %RED[2,1] + %RED[2,2];
//                    measure bluePercentage = %BLUE[0,0] + %BLUE[0,1] + %BLUE[0,2] + %BLUE[1,0] + %BLUE[1,1] + %BLUE[1,2] + %BLUE[2,0] + %BLUE[2,1] + %BLUE[2,2];
//                    measure uncPercentage = %UNC[0,0] + %UNC[0,1] + %UNC[0,2] + %UNC[1,0] + %UNC[1,1] + %UNC[1,2] + %UNC[2,0] + %UNC[2,1] + %UNC[2,2];
//
//                    measure redQuantity = #RED[0,0] + #RED[0,1] + #RED[0,2] + #RED[1,0] + #RED[1,1] + #RED[1,2] + #RED[2,0] + #RED[2,1] + #RED[2,2];
//                    measure blueQuantity  = #BLUE[0,0] + #BLUE[0,1] + #BLUE[0,2] + #BLUE[1,0] + #BLUE[1,1] + #BLUE[1,2] + #BLUE[2,0] + #BLUE[2,1] + #BLUE[2,2];
//                    measure uncQuantity  = #UNC[0,0] + #UNC[0,1] + #UNC[0,2] + #UNC[1,0] + #UNC[1,1] + #UNC[1,2] + #UNC[2,0] + #UNC[2,1] + #UNC[2,2];
//
//                    system initial = RED[0,0]<1*red_00>|RED[0,1]<1*red_01>|RED[0,2]<1*red_02>|RED[1,0]<1*red_10>|RED[1,1]<1*red_11>|RED[1,2]<1*red_12>|RED[2,0]<1*red_20>|RED[2,1]<1*red_21>|RED[2,2]<1*red_22>|BLUE[0,0]<1*blue_00>|BLUE[0,1]<1*blue_01>|BLUE[0,2]<1*blue_02>|BLUE[1,0]<1*blue_10>|BLUE[1,1]<1*blue_11>|BLUE[1,2]<1*blue_12>|BLUE[2,0]<1*blue_20>|BLUE[2,1]<1*blue_21>|BLUE[2,2]<1*blue_22>;
//
//                """;
//
//        PopulationModelGenerator pmg = new PopulationModelGenerator(tspModel);
//        PopulationModelDefinition pmd = null;
//        try {
//            pmd = pmg.getPopulationModelDefinition();
//        } catch (ModelGenerationException e) {
//            throw new RuntimeException(e);
//        }
//        PopulationModel pm = pmd.createModel();
//        String[] measures = pm.measures();
//        System.out.println(Arrays.toString(measures));
//    }

}

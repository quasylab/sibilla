package it.unicam.quasylab.sibilla.core.runtime;

import it.unicam.quasylab.sibilla.core.models.pm.PopulationModel;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationModelDefinition;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.pso.PSOTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.FullFactorialSamplingTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.LatinHyperCubeSamplingTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.*;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.core.tools.stl.QualitativeMonitor;
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.langs.pm.ModelGenerationException;
import it.unicam.quasylab.sibilla.langs.pm.PopulationModelGenerator;
import it.unicam.quasylab.sibilla.langs.stl.StlLoader;
import it.unicam.quasylab.sibilla.langs.stl.StlModelGenerationException;
import it.unicam.quasylab.sibilla.langs.stl.StlMonitorFactory;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OptimalFeasibilityTest {

    private class LPMModel{
        private final String modelSpecification;
        private final String formulaSpecification;
        private final String formulaName;
        private final double[] formulaParameter;
        private final PopulationState initialPopulationState;
        private final Map<String, ToDoubleFunction<PopulationState>> measuresMap;
        private final int numberOfTrajectories;

        public LPMModel(String modelSpecification,
                        String formulaSpecification,
                        String formulaName,
                        PopulationState initialPopulationState,
                        Map<String, ToDoubleFunction<PopulationState>> measuresMap,
                        int numberOfTrajectories) {
            this.modelSpecification = modelSpecification;
            this.formulaSpecification = formulaSpecification;
            this.formulaName = formulaName;
            this.initialPopulationState = initialPopulationState;
            this.measuresMap = measuresMap;
            this.numberOfTrajectories = numberOfTrajectories;
            this.formulaParameter = new double[]{};
        }

        public LPMModel(String modelSpecification,
                        String formulaSpecification,
                        String formulaName,
                        PopulationState initialPopulationState,
                        Map<String, ToDoubleFunction<PopulationState>> measuresMap,
                        int numberOfTrajectories,
                        double[] formulaParameter) {
            this.modelSpecification = modelSpecification;
            this.formulaSpecification = formulaSpecification;
            this.formulaName = formulaName;
            this.initialPopulationState = initialPopulationState;
            this.measuresMap = measuresMap;
            this.numberOfTrajectories = numberOfTrajectories;
            this.formulaParameter = formulaParameter;
        }

        private PopulationModelGenerator getPopulationModelGenerator(String modelSpecification){
            return new PopulationModelGenerator(modelSpecification);
        }

        private PopulationModelDefinition getPopulationModelDefinition(PopulationModelGenerator pmg) throws ModelGenerationException {
            return pmg.getPopulationModelDefinition();
        }

        private PopulationModel getPopulationModel(PopulationModelDefinition pmd, Map<String,Double> parameters){
            for (String key : parameters.keySet()){
                pmd.setParameter(key,new SibillaDouble(parameters.get(key)));
            }
            return pmd.createModel();
        }

        private StlMonitorFactory<PopulationState> getMonitorFactory(String formulaSpecification, Map<String, ToDoubleFunction<PopulationState>> measuresMap ) throws StlModelGenerationException {
            StlLoader loader = new StlLoader(formulaSpecification);
            return loader.getModelFactory(measuresMap);
        }
        private QualitativeMonitor<PopulationState> getQualitativeMonitor(StlMonitorFactory<PopulationState> monitorFactory, String formulaName, double[] formulaParameter){
            return monitorFactory.getQualitativeMonitor(formulaName,formulaParameter);
        }

        private Trajectory<PopulationState> sampleTrajectory(SimulationEnvironment se, RandomGenerator rg, PopulationModelDefinition pmd, PopulationState initialState, double deadline, Map<String,Double> parameters){
            PopulationModel pm = getPopulationModel(pmd,parameters);
            Trajectory<PopulationState> trajectory= se.sampleTrajectory(
                    rg,
                    pm,
                    initialState,
                    deadline);
            trajectory.setEnd(deadline);
            return trajectory;
        }

        private double[] probReachFormula(QualitativeMonitor<PopulationState> qm,PopulationModelDefinition pmd,PopulationState initialState, int numberOfTrajectories,Map<String,Double> parameters){
            SimulationEnvironment se = new SimulationEnvironment();
            RandomGenerator rg = new DefaultRandomGenerator();
            Supplier<Trajectory<PopulationState>> trjSup = ()-> sampleTrajectory(se,rg,pmd,initialState,qm.getTimeHorizon(),parameters);
            return QualitativeMonitor
                    .computeProbability(
                            qm,
                            trjSup,
                            numberOfTrajectories,
                            new double[]{0.0}
                    );
        }

        public double probReachAtTimeZero(Map<String,Double> parameters) throws StlModelGenerationException, ModelGenerationException {
            QualitativeMonitor<PopulationState> qm = this.getQualitativeMonitor(this.getMonitorFactory(this.formulaSpecification,this.measuresMap),
                    this.formulaName,
                    this.formulaParameter);

            PopulationModelDefinition pmd = this.getPopulationModelDefinition(this.getPopulationModelGenerator(this.modelSpecification));

            return probReachFormula(qm,pmd,this.initialPopulationState,this.numberOfTrajectories,parameters)[0];
        }

        public ToDoubleFunction<Map<String,Double>> probReachAtTimeZeroFunction(){
            return m -> {
                try {
                    double value = this.probReachAtTimeZero(m);
                    System.out.println(value);
                    return value;
                } catch (StlModelGenerationException | ModelGenerationException e) {
                    throw new RuntimeException(e);
                }
            };


    }
    }


    /**
     * This test case verify the effectiveness of optimization over a SIR epidemiological model.
     * The case model and condition referred in this test, can be found
     * at <a href="https://arxiv.org/pdf/1402.1450.pdf">the case</a>.
     * The parameter ranges considered are:
     * k_i (rate of infection) - between 0.005 and 0.3
     * k_r (rate of recovery) - between 0.005 and 0.2
     * The test aims to find the maximum or optimal values for these parameters. In this case,
     * the optimal values found are:
     * max -> 0.35
     * k_i -> 0.25
     * k_r -> 0.05
     * The test employs the Sibilla probabilistic model checker, which generates a surrogate model using random forest.
     * It then uses the Particle Swarm Optimization (PSO) technique to find the global maximum
     * within the parameter space (HyperRectangle).
     * The test validates the resulting maximum values against the expected optimal parameters,
     * asserting that the difference is within a specified tolerance.

     */
    @Disabled
    @Test
    public void testSirCase(){
        String sirFormulaName = "formula_id";
        String sirFormulaSpecification = """
                measure #I
                formula formula_id [] : ( \\E[100,120][ #I == 0] )&& (\\G[0,100][ #I > 0 ]) endformula
                """;

        String sirModelSpecification = """
                param k_i = 0.05;
                param k_r = 0.05;
                
                species S;
                species I;
                species R;
                
                rule infection {
                    S|I -[ #S * %I * k_i ]-> I|I
                }
                
                rule recovered {
                    I -[ #I * k_r ]-> R
                }
                """;

        PopulationState initialState = new PopulationState(new int[]{90, 10, 0});

        Map<String, ToDoubleFunction<PopulationState>> sirMeasuresMapping = new HashMap<>();
        sirMeasuresMapping.put("#S", s -> s.getOccupancy(0));
        sirMeasuresMapping.put("#I", s -> s.getOccupancy(1));
        sirMeasuresMapping.put("#R", s -> s.getOccupancy(2));

        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval("k_i",0.005,0.3),
                new ContinuousInterval("k_r",0.005,0.2));

        LPMModel sirModel = new LPMModel(sirModelSpecification,
                sirFormulaSpecification,
                sirFormulaName,
                initialState,
                sirMeasuresMapping,
                100);

        ToDoubleFunction<Map<String,Double>> probReachFunction = sirModel.probReachAtTimeZeroFunction();

        SurrogateFactory surrogateFactory = new RandomForest();
        SurrogateModel randomForestModel = surrogateFactory.getSurrogateModel(
                probReachFunction,
                new LatinHyperCubeSamplingTask(),
                hr,1000,0.95,
                new Properties());

        ToDoubleFunction<Map<String,Double>> surrogateFunction = randomForestModel.getSurrogateFunction(true);

        Map<String,Double> maximizingValues = new PSOTask().maximize(surrogateFunction,hr);
        System.out.println(maximizingValues);

        assertEquals(maximizingValues.get("k_i"),0.25,0.1);
        assertEquals(maximizingValues.get("k_r"),0.05,0.1);
    }

    @Disabled
    @Test
    public void testRsCase(){
        String rsFormulaName = "formula_id";

        String rsFormulaSpecification= """
                measure #S
                measure #I
                formula formula_id [] : ( \\G[3,5]( [ #I > 0 ] ) )  && ( \\E[0,1] ( \\G[0,0.02] ( [#S > 50 ] ) ) )  endformula
                """;

        String rsModelSpecification = """
                param k_s = 0.05;
                param k_r = 0.05;

                species S; /* spreaders */
                species I; /* ignorants */
                species B; /* blockers  */


                rule spreading {
                    S|I -[ #S * #I * k_s  ]-> S|S
                }

                rule stop_spreading_1 {
                    S|S -[ #S * (#S - 1) * k_r ]-> S|B
                }

                rule stop_spreading_2 {
                    S|B -[ #B * #S * k_r  ]-> B|B
                }
                """;

        PopulationState initialState = new PopulationState(new int[]{10, 90, 0});

        Map<String, ToDoubleFunction<PopulationState>> rsMeasuresMapping = new HashMap<>();
        rsMeasuresMapping.put("#S", s -> s.getOccupancy(0));
        rsMeasuresMapping.put("#I", s -> s.getOccupancy(1));
        rsMeasuresMapping.put("#B", s -> s.getOccupancy(2));

        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval("k_s",0.0001,2),
                new ContinuousInterval("k_r",0.0001,0.5));

        LPMModel rsModel = new LPMModel(rsModelSpecification,
                rsFormulaSpecification,
                rsFormulaName,
                initialState,
                rsMeasuresMapping,
                100);

        ToDoubleFunction<Map<String,Double>> probReachFunction = rsModel.probReachAtTimeZeroFunction();

        DataSet dataSetProbReach = new DataSet(hr, new FullFactorialSamplingTask(),50, probReachFunction);

        storeCSV(dataSetProbReach,"RS_Test_");
    }

    @Disabled
    @Test
    public void testSirDatasetCSV(){
        String sirFormulaName = "formula_id";
        String sirFormulaSpecification = """
                measure #I
                formula formula_id [] : ( \\E[100,120][ #I == 0] )&& (\\G[0,100][ #I > 0 ]) endformula
                """;

        String sirModelSpecification = """
                param k_i = 0.05;
                param k_r = 0.05;
                
                species S;
                species I;
                species R;
                
                rule infection {
                    S|I -[ #S * %I * k_i ]-> I|I
                }
                
                rule recovered {
                    I -[ #I * k_r ]-> R
                }
                """;

        PopulationState initialState = new PopulationState(new int[]{90, 10, 0});

        Map<String, ToDoubleFunction<PopulationState>> sirMeasuresMapping = new HashMap<>();
        sirMeasuresMapping.put("#S", s -> s.getOccupancy(0));
        sirMeasuresMapping.put("#I", s -> s.getOccupancy(1));
        sirMeasuresMapping.put("#R", s -> s.getOccupancy(2));

        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval("k_i",0.005,0.3),
                new ContinuousInterval("k_r",0.005,0.2));

        LPMModel sirModel = new LPMModel(sirModelSpecification,
                sirFormulaSpecification,
                sirFormulaName,
                initialState,
                sirMeasuresMapping,
                100);

        ToDoubleFunction<Map<String,Double>> probReachFunction = sirModel.probReachAtTimeZeroFunction();

        DataSet dataSetProbReach = new DataSet(hr, new FullFactorialSamplingTask(),50, probReachFunction);

        storeCSV(dataSetProbReach,"SIR_Test_Big");
    }

    @Disabled
    @Test
    public void testSirSurrogateDatasetCSV(){
        String sirFormulaName = "formula_id";
        String sirFormulaSpecification = """
                measure #I
                formula formula_id [] : ( \\E[100,120][ #I == 0] )&& (\\G[0,100][ #I > 0 ]) endformula
                """;

        String sirModelSpecification = """
                param k_i = 0.05;
                param k_r = 0.05;
                
                species S;
                species I;
                species R;
                
                rule infection {
                    S|I -[ #S * %I * k_i ]-> I|I
                }
                
                rule recovered {
                    I -[ #I * k_r ]-> R
                }
                """;

        PopulationState initialState = new PopulationState(new int[]{90, 10, 0});

        Map<String, ToDoubleFunction<PopulationState>> sirMeasuresMapping = new HashMap<>();
        sirMeasuresMapping.put("#S", s -> s.getOccupancy(0));
        sirMeasuresMapping.put("#I", s -> s.getOccupancy(1));
        sirMeasuresMapping.put("#R", s -> s.getOccupancy(2));

        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval("k_i",0.005,0.3),
                new ContinuousInterval("k_r",0.005,0.2));

        LPMModel sirModel = new LPMModel(sirModelSpecification,
                sirFormulaSpecification,
                sirFormulaName,
                initialState,
                sirMeasuresMapping,
                100);

        ToDoubleFunction<Map<String,Double>> probReachFunction = sirModel.probReachAtTimeZeroFunction();

        ToDoubleFunction<Map<String,Double>> surrogateReachFunctionRFR = defaultRandomForestSurrogate(probReachFunction,hr);
        DataSet dataSetProbReachRFR = new DataSet(hr, new FullFactorialSamplingTask(),50, surrogateReachFunctionRFR);
        storeCSV(dataSetProbReachRFR,"SIR_Test_RFR_surrogate");

        ToDoubleFunction<Map<String,Double>> surrogateReachFunctionRBFN = defaultRBFNetworkSurrogate(probReachFunction,hr);
        DataSet dataSetProbReachRBFN = new DataSet(hr, new FullFactorialSamplingTask(),50, surrogateReachFunctionRBFN);
        storeCSV(dataSetProbReachRBFN,"SIR_Test_RBFN_surrogate");


        ToDoubleFunction<Map<String,Double>> surrogateReachFunctionGTB = defaultGradientTreeBoostSurrogate(probReachFunction,hr);
        DataSet dataSetProbReachGTB = new DataSet(hr, new FullFactorialSamplingTask(),50, surrogateReachFunctionGTB);
        storeCSV(dataSetProbReachGTB,"SIR_Test_GTB_surrogate");




    }

    @Disabled
    @Test
    public void testRepressillatorDataSetCSV() throws StlModelGenerationException, ModelGenerationException {
        String rFormulaName = "f_id";
        String rFormulaSpecification = """
                measure #PZ \s
                formula f_id [kT = 1500, kB = 500 , t = 100, tW = 25, tE = 10] :  \\E [0,t] ( \\G[tE,tW+tE]([#PZ > kB] && [#PZ < kT]) && \\E [0,tE][#PZ < kB]  && \\E [tW+tE,tE+tW+tE][#PZ < kB] )  endformula
                """;
        //double[] formulaParameter = new double[]{2000.0, 250, 100.0, 2.0, 20.0};
        //double[] formulaParameter = new double[]{2000.0, 250, 100.0, 10.0, 15.0};

        //double[] formulaParameter = new double[]{1500.0, 250, 100.0, 10.0, 15.0};  // promising
        //double[] formulaParameter = new double[]{1000.0, 500.0, 100.0, 10.0, 15.0};  // more promising
        //double[] formulaParameter = new double[]{800.0, 500.0 , 100.0, 10.0, 15.0};  // more promising
        double[] formulaParameter = new double[]{800.0, 500.0 , 100.0, 10.0, 20.0};  // more promising

        String rModelSpecification = """
                param n = 2;         /* Hill coefficient n */
                param KM = 40;       /* K_M*/
                param tau_mRNA = 2;  /* mRNA half life */
                param tau_prot = 10; /* protein half life */
                param ps_a = 0.5;    /* promotor strength (repressed) ( tps_repr ) */
                param ps_0 = 0.0005; /* promotor strength (full) ( tps_active ) */
                
                const ln2 = 0.69314718056;
                const beta = 0.2;
                const alpha0 = 0.2164;
                const alpha = 216.404;
                const eff = 20;
                const t_ave =  tau_mRNA / ln2;   /* average mRNA lifetime */
                const kd_mRNA = ln2 / tau_prot;  /* mRNA decay rate */
                const kd_prot = ln2 / tau_mRNA;  /* protein decay rate  */
                const k_tl = eff / t_ave;        /* translation rate  */
                const a_tr = (ps_a -ps_0)*60;    /* transcription rate  */
                const a0_tr = ps_0 * 60;         /* transcription rate (repressed)   */
                                
                const startY = 20; /* Initial number of Y agents */
                const startVOID = 1; /* Initial number of VOID agents */
                                
                species VOID;  /* To represent nothingness, fictitious species are created */
                                
                species PX; /*  protein produced by X */
                species PY; /*  protein produced by Y */
                species PZ; /*  protein produced by Z */
                species X; /*  mRNA X (LacI) */
                species Y; /*  mRNA Y (TetR) */
                species Z; /*  mRNA Z (CI)*/
                                
                                
                rule degradation_of_X_transcripts {
                    X|VOID -[ kd_mRNA * #X ]-> VOID
                }
                                
                rule degradation_of_Y_transcripts {
                    Y|VOID -[ kd_mRNA * #Y ]-> VOID
                }
                                
                rule degradation_of_Z_transcripts {
                    Z|VOID -[ kd_mRNA * #Z ]-> VOID
                }
                                
                rule translation_of_X {
                    VOID -[ k_tl * #X ]-> VOID|PX
                }
                                
                rule translation_of_Y {
                    VOID -[ k_tl * #Y ]-> VOID|PY
                }
                                
                rule translation_of_Z {
                    VOID -[ k_tl * #Z ]-> VOID|PZ
                }
                                
                rule degradation_of_X {
                    PX|VOID -[ kd_prot * #PX ]-> VOID
                }
                                
                rule degradation_of_Y {
                    PY|VOID -[ kd_prot * #PY ]-> VOID
                }
                                
                rule degradation_of_Z {
                    PZ|VOID -[ kd_prot * #PZ ]-> VOID
                }
                                
                rule transcription_of_X {
                    VOID -[ a0_tr + ( ( a_tr * KM^(n) )/( KM^n + #PZ^n ) ) ]-> VOID|X
                }
                                
                rule transcription_of_Y {
                    VOID -[ a0_tr + ( ( a_tr * KM^(n) )/( KM^n + #PX^n ) ) ]-> VOID|Y
                }
                                
                rule transcription_of_Z {
                    VOID -[ a0_tr + ( ( a_tr * KM^(n) )/( KM^n + #PY^n ) ) ]-> VOID|Z
                }
                                
                system initial = Y < startY >|VOID < startVOID >;
                """;

        PopulationState initialState = new PopulationState(new int[]{1, 0, 0, 0, 0, 20, 0});

        Map<String, ToDoubleFunction<PopulationState>> sirMeasuresMapping = new HashMap<>();
        sirMeasuresMapping.put("#PZ", s -> s.getOccupancy(3));

        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval("tau_mRNA",1.0,15.0),
                new ContinuousInterval("tau_prot",1.0,15.0));

        LPMModel reprModel = new LPMModel(rModelSpecification,
                rFormulaSpecification,
                rFormulaName,
                initialState,
                sirMeasuresMapping,
                50,
                formulaParameter);
//        Map<String,Double> param = new HashMap<>();
//        param.put("tau_mRNA",4.5);
//        param.put("tau_prot",4.5);
//        double value = reprModel.probReachAtTimeZero(param);
//        System.out.println(value);

        ToDoubleFunction<Map<String,Double>> probReachFunction = reprModel.probReachAtTimeZeroFunction();

        DataSet dataSetProbReach = new DataSet(hr, new FullFactorialSamplingTask(),20, probReachFunction);

        storeCSV(dataSetProbReach,"Repressillator_Test");

    }

    @Disabled
    @Test
    public void testRepressillatorSurrogate(){
        String rFormulaName = "f_id";
        String rFormulaSpecification = """
                measure #PZ \s
                formula f_id [kT = 2000, kB = 500 , t = 100, tW = 25, tE = 10] : \\E [0,t] ( \\G[tE,tW+tE]([#PZ > kB] && [#PZ < kT]) && \\E [0,tE][#PZ < kB]  && \\E [tW+tE,tE+tW+tE][#PZ < kB] )  endformula
                """;
        double[] formulaParameter = new double[]{800.0, 500.0 , 100.0, 10.0, 20.0};
        String rModelSpecification = """
                param n = 2;         /* Hill coefficient n */
                param KM = 40;       /* K_M*/
                param tau_mRNA = 2;  /* mRNA half life */
                param tau_prot = 10; /* protein half life */
                param ps_a = 0.5;    /* promotor strength (repressed) ( tps_repr ) */
                param ps_0 = 0.0005; /* promotor strength (full) ( tps_active ) */
                                
                const ln2 = 0.69314718056;
                const beta = 0.2;
                const alpha0 = 0.2164;
                const alpha = 216.404;
                const eff = 20;
                const t_ave =  tau_mRNA / ln2;   /* average mRNA lifetime */
                const kd_mRNA = ln2 / tau_prot;  /* mRNA decay rate */
                const kd_prot = ln2 / tau_mRNA;  /* protein decay rate  */
                const k_tl = eff / t_ave;        /* translation rate  */
                const a_tr = (ps_a -ps_0)*60;    /* transcription rate  */
                const a0_tr = ps_0 * 60;         /* transcription rate (repressed)   */
                                
                const startY = 20; /* Initial number of Y agents */
                const startVOID = 1; /* Initial number of VOID agents */
                                
                species VOID;  /* To represent nothingness, fictitious species are created */
                                
                species PX; /*  protein produced by X */
                species PY; /*  protein produced by Y */
                species PZ; /*  protein produced by Z */
                species X; /*  mRNA X (LacI) */
                species Y; /*  mRNA Y (TetR) */
                species Z; /*  mRNA Z (CI)*/
                                
                                
                rule degradation_of_X_transcripts {
                    X|VOID -[ kd_mRNA * #X ]-> VOID
                }
                                
                rule degradation_of_Y_transcripts {
                    Y|VOID -[ kd_mRNA * #Y ]-> VOID
                }
                                
                rule degradation_of_Z_transcripts {
                    Z|VOID -[ kd_mRNA * #Z ]-> VOID
                }
                                
                rule translation_of_X {
                    VOID -[ k_tl * #X ]-> VOID|PX
                }
                                
                rule translation_of_Y {
                    VOID -[ k_tl * #Y ]-> VOID|PY
                }
                                
                rule translation_of_Z {
                    VOID -[ k_tl * #Z ]-> VOID|PZ
                }
                                
                rule degradation_of_X {
                    PX|VOID -[ kd_prot * #PX ]-> VOID
                }
                                
                rule degradation_of_Y {
                    PY|VOID -[ kd_prot * #PY ]-> VOID
                }
                                
                rule degradation_of_Z {
                    PZ|VOID -[ kd_prot * #PZ ]-> VOID
                }
                                
                rule transcription_of_X {
                    VOID -[ a0_tr + ( ( a_tr * KM^(n) )/( KM^n + #PZ^n ) ) ]-> VOID|X
                }
                                
                rule transcription_of_Y {
                    VOID -[ a0_tr + ( ( a_tr * KM^(n) )/( KM^n + #PX^n ) ) ]-> VOID|Y
                }
                                
                rule transcription_of_Z {
                    VOID -[ a0_tr + ( ( a_tr * KM^(n) )/( KM^n + #PY^n ) ) ]-> VOID|Z
                }
                                
                system initial = Y < startY >|VOID < startVOID >;
                """;

        PopulationState initialState = new PopulationState(new int[]{1, 0, 0, 0, 0, 20, 0});

        Map<String, ToDoubleFunction<PopulationState>> reprMeasuresMapping = new HashMap<>();
        reprMeasuresMapping.put("#PZ", s -> s.getOccupancy(3));

        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval("tau_mRNA",1.0,15.0),
                new ContinuousInterval("tau_prot",1.0,15.0));

        LPMModel reprModel = new LPMModel(rModelSpecification,
                rFormulaSpecification,
                rFormulaName,
                initialState,
                reprMeasuresMapping,
                10,
                formulaParameter);

        ToDoubleFunction<Map<String,Double>> probReachFunction = reprModel.probReachAtTimeZeroFunction();

        DataSet ds = defaultDataset(probReachFunction,hr);

        ToDoubleFunction<Map<String,Double>> surrogateReachFunctionRFR = defaultSurrogate(new RandomForest(),ds);
        ToDoubleFunction<Map<String,Double>> surrogateReachFunctionRBFN = defaultSurrogate(new RBFNetwork(),ds);
        ToDoubleFunction<Map<String,Double>> surrogateReachFunctionGTB = defaultSurrogate(new GradientTreeBoost(),ds);
        ToDoubleFunction<Map<String,Double>> surrogateReachFunctionGPR = defaultSurrogate(new GaussianProcess(),ds);



//        ToDoubleFunction<Map<String,Double>> surrogateReachFunctionRFR = defaultRandomForestSurrogate(probReachFunction,hr);
//        ToDoubleFunction<Map<String,Double>> surrogateReachFunctionRBFN = defaultRBFNetworkSurrogate(probReachFunction,hr);
//        ToDoubleFunction<Map<String,Double>> surrogateReachFunctionGTB = defaultGradientTreeBoostSurrogate(probReachFunction,hr);
//        ToDoubleFunction<Map<String,Double>> surrogateReachFunctionGPR = defaultGPRegressionSurrogate(probReachFunction,hr);


        boolean writeDatasetCSV = true;
        if(writeDatasetCSV){
            DataSet dataSetProbReachRFR = new DataSet(hr, new FullFactorialSamplingTask(),15, surrogateReachFunctionRFR);
            storeCSV(dataSetProbReachRFR,"Repr_Test_RFR_surrogate");

            DataSet dataSetProbReachRBFN = new DataSet(hr, new FullFactorialSamplingTask(),15, surrogateReachFunctionRBFN);
            storeCSV(dataSetProbReachRBFN,"Repr_Test_RBFN_surrogate");

            DataSet dataSetProbReachGTB = new DataSet(hr, new FullFactorialSamplingTask(),15, surrogateReachFunctionGTB);
            storeCSV(dataSetProbReachGTB,"Repr_Test_GTB_surrogate");

            DataSet dataSetProbReachGPR = new DataSet(hr, new FullFactorialSamplingTask(),15,surrogateReachFunctionGPR);
            storeCSV(dataSetProbReachGPR,"Repr_Test_GPR_surrogate");

        }


    }

    private DataSet defaultDataset( ToDoubleFunction<Map<String,Double>> function, HyperRectangle hr){
        return new DataSet(hr,new LatinHyperCubeSamplingTask(),200,function);
    }

    private ToDoubleFunction<Map<String,Double>> defaultSurrogate( SurrogateFactory surrogate,DataSet dataSet){
        return surrogate.getSurrogateModel(dataSet,1.0,new Properties()).getSurrogateFunction(true);
    }

    @Disabled
    @Test
    public void testRepressillatorDataSetCSV2() throws StlModelGenerationException, ModelGenerationException {
        String rFormulaName = "f_id";
        String rFormulaSpecification = """
                measure #PZ \s
                formula f_id [kT = 2000, kB = 250] : \\G[40,60]([#PZ > kB] && [#PZ < kT]) && \\E [20,40][#PZ < kB]  && \\E [60,80][#PZ < kB]  endformula
                """;
        double[] formulaParameter = new double[]{2000.0, 300.0};
        String rModelSpecification = """
                param n = 2;         /* Hill coefficient n */
                param KM = 40;       /* K_M*/
                param tau_mRNA = 2;  /* mRNA half life */
                param tau_prot = 10; /* protein half life */
                param ps_a = 0.5;    /* promotor strength (repressed) ( tps_repr ) */
                param ps_0 = 0.0005; /* promotor strength (full) ( tps_active ) */
                                
                const ln2 = 0.69314718056;
                const beta = 0.2;
                const alpha0 = 0.2164;
                const alpha = 216.404;
                const eff = 20;
                const t_ave =  tau_mRNA / ln2;   /* average mRNA lifetime */
                const kd_mRNA = ln2 / tau_prot;  /* mRNA decay rate */
                const kd_prot = ln2 / tau_mRNA;  /* protein decay rate  */
                const k_tl = eff / t_ave;        /* translation rate  */
                const a_tr = (ps_a -ps_0)*60;    /* transcription rate  */
                const a0_tr = ps_0 * 60;         /* transcription rate (repressed)   */
                                
                const startY = 20; /* Initial number of Y agents */
                const startVOID = 1; /* Initial number of VOID agents */
                                
                species VOID;  /* To represent nothingness, fictitious species are created */
                                
                species PX; /*  protein produced by X */
                species PY; /*  protein produced by Y */
                species PZ; /*  protein produced by Z */
                species X; /*  mRNA X (LacI) */
                species Y; /*  mRNA Y (TetR) */
                species Z; /*  mRNA Z (CI)*/
                                
                                
                rule degradation_of_X_transcripts {
                    X|VOID -[ kd_mRNA * #X ]-> VOID
                }
                                
                rule degradation_of_Y_transcripts {
                    Y|VOID -[ kd_mRNA * #Y ]-> VOID
                }
                                
                rule degradation_of_Z_transcripts {
                    Z|VOID -[ kd_mRNA * #Z ]-> VOID
                }
                                
                rule translation_of_X {
                    VOID -[ k_tl * #X ]-> VOID|PX
                }
                                
                rule translation_of_Y {
                    VOID -[ k_tl * #Y ]-> VOID|PY
                }
                                
                rule translation_of_Z {
                    VOID -[ k_tl * #Z ]-> VOID|PZ
                }
                                
                rule degradation_of_X {
                    PX|VOID -[ kd_prot * #PX ]-> VOID
                }
                                
                rule degradation_of_Y {
                    PY|VOID -[ kd_prot * #PY ]-> VOID
                }
                                
                rule degradation_of_Z {
                    PZ|VOID -[ kd_prot * #PZ ]-> VOID
                }
                                
                rule transcription_of_X {
                    VOID -[ a0_tr + ( ( a_tr * KM^(n) )/( KM^n + #PZ^n ) ) ]-> VOID|X
                }
                                
                rule transcription_of_Y {
                    VOID -[ a0_tr + ( ( a_tr * KM^(n) )/( KM^n + #PX^n ) ) ]-> VOID|Y
                }
                                
                rule transcription_of_Z {
                    VOID -[ a0_tr + ( ( a_tr * KM^(n) )/( KM^n + #PY^n ) ) ]-> VOID|Z
                }
                                
                system initial = Y < startY >|VOID < startVOID >;
                """;

        PopulationState initialState = new PopulationState(new int[]{1, 0, 0, 0, 0, 20, 0});

        Map<String, ToDoubleFunction<PopulationState>> sirMeasuresMapping = new HashMap<>();
        sirMeasuresMapping.put("#PZ", s -> s.getOccupancy(3));

        HyperRectangle hr = new HyperRectangle(
                new ContinuousInterval("tau_mRNA",1.0,15.0),
                new ContinuousInterval("tau_prot",1.0,15.0));

        LPMModel reprModel = new LPMModel(rModelSpecification,
                rFormulaSpecification,
                rFormulaName,
                initialState,
                sirMeasuresMapping,
                50,
                formulaParameter);

        ToDoubleFunction<Map<String,Double>> probReachFunction = reprModel.probReachAtTimeZeroFunction();

        DataSet dataSetProbReach = new DataSet(hr, new FullFactorialSamplingTask(),10, probReachFunction);

        storeCSV(dataSetProbReach,"Repressillator_Ver_2_Test");

    }


    private ToDoubleFunction<Map<String,Double>> defaultSurrogate(SurrogateFactory surrogateFactory, ToDoubleFunction<Map<String,Double>> function, HyperRectangle hr){
        SurrogateModel randomForestModel = surrogateFactory.getSurrogateModel(
                function,
                new LatinHyperCubeSamplingTask(),
                hr,1000,0.95,
                new Properties());

        return randomForestModel.getSurrogateFunction(true);
    }

    private ToDoubleFunction<Map<String,Double>> defaultRandomForestSurrogate(ToDoubleFunction<Map<String,Double>> function, HyperRectangle hr){
        return defaultSurrogate(new RandomForest(),function,hr);
    }

    private ToDoubleFunction<Map<String,Double>> defaultGradientTreeBoostSurrogate(ToDoubleFunction<Map<String,Double>> function, HyperRectangle hr){
        return defaultSurrogate(new GradientTreeBoost(),function,hr);
    }

    private ToDoubleFunction<Map<String,Double>> defaultRBFNetworkSurrogate(ToDoubleFunction<Map<String,Double>> function, HyperRectangle hr){
        return defaultSurrogate(new RBFNetwork(),function,hr);
    }

    @Test
    public void testPrinter(){
        String csvData = "Name, Age, City\nJohn, 25, New York\nAlice, 30, London";
        writeCSVFile(csvData,"exampleTest");
    }

    private void storeCSV(DataSet dataSet, String fileName) {
        writeCSVFile(CSVWriter.getCSVStringFromTable(dataSet),fileName);
    }
    private static void writeCSVFile(String csvData,String fileName) {
        String userDocumentsFolderPath = System.getProperty("user.home") + "/Documents";
        String folderName = "SibillaTestFolder";
        Path folderPath = Paths.get(userDocumentsFolderPath, folderName);
        try {
            Files.createDirectories(folderPath);
            System.out.println("Folder created: " + folderPath);
        } catch (IOException e) {
            System.err.println("Error creating folder: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        int fileNumber = 0;
        String filePathStr = folderPath.resolve(fileName + ".csv").toString();
        while (Files.exists(Paths.get(filePathStr))) {
            fileNumber++;
            filePathStr = folderPath.resolve(fileName + "_" + fileNumber + ".csv").toString();
        }
        try {
            Path filePath = Paths.get(filePathStr);
            Files.write(filePath, csvData.getBytes());
            System.out.println("CSV file has been written to: " + filePath);
        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

package it.unicam.quasylab.sibilla.core.runtime;

import it.unicam.quasylab.sibilla.core.models.pm.PopulationModel;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationModelDefinition;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.pso.PSOTask;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.FullFactorialSamplingTask;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.LatinHyperCubeSamplingTask;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.tools.synthesis.surrogate.DataSet;
import it.unicam.quasylab.sibilla.tools.synthesis.surrogate.RandomForest;
import it.unicam.quasylab.sibilla.tools.synthesis.surrogate.SurrogateFactory;
import it.unicam.quasylab.sibilla.tools.synthesis.surrogate.SurrogateModel;
import it.unicam.quasylab.sibilla.core.simulator.DefaultRandomGenerator;
import it.unicam.quasylab.sibilla.core.simulator.SimulationEnvironment;
import it.unicam.quasylab.sibilla.core.simulator.Trajectory;
import it.unicam.quasylab.sibilla.tools.stl.QualitativeMonitor;
import it.unicam.quasylab.sibilla.core.util.values.SibillaDouble;
import it.unicam.quasylab.sibilla.langs.pm.ModelGenerationException;
import it.unicam.quasylab.sibilla.langs.pm.PopulationModelGenerator;
import it.unicam.quasylab.sibilla.tools.stl.StlLoader;
import it.unicam.quasylab.sibilla.tools.stl.StlModelGenerationException;
import it.unicam.quasylab.sibilla.tools.stl.StlMonitorFactory;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SIRModelOptimalFeasibilityTest {
    private String getSirModelSpecification(){
        return  """
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
    }
    private String getSirFormulaSpecification(){
        return  """
                measure #I
                formula formula_id [] : ( \\E[100,120][ #I == 0] )&& (\\G[0,100][ #I > 0 ]) endformula
                """;
    }
    private String getSirFormulaName(){
        return  "formula_id";
    }
    private PopulationState geSirInitialState(){
        return new PopulationState(new int[]{90, 10, 0});
    }
    private HyperRectangle getSirSearchSpace(){
        return new HyperRectangle(
                new ContinuousInterval("k_i",0.005,0.3),
                new ContinuousInterval("k_r",0.005,0.2));
    }
    private PopulationModelGenerator getPopulationModelGenerator(String modelSpecification){
        return new PopulationModelGenerator(modelSpecification);
    }
    private PopulationModelDefinition getPopulationModelDefinition(PopulationModelGenerator pmg) throws ModelGenerationException {
        return pmg.getPopulationModelDefinition();
    }
    private PopulationModel getPopulationModel(PopulationModelDefinition pmd,Map<String,Double> parameters){
        for (String key : parameters.keySet()){
            pmd.setParameter(key,new SibillaDouble(parameters.get(key)));
        }
        monitoringParametersInDefinition(pmd,true);
        return pmd.createModel();
    }

    private void monitoringParametersInDefinition(PopulationModelDefinition pmd,boolean silent){
        if (!silent) {
            String[] parameters = pmd.getModelParameters();
            System.out.println("\n");
            for (String p : parameters){
                System.out.println(p+ " = " + pmd.getParameterValue(p));
            }
        }
    }
    private Map<String, ToDoubleFunction<PopulationState>> getSirMeasuresMap(){
        Map<String, ToDoubleFunction<PopulationState>> measuresMap = new HashMap<>();
        measuresMap.put("#S", s -> s.getOccupancy(0));
        measuresMap.put("#I", s -> s.getOccupancy(1));
        measuresMap.put("#R", s -> s.getOccupancy(2));
        return measuresMap;
    }
    private StlMonitorFactory<PopulationState> getMonitorFactory(String formulaSpecification, Map<String, ToDoubleFunction<PopulationState>> measuresMap ) throws StlModelGenerationException {
        StlLoader loader = new StlLoader(formulaSpecification);
        return loader.getModelFactory(measuresMap);
    }
    private QualitativeMonitor<PopulationState> getQualitativeMonitor( StlMonitorFactory<PopulationState> monitorFactory, String formulaName, double[] formulaParameter){
        return monitorFactory.getQualitativeMonitor(formulaName,formulaParameter);
    }

    private Trajectory<PopulationState> sampleTrajectory(SimulationEnvironment se, RandomGenerator rg, PopulationModelDefinition pmd, PopulationState initialState, double deadline,Map<String,Double> parameters){
        PopulationModel pm = getPopulationModel(pmd,parameters);
        Trajectory<PopulationState> trajectory= se.sampleTrajectory(
                rg,
                pm,
                initialState,
                deadline);
        trajectory.setEnd(deadline);
        return trajectory;
    }


    private double probReachFormulaAtTime0(QualitativeMonitor<PopulationState> qm,PopulationModelDefinition pmd,PopulationState initialState, int numberOfTrajectories,Map<String,Double> parameters){
        SimulationEnvironment se = new SimulationEnvironment();
        RandomGenerator rg = new DefaultRandomGenerator();
        Supplier<Trajectory<PopulationState>> trjSup = ()-> sampleTrajectory(se,rg,pmd,initialState,qm.getTimeHorizon(),parameters);
        double[] probabilities = QualitativeMonitor
                .computeProbability(
                        qm,
                        trjSup,
                        numberOfTrajectories,
                        new double[]{0.0}
                );
        return probabilities[0];
    }



    @Disabled
    @Test
    public void testSIRSmall() throws StlModelGenerationException, ModelGenerationException{
        PopulationState initialSirState = geSirInitialState();
        Map<String, ToDoubleFunction<PopulationState>> sirMeasuresMapping = getSirMeasuresMap();
        HyperRectangle searchSirSpace = getSirSearchSpace();
        QualitativeMonitor<PopulationState> qm = getQualitativeMonitor(
                getMonitorFactory(getSirFormulaSpecification(),sirMeasuresMapping),
                getSirFormulaName(),
                new double[]{}
        );
        PopulationModelDefinition pmd = getPopulationModelDefinition(
                getPopulationModelGenerator(getSirModelSpecification())
        );
        ToDoubleFunction<Map<String,Double>> probFunction = m ->
                probReachFormulaAtTime0(qm,pmd,initialSirState,10,m);
        DataSet dataSetProbReach = new DataSet(searchSirSpace, new FullFactorialSamplingTask(),20, probFunction);

        storeCSV(dataSetProbReach,"SIR_Test_Small");

    }

    @Disabled
    @Test
    public void testSIRBig() throws StlModelGenerationException, ModelGenerationException{
        PopulationState initialSirState = geSirInitialState();
        Map<String, ToDoubleFunction<PopulationState>> sirMeasuresMapping = getSirMeasuresMap();
        HyperRectangle searchSirSpace = getSirSearchSpace();
        QualitativeMonitor<PopulationState> qm = getQualitativeMonitor(
                getMonitorFactory(getSirFormulaSpecification(),sirMeasuresMapping),
                getSirFormulaName(),
                new double[]{}
        );
        PopulationModelDefinition pmd = getPopulationModelDefinition(
                getPopulationModelGenerator(getSirModelSpecification())
        );
        ToDoubleFunction<Map<String,Double>> probFunction = m ->
                probReachFormulaAtTime0(qm,pmd,initialSirState,100,m);
        DataSet dataSetProbReach = new DataSet(searchSirSpace, new FullFactorialSamplingTask(),100, probFunction);

        storeCSV(dataSetProbReach,"SIR_Test_Big");

    }




    private void storeCSV(DataSet dataSet, String fileName) {
        writeCSVFile(CSVWriter.getCSVStringFromTable(dataSet),fileName);
    }


    //@Disabled
    @Test
    public void testSirCaseMaximization() throws StlModelGenerationException, ModelGenerationException{

        PopulationState initialSirState = geSirInitialState();
        Map<String, ToDoubleFunction<PopulationState>> sirMeasuresMapping = getSirMeasuresMap();
        HyperRectangle searchSirSpace = getSirSearchSpace();
        QualitativeMonitor<PopulationState> qm = getQualitativeMonitor(
                getMonitorFactory(getSirFormulaSpecification(),sirMeasuresMapping),
                getSirFormulaName(),
                new double[]{}
        );
        PopulationModelDefinition pmd = getPopulationModelDefinition(
                getPopulationModelGenerator(getSirModelSpecification())
        );


        ToDoubleFunction<Map<String,Double>> probFunction = m ->
                probReachFormulaAtTime0(qm,pmd,initialSirState,100,m);

        SurrogateFactory surrogateFactory = new RandomForest();
        SurrogateModel randomForestModel = surrogateFactory.getSurrogateModel(
                probFunction,
                new LatinHyperCubeSamplingTask(),
                searchSirSpace,500,0.95,
                new Properties());

        ToDoubleFunction<Map<String,Double>> surrogateFunction = randomForestModel.getSurrogateFunction(true);

        Map<String,Double> maximizingValues = new PSOTask().maximize(surrogateFunction,searchSirSpace);
        System.out.println(maximizingValues);
        System.out.println("surrogate value : " + surrogateFunction.applyAsDouble(maximizingValues));
        System.out.println("actual value    : " + probFunction.applyAsDouble(maximizingValues));

        assertEquals(maximizingValues.get("k_i"),0.25,0.1);
        assertEquals(maximizingValues.get("k_r"),0.05,0.1);
    }



    @Disabled
    @Test
    public void testPrinter(){
        String csvData = "Name, Age, City\nJohn, 25, New York\nAlice, 30, London";
        writeCSVFile(csvData,"SirExample");
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

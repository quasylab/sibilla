package it.unicam.quasylab.sibilla.core.runtime;

import it.unicam.quasylab.sibilla.core.models.pm.PopulationModel;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationModelDefinition;
import it.unicam.quasylab.sibilla.core.models.pm.PopulationState;
import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.pso.PSOTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.FullFactorialSamplingTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.LatinHyperCubeSamplingTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.ContinuousInterval;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.DataSet;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.RandomForest;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.SurrogateFactory;
import it.unicam.quasylab.sibilla.core.optimization.surrogate.SurrogateModel;
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


public class RSModelOptimalFeasibilityTest {

    private String getRsModelSpecification(){
        return  """
                param k_s = 0.05;
                param k_r = 0.02;
                
                species S;
                species I;
                species B;
                
                
                rule spreading {
                    S|I -[ #S * #I * k_s ]-> S|S
                }
                
                rule stop_spreading_1 {
                    S|B -[ #S * (#S - 1) * k_r ]-> B|B
                }
                
                rule stop_spreading_2 {
                    S|B -[ #B * #S * k_r]-> B|B
                }
                """;
    }
    private String getRsFormulaSpecification(){
        return  """
                measure #S
                measure #I
                formula formula_id [] : ( \\G[3,5]( [ #I > 0 ] ) )  && ( \\E[0,1] ( \\G[0,0.02] ( [#S > 50 ] ) ) )  endformula
                """;
    }
    private String getSibFormulaName(){
        return  "formula_id";
    }
    private PopulationState getRsInitialState(){
        return new PopulationState(new int[]{10, 90, 0});
    }
    private HyperRectangle getRsSearchSpace(){
        return new HyperRectangle(
                new ContinuousInterval("k_s",0.0001,2),
                new ContinuousInterval("k_r",0.0001,0.5));
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
        monitoringParametersInDefinition(pmd,true);
        return pmd.createModel();
    }
    @SuppressWarnings("SameParameterValue")
    private void monitoringParametersInDefinition(PopulationModelDefinition pmd,boolean silent){
        if (!silent) {
            String[] parameters = pmd.getModelParameters();
            System.out.println("\n");
            for (String p : parameters){
                System.out.println(p+ " = " + pmd.getParameterValue(p));
            }
        }
    }
    private Map<String, ToDoubleFunction<PopulationState>> getRsMeasuresMap(){
        Map<String, ToDoubleFunction<PopulationState>> measuresMap = new HashMap<>();
        measuresMap.put("#S", s -> s.getOccupancy(0));
        measuresMap.put("#I", s -> s.getOccupancy(1));
        measuresMap.put("#B", s -> s.getOccupancy(2));
        return measuresMap;
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
    public void testRSSmall() throws StlModelGenerationException, ModelGenerationException{
        PopulationState initialRsState = getRsInitialState();
        Map<String, ToDoubleFunction<PopulationState>> rsMeasuresMapping = getRsMeasuresMap();
        HyperRectangle searchRsSpace = getRsSearchSpace();
        QualitativeMonitor<PopulationState> qm = getQualitativeMonitor(
                getMonitorFactory(getRsFormulaSpecification(),rsMeasuresMapping),
                getSibFormulaName(),
                new double[]{}
        );
        PopulationModelDefinition pmd = getPopulationModelDefinition(
                getPopulationModelGenerator(getRsModelSpecification())
        );
        ToDoubleFunction<Map<String,Double>> probFunction = m ->
                probReachFormulaAtTime0(qm,pmd,initialRsState,10,m);
        DataSet dataSetProbReach = new DataSet(searchRsSpace, new FullFactorialSamplingTask(),20, probFunction);

        storeCSV(dataSetProbReach,"RS_Test_Small");

    }

    @Disabled
    @Test
    public void testRSBig() throws StlModelGenerationException, ModelGenerationException{
        PopulationState initialRsState = getRsInitialState();
        Map<String, ToDoubleFunction<PopulationState>> rsMeasuresMapping = getRsMeasuresMap();
        HyperRectangle searchRsSpace = getRsSearchSpace();
        QualitativeMonitor<PopulationState> qm = getQualitativeMonitor(
                getMonitorFactory(getRsFormulaSpecification(),rsMeasuresMapping),
                getSibFormulaName(),
                new double[]{}
        );
        PopulationModelDefinition pmd = getPopulationModelDefinition(
                getPopulationModelGenerator(getRsModelSpecification())
        );
        ToDoubleFunction<Map<String,Double>> probFunction = m ->
                probReachFormulaAtTime0(qm,pmd,initialRsState,50,m);
        DataSet dataSetProbReach = new DataSet(searchRsSpace, new FullFactorialSamplingTask(),25, probFunction);

        storeCSV(dataSetProbReach,"RS_Test_Big");

    }




    private void storeCSV(DataSet dataSet, String fileName) {
        writeCSVFile(CSVWriter.getCSVStringFromTable(dataSet),fileName);

    }


    @Disabled
    @Test
    public void testRsCaseMaximization() throws StlModelGenerationException, ModelGenerationException{

        PopulationState initialRsState = getRsInitialState();
        Map<String, ToDoubleFunction<PopulationState>> RsMeasuresMapping = getRsMeasuresMap();
        HyperRectangle searchRsSpace = getRsSearchSpace();
        QualitativeMonitor<PopulationState> qm = getQualitativeMonitor(
                getMonitorFactory(getRsFormulaSpecification(),RsMeasuresMapping),
                getSibFormulaName(),
                new double[]{}
        );
        PopulationModelDefinition pmd = getPopulationModelDefinition(
                getPopulationModelGenerator(getRsModelSpecification())
        );


        ToDoubleFunction<Map<String,Double>> probFunction = m ->
                probReachFormulaAtTime0(qm,pmd,initialRsState,100,m);

        SurrogateFactory surrogateFactory = new RandomForest();
        SurrogateModel randomForestModel = surrogateFactory.getSurrogateModel(
                probFunction,
                new LatinHyperCubeSamplingTask(),
                searchRsSpace,500,0.95,
                new Properties());

        ToDoubleFunction<Map<String,Double>> surrogateFunction = randomForestModel.getSurrogateFunction(true);

        Map<String,Double> maximizingValues = new PSOTask().maximize(surrogateFunction,searchRsSpace);
        System.out.println(maximizingValues);

        assertEquals(maximizingValues.get("k_i"),0.25,0.1);
        assertEquals(maximizingValues.get("k_r"),0.05,0.1);
    }



    @Disabled
    @Test
    public void testPrinter(){
        String csvData = "Name, Age, City\nJohn, 25, New York\nAlice, 30, London";
        writeCSVFile(csvData,"RsExample");
    }

    private static void writeCSVFile(String csvData,String fileName) {
        // Get the user's document folder path
        String userDocumentsFolderPath = System.getProperty("user.home") + "/Documents";
        // Folder name
        String folderName = "SibillaTestFolder";
        // Create folder "SibillaTest" in the user's document folder
        Path folderPath = Paths.get(userDocumentsFolderPath, folderName);
        try {
            Files.createDirectories(folderPath);
            System.out.println("Folder created: " + folderPath);
        } catch (IOException e) {
            System.err.println("Error creating folder: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Specify the path for the CSV file inside "SibillaTest" folder
        int fileNumber = 0;
        String filePathStr = folderPath.resolve(fileName + ".csv").toString();
        while (Files.exists(Paths.get(filePathStr))) {
            fileNumber++;
            filePathStr = folderPath.resolve(fileName + "_" + fileNumber + ".csv").toString();
        }

        // Write CSV data to the file
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

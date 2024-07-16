package it.unicam.quasylab.sibilla.tools.synthesis.surrogate;

import it.unicam.quasylab.sibilla.tools.synthesis.sampling.SamplingTask;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.HyperRectangle;
import smile.data.Tuple;
import smile.regression.RandomForest;

import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.function.ToDoubleFunction;
import java.util.stream.LongStream;

/**
 * class representing a surrogate model, specifically the model is the random forest regression
 *
 * @author      Lorenzo Matteucci
 */
public class RandomForestModel extends AbstractSurrogateModel{

    private RandomForest randomForest;

    private int numberOfTrees;
    private int mtry;
    private int maxDepth;
    private int maxNodes;
    private int nodeSize;
    private double subSample;

    public RandomForestModel(DataSet dataSet, double trainingPortion, Properties properties) {
        super(dataSet, trainingPortion, properties);
    }

    public RandomForestModel(ToDoubleFunction<Map<String, Double>> functionToBeSurrogate, SamplingTask samplingTask, HyperRectangle sampleSpace, int numberOfSamples,double trainingPortion, Properties properties) {
        super(functionToBeSurrogate, samplingTask, sampleSpace, numberOfSamples, trainingPortion, properties);
    }

    public RandomForestModel(DataSet dataSet,double trainingPortion, Properties properties,Long seed) {
        super(dataSet, trainingPortion, properties, seed);
    }

    public RandomForestModel(ToDoubleFunction<Map<String, Double>> functionToBeSurrogate, SamplingTask samplingTask, HyperRectangle sampleSpace, int numberOfSamples,double trainingPortion, Properties properties,Long seed) {
        super(functionToBeSurrogate, samplingTask, sampleSpace, numberOfSamples, trainingPortion, properties, seed);
    }

    @Override
    public double predict(Double[] inputVector) {
        Tuple predictorTuple = Tuple.of(inputVector,this.getModelScheme());
        return this.randomForest.predict(predictorTuple);
    }

    @Override
    public void fit() {
        long start = System.nanoTime();
        this.randomForest = RandomForest.fit(
                DEFAULT_FORMULA,
                this.trainingSet.smile().toDataFrame(),
                this.numberOfTrees, this.mtry, this.maxDepth, this.maxNodes, this.nodeSize, this.subSample,randomLongStream(this.seed));

        this.fitTime = (System.nanoTime() - start) / 1E6;
    }


    public static LongStream randomLongStream(long seed) {
        Random random = new Random(seed);
        return LongStream.generate(random::nextLong);
    }


    @Override
    public void setProperties(Properties properties) {
        int NUMBER_OF_TREES = 500;
        int MTRY = Math.max(this.trainingSet.smile().toDataFrame().ncol()/3, 1);
        int MAX_DEPTH = 200;
        int MAX_NODES = this.trainingSet.smile().toDataFrame().size() / 5;
        int NODE_SIZE = 5;
        double SUB_SAMPLE = 1.0;

        this.numberOfTrees = Integer.parseInt(properties.getProperty("rf.trees", NUMBER_OF_TREES+""));
        properties.setProperty("rf.trees", String.valueOf(this.numberOfTrees));

        this.mtry = Integer.parseInt(properties.getProperty("rf.mtry", MTRY+""));
        properties.setProperty("rf.mtry", String.valueOf(this.mtry));

        this.maxDepth = Integer.parseInt(properties.getProperty("rf.max_depth", MAX_DEPTH+""));
        properties.setProperty("rf.max_depth", String.valueOf(this.maxDepth));

        this.maxNodes = Integer.parseInt(properties.getProperty("rf.max_nodes", MAX_NODES+""));
        properties.setProperty("rf.max_nodes", String.valueOf(this.maxNodes));

        this.nodeSize = Integer.parseInt(properties.getProperty("rf.node_size", NODE_SIZE+""));
        properties.setProperty("rf.node_size", String.valueOf(this.nodeSize));

        this.subSample = Double.parseDouble(properties.getProperty("rf.rate", SUB_SAMPLE+""));
        properties.setProperty("rf.rate", String.valueOf(this.subSample));

        this.properties = properties;
    }

    @Override
    public String toString() {
        String str = "\n Model : Random Forest Regression ";
        str += "\n  - number of trees      : "+this.numberOfTrees;
        str += "\n  - mtry                 : "+this.mtry;
        str += "\n  - max trees depth      : "+this.maxDepth;
        str += "\n  - max nodes            : "+this.maxNodes;
        str += "\n  - nodes size           : "+this.nodeSize;
        str += "\n  - subSample            : "+this.subSample;
        return str;
    }



}








package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import it.unicam.quasylab.sibilla.core.optimization.sampling.SamplingTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import smile.base.rbf.RBF;
import smile.data.DataFrame;
import smile.regression.RBFNetwork;

import java.util.Map;
import java.util.Properties;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.DEFAULT_COLUMN_RESULT_NAME;


public class RBFNetworkModel extends AbstractSurrogateModel{

    private RBFNetwork<double[]> rbfNetwork;

    private int rbfNeuronsToLearn;
    private boolean normalized;

    public RBFNetworkModel(TrainingSet trainingSet, Properties properties) {
        super(trainingSet, properties);
    }

    public RBFNetworkModel(ToDoubleFunction<Map<String, Double>> functionToBeSurrogate, SamplingTask samplingTask, HyperRectangle sampleSpace, int numberOfSamples, Properties properties) {
        super(functionToBeSurrogate, samplingTask, sampleSpace, numberOfSamples, properties);
    }

    @Override
    public double predict(Double[] inputVector) {
       return this.rbfNetwork.predict(Stream.of(inputVector).mapToDouble(Double::doubleValue).toArray());
    }

    @Override
    public void fit() {
        DataFrame trainingSetDataFrame = trainingSet.smile().toDataFrame();
        var y = trainingSetDataFrame.column(DEFAULT_COLUMN_RESULT_NAME).toDoubleArray();
        int[] columnIndexesX = new int[trainingSet.columnCount()-1];
        for (int i = 0; i < trainingSet.columnCount()-1; i++) {
            columnIndexesX[i] = i;
        }
        double[][] x = trainingSetDataFrame.select(columnIndexesX).toArray();
        long start = System.nanoTime();
        this.rbfNetwork = smile.regression.RBFNetwork.fit(x, y, RBF.fit(x, this.rbfNeuronsToLearn), this.normalized);
        this.fitTime = (System.nanoTime() - start) / 1E6;
    }


    @Override
    public void setProperties(Properties properties) {
        int RBF_NEURONS_NUMBER = trainingSet.columnCount()*2;
        boolean NORMALIZED = false;

        this.rbfNeuronsToLearn = Integer.parseInt(properties.getProperty("rbfn.neurons", RBF_NEURONS_NUMBER+""));
        this.normalized = Boolean.parseBoolean(properties.getProperty("rbfn.normalized",NORMALIZED+""));

        this.properties = properties;
    }

    @Override
    public String toString() {
        if(this.rbfNetwork == null)
            return "\n Model : Radial basis function network";
        String str = "\n Model : Radial basis function network";
        str += "\n  - RBF neurons number : "+rbfNeuronsToLearn;
        str += "\n  - normalized         : "+normalized;
        return str;
    }
}


//
//    private RBFNetwork<double[]> rbfNetwork;
//    private Properties properties;
//    private double fitTime;
//    private TrainingSet trainingSet;
//    private int rbfNeuronsToLearn;
//    private boolean normalized;
//
//    public RBFNetworkModel(){
//        this(new Properties());
//    }
//
//    public RBFNetworkModel(Properties properties){
//        this.properties=properties;
//    }
//
//    @Override
//    public double predict(Double[] input) {
//        return this.rbfNetwork.predict(Stream.of(input).mapToDouble(Double::doubleValue).toArray());
//    }
//
//    @Override
//    public void fit(TrainingSet trainingSet) {
//        this.trainingSet = trainingSet;
//        this.rbfNeuronsToLearn = Integer.parseInt(this.properties.getProperty("surrogate.rbf.network.neurons",String.valueOf(trainingSet.columnCount()*2)));
//        this.normalized = Boolean.parseBoolean(this.properties.getProperty("surrogate.rbf.network.normalized","false"));
//        DataFrame trainingSetDataFrame = trainingSet.smile().toDataFrame();
//        var y = trainingSetDataFrame.column(DEFAULT_COLUMN_RESULT_NAME).toDoubleArray();
//        int[] columnIndexesX = new int[trainingSet.columnCount()-1];
//        for (int i = 0; i < trainingSet.columnCount()-1; i++) {
//            columnIndexesX[i] = i;
//        }
//        double[][] x = trainingSetDataFrame.select(columnIndexesX).toArray();
//        long start = System.nanoTime();
//        this.rbfNetwork = smile.regression.RBFNetwork.fit(x, y, RBF.fit(x, this.rbfNeuronsToLearn), this.normalized);
//        this.fitTime = (System.nanoTime() - start) / 1E6;
//    }
//
//    @Override
//    public SurrogateMetrics getInSampleMetrics() {
//        return new SurrogateMetrics(this,this.trainingSet,this.fitTime);
//    }
//
//    @Override
//    public SurrogateMetrics getOutOfSampleMetrics(TrainingSet outOfSampleTrainingSet) {
//        return new SurrogateMetrics(this,outOfSampleTrainingSet,this.fitTime);
//    }
//
//    @Override
//    public void setProperties(Properties properties) {
//        this.properties = properties;
//    }
//
//    @Override
//    public String toString() {
//        if(this.rbfNetwork == null)
//            return "\n Model : Radial basis function network";
//        String str = "\n Model : Radial basis function network";
//        str += "\n  - RBF neurons number : "+rbfNeuronsToLearn;
//        str += "\n  - normalized         : "+normalized;
//        return str;
//    }

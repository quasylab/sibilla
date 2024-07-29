package it.unicam.quasylab.sibilla.tools.synthesis.surrogate;

import it.unicam.quasylab.sibilla.tools.synthesis.sampling.SamplingTask;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.HyperRectangle;
import smile.data.formula.Formula;
import smile.data.type.StructType;
import smile.math.MathEx;

import java.util.Map;
import java.util.Properties;
import java.util.function.ToDoubleFunction;

import static it.unicam.quasylab.sibilla.tools.synthesis.Commons.DEFAULT_COLUMN_RESULT_NAME;

public abstract class AbstractSurrogateModel implements SurrogateModel {


    protected final DataSet trainingSet;
    protected final DataSet testSet;

    protected Properties properties;
    protected Formula DEFAULT_FORMULA = Formula.lhs(DEFAULT_COLUMN_RESULT_NAME);
    protected double fitTime;
    protected Long seed;

    public AbstractSurrogateModel(DataSet dataSet,double trainingPortion, Properties properties, Long seed){
        this.seed = seed;
        DataSet[] splitDataset = dataSet.trainTestSplit(trainingPortion,seed);
        this.trainingSet = splitDataset[0];
        this.testSet = splitDataset[1];
        this.setProperties(properties);
        initializeRandomNumberGenerator();

    }

    public AbstractSurrogateModel(ToDoubleFunction<Map<String,Double>> functionToBeSurrogate,
                                  SamplingTask samplingTask,
                                  HyperRectangle sampleSpace,
                                  int numberOfSamples,
                                  double trainingPortion,
                                  Properties properties,
                                  Long seed){
        this.seed = seed;
        DataSet dataSet = new DataSet(sampleSpace, samplingTask, numberOfSamples,functionToBeSurrogate,seed);
        DataSet[] splitDataset = dataSet.trainTestSplit(trainingPortion,seed);
        this.trainingSet = splitDataset[0];
        this.testSet = splitDataset[1];
        this.setProperties(properties);
        initializeRandomNumberGenerator();
    }

    public AbstractSurrogateModel(DataSet dataSet,double trainingPortion, Properties properties){
        this(dataSet,trainingPortion,properties,System.nanoTime());
    }

    public AbstractSurrogateModel(ToDoubleFunction<Map<String,Double>> functionToBeSurrogate,
                                  SamplingTask samplingTask,
                                  HyperRectangle sampleSpace,
                                  int numberOfSamples,
                                  double trainingPortion,
                                  Properties properties){
       this(functionToBeSurrogate,samplingTask,sampleSpace,numberOfSamples,trainingPortion,properties,System.nanoTime());
    }


    @Override
    public ToDoubleFunction<Map<String,Double>> getSurrogateFunction(boolean performTraining){
        if(performTraining)
            this.fit();
        return input -> this.predict(trainingSet.columnNames().stream()
                .limit(trainingSet.columnNames().size()-1)
                .map(input::get)
                .toArray(Double[]::new)
        );
    }

    @Override
    public ToDoubleFunction<Map<String, Double>> getSurrogateFunction() {
        return input -> this.predict(trainingSet.columnNames().stream()
                .limit(trainingSet.columnNames().size()-1)
                .map(input::get)
                .toArray(Double[]::new));
    }

    /**
     * Return a predicted value by passing it a vector of parameters
     * to the surrogate regression model
     *
     * @param  inputVector the input vector
     * @return      the predicted Value
     */
    abstract double predict(Double[] inputVector);

    @Override
    public SurrogateMetrics getInSampleMetrics() {
        return new SurrogateMetrics(this,this.trainingSet,trainingSet.rowCount(),testSet.rowCount(),this.fitTime);
    }

    @Override
    public SurrogateMetrics getOutOfSampleMetrics() {
        return new SurrogateMetrics(this, this.testSet,trainingSet.rowCount(),testSet.rowCount(),this.fitTime);
    }

    @Override
    public void setProperty(String key,String value){
        if(this.properties.containsKey(key))
            this.properties.setProperty(key, value);
    }

    @Override
    public Properties getProperties(){
        return this.properties;
    }

    protected StructType getModelScheme(){
        return this.trainingSet.smile().toDataFrame().schema();
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
        initializeRandomNumberGenerator();
    }

    /*
        Initialize the random number generator
         */
    private void initializeRandomNumberGenerator(){
        MathEx.setSeed(this.seed);
    }

}

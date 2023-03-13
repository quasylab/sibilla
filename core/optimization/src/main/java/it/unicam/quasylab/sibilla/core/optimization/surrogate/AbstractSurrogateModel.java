package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import it.unicam.quasylab.sibilla.core.optimization.sampling.LatinHyperCubeSamplingTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.SamplingTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;
import smile.data.formula.Formula;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.ToDoubleFunction;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.DEFAULT_COLUMN_RESULT_NAME;

public abstract class AbstractSurrogateModel implements SurrogateModel{

    protected final TrainingSet trainingSet;
    protected Properties properties;
    protected Formula DEFAULT_FORMULA = Formula.lhs(DEFAULT_COLUMN_RESULT_NAME);
    protected double fitTime;

    public AbstractSurrogateModel(TrainingSet trainingSet, Properties properties){
        this.trainingSet = trainingSet;
        this.setProperties(properties);

    }

    public AbstractSurrogateModel(ToDoubleFunction<Map<String,Double>> functionToBeSurrogate,
                                  SamplingTask samplingTask,
                                  HyperRectangle sampleSpace,
                                  int numberOfSamples,
                                  Properties properties){
        this.trainingSet = new TrainingSet(sampleSpace, samplingTask, numberOfSamples,functionToBeSurrogate);
        this.setProperties(properties);
    }


    @Override
    public ToDoubleFunction<Map<String,Double>> getSurrogateFunction(){
        this.fit();
        return input -> this.predict(trainingSet.columnNames().stream().limit(trainingSet.columnNames().size()-1).map(input::get).toArray(Double[]::new));
    }

    /**
     * Return a predicted value by passing it a vector of parameters
     * to the surrogate regression model
     *
     * @param  inputVector the input vector
     * @return      the predicted Value
     */
    abstract double predict(Double[] inputVector);
    /**
     * Train the surrogate model by passing a training set
     */
    abstract void fit();

    @Override
    public SurrogateMetrics getInSampleMetrics() {
        return new SurrogateMetrics(this,this.trainingSet,this.fitTime);
    }

    @Override
    public SurrogateMetrics getOutOfSampleMetrics(TrainingSet outOfSampleTrainingSet) {
        return new SurrogateMetrics(this,outOfSampleTrainingSet,this.fitTime);
    }


    @Override
    public void setProperty(String key,String value){
        if(this.properties.containsKey(key))
            this.properties.setProperty(key, value);
    }




}

package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import smile.data.DataFrame;
import smile.data.Tuple;
import smile.data.formula.Formula;
import smile.regression.RandomForest;
import smile.validation.RegressionMetrics;

import java.util.Properties;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.DEFAULT_COLUMN_RESULT_NAME;

public class RandomForestSurrogate implements Surrogate{
    private RandomForest randomForest;
    private final Properties properties;
    private final Formula formula;

    public RandomForestSurrogate(){
        this(new Properties(),Formula.lhs(DEFAULT_COLUMN_RESULT_NAME));
    }

    public RandomForestSurrogate(Properties properties){
        this(properties,Formula.lhs(DEFAULT_COLUMN_RESULT_NAME));
    }
    public RandomForestSurrogate(Properties properties,Formula formula){
        this.properties =properties;
        this.formula = formula;
    }


    @Override
    public double predict(Double[] x) {
        Tuple predictorTuple = Tuple.of(x,this.randomForest.schema());
        return this.randomForest.predict(predictorTuple);
    }

    @Override
    public void fit(TrainingSet trainingSet) {
        DataFrame trainingSetDataFrame = trainingSet.smile().toDataFrame();
        this.randomForest = RandomForest.fit(this.formula,trainingSetDataFrame,this.properties);
    }

    public RegressionMetrics getSurrogateMetrics(){
        return this.randomForest.metrics();
    }
}

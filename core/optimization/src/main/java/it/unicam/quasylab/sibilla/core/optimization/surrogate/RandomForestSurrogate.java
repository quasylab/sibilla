package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import smile.data.DataFrame;
import smile.data.Tuple;
import smile.data.formula.Formula;
import smile.regression.RandomForest;
import smile.validation.RegressionMetrics;

import java.util.Properties;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.DEFAULT_COLUMN_RESULT_NAME;
/**
 * class representing a surrogate model, specifically the model is the random forest regression
 *
 * @author      Lorenzo Matteucci
 */
public class RandomForestSurrogate implements Surrogate{
    private RandomForest randomForest;
    private Properties properties;
    private final Formula formula;
    /**
     * The constructor of the random regression forest tree, default properties and formula
     * are used
     */
    public RandomForestSurrogate(){
        this(new Properties(),Formula.lhs(DEFAULT_COLUMN_RESULT_NAME));
    }

    /**
     * The constructor of the random regression forest tree
     *
     * @param properties the properties of the Random Forest Regression:
     *                   <ul>
     *                      <li> <code>Surrogate.RandomForest.ntrees</code> the number of trees.
     *                      <li> <code>Surrogate.RandomForest.mtry</code> the number of input variables
     *                         to be used to determine the decision at a node of the tree.
     *                         p/3 generally give good performance, where p is the number of variables.
     *                      <li> <code>surrogate.random.forest.depth</code> the maximum depth of the tree.
     *                      <li> <code>surrogate.random.forest.nodes</code> the maximum number of leaf
     *                         nodes in the tree.
     *                      <li> <code>surrogate.random.forest.size</code> the number of instances in
     *                      a node below which the tree will not split, nodeSize = 5 generally gives good results.
     *                      <li> <code>surrogate.random.forest.rate</code> the sampling rate for training tree.
     *                      1.0 means sampling with replacement. < 1.0 means sampling without replacement.
     *                   </ul>
     */
    public RandomForestSurrogate(Properties properties){
        this(properties,Formula.lhs(DEFAULT_COLUMN_RESULT_NAME));
    }
    /**
     * The constructor of the random regression forest tree
     *
     * @param properties the properties of the Random Forest Regression:
     *                   <ul>
     *                      <li> <code>Surrogate.RandomForest.ntrees</code> the number of trees.
     *                      <li> <code>Surrogate.RandomForest.mtry</code> the number of input variables
     *                         to be used to determine the decision at a node of the tree.
     *                         p/3 generally give good performance, where p is the number of variables.
     *                      <li> <code>surrogate.random.forest.depth</code> the maximum depth of the tree.
     *                      <li> <code>surrogate.random.forest.nodes</code> the maximum number of leaf
     *                         nodes in the tree.
     *                      <li> <code>surrogate.random.forest.size</code> the number of instances in
     *                      a node below which the tree will not split, nodeSize = 5 generally gives good results.
     *                      <li> <code>surrogate.random.forest.rate</code> the sampling rate for training tree.
     *                      1.0 means sampling with replacement. < 1.0 means sampling without replacement.
     *                   </ul>
     *  @param formula The formula interface symbolically specifies the predictors and the response.
     */
    public RandomForestSurrogate(Properties properties,Formula formula){
        this.properties = properties;
        this.formula = formula;
    }
    /**
     * Set properties
     *
     * @param properties the properties of the Random Forest Regression:
     *                   <ul>
     *                      <li> <code>Surrogate.RandomForest.ntrees</code> the number of trees.
     *                      <li> <code>Surrogate.RandomForest.mtry</code> the number of input variables
     *                         to be used to determine the decision at a node of the tree.
     *                         p/3 generally give good performance, where p is the number of variables.
     *                      <li> <code>surrogate.random.forest.depth</code> the maximum depth of the tree.
     *                      <li> <code>surrogate.random.forest.nodes</code> the maximum number of leaf
     *                         nodes in the tree.
     *                      <li> <code>surrogate.random.forest.size</code> the number of instances in
     *                      a node below which the tree will not split, nodeSize = 5 generally gives good results.
     *                      <li> <code>surrogate.random.forest.rate</code> the sampling rate for training tree.
     *                      1.0 means sampling with replacement. < 1.0 means sampling without replacement.
     *                   </ul>
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public double predict(Double[] x) {
        Tuple predictorTuple = Tuple.of(x,this.randomForest.schema());
        return this.randomForest.predict(predictorTuple);
    }

    @Override
    public void fit(TrainingSet trainingSet) {
        DataFrame trainingSetDataFrame = trainingSet.smile().toDataFrame();
        this.randomForest = RandomForest.fit(this.formula,
                trainingSetDataFrame,
                Integer.parseInt(this.properties.getProperty("surrogate.random.forest.trees", "1000")),
                Integer.parseInt(this.properties.getProperty("surrogate.random.forest.mtry", "0")),
                Integer.parseInt(this.properties.getProperty("surrogate.random.forest.depth", "200")),
                Integer.parseInt(this.properties.getProperty("surrogate.random.forest.nodes", String.valueOf(trainingSetDataFrame.size() / 5))),
                Integer.parseInt(this.properties.getProperty("surrogate.random.forest.size", "5")),
                Double.parseDouble(this.properties.getProperty("surrogate.random.forest.rate", "1.0"))
                );
    }

    public RegressionMetrics getMetrics(){
        return this.randomForest.metrics();
    }
}

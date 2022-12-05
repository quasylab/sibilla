package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import smile.base.cart.Loss;
import smile.data.DataFrame;
import smile.data.Tuple;
import smile.data.formula.Formula;
import smile.regression.GradientTreeBoost;

import java.util.Properties;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.DEFAULT_COLUMN_RESULT_NAME;

public class GradientTreeBoostSurrogate implements Surrogate{

    private GradientTreeBoost gradientTreeBoost;
    private TrainingSet trainingSet;
    private Formula formula;
    private Properties properties;

    private int ntrees;
    private Loss loss;
    private int maxDepth;
    private int maxNodes;
    private int nodeSize;
    private double shrinkage;
    private double subsample;

    private double fitTime;

    /**
     * The constructor of the Gradient Tree Boost, default properties and formula
     * are used
     */
    public GradientTreeBoostSurrogate(){
        this(new Properties(), Formula.lhs(DEFAULT_COLUMN_RESULT_NAME));
    }

    /**
     * The constructor of the Gradient Tree Boost
     *
     * @param properties the properties of the  Gradient Tree Boost:
     *                   <ul>
     *                      <li> <code>loss</code> loss function for regression. By default, least absolute deviation is employed for robust regression.
     *                      <li> <code>ntrees</code> the number of iterations (trees).
     *                      <li> <code>maxDepth</code> the maximum depth of the tree.
     *                      <li> <code>maxNodes</code> the maximum number of leaf nodes in the tree.
     *                      <li> <code>nodeSize</code> the number of instances in a node below which the tree will not split, setting nodeSize = 5 generally gives good results.
     *                      <li> <code>shrinkage</code> the shrinkage parameter in (0, 1] controls the learning rate of procedure.
     *                      <li> <code>subsample</code> the sampling fraction for stochastic tree boosting.
     *                   </ul>
     */
    public GradientTreeBoostSurrogate(Properties properties){
        this(properties,Formula.lhs(DEFAULT_COLUMN_RESULT_NAME));
    }
    /**
     * The constructor of the  Gradient Tree Boost
     *
     * @param properties the properties of the  Gradient Tree Boost:
     *                   <ul>
     *                      <li> <code>loss</code> loss function for regression. By default, least absolute deviation is employed for robust regression.
     *                      <li> <code>ntrees</code> the number of iterations (trees).
     *                      <li> <code>maxDepth</code> the maximum depth of the tree.
     *                      <li> <code>maxNodes</code> the maximum number of leaf nodes in the tree.
     *                      <li> <code>nodeSize</code> the number of instances in a node below which the tree will not split, setting nodeSize = 5 generally gives good results.
     *                      <li> <code>shrinkage</code> the shrinkage parameter in (0, 1] controls the learning rate of procedure.
     *                      <li> <code>subsample</code> the sampling fraction for stochastic tree boosting.
     *                   </ul>
     *  @param formula The formula interface symbolically specifies the predictors and the response.
     */
    public GradientTreeBoostSurrogate(Properties properties,Formula formula){
        this.properties = properties;
        this.formula = formula;
    }



    @Override
    public double predict(Double[] inputVector) {
        Tuple predictorTuple = Tuple.of(inputVector,this.gradientTreeBoost.schema());
        return this.gradientTreeBoost.predict(predictorTuple);
    }

    @Override
    public void fit(TrainingSet trainingSet) {
        this.trainingSet = trainingSet;
        DataFrame trainingSetDataFrame = this.trainingSet.smile().toDataFrame();

        this.ntrees = Integer.parseInt(this.properties.getProperty("smile.gbt.trees", "500"));
        this.loss = Loss.valueOf(this.properties.getProperty("smile.gbt.loss", "LeastAbsoluteDeviation"));
        this.maxDepth = Integer.parseInt(this.properties.getProperty("smile.gbt.max.depth", "20"));
        this.maxNodes = Integer.parseInt(this.properties.getProperty("smile.gbt.max.nodes", "6"));
        this.nodeSize = Integer.parseInt(this.properties.getProperty("smile.gbt.node.size", "5"));
        this.shrinkage = Double.parseDouble(this.properties.getProperty("smile.gbt.shrinkage", "0.05"));
        this.subsample = Double.parseDouble(this.properties.getProperty("smile.gbt.sample.rate", "0.7"));

        long start = System.nanoTime();
        this.gradientTreeBoost = gradientTreeBoost.fit(
                this.formula,
                trainingSetDataFrame,
                this.loss,
                this.ntrees,
                this.maxDepth,
                this.maxNodes,
                this.nodeSize,
                this.shrinkage,
                this.subsample
        );
        this.fitTime = (System.nanoTime() - start) / 1E6;
    }

    @Override
    public SurrogateMetrics getInSampleMetrics() {
        return new SurrogateMetrics(this,this.trainingSet,this.fitTime);
    }

    @Override
    public SurrogateMetrics getOutOfSampleMetrics(TrainingSet outOfSampleTrainingSet) {
        return new SurrogateMetrics(this,outOfSampleTrainingSet,this.fitTime);
    }




    @Override
    public String toString() {
        String str = "\n Model : Gradient Tree Boost ";
        str += "\n  - loss function     : "+this.loss.toString();
        str += "\n  - number of trees   : "+this.ntrees;
        str += "\n  - max tree  depth   : "+this.maxDepth;
        str += "\n  - max leaf nodes    : "+this.maxNodes;
        str += "\n  - node size         : "+this.nodeSize;
        str += "\n  - shrinkage         : "+this.shrinkage;
        str += "\n  - subsample         : "+this.subsample;
        return str;
    }

}

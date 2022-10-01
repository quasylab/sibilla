package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import smile.base.rbf.RBF;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.regression.RBFNetwork;
import smile.validation.RegressionMetrics;

import java.util.Properties;

import static it.unicam.quasylab.sibilla.core.optimization.Constants.DEFAULT_COLUMN_RESULT_NAME;

//TODO: learn of to predict with RBFNetwork and complete the surrogate and remove the suppress warnings
@SuppressWarnings("all")
public class RBFNetworkSurrogate implements Surrogate{


    private RBFNetwork rbfNetwork;
    private final Properties properties;
    private final Formula formula;


    public RBFNetworkSurrogate(){
        this(new Properties(),Formula.lhs(DEFAULT_COLUMN_RESULT_NAME));
    }

    public RBFNetworkSurrogate(Properties properties){
        this(properties,Formula.lhs(DEFAULT_COLUMN_RESULT_NAME));
    }

    public RBFNetworkSurrogate(Properties properties, Formula formula) {
        this.properties = properties;
        this.formula = formula;
    }



    @Override
    public double predict(Double[] x) {
        return 0;
    }

    @Override
    public void fit(TrainingSet trainingSet) {
        DataFrame trainingSetDataFrame = trainingSet.smile().toDataFrame();
        var y = trainingSetDataFrame.column(DEFAULT_COLUMN_RESULT_NAME).toDoubleArray();
        int[] columnIndexesX = new int[trainingSet.columnCount()-1];
        for (int i = 0; i < trainingSet.columnCount()-1; i++) {
            columnIndexesX[i] = i;
        }
        var x = trainingSetDataFrame.select(columnIndexesX).toArray();
        this.rbfNetwork = smile.regression.RBFNetwork.fit(x, y, RBF.fit(x, trainingSet.columnCount()-1), false);
    }

    @Override
    public RegressionMetrics getMetrics() {
        return null;
    }
}

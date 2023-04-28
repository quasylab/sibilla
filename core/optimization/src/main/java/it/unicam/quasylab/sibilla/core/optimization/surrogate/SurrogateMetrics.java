package it.unicam.quasylab.sibilla.core.optimization.surrogate;


import smile.validation.metric.*;
import tech.tablesaw.api.DoubleColumn;

import tech.tablesaw.api.Table;


import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;


public class SurrogateMetrics {

    double mse;
    double rmse;
    double rss;
    double mad;
    double rSquared;
    double fitTime;
    TrainingSet trainingSet;
    Table truthVsPredictedTable;

    double[] truth;
    double[] predicted;

    SurrogateModel surrogate;

    public SurrogateMetrics(SurrogateModel surrogate, TrainingSet trainingSet, double fitTime){

        this.trainingSet = trainingSet;
        this.truth = trainingSet.getResultColumn().asDoubleArray();
        this.predicted = getPredictedValues(surrogate,trainingSet);
        this.fitTime = fitTime;
        this.surrogate = surrogate;

        truthVsPredictedTable = Table.create(trainingSet.columns());
        truthVsPredictedTable.addColumns(DoubleColumn.create("predicted", predicted));

        this.mse = MSE.of(this.truth,this.predicted);
        this.rmse = RMSE.of(this.truth,this.predicted);
        this.rss = RSS.of(this.truth,this.predicted);
        this.mad = MAD.of(this.truth,this.predicted);
        this.rSquared = R2.of(this.truth,this.predicted);

    }



    private double[] getPredictedValues(SurrogateModel surrogate, TrainingSet trainingSet) {
        double[] predicted = new double[truth.length];
        ToDoubleFunction<Map<String,Double>> surrogateFunction = surrogate.getSurrogateFunction();
        List<Map<String,Double>> listOfRowsAsMaps = trainingSet.toMapList();
        for (int i = 0; i < truth.length; i++) {
            Map<String,Double> row = listOfRowsAsMaps.get(i);
            predicted[i] = surrogateFunction.applyAsDouble(row);
        }
        return predicted;
    }

    public double getMse() {
        return mse;
    }

    public double getRmse() {
        return rmse;
    }

    public double getRss() {
        return rss;
    }

    public double getMad() {
        return mad;
    }

    public double getrSquared() {
        return rSquared;
    }

    public double getFitTime() {
        return fitTime;
    }

    public TrainingSet getTrainingSet() {
        return trainingSet;
    }

    public String getFitTimeInSeconds(){
        return this.getFitTime() /1000 + " sec";
    }

    public Table getTruthVsPredictedTable(){
        return truthVsPredictedTable;
    }
    @Override
    public String toString() {
        return "Metrics : \n"+
                "MSE  : "+ getMse() +"\n"+
                "RMSE : "+ getRmse() +"\n"+
                "RSS  : "+ getRss() +"\n"+
                "MAD  : "+ getMad() +"\n"+
                "R^2  : " + getrSquared() +"\n"+
                "\n"+
                "Fit time : "+ getFitTimeInSeconds()+"\n"+
                "Training set size : "+ getTrainingSet().rowCount()+"\n";
    }
}

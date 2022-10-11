package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import smile.validation.metric.*;
import tech.tablesaw.api.Row;


public class SurrogateMetrics {

    double mse;
    double rmse;
    double rss;
    double mad;
    double rSquared;
    double fitTime;
    TrainingSet trainingSet;

    double[] truth;
    double[] predicted;

    Surrogate surrogate;

    public SurrogateMetrics(Surrogate surrogate, TrainingSet trainingSet, double fitTime){

        this.trainingSet = trainingSet;
        this.truth = trainingSet.getResultColumn().asDoubleArray();
        this.predicted = getPredictedValues(surrogate,trainingSet);
        this.fitTime = fitTime;
        this.surrogate = surrogate;

        this.mse = MSE.of(this.truth,this.predicted);
        this.rmse = RMSE.of(this.truth,this.predicted);
        this.rss = RSS.of(this.truth,this.predicted);
        this.mad = MAD.of(this.truth,this.predicted);
        this.rSquared = R2.of(this.truth,this.predicted);

    }

    private double[] getPredictedValues(Surrogate surrogate, TrainingSet trainingSet) {
        double[] predicted = new double[truth.length];
        for (int i = 0; i < truth.length; i++) {
            Row trainingSetRow = trainingSet.row(i);
            Double[] inputVector = new Double[trainingSetRow.columnCount()-1];
            for (int j = 0; j < inputVector.length; j++) {
                inputVector[j] = trainingSetRow.getDouble(j);
            }
            predicted[i] = surrogate.predict(inputVector);
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
        return this.fitTime /1000 + " sec";
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
                "Training set size : "+ getTrainingSet().rowCount()+"\n"
                +surrogate;
    }
}

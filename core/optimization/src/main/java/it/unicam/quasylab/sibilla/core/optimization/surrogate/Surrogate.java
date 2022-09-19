package it.unicam.quasylab.sibilla.core.optimization.surrogate;
import smile.validation.RegressionMetrics;

public interface Surrogate {
    double predict(Double[] x);

    void fit(TrainingSet trainingSet);

    RegressionMetrics getSurrogateMetrics();
}

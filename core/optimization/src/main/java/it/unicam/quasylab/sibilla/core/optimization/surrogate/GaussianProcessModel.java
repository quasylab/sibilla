package it.unicam.quasylab.sibilla.core.optimization.surrogate;

import it.unicam.quasylab.sibilla.core.optimization.sampling.SamplingTask;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.function.ToDoubleFunction;

import smile.math.kernel.GaussianKernel;
import smile.regression.GaussianProcessRegression;


public class GaussianProcessModel extends AbstractSurrogateModel{
    private GaussianProcessRegression<double[]> gpRegression;
    double noise;
    boolean normalize;
    double tolerance;
    int maxIterations;

    double sigma;
    double lo;
    double hi;

    public GaussianProcessModel(DataSet dataSet, double trainingPortion, Properties properties) {
        super(dataSet, trainingPortion, properties);
    }

    public GaussianProcessModel(ToDoubleFunction<Map<String, Double>> functionToBeSurrogate, SamplingTask samplingTask, HyperRectangle sampleSpace, int numberOfSamples, double trainingPortion, Properties properties) {
        super(functionToBeSurrogate, samplingTask, sampleSpace, numberOfSamples, trainingPortion, properties);
    }

    @Override
    double predict(Double[] inputVector) {
        return this.gpRegression.predict(Arrays.stream(inputVector)
                .mapToDouble(Double::doubleValue)
                .toArray());
    }

    double predict(Double[] inputVector, double[] estimation){
        return this.gpRegression.predict(Arrays.stream(inputVector)
                        .mapToDouble(Double::doubleValue)
                        .toArray(),
                estimation);
    }

    @Override
    void fit() {
        double[][] x = this.trainingSet.getDataMatrix();
        double[] y = this.trainingSet.getResultValues();
        long start = System.nanoTime();
        this.gpRegression = GaussianProcessRegression.fit(
                x,y,
                new GaussianKernel(this.sigma,this.lo,this.hi),
                this.noise,
                this.normalize,
                this.tolerance,
                this.maxIterations);
        this.fitTime = (System.nanoTime() - start) / 1E6;
    }

    @Override
    public void setProperties(Properties properties) {
        double SIGMA = 5.0;
        double LO = 1E-05;
        double HI = 1E5;


        double NOISE = 0.1; // Default value for noise
        boolean NORMALIZE = true; // Default value for normalize
        double TOLERANCE = 1E-5; // Default value for tolerance
        int MAX_ITERATIONS = 1024; // Default value for maxIterations
        this.noise = Double.parseDouble(properties.getProperty("gp.noise", NOISE + ""));
        this.normalize = Boolean.parseBoolean(properties.getProperty("gp.normalize", String.valueOf(NORMALIZE)));
        this.tolerance = Double.parseDouble(properties.getProperty("gp.tolerance", TOLERANCE + ""));
        this.maxIterations = Integer.parseInt(properties.getProperty("gp.maxIterations", MAX_ITERATIONS + ""));


        // GAUSSIAN KERNEL
        this.sigma = Double.parseDouble(properties.getProperty("gp.sigma", SIGMA + ""));
        this.lo = Double.parseDouble(properties.getProperty("gp.lo", LO + ""));
        this.hi = Double.parseDouble(properties.getProperty("gp.hi", HI + ""));


        this.properties = properties;
    }
}
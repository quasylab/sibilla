package it.unicam.quasylab.sibilla.tools.synthesis.surrogate;

import it.unicam.quasylab.sibilla.tools.synthesis.sampling.SamplingTask;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.HyperRectangle;
import smile.math.kernel.GaussianKernel;
import smile.regression.GaussianProcessRegression;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.function.ToDoubleFunction;


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
    public void fit() {
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
        double NOISE = 0.1;
        boolean NORMALIZE = true;
        double TOLERANCE = 1E-5;
        int MAX_ITERATIONS = 1024;

        this.sigma = Double.parseDouble(properties.getProperty("gp.sigma", SIGMA+""));
        properties.setProperty("gp.sigma", String.valueOf(this.sigma));

        this.lo = Double.parseDouble(properties.getProperty("gp.lo", LO+""));
        properties.setProperty("gp.lo", String.valueOf(this.lo));

        this.hi = Double.parseDouble(properties.getProperty("gp.hi", HI+""));
        properties.setProperty("gp.hi", String.valueOf(this.hi));

        this.noise = Double.parseDouble(properties.getProperty("gp.noise", NOISE+""));
        properties.setProperty("gp.noise", String.valueOf(this.noise));

        this.normalize = Boolean.parseBoolean(properties.getProperty("gp.normalize", String.valueOf(NORMALIZE)));
        properties.setProperty("gp.normalize", String.valueOf(this.normalize));

        this.tolerance = Double.parseDouble(properties.getProperty("gp.tolerance", TOLERANCE+""));
        properties.setProperty("gp.tolerance", String.valueOf(this.tolerance));

        this.maxIterations = Integer.parseInt(properties.getProperty("gp.maxIterations", MAX_ITERATIONS+""));
        properties.setProperty("gp.maxIterations", String.valueOf(this.maxIterations));

        this.properties = properties;
    }
}
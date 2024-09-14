package it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads;

import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.OptimizationTask;
import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads.mesh.Mesh;
import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads.poll.PollMethod;
import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads.search.LHSSearchMethod;
import it.unicam.quasylab.sibilla.tools.synthesis.optimizationalgorithm.mads.search.SearchMethod;
import it.unicam.quasylab.sibilla.tools.synthesis.sampling.interval.HyperRectangle;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public abstract class AbstractMADSTask implements OptimizationTask {

    protected HyperRectangle searchSpace;

    protected BarrierFunction barrierFunction;
    protected Map<String,Double> minimizingParametersFound;
    protected double minimumFound;

    protected int numberOfSearchPoint;

    protected double deltaMesh;
    protected double deltaPoll;

    protected double tau;
    protected double epsilon;
    protected int maxIteration;

    protected SearchMethod searchMethod;
    protected PollMethod pollMethod;

    protected Mesh mesh;
    protected boolean opportunistic;
    protected Random random;

    public int iteration;

    // needed for Orthogonal
    int t_0;
    int t;
    int t_max;
    double minDeltaPoll;


    @Override
    public Map<String, Double> minimize(ToDoubleFunction<Map<String, Double>> objectiveFunction, HyperRectangle searchSpace, List<Predicate<Map<String, Double>>> constraints, Properties properties, Long seed) {
        this.random = new Random(seed);
        this.searchSpace = searchSpace;
        this.searchSpace.setSeeds(seed);
        constraints.addAll(getSearchSpaceAsConstraintList(searchSpace));
        this.setProperties(properties);
        this.barrierFunction = new BarrierFunction(objectiveFunction,constraints);
        Map<String,Double> randomPointInSearchSpace = this.searchSpace.getRandomValue();
        setAsIncumbentMinimum(randomPointInSearchSpace,this.barrierFunction.evaluate(randomPointInSearchSpace));
        this.mesh = new Mesh(this.searchSpace,this.minimizingParametersFound,this.deltaMesh);

        this.t_0 = getNthPrimeNumber(searchSpace.getDimensionality());
        this.t = this.t_0;
        this.t_max = this.t_0;
        this.minDeltaPoll = Double.MAX_VALUE;
        performAlgorithm();

        return minimizingParametersFound;
    }

    @Override
    public void setProperties(Properties properties) {

        double DELTA_MESH = 1.0;
        double DELTA_POLL = 1.0;
        double TAU = 1.0 / 4.0;
        double EPSILON = 1.0E-50;
        int ITERATION = 500;
        int SEARCH_POINTS = 20;
        boolean OPPORTUNISTIC = false;

        this.searchMethod = getSearchMethod();
        this.deltaMesh = Double.parseDouble(properties.getProperty("mads.delta_mesh", Double.toString(DELTA_MESH)));
        this.deltaPoll = Double.parseDouble(properties.getProperty("mads.delta_poll", Double.toString(DELTA_POLL)));
        this.tau = Double.parseDouble(properties.getProperty("mads.tau", Double.toString(TAU)));
        this.epsilon = Double.parseDouble(properties.getProperty("mads.epsilon", Double.toString(EPSILON)));
        this.maxIteration = Integer.parseInt(properties.getProperty("mads.iteration", Integer.toString(ITERATION)));
        this.numberOfSearchPoint = Integer.parseInt(properties.getProperty("mads.search_points",Integer.toString(SEARCH_POINTS)));
        this.opportunistic = Boolean.getBoolean(properties.getProperty("mads.opportunistic",  Boolean.toString(OPPORTUNISTIC)));
    }


    public SearchMethod getSearchMethod(){
        return new LHSSearchMethod();
    }

    protected void performAlgorithm(){
        this.iteration = 0;
        while (!(this.deltaPoll < this.epsilon) && !(this.iteration >= this.maxIteration) ){
            parameterUpdate();
            search();
            this.iteration++;
        }
    }


    protected void parameterUpdate(){
        setDeltaMesh(Math.min( deltaPoll , Math.pow(deltaPoll,2)));
    }


    protected void search(){
        List<Map<String,Double>> S = searchMethod.generateTrialPoints(this.numberOfSearchPoint,mesh,this.random);
        boolean searchSuccess = false;
        for (Map<String,Double> point: S ) {
            double currentEvaluation = barrierFunction.evaluate(point);
            if(currentEvaluation < minimumFound){
                this.setAsIncumbentMinimum(point,currentEvaluation);
                this.setDeltaPoll(Math.pow( this.tau , -1.0) * this.deltaPoll);
                searchSuccess = true;
                if(opportunistic)
                    break;
            }
        }
        if (!searchSuccess)
            poll();
    }

    protected void poll(){
        if(this.deltaPoll==minDeltaPoll)
            this.t = this.getL() + t_0;
        else
            this.t = 1 + this.t_max;
        List<Map<String,Double>> polledPoints = this.pollMethod.getPolledPoints( this);
        boolean pollSucceed = false;
        for (Map<String,Double> polledPoint: polledPoints) {
            double currentEvaluation = barrierFunction.evaluate(polledPoint);
            if(currentEvaluation < minimumFound){
                this.setAsIncumbentMinimum(polledPoint,currentEvaluation);
                pollSucceed = true;
                if(opportunistic)
                    break;
            }
        }

        if(pollSucceed)
            this.setDeltaPoll( Math.pow(tau, -1.0) * this.deltaPoll);
        else
            this.setDeltaPoll( tau * this.deltaPoll);
        this.t_max = Math.max(this.t, this.t_max);
    }


    protected void setAsIncumbentMinimum( Map<String,Double> parameters, double evaluation){
        this.minimizingParametersFound = parameters;
        this.minimumFound = evaluation;
    }

    protected void setDeltaMesh(double deltaMesh){
        if(deltaMesh == 0.0)
            deltaMesh = Double.MIN_VALUE;
        this.deltaMesh = deltaMesh;
        this.mesh.setDeltaMesh(deltaMesh);
    }

    protected void setDeltaPoll(double deltaPoll){
        this.deltaPoll = deltaPoll;
        if(this.deltaPoll < minDeltaPoll)
            minDeltaPoll = this.deltaPoll;

    }

    private int getNthPrimeNumber(int n){
        int[] primeNumbers = new int[]{2, 3, 5, 7, 11, 13, 17, 19, 23, 29,
                31, 37, 41, 43, 47, 53, 59, 61, 67, 71,
                73, 79, 83, 89, 97, 101, 103, 107, 109, 113,
                127, 131, 137, 139, 149, 151, 157, 163, 167, 173,
                179, 181, 191, 193, 197, 199, 211, 223, 227, 229,
                233, 239, 241, 251, 257, 263, 269, 271, 277, 281,
                283, 293, 307, 311, 313, 317, 331, 337, 347, 349,
                353, 359, 367, 373, 379, 383, 389, 397, 401, 409,
                419, 421, 431, 433, 439, 443, 449, 457, 461, 463,
                467, 479, 487, 491, 499, 503, 509, 521, 523, 541};
        return primeNumbers[n-1];
    }

    public int getL(){
        return (int) ((-1)*log(this.deltaPoll,4));
    }

    public int getT(){
        return this.t;
    }

    public Random getRandomInstance(){
        return this.random;
    }

    public Map<String,Double> currentBestFound(){
        return this.minimizingParametersFound;
    }

    @SuppressWarnings("SameParameterValue")
    private double log(double value, double base) {
        return Math.log(value)/Math.log(base);
    }

    public int getDimensionality(){
        return this.searchSpace.getDimensionality();
    }

    public double getDeltaPoll(){
        return this.deltaPoll;
    }

}

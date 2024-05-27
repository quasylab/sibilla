package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.OptimizationTask;
import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.mesh.Mesh;
import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.poll.PollMethod;
import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.search.LHSSearchMethod;
import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.search.SearchMethod;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;

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
    public boolean terminated;



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

        performAlgorithm();

        return minimizingParametersFound;
    }

    @Override
    public void setProperties(Properties properties) {

        double DELTA_MESH = 1.0;
        double DELTA_POLL = 1.0;
        double TAU = 4.0;
        double EPSILON = Double.POSITIVE_INFINITY;
        int ITERATION = 500;
        int SEARCH_POINTS = 20;
        boolean OPPORTUNISTIC = false;

        this.searchMethod = getSearchMethod();
        this.deltaMesh = Double.parseDouble(properties.getProperty("mads.delta_mesh", Double.toString(DELTA_MESH)));
        this.deltaMesh = Double.parseDouble(properties.getProperty("mads.delta_poll", Double.toString(DELTA_POLL)));
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
        while (iteration < maxIteration){
            parameterUpdate();
            search();
            this.iteration++;
        }
    }

    protected void parameterUpdate(){
        this.deltaPoll = Math.min( deltaMesh , Math.pow(deltaMesh,2));
    }

    protected void search(){
        List<Map<String,Double>> S = searchMethod.generateTrialPoints(this.numberOfSearchPoint,mesh,this.random);
        boolean searchSuccess = false;
        for (Map<String,Double> point: S ) {
            double currentEvaluation = barrierFunction.evaluate(point);
            if(currentEvaluation < minimumFound){
                this.setAsIncumbentMinimum(point,currentEvaluation);
                this.setDeltaMesh(Math.pow( this.tau , -1.0) * this.deltaMesh);
                searchSuccess = true;
                if(opportunistic)
                    break;
            }
        }
        if (!searchSuccess)
            poll();
        else
            termination();
    }

    protected void poll(){
        List<Map<String,Double>> polledPoints = this.pollMethod.getPolledPoints( this.minimizingParametersFound, this.deltaPoll, this.random);
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
            this.setDeltaMesh( Math.pow(tau, -1.0) * this.deltaMesh);
        else
            this.setDeltaMesh( tau * this.deltaMesh);
    }

    protected void termination(){
        this.terminated = ((this.deltaMesh >= epsilon) || (this.iteration < this.maxIteration));
    }


    protected void setAsIncumbentMinimum( Map<String,Double> parameters, double evaluation){
        this.minimizingParametersFound = parameters;
        this.minimumFound = evaluation;
    }

    protected void setDeltaMesh(double deltaMesh){
        this.deltaMesh = deltaMesh;
        this.mesh.setDeltaMesh(deltaMesh);
    }




}

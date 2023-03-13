package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads;

import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.poll.PollMethod;
import it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads.search.SearchMethod;
import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;

import java.util.*;
import java.util.function.ToDoubleFunction;

public class AbstractMADS {

    // common optimization variables

    protected HyperRectangle searchSpace;
    protected ToDoubleFunction<Map<String,Double>> objectiveFunction;

    // imminent solution
    protected Map<String,Double> minimizingPointsUntilNow;
    protected double minimumFoundUntilNow;

    // properties

    int numberSearchPoint;


    // algorithm parameter
    protected double deltaMesh;
    protected int[][] D;
    protected double tau; //mesh size adjustment;
    protected double epsilon; //stopping tolerance;
    protected int iterationCounter;

    protected SearchMethod searchMethod;
    protected PollMethod pollMethod;
    protected Mesh mesh;


    protected void initialisation(){
        deltaMesh = 1;
        //D = pollMethod.getPositiveBasis(searchSpace.getDimensionality(),deltaMesh);
        tau = 1.0/2.0;
        epsilon = 0.01;
        iterationCounter = 0;

        // the minimum of the function is initialized as a random point in the search space
        this.minimizingPointsUntilNow = searchSpace.getRandomValue();
        this.minimumFoundUntilNow = objectiveFunction.applyAsDouble(this.minimizingPointsUntilNow);
        
    }

    protected void search(){
        List<Map<String,Double>> S = searchMethod.generateTrialPoints(this.numberSearchPoint,mesh);
        boolean searchStepSuccess = false;
        for (Map<String,Double> point: S ) {
            double currentEvaluation = objectiveFunction.applyAsDouble(point);
            if(currentEvaluation < minimumFoundUntilNow){
                minimizingPointsUntilNow = point;
                minimumFoundUntilNow = currentEvaluation;
                searchStepSuccess= true;
            }
        }
        if(searchStepSuccess)
            termination();
        else
            poll();

    }

    protected void poll(){
        //List<Map<String,Double>> S = pollMethod.getPolledPoints();
    }

    protected void termination(){

    }





}

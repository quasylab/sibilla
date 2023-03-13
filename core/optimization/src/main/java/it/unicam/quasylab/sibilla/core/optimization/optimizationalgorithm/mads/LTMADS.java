package it.unicam.quasylab.sibilla.core.optimization.optimizationalgorithm.mads;

import it.unicam.quasylab.sibilla.core.optimization.sampling.interval.HyperRectangle;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("all")
//TODO
public class LTMADS {

    private final Function<Map<String,Double>,Double> functionToOptimize;
    private final List<Predicate<Map<String,Double>>> constraints;
    private final HyperRectangle searchSpace;



    private Map<String, Double> incumbentSolution;
    private double deltaMesh;
    private double deltaPoll;
    private double tau;
    private int wMinus;
    private int wPlus;


    private HyperRectangle meshSpace;



    public LTMADS(Function<Map<String,Double>,Double> functionToOptimize,
                                     List<Predicate<Map<String,Double>>> constraints,
                                     HyperRectangle searchSpace,
                                     Properties properties){

        this.functionToOptimize = functionToOptimize;
        this.constraints = Optional.ofNullable(constraints).orElse(new ArrayList<>());
        this.searchSpace = searchSpace;
    }



}
